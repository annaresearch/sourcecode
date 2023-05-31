package org.detector.util;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.json.JSONObject;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.detector.analysis.Schedule.fullName2folderName;

public class Utility {

    public static final String sep_regex = "/|\\\\";
    public static final String sep = File.separator;

    public static Properties properties;

    static {
        properties = new Properties();
        File file = new File("./config.properties");
        try {
            InputStream in = new FileInputStream(file);
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String file_sep = OSUtil.isWindows() ? ";" : ":";
    public static final boolean DIFFERENTIAL_TESTING = Boolean.parseBoolean(getProperty("DIFFERENTIAL_TESTING"));
    public static final boolean INJECTION_TESTING = Boolean.parseBoolean((getProperty("INJECTION_TESTING")));
    public static final boolean EQUAL_ANNA_TESTING = Boolean.parseBoolean(getProperty("EQUAL_ANNA_TESTING"));
    public static final boolean COUNT_SRC = Boolean.parseBoolean(getProperty("COUNT_SRC"));
    public static final boolean COUNT_DST = Boolean.parseBoolean(getProperty("COUNT_DST"));
    public static final boolean LOW_THREE = Boolean.parseBoolean(getProperty("LOW_THREE"));
    public static final boolean DEBUG = Boolean.parseBoolean(getProperty("DEBUG"));
    public static final boolean DELOMBOK = Boolean.parseBoolean(getProperty("DELOMBOK"));
    public static final boolean USE_FORMAT = Boolean.parseBoolean(getProperty("USE_FORMAT"));
    public static final String PROJECT_PATH = System.getProperty("user.dir");
    public static final String TOOL_PATH = PROJECT_PATH + sep + "tools";
    public static final String ANNOTATION_LIBRARY_PATH = getProperty("ANNOTATION_LIBRARY_PATH");
    public static final String SEED_PATH = DIFFERENTIAL_TESTING ? getProperty("SEED_PATH").replace("seeds", "seeds_checker1") : getProperty("SEED_PATH");
    public static final String EVALUATION_PATH = getProperty("EVALUATION_PATH");
    public static final String RESULT_PATH = EVALUATION_PATH + sep + "results";
    public static final String RULE_CHECK_PATH = EVALUATION_PATH + sep + "rule_check_results";
    public static final String CLASS_FOLDER_PATH = EVALUATION_PATH + sep + "classes";
    public static final String MUTANT_FOLDER_PATH = EVALUATION_PATH + sep + "mutants";
    public static final String DECOMPILED_FOLDER_PATH = EVALUATION_PATH + sep + "decompile";
    public static final File classFolder = new File(CLASS_FOLDER_PATH);
    public static final File resultFolder = new File(RESULT_PATH);
    public static final File ruleCheckResultPath = new File(RULE_CHECK_PATH);
    public static final File mutantFolder = new File(MUTANT_FOLDER_PATH);
    public static final File decompiledFolder = new File(DECOMPILED_FOLDER_PATH);

    public static final boolean PMD_MUTATION = Boolean.parseBoolean(getProperty("PMD_MUTATION"));
    public static final boolean SPOTBUGS_MUTATION = Boolean.parseBoolean(getProperty("SPOTBUGS_MUTATION"));
    public static final boolean INFER_MUTATION = Boolean.parseBoolean(getProperty("INFER_MUTATION"));
    public static final boolean CHECKSTYLE_MUTATION = Boolean.parseBoolean(getProperty("CHECKSTYLE_MUTATION"));
    public static final boolean SONARQUBE_MUTATION = Boolean.parseBoolean(getProperty("SONARQUBE_MUTATION"));
    public static final boolean COMPILE = (SPOTBUGS_MUTATION || INFER_MUTATION) ? true : false;
    public static final boolean OFFSET_IMPACT = Boolean.parseBoolean(getProperty("OFFSET_IMPACT"));

    public static final String JAVA_PATH = getProperty("JAVA_PATH");
    public static final String JAVAC_PATH = getProperty("JAVAC_PATH");
    public static final String GOOGLE_FORMAT_PATH = getProperty("GOOGLE_FORMAT_PATH");
    public static final String PMD_CONFIG_PATH = getProperty("PMD_CONFIG_PATH");
    public static final String SPOTBUGS_PATH = getProperty("SPOTBUGS_PATH");
    public static final String INFER_PATH = getProperty("INFER_PATH");
    public static final String CHECKSTYLE_PATH = getProperty("CHECKSTYLE_PATH");
    public static final String SONAR_SCANNER_PATH = getProperty("SONAR_SCANNER_PATH");
    public static final String SONARQUBE_PROJECT_KEY = getProperty("SONARQUBE_PROJECT_KEY");
    public static String annaClassFolderPath = getProperty("ANNA_CLASS_FOLDER_PATH");
    public static String annaJarFolderPath = getProperty("ANNA_JAR_FOLDER_PATH");
    public static String MOCK_ANNOTATION_JAR_PATH = getProperty("MOCK_ANNOTATION_JAR_PATH");
    public static final String SPOTBUGS_DEPENDENCY_PATH = PROJECT_PATH + sep + "tools" + sep + "SpotBugs_Dependency";
    public static final String INFER_DEPENDENCY_PATH = PROJECT_PATH + sep + "tools" + sep + "Infer_Dependency";
    public static final String ANNOTATION_DEPENDENCY_PATH = PROJECT_PATH + sep + "tools" + sep + "Annotation_Dependency";
    public static final List<String> spotBugsJarList = getFilenamesFromFolder(SPOTBUGS_DEPENDENCY_PATH, true);
    public static final List<String> inferJarList = getFilenamesFromFolder(INFER_DEPENDENCY_PATH, true);
    public static final List<String> annotationJarList = getFilenamesFromFolder(ANNOTATION_DEPENDENCY_PATH, true);

    public static StringBuilder spotBugsDependencyJarStr = new StringBuilder();
    public static StringBuilder inferDependencyJarStr = new StringBuilder();
    public static StringBuilder annotationJarStr = new StringBuilder();
    public static String lombokPath = null;

    public static HashMap<String, HashMap<String, List<TriTuple>>> compactIssues = new HashMap<>();
    public static Map<String, String> filepath2annotation = new HashMap<>();
    public static Map<String, String> mutant2seed = new HashMap<>();

    public static void init() {
        File javacFile = new File(JAVAC_PATH);
        if (!javacFile.exists()) {
            System.err.println("Javac is not existed!");
            System.exit(-1);
        }
        File javaFile = new File(JAVA_PATH);
        if (!javaFile.exists()) {
            System.err.println("Java is not existed!");
            System.exit(-1);
        }
        File spotbugsFile = new File(SPOTBUGS_PATH);
        if (!spotbugsFile.exists()) {
            System.err.println("SpotBugs is not existed!");
            System.exit(-1);
        }
        File cstyleFile = new File(CHECKSTYLE_PATH);
        if (!cstyleFile.exists()) {
            System.err.println("CheckStyle is not existed!");
            System.exit(-1);
        }
        spotBugsDependencyJarStr.append("." + file_sep);
        for (int i = spotBugsJarList.size() - 1; i >= 1; i--) {
            spotBugsDependencyJarStr.append(spotBugsJarList.get(i) + file_sep);
        }
        spotBugsDependencyJarStr.append(spotBugsJarList.get(0));
        inferDependencyJarStr.append("." + file_sep);
        for (int i = inferJarList.size() - 1; i >= 1; i--) {
            inferDependencyJarStr.append(inferJarList.get(i) + file_sep);
        }
        inferDependencyJarStr.append(inferJarList.get(0));
        for (String annotationJar : annotationJarList) {
            if (annotationJar.toLowerCase().contains("lombok")) {
                lombokPath = annotationJar;
                break;
            }
        }
        annotationJarStr.append("." + file_sep);
        for (int i = annotationJarList.size() - 1; i >= 1; i--) {
            annotationJarStr.append(annotationJarList.get(i) + file_sep);
        }
        annotationJarStr.append(annotationJarList.get(0));
        try {
            File EVALUATION_DIR = new File(EVALUATION_PATH);
            if (EVALUATION_DIR.exists()) {
                if(DEBUG) {
                    FileUtils.deleteDirectory(EVALUATION_DIR);
                } else {
                    System.out.println("EVALUATION_DIR is existed! Check it to determine retain or remove!");
                    System.out.println("1: Delete the existed directory and continue to run. Others: exit.");
                    Scanner in = new Scanner(System.in);
                    int tag = in.nextInt();
                    if (tag == 1) {
                        FileUtils.deleteDirectory(EVALUATION_DIR);
                    } else {
                        System.exit(-1);
                    }
                }
            }
            EVALUATION_DIR.mkdir();
            resultFolder.mkdir();
            ruleCheckResultPath.mkdir();
            mutantFolder.mkdir();
            if (SPOTBUGS_MUTATION || INFER_MUTATION) {
                classFolder.mkdir();
            }
            if (DIFFERENTIAL_TESTING) {
                decompiledFolder.mkdir();
            }
            List<String> subSeedFolderNameList = getDirectFilenamesFromFolder(SEED_PATH, false);
            for (String subSeedFolderName : subSeedFolderNameList) {
                File subSeedFolder = new File(mutantFolder.getAbsolutePath() + File.separator + subSeedFolderName);
                subSeedFolder.mkdir();
                subSeedFolder = new File(decompiledFolder.getAbsolutePath() + sep + subSeedFolderName);
                subSeedFolder.mkdir();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String name) {
        if (properties.containsKey(name)) {
            return properties.getProperty(name);
        } else {
            System.err.println(name + " is not existed!");
            System.exit(-1);
            return null;
        }
    }

    public static String removePostfix(String token) {
        if (token.endsWith(".java") || token.endsWith(".jar") || token.endsWith(".txt") || token.endsWith(".class") || token.endsWith(".rar")) {
            return token.substring(0, token.lastIndexOf('.'));
        } else {
            return token;
        }
    }

    public static String Path2Last(String path) {
        String[] tokens = path.split(sep_regex);
        String target = tokens[tokens.length - 1];
        return removePostfix(target);
    }

    public static List<String> getDirectFilenamesFromFolder(String path, boolean getAbsolutePath) {
        LinkedList<String> fileList = new LinkedList<>();
        if (path.contains(".DS_Store") || path.contains(".git")) {
            return fileList;
        }
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files == null) {
            System.err.println("GetDirectFile Cannot find: " + path);
        }
        for (File file : files) {
            if (file.getAbsolutePath().contains(".DS_Store") || file.getAbsolutePath().contains(".git")) {
                continue;
            }
            fileList.add(file.getAbsolutePath());
        }
        if (getAbsolutePath) {
            return fileList;
        } else {
            LinkedList<String> pureNames = new LinkedList<>();
            for (String srcName : fileList) {
                String[] tokens = srcName.split(sep_regex);
                pureNames.add(tokens[tokens.length - 1]);
            }
            return pureNames;
        }
    }

    // Do Not Include Directory
    public static List<String> getFilenamesFromFolder(String path, boolean getAbsolutePath) {
        LinkedList<String> fileList = new LinkedList<>();
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files == null) {
            System.err.println("GetFileName cannot find: " + path);
            System.err.println(1 / 0);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                fileList.addAll(getFilenamesFromFolder(file.getAbsolutePath(), getAbsolutePath));
            } else {
                if (file.getAbsolutePath().contains(".DS_Store") || file.getAbsolutePath().contains(".git")) {
                    continue;
                }
                fileList.add(file.getAbsolutePath());
            }
        }
        if (getAbsolutePath) {
            return fileList;
        } else {
            LinkedList<String> pureNames = new LinkedList<>();
            for (String srcName : fileList) {
                String[] tokens = srcName.split(sep);
                pureNames.add(tokens[tokens.length - 1]);
            }
            return pureNames;
        }
    }

    public static List<String> readLines(String targetPath) {
        List<String> lines = new ArrayList<>();
        File targetFile = new File(targetPath);
        if (!targetFile.exists() || targetFile.length() == 0) {
            System.out.println("FileRead Exception in: " + targetPath);
            return lines;
        }
        try {
            FileInputStream inputStream = new FileInputStream(targetPath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                lines.add(str);
            }
            inputStream.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void writeLinesToFile(String[] contents, String targetPath) {
        try {
            FileOutputStream fos = new FileOutputStream(targetPath);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            for (String line : contents) {
                bw.write(line + "\n");
            }
            bw.close();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLinesToFile(String content, String targetPath) {
        try {
            FileOutputStream fos = new FileOutputStream(targetPath, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(content + "\n");
            bw.close();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLinesToFile(List<String> contents, String targetPath) {
        try {
            FileOutputStream fos = new FileOutputStream(targetPath, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            for (String line : contents) {
                bw.write(line + "\n");
            }
            bw.close();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int sumCompilation = 0;
    public static int sumInvocation = 0;
    public static List<String> failedCompilation = new ArrayList<>();
    public static List<String> failedInvocation = new ArrayList<>();
    public static List<String> fail2getReports = new ArrayList<>();
    public static int succ2getReports = 0;

    public static String invokeCommandsByZTWithOutput(String[] cmdArgs) {
        StringBuilder argStr = new StringBuilder();
        String output = "";
        for (String arg : cmdArgs) {
            argStr.append(arg + " ");
        }
        if (argStr.toString().contains("javac")) {
            sumCompilation++;
        }
        if (argStr.toString().contains("/bin/bash")) {
            sumInvocation++;
        }
        try {
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            output = new ProcessExecutor().command(cmdArgs).redirectError(errorStream).readOutput(true).execute().outputUTF8();
        } catch (Exception e) {
            e.printStackTrace();
            return output;
        }
        return output;
    }

    public static boolean deleteSonarQubeProject(String projectName) {
        String[] curlPostCommands = new String[6];
        curlPostCommands[0] = "curl";
        curlPostCommands[1] = "-u";
        curlPostCommands[2] = "admin:123456";
        curlPostCommands[3] = "-X";
        curlPostCommands[4] = "POST";
        curlPostCommands[5] = "http://localhost:9000/api/projects/delete?project=" + projectName;
        return invokeCommandsByZT(curlPostCommands);
    }

    public static boolean createSonarQubeProject(String projectName) {
        String[] curlPostCommands = new String[6];
        curlPostCommands[0] = "curl";
        curlPostCommands[1] = "-u";
        curlPostCommands[2] = "admin:123456";
        curlPostCommands[3] = "-X";
        curlPostCommands[4] = "POST";
        curlPostCommands[5] = "http://localhost:9000/api/projects/create?name=" + projectName + "&project=" + projectName;
        return invokeCommandsByZT(curlPostCommands);
    }

    public static boolean invokeCommandsByZT(String[] cmdArgs, File reportFile) {
        StringBuilder argStr = new StringBuilder();
        for (String arg : cmdArgs) {
            argStr.append(arg + " ");
        }
        if (argStr.toString().contains("javac")) {
            sumCompilation++;
        }
        if (argStr.toString().contains("/bin/bash")) {
            sumInvocation++;
        }
        try {
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            int exitValue = new ProcessExecutor().command(cmdArgs).redirectError(errorStream).execute().getExitValue();
            if (CHECKSTYLE_MUTATION) {
                String errorInfo = new String(errorStream.toByteArray());
                if (argStr.toString().contains("javac")) {
                    if (exitValue != 0) {
                        failedCompilation.add(argStr.toString());
                        return false;
                    }
                }
                if (argStr.toString().contains("/bin/bash")) {
                    if (exitValue == 254) {
                        System.out.println(errorInfo);
                    }
                    if (errorInfo.contains("Exception")) {
                        failedInvocation.add(argStr.toString());
                        return false;
                    }
                }
            }
            if (!CHECKSTYLE_MUTATION && exitValue != 0) {
                if (PMD_MUTATION && reportFile.exists()) {
                    return true;
                }
                if (DEBUG) {
                    System.out.println("Execute Commands Error!");
                    System.out.println(argStr);
                    System.out.println("Error Message: " + new String(errorStream.toByteArray()));
                }
                if (argStr.toString().contains("/bin/")) {
                    failedInvocation.add(argStr.toString());
                }
                if (argStr.toString().contains("javac")) {
                    failedCompilation.add(argStr.toString());
                }
                return false;
            }
        } catch (InterruptedException | TimeoutException | IOException e) {
            System.err.println(argStr);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean invokeCommandsByZT(String[] cmdArgs) {
        StringBuilder argStr = new StringBuilder();
        for (String arg : cmdArgs) {
            argStr.append(arg + " ");
        }
        if (argStr.toString().contains("javac")) {
            sumCompilation++;
        }
        if (argStr.toString().contains("/bin/bash")) {
            sumInvocation++;
        }
        try {
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            int exitValue = new ProcessExecutor().command(cmdArgs).redirectError(errorStream).execute().getExitValue();
            if (CHECKSTYLE_MUTATION || PMD_MUTATION) {
                String errorInfo = new String(errorStream.toByteArray());
                if (argStr.toString().contains("javac")) {
                    if (exitValue != 0) {
                        failedCompilation.add(argStr.toString());
                        return false;
                    }
                }
                if (argStr.toString().contains("/bin/bash")) {
                    if (exitValue == 254) {
                        System.out.println(errorInfo);
                    }
                    if (errorInfo.contains("Exception")) {
                        failedInvocation.add(argStr.toString());
                        return false;
                    }
                }
            }
            if (!CHECKSTYLE_MUTATION && exitValue != 0) {
                if (DEBUG) {
                    System.out.println("Execute Commands Error!");
                    System.out.println(argStr);
                    System.out.println("Error Message: " + new String(errorStream.toByteArray()));
                }
                if (argStr.toString().contains("/bin/")) {
                    failedInvocation.add(argStr.toString());
                }
                if (argStr.toString().contains("javac")) {
                    failedCompilation.add(argStr.toString());
                }
                return false;
            }
        } catch (InterruptedException | TimeoutException | IOException e) {
            System.out.println("Error Commands: " + argStr);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deLombok(String srcFolderPath, String srcFileName, String deAnnotationFileFolder) {
        if (!srcFileName.endsWith(".java")) {
            System.err.println("File: " + srcFileName + " is not ended by .java");
            System.exit(-1);
        }
        srcFileName = srcFileName.substring(0, srcFileName.length() - 5);
        String srcPath = srcFolderPath + sep + srcFileName + ".java";
        List<String> cmdList = new ArrayList<>();
        cmdList.add(JAVA_PATH);
        cmdList.add("-jar");
        cmdList.add(lombokPath);
        cmdList.add("delombok");
        cmdList.add("--classpath");
        if (SPOTBUGS_MUTATION || SONARQUBE_MUTATION) {
            cmdList.add(spotBugsDependencyJarStr.toString() + annotationJarStr.substring(1));
        }
        if (INFER_MUTATION) {
            cmdList.add(inferDependencyJarStr.toString() + annotationJarStr.substring(1));
        }
        if (PMD_MUTATION || CHECKSTYLE_MUTATION) {
            cmdList.add(annotationJarStr.toString());
        }
        cmdList.add("-f");
        cmdList.add("finalParams:skip");
        cmdList.add("-f");
        cmdList.add("generated:skip");
        cmdList.add("-f");
        cmdList.add("suppressWarnings:skip");
        cmdList.add("-f");
        cmdList.add("generateDelombokComment:skip");
        cmdList.add("-d");
        cmdList.add(deAnnotationFileFolder);
        cmdList.add(srcPath);
        return invokeCommandsByZT(cmdList.toArray(new String[cmdList.size()]));
    }

    // Format: javac DeTest.java -d target-dir -cp .:lombok-1.18.24.jar -printsource
    public static boolean compileJavaAnnotation(String srcFolderPath, String srcFileName, File deFolder) {
        if (!srcFileName.endsWith(".java")) {
            System.err.println("File: " + srcFileName + " is not ended by .java");
            System.exit(-1);
        }
        srcFileName = srcFileName.substring(0, srcFileName.length() - 5);
        List<String> cmd_list = new ArrayList<>();
        cmd_list.add(JAVAC_PATH);
        cmd_list.add("-d");
        cmd_list.add(deFolder.getAbsolutePath());  // generate decompiled code files
        cmd_list.add("-cp");
        if (SPOTBUGS_MUTATION || SONARQUBE_MUTATION) {
            cmd_list.add(spotBugsDependencyJarStr.toString() + annotationJarStr.substring(1));
        }
        if (INFER_MUTATION) {
            cmd_list.add(inferDependencyJarStr.toString() + annotationJarStr.substring(1));
        }
        if (PMD_MUTATION || CHECKSTYLE_MUTATION) {
            cmd_list.add(annotationJarStr.toString());
        }
        cmd_list.add(srcFolderPath + sep + srcFileName + ".java");
        cmd_list.add("-printsource");
        boolean tag1 = invokeCommandsByZT(cmd_list.toArray(new String[cmd_list.size()]));
        boolean tag2 = deFolder.exists();
        return tag1 && tag2;
    }

    public static boolean compileJavaSourceFile(String srcFolderPath, String fileName, String classFileFolder) {
        if (PMD_MUTATION || SONARQUBE_MUTATION) {
            System.err.println("Why these three tools here?");
            System.exit(-1);
        }
        if (!fileName.endsWith(".java")) {
            System.err.println("File: " + fileName + " is not ended by .java");
            System.exit(-1);
        }
        fileName = fileName.substring(0, fileName.length() - 5);
        List<String> cmd_list = new ArrayList<>();
        cmd_list.add(JAVAC_PATH);
        cmd_list.add("-d");
        cmd_list.add(classFileFolder);  // Generated class files are saved in this folder.
        cmd_list.add("-cp");
        String srcFilePath = srcFolderPath + File.separator + fileName + ".java";
        if (SPOTBUGS_MUTATION) {
            if (DIFFERENTIAL_TESTING) {
                cmd_list.add(spotBugsDependencyJarStr.toString() + annotationJarStr.substring(1));
            } else {
                cmd_list.add(spotBugsDependencyJarStr.toString());
            }
        }
        if (INFER_MUTATION) {
            if (DIFFERENTIAL_TESTING) {
                cmd_list.add(spotBugsDependencyJarStr.toString() + annotationJarStr.substring(1));
            } else {
                cmd_list.add(inferDependencyJarStr.toString());
            }
        }
        if (EQUAL_ANNA_TESTING) {
            String annotationFullName = filepath2annotation.get(srcFilePath);
            String annaJarFolderName = fullName2folderName.get(annotationFullName);
            File annotationJarFile = new File(annaJarFolderPath + sep + annaJarFolderName + ".jar");
            if (EQUAL_ANNA_TESTING && !annotationJarFile.exists()) {
                System.err.println(annotationJarFile.getParentFile() + " is not found!");
                System.exit(-1);
            }
            String newCmd = cmd_list.get(cmd_list.size() - 1) + (file_sep + annotationJarFile.getAbsolutePath());
            cmd_list.remove(cmd_list.size() - 1);
            cmd_list.add(newCmd);
        }
        cmd_list.add(srcFilePath);
        return invokeCommandsByZT(cmd_list.toArray(new String[cmd_list.size()]));
    }

    public static boolean hasConstructor(TypeDeclaration type) {
        for (MethodDeclaration method : type.getMethods()) {
            if (method.isConstructor()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasNoArgsConstructor(TypeDeclaration type) {
        for (MethodDeclaration method : type.getMethods()) {
            if (method.isConstructor() && method.parameters().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAllArgsConstructor(TypeDeclaration type) {
        Map<String, Integer> fieldType2cnt = new HashMap<>();
        Map<String, Integer> parameterType2cnt = new HashMap<>();
        for (FieldDeclaration field : type.getFields()) {
            String varType = field.getType().toString();
            if (fieldType2cnt.containsKey(varType)) {
                fieldType2cnt.put(varType, fieldType2cnt.get(varType) + field.fragments().size());
            } else {
                fieldType2cnt.put(varType, field.fragments().size());
            }
        }
        for (MethodDeclaration method : type.getMethods()) {
            if (method.isConstructor()) {
                for (ASTNode field : (List<ASTNode>) method.parameters()) {
                    if (field instanceof SingleVariableDeclaration) {
                        String parameterType = ((SingleVariableDeclaration) field).getType().toString();
                        if (parameterType2cnt.containsKey(parameterType)) {
                            parameterType2cnt.put(parameterType, parameterType2cnt.get(parameterType) + 1);
                        } else {
                            parameterType2cnt.put(parameterType, 1);
                        }
                    }
                }
            }
        }
        if (fieldType2cnt.keySet().size() == parameterType2cnt.keySet().size()) {
            for (String fieldName : fieldType2cnt.keySet()) {
                if (parameterType2cnt.containsKey(fieldName)) {
                    return false;
                }
                if (fieldType2cnt.get(fieldName) != parameterType2cnt.get(fieldName)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public static <T> List<List<T>> listAveragePartition(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int reminder = source.size() % n;
        int number = source.size() / n;
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value;
            if (reminder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                reminder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    public static String formatTime(long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;
        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;
        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + " day ");
        }
        if (hour > 0) {
            sb.append(hour + " hour ");
        }
        if (minute > 0) {
            sb.append(minute + " min ");
        }
        if (second > 0) {
            sb.append(second + " sec ");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + " ms");
        }
        return sb.toString();
    }

    // Add setting and dummy-binaries folder for SonarQube seed folder
    public void writeSettingFile(String seedFolderPath, String settingFilePath) {
        List<String> contents = new ArrayList<>();
        contents.add("sonar.projectKey=" + SONARQUBE_PROJECT_KEY);
        contents.add("sonar.projectName=" + SONARQUBE_PROJECT_KEY);
        contents.add("sonar.projectVersion=1.0");
        contents.add("sonar.login=admin");
        contents.add("sonar.sourceEncoding=UTF-8");
        contents.add("sonar.scm.disabled=true");
        contents.add("sonar.cpd.exclusions=**/*");
        contents.add("sonar.sources=" + seedFolderPath);
        contents.add("sonar.java.source=11");
        File dummyFolder = new File(seedFolderPath + File.separator + "dummy-binaries");
        if (!dummyFolder.exists()) {
            dummyFolder.mkdir();
        }
        File dummyFile = new File(seedFolderPath + File.separator + "dummy-binaries" + File.separator + "dummy.txt");
        if (!dummyFile.exists()) {
            try {
                dummyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        contents.add("sonar.java.binaries=" + dummyFolder.getAbsolutePath());
        contents.add("sonar.java.test.binaries=" + dummyFolder.getAbsolutePath());
        writeLinesToFile(contents, settingFilePath);
    }

    // Add setting and dummy-binaries folder for SonarQube seed folder
    public void writeSettingFile(String projectKey, String srcFolderPath, String settingFilePath) {
        List<String> contents = new ArrayList<>();
        contents.add("sonar.projectKey=" + projectKey);
        contents.add("sonar.projectName=" + projectKey);
        contents.add("sonar.projectVersion=1.0");
        contents.add("sonar.login=admin");
        contents.add("sonar.password=123456");
        contents.add("sonar.sourceEncoding=UTF-8");
        contents.add("sonar.scm.disabled=true");
        contents.add("sonar.cpd.exclusions=**/*");
        contents.add("sonar.sources=" + srcFolderPath + sep + "seeds");
        contents.add("sonar.java.source=11");
        File dummyFolder = new File(srcFolderPath + File.separator + "dummy-binaries");
        if (dummyFolder.exists()) {
            try {
                FileUtils.deleteDirectory(dummyFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dummyFolder.mkdir();
        File dummyFile = new File(srcFolderPath + File.separator + "dummy-binaries" + File.separator + "dummy.txt");
        if (!dummyFile.exists()) {
            try {
                dummyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        contents.add("sonar.java.binaries=" + dummyFolder.getAbsolutePath());
        contents.add("sonar.java.test.binaries=" + dummyFolder.getAbsolutePath());
        writeLinesToFile(contents, settingFilePath);
    }

    public static void waitTaskEnd() {
        boolean start = false;
        long startTime = System.currentTimeMillis();
        while (true) {
            String[] curlCommands = new String[4];
            curlCommands[0] = "curl";
            curlCommands[1] = "-u";
            curlCommands[2] = "admin:123456";
            curlCommands[3] = "http://localhost:9000/api/ce/activity_status?component=" + SONARQUBE_PROJECT_KEY;
            String output = invokeCommandsByZTWithOutput(curlCommands);
            JSONObject root = new JSONObject(output);
            int pending = root.getInt("pending");
            int failing = root.getInt("failing");
            int inProgress = root.getInt("inProgress");
            if (pending > 0 || inProgress > 0) {
                start = true;
            }
            if (start && pending == 0 && inProgress == 0 && failing == 0) {
                break;
            }
            if (failing > 0) {
                System.err.println("Failed CE!");
                System.exit(-1);
            }
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 1000 * 6) {
                break;
            }
        }
        if (DEBUG) {
            System.out.println("Wait Time: " + (double) (System.currentTimeMillis() - startTime) / 1000 + "(s)");
        }
    }

    public static Set<String> getRuleNames() {
        String ruleNamePath = PROJECT_PATH + sep + "tools" + sep + "SonarQube_Rules.txt";
        List<String> lines = readLines(ruleNamePath);
        if (lines.size() > 1) {
            System.err.println("Expected line number is ONE!");
            System.exit(-1);
        }
        String[] ruleNames = lines.get(0).split(",");
        Set<String> ruleNameSet = new HashSet<>();
        for (String ruleName : ruleNames) {
            ruleNameSet.add(ruleName);
        }
        return ruleNameSet;
    }

}
