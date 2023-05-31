package org.detector.analysis;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import org.apache.commons.io.FileUtils;
import org.detector.report.Report;
import org.detector.report.PMD_Report;
import org.detector.report.SpotBugs_Report;
import org.detector.report.CheckStyle_Report;
import org.detector.report.Infer_Report;
import org.detector.report.SonarQube_Report;
import org.detector.report.Violation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;

import static org.detector.analysis.DiffAnalysis.diffAnalysis;
import static org.detector.util.Utility.CHECKSTYLE_MUTATION;
import static org.detector.util.Utility.INFER_MUTATION;
import static org.detector.util.Utility.PMD_CONFIG_PATH;
import static org.detector.util.Utility.PMD_MUTATION;
import static org.detector.util.Utility.SONARQUBE_MUTATION;
import static org.detector.util.Utility.SONARQUBE_PROJECT_KEY;
import static org.detector.util.Utility.SPOTBUGS_MUTATION;
import static org.detector.util.Utility.annotationJarStr;
import static org.detector.util.Utility.classFolder;
import static org.detector.util.Utility.createSonarQubeProject;
import static org.detector.util.Utility.deleteSonarQubeProject;
import static org.detector.util.Utility.sep;
import static org.detector.analysis.Schedule.failedSrcMutant;
import static org.detector.util.Utility.CHECKSTYLE_PATH;
import static org.detector.util.Utility.DEBUG;
import static org.detector.util.Utility.EVALUATION_PATH;
import static org.detector.util.Utility.INFER_PATH;
import static org.detector.util.Utility.JAVA_PATH;
import static org.detector.util.Utility.PROJECT_PATH;
import static org.detector.util.Utility.Path2Last;
import static org.detector.util.Utility.SONAR_SCANNER_PATH;
import static org.detector.util.Utility.SPOTBUGS_PATH;
import static org.detector.util.Utility.compileJavaSourceFile;
import static org.detector.util.Utility.filepath2annotation;
import static org.detector.util.Utility.getDirectFilenamesFromFolder;
import static org.detector.util.Utility.getFilenamesFromFolder;
import static org.detector.util.Utility.inferDependencyJarStr;
import static org.detector.util.Utility.invokeCommandsByZT;
import static org.detector.util.Utility.invokeCommandsByZTWithOutput;
import static org.detector.util.Utility.removePostfix;
import static org.detector.util.Utility.waitTaskEnd;
import static org.detector.util.Utility.writeLinesToFile;

public class DiffChecker {

    public static void run() {
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
    }

    private static void runPMD() {
        List<String> ruleNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "decompile", false);
        for (int i = 0; i < ruleNames.size(); i++) {
            String ruleName = ruleNames.get(i);
            List<String> fileNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "decompile" + sep + ruleName, false);
            for(String fileName : fileNames) {
                Path srcReportPath = Paths.get(EVALUATION_PATH + sep + "results" + sep + fileName + "_src.json");
                Path dstReportPath = Paths.get(EVALUATION_PATH + sep + "results" + sep + fileName + "_dst.json");
                Path srcDetectionPath = Paths.get(EVALUATION_PATH + sep + "mutants" + sep + ruleName + sep + fileName + ".java");
                Path dstDetectionPath = Paths.get(EVALUATION_PATH + sep + "decompile" + sep + ruleName + sep + fileName);
                PMDConfiguration srcConfig = new PMDConfiguration();
                srcConfig.setReportFormat("json");
                srcConfig.setReportFile(srcReportPath);
                srcConfig.addRuleSet(PMD_CONFIG_PATH);
                srcConfig.setIgnoreIncrementalAnalysis(true);
                PmdAnalysis srcAnalysis = PmdAnalysis.create(srcConfig);
                srcAnalysis.files().addFile(srcDetectionPath);
                srcAnalysis.performAnalysis();
                PMDConfiguration dstConfig = new PMDConfiguration();
                dstConfig.setReportFormat("json");
                dstConfig.setReportFile(dstReportPath);
                dstConfig.addRuleSet(PMD_CONFIG_PATH);
                dstConfig.setIgnoreIncrementalAnalysis(true);
                PmdAnalysis dstAnalysis = PmdAnalysis.create(dstConfig);
                try {
                    dstAnalysis.files().addDirectory(dstDetectionPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                dstAnalysis.performAnalysis();
                if(!srcReportPath.toFile().exists() || !dstReportPath.toFile().exists()) {
                    System.out.println("One Report does not exist!");
                    continue;
                }
                List<Report> srcReportList = PMD_Report.readResultFile(srcReportPath.toString());
                List<Report> dstReportList = PMD_Report.readResultFile(dstReportPath.toString());
                HashMap<String, Integer> srcBug2Num = new HashMap<>();
                HashMap<String, Integer> dstBug2Num = new HashMap<>();
                for (Report report : srcReportList) {
                    for(Violation violation : report.getViolations()) {
                        if(!srcBug2Num.containsKey(violation.getBugType())) {
                            srcBug2Num.put(violation.getBugType(), 0);
                        }
                        srcBug2Num.put(violation.getBugType(), srcBug2Num.get(violation.getBugType()) + 1);
                    }
                }
                for (Report report : dstReportList) {
                    for(Violation violation : report.getViolations()) {
                        if(!dstBug2Num.containsKey(violation.getBugType())) {
                            dstBug2Num.put(violation.getBugType(), 0);
                        }
                        dstBug2Num.put(violation.getBugType(), dstBug2Num.get(violation.getBugType()) + 1);
                    }
                }
                diffAnalysis(srcDetectionPath.toString(), dstDetectionPath.toString(), filepath2annotation.get(srcDetectionPath.toString()), srcBug2Num, dstBug2Num);
            }
        }
    }

    private static void runSpotBugs() {
        List<String> ruleNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "decompile", false);
        for (int i = 0; i < ruleNames.size(); i++) {
            String ruleName = ruleNames.get(i);
            String srcDetectionPath = EVALUATION_PATH + sep + "mutants" + sep + ruleName;
            String dstDetectionPath = EVALUATION_PATH + sep + "decompile" + sep + ruleName;
            List<String> dstFileNames = getFilenamesFromFolder(dstDetectionPath, false);
            File srcClassFolder = new File(EVALUATION_PATH + sep + "classes" + sep + ruleName + "_src");
            File dstClassFolder = new File(EVALUATION_PATH + sep + "classes" + sep + ruleName + "_dst");
            if (!srcClassFolder.exists()) {
                srcClassFolder.mkdir();
            }
            if (!dstClassFolder.exists()) {
                dstClassFolder.mkdir();
            }
            for (String fileNameWithPostfix : dstFileNames) {
                String fileName = removePostfix(fileNameWithPostfix);
                String srcReportPath = EVALUATION_PATH + sep + "results" + sep + fileName + "_src.xml";
                String dstReportPath = EVALUATION_PATH + sep + "results" + sep + fileName + "_dst.xml";
                if (!compileJavaSourceFile(srcDetectionPath, fileNameWithPostfix, srcClassFolder.getAbsolutePath())
                        || !compileJavaSourceFile(dstDetectionPath, fileNameWithPostfix, dstClassFolder.getAbsolutePath())) {
                    continue;
                }
                String[] srcInvokeCommands = new String[3];
                String[] dstInvokeCommands = new String[3];
                srcInvokeCommands[0] = "/bin/bash";
                dstInvokeCommands[0] = "/bin/bash";
                srcInvokeCommands[1] = "-c";
                dstInvokeCommands[1] = "-c";
                srcInvokeCommands[2] = SPOTBUGS_PATH + " -textui"
                        + " -xml:withMessages" + " -output " + srcReportPath + " "
                        + srcClassFolder.getAbsolutePath() + sep + fileName + ".class";
                dstInvokeCommands[2] = SPOTBUGS_PATH + " -textui"
                        + " -xml:withMessages" + " -output " + dstReportPath + " "
                        + dstClassFolder.getAbsolutePath() + sep + fileName + ".class";
                if(invokeCommandsByZT(srcInvokeCommands) && invokeCommandsByZT(dstInvokeCommands)) {
                    List<Report> srcReportList = SpotBugs_Report.readResultFile(ruleName, srcReportPath);
                    List<Report> dstReportList = SpotBugs_Report.readResultFile(ruleName, dstReportPath);
                    HashMap<String, Report> srcPath2Report = new HashMap<>();
                    for (Report report : srcReportList) {
                        srcPath2Report.put(report.getFilePath(), report);
                    }
                    for (Report dstReport : dstReportList) {
                        String mutantName = Path2Last(dstReport.getFilePath());
                        String srcMutantPath = EVALUATION_PATH + sep + "mutants" + sep + ruleName + sep + mutantName + ".java";
                        if (!srcPath2Report.containsKey(srcMutantPath)) {
                            failedSrcMutant.add(dstReport.getFilePath());
                            continue;
                        }
                        Report srcReport = srcPath2Report.get(srcMutantPath);
                        diffAnalysis(srcReport, dstReport, filepath2annotation.get(srcReport.getFilePath()));
                    }
                }
            }
        }
    }

    private static void runCheckStyle() {
        String configPath = PROJECT_PATH + sep + "tools" + sep + "google_check.xml";
        List<String> ruleNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "decompile", false);
        for (int i = 0; i < ruleNames.size(); i++) {
            String ruleName = ruleNames.get(i);
            String srcDetectionPath = EVALUATION_PATH + sep + "mutants" + sep + ruleName;
            String dstDetectionPath = EVALUATION_PATH + sep + "decompile" + sep + ruleName;
            List<String> dstFileNames = getFilenamesFromFolder(dstDetectionPath, false);
            for (String fileNameWithPostfix : dstFileNames) {
                String fileName = removePostfix(fileNameWithPostfix);
                String srcFilePath = srcDetectionPath + sep + fileNameWithPostfix;
                String dstFilePath = dstDetectionPath + sep + fileNameWithPostfix;
                String srcReportPath = EVALUATION_PATH + sep + "results" + sep + fileName + "_src.xml";
                String dstReportPath = EVALUATION_PATH + sep + "results" + sep + fileName + "_dst.xml";
                String[] srcInvokeCommands = new String[3];
                String[] dstInvokeCommands = new String[3];
                srcInvokeCommands[0] = "/bin/bash";
                dstInvokeCommands[0] = "/bin/bash";
                srcInvokeCommands[1] = "-c";
                dstInvokeCommands[1] = "-c";
                srcInvokeCommands[2] = JAVA_PATH + " -jar " + CHECKSTYLE_PATH + " -f" + " plain" + " -o " + srcReportPath + " -c " + configPath + " " + srcFilePath;
                dstInvokeCommands[2] = JAVA_PATH + " -jar " + CHECKSTYLE_PATH + " -f" + " plain" + " -o " + dstReportPath + " -c " + configPath + " " + dstFilePath;
                if (invokeCommandsByZT(srcInvokeCommands) && invokeCommandsByZT(dstInvokeCommands)) {
                    Report srcReport = CheckStyle_Report.readSingleResultFile(srcFilePath, srcReportPath);
                    Report dstReport = CheckStyle_Report.readSingleResultFile(dstFilePath, dstReportPath);
                    if (srcReport == null || dstReport == null) {
                        failedSrcMutant.add(dstReport.getFilePath());
                        continue;
                    }
                    diffAnalysis(srcReport, dstReport, filepath2annotation.get(srcReport.getFilePath()));
                } else {
                    System.out.println("Invoke CheckStyle error: " + srcFilePath);
                }
            }
        }
    }

    private static void runInfer() {
        List<String> ruleNames = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "decompile", false);
        for (int i = 0; i < ruleNames.size(); i++) {
            String ruleName = ruleNames.get(i);
            List<String> dstFileNames = getFilenamesFromFolder(EVALUATION_PATH + sep + "decompile" + sep + ruleName, false);
            for (int j = 0; j < dstFileNames.size(); j++) {
                String fileNameWithPostfix = dstFileNames.get(j);
                String fileName = removePostfix(fileNameWithPostfix);
                String srcDetectionPath = EVALUATION_PATH + sep + "mutants" + sep + ruleName + sep + fileName + ".java";
                String dstDetectionPath = EVALUATION_PATH + sep + "decompile" + sep + ruleName + sep + fileName + ".java";
                File srcReportFolder = new File(EVALUATION_PATH + sep + "results" + sep + fileName + "_src");
                File dstReportFolder = new File(EVALUATION_PATH + sep + "results" + sep + fileName + "_dst");
                String[] srcInvokeCommands = new String[3];
                String[] dstInvokeCommands = new String[3];
                String srcCmd = INFER_PATH + " run -o " + srcReportFolder.getAbsolutePath() + " -- javac " +
                        " -d " + classFolder.getAbsolutePath() +
                        " -cp " + inferDependencyJarStr + annotationJarStr.substring(1) + " " + srcDetectionPath;
                String dstCmd = INFER_PATH + " run -o " + dstReportFolder.getAbsolutePath() + " -- javac " +
                        " -d " + classFolder.getAbsolutePath() +
                        " -cp " + inferDependencyJarStr + annotationJarStr.substring(1) + " " + dstDetectionPath;
                srcInvokeCommands[0] = "/bin/bash";
                dstInvokeCommands[0] = "/bin/bash";
                srcInvokeCommands[1] = "-c";
                dstInvokeCommands[1] = "-c";
                srcInvokeCommands[2] = srcCmd;
                dstInvokeCommands[2] = dstCmd;
                invokeCommandsByZT(srcInvokeCommands);
                invokeCommandsByZT(dstInvokeCommands);
                if(DEBUG) {
                    System.out.println("Infer invocation: " + srcCmd);
                    System.out.println("Infer invocation: " + dstCmd);
                }
                File srcReportFile = new File(srcReportFolder.getAbsolutePath() + sep + "report.json");
                File dstReportFile = new File(dstReportFolder.getAbsolutePath() + sep + "report.json");
                File newSrcReportFile = new File(EVALUATION_PATH + sep + "results" + sep + srcReportFolder.getName() + ".json");
                File newDstReportFile = new File(EVALUATION_PATH + sep + "results" + sep + dstReportFolder.getName() + ".json");
                if(!srcReportFile.exists() || !dstReportFile.exists()) {
                    try {
                        FileUtils.deleteDirectory(srcReportFolder);
                        FileUtils.deleteDirectory(dstReportFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                Report srcReport = Infer_Report.readSingleResultFile(srcDetectionPath, srcReportFile);
                Report dstReport = Infer_Report.readSingleResultFile(dstDetectionPath, dstReportFile);
                try {
                    FileUtils.moveFile(srcReportFile, newSrcReportFile);
                    FileUtils.moveFile(dstReportFile, newDstReportFile);
                    FileUtils.deleteDirectory(srcReportFolder);
                    FileUtils.deleteDirectory(dstReportFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(srcReport.getViolations().size() == dstReport.getViolations().size()) {
                    continue;
                }
                String tag;
                if (srcReport.getViolations().size() > dstReport.getViolations().size()) {
                    tag = "-----Warning in SRC-----";
                } else {
                    tag = "-----Warning in DST-----";
                }
                if (DEBUG) {
                    System.out.println(tag);
                    System.out.println("SRC Path: " + srcReport.getFilePath());
                    System.out.println("DST Path: " + dstReport.getFilePath());
                    System.out.println("Src Violation Size: " + srcReport.getViolations().size() + " Dst Violation Size: " + dstReport.getViolations().size());
                    System.out.println("----------");
                }
                diffAnalysis(srcReport, dstReport, filepath2annotation.get(srcReport.getFilePath()));
            }
        }
    }

    private static void runSonarQube() {
        List<String> dstFolderPaths = getDirectFilenamesFromFolder(EVALUATION_PATH + sep + "decompile", true);
        for(int i = 0; i < dstFolderPaths.size(); i++) {
            File dstFile = new File(dstFolderPaths.get(i));
            File srcFile = new File(EVALUATION_PATH + sep + "mutants" + sep + dstFile.getParentFile().getName() + sep + dstFile.getName());
            deleteSonarQubeProject(SONARQUBE_PROJECT_KEY);
            createSonarQubeProject(SONARQUBE_PROJECT_KEY);
            String[] srcInvokeCommands = new String[3];
            srcInvokeCommands[0] = "/bin/bash";
            srcInvokeCommands[1] = "-c";
            srcInvokeCommands[2] = SONAR_SCANNER_PATH + " -Dsonar.projectKey=" + SONARQUBE_PROJECT_KEY
                    + " -Dsonar.projectBaseDir=" + EVALUATION_PATH + sep + "mutants"
                    + " -Dsonar.sources=" + srcFile.getAbsolutePath() + " -Dsonar.host.url=http://localhost:9000"
                    + " -Dsonar.login=admin -Dsonar.password=123456";
            boolean srcHasExec = invokeCommandsByZT(srcInvokeCommands);
            if (srcHasExec) {
                waitTaskEnd();
            } else {
                return;
            }
            int srcBugCnt, dstBugCnt;
            String[] curlCommands = new String[4];
            curlCommands[0] = "curl";
            curlCommands[1] = "-u";
            curlCommands[2] = "admin:123456";
            curlCommands[3] = "http://localhost:9000/api/issues/search?p=1&ps=500&componentKeys=" + SONARQUBE_PROJECT_KEY;
            String srcOutput = invokeCommandsByZTWithOutput(curlCommands);
            String mutantName = dstFile.getName().substring(0, dstFile.getName().length() - 5);
            writeLinesToFile(srcOutput, EVALUATION_PATH + sep + "results" + sep + mutantName + "_src.json");
            deleteSonarQubeProject(SONARQUBE_PROJECT_KEY);
            createSonarQubeProject(SONARQUBE_PROJECT_KEY);
            String[] dstInvokeCommands = new String[3];
            dstInvokeCommands[0] = "/bin/bash";
            dstInvokeCommands[1] = "-c";
            dstInvokeCommands[2] = SONAR_SCANNER_PATH + " -Dsonar.projectKey=" + SONARQUBE_PROJECT_KEY
                    + " -Dsonar.projectBaseDir=" + EVALUATION_PATH + sep + "decompile"
                    + " -Dsonar.sources=" + dstFile.getAbsolutePath() + " -Dsonar.host.url=http://localhost:9000"
                    + " -Dsonar.login=admin -Dsonar.password=123456";
            boolean dstHasExec = invokeCommandsByZT(dstInvokeCommands);
            if (dstHasExec) {
                waitTaskEnd();
            } else {
                return;
            }
            curlCommands[0] = "curl";
            curlCommands[1] = "-u";
            curlCommands[2] = "admin:123456";
            curlCommands[3] = "http://localhost:9000/api/issues/search?p=1&ps=500&componentKeys=" + SONARQUBE_PROJECT_KEY;
            String dstOutput = invokeCommandsByZTWithOutput(curlCommands);
            writeLinesToFile(dstOutput, EVALUATION_PATH + sep + "results" + sep + mutantName + "_dst.json");
            List<Report> srcReports = SonarQube_Report.readResultFile(srcFile.getAbsolutePath(), srcOutput);
            List<Report> dstReports = SonarQube_Report.readResultFile(dstFile.getAbsolutePath(), dstOutput);
            if(srcReports.size() > 1 || dstReports.size() > 1) {
                System.out.println("Error in bug size: " + srcFile.getAbsolutePath());
                System.out.println("Error in bug size: " + srcFile.getAbsolutePath());
                continue;
            }
            Report srcReport = null, dstReport = null;
            if(srcReports.size() == 0) {
                srcBugCnt = 0;
            } else {
                srcReport = srcReports.get(0);
                srcBugCnt = srcReport.getViolations().size();
            }
            if(dstReports.size() == 0) {
                dstBugCnt = 0;
            } else {
                dstReport = dstReports.get(0);
                dstBugCnt = dstReport.getViolations().size();
            }
            if(srcBugCnt == dstBugCnt) {
                continue;
            }
            diffAnalysis(srcReport, dstReport, filepath2annotation.get(srcReport.getFilePath()));
        }
    }

}