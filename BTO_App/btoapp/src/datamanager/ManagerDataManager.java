package datamanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.HDBManager;
import utils.FilePathConfig;

/**
 * Data manager for handling HDB Manager data operations.
 * Handles loading and saving manager data from/to the manager data file.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ManagerDataManager {
    
    private Map<String, HDBManager> managerMap;
    private String filePath;
    
    /**
     * Constructor for ManagerDataManager with specified file path.
     * 
     * @param filePath The path to the manager data file
     */
    public ManagerDataManager(String filePath) {
        this.filePath = filePath;
        this.managerMap = new HashMap<>();
    }
    
    /**
     * Default constructor that uses the default file path from FilePathConfig.
     */
    public ManagerDataManager() {
        this(FilePathConfig.MANAGER_LIST_PATH);
    }
    
    /**
     * Gets the current file path being used.
     * 
     * @return The file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Sets a new file path for the manager data file.
     * 
     * @param filePath The new file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Validates the NRIC format.
     * 
     * @param nric The NRIC to validate
     * @return true if the NRIC is valid, false otherwise
     */
    private boolean validateNRICFormat(String nric) {
        if (nric == null) {
            return false;
        }
        
        // NRIC should start with S or T, followed by 7 digits, and end with a letter
        return nric.matches("^[ST]\\d{7}[A-Z]$");
    }
    
    /**
     * Loads manager data from the file.
     * 
     * @return true if the data was successfully loaded, false otherwise
     */
    public boolean loadManagerData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip header line if it exists
            if ((line = reader.readLine()) != null && line.contains("Name") && line.contains("NRIC")) {
                // Skip the header
            }
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                
                if (parts.length < 5) {
                    System.out.println("Invalid manager data format: " + line);
                    continue;
                }
                
                String name = parts[0];
                String nric = parts[1];
                
                if (!validateNRICFormat(nric)) {
                    System.out.println("Invalid NRIC format for manager: " + nric);
                    continue;
                }
                
                int age;
                try {
                    age = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid age format for manager: " + parts[2]);
                    continue;
                }
                
                String maritalStatus = parts[3];
                String password = parts[4];
                
                // Create manager
                HDBManager manager = new HDBManager(name, nric, age, maritalStatus, password);
                
                // Add manager to map
                managerMap.put(nric.toUpperCase(), manager);
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error loading manager data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves manager data to the file.
     * 
     * @return true if the data was successfully saved, false otherwise
     */
    public boolean saveManagerData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Name\tNRIC\tAge\tMarital Status\tPassword");
            writer.newLine();
            
            // Write managers
            for (HDBManager manager : managerMap.values()) {
                writer.write(manager.getName() + "\t");
                writer.write(manager.getNric() + "\t");
                writer.write(manager.getAge() + "\t");
                writer.write(manager.getMaritalStatus() + "\t");
                writer.write(manager.getPassword());
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving manager data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Adds a manager to the manager map.
     * 
     * @param manager The manager to add
     * @return true if the manager was successfully added, false otherwise
     */
    public boolean addManager(HDBManager manager) {
        if (manager == null || !validateNRICFormat(manager.getNric())) {
            return false;
        }
        
        String nric = manager.getNric().toUpperCase();
        
        // Check if manager already exists
        if (managerMap.containsKey(nric)) {
            return false;
        }
        
        managerMap.put(nric, manager);
        return true;
    }
    
    /**
     * Removes a manager from the manager map.
     * 
     * @param nric The NRIC of the manager to remove
     * @return true if the manager was successfully removed, false otherwise
     */
    public boolean removeManager(String nric) {
        if (nric == null || !validateNRICFormat(nric)) {
            return false;
        }
        
        nric = nric.toUpperCase();
        
        // Check if manager exists
        if (!managerMap.containsKey(nric)) {
            return false;
        }
        
        managerMap.remove(nric);
        return true;
    }
    
    /**
     * Gets a manager by NRIC.
     * 
     * @param nric The NRIC of the manager to retrieve
     * @return The manager if found, null otherwise
     */
    public HDBManager getManagerByNric(String nric) {
        if (nric == null || !validateNRICFormat(nric)) {
            return null;
        }
        
        return managerMap.get(nric.toUpperCase());
    }
    
    /**
     * Gets all managers.
     * 
     * @return A list of all managers
     */
    public List<HDBManager> getAllManagers() {
        return new ArrayList<>(managerMap.values());
    }
    
    /**
     * Updates a manager in the manager map.
     * 
     * @param manager The updated manager
     * @return true if the manager was successfully updated, false otherwise
     */
    public boolean updateManager(HDBManager manager) {
        if (manager == null || !validateNRICFormat(manager.getNric())) {
            return false;
        }
        
        String nric = manager.getNric().toUpperCase();
        
        // Check if manager exists
        if (!managerMap.containsKey(nric)) {
            return false;
        }
        
        managerMap.put(nric, manager);
        return true;
    }
    
    /**
     * Gets the number of managers.
     * 
     * @return The number of managers
     */
    public int getManagerCount() {
        return managerMap.size();
    }
    
    /**
     * Checks if a manager with the given NRIC exists.
     * 
     * @param nric The NRIC to check
     * @return true if a manager with the given NRIC exists, false otherwise
     */
    public boolean managerExists(String nric) {
        if (nric == null || !validateNRICFormat(nric)) {
            return false;
        }
        
        return managerMap.containsKey(nric.toUpperCase());
    }
    
    /**
     * Creates a new manager and adds it to the manager map.
     * 
     * @param name The name of the manager
     * @param nric The NRIC of the manager
     * @param age The age of the manager
     * @param maritalStatus The marital status of the manager
     * @param password The password for the manager
     * @return The created manager if successful, null otherwise
     */
    public HDBManager createManager(String name, String nric, int age, String maritalStatus, String password) {
        if (name == null || nric == null || !validateNRICFormat(nric) || 
            maritalStatus == null || password == null) {
            return null;
        }
        
        nric = nric.toUpperCase();
        
        // Check if manager already exists
        if (managerMap.containsKey(nric)) {
            return null;
        }
        
        // Create manager
        HDBManager manager = new HDBManager(name, nric, age, maritalStatus, password);
        
        // Add to map
        managerMap.put(nric, manager);
        
        return manager;
    }
    
    /**
     * Clears all managers from the manager map.
     */
    public void clearManagers() {
        managerMap.clear();
    }
    
    /**
     * Gets managers filtered by age.
     * 
     * @param minAge The minimum age
     * @param maxAge The maximum age
     * @return A list of managers within the age range
     */
    public List<HDBManager> getManagersByAgeRange(int minAge, int maxAge) {
        List<HDBManager> filteredManagers = new ArrayList<>();
        
        for (HDBManager manager : managerMap.values()) {
            int age = manager.getAge();
            if (age >= minAge && age <= maxAge) {
                filteredManagers.add(manager);
            }
        }
        
        return filteredManagers;
    }
    
    /**
     * Gets managers filtered by marital status.
     * 
     * @param maritalStatus The marital status to filter by
     * @return A list of managers with the specified marital status
     */
    public List<HDBManager> getManagersByMaritalStatus(String maritalStatus) {
        if (maritalStatus == null) {
            return new ArrayList<>();
        }
        
        List<HDBManager> filteredManagers = new ArrayList<>();
        
        for (HDBManager manager : managerMap.values()) {
            if (manager.getMaritalStatus().equalsIgnoreCase(maritalStatus)) {
                filteredManagers.add(manager);
            }
        }
        
        return filteredManagers;
    }
    
    /**
     * Changes the password of a manager.
     * 
     * @param nric The NRIC of the manager
     * @param oldPassword The current password
     * @param newPassword The new password
     * @return true if the password was successfully changed, false otherwise
     */
    public boolean changeManagerPassword(String nric, String oldPassword, String newPassword) {
        if (nric == null || oldPassword == null || newPassword == null || 
            !validateNRICFormat(nric)) {
            return false;
        }
        
        HDBManager manager = getManagerByNric(nric);
        if (manager == null) {
            return false;
        }
        
        // Check if old password matches
        if (!manager.getPassword().equals(oldPassword)) {
            return false;
        }
        
        // Change password and update manager
        return manager.changePassword(oldPassword, newPassword);
    }
}