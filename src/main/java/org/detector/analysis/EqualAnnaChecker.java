package org.detector.analysis;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import org.apache.commons.io.FileUtils;
import org.detector.report.CheckStyle_Report;
import org.detector.report.Infer_Report;
import org.detector.report.PMD_Report;
import org.detector.report.Report;
import org.detector.report.SonarQube_Report;
import org.detector.report.SpotBugs_Report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.detector.analysis.DiffAnalysis.diffAnalysis;
import static org.detector.analysis.Schedule.fullName2folderName;
import static org.detector.analysis.Schedule.subFolder2index;
import static org.detector.util.Utility.CHECKSTYLE_MUTATION;
import static org.detector.util.Utility.CHECKSTYLE_PATH;
import static org.detector.util.Utility.EVALUATION_PATH;
import static org.detector.util.Utility.INFER_MUTATION;
import static org.detector.util.Utility.INFER_PATH;
import static org.detector.util.Utility.JAVAC_PATH;
import static org.detector.util.Utility.JAVA_PATH;
import static org.detector.util.Utility.MUTANT_FOLDER_PATH;
import static org.detector.util.Utility.PMD_CONFIG_PATH;
import static org.detector.util.Utility.PMD_MUTATION;
import static org.detector.util.Utility.PROJECT_PATH;
import static org.detector.util.Utility.Path2Last;
import static org.detector.util.Utility.SONARQUBE_MUTATION;
import static org.detector.util.Utility.SONARQUBE_PROJECT_KEY;
import static org.detector.util.Utility.SONAR_SCANNER_PATH;
import static org.detector.util.Utility.SPOTBUGS_MUTATION;
import static org.detector.util.Utility.SPOTBUGS_PATH;
import static org.detector.util.Utility.annaJarFolderPath;
import static org.detector.util.Utility.compileJavaSourceFile;
import static org.detector.util.Utility.createSonarQubeProject;
import static org.detector.util.Utility.deleteSonarQubeProject;
import static org.detector.util.Utility.fail2getReports;
import static org.detector.util.Utility.filepath2annotation;
import static org.detector.util.Utility.file_sep;
import static org.detector.util.Utility.getDirectFilenamesFromFolder;
import static org.detector.util.Utility.getFilenamesFromFolder;
import static org.detector.util.Utility.inferDependencyJarStr;
import static org.detector.util.Utility.invokeCommandsByZT;
import static org.detector.util.Utility.invokeCommandsByZTWithOutput;
import static org.detector.util.Utility.removePostfix;
import static org.detector.util.Utility.sep;
import static org.detector.util.Utility.succ2getReports;
import static org.detector.util.Utility.waitTaskEnd;
import static org.detector.util.Utility.writeLinesToFile;

import java.lang.annotation.AnnotationTypeMismatchException;

public class EqualAnnaChecker {

    public static void run() {
        System.out.println("Begin to run Equal Anna Checker...");
        if (PMD_MUTATION) {
            runPMD();
        }
        if (SPOTBUGS_MUTATION) {
            runSpotBugs();
        }
        if (CHECKSTYLE_MUTATION) {
            runCheckStyle();
        }
        if (INFER_MUTATION) {
            runInfer();
        }
        if (SONARQUBE_MUTATION) {
            runSonarQube();
        }
        System.out.println("End Equal Anna Checker!");
    }

    private static void diff(Map<String, Report> path2report) {
        for(HashMap<String, List<String>> anna2paths : subFolder2index.values()) {
            for (List<String> paths : anna2paths.values()) {
                for (int i = 0; i < paths.size() - 1; i++) {
                    String path1 = paths.get(i);
                    if (!path2report.containsKey(path1)) {
                        fail2getReports.add(path1);
                        continue;
                    }
                    succ2getReports++;
                    Report report1 = path2report.get(path1);
                    for (int j = i + 1; j < paths.size(); j++) {
                        String path2 = paths.get(j);
                        try {
                            if (!path2report.containsKey(path2)) {
                                fail2getReports.add(path2);
                                continue;
                            }
                            succ2getReports++;
                            Report report2 = path2report.get(path2);
                            diffAnalysis(report1, report2, filepath2annotation.get(path1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void runPMD() {
        HashMap<String, Report> path2report = new HashMap<>();
        List<String> ruleNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "mutants", true);
        System.out.println("All Rule Name Size: " + ruleNames.size());
        for (int i = 0; i < ruleNames.size(); i++) {
            String ruleName = ruleNames.get(i);
            System.out.println(i + " Rule Name: " + ruleName);
            List<String> mutantPaths = getFilenamesFromFolder(ruleName, true);
            for(String mutantPath : mutantPaths) {
                String mutantName = Path2Last(mutantPath);
                Path detectionPath = Paths.get(mutantPath);
                Path reportPath = Paths.get(EVALUATION_PATH + sep + "results" + sep + mutantName + ".json");;
                PMDConfiguration srcConfig = new PMDConfiguration();
                srcConfig.setReportFormat("json");
                srcConfig.setReportFile(reportPath);
                srcConfig.addRuleSet(PMD_CONFIG_PATH);
                srcConfig.setIgnoreIncrementalAnalysis(true);
                PmdAnalysis srcAnalysis = PmdAnalysis.create(srcConfig);
                srcAnalysis.files().addFile(detectionPath);
                srcAnalysis.performAnalysis();
                List<Report> reportList = PMD_Report.readResultFile(reportPath.toFile().getAbsolutePath());
                for (int reportIndex = 0; reportIndex < reportList.size(); reportIndex++) {
                    Report report = reportList.get(reportIndex);
                    if (!path2report.containsKey(report.getFilePath())) {
                        path2report.put(report.getFilePath(), report);
                    }
                }
            }
        }
        diff(path2report);
    }

    public static void runSpotBugs() {
        List<String> ruleNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "mutants", false);
        HashMap<String, Report> path2report = new HashMap<>();
        for (int ruleNameIndex = 0; ruleNameIndex < ruleNames.size(); ruleNameIndex++) {
            String ruleName = ruleNames.get(ruleNameIndex);
            String detectionPath = EVALUATION_PATH + sep + "mutants" + sep + ruleName;
            List<String> fileNames = getFilenamesFromFolder(detectionPath, false);
            File classFolder = new File(EVALUATION_PATH + sep + "classes" + sep + ruleName);
            if (!classFolder.exists()) {
                classFolder.mkdir();
            }
            for (String fileNameWithPostfix : fileNames) {
                String fileName = removePostfix(fileNameWithPostfix);
                String srcReportPath = EVALUATION_PATH + sep + "results" + sep + fileName + ".xml";
                if (!compileJavaSourceFile(detectionPath, fileNameWithPostfix, classFolder.getAbsolutePath())) {
                    continue;
                }
                File classFile = new File(classFolder.getAbsolutePath() + sep + fileName + ".class");
                String[] invokeCommands = new String[3];
                invokeCommands[0] = "/bin/bash";
                invokeCommands[1] = "-c";
                invokeCommands[2] = SPOTBUGS_PATH + " -textui"
                        + " -xml:withMessages" + " -output " + srcReportPath + " "
                        + classFile.getAbsolutePath();
                if (invokeCommandsByZT(invokeCommands)) {
                    List<Report> srcReportList = SpotBugs_Report.readResultFile(ruleName, srcReportPath);
                    for (Report report : srcReportList) {
                        path2report.put(EVALUATION_PATH + sep + "mutants" + sep + report.getFilePath(), report);
                    }
                }
                try {
                    FileUtils.delete(classFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        diff(path2report);
    }

    public static void runCheckStyle() {
        List<String> subFolderNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "mutants", false);
        Map<String, Report> path2report = new HashMap<>();
        for (int ruleNameIndex = 0; ruleNameIndex < subFolderNames.size(); ruleNameIndex++) {
            String subFolderName = subFolderNames.get(ruleNameIndex);
            String configPath = PROJECT_PATH + sep + "tools" + sep + "google_check.xml";
            String detectionPath = EVALUATION_PATH + sep + "mutants" + sep + subFolderName;
            List<String> fileNames = getFilenamesFromFolder(detectionPath, false);
            for (String fileNameWithPostfix : fileNames) {
                String fileName = removePostfix(fileNameWithPostfix);
                String filePath = detectionPath + sep + fileNameWithPostfix;
                String reportPath = EVALUATION_PATH + sep + "results" + sep + fileName + ".txt";
                String[] invokeCommands = new String[3];
                invokeCommands[0] = "/bin/bash";
                invokeCommands[1] = "-c";
                invokeCommands[2] = JAVA_PATH + " -jar " + CHECKSTYLE_PATH + " -f" + " plain" + " -o " + reportPath + " -c " + configPath + " " + filePath;
                boolean isCheckStyleOK = invokeCommandsByZT(invokeCommands);
                if (isCheckStyleOK) {
                    List<Report> srcReportList = CheckStyle_Report.readResultFile(reportPath);
                    for (Report report : srcReportList) {
                        path2report.put(report.getFilePath(), report);
                    }
                } else {
                    System.err.println("Invoke CheckStyle failed, but class files generated.");
                }
            }
        }
        diff(path2report);
    }

    public static void runInfer() {
        List<String> ruleNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "mutants", false);
        Map<String, Report> path2report = new HashMap<>();
        for (int ruleNameIndex = 0; ruleNameIndex < ruleNames.size(); ruleNameIndex++) {
            String ruleName = ruleNames.get(ruleNameIndex);
            String detectionPath = EVALUATION_PATH + sep + "mutants" + sep + ruleName;
            File classFolder = new File(EVALUATION_PATH + sep + "classes" + sep + ruleName);
            if (!classFolder.exists()) {
                classFolder.mkdir();
            }
            List<String> fileNames = getFilenamesFromFolder(detectionPath, false);
            for (String fileNameWithPostfix : fileNames) {
                String fileName = removePostfix(fileNameWithPostfix);
                File reportFolder = new File(EVALUATION_PATH + sep + "results" + sep + fileName);
                String srcFilePath = detectionPath + sep + fileName + ".java";
                String annotationFullName = filepath2annotation.get(srcFilePath);
                String annaJarFolderName = fullName2folderName.get(annotationFullName);
                File annotationJarFile = new File(annaJarFolderPath + sep + annaJarFolderName + ".jar");
                if (!annotationJarFile.exists()) {
                    System.err.println("Annotation jar file for Infer is not existed!");
                    System.exit(-1);
                }
                String cmd = INFER_PATH + " run -o " + reportFolder.getAbsolutePath() + " -- " + JAVAC_PATH +
                        " -d " + classFolder.getAbsolutePath() + sep + fileName +
                        " -cp " + inferDependencyJarStr + file_sep + annotationJarFile.getAbsolutePath() + " " + srcFilePath;
                String[] invokeCommands = {"/bin/bash", "-c", cmd};
                invokeCommandsByZT(invokeCommands);
                File reportFile = new File(reportFolder.getAbsolutePath() + sep + "report.json");
                File newReportFile = new File(EVALUATION_PATH + sep + "results" + sep + fileName + ".json");
                if (!reportFile.exists()) {
                    try {
                        FileUtils.deleteDirectory(reportFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                Report report = Infer_Report.readSingleResultFile(srcFilePath, reportFile);
                try {
                    FileUtils.moveFile(reportFile, newReportFile);
                    FileUtils.deleteDirectory(reportFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                path2report.put(report.getFilePath(), report);
            }
        }
        diff(path2report);
    }

    public static void runSonarQube() {
        Map<String, Report> path2report = new HashMap<>();
        List<String> mutantPaths = getFilenamesFromFolder(MUTANT_FOLDER_PATH, true);
        for(int i = 0; i < mutantPaths.size(); i++) {
            deleteSonarQubeProject(SONARQUBE_PROJECT_KEY);
            createSonarQubeProject(SONARQUBE_PROJECT_KEY);
            File mutantFile = new File(mutantPaths.get(i));
            String[] invokeCommands = new String[3];
            invokeCommands[0] = "/bin/bash";
            invokeCommands[1] = "-c";
            invokeCommands[2] = SONAR_SCANNER_PATH + " -Dsonar.projectKey=" + SONARQUBE_PROJECT_KEY
                    + " -Dsonar.projectBaseDir=" + EVALUATION_PATH
                    + " -Dsonar.sources=" + mutantFile.getAbsolutePath()
                    + " -Dsonar.host.url=http://localhost:9000"
                    + " -Dsonar.login=admin -Dsonar.password=123456";
            boolean hasExec = invokeCommandsByZT(invokeCommands);
            if (hasExec) {
                waitTaskEnd();
            } else {
                continue;
            }
            String[] curlCommands = new String[4];
            curlCommands[0] = "curl";
            curlCommands[1] = "-u";
            curlCommands[2] = "admin:123456";
            curlCommands[3] = "http://localhost:9000/api/issues/search?p=1&ps=500&componentKeys=" + SONARQUBE_PROJECT_KEY;
            String output = invokeCommandsByZTWithOutput(curlCommands);
            String mutantName = mutantFile.getName().substring(0, mutantFile.getName().length() - 5);
            writeLinesToFile(output, EVALUATION_PATH + sep + "results" + sep + mutantName + ".json");
            Report report = SonarQube_Report.readSingleResultFile(mutantFile.getAbsolutePath(), output);
            path2report.put(mutantFile.getAbsolutePath(), report);
        }
        diff(path2report);
    }

}
