package com.bto.model;

import java.util.List;
import java.util.Map;

/**
 * Abstract User class representing the base user type in the system.
 * All specific user types inherit from this class.
 */
public abstract class User {
    private String userID;
    private String password;
    private int age;
    private String maritalStatus;
    protected DataManager dataManager;
    
    /**
     * Constructor for User.
     * 
     * @param userID The unique identifier for the user (NRIC)
     * @param password The user's password
     * @param age The user's age
     * @param maritalStatus The user's marital status
     */
    public User(String userID, String password, int age, String maritalStatus) {
        this.userID = userID;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
    }
    
    /**
     * Authenticate user credentials.
     * 
     * @param inputPassword The password to verify
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    // /**
    //  * Change the user's password.
    //  * 
    //  * @param newPassword The new password to set
    //  */
    // public void changePassword(String newPassword) {
    //     this.password = newPassword;
    // }
        /**
     * Change the user's password.
     * 
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return true if password was successfully changed, false otherwise
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        // Verify the current password
        if (!this.password.equals(currentPassword)) {
            return false;
        }
        
        // Update the password
        this.password = newPassword;
        
        // Update user in data storage if needed
        // DataManager.getInstance().updateUser(this);
        
        return true;
    }
    
    /**
     * Abstract method for viewing projects.
     * Different user types will implement this differently based on their permissions.
     * 
     * @param filters Optional filters to apply to the project list
     * @return A list of projects that match the filters
     */
    public abstract List<Project> viewProjects(Map<String, Object> filters);
    
    /**
     * Submit an enquiry about a project.
     * 
     * @param project The project the enquiry is about
     * @param enquiryText The text of the enquiry
     * @return The created Enquiry object
     */
    public Enquiry submitEnquiry(Project project, String enquiryText) {
        Enquiry enquiry = new Enquiry(this, project, enquiryText);
        project.addEnquiry(enquiry);
        return enquiry;
    }
    
    // Getters and setters
    public String getUserID() {
        return userID;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getMaritalStatus() {
        return maritalStatus;
    }
    
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    ////
    // Add this to User.java
    public String getName() {
        // If you don't have actual names stored, return the userID (NRIC)
        return userID;
    }

    /**
     * Set the data manager for this user.
     * 
     * @param dataManager The data manager to use
     */
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

}