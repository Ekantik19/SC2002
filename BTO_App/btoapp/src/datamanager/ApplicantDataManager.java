package datamanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Applicant;
import utils.FilePathConfig;

/**
 * ApplicantDataManager handles the interaction between the application and the ApplicantList.txt file.
 * It is responsible for reading and writing applicant data to the text file.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ApplicantDataManager {
    
    private String filePath;
    private static final String DELIMITER = "\t";
    
    /**
     * Default constructor that uses the file path from FilePathConfig.
     */
    public ApplicantDataManager() {
        this(FilePathConfig.APPLICANT_LIST_PATH);
    }
    
    /**
     * Constructor for ApplicantDataManager with a specified file path.
     * 
     * @param filePath The path to the applicant data file
     */
    public ApplicantDataManager(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Reads all applicants from the applicant data file.
     * 
     * @return A list of Applicant objects
     */
    public List<Applicant> readAllApplicants() {
        List<Applicant> applicants = new ArrayList<>();
        
        File file = new File(filePath);
        
        if (!file.exists()) {
            return applicants;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // Skip the header row
                }
                
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 5) {
                    String name = parts[0];
                    String nric = parts[1];
                    int age = Integer.parseInt(parts[2]);
                    String maritalStatus = parts[3];
                    String password = parts[4];
                    
                    Applicant applicant = new Applicant(name, nric, age, maritalStatus, password);
                    applicants.add(applicant);
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR reading applicant data: " + e.getMessage());
            e.printStackTrace();
        }
        return applicants;
    }
    
    /**
     * Finds an applicant by their NRIC.
     * 
     * @param nric The NRIC to search for
     * @return The applicant if found, null otherwise
     */
    public Applicant findApplicantByNRIC(String nric) {
        List<Applicant> applicants = readAllApplicants();
        
        for (Applicant applicant : applicants) {
            if (applicant.getNric().equals(nric)) {
                return applicant;
            }
        }
        
        return null;
    }
    
    /**
     * Updates an existing applicant in the applicant data file.
     * 
     * @param applicant The applicant to update
     * @return true if the applicant was updated successfully, false otherwise
     */
    public boolean updateApplicant(Applicant applicant) {
        List<Applicant> applicants = readAllApplicants();
        
        for (int i = 0; i < applicants.size(); i++) {
            if (applicants.get(i).getNric().equals(applicant.getNric())) {
                applicants.set(i, applicant);
                return writeApplicants(applicants);
            }
        }
        
        return false; // Applicant not found
    }
    
    /**
     * Writes the list of applicants to the applicant data file.
     * 
     * @param applicants The list of applicants to write
     * @return true if the applicants were written successfully, false otherwise
     */
    public boolean writeApplicants(List<Applicant> applicants) {
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Name" + DELIMITER + "NRIC" + DELIMITER + "Age" + DELIMITER + 
                        "Marital Status" + DELIMITER + "Password");
            writer.newLine();
            
            // Write applicant data
            for (Applicant applicant : applicants) {
                writer.write(
                    applicant.getName() + DELIMITER +
                    applicant.getNric() + DELIMITER +
                    applicant.getAge() + DELIMITER +
                    applicant.getMaritalStatus() + DELIMITER +
                    applicant.getPassword()
                );
                writer.newLine();
            }
        
            return true;
        } catch (IOException e) {
            System.out.println("Error writing applicant data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}