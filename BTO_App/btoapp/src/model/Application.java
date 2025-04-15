package model;

import java.util.Date;
import model.abstracts.AApplication;
import model.enums.ApplicationStatus;
import model.enums.FlatType;

/**
 * Class representing a BTO application in the system.
 * Extends the AApplication abstract class.
 * 
 * @author Your Name
 * @version 1.0
 */
public class Application extends AApplication {
    
    /**
     * Constructor for Application.
     * 
     * @param applicationId The unique identifier for the application
     * @param applicant The applicant who submitted the application
     * @param project The project being applied for
     * @param selectedFlatType The type of flat selected by the applicant
     */
    public Application(String applicationId, Applicant applicant, Project project, FlatType selectedFlatType) {
        super(applicationId != null ? applicationId : generateId(applicant, project), 
              applicant, project, selectedFlatType);
        
        System.out.println("DEBUG: Created application with ID: " + getApplicationId());
    }
    
    /**
     * Generates a unique application ID based on applicant NRIC and project name.
     * 
     * @param applicant The applicant
     * @param project The project
     * @return A generated application ID
     */
    private static String generateId(Applicant applicant, Project project) {
        if (applicant != null && project != null) {
            String nricPart = applicant.getNric().substring(1, 8);
            String projectPart = project.getProjectName()
                .substring(0, Math.min(3, project.getProjectName().length()))
                .toUpperCase();
            
            String newId = "APP-" + nricPart + "-" + projectPart;
            System.out.println("DEBUG: Generated application ID: " + newId);
            return newId;
        } else {
            System.out.println("DEBUG: Cannot generate application ID - missing applicant or project");
            return "APP-UNKNOWN";
        }
    }
    
    /**
     * Checks if the application is currently active (pending or successful).
     * 
     * @return true if the application is active, false otherwise
     */
    public boolean isActive() {
        return getStatus() == ApplicationStatus.PENDING || 
               getStatus() == ApplicationStatus.SUCCESSFUL;
    }
    
    /**
     * Calculates the days remaining until the application period closes.
     * 
     * @return the number of days remaining, or 0 if the period has closed
     */
    public int getDaysRemainingUntilClosing() {
        Date now = new Date();
        Date closingDate = getProject().getApplicationClosingDate();
        
        if (now.after(closingDate)) {
            return 0;
        }
        
        // Calculate days difference
        long diffInMillis = closingDate.getTime() - now.getTime();
        return (int) (diffInMillis / (1000 * 60 * 60 * 24));
    }
    
    /**
     * Returns a detailed string representation of the application.
     * 
     * @return A string with detailed application information
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Application ID: ").append(getApplicationId()).append("\n");
        sb.append("Applicant: ").append(getApplicant().getName()).append(" (").append(getApplicant().getNric()).append(")\n");
        sb.append("Project: ").append(getProject().getProjectName()).append("\n");
        sb.append("Neighborhood: ").append(getProject().getNeighborhood()).append("\n");
        sb.append("Flat Type: ").append(getSelectedFlatType().getDisplayName()).append("\n");
        sb.append("Application Date: ").append(getApplicationDate()).append("\n");
        sb.append("Status: ").append(getStatus().getDisplayName()).append("\n");
        
        if (isWithdrawalRequested()) {
            sb.append("Withdrawal Requested: Yes\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Returns a string representation of the application.
     * 
     * @return A string with basic application information
     */
    @Override
    public String toString() {
        return "Application ID: " + getApplicationId() + 
               ", Project: " + getProject().getProjectName() + 
               ", Flat Type: " + getSelectedFlatType().getDisplayName() + 
               ", Status: " + getStatus().getDisplayName();
    }
}