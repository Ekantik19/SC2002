// AAuthenticationController.java
package controller.abstracts;

import java.util.HashMap;
import java.util.Map;

import controller.interfaces.IAuthenticationController;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.User;
import model.enums.UserRole;

/**
 * Abstract class for Authentication Controller in the BTO Management System.
 * Implements common authentication-related functionality.
 * 
 * @author Your Name
 * @version 1.0
 */
public abstract class AAuthenticationController extends ABaseController implements IAuthenticationController {
    
    protected Map<String, User> userMap;
    protected User currentUser;
    
    /**
     * Constructor for AAuthenticationController.
     */
    public AAuthenticationController() {
        userMap = new HashMap<>();
        currentUser = null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public User login(String nric, String password) {
        // Debug outputs
        System.out.println("DEBUG: Attempting login with NRIC: " + nric);
        System.out.println("DEBUG: Total users in userMap: " + userMap.size());
        
        // Validate input
        if (!validateNotNullOrEmpty(nric, "NRIC") || !validateNotNullOrEmpty(password, "Password")) {
            System.out.println("DEBUG: Invalid input - NRIC or password empty");
            return null;
        }
        
        // Normalize NRIC
        nric = nric.toUpperCase();
        
        // Debug output for all users in the map
        System.out.println("DEBUG: Users in map:");
        for (String key : userMap.keySet()) {
            System.out.println("DEBUG: - " + key + ": " + userMap.get(key).getName());
        }
        
        // Check if user exists
        User user = userMap.get(nric);
        if (user == null) {
            System.out.println("DEBUG: User not found for NRIC: " + nric);
            return null;
        }
        
        System.out.println("DEBUG: Found user: " + user.getName());
        System.out.println("DEBUG: Stored password: " + user.getPassword());
        
        // Validate password
        if (user.authenticate(nric, password)) {
            System.out.println("DEBUG: Authentication successful");
            currentUser = user;
            return user;
        }
        
        System.out.println("DEBUG: Authentication failed - password mismatch");
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changePassword(String nric, String oldPassword, String newPassword) {
        // Validate input
        if (!validateNotNullOrEmpty(nric, "NRIC") || 
            !validateNotNullOrEmpty(oldPassword, "Old Password") || 
            !validateNotNullOrEmpty(newPassword, "New Password")) {
            return false;
        }
        
        // Normalize NRIC
        nric = nric.toUpperCase();
        
        // Check if user exists
        User user = userMap.get(nric);
        if (user == null) {
            return false;
        }
        
        // Change password
        return user.changePassword(oldPassword, newPassword);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateNRICFormat(String nric) {
        if (nric == null) {
            return false;
        }
        
        // NRIC format: Starts with S or T, followed by 7 digits, ends with a letter
        return nric.matches("^[ST]\\d{7}[A-Z]$");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByNRIC(String nric) {
        if (!validateNotNullOrEmpty(nric, "NRIC")) {
            return null;
        }
        
        return userMap.get(nric.toUpperCase());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Applicant getApplicantByNRIC(String nric) {
        User user = getUserByNRIC(nric);
        if (user instanceof Applicant) {
            return (Applicant) user;
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HDBOfficer getOfficerByNRIC(String nric) {
        User user = getUserByNRIC(nric);
        if (user instanceof HDBOfficer) {
            return (HDBOfficer) user;
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HDBManager getManagerByNRIC(String nric) {
        User user = getUserByNRIC(nric);
        if (user instanceof HDBManager) {
            return (HDBManager) user;
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public UserRole getUserRole(String nric) {
        User user = getUserByNRIC(nric);
        if (user != null) {
            return user.getRole();
        }
        return null;
    }
    
    /**
     * Gets the current logged-in user.
     * 
     * @return The current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Adds a user to the user map.
     * 
     * @param user The user to add
     * @return true if the user was successfully added, false otherwise
     */
    protected boolean addUser(User user) {
        if (!validateNotNull(user, "User")) {
            return false;
        }
        
        if (userMap.containsKey(user.getNric())) {
            return false;
        }
        
        userMap.put(user.getNric(), user);
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadUserData(String filePath) {
        // This is an abstract method that will be implemented by concrete subclasses
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveUserData(String filePath) {
        // This is an abstract method that will be implemented by concrete subclasses
        return false;
    }
}