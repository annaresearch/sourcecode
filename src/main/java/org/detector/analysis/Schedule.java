package org.detector.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;
import org.detector.util.TriTuple;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.detector.Main.sdf;
import static org.detector.Main.startTimeStamp;

import static org.detector.analysis.TypeWrapper.fail2reset;
import static org.detector.analysis.Wrapper.getChildrenNodes;
import static org.detector.report.PMD_Report.errorPMDPaths;
import static org.detector.util.Utility.ANNOTATION_LIBRARY_PATH;
import static org.detector.util.Utility.CHECKSTYLE_MUTATION;
import static org.detector.util.Utility.COMPILE;
import static org.detector.util.Utility.DEBUG;
import static org.detector.util.Utility.DIFFERENTIAL_TESTING;
import static org.detector.util.Utility.EQUAL_ANNA_TESTING;
import static org.detector.util.Utility.EVALUATION_PATH;
import static org.detector.util.Utility.INFER_MUTATION;
import static org.detector.util.Utility.LOW_THREE;

import static org.detector.util.Utility.OFFSET_IMPACT;
import static org.detector.util.Utility.PMD_MUTATION;
import static org.detector.util.Utility.PROJECT_PATH;
import static org.detector.util.Utility.Path2Last;
import static org.detector.util.Utility.SEED_PATH;
import static org.detector.util.Utility.SONARQUBE_MUTATION;
import static org.detector.util.Utility.SPOTBUGS_MUTATION;
import static org.detector.util.Utility.TOOL_PATH;
import static org.detector.util.Utility.annaClassFolderPath;
import static org.detector.util.Utility.compactIssues;
import static org.detector.util.Utility.compileJavaAnnotation;
import static org.detector.util.Utility.fail2getReports;
import static org.detector.util.Utility.failedCompilation;
import static org.detector.util.Utility.failedInvocation;
import static org.detector.util.Utility.filepath2annotation;
import static org.detector.util.Utility.getDirectFilenamesFromFolder;
import static org.detector.util.Utility.getFilenamesFromFolder;
import static org.detector.util.Utility.mutant2seed;
import static org.detector.util.Utility.readLines;
import static org.detector.util.Utility.ruleCheckResultPath;
import static org.detector.util.Utility.sep;
import static org.detector.util.Utility.succ2getReports;
import static org.detector.util.Utility.sumCompilation;
import static org.detector.util.Utility.sumInvocation;
import static org.detector.util.Utility.writeLinesToFile;

public class Schedule {

    public static List<String> fail2compile = new ArrayList<>();
    public static List<String> fail2deannotation = new ArrayList<>();
    public static List<String> failedSrcMutant = new ArrayList<>();

    public static HashMap<String, HashMap<String, List<String>>> subFolder2index = new HashMap<>();

    private static final Schedule schedule = new Schedule();
    private Schedule() {}

    public static Schedule getInstance() {
        return schedule;
    }

    public static List<AnnotationWrapper> getAnnotations() {
        System.out.println("Search Annotation Lib in: " + ANNOTATION_LIBRARY_PATH);
        List<String> annotationPaths = getFilenamesFromFolder(ANNOTATION_LIBRARY_PATH, true);
        List<AnnotationWrapper> annotations = new ArrayList<>();
        for (String annotationPath : annotationPaths) {
            if (!annotationPath.contains(".java")) {
                System.err.println("Wrong File: " + annotationPath);
                System.exit(-1);
            }
            List<AnnotationWrapper> wrappers = AnnotationWrapper.annotationParser(annotationPath);
            for (AnnotationWrapper wrapper : wrappers) {
                if (wrapper.getTargets().size() == 0) {
                    continue;
                }
                if (wrapper.getRetention() == -1) {
                    continue;
                }
                if (wrapper.getFilePath().contains("android" + sep)) {
                    continue;
                }
                annotations.add(wrapper);
            }
        }
        return annotations;
    }

    public List<String> getSeedPaths() {
        System.out.println("Get Seed From: " + SEED_PATH);
        List<String> seedPaths = new ArrayList<>();
        List<String> subFolderPaths = getDirectFilenamesFromFolder(SEED_PATH, true);
        for (String subFolderPath : subFolderPaths) {
            List<String> filePaths = getFilenamesFromFolder(subFolderPath, true);
            if (filePaths.size() > 3 && LOW_THREE) {
                for (int i = 0; i < 3; i++) {
                    seedPaths.add(filePaths.get(i));
                }
            } else {
                seedPaths.addAll(filePaths);
            }
        }
        return seedPaths;
    }

    public static int locateConstructor(String className, List<String> lines) {
        int lineNumber = -1;
        for (int i = 0; i < lines.size() - 3; i++) {
            String line = lines.get(i);
            if (line.contains("public " + className + "()")
                    || line.strip().contains(className + "()")) {
                if(lines.get(i + 1).contains("super();")
                        && lines.get(i + 2).contains("}")) {
                    lineNumber = i;
                    break;
                }
            }
        }
        return lineNumber;
    }

    public static HashSet<String> locateNoArgsConstructor(TypeWrapper wrapper) {
        HashSet<String> names = new HashSet<>();
        for(TypeDeclaration type : wrapper.getTypes()) {
            List<ASTNode> nodes = getChildrenNodes(type);
            for(int i = 0; i < nodes.size(); i++) {
                ASTNode node = nodes.get(i);
                if(node instanceof MethodDeclaration) {
                    MethodDeclaration method = (MethodDeclaration) node;
                    if(method.isConstructor() && method.parameters().isEmpty()) {
                        names.add(method.getName().getFullyQualifiedName());
                    }
                }
            }
        }
        return names;
    }

    public static HashSet<String> locateNoArgsConstructor(String filePath) {
        TypeWrapper wrapper = new TypeWrapper(filePath);
        HashSet<String> names = locateNoArgsConstructor(wrapper);
        return names;
    }

    public static int locateNoArgsConstructor(List<String> lines, String fileName) {
        int ok = -1;
        for (int i = 0; i < lines.size() - 3; i++) {
            String line = lines.get(i);
            if (line.contains("public " + fileName + "()")
                    || line.strip().contains(fileName + "()")) {
                    if(lines.get(i + 1).contains("super();")
                            && lines.get(i + 2).contains("}")) {
                        ok = i;
                        break;
                    }
            }
        }
        return ok;
    }

    public static int failDeAnnotation = 0, sumDeAnnotation = 0;
    public void deAnnotation(TypeWrapper initWrapper) { // Recover annotation semantics
        sumDeAnnotation++;
        File deFolder;
        if(PMD_MUTATION) {
            deFolder = new File(EVALUATION_PATH + sep + "decompile" + sep + initWrapper.getFolderName() + sep + initWrapper.getFileName());
        } else {
            deFolder = new File(EVALUATION_PATH + sep + "decompile" + sep + initWrapper.getFolderName());
        }
        boolean hasDeAnnotated = compileJavaAnnotation(initWrapper.getFolderPath(), initWrapper.getFileName() + ".java", deFolder);
        if (!hasDeAnnotated) {
            if(deFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(deFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            fail2deannotation.add(initWrapper.getFilePath());
            failDeAnnotation++;
            return;
        }
        List<String> filePaths = getFilenamesFromFolder(deFolder.getAbsolutePath(), true);
        for(String filePath : filePaths) {
            List<String> dstLines = readLines(filePath);
            for (int i = dstLines.size() - 1; i >= 0; i--) { // Remove useless lines generated by de-annotation
                String str = dstLines.get(i);
                AnnotationWrapper annotation = initWrapper.getInsertedAnnotationWrapper();
                if (str.contains("@Override()")) {
                    str = str.replace("@Override()", "@Override");
                    dstLines.set(i, str);
                }
                if (str.contains("/*synthetic*/")) {
                    str = str.replace("/*synthetic*/", "");
                    dstLines.set(i, str);
                }
                if (str.contains("@java.lang.SuppressWarnings(value = \"all\")")
                        || str.contains("@java.lang.SuppressWarnings(\"all\")")
                        || str.contains("@SuppressWarnings(value = {\"all\"})")
                        || str.contains("@SuppressWarnings(value = \"all\")")
                        || (annotation.isSourceLevel() && str.contains("@" + annotation.getName()))) {
                    dstLines.remove(i);
                }
                if (i >= 2 && str.trim().contains("}")
                        && dstLines.get(i - 1).trim().equals("super();")
                        && dstLines.get(i - 2).trim().equals("() {")) {
                    dstLines.remove(i); // used to delete the constructor of anonymous class -> This is a grammar bug...
                    dstLines.remove(i - 1);
                    dstLines.remove(i - 2);
                    i -= 2;
                }
            }
            writeLinesToFile(dstLines, filePath);
        }
    }

    public static HashMap<String, HashMap<String, Integer>> path2bugNum = new HashMap<>();

    public void readImpactFile() {
        String path = null;
        if(PMD_MUTATION) {
            path = PROJECT_PATH + sep + "offset" + sep + "PMD_OFFSET.txt";
        }
        if(SPOTBUGS_MUTATION) {
            path = PROJECT_PATH + sep + "offset" + sep + "SPOTBUGS_OFFSET.txt";
        }
        if(CHECKSTYLE_MUTATION) {
            path = PROJECT_PATH + sep + "offset" + sep + "CHECKSTYLE_OFFSET.txt";
        }
        if(INFER_MUTATION) {
            path = PROJECT_PATH + sep + "offset" + sep + "INFER_OFFSET.txt";
        }
        if(SONARQUBE_MUTATION) {
            path = PROJECT_PATH + sep + "offset" + sep + "SONARQUBE_OFFSET.txt";
        }
        if(path == null) {
            System.out.println("No Impact File Found!");
            System.exit(-1);
        }
        System.out.println("Parse Impact File: " + path);
        List<String> lines = readLines(path);
        if(!lines.get(0).startsWith("Path: ")) {
            System.err.println("Error Impact File!");
            System.exit(-1);
        }
        for(int i = 0; i < lines.size();) {
            String line = lines.get(i);
            if(line.startsWith("Path: ")) {
                String filePath = line.substring(6);
                path2bugNum.put(filePath, new HashMap<>());
                i++;
                while(i < lines.size() && !lines.get(i).startsWith("Path:")) {
                    String[] tokens = lines.get(i).split(",");
                    String bugType = tokens[0];
                    Integer number = Integer.parseInt(tokens[1]);
                    path2bugNum.get(filePath).put(bugType, number);
                    i++;
                }
            }
        }
    }

    public void runDifferentialTesting() {
        if(OFFSET_IMPACT) {
            readImpactFile();
        }
        List<AnnotationWrapper> annotations = getAnnotations();
        List<String> seedPaths = getSeedPaths();
        System.out.println("Annotation Size: " + annotations.size());
        System.out.println("Seed Size: " + seedPaths.size());
        for (int i = 0; i < seedPaths.size(); i++) {
            String seedPath = seedPaths.get(i);
            TypeWrapper seedWrapper = new TypeWrapper(seedPath);
            for(int j = 0; j < annotations.size(); j++) {
                AnnotationWrapper annotation = annotations.get(j);
                List<TypeWrapper> mutants = seedWrapper.transformByAnnotationInsertion(annotation);
                for (int k = 0; k < mutants.size(); k++) {
                    TypeWrapper mutant = mutants.get(k);
                    filepath2annotation.put(mutant.getFilePath(), mutant.getInsertedAnnotationWrapper().getFullName());
                    deAnnotation(mutant);
                }
            }
        }
        long execTime = System.currentTimeMillis() - startTimeStamp;
        long minutes = (execTime / 1000) / 60, seconds = (execTime / 1000) % 60;
        System.out.format("DeAnnotation execution time: %d min(s) %d sec(s).\n", minutes, seconds);
        DiffChecker.run();
    }

    public List<String> getAnnotationFilepath(String targetFolder) {
        List<String> annotationPaths = new ArrayList<>();
        List<String> filePaths = getFilenamesFromFolder(targetFolder, true);
        for (String filePath : filePaths) {
            if (filePath.endsWith(".java") && !filePath.contains("src" + sep + "test")) {
                List<String> lines = readLines(filePath);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.contains("@interface")) {
                        annotationPaths.add(filePath);
                        break;
                    }
                }
            }
        }
        return annotationPaths;
    }

    public void filter(String targetFolder) {
        List<String> paths = getFilenamesFromFolder(targetFolder, true);
        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                System.err.println("Cannot Be a Directory!");
            }
            if (!path.endsWith(".java") && !path.contains(".java")) {
                try {
                    Files.deleteIfExists(Paths.get(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> preprocessAnnotationLibs() {
        List<String> annotationLibPaths = new ArrayList<>();
        File pathFile = new File("." + sep + "AnnotationLibPaths.txt");
        if (pathFile.exists()) {
            System.out.println("Read Annotation Paths from text...");
            List<String> paths = readLines(pathFile.getAbsolutePath());
            if (paths.get(0).equals(ANNOTATION_LIBRARY_PATH)) {
                for (int i = 1; i < paths.size(); i++) {
                    annotationLibPaths.add(paths.get(i));
                }
            } else {
                System.out.println("Stored text file is not matched.");
            }
        }
        if (annotationLibPaths.size() == 0) {
            System.out.println("Generate text file to store Annotation Library Paths...");
            annotationLibPaths = getAnnotationFilepath(ANNOTATION_LIBRARY_PATH);
            List<String> contents = new ArrayList<>();
            contents.add(ANNOTATION_LIBRARY_PATH);
            contents.addAll(annotationLibPaths);
            writeLinesToFile(contents, pathFile.getAbsolutePath());
        }
        return annotationLibPaths;
    }

    public void injectAnnotation(List<TypeWrapper> seedWrappers, List<AnnotationWrapper> annotations) {
        ArrayDeque<TypeWrapper> que = new ArrayDeque<>();  // que is used to generate mutants
        que.addAll(seedWrappers);
        while (!que.isEmpty()) {
            TypeWrapper head = que.pollFirst();
            if (EQUAL_ANNA_TESTING) {
                head.transformForEqualAnna(annotations);
            } else {
                for (int i = 0; i < annotations.size(); i++) {
                    AnnotationWrapper annotation = annotations.get(i);
                    head.transformByAnnotationInsertion(annotation);
                }
            }
        }
    }

    public void runInjectionChecker() {
        String mockAnnotationPath = TOOL_PATH + sep + "MockAnnotation.java";
        System.out.println("Mock Annotation Path: " + mockAnnotationPath);
        List<AnnotationWrapper> annotations = AnnotationWrapper.annotationParser(mockAnnotationPath);
        List<String> seedPaths = getSeedPaths();
        InjectChecker.run(seedPaths, annotations);
    }

    public static Map<String, String> fullName2folderName = new HashMap<>();

    public Map<String, HashMap<String, AnnotationWrapper>> equalAnnaFinder() {
        Map<String, HashMap<String, AnnotationWrapper>> anna2path = new HashMap<>();
        System.out.println("Maven Class Lib Path: " + annaClassFolderPath);
        List<String> subClassFolderPaths = getDirectFilenamesFromFolder(annaClassFolderPath, true);
        for (String subClassFolderPath : subClassFolderPaths) {
            String subFolderName = Path2Last(subClassFolderPath);
            List<String> classPaths = getFilenamesFromFolder(subClassFolderPath, true);
            for (String classPath : classPaths) {
                try {
                    if (classPath.endsWith(".class") && !classPath.contains("$")) {
                        ClassParser parser = new ClassParser(classPath);
                        JavaClass clazz = parser.parse();
                        if (clazz.isAnnotation()) {
                            boolean isDeprecated = false;
                            for (AnnotationEntry entry : clazz.getAnnotationEntries()) {
                                if ("@Ljava/lang/Deprecated;".equals(entry.toString())) {
                                    isDeprecated = true;
                                    break;
                                }
                            }
                            if (isDeprecated) {
                                continue;
                            }
                            AnnotationWrapper wrapper = new AnnotationWrapper(clazz, classPath);
                            if (wrapper.getRetention() == -1 || wrapper.getTargets().size() == 0) {
                                continue;
                            }
                            fullName2folderName.put(wrapper.getFullName(), subFolderName);
                            HashMap<String, AnnotationWrapper> name2wrapper;
                            String key = wrapper.getAnnotationName() + wrapper.getRetention();
                            if (!anna2path.containsKey(key)) {
                                name2wrapper = new HashMap<>();
                                anna2path.put(key, name2wrapper);
                            } else {
                                name2wrapper = anna2path.get(key);
                            }
                            if (!name2wrapper.containsKey(wrapper.toString())) {
                                name2wrapper.put(wrapper.toString(), wrapper);
                            }
                        }
                    }
                } catch (IOException e) {
                }
            }
        }
        int sum = 0;
        for(HashMap<String, AnnotationWrapper> value : anna2path.values()) {
            sum += value.size();
        }
        System.out.println("Anna Sum=" + sum);
        int annaCnt = 0;
        List<String> removedAnnotations = new ArrayList<>();
        for (Map.Entry<String, HashMap<String, AnnotationWrapper>> entry : anna2path.entrySet()) {
            annaCnt += entry.getValue().size();
            if (entry.getValue().size() > 1) {
                AnnotationWrapper[] wrappers = entry.getValue().values().toArray(new AnnotationWrapper[0]);
                if (wrappers.length == 2) {
                    Set<ElementType> list1 = wrappers[0].getTargets();
                    Set<ElementType> list2 = wrappers[1].getTargets();
                    if (!list1.equals(list2)) {
                        removedAnnotations.add(entry.getKey());
                    }
                }
                if (wrappers.length > 2) {
                    HashMap<Integer, List<Set<ElementType>>> hash2set = new HashMap<>();
                    for (AnnotationWrapper wrapper : wrappers) {
                        if (!hash2set.containsKey(wrapper.getTargets().hashCode())) {
                            hash2set.put(wrapper.getTargets().hashCode(), new ArrayList<>());
                        }
                        List<Set<ElementType>> setList = hash2set.get(wrapper.getTargets().hashCode());
                        setList.add(wrapper.getTargets());
                    }
                    int maxLen = Integer.MIN_VALUE, maxKey = -1;
                    for (Map.Entry<Integer, List<Set<ElementType>>> e : hash2set.entrySet()) {
                        if (e.getValue().size() > maxLen) {
                            maxKey = e.getKey();
                            maxLen = e.getValue().size();
                        }
                    }
                    List<String> removedSubAnnotation = new ArrayList<>();
                    if (maxLen < 2) {
                        removedAnnotations.add(entry.getKey());
                    } else {
                        for (Map.Entry<String, AnnotationWrapper> name2wrapper : entry.getValue().entrySet()) {
                            if (name2wrapper.getValue().getTargets().hashCode() != maxKey) {
                                removedSubAnnotation.add(name2wrapper.getKey());
                            }
                        }
                        for (String annotation : removedSubAnnotation) {
                            entry.getValue().remove(annotation);
                        }
                    }
                }

            } else {
                removedAnnotations.add(entry.getKey());
            }
        }
        for (String removedAnnotation : removedAnnotations) {
            anna2path.remove(removedAnnotation);
        }
        System.out.println("First stage annotation number: " + annaCnt);
        annaCnt = 0;
        for (Map.Entry<String, HashMap<String, AnnotationWrapper>> entry : anna2path.entrySet()) {
            annaCnt += entry.getValue().size();
            if(DEBUG) {
                System.out.println(entry.getKey());
                for(Map.Entry<String, AnnotationWrapper> subEntry : entry.getValue().entrySet()) {
                    AnnotationWrapper wrapper = subEntry.getValue();
                    System.out.println(wrapper.getFullName());
                }
            }
        }
        System.out.println("Second stage annotation number: " + annaCnt);
        System.out.println("Anna Pair Cnt: " + anna2path.size());
        return anna2path;
    }

    public void generateEqualAnnaMutants(Map<String, HashMap<String, AnnotationWrapper>> anna2path) {
        List<String> seedPaths = getSeedPaths();
        System.out.println("Seed Size: " + seedPaths.size());
        List<TypeWrapper> seedWrappers = new ArrayList<>();
        for (int i = 0; i < seedPaths.size(); i++) {
            String seedPath = seedPaths.get(i);
            TypeWrapper initSeed = new TypeWrapper(seedPath);
            seedWrappers.add(initSeed);
        }
        List<String> subFolderNames = getDirectFilenamesFromFolder(SEED_PATH, false);
        for (String subFolderName : subFolderNames) {
            if (subFolderName.startsWith(".")) {
                continue;
            }
            subFolder2index.put(subFolderName, new HashMap<>());
        }
        for (HashMap<String, AnnotationWrapper> value : anna2path.values()) {
            List<AnnotationWrapper> annotationGroups = new ArrayList<>(value.values());
            injectAnnotation(seedWrappers, annotationGroups);
        }
        System.out.println("EqualAnna Mutants have been generated and Size is: " + getFilenamesFromFolder(EVALUATION_PATH + sep + "mutants", true).size());
    }

    private static final HashMap<String, String> annaRecover;

    static {
        annaRecover = new HashMap<>();
        annaRecover.put("ParametersAreNullableByDefault", "Nullable");
        annaRecover.put("NotNull", "NonNull");
        annaRecover.put("FieldsAreNonnullByDefault", "NonNull");
        annaRecover.put("ParametersAreNonnullByDefault", "NonNull");
        annaRecover.put("NotEmpty", "NonNull");
        annaRecover.put("FieldsAreNullableByDefault", "Nullable");
        annaRecover.put("NotBlank", "NonNull");
        annaRecover.put("ParametricNullness", "Nullable");
        annaRecover.put("ElementTypesAreNonnullByDefault", "NonNull");
        annaRecover.put("Null", "Nullable");
    }

    public void runEqualAnna() {
        Map<String, HashMap<String, AnnotationWrapper>> anna2path = equalAnnaFinder();
        generateEqualAnnaMutants(anna2path);
        EqualAnnaChecker.run();
    }

    public void writeCompactIssues() {
        for (Map.Entry<String, HashMap<String, List<TriTuple>>> entry : compactIssues.entrySet()) {
            String rule = entry.getKey();
            HashMap<String, List<TriTuple>> seq2mutants = entry.getValue();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            root.put("Rule", rule);
            root.put("SeqSize", seq2mutants.size());
            ArrayNode bugs = mapper.createArrayNode();
            for (Map.Entry<String, List<TriTuple>> subEntry : seq2mutants.entrySet()) {
                ObjectNode bug = mapper.createObjectNode();
                bug.put("Transform_Sequence", subEntry.getKey());
                ArrayNode tuples = mapper.createArrayNode();
                for (TriTuple triTuple : subEntry.getValue()) {
                    ObjectNode tuple = mapper.createObjectNode();
                    tuple.put("Seed", triTuple.first);
                    tuple.put("Mutant", triTuple.second);
                    tuple.put("BugType", triTuple.third);
                    tuples.add(tuple);
                }
                bug.putIfAbsent("Bugs", tuples);
                bugs.add(bug);
            }
            root.putIfAbsent("Results", bugs);
            try {
                File jsonFile = new File(ruleCheckResultPath + sep + rule + ".json");
                if (!jsonFile.exists()) {
                    jsonFile.createNewFile();
                }
                FileWriter jsonWriter = new FileWriter(jsonFile);
                BufferedWriter jsonBufferedWriter = new BufferedWriter(jsonWriter);
                jsonBufferedWriter.write(root.toString());
                jsonBufferedWriter.close();
                jsonWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeEvaluationResults() {
        writeCompactIssues();
        List<String> contents = new ArrayList<>();
        contents.add("Number of Bug(s): " + compactIssues.size());
        StringBuilder bugNames = new StringBuilder();
        bugNames.append("Buggy Rules: [");
        for(String bugName : compactIssues.keySet()) {
            bugNames.append(bugName + ", ");
        }
        if(compactIssues.size() > 0) {
            bugNames.deleteCharAt(bugNames.length() - 1);
        }
        bugNames.append("]");
        contents.add(bugNames.toString());
        contents.add("\n");
        contents.add("Start Time: " + sdf.format(new Date(Long.parseLong(String.valueOf(startTimeStamp)))));
        long endTimeStamp = System.currentTimeMillis();
        contents.add("End Time: " + sdf.format(new Date(Long.parseLong(String.valueOf(endTimeStamp)))));
        long execTime = endTimeStamp - startTimeStamp;
        long minutes = (execTime / 1000) / 60;
        long seconds = (execTime / 1000) % 60;
        contents.add(String.format("Overall execution time: %d min(s) %d sec(s).\n", minutes, seconds));
        writeLinesToFile(contents, EVALUATION_PATH + sep + "Output.log");
        contents.clear();
        for(Map.Entry<String, String> entry : mutant2seed.entrySet()) {
            contents.add("M: " + entry.getKey());
            contents.add("S: " + entry.getValue());
        }
        writeLinesToFile(contents, EVALUATION_PATH + sep + "Mutant2Seed.log");
        contents.clear();
        writeLinesToFile(fail2reset, EVALUATION_PATH + sep + "Fail2Reset.log");
        writeLinesToFile(fail2deannotation, EVALUATION_PATH + sep + "Fail2DeAnnotation.log");
        writeLinesToFile(fail2compile, EVALUATION_PATH + sep + "Fail2Compile.log");
        writeLinesToFile(failedSrcMutant, EVALUATION_PATH + sep + "FailedSrcMutant.log");
        writeLinesToFile(fail2getReports, EVALUATION_PATH + sep + "Fail2GetReport.log");
        writeLinesToFile(failedCompilation, EVALUATION_PATH + sep + "FailedCompilation.log");
        writeLinesToFile(failedInvocation, EVALUATION_PATH + sep + "FailedInvocation.log");
        if(PMD_MUTATION) {
            writeLinesToFile(errorPMDPaths, EVALUATION_PATH + sep + "ErrorPMDReportPath.log");
        }
        for (Map.Entry<String, String> entry : filepath2annotation.entrySet()) {
            contents.add(entry.getKey() + "---" + entry.getValue());
        }
        writeLinesToFile(contents, EVALUATION_PATH + sep + "filepath2annotation.log");
        contents.clear();
        if(COMPILE) {
            System.out.println("Number of failed compilation: " + failedCompilation.size());
            System.out.println("Number of compilation: " + sumCompilation);
        }
        if(DIFFERENTIAL_TESTING) {
            System.out.println("Number of failed deAnnotation: " + failDeAnnotation);
            System.out.println("Number of deAnnotation: " + sumDeAnnotation);
        }
        System.out.println("Number of failed invocation: " + failedInvocation.size());
        System.out.println("Number of invocation: " + sumInvocation);
        if(EQUAL_ANNA_TESTING) {
            System.out.println("Success to get Report: " + succ2getReports + "  Fail to get Report: " + fail2getReports.size());
        }
    }

}
