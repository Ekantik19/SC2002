package com.bto.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bto.controller.abstracts.ABaseController;
import com.bto.controller.interfaces.IReportController;
import com.bto.model.Application;
import com.bto.model.HDBManager;
import com.bto.model.HDBOfficer;
import com.bto.model.Project;
import com.bto.model.Receipt;
import com.bto.model.Report;
import com.bto.model.enums.FlatType;

/**
 * Controller for report and receipt generation in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ReportController extends ABaseController implements IReportController {
    
    private Map<String, Report> reportMap;
    private Map<String, Receipt> receiptMap;
    private ProjectController projectController;
    private ApplicationController applicationController;
    
    /**
     * Constructor for ReportController.
     * 
     * @param projectController The project controller to use
     * @param applicationController The application controller to use
     */
    public ReportController(ProjectController projectController, ApplicationController applicationController) {
        this.reportMap = new HashMap<>();
        this.receiptMap = new HashMap<>();
        this.projectController = projectController;
        this.applicationController = applicationController;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Receipt generateBookingReceipt(String applicationId, HDBOfficer officer) {
        // Validate input
        if (!validateNotNullOrEmpty(applicationId, "Application ID") || 
            !validateNotNull(officer, "Officer")) {
            return null;
        }
        
        // Get application
        Application application = applicationController.viewApplication(applicationId);
        if (application == null) {
            System.out.println("Application not found");
            return null;
        }
        
        // Generate receipt
        Receipt receipt = officer.generateBookingReceipt(application);
        if (receipt != null) {
            // Add to map
            receiptMap.put(receipt.getReceiptId(), receipt);
        }
        
        return receipt;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Report generateProjectBookingReport(String projectId, String reportTitle, HDBManager manager) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNullOrEmpty(reportTitle, "Report Title") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        // Get project
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return null;
        }
        
        // Check if manager is in charge of project
        if (!project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project");
            return null;
        }
        
        // Generate report
        Report report = manager.generateReport(project, reportTitle);
        if (report != null) {
            // Add to map
            reportMap.put(report.getReportId(), report);
        }
        
        return report;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Report generateMaritalStatusReport(String projectId, String maritalStatus, HDBManager manager) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNullOrEmpty(maritalStatus, "Marital Status") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        // Validate marital status
        if (!maritalStatus.equalsIgnoreCase("Single") && !maritalStatus.equalsIgnoreCase("Married")) {
            System.out.println("Invalid marital status: " + maritalStatus);
            return null;
        }
        
        // Get base report
        Report baseReport = generateProjectBookingReport(projectId, 
                                                        "Bookings by " + maritalStatus + " Applicants", 
                                                        manager);
        if (baseReport == null) {
            return null;
        }
        
        // Filter by marital status
        Report filteredReport = baseReport.filterByMaritalStatus(maritalStatus);
        if (filteredReport != null) {
            // Add to map
            reportMap.put(filteredReport.getReportId(), filteredReport);
        }
        
        return filteredReport;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Report generateFlatTypeReport(String projectId, FlatType flatType, HDBManager manager) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(flatType, "Flat Type") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        // Get base report
        Report baseReport = generateProjectBookingReport(projectId, 
                                                        "Bookings for " + flatType.getDisplayName() + " Flats", 
                                                        manager);
        if (baseReport == null) {
            return null;
        }
        
        // Filter by flat type
        Report filteredReport = baseReport.filterByFlatType(flatType);
        if (filteredReport != null) {
            // Add to map
            reportMap.put(filteredReport.getReportId(), filteredReport);
        }
        
        return filteredReport;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Report generateAgeRangeReport(String projectId, int minAge, int maxAge, HDBManager manager) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return null;
        }
        
        // Validate age range
        if (minAge < 0 || maxAge < minAge) {
            System.out.println("Invalid age range: " + minAge + " - " + maxAge);
            return null;
        }
        
        // Get base report
        Report baseReport = generateProjectBookingReport(projectId, 
                                                        "Bookings by Applicants Aged " + minAge + " to " + maxAge, 
                                                        manager);
        if (baseReport == null) {
            return null;
        }
        
        // Filter by age range
        Report filteredReport = baseReport.filterByAgeRange(minAge, maxAge);
        if (filteredReport != null) {
            // Add to map
            reportMap.put(filteredReport.getReportId(), filteredReport);
        }
        
        return filteredReport;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exportReport(String reportId, String filePath, HDBManager manager) {
        // Validate input
        if (!validateNotNullOrEmpty(reportId, "Report ID") || 
            !validateNotNullOrEmpty(filePath, "File Path") || 
            !validateNotNull(manager, "Manager")) {
            return false;
        }
        
        // Get report
        Report report = reportMap.get(reportId);
        if (report == null) {
            System.out.println("Report not found");
            return false;
        }
        
        // Check if report project is managed by the manager
        if (!report.getProject().getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project");
            return false;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write report entries in CSV format
            writer.write("Name,NRIC,Age,Marital Status,Flat Type,Booking Date");
            writer.newLine();
            
            for (Report.ReportEntry entry : report.getEntries()) {
                writer.write(String.format("%s,%s,%d,%s,%s,%s",
                                          entry.getApplicantName(),
                                          entry.getApplicantNric(),
                                          entry.getApplicantAge(),
                                          entry.getMaritalStatus(),
                                          entry.getFlatType().getDisplayName(),
                                          entry.getBookingDate()));
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error exporting report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Report getReportById(String reportId) {
        if (!validateNotNullOrEmpty(reportId, "Report ID")) {
            return null;
        }
        
        return reportMap.get(reportId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getProjectReports(String projectId, HDBManager manager) {
        // Validate input
        if (!validateNotNullOrEmpty(projectId, "Project ID") || 
            !validateNotNull(manager, "Manager")) {
            return new ArrayList<>();
        }
        
        // Get project
        Project project = projectController.getProjectById(projectId);
        if (project == null) {
            System.out.println("Project not found");
            return new ArrayList<>();
        }
        
        // Check if manager is in charge of project
        if (!project.getManagerInCharge().getNric().equals(manager.getNric())) {
            System.out.println("Manager is not in charge of this project");
            return new ArrayList<>();
        }
        
        // Filter reports by project
        List<Report> projectReports = new ArrayList<>();
        for (Report report : reportMap.values()) {
            if (report.getProject().getProjectName().equals(project.getProjectName())) {
                projectReports.add(report);
            }
        }
        
        return projectReports;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportsByManager(HDBManager manager) {
        if (!validateNotNull(manager, "Manager")) {
            return new ArrayList<>();
        }
        
        // Filter reports by manager
        List<Report> managerReports = new ArrayList<>();
        for (Report report : reportMap.values()) {
            if (report.getProject().getManagerInCharge().getNric().equals(manager.getNric())) {
                managerReports.add(report);
            }
        }
        
        return managerReports;
    }
    
    /**
     * Gets a receipt by its ID.
     * 
     * @param receiptId The ID of the receipt to retrieve
     * @return The requested receipt if found, null otherwise
     */
    public Receipt getReceiptById(String receiptId) {
        if (!validateNotNullOrEmpty(receiptId, "Receipt ID")) {
            return null;
        }
        
        return receiptMap.get(receiptId);
    }
    
    /**
     * Gets all receipts for a specific application.
     * 
     * @param applicationId The ID of the application to get receipts for
     * @return A list of receipts for the specified application
     */
    public List<Receipt> getReceiptsByApplication(String applicationId) {
        if (!validateNotNullOrEmpty(applicationId, "Application ID")) {
            return new ArrayList<>();
        }
        
        // Filter receipts by application
        List<Receipt> applicationReceipts = new ArrayList<>();
        for (Receipt receipt : receiptMap.values()) {
            if (receipt.getApplicationId().equals(applicationId)) {
                applicationReceipts.add(receipt);
            }
        }
        
        return applicationReceipts;
    }
}