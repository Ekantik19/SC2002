package model;

import enquiry.Enquiry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.enums.FlatType;

/**
 * Class representing an applicant in the BTO Management System.
 * Extends the User class with functionality specific to BTO applicants.
 * 
 * @author Your Name
 * @version 1.0
 */
public class Applicant extends User {
    
    private Application currentApplication;
    private FlatType bookedFlatType;
    private Project bookedProject;
    private Set<Enquiry> enquiries;
    
    /**
     * Constructor for Applicant.
     * 
     * @param name The name of the applicant
     * @param nric The NRIC of the applicant
     * @param age The age of the applicant
     * @param maritalStatus The marital status of the applicant
     * @param password The applicant's password
     */
    public Applicant(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
        this.enquiries = new HashSet<>();
    }
    
    /**
     * Determines if the applicant has an active application.
     * 
     * @return true if the applicant has an active application, false otherwise
     */
    public boolean hasActiveApplication() {
        return currentApplication != null && 
               currentApplication.isActive();
    }
    
    /**
     * Adds an enquiry to the set of enquiries made by the applicant.
     *
     * @param enquiry the enquiry to be added
     */
    public void addEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            enquiries.add(enquiry);
        }
    }

    /**
    * Removes an enquiry from the set of enquiries made by the applicant.
    *
    * @param enquiry the enquiry to be removed
    */
    public void removeEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            System.out.println("Removing enquiry " + enquiry.getEnquiryId() + 
                            " from applicant " + getName());
            boolean removed = enquiries.remove(enquiry);
            System.out.println("Removal result: " + removed);
        }
    }

    /**
     * Gets a list of enquiries made by the applicant.
     *
     * @return a list of enquiries made by the applicant
     */
    public List<Enquiry> getEnquiries() {
        System.out.println("Getting enquiries for applicant " + getName() + 
                        ", count: " + enquiries.size());
        return new ArrayList<>(enquiries);
    }

    /**
     * Checks if the applicant has booked a flat.
     *
     * @return true if a flat has been booked, false otherwise
     */
    public boolean hasBookedFlat() {
        return bookedFlatType != null && bookedProject != null;
    }

    // Getters and Setters

    /**
     * Retrieves the current application of the applicant.
     *
     * @return The current application
     */
    public Application getCurrentApplication() {
        return currentApplication;
    }

    /**
     * Sets the current application for the applicant.
     *
     * @param currentApplication The application to set
     */
    public void setCurrentApplication(Application currentApplication) {
        this.currentApplication = currentApplication;
    }

    /**
     * Sets the flat type that the applicant has booked.
     *
     * @param bookedFlatType The flat type to set
     */
    public void setBookedFlatType(FlatType bookedFlatType) {
        this.bookedFlatType = bookedFlatType;
    }

    /**
     * Sets the project where the applicant has booked a flat.
     *
     * @param bookedProject The project to set
     */
    public void setBookedProject(Project bookedProject) {
        this.bookedProject = bookedProject;
    }

    /**
     * Returns a string representation of the Applicant.
     * 
     * @return A string with the applicant's details
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        
        if (currentApplication != null) {
            sb.append(", Current Application: ").append(currentApplication.getApplicationId());
        }
        
        if (bookedProject != null && bookedFlatType != null) {
            sb.append(", Booked: ").append(bookedProject.getProjectName())
            .append(" (").append(bookedFlatType.getDisplayName()).append(")");
        }
        
        return sb.toString();
    }
}