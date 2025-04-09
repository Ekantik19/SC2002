package com.bto.model.abstracts;

import com.bto.model.enums.UserRole;

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
     * Validates if the provided password matches the user's password.
     * 
     * @param inputPassword The password to validate
     * @return true if the password matches, false otherwise
     */
    public boolean validatePassword(String inputPassword) {
        return this.password.equals(inputPassword);
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
    
    /**
     * Checks if the user is eligible to apply for a BTO project.
     * This is an abstract method to be implemented by subclasses.
     * 
     * @return true if the user is eligible, false otherwise
     */
    public abstract boolean isEligibleForBTO();
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNric() {
        return nric;
    }
    
    public void setNric(String nric) {
        this.nric = nric;
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
    
    protected String getPassword() {
        return password;
    }
    
    public UserRole getRole() {
        return role;
    }
    
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