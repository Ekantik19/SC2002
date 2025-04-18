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
            System.out.println("DEBUG: Invalid manager or NRIC format");
            return false;
        }
        
        String nric = manager.getNric().toUpperCase();
        
        // If the manager doesn't exist, add a log message
        if (!managerMap.containsKey(nric)) {
            System.out.println("DEBUG: Manager not found in map. Adding new manager: " + nric);
        }
        
        // Always update/add the manager to the map
        managerMap.put(nric, manager);
        return true;
    }
}