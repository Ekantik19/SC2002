package model.abstracts;

import model.enums.UserRole;

/**
 * Abstract class representing a user in the BTO Management System.
 * Contains common attributes and methods for all types of users.
 * 
 * @author Your Name
 * @version 1.0
 */
public abstract class AUser {
    
    private String name;
    private String nric;
    private int age;
    private String maritalStatus;
    private String password;
    private UserRole role;
    
    /**
     * Constructor for AUser.
     * 
     * @param name The name of the user
     * @param nric The NRIC (National Registration Identity Card) of the user
     * @param age The age of the user
     * @param maritalStatus The marital status of the user (Single/Married)
     * @param password The user's password
     * @param role The role of the user in the system
     */
    public AUser(String name, String nric, int age, String maritalStatus, String password, UserRole role) {
        this.name = name;
        this.nric = nric;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.password = password;
        this.role = role;
    }
    
    /**
     * Changes the user's password.
     * 
     * @param newPassword The new password
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
    
    /**
     * Checks if the user is married.
     * 
     * @return true if the user is married, false otherwise
     */
    public boolean isMarried() {
        return "Married".equalsIgnoreCase(maritalStatus);
    }
    
    // Getters and Setters
    
    /**
     * Retrieves the user's name.
     *
     * @return the name of the user
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the user's name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Retrieves the user's NRIC.
     *
     * @return the NRIC of the user
     */
    public String getNric() {
        return nric;
    }
    
    /**
     * Sets the user's NRIC.
     *
     * @param nric the NRIC to set
     */
    public void setNric(String nric) {
        this.nric = nric;
    }
    
    /**
     * Retrieves the user's age.
     *
     * @return the age of the user
     */
    public int getAge() {
        return age;
    }
    
    /**
     * Sets the user's age.
     *
     * @param age the age to set
     */
    public void setAge(int age) {
        this.age = age;
    }
    
    /**
     * Retrieves the user's marital status.
     *
     * @return the marital status of the user
     */
    public String getMaritalStatus() {
        return maritalStatus;
    }
    
    /**
     * Sets the user's marital status.
     *
     * @param maritalStatus the marital status to set
     */
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
    /**
     * Retrieves the user's password.
     * Protected to limit access to subclasses.
     * 
     * @return The user's password
     */
    protected String getPassword() {
        return password;
    }

    /**
     * Retrieves the user's role.
     *
     * @return the role of the user
     */
    public UserRole getRole() {
        return role;
    }
    /**
     * Sets the user's role.
     * Protected to limit role modification to subclasses.
     * 
     * @param role The new role for the user
     */
    protected void setRole(UserRole role) {
        this.role = role;
    }
    
    /**
     * Returns a string representation of the user.
     * 
     * @return A string containing the user's name, NRIC, and role
     */
    @Override
    public String toString() {
        return "Name: " + name + ", NRIC: " + nric + ", Role: " + role.getDisplayName();
    }
}