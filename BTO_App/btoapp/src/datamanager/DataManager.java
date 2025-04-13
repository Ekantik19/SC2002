package datamanager;

/**
 * DataManager is the base class for all data managers in the BTO Management System.
 * It provides common functionality for data access and manipulation.
 * 
 * @author Your Name
 * @version 1.0
 */
public class DataManager {
    
    /**
     * Constructor for DataManager.
     */
    public DataManager() {
        // Initialize common resources if needed
    }
    
    /**
     * Validates that a file exists and is readable.
     * 
     * @param filePath The path to the file
     * @return true if the file exists and is readable, false otherwise
     */
    protected boolean validateFile(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            return file.exists() && file.canRead();
        } catch (Exception e) {
            System.out.println("Error validating file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a backup of a file before making changes.
     * 
     * @param filePath The path to the file
     * @return true if the backup was created successfully, false otherwise
     */
    protected boolean createBackup(String filePath) {
        try {
            java.io.File sourceFile = new java.io.File(filePath);
            java.io.File backupFile = new java.io.File(filePath + ".bak");
            
            // Delete existing backup if it exists
            if (backupFile.exists()) {
                backupFile.delete();
            }
            
            // Create backup
            java.nio.file.Files.copy(sourceFile.toPath(), backupFile.toPath());
            return true;
        } catch (Exception e) {
            System.out.println("Error creating backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Restores a file from backup in case of error.
     * 
     * @param filePath The path to the file
     * @return true if the restore was successful, false otherwise
     */
    protected boolean restoreFromBackup(String filePath) {
        try {
            java.io.File sourceFile = new java.io.File(filePath);
            java.io.File backupFile = new java.io.File(filePath + ".bak");
            
            if (!backupFile.exists()) {
                return false;
            }
            
            // Delete current file if it exists
            if (sourceFile.exists()) {
                sourceFile.delete();
            }
            
            // Restore from backup
            java.nio.file.Files.copy(backupFile.toPath(), sourceFile.toPath());
            return true;
        } catch (Exception e) {
            System.out.println("Error restoring from backup: " + e.getMessage());
            return false;
        }
    }
}