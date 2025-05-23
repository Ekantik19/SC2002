package controller.abstracts;

import controller.interfaces.IAuthenticationController;
import java.util.HashMap;
import java.util.Map;
import model.Applicant;
import model.HDBManager;
import model.HDBOfficer;
import model.User;
import model.enums.UserRole;

/**
 * Abstract class for Authentication Controller in the BTO Management System.
 * Implements common authentication-related functionality.
 * 
 * Key Features:
 * - User login and authentication
 * - Password change mechanism
 * - NRIC (National Registration Identity Card) validation
 * - User role and type management
 * 
 * The class uses a Map to store and manage users, allowing quick lookup by NRIC.
 * It supports different user types: Applicant, HDB Officer, and HDB Manager.
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
 
        // Validate input
        if (!validateNotNullOrEmpty(nric, "NRIC") || !validateNotNullOrEmpty(password, "Password")) {
            System.out.println("Invalid input - NRIC or password empty");
            return null;
        }
        
        // Normalize NRIC
        nric = nric.toUpperCase();
        
        // Check if user exists
        User user = userMap.get(nric);
        if (user == null) {
            System.out.println("User not found for NRIC: " + nric);
            return null;
        }
        
        System.out.println("Found user: " + user.getName());
        System.out.println("Stored password: " + user.getPassword());
        
        // Validate password
        if (user.authenticate(nric, password)) {
            System.out.println("Authentication successful");
            currentUser = user;
            return user;
        }
        
        System.out.println("Authentication failed - password mismatch");
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