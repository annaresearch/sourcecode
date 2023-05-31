package org.detector.analysis;

import org.apache.commons.io.FileUtils;
import org.detector.report.CheckStyle_Report;
import org.detector.report.Infer_Report;
import org.detector.report.PMD_Report;
import org.detector.report.Report;
import org.detector.report.SonarQube_Report;
import org.detector.report.SpotBugs_Report;
import org.detector.util.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.detector.analysis.DiffAnalysis.diffAnalysis;
import static org.detector.util.Utility.CHECKSTYLE_MUTATION;
import static org.detector.util.Utility.CHECKSTYLE_PATH;
import static org.detector.util.Utility.EVALUATION_PATH;
import static org.detector.util.Utility.INFER_MUTATION;
import static org.detector.util.Utility.INFER_PATH;
import static org.detector.util.Utility.JAVA_PATH;
import static org.detector.util.Utility.MOCK_ANNOTATION_JAR_PATH;
import static org.detector.util.Utility.PMD_CONFIG_PATH;
import static org.detector.util.Utility.PMD_MUTATION;
import static org.detector.util.Utility.PROJECT_PATH;
import static org.detector.util.Utility.SONARQUBE_MUTATION;
import static org.detector.util.Utility.SONARQUBE_PROJECT_KEY;
import static org.detector.util.Utility.SONAR_SCANNER_PATH;
import static org.detector.util.Utility.SPOTBUGS_MUTATION;
import static org.detector.util.Utility.SPOTBUGS_PATH;
import static org.detector.util.Utility.compileJavaSourceFile;
import static org.detector.util.Utility.createSonarQubeProject;
import static org.detector.util.Utility.deleteSonarQubeProject;
import static org.detector.util.Utility.file_sep;
import static org.detector.util.Utility.inferDependencyJarStr;
import static org.detector.util.Utility.invokeCommandsByZT;
import static org.detector.util.Utility.invokeCommandsByZTWithOutput;
import static org.detector.util.Utility.sep;
import static org.detector.util.Utility.waitTaskEnd;
import static org.detector.util.Utility.writeLinesToFile;

public class InjectChecker {

    private static Map<TypeWrapper, List<TypeWrapper>> head2mutants = new HashMap<>();

    public static void run(List<String> seedPaths, List<AnnotationWrapper> annotations) {
        List<TypeWrapper> srcWrappers = new ArrayList<>();
        for (int i = 0; i < seedPaths.size(); i++) {
            String seedPath = seedPaths.get(i);
            TypeWrapper initSeed = new TypeWrapper(seedPath);
            srcWrappers.add(initSeed);
        }
        List<TypeWrapper> initWrappers = new ArrayList<>();
        for(int i = 0; i < srcWrappers.size(); i++) {
            TypeWrapper head = srcWrappers.get(i);
            List<TypeWrapper> mutants = new ArrayList<>();
            for (int j = 0; j < annotations.size(); j++) {
                AnnotationWrapper annotation = annotations.get(j);
                mutants.addAll(head.transformByAnnotationInsertion(annotation));
            }
            if (!head2mutants.containsKey(head)) {
                head2mutants.put(head, new ArrayList<>());
            }
            head2mutants.get(head).addAll(mutants);
            initWrappers.addAll(mutants);
        }
        System.out.println("Init Wrapper Size: " + initWrappers.size());
        if(PMD_MUTATION) {
            runPMD();
        }
        if(SPOTBUGS_MUTATION) {
            runSpotBugs();
        }
        if(CHECKSTYLE_MUTATION) {
            runCheckStyle();
        }
        if(INFER_MUTATION) {
            runInfer();
        }
        if(SONARQUBE_MUTATION) {
            runSonarQube();
        }
    }

    public static void runPMD() {
        for (Map.Entry<TypeWrapper, List<TypeWrapper>> entry : head2mutants.entrySet()) {
            int srcBugCnt, dstBugCnt;
            TypeWrapper srcWrapper = entry.getKey();
            String srcReportPath = EVALUATION_PATH + sep + "results" + sep + srcWrapper.getFileName() + ".json";
            String[] srcCmds = new String[3];
            srcCmds[0] = "/bin/bash";
            srcCmds[1] = "-c";
            invokeCommandsByZT(srcCmds);
            List<Report> reportList = PMD_Report.readResultFile(srcReportPath);
            if (reportList.size() > 1) {
                System.err.println("Not expected report list size! [1]");
                System.err.println("Error report path: " + srcReportPath);
                System.exit(-1);
            }
            Report srcReport = null, dstReport = null;
            if (reportList.size() == 0) {
                File srcReportFile = new File(srcReportPath);
                if (!srcReportFile.exists()) {
                    System.err.println("[1] RT Fail to run PMD: " + srcReportPath);
                    System.out.println("Check file: " + srcWrapper.getFilePath());
                    continue;
                }
                srcBugCnt = 0;
            } else {
                srcReport = reportList.get(0);
                srcBugCnt = srcReport.getViolations().size();
            }
            for (TypeWrapper initWrapper : entry.getValue()) {
                String dstReportPath = EVALUATION_PATH + sep + "results" + sep + initWrapper.getFileName() + ".json";
                String[] dstCmds = new String[3];
                dstCmds[0] = "/bin/bash";
                dstCmds[1] = "-c";
                invokeCommandsByZT(dstCmds);
                reportList = PMD_Report.readResultFile(dstReportPath);
                if (reportList.size() > 1) {
                    System.err.println("Not expected report list size! [2]");
                    System.err.println("Error report path: " + dstReportPath);
                    System.exit(-1);
                }
                if (reportList.size() == 0) {
                    File dstReportFile = new File(dstReportPath);
                    if (!dstReportFile.exists()) {
                        System.err.println("[2] RT Fail to run PMD: " + dstReportPath);
                        System.err.println("Check path: " + initWrapper.getFilePath());
                        continue;
                    }
                    dstBugCnt = 0;
                } else {
                    dstReport = reportList.get(0);
                    dstBugCnt = dstReport.getViolations().size();
                }
                if (srcBugCnt == dstBugCnt) {
                    continue;
                }
                if (srcReport != null && dstReport != null) {
                    diffAnalysis(srcReport, dstReport, initWrapper.getInsertedAnnotationWrapper().getAnnotationName());
                }
            }
        }
    }

    public static void runSpotBugs() {
        for (Map.Entry<TypeWrapper, List<TypeWrapper>> entry : head2mutants.entrySet()) {
            int srcBugCnt = 0, dstBugCnt = 0;
            TypeWrapper srcWrapper = entry.getKey();
            File srcClassFolder = new File(EVALUATION_PATH + sep + "classes" + sep + srcWrapper.getFileName());
            if (!srcClassFolder.exists()) {
                srcClassFolder.mkdir();
            }
            String srcReportPath = EVALUATION_PATH + sep + "results" + sep + srcWrapper.getFileName() + ".xml";
            if (!compileJavaSourceFile(srcWrapper.getFolderPath(), srcWrapper.getFileName() + ".java", srcClassFolder.getAbsolutePath())) {
                continue;
            }
            String[] invokeSrcCommands = new String[3];
            invokeSrcCommands[0] = "/bin/bash";
            invokeSrcCommands[1] = "-c";
            invokeSrcCommands[2] = SPOTBUGS_PATH + " -textui"
                    + " -xml:withMessages" + " -output " + srcReportPath + " " + srcClassFolder.getAbsolutePath();
            invokeCommandsByZT(invokeSrcCommands);
            File srcReportFile = new File(srcReportPath);
            if(!srcReportFile.exists()) {
                continue;
            }
            Report srcReport = null, dstReport = null;
            List<Report> reportList = SpotBugs_Report.readResultFile(srcWrapper.getFolderPath(), srcReportPath);
            if (reportList.size() == 0) {
                srcBugCnt = 0;
            } else {
                srcReport = reportList.get(0);
                srcBugCnt += srcReport.getViolations().size();
            }
            for (TypeWrapper dstWrapper : entry.getValue()) {
                String dstReportPath = EVALUATION_PATH + sep + "results" + sep + dstWrapper.getFileName() + ".xml";
                File dstClassFolder = new File(EVALUATION_PATH + sep + "classes" + sep + dstWrapper.getFileName());
                if (!dstClassFolder.exists()) {
                    dstClassFolder.mkdir();
                }
                if (!compileJavaSourceFile(dstWrapper.getFolderPath(), dstWrapper.getFileName() + ".java", dstClassFolder.getAbsolutePath())) {
                    continue;
                }
                String[] invokeDstCommands = new String[3];
                invokeDstCommands[0] = "/bin/bash";
                invokeDstCommands[1] = "-c";
                invokeDstCommands[2] = SPOTBUGS_PATH + " -textui"
                        + " -xml:withMessages" + " -output " + dstReportPath + " " + dstClassFolder.getAbsolutePath();
                invokeCommandsByZT(invokeDstCommands);
                reportList = SpotBugs_Report.readResultFile(dstWrapper.getFolderPath(), dstReportPath);
                if (reportList.size() == 0) {
                    dstBugCnt = 0;
                } else {
                    dstReport = reportList.get(0);
                    dstBugCnt = dstReport.getViolations().size();
                }
                if (srcBugCnt == dstBugCnt) {
                    continue;
                }
                if (srcReport != null && dstReport != null) {
                    diffAnalysis(srcReport, dstReport, dstWrapper.getInsertedAnnotationWrapper().getAnnotationName());
                }
            }
        }
    }

    public static void runCheckStyle() {
        String configPath = PROJECT_PATH + sep + "tools" + sep + "google_check.xml";
        for (Map.Entry<TypeWrapper, List<TypeWrapper>> entry : head2mutants.entrySet()) {
            int srcBugCnt, dstBugCnt;
            TypeWrapper srcWrapper = entry.getKey();
            String srcReportPath = EVALUATION_PATH + sep + "results" + sep + srcWrapper.getFileName() + ".txt";
            String[] invokeSrcCommands = new String[3];
            invokeSrcCommands[0] = "/bin/bash";
            invokeSrcCommands[1] = "-c";
            invokeSrcCommands[2] = JAVA_PATH + " -jar " + CHECKSTYLE_PATH + " -f" + " plain" + " -o " + srcReportPath + " -c " + configPath + " " + srcWrapper.getFilePath();
            invokeCommandsByZT(invokeSrcCommands);
            File srcReportFile = new File(srcReportPath);
            if(!srcReportFile.exists()) {
                continue;
            }
            List<Report> reportList = CheckStyle_Report.readResultFile(srcReportPath);
            Report srcReport = null, dstReport = null;
            if (reportList.size() == 0) {
                srcBugCnt = 0;
            } else {
                srcReport = reportList.get(0);
                srcBugCnt = srcReport.getViolations().size();
            }
            for (TypeWrapper dstWrapper : entry.getValue()) {
                String dstReportPath = EVALUATION_PATH + sep + "results" + sep + dstWrapper.getFileName() + ".txt";
                String[] invokeDstCommands = new String[3];
                invokeDstCommands[0] = "/bin/bash";
                invokeDstCommands[1] = "-c";
                invokeDstCommands[2] = JAVA_PATH + " -jar " + CHECKSTYLE_PATH + " -f" + " plain" + " -o " + dstReportPath + " -c " + configPath + " " + dstWrapper.getFilePath();
                invokeCommandsByZT(invokeDstCommands);
                File dstReportFile = new File(dstReportPath);
                if (!dstReportFile.exists()) {
                    continue;
                }
                reportList = CheckStyle_Report.readResultFile(dstReportPath);
                if (reportList.size() == 0) {
                    dstBugCnt = 0;
                } else {
                    dstReport = reportList.get(0);
                    dstBugCnt = dstReport.getViolations().size();
                }
                if (srcBugCnt == dstBugCnt) {
                    continue;
                }
                if (srcReport != null && dstReport != null) {
                    diffAnalysis(srcReport, dstReport, dstWrapper.getInsertedAnnotationWrapper().getAnnotationName());
                }
            }
        }
    }

    public static void runInfer() {
        for (Map.Entry<TypeWrapper, List<TypeWrapper>> entry : head2mutants.entrySet()) {
            TypeWrapper srcWrapper = entry.getKey();
            String srcFileName = srcWrapper.getFileName();
            File srcReportFolder = new File(EVALUATION_PATH + sep + "results" + sep + srcFileName);
            File srcClassFolder = new File(EVALUATION_PATH + sep + "classes" + sep + srcFileName);
            String srcDetectionPath = srcWrapper.getFilePath();
            String srcCmd = INFER_PATH + " run -o " + srcReportFolder.getAbsolutePath() + " -- javac " +
                    " -d " + srcClassFolder.getAbsolutePath() + sep + srcFileName +
                    " -cp " + inferDependencyJarStr + file_sep + MOCK_ANNOTATION_JAR_PATH + " " + srcDetectionPath;
            String[] srcInvokeCommands = {"/bin/bash", "-c", srcCmd};
            invokeCommandsByZT(srcInvokeCommands);
            File srcReportFile = new File(srcReportFolder.getAbsolutePath() + sep + "report.json");
            if(!srcReportFile.exists()) {
                try {
                    FileUtils.deleteDirectory(srcReportFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                continue;
            }
            Report srcReport = Infer_Report.readSingleResultFile(srcWrapper.getFilePath(), srcReportFile);
            try {
                File newSrcReportFile = new File(EVALUATION_PATH + sep + "results" + sep + srcReportFolder.getName() + ".json");
                FileUtils.moveFile(srcReportFile, newSrcReportFile);
                FileUtils.deleteDirectory(srcReportFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (TypeWrapper dstWrapper : entry.getValue()) {
                String dstFileName = dstWrapper.getFileName();
                File dstReportFolder = new File(EVALUATION_PATH + sep + "results" + sep + dstFileName);
                File dstClassFolder = new File(EVALUATION_PATH + sep + "classes" + sep + dstFileName);
                String dstDetectionPath = dstWrapper.getFilePath();
                String dstCmd = INFER_PATH + " run -o " + dstReportFolder.getAbsolutePath() + " -- javac " +
                        " -d " + dstClassFolder.getAbsolutePath() + sep + dstFileName +
                        " -cp " + inferDependencyJarStr + file_sep + MOCK_ANNOTATION_JAR_PATH + " " + dstDetectionPath;
                String[] dstInvokeCommands = {"/bin/bash", "-c", dstCmd};
                invokeCommandsByZT(dstInvokeCommands);
                File dstReportFile = new File(dstReportFolder.getAbsolutePath() + sep + "report.json");
                if(!dstReportFile.exists()) {
                    try {
                        File newDstReportFile = new File(EVALUATION_PATH + sep + "results" + sep + dstReportFolder.getName() + ".json");
                        System.out.println("newDst Path: " + newDstReportFile.getAbsolutePath());
                        FileUtils.moveFile(dstReportFile, newDstReportFile);
                        FileUtils.deleteDirectory(dstReportFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                Report dstReport = Infer_Report.readSingleResultFile(dstWrapper.getFilePath(), dstReportFile);
                try {
                    File newDstReportFile = new File(EVALUATION_PATH + sep + "results" + sep + dstReportFolder.getName() + ".json");
                    FileUtils.moveFile(dstReportFile, newDstReportFile);
                    FileUtils.deleteDirectory(dstReportFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (srcReport.getViolations().size() == dstReport.getViolations().size()) {
                    continue;
                }
                if (srcReport != null && dstReport != null) {
                    diffAnalysis(srcReport, dstReport, dstWrapper.getInsertedAnnotationWrapper().getAnnotationName());
                }
            }
        }
    }

    public static void runSonarQube() {
        for (Map.Entry<TypeWrapper, List<TypeWrapper>> entry : head2mutants.entrySet()) {
            TypeWrapper srcWrapper = entry.getKey();
            String[] invokeCommands = new String[3];
            deleteSonarQubeProject(SONARQUBE_PROJECT_KEY);
            createSonarQubeProject(SONARQUBE_PROJECT_KEY);
            invokeCommands[0] = "/bin/bash";
            invokeCommands[1] = "-c";
            invokeCommands[2] = SONAR_SCANNER_PATH
                    + " -Dsonar.projectKey=" + SONARQUBE_PROJECT_KEY
                    + " -Dsonar.projectBaseDir=" + PROJECT_PATH
                    + " -Dsonar.sources=" + srcWrapper.getFilePath()
                    + " -Dsonar.host.url=http://localhost:9000 -Dsonar.login=admin -Dsonar.password=123456";
            if(invokeCommandsByZT(invokeCommands)) {
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
            writeLinesToFile(output, EVALUATION_PATH + sep + "results" + sep + srcWrapper.getFileName() + ".json");
            Report srcReport = SonarQube_Report.readSingleResultFile(srcWrapper.getFilePath(), output);
            for (TypeWrapper dstWrapper : entry.getValue()) {
                deleteSonarQubeProject(SONARQUBE_PROJECT_KEY);
                createSonarQubeProject(SONARQUBE_PROJECT_KEY);
                invokeCommands[0] = "/bin/bash";
                invokeCommands[1] = "-c";
                invokeCommands[2] = SONAR_SCANNER_PATH
                        + " -Dsonar.projectKey=" + SONARQUBE_PROJECT_KEY
                        + " -Dsonar.projectBaseDir=" + EVALUATION_PATH
                        + " -Dsonar.sources=" + dstWrapper.getFilePath()
                        + " -Dsonar.host.url=http://localhost:9000 -Dsonar.login=admin -Dsonar.password=123456";
                if(invokeCommandsByZT(invokeCommands)) {
                    waitTaskEnd();
                } else {
                    continue;
                }
                curlCommands = new String[4];
                curlCommands[0] = "curl";
                curlCommands[1] = "-u";
                curlCommands[2] = "admin:123456";
                curlCommands[3] = "http://localhost:9000/api/issues/search?p=1&ps=500&componentKeys=" + SONARQUBE_PROJECT_KEY;
                output = invokeCommandsByZTWithOutput(curlCommands);
                writeLinesToFile(output, EVALUATION_PATH + sep + "results" + sep + dstWrapper.getFileName() + ".json");
                SonarQube_Report dstReport = SonarQube_Report.readSingleResultFile(dstWrapper.getFilePath(), output);
                if(Utility.DEBUG) {
                    if(srcReport == null) {
                        System.out.println("Not exist Src Report: " + srcReport.getFilePath());
                    }
                    if(dstReport == null) {
                        System.out.println("Not exist Dst Report: " + dstReport.getFilePath());
                    }
                }
                if (srcReport != null && dstReport != null) {
                    diffAnalysis(srcReport, dstReport, dstWrapper.getInsertedAnnotationWrapper().getAnnotationName());
                }
            }
        }
    }

}
