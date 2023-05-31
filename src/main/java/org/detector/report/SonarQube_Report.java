package org.detector.report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.detector.util.Utility.EVALUATION_PATH;
import static org.detector.util.Utility.PROJECT_PATH;
import static org.detector.util.Utility.sep;

public class SonarQube_Report implements Report {

    private String filePath;
    private List<Violation> violations;

    public SonarQube_Report(String filePath) {
        this.filePath = filePath;
        this.violations = new ArrayList<>();
    }

    @Override
    public void addViolation(Violation violation) {
        this.violations.add(violation);
    }

    @Override
    public List<Violation> getViolations() {
        return this.violations;
    }

    @Override
    public String getFilePath() {
        return this.filePath;
    }

    public static SonarQube_Report readSingleResultFile(String filePath, String jsonContent) {
        JSONObject root = new JSONObject(jsonContent);
        int total = root.getInt("total");
        if(total > 10000) {
            return null;
        }
        SonarQube_Report report = new SonarQube_Report(filePath);
        if(total == 0) {
            return report;
        }
        JSONArray issues = root.getJSONArray("issues");
        for(int i = 0; i < issues.length(); i++) {
            try {
                JSONObject issue = (JSONObject) issues.get(i);
                if(issue.has("component") && issue.has("textRange")) {
                    String ruleName = issue.getString("rule");
                    String component = issue.getString("component");
                    String relativePath = component.split(":")[1];
                    String readFilePath;
                    if(relativePath.startsWith("seeds_checker1" + sep) || relativePath.startsWith("seeds" + sep)) {
                        readFilePath = PROJECT_PATH + sep + relativePath;
                    } else {
                        readFilePath = EVALUATION_PATH + sep + relativePath;
                    }
                    if(!filePath.equals(readFilePath)) {
                        System.out.println("Read Path: " + readFilePath);
                        System.out.println("Error in: " + filePath);
                    }
                    JSONObject textRange = (JSONObject) issue.get("textRange");
                    int startLine = textRange.getInt("startLine");
                    int endLine = textRange.getInt("endLine");
                    int startOffset = textRange.getInt("startOffset");
                    int endOffset = textRange.getInt("endOffset");
                    SonarQube_Violation violation = new SonarQube_Violation(ruleName, startLine, endLine, startOffset, endOffset);
                    report.addViolation(violation);
                }
            } catch (JSONException e) {
                System.err.println(jsonContent);
                e.printStackTrace();
            }
        }
        return report;
    }

    public static List<Report> readResultFile(String filePath, String jsonContent) {
        List<Report> reports = new ArrayList<>();
        Map<String, Report> path2report = new HashMap<>();
        JSONObject root = new JSONObject(jsonContent);
        int total = root.getInt("total");
        if(total > 10000) {
            return reports;
        }
        if(total == 0) {
            return reports;
        }
        JSONArray issues = root.getJSONArray("issues");
        for(int i = 0; i < issues.length(); i++) {
            try {
                JSONObject issue = (JSONObject) issues.get(i);
                if(issue.has("component") && issue.has("textRange")) {
                    String ruleName = issue.getString("rule");
                    String component = issue.getString("component");
                    String readFilePath = EVALUATION_PATH + sep + component.split(":")[1];
                    if(!filePath.equals(readFilePath)) {
                        System.err.println("Error in: " + filePath);
                    }
                    JSONObject textRange = (JSONObject) issue.get("textRange");
                    int startLine = textRange.getInt("startLine");
                    int endLine = textRange.getInt("endLine");
                    int startOffset = textRange.getInt("startOffset");
                    int endOffset = textRange.getInt("endOffset");
                    Report report;
                    if (path2report.containsKey(filePath)) {
                        report = path2report.get(filePath);
                    } else {
                        report = new SonarQube_Report(filePath);
                        path2report.put(filePath, report);
                    }
                    SonarQube_Violation violation = new SonarQube_Violation(ruleName, startLine, endLine, startOffset, endOffset);
                    report.addViolation(violation);
                }
            } catch (JSONException e) {
                System.err.println(jsonContent);
                e.printStackTrace();
            }
        }
        return reports;
    }

    @Override
    public String toString() {
        return "Path: " + this.filePath + "\nViolation Size: " + this.violations.size();
    }

}
