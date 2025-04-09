package com.bto.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.DataManager;
import com.bto.model.HDBManager;
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
     * @param user The user generating the report (must be HDB Manager)
     * @return The generated report object, or null if unauthorized
     */
    public Report generateBookingReport(User user) {
        // Verify user is an HDB Manager
        if (!(user instanceof HDBManager)) {
            return null;
        }
        
        HDBManager manager = (HDBManager) user;
        List<Application> allApplications = dataManager.getAllApplications();
        
        // Filter to only applications with status "Booked"
        List<Application> bookedApplications = allApplications.stream()
                .filter(app -> Application.STATUS_BOOKED.equalsIgnoreCase(app.getStatus()))
                .collect(Collectors.toList());
        
        Report report = new Report(manager, "All Booked Flats Report", bookedApplications, dataManager);
        dataManager.saveReport(report);
        return report;
    }

    /**
     * Generate a filtered report based on specific criteria.
     * 
     * @param user The user generating the report (must be HDB Manager)
     * @param filters Map of filter criteria
     * @return The generated report object, or null if unauthorized
     */
    public Report generateFilteredReport(User user, Map<String, Object> filters) {
        // Verify user is an HDB Manager
        if (!(user instanceof HDBManager)) {
            return null;
        }
        
        HDBManager manager = (HDBManager) user;
        List<Application> allApplications = dataManager.getAllApplications();
        
        // Start with booked applications
        List<Application> filteredApplications = allApplications.stream()
                .filter(app -> Application.STATUS_BOOKED.equalsIgnoreCase(app.getStatus()))
                .collect(Collectors.toList());
        
        // Apply filters if present
        if (filters != null && !filters.isEmpty()) {
            // Project Name Filter
            if (filters.containsKey("projectName")) {
                String projectName = (String) filters.get("projectName");
                if (projectName != null && !projectName.trim().isEmpty()) {
                    filteredApplications = filteredApplications.stream()
                        .filter(app -> app.getProject() != null && 
                                    projectName.trim().equalsIgnoreCase(app.getProject().getProjectName()))
                        .collect(Collectors.toList());
                }
            }
            
            // Flat Type Filter
            if (filters.containsKey("flatType")) {
                String flatType = (String) filters.get("flatType");
                if (flatType != null && !flatType.trim().isEmpty()) {
                    filteredApplications = filteredApplications.stream()
                        .filter(app -> flatType.trim().equalsIgnoreCase(app.getFlatTypeBooked()))
                        .collect(Collectors.toList());
                }
            }
            
            // Marital Status Filter
            if (filters.containsKey("maritalStatus")) {
                String maritalStatus = (String) filters.get("maritalStatus");
                if (maritalStatus != null && !maritalStatus.trim().isEmpty()) {
                    filteredApplications = filteredApplications.stream()
                        .filter(app -> {
                            User applicant = app.getApplicant();
                            return applicant instanceof Applicant && 
                                maritalStatus.trim().equalsIgnoreCase(((Applicant)applicant).getMaritalStatus());
                        })
                        .collect(Collectors.toList());
                }
            }
            
            // Age Filters
            if (filters.containsKey("minAge")) {
                int minAge = getIntValue(filters.get("minAge"));
                if (minAge > 0) {
                    filteredApplications = filteredApplications.stream()
                        .filter(app -> {
                            User applicant = app.getApplicant();
                            return applicant instanceof Applicant && 
                                ((Applicant)applicant).getAge() >= minAge;
                        })
                        .collect(Collectors.toList());
                }
            }
            
            if (filters.containsKey("maxAge")) {
                int maxAge = getIntValue(filters.get("maxAge"));
                if (maxAge > 0) {
                    filteredApplications = filteredApplications.stream()
                        .filter(app -> {
                            User applicant = app.getApplicant();
                            return applicant instanceof Applicant && 
                                ((Applicant)applicant).getAge() <= maxAge;
                        })
                        .collect(Collectors.toList());
                }
            }
        }
        
        // Generate report title based on applied filters
        String title = generateReportTitle(filters);
        
        // Create and save the report
        Report report = new Report(manager, title, filteredApplications, dataManager);
        dataManager.saveReport(report);
        return report;
    }
    
    /**
     * Get all reports for the manager.
     * 
     * @param manager The HDB Manager
     * @return List of reports for the manager
     */
    public List<Report> getManagerReports(HDBManager manager) {
        return dataManager.getAllReports().stream()
                .filter(r -> r.getManager() != null && 
                         r.getManager().getUserID().equals(manager.getUserID()))
                .collect(Collectors.toList());
    }
    
    /**
     * Create a filter map with provided criteria.
     * Utility method to simplify filter creation from the view.
     * 
     * @param projectName Project name filter
     * @param flatType Flat type filter
     * @param maritalStatus Marital status filter
     * @param minAge Minimum age filter
     * @param maxAge Maximum age filter
     * @return Map of filter criteria
     */
    public Map<String, Object> createFilterMap(
            String projectName, String flatType, String maritalStatus, int minAge, int maxAge) {
        Map<String, Object> filters = new HashMap<>();
        
        if (projectName != null && !projectName.isEmpty()) {
            filters.put("projectName", projectName);
        }
        
        if (flatType != null && !flatType.isEmpty()) {
            filters.put("flatType", flatType);
        }
        
        if (maritalStatus != null && !maritalStatus.isEmpty()) {
            filters.put("maritalStatus", maritalStatus);
        }
        
        if (minAge > 0) {
            filters.put("minAge", minAge);
        }
        
        if (maxAge > 0) {
            filters.put("maxAge", maxAge);
        }
        
        return filters;
    }
    
    /**
     * Helper method to safely convert an object to int.
     * 
     * @param obj The object to convert
     * @return The int value, or 0 if conversion fails
     */
    private int getIntValue(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * Generate a report title based on applied filters.
     * 
     * @param filters The filters applied to the report
     * @return A descriptive title for the report
     */
    private String generateReportTitle(Map<String, Object> filters) {
        StringBuilder titleBuilder = new StringBuilder("Filtered Report: ");
        boolean hasFilters = false;
        
        if (filters != null && !filters.isEmpty()) {
            if (filters.containsKey("projectName")) {
                String projectName = (String) filters.get("projectName");
                if (projectName != null && !projectName.trim().isEmpty()) {
                    titleBuilder.append("Project: ").append(projectName).append(", ");
                    hasFilters = true;
                }
            }
            
            if (filters.containsKey("flatType")) {
                String flatType = (String) filters.get("flatType");
                if (flatType != null && !flatType.trim().isEmpty()) {
                    titleBuilder.append("Flat Type: ").append(flatType).append(", ");
                    hasFilters = true;
                }
            }
            
            if (filters.containsKey("maritalStatus")) {
                String maritalStatus = (String) filters.get("maritalStatus");
                if (maritalStatus != null && !maritalStatus.trim().isEmpty()) {
                    titleBuilder.append("Marital Status: ").append(maritalStatus).append(", ");
                    hasFilters = true;
                }
            }
            
            if (filters.containsKey("minAge")) {
                int minAge = getIntValue(filters.get("minAge"));
                if (minAge > 0) {
                    titleBuilder.append("Min Age: ").append(minAge).append(", ");
                    hasFilters = true;
                }
            }
            
            if (filters.containsKey("maxAge")) {
                int maxAge = getIntValue(filters.get("maxAge"));
                if (maxAge > 0) {
                    titleBuilder.append("Max Age: ").append(maxAge).append(", ");
                    hasFilters = true;
                }
            }
        }
        
        return hasFilters ? 
            titleBuilder.substring(0, titleBuilder.length() - 2) : 
            "Filtered Report: All Booked Applications";
    }
}