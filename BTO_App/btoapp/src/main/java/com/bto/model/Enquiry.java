package com.bto.model;

import java.util.Date;
import java.util.UUID;

/**
 * Represents an enquiry in the BTO Management System.
 */
public class Enquiry {
    private String enquiryId;
    private User user;
    private Project project;
    private String enquiryText;
    private String response;
    private Date creationDate;
    private Date responseDate;
    
    /**
     * Constructor for creating a new enquiry.
     * 
     * @param user The user who created the enquiry
     * @param project The project the enquiry is about
     * @param enquiryText The text of the enquiry
     */
    public Enquiry(User user, Project project, String enquiryText) {
        this.enquiryId = UUID.randomUUID().toString();
        this.user = user;
        this.project = project;
        this.enquiryText = enquiryText;
        this.creationDate = new Date();
    }
    
    /**
     * Get the enquiry ID.
     * 
     * @return The enquiry ID
     */
    public String getEnquiryId() {
        return enquiryId;
    }
    
    /**
     * Set the enquiry ID.
     * 
     * @param enquiryId The enquiry ID to set
     */
    public void setEnquiryId(String enquiryId) {
        this.enquiryId = enquiryId;
    }
    
    /**
     * Get the user who created the enquiry.
     * 
     * @return The user
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Get the project the enquiry is about.
     * 
     * @return The project
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Get the enquiry text.
     * 
     * @return The enquiry text
     */
    public String getEnquiryText() {
        return enquiryText;
    }
    
    /**
     * Update the enquiry text.
     * 
     * @param newText The new text for the enquiry
     */
    public void updateEnquiry(String newText) {
        this.enquiryText = newText;
    }
    
    /**
     * Get the response to the enquiry.
     * 
     * @return The response, or null if there is no response
     */
    public String getResponse() {
        return response;
    }
    
    /**
     * Add a response to the enquiry.
     * 
     * @param response The response to add
     */
    public void addResponse(String response) {
        this.response = response;
        this.responseDate = new Date();
    }
    
    /**
     * Get the creation date of the enquiry.
     * 
     * @return The creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }
    
    /**
     * Get the response date of the enquiry.
     * 
     * @return The response date, or null if there is no response
     */
    public Date getResponseDate() {
        return responseDate;
    }
    
    /**
     * Check if the enquiry has been responded to.
     * 
     * @return true if the enquiry has a response, false otherwise
     */
    public boolean hasResponse() {
        return response != null && !response.isEmpty();
    }
    
    @Override
    public String toString() {
        return "Enquiry from " + user.getUserID() + 
               " about Project " + project.getProjectName() + 
               ": " + enquiryText + 
               (hasResponse() ? "\nResponse: " + response : "");
    }

        /**
     * Get the enquiry ID (alias for getEnquiryId() for API consistency).
     * 
     * @return The enquiry ID
     */
    public int getEnquiryID() {
        try {
            return Integer.parseInt(enquiryId);
        } catch (NumberFormatException e) {
            // If the enquiryId is not parseable as an integer, return -1 or handle as appropriate
            return -1;
        }
    }
    
}