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
     * Authenticates a user with their credentials.
     * 
     * @param nric The user's NRIC
     * @param password The user's password
     * @return true if authentication is successful, false otherwise
     */
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