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

    private Date bookingDate;
    
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
            return newId;
        } else {
            System.out.println("Cannot generate application ID - missing applicant or project");
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
     * Sets the booking date for this application.
     * 
     * @param bookingDate The date when the flat was booked
     */
    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    /**
     * Gets the booking date for this application.
     * 
     * @return The booking date, or null if not booked
     */
    public Date getBookingDate() {
        return this.bookingDate;
    }
}