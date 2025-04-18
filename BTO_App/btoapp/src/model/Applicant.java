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
     * Requests a withdrawal of the current application.
     * 
     * @return true if the withdrawal was successfully requested, false otherwise
     */
    public boolean requestWithdrawal() {
        if (currentApplication != null) {
            return currentApplication.requestWithdrawal();
        }
        return false;
    }
    
    /**
     * Adds an enquiry to the set of enquiries made by the applicant.
     *
     * @param enquiry the enquiry to be added
     */
    public void addEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            System.out.println("DEBUG: Adding enquiry " + enquiry.getEnquiryId() + 
                              " to applicant " + getName());
            enquiries.add(enquiry);
            System.out.println("DEBUG: Applicant now has " + enquiries.size() + " enquiries");
        }
    }

    /**
    * Removes an enquiry from the set of enquiries made by the applicant.
    *
    * @param enquiry the enquiry to be removed
    */
    public void removeEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            System.out.println("DEBUG: Removing enquiry " + enquiry.getEnquiryId() + 
                            " from applicant " + getName());
            boolean removed = enquiries.remove(enquiry);
            System.out.println("DEBUG: Removal result: " + removed);
        }
    }

    /**
     * Gets a list of enquiries made by the applicant.
     *
     * @return a list of enquiries made by the applicant
     */
    public List<Enquiry> getEnquiries() {
        System.out.println("DEBUG: Getting enquiries for applicant " + getName() + 
                        ", count: " + enquiries.size());
        return new ArrayList<>(enquiries);
    }

    /**
     * Gets an enquiry by its ID.
     * 
     * @param enquiryId The ID of the enquiry to retrieve
     * @return The enquiry with the specified ID, or null if not found
     */
    public Enquiry getEnquiryById(String enquiryId) {
        for (Enquiry enquiry : enquiries) {
            if (enquiry.getEnquiryId().equals(enquiryId)) {
                return enquiry;
            }
        }
        return null;
    }

    public boolean hasBookedFlat() {
        return bookedFlatType != null && bookedProject != null;
    }

    // Getters and Setters

    public Application getCurrentApplication() {
        return currentApplication;
    }

    public void setCurrentApplication(Application currentApplication) {
        this.currentApplication = currentApplication;
    }

    public FlatType getBookedFlatType() {
        return bookedFlatType;
    }

    public void setBookedFlatType(FlatType bookedFlatType) {
        this.bookedFlatType = bookedFlatType;
    }

    public Project getBookedProject() {
        return bookedProject;
    }

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