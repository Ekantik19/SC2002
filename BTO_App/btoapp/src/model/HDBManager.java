package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import model.enums.ApplicationStatus;
import model.enums.FlatType;
import model.enums.UserRole;

/**
 * Class representing an HDB Manager in the BTO Management System.
 * Extends the User class directly, representing a distinct role from HDBOfficer.
 * 
 * @author Your Name
 * @version 1.0
 */
public class HDBManager extends User {
    
    private List<Project> createdProjects;
    private List<Report> generatedReports;
    
    /**
     * Constructor for HDBManager.
     * 
     * @param name The name of the manager
     * @param nric The NRIC of the manager
     * @param age The age of the manager
     * @param maritalStatus The marital status of the manager
     * @param password The manager's password
     */
    public HDBManager(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
        this.createdProjects = new ArrayList<>();
        this.generatedReports = new ArrayList<>();
        setRole(UserRole.MANAGER);
    }
    
    /**
     * Checks if the manager is available to handle a project during a specific period.
     * 
     * @param openingDate The opening date of the application period
     * @param closingDate The closing date of the application period
     * @return true if the manager is available, false otherwise
     */
    private boolean isAvailableDuringPeriod(Date openingDate, Date closingDate) {
        for (Project project : createdProjects) {
            // Check if the manager is already handling a project during this period
            if (project.getApplicationOpeningDate().before(closingDate) && 
                project.getApplicationClosingDate().after(openingDate)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Creates a new BTO project.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param flatTypes The types of flats available in the project
     * @param numberOfUnits The number of units for each flat type
     * @param sellingPrices The selling prices for each flat type
     * @param openingDate The application opening date
     * @param closingDate The application closing date
     * @param officerSlots The number of officer slots for the project
     * @return the created project if successful, null otherwise
     */
    public Project createProject(String projectName, String neighborhood, List<FlatType> flatTypes, 
                               List<Integer> numberOfUnits, List<Double> sellingPrices, 
                               Date openingDate, Date closingDate, int officerSlots) {
        // Check if the manager is available during this period
        if (!isAvailableDuringPeriod(openingDate, closingDate)) {
            return null;
        }
        
        // Validate input parameters
        if (flatTypes.size() != numberOfUnits.size() || flatTypes.size() != sellingPrices.size()) {
            return null;
        }
        
        // Create a new project
        Project project = new Project(projectName, neighborhood, openingDate, closingDate, this, officerSlots);
        
        // Add flat types
        for (int i = 0; i < flatTypes.size(); i++) {
            project.addFlatType(flatTypes.get(i), numberOfUnits.get(i), sellingPrices.get(i));
        }
        
        // Add to created projects
        createdProjects.add(project);
        
        return project;
    }
    
    /**
     * Updates an existing BTO project.
     * 
     * @param project The project to update
     * @param projectName The new name of the project
     * @param neighborhood The new neighborhood of the project
     * @param openingDate The new application opening date
     * @param closingDate The new application closing date
     * @param officerSlots The new number of officer slots for the project
     * @return true if the project was successfully updated, false otherwise
     */
    public boolean updateProject(Project project, String projectName, String neighborhood, 
                                Date openingDate, Date closingDate, int officerSlots) {
        // Check if the project is managed by this manager
        if (!project.getManagerInCharge().getNric().equals(this.getNric())) {
            return false;
        }
        
        // Update project details
        project.setProjectName(projectName);
        project.setNeighborhood(neighborhood);
        project.setApplicationOpeningDate(openingDate);
        project.setApplicationClosingDate(closingDate);
        
        // Update officer slots if possible
        if (project.getAssignedOfficers().size() <= officerSlots) {
            project.setOfficerSlots(officerSlots);
        }
        
        return true;
    }
    
    /**
     * Deletes a BTO project.
     * 
     * @param project The project to delete
     * @return true if the project was successfully deleted, false otherwise
     */
    public boolean deleteProject(Project project) {
        // Check if the project is managed by this manager
        if (!project.getManagerInCharge().getNric().equals(this.getNric())) {
            return false;
        }
        
        // Check if the project has any applications
        if (!project.getApplications().isEmpty()) {
            return false;
        }

        createdProjects.remove(project);
        
        return true;
    }
    
    /**
     * Toggles the visibility of a BTO project.
     * 
     * @param project The project to toggle visibility for
     * @param visible The new visibility status
     * @return true if the visibility was successfully toggled, false otherwise
     */
    public boolean toggleProjectVisibility(Project project, boolean visible) {
        // Check if the project is managed by this manager
        if (!project.getManagerInCharge().getNric().equals(this.getNric())) {
            return false;
        }
        
        project.setVisible(visible);
        return true;
    }
    
    /**
     * Approves an HDB officer's registration for a project.
     * 
     * @param officer The officer to approve
     * @return true if the approval was successful, false otherwise
     */
    public boolean approveOfficerRegistration(HDBOfficer officer) {
        // Check if the project is managed by this manager
        Project officerProject = officer.getAssignedProject();
        if (officerProject == null || !officerProject.getManagerInCharge().getNric().equals(this.getNric())) {
            return false;
        }
        
        return officer.approveRegistration();
    }
    
    /**
     * Rejects an HDB officer's registration for a project.
     * 
     * @param officer The officer to reject
     * @return true if the rejection was successful, false otherwise
     */
    public boolean rejectOfficerRegistration(HDBOfficer officer) {
        // Check if the project is managed by this manager
        Project officerProject = officer.getAssignedProject();
        if (officerProject == null || !officerProject.getManagerInCharge().getNric().equals(this.getNric())) {
            return false;
        }
        
        return officer.rejectRegistration();
    }
    
    /**
     * Generates a report for a project.
     * 
     * @param project The project to generate a report for
     * @param reportTitle The title of the report
     * @return The generated report
     */
    public Report generateReport(Project project, String reportTitle) {
        // Check if the project is managed by this manager
        if (!project.getManagerInCharge().getNric().equals(this.getNric())) {
            return null;
        }
        
        // Create a report ID
        String reportId = "RPT-" + project.getProjectName().substring(0, Math.min(3, project.getProjectName().length())).toUpperCase() + 
                         "-" + System.currentTimeMillis() % 10000;
        
        // Create a new report
        Report report = new Report(reportId, project, reportTitle, new Date());
        
        // Populate the report with all applications with BOOKED status
        report.populateFromApplications(project.getApplications().stream()
            .filter(app -> app.getStatus() == ApplicationStatus.BOOKED)
            .collect(Collectors.toList()));
        
        // Add to generated reports
        generatedReports.add(report);
        
        return report;
    }
}