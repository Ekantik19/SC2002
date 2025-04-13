package model;

import model.abstracts.AUser;
import model.enums.UserRole;
import model.interfaces.IUserManagement;

/**
 * Class representing a user in the BTO Management System.
 * Implements the IUserManagement interface.
 * 
 * @author Your Name
 * @version 1.0
 */
public class User extends AUser implements IUserManagement {
    
    private static final String NRIC_PATTERN = "^[ST]\\d{7}[A-Z]$";
    
    /**
     * Constructor for User.
     * 
     * @param name The name of the user
     * @param nric The NRIC (National Registration Identity Card) of the user
     * @param age The age of the user
     * @param maritalStatus The marital status of the user (Single/Married)
     * @param password The user's password
     */
    public User(String name, String nric, int age, String maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password, UserRole.APPLICANT);
        
        if (!validateNRIC(nric)) {
            throw new IllegalArgumentException("Invalid NRIC format. NRIC must start with S or T, followed by 7 digits, and end with a letter.");
        }
    }
    
    /**
     * Determines if the user is eligible to apply for BTO projects.
     * 
     * @return true if the user is eligible, false otherwise
     */
    @Override
    public boolean isEligibleForBTO() {
        // Basic eligibility: Age 21+ for married, 35+ for singles
        if (isMarried()) {
            return getAge() >= 21;
        } else {
            return getAge() >= 35;
        }
    }
    
    /**
     * Authenticates a user with their credentials.
     * 
     * @param nric The user's NRIC
     * @param password The user's password
     * @return true if authentication is successful, false otherwise
     */
    // @Override
    // public boolean authenticate(String nric, String password) {
    //     return getNric().equals(nric) && validatePassword(password);
    // }

    // In User.java
    @Override
    public boolean authenticate(String nric, String password) {
        System.out.println("DEBUG: User.authenticate - checking NRIC: " + nric);
        System.out.println("DEBUG: User.authenticate - checking password: '" + password + "'");
        System.out.println("DEBUG: User.authenticate - stored NRIC: " + getNric());
        System.out.println("DEBUG: User.authenticate - stored password: '" + getPassword() + "'");
        
        return getNric().equals(nric) && validatePassword(password);
    }

    private boolean validatePassword(String password) {
        System.out.println("DEBUG: User.validatePassword - comparing '" + password + "' with '" + getPassword() + "'");
        boolean result = getPassword().equals(password);
        System.out.println("DEBUG: User.validatePassword - result: " + result);
        return result;
    }
    
    /**
     * Changes the user's password.
     * 
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if the password was successfully changed, false otherwise
     */
    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        if (validatePassword(oldPassword)) {
            changePassword(newPassword);
            return true;
        }
        return false;
    }
    
    /**
     * Validates the NRIC format.
     * 
     * @param nric The NRIC to validate
     * @return true if the NRIC is valid, false otherwise
     */
    @Override
    public boolean validateNRIC(String nric) {
        return nric != null && nric.matches(NRIC_PATTERN);
    }
    
    /**
     * Updates the user's profile.
     * 
     * @param name The user's name
     * @param age The user's age
     * @param maritalStatus The user's marital status
     * @return true if the profile was successfully updated, false otherwise
     */
    @Override
    public boolean updateProfile(String name, int age, String maritalStatus) {
        // Validation logic could be added here
        setName(name);
        setAge(age);
        setMaritalStatus(maritalStatus);
        return true;
    }
    
    /**
     * Logs out the current user.
     * 
     * @return true if logout was successful, false otherwise
     */
    @Override
    public boolean logout() {
        // This would be implemented in a controller class
        return true;
    }
    
    /**
     * Returns a string representation of the User.
     * 
     * @return A string with the user's details
     */
    @Override
    public String toString() {
        return super.toString() + ", Age: " + getAge() + ", Marital Status: " + getMaritalStatus();
    }

    /**
     * Gets the user's password.
     * This method provides public access to the password for authentication purposes.
     * 
     * @return The user's password
     */
    @Override
    public String getPassword() {
        return super.getPassword();
    }
}