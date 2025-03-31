package com.bto.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.DataManager;
import com.bto.model.HDBManager;
import com.bto.model.Project;
import com.bto.model.Report;
import com.bto.model.User;

/**
 * Controller for generating and managing reports in the BTO Management System.
 * Handles the business logic for generating various types of reports on applicant data.
 */
public class ReportController {
    private DataManager dataManager;
    
    /**
     * Constructor for ReportController.
     * 
     * @param dataManager The data manager for persistent storage
     */
    public ReportController(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    // public ReportController() {
    //     this.dataManager = DataManager.getInstance();
    // }
    
    /**
     * Check if the user is authorized to generate reports.
     * Only HDB Managers can generate reports.
     * 
     * @param user The user to check
     * @return True if the user is authorized, false otherwise
     */
    public boolean isAuthorizedForReports(User user) {
        return user instanceof HDBManager;
    }
    
    /**
     * Generate a report of all applications with flat bookings.
     * 
     * @param manager The HDB Manager generating the report
     * @return The generated report object
     */
    public Report generateBookingReport(HDBManager manager) {
        List<Application> allApplications = dataManager.getAllApplications();
        
        // Filter to only applications with status "Booked"
        List<Application> bookedApplications = allApplications.stream()
                .filter(app -> "Booked".equalsIgnoreCase(app.getStatus()))
                .collect(Collectors.toList());
        
        //Report report = new Report(manager, "All Booked Flats Report", bookedApplications);
        Report report = new Report(manager, "All Booked Flats Report", bookedApplications, dataManager);
        dataManager.saveReport(report);
        return report;
    }
    
    /**
     * Generate a filtered report based on specific criteria.
     * 
     * @param manager The HDB Manager generating the report
     * @param projectName Filter by project name (null for all projects)
     * @param flatType Filter by flat type (null for all types)
     * @param maritalStatus Filter by marital status (null for both)
     * @param minAge Minimum age filter (0 for no minimum)
     * @param maxAge Maximum age filter (0 for no maximum)
     * @return The generated report object
     */
    public Report generateFilteredReport(HDBManager manager, String projectName, 
                                        String flatType, Boolean maritalStatus, 
                                        int minAge, int maxAge) {
        List<Application> allApplications = dataManager.getAllApplications();
        
        // Only consider booked applications
        List<Application> filteredApplications = allApplications.stream()
                .filter(app -> "Booked".equalsIgnoreCase(app.getStatus()))
                .collect(Collectors.toList());
        
        // Apply filters
        if (projectName != null && !projectName.isEmpty()) {
            filteredApplications = filteredApplications.stream()
                    .filter(app -> app.getProject().getProjectName().equalsIgnoreCase(projectName))
                    .collect(Collectors.toList());
        }
        
        if (flatType != null && !flatType.isEmpty()) {
            filteredApplications = filteredApplications.stream()
                    .filter(app -> app.getFlatType().equalsIgnoreCase(flatType))
                    .collect(Collectors.toList());
        }
        
        if (maritalStatus != null) {
            filteredApplications = filteredApplications.stream()
                    .filter(app -> {
                        User user = app.getApplicant();
                        if (user instanceof Applicant) {
                            Applicant applicant = (Applicant) user;
                            return applicant.isMarried() == maritalStatus;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        if (minAge > 0) {
            filteredApplications = filteredApplications.stream()
                    .filter(app -> {
                        User user = app.getApplicant();
                        if (user instanceof Applicant) {
                            Applicant applicant = (Applicant) user;
                            return applicant.getAge() >= minAge;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        if (maxAge > 0) {
            filteredApplications = filteredApplications.stream()
                    .filter(app -> {
                        User user = app.getApplicant();
                        if (user instanceof Applicant) {
                            Applicant applicant = (Applicant) user;
                            return applicant.getAge() <= maxAge;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        // Build report title based on filters
        StringBuilder titleBuilder = new StringBuilder("Filtered Report: ");
        if (projectName != null && !projectName.isEmpty()) {
            titleBuilder.append("Project: ").append(projectName).append(", ");
        }
        if (flatType != null && !flatType.isEmpty()) {
            titleBuilder.append("Flat Type: ").append(flatType).append(", ");
        }
        if (maritalStatus != null) {
            titleBuilder.append(maritalStatus ? "Married, " : "Single, ");
        }
        if (minAge > 0) {
            titleBuilder.append("Min Age: ").append(minAge).append(", ");
        }
        if (maxAge > 0) {
            titleBuilder.append("Max Age: ").append(maxAge).append(", ");
        }
        
        // Remove trailing comma and space if present
        String title = titleBuilder.toString();
        if (title.endsWith(", ")) {
            title = title.substring(0, title.length() - 2);
        }
        
        //Report report = new Report(manager, title, filteredApplications);
        Report report = new Report(manager, title, filteredApplications, dataManager);
        dataManager.saveReport(report);
        return report;
    }
    
    /**
     * Generate a summary report with statistics on flat type distribution by marital status.
     * 
     * @param manager The HDB Manager generating the report
     * @return The generated report with added statistics
     */
    public Report generateStatisticalReport(HDBManager manager) {
        List<Application> bookedApplications = dataManager.getAllApplications().stream()
                .filter(app -> "Booked".equalsIgnoreCase(app.getStatus()))
                .collect(Collectors.toList());
        
        // Count applications by flat type and marital status
        Map<String, Integer> twoRoomMarried = new HashMap<>();
        Map<String, Integer> twoRoomSingle = new HashMap<>();
        Map<String, Integer> threeRoomMarried = new HashMap<>();
        Map<String, Integer> threeRoomSingle = new HashMap<>();
        
        for (Application app : bookedApplications) {
            User user = app.getApplicant();
            if (user instanceof Applicant) {
                Applicant applicant = (Applicant) user;
                boolean married = applicant.isMarried();
                String flatType = app.getFlatType();
                String projectName = app.getProject().getProjectName();
                
                if ("2-Room".equalsIgnoreCase(flatType)) {
                    if (married) {
                        twoRoomMarried.put(projectName, twoRoomMarried.getOrDefault(projectName, 0) + 1);
                    } else {
                        twoRoomSingle.put(projectName, twoRoomSingle.getOrDefault(projectName, 0) + 1);
                    }
                } else if ("3-Room".equalsIgnoreCase(flatType)) {
                    if (married) {
                        threeRoomMarried.put(projectName, threeRoomMarried.getOrDefault(projectName, 0) + 1);
                    } else {
                        threeRoomSingle.put(projectName, threeRoomSingle.getOrDefault(projectName, 0) + 1);
                    }
                }
            }
        }
        
        //Report report = new Report(manager, "Statistical Report: Flat Distribution by Marital Status", bookedApplications);
        Report report = new Report(manager, "Statistical Report: Flat Distribution by Marital Status", bookedApplications, dataManager);
        report.addStatistic("Married Applicants - 2-Room Flats", twoRoomMarried);
        report.addStatistic("Single Applicants - 2-Room Flats", twoRoomSingle);
        report.addStatistic("Married Applicants - 3-Room Flats", threeRoomMarried);
        report.addStatistic("Single Applicants - 3-Room Flats", threeRoomSingle);
        
        dataManager.saveReport(report);
        return report;
    }
    
    /**
     * Get all projects managed by a specific HDB Manager.
     * 
     * @param manager The HDB Manager
     * @return A list of projects managed by the manager
     */
    public List<Project> getManagerProjects(HDBManager manager) {
        List<Project> allProjects = dataManager.getAllProjects();
        return allProjects.stream()
                .filter(p -> p.getHdbManagerInCharge() != null && 
                             p.getHdbManagerInCharge().getUserID().equals(manager.getUserID()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get all previously generated reports.
     * 
     * @return A list of all reports
     */
    public List<Report> getAllReports() {
        return dataManager.getAllReports();
    }
    
    /**
     * Get reports generated by a specific HDB Manager.
     * 
     * @param manager The HDB Manager
     * @return A list of reports generated by the manager
     */
    public List<Report> getManagerReports(HDBManager manager) {
        List<Report> allReports = dataManager.getAllReports();
        return allReports.stream()
                .filter(r -> r.getManager().getUserID().equals(manager.getUserID()))
                .collect(Collectors.toList());
    }

        /**
     * Save a report to persistent storage.
     * 
     * @param report The report to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveReport(Report report) {
        try {
            dataManager.saveReport(report);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving report: " + e.getMessage());
            return false;
        }
    }
}