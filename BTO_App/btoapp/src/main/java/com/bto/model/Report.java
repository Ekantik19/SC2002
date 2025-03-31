package com.bto.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a report of BTO applications with filtering options.
 */
public class Report {
    private static int nextId = 1;
    
    private int reportID;
    private Map<String, Object> filters;
    private Date generationDate;
    private HDBManager manager;
    private String title;
    private List<Application> applications;
    private Map<String, Map<String, Integer>> statistics;
    private DataManager dataManager;
    
    /**
     * Constructor for Report with filters.
     * 
     * @param filters The filters to apply to the report
     * @param dataManager The data manager to use
     */
    public Report(Map<String, Object> filters, DataManager dataManager) {
        this.reportID = nextId++;
        this.filters = filters;
        this.generationDate = new Date();
        this.statistics = new HashMap<>();
        this.dataManager = dataManager;
    }
    
    /**
     * Constructor for Report with manager, title, and applications.
     * 
     * @param manager The HDB Manager generating the report
     * @param title The title of the report
     * @param applications The list of applications included in the report
     * @param dataManager The data manager to use
     */
    public Report(HDBManager manager, String title, List<Application> applications, DataManager dataManager) {
        this.reportID = nextId++;
        this.manager = manager;
        this.title = title;
        this.applications = applications;
        this.generationDate = new Date();
        this.filters = new HashMap<>();
        this.statistics = new HashMap<>();
        this.dataManager = dataManager;
    }
    /**
     * Add a statistic to the report.
     * 
     * @param name The name of the statistic
     * @param data The data for the statistic
     */
    public void addStatistic(String name, Map<String, Integer> data) {
        statistics.put(name, data);
    }
    
    /**
     * Generate a formatted report string.
     * 
     * @return The formatted report
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("=================================================\n");
        report.append("                  BTO APPLICATIONS REPORT        \n");
        report.append("=================================================\n");
        report.append("Report ID: ").append(reportID).append("\n");
        report.append("Date: ").append(generationDate).append("\n\n");
        
        // If we have applications directly, use them
        List<Application> reportApplications = applications;
        
        // Otherwise, get filtered applications
        if (reportApplications == null) {
            reportApplications = filterApplications();
        }
        
        report.append("Total Applications: ").append(reportApplications.size()).append("\n\n");
        
        if (filters != null && !filters.isEmpty()) {
            report.append("Filters Applied:\n");
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            report.append("\n");
        }
        
        if (statistics != null && !statistics.isEmpty()) {
            report.append("Statistics:\n");
            for (Map.Entry<String, Map<String, Integer>> stat : statistics.entrySet()) {
                report.append(stat.getKey()).append(":\n");
                for (Map.Entry<String, Integer> entry : stat.getValue().entrySet()) {
                    report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
            report.append("\n");
        }
        
        report.append("Application Details:\n");
        report.append(String.format("%-10s | %-15s | %-15s | %-15s | %-10s | %-10s\n", 
                      "App ID", "Applicant ID", "Project", "Status", "Age", "Marital"));
        report.append("--------------------------------------------------------------------------------\n");
        
        for (Application app : reportApplications) {
            report.append(String.format("%-10d | %-15s | %-15s | %-15s | %-10d | %-10s\n",
                          app.getApplicationID(),
                          app.getApplicant().getUserID(),
                          app.getProject().getProjectName(),
                          app.getStatus(),
                          app.getApplicant().getAge(),
                          app.getApplicant().getMaritalStatus()));
        }
        
        report.append("=================================================\n");
        
        return report.toString();
    }
    
    /**
     * Filter applications based on the report filters.
     * 
     * @return The filtered list of applications
     */
    public List<Application> filterApplications() {
        //List<Application> allApplications = DataManager.getInstance().getAllApplications();
        List<Application> allApplications = dataManager.getAllApplications();

        // If no filters, return all applications
        if (filters == null || filters.isEmpty()) {
            return allApplications;
        }
        
        // Apply filters
        List<Application> filteredApplications = new ArrayList<>(allApplications);
        
        // Filter by project if specified
        if (filters.containsKey("projectID")) {
            int projectID = (int) filters.get("projectID");
            filteredApplications = filteredApplications.stream()
                .filter(app -> app.getProject().getProjectID() == projectID)
                .collect(Collectors.toList());
        }
        
        // Filter by status if specified
        if (filters.containsKey("status")) {
            String status = (String) filters.get("status");
            filteredApplications = filteredApplications.stream()
                .filter(app -> app.getStatus().equals(status))
                .collect(Collectors.toList());
        }
        
        // Filter by marital status if specified
        if (filters.containsKey("maritalStatus")) {
            String maritalStatus = (String) filters.get("maritalStatus");
            filteredApplications = filteredApplications.stream()
                .filter(app -> app.getApplicant().getMaritalStatus().equals(maritalStatus))
                .collect(Collectors.toList());
        }
        
        // Filter by flat type if specified
        if (filters.containsKey("flatType")) {
            String flatType = (String) filters.get("flatType");
            filteredApplications = filteredApplications.stream()
                .filter(app -> flatType.equals(app.getFlatTypeBooked()))
                .collect(Collectors.toList());
        }
        
        return filteredApplications;
    }
    
    /**
     * Filter applications by a specific category.
     * 
     * @param category The category to filter by
     * @param value The value to match
     * @return The filtered list of applications
     */
    public List<Application> filterByCategory(String category, Object value) {
        Map<String, Object> tempFilters = filters;
        tempFilters.put(category, value);
        
        //Report tempReport = new Report(tempFilters);
        Report tempReport = new Report(tempFilters, dataManager);
        return tempReport.filterApplications();
    }
    
    // Getters
    public int getReportID() {
        return reportID;
    }
    
    public Map<String, Object> getFilters() {
        return filters;
    }
    
    public Date getGenerationDate() {
        return generationDate;
    }
    
    public HDBManager getManager() {
        return manager;
    }
    
    public String getTitle() {
        return title;
    }
    
    public List<Application> getApplications() {
        return applications;
    }

        /**
     * Get the statistics for this report.
     * 
     * @return A map of statistic names to their data
     */
    public Map<String, Map<String, Integer>> getStatistics() {
        return statistics;
    }
}