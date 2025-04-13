package model.interfaces;

import java.util.List;

import model.Application;
import model.Project;
import model.Receipt;
import model.Report;
import model.enums.FlatType;

/**
 * Interface for generating reports in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IReportGeneration {
    
    /**
     * Generates a receipt for a flat booking.
     * 
     * @param application The application to generate a receipt for
     * @return The generated receipt
     */
    Receipt generateBookingReceipt(Application application);
    
    /**
     * Generates a report of all booked flats for a project.
     * 
     * @param project The project to generate a report for
     * @return The generated report
     */
    Report generateProjectBookingReport(Project project);
    
    /**
     * Generates a report of all booked flats filtered by marital status.
     * 
     * @param project The project to generate a report for
     * @param maritalStatus The marital status to filter by ("Single" or "Married")
     * @return The generated report
     */
    Report generateMaritalStatusReport(Project project, String maritalStatus);
    
    /**
     * Generates a report of all booked flats filtered by flat type.
     * 
     * @param project The project to generate a report for
     * @param flatType The flat type to filter by
     * @return The generated report
     */
    Report generateFlatTypeReport(Project project, FlatType flatType);
    
    /**
     * Generates a report of all booked flats filtered by age range.
     * 
     * @param project The project to generate a report for
     * @param minAge The minimum age to include
     * @param maxAge The maximum age to include
     * @return The generated report
     */
    Report generateAgeRangeReport(Project project, int minAge, int maxAge);
    
    /**
     * Gets a list of all reports for a project.
     * 
     * @param project The project to get reports for
     * @return A list of all reports for the specified project
     */
    List<Report> getProjectReports(Project project);
    
    /**
     * Gets a report by ID.
     * 
     * @param reportId The ID of the report to retrieve
     * @return The report with the specified ID, or null if not found
     */
    Report getReportById(String reportId);
    
    /**
     * Exports a report to a file.
     * 
     * @param report The report to export
     * @param filePath The path to export the report to
     * @return true if the report was successfully exported, false otherwise
     */
    boolean exportReport(Report report, String filePath);
}