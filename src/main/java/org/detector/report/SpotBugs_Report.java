package org.detector.report;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static org.detector.util.Utility.DEBUG;

public class SpotBugs_Report implements Report {

    private String filePath;
    private List<Violation> violations;
    
    public SpotBugs_Report(String filePath) {
        this.filePath = filePath;
        this.violations = new ArrayList<>();
    }

    public void addViolation(Violation newViolation) {
        this.violations.add(newViolation);
    }

    public String getFilePath() {
        return this.filePath;
    }

    public List<Violation> getViolations() {
        return this.violations;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("SpotBugs Report: " + this.filePath + "\n");
        for(int i = 0; i < violations.size(); i++) {
            str.append(violations.get(i) + "\n");
        }
        return str.toString();
    }

    public static Report readSingleResultFile(String srcPath, String seedFolderName, String reportPath) {
        if (DEBUG) {
            System.out.println("SpotBugs Detection Result FileName: " + reportPath);
        }
        if(!srcPath.endsWith(".java")) {
            System.out.println("SpotBugs Src Path is not ended by .java!");
            System.exit(-1);
        }
        SpotBugs_Report report = new SpotBugs_Report(srcPath);
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(new File(reportPath));
            Element root = document.getRootElement();
            List<Element> bugInstances = root.elements("BugInstance");
            for (Element bugInstance : bugInstances) {
                List<Element> sourceLines = bugInstance.elements("SourceLine");
                for (Element sourceLine : sourceLines) {
                    SpotBugs_Violation violation = new SpotBugs_Violation(seedFolderName, sourceLine, bugInstance.attribute("type").getText());
                    report.addViolation(violation);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return report;
    }

    // Variable seedFolderPath contains sub seed folder name
    public static List<Report> readResultFile(String seedFolderPath, String reportPath) {
        if (DEBUG) {
            System.out.println("SpotBugs Detection Result FilePath: " + reportPath);
        }
        List<Report> reports = new ArrayList<>();
        HashMap<String, Report> filepath2report = new HashMap<>();
        SAXReader saxReader = new SAXReader();
        try {
            Document report = saxReader.read(new File(reportPath));
            Element root = report.getRootElement();
            List<Element> bugInstances = root.elements("BugInstance");
            for (Element bugInstance : bugInstances) {
                List<Element> sourceLines = bugInstance.elements("SourceLine");
                for (Element sourceLine : sourceLines) {
                    SpotBugs_Violation violation = new SpotBugs_Violation(seedFolderPath, sourceLine, bugInstance.attribute("type").getText());
                    String filepath = violation.getFilepath();
                    if (filepath2report.containsKey(filepath)) {
                        filepath2report.get(filepath).addViolation(violation);
                    } else {
                        SpotBugs_Report spotBugs_report = new SpotBugs_Report(filepath);
                        spotBugs_report.addViolation(violation);
                        filepath2report.put(filepath, spotBugs_report);
                        reports.add(spotBugs_report);
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return reports;
    }

}

