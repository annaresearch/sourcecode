package org.detector.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.detector.util.TriTuple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.detector.analysis.Schedule.path2bugNum;
import static org.detector.util.Utility.DEBUG;
import static org.detector.util.Utility.DIFFERENTIAL_TESTING;
import static org.detector.util.Utility.OFFSET_IMPACT;
import static org.detector.util.Utility.compactIssues;
import static org.detector.util.Utility.mutant2seed;

public class PMD_Report implements Report {

    private String filePath;  // filePath means source code file path.
    private List<Violation> violations;

    private PMD_Report(String filePath) {
        this.filePath = filePath;
        this.violations = new ArrayList<>();
    }

    public String getFilePath() {
        return this.filePath;
    }

    @Override
    public void addViolation(Violation newViolation) {
        this.violations.add(newViolation);
    }

    public List<Violation> getViolations() {
        return this.violations;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("PMD_Report Filename: " + this.filePath + "\n");
        for (Violation pmd_violation : this.violations) {
            out.append(pmd_violation.toString() + "\n");
        }
        return out.toString();
    }

    public Map<String, Integer> type2cnt() {
        Map<String, Integer> res = new HashMap<>();
        for(Violation violation : violations) {
            if(!res.containsKey(violation.getBugType())) {
                res.put(violation.getBugType(), 0);
            }
            res.put(violation.getBugType(), res.get(violation.getBugType()) + 1);
        }
        return res;
    }

    @Override
    public boolean equals(Object rhs) {
        if(rhs instanceof PMD_Report) {
            Map<String, Integer> type2cnt1 = this.type2cnt();
            Map<String, Integer> type2cnt2 = ((PMD_Report) rhs).type2cnt();
            if(type2cnt1.size() != type2cnt2.size()) {
                return false;
            }
            for(String type : type2cnt1.keySet()) {
                if(!type2cnt2.containsKey(type) || type2cnt1.get(type) != type2cnt2.get(type)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static List<String> errorPMDPaths = new ArrayList<>();

    public static Report readSingleResultFile(final String jsonPath) {
        Report report = null;
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(jsonPath);
        if(!jsonFile.exists()) {
            return null;
        }
        try {
            JsonNode rootNode = mapper.readTree(jsonFile);
            JsonNode reportNodes = rootNode.get("files");
            JsonNode processErrorNode = rootNode.get("processingErrors");
            JsonNode configErrorNode = rootNode.get("configurationErrors");
            if(reportNodes.size() > 0) {
                report = new PMD_Report(reportNodes.get(0).get("filename").asText());
            } else {
                return null;
            }
            if(processErrorNode.size() > 0 || configErrorNode.size() > 0) {
                errorPMDPaths.add(jsonPath);
                return report;
            }
            for (int i = 0; i < reportNodes.size(); i++) {
                JsonNode reportNode = reportNodes.get(i);
                JsonNode violationNodes = reportNode.get("violations");
                for (int j = 0; j < violationNodes.size(); j++) {
                    JsonNode violationNode = violationNodes.get(j);
                    PMD_Violation violation = new PMD_Violation(violationNode);
                    report.addViolation(violation);
                }
            }
        } catch (JsonProcessingException e) {
            System.err.println("Exceptional Json Path:" + jsonPath);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return report;
    }

    public static List<Report> readResultFile(final String jsonPath) {
        List<Report> reports = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(jsonPath);
        if(!jsonFile.exists()) {
            return reports;
        }
        try {
            JsonNode rootNode = mapper.readTree(jsonFile);
            JsonNode reportNodes = rootNode.get("files");
            JsonNode processErrorNode = rootNode.get("processingErrors");
            JsonNode configErrorNode = rootNode.get("configurationErrors");
            if(processErrorNode.size() > 0 || configErrorNode.size() > 0) {
                errorPMDPaths.add(jsonPath);
                return reports;
            }
            for (int i = 0; i < reportNodes.size(); i++) {
                JsonNode reportNode = reportNodes.get(i);
                PMD_Report newReport = new PMD_Report(reportNode.get("filename").asText());
                JsonNode violationNodes = reportNode.get("violations");
                for (int j = 0; j < violationNodes.size(); j++) {
                    JsonNode violationNode = violationNodes.get(j);
                    PMD_Violation violation = new PMD_Violation(violationNode);
                    newReport.addViolation(violation);
                }
                reports.add(newReport);
            }
        } catch (JsonProcessingException e) {
            System.err.println("Exceptional Json Path:" + jsonPath);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reports;
    }

//    // Differential analysis based on the impact offset.
//    public static void diffAnalysis4DiffTesting(PMD_Report srcReport, PMD_Report dstReport, String annotationName) {
//        Map<String, Integer> srcBug2Cnt = new HashMap<>();  // bug type -> count
//        Map<String, Integer> dstBug2Cnt = new HashMap<>();
//        for(Violation violation : srcReport.getViolations()) {
//            if(!srcBug2Cnt.containsKey(violation.getBugType())) {
//                srcBug2Cnt.put(violation.getBugType(), 0);
//            }
//            srcBug2Cnt.put(violation.getBugType(), srcBug2Cnt.get(violation.getBugType()) + 1);
//        }
//        for(Violation violation : dstReport.violations) {
//            if(!dstBug2Cnt.containsKey(violation.getBugType())) {
//                dstBug2Cnt.put(violation.getBugType(), 0);
//            }
//            dstBug2Cnt.put(violation.getBugType(), dstBug2Cnt.get(violation.getBugType()) + 1);
//        }
//        if(DEBUG) {
//            System.out.println("Source file report: " + srcReport.getFilePath());
//            for(Map.Entry<String, Integer> entry : srcBug2Cnt.entrySet()) {
//                System.out.println(entry.getKey() + " " + entry.getValue());
//            }
//            System.out.println("Mutant file report: " + dstReport.getFilePath());
//            for(Map.Entry<String, Integer> entry : dstBug2Cnt.entrySet()) {
//                System.out.println(entry.getKey() + " " + entry.getValue());
//            }
//        }
//        List<String> sourceWarnings = new ArrayList<>();
//        String initSeedPath = mutant2seed.get(srcReport.getFilePath());
//        if(initSeedPath.contains("_Validation")) {
//            initSeedPath = initSeedPath.replace("_Validation", "_Seeds");
//        }
//        HashMap<String, Integer> bugType2Num = path2bugNum.get(initSeedPath);  // impact effect
//        if(bugType2Num == null) {
//            System.out.println("Impact not found: " + initSeedPath);
//            System.out.println("Report Path: " + srcReport.getFilePath());
//            return;
//        }
//        for (Map.Entry<String, Integer> entry : dstBug2Cnt.entrySet()) {
//            int sourceBugNum, mutantBugNum;
//            String bugType = entry.getKey();
//            if (!srcBug2Cnt.containsKey(bugType)) {
//                sourceBugNum = 0; // Because mutant has, but source does not have.
//            } else {
//                sourceBugNum = srcBug2Cnt.get(bugType);
//            }
//            mutantBugNum = dstBug2Cnt.get(entry.getKey());
//            int dv;
//            if(bugType2Num.containsKey(bugType)) {
//                dv = (mutantBugNum - sourceBugNum) - bugType2Num.get(bugType);
//            } else {
//                dv = mutantBugNum - sourceBugNum;
//            }
//            if(dv < 0) {
//                sourceWarnings.add(bugType);
//            }
//        }
//        for (Map.Entry<String, Integer> entry : srcBug2Cnt.entrySet()) {
//            String bugType = entry.getKey();
//            if (!dstBug2Cnt.containsKey(bugType)) {
//                sourceWarnings.add(bugType);
//                if(bugType2Num.containsKey(bugType)) {
//                    if(bugType2Num.get(bugType) + entry.getValue() > 0) {
//                        sourceWarnings.add(bugType);
//                    }
//                } else {
//                    sourceWarnings.add(bugType);
//                }
//            }
//        }
//        for (int i = 0; i < sourceWarnings.size(); i++) {
//            String bugType = sourceWarnings.get(i);
//            if (!compactIssues.containsKey(bugType)) {
//                HashMap<String, List<TriTuple>> seq2paths = new HashMap<>();
//                compactIssues.put(bugType, seq2paths);
//            }
//            HashMap<String, List<TriTuple>> seq2paths = compactIssues.get(bugType);
//            if (!seq2paths.containsKey(annotationName)) {
//                ArrayList<TriTuple> paths = new ArrayList<>();
//                seq2paths.put(annotationName, paths);
//            }
//            List<TriTuple> paths = seq2paths.get(annotationName);
//            paths.add(new TriTuple(srcReport.getFilePath(), dstReport.getFilePath(), "FP"));
//        }
//    }
//
//    public static void diffAnalysis(PMD_Report srcReport, PMD_Report dstReport, String annotationName) {
//        if(DIFFERENTIAL_TESTING && OFFSET_IMPACT) {
//            diffAnalysis4DiffTesting(srcReport, dstReport, annotationName);
//            return;
//        }
//        // Checker 2 and Checker 3
//        if (srcReport.violations.size() != dstReport.violations.size()) { // Checking depth is to mutate initial seeds
//            // bug type -> count
//            Map<String, Integer> srcBug2Cnt = new HashMap<>();
//            for(Violation violation : srcReport.getViolations()) {
//                if(!srcBug2Cnt.containsKey(violation.getBugType())) {
//                    srcBug2Cnt.put(violation.getBugType(), 0);
//                }
//                srcBug2Cnt.put(violation.getBugType(), srcBug2Cnt.get(violation.getBugType()) + 1);
//            }
//            Map<String, Integer> dstBug2Cnt = new HashMap<>();
//            for(Violation violation : dstReport.violations) {
//                if(!dstBug2Cnt.containsKey(violation.getBugType())) {
//                    dstBug2Cnt.put(violation.getBugType(), 0);
//                }
//                dstBug2Cnt.put(violation.getBugType(), dstBug2Cnt.get(violation.getBugType()) + 1);
//            }
//            List<Map.Entry<String, Integer>> mutantWarnings = new ArrayList<>();
//            List<Map.Entry<String, Integer>> sourceWarnings = new ArrayList<>();
//            for (Map.Entry<String, Integer> entry : dstBug2Cnt.entrySet()) {
//                if (!srcBug2Cnt.containsKey(entry.getKey())) {
//                    mutantWarnings.add(entry); // Because mutant has, but source does not have.
//                } else {
//                    int source_bugCnt = srcBug2Cnt.get(entry.getKey());
//                    int mutant_bugCnt = dstBug2Cnt.get(entry.getKey());
//                    if (source_bugCnt == mutant_bugCnt) {
//                        continue;
//                    }
//                    if (source_bugCnt > mutant_bugCnt) {
//                        sourceWarnings.add(entry);
//                    } else {
//                        mutantWarnings.add(entry);
//                    }
//                }
//            }
//            for (Map.Entry<String, Integer> entry : srcBug2Cnt.entrySet()) {
//                if (!dstBug2Cnt.containsKey(entry.getKey())) {
//                    sourceWarnings.add(entry); // Because source file has, but mutant file does not have.
//                }
//            }
//            if(!DIFFERENTIAL_TESTING) {
//                for (int i = 0; i < mutantWarnings.size(); i++) {
//                    String bugType = mutantWarnings.get(i).getKey();
//                    if (!compactIssues.containsKey(bugType)) {
//                        HashMap<String, List<TriTuple>> seq2paths = new HashMap<>();
//                        compactIssues.put(bugType, seq2paths);
//                    }
//                    HashMap<String, List<TriTuple>> seq2paths = compactIssues.get(bugType);
//                    if (!seq2paths.containsKey(annotationName)) {
//                        ArrayList<TriTuple> paths = new ArrayList<>();
//                        seq2paths.put(annotationName, paths);
//                    }
//                    List<TriTuple> paths = seq2paths.get(annotationName);
//                    paths.add(new TriTuple(srcReport.getFilePath(), dstReport.getFilePath(), "FP"));
//                }
//            }
//            for (int i = 0; i < sourceWarnings.size(); i++) {
//                String bugType = sourceWarnings.get(i).getKey();
//                if (!compactIssues.containsKey(bugType)) {
//                    HashMap<String, List<TriTuple>> seq2paths = new HashMap<>();
//                    compactIssues.put(bugType, seq2paths);
//                }
//                HashMap<String, List<TriTuple>> seq2paths = compactIssues.get(bugType);
//                if (!seq2paths.containsKey(annotationName)) {
//                    ArrayList<TriTuple> paths = new ArrayList<>();
//                    seq2paths.put(annotationName, paths);
//                }
//                List<TriTuple> paths = seq2paths.get(annotationName);
//                paths.add(new TriTuple(srcReport.getFilePath(), dstReport.getFilePath(), "FN"));
//            }
//        }
//    }

}
