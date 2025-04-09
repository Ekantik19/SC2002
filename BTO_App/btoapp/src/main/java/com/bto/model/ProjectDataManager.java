package com.bto.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ProjectDataManager handles the interaction between the application and the project text file.
 * It is responsible for adding, loading, and updating project information.
 */
public class ProjectDataManager {
    
    private static final String filePath = "src/main/java/com/resources/ProjectList.txt";
    //"C:\Users\luisa\OneDrive\Documents\GitHub\SC2002\BTO_App\btoapp\src\main\java\com\resources"
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    
    /**
     * Load all projects from the text file.
     * 
     * @param managers List of HDB managers to link with projects
     * @param officers List of HDB officers to link with projects
     * @return List of Project objects
     */
    public static List<Project> loadProjects(List<HDBManager> managers, List<HDBOfficer> officers) {
        List<Project> projects = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // Skip header line
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 11) {
                    String projectName = parts[0];
                    String neighborhood = parts[1];
                    
                    // Parse flat types and prices
                    Map<String, Integer> flatTypes = new HashMap<>();
                    flatTypes.put(parts[2], Integer.parseInt(parts[3])); // Type 1
                    flatTypes.put(parts[5], Integer.parseInt(parts[6])); // Type 2
                    
                    Map<String, Double> flatPrices = new HashMap<>();
                    flatPrices.put(parts[2], Double.parseDouble(parts[4])); // Price for Type 1
                    flatPrices.put(parts[5], Double.parseDouble(parts[7])); // Price for Type 2
                    
                    // Parse dates
                    Date openingDate = DATE_FORMAT.parse(parts[8]);
                    Date closingDate = DATE_FORMAT.parse(parts[9]);
                    
                    // Find manager
                    String managerID = parts[10];
                    HDBManager manager = null;
                    for (HDBManager m : managers) {
                        if (m.getUserID().equals(managerID)) {
                            manager = m;
                            break;
                        }
                    }
                    
                    // Create project
                    Project project = new Project(projectName, neighborhood, openingDate, closingDate, manager);
                    
                    // Add flat types
                    for (Map.Entry<String, Integer> entry : flatTypes.entrySet()) {
                        String flatType = entry.getKey();
                        int units = entry.getValue();
                        double price = flatPrices.get(flatType);
                        project.addFlatType(flatType, units);
                        // Assuming Project class has a method to set prices
                        // project.setFlatTypePrice(flatType, price);
                    }
                    
                    // Set officer slots if available
                    if (parts.length > 11) {
                        int officerSlots = Integer.parseInt(parts[11]);
                        project.setMaxOfficerSlots(officerSlots);
                        
                        // Parse and link assigned officers if available
                        if (parts.length > 12 && parts[12] != null && !parts[12].isEmpty()) {
                            String[] officerIDs = parts[12].replace("\"", "").split(",");
                            for (String officerID : officerIDs) {
                                for (HDBOfficer officer : officers) {
                                    if (officer.getUserID().equals(officerID.trim())) {
                                        officer.setAssignedProject(project);
                                        officer.setRegistrationStatus(HDBOfficer.STATUS_APPROVED);
                                        project.getAssignedOfficers().add(officer);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Set visibility
                    project.setVisible(true);
                    
                    projects.add(project);
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error loading projects: " + e.getMessage());
        }
        
        return projects;
    }
    
    /**
     * Add a new project to the text file.
     * 
     * @param project The project to add
     */
    public static void addProject(Project project) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            StringBuilder line = new StringBuilder();
            
            // Project basic info
            line.append(project.getProjectName()).append("\t");
            line.append(project.getNeighborhood()).append("\t");
            
            // Flat types - assume 2-Room and 3-Room are the fixed types
            String type1 = "2-Room";
            String type2 = "3-Room";
            
            line.append(type1).append("\t");
            line.append(project.getRemainingUnits(type1)).append("\t");
            line.append("0.0").append("\t"); // Placeholder for price
            
            line.append(type2).append("\t");
            line.append(project.getRemainingUnits(type2)).append("\t");
            line.append("0.0").append("\t"); // Placeholder for price
            
            // Dates
            line.append(DATE_FORMAT.format(project.getOpeningDate())).append("\t");
            line.append(DATE_FORMAT.format(project.getClosingDate())).append("\t");
            
            // Manager
            if (project.getManagerInCharge() != null) {
                line.append(project.getManagerInCharge().getUserID());
            } else {
                line.append("");
            }
            
            // Officer slots
            line.append("\t").append(project.getMaxOfficerSlots());
            
            // Officer IDs
            if (!project.getAssignedOfficers().isEmpty()) {
                line.append("\t\"");
                for (int i = 0; i < project.getAssignedOfficers().size(); i++) {
                    if (i > 0) {
                        line.append(",");
                    }
                    line.append(project.getAssignedOfficers().get(i).getUserID());
                }
                line.append("\"");
            }
            
            bw.write(line.toString());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error adding project to file: " + e.getMessage());
        }
    }
    
    /**
     * Update a project's information in the text file.
     * 
     * @param updatedProject The project with updated information
     */
    public static void updateProject(Project updatedProject) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 1 && parts[0].equals(updatedProject.getProjectName())) {
                    // Found the project to update, write updated information
                    StringBuilder updatedLine = new StringBuilder();
                    
                    // Project basic info
                    updatedLine.append(updatedProject.getProjectName()).append("\t");
                    updatedLine.append(updatedProject.getNeighborhood()).append("\t");
                    
                    // Flat types - assume 2-Room and 3-Room are the fixed types
                    String type1 = "2-Room";
                    String type2 = "3-Room";
                    
                    updatedLine.append(type1).append("\t");
                    updatedLine.append(updatedProject.getRemainingUnits(type1)).append("\t");
                    updatedLine.append("0.0").append("\t"); // Placeholder for price
                    
                    updatedLine.append(type2).append("\t");
                    updatedLine.append(updatedProject.getRemainingUnits(type2)).append("\t");
                    updatedLine.append("0.0").append("\t"); // Placeholder for price
                    
                    // Dates
                    updatedLine.append(DATE_FORMAT.format(updatedProject.getOpeningDate())).append("\t");
                    updatedLine.append(DATE_FORMAT.format(updatedProject.getClosingDate())).append("\t");
                    
                    // Manager
                    if (updatedProject.getManagerInCharge() != null) {
                        updatedLine.append(updatedProject.getManagerInCharge().getUserID());
                    } else {
                        updatedLine.append("");
                    }
                    
                    // Officer slots
                    updatedLine.append("\t").append(updatedProject.getMaxOfficerSlots());
                    
                    // Officer IDs
                    if (!updatedProject.getAssignedOfficers().isEmpty()) {
                        updatedLine.append("\t\"");
                        for (int i = 0; i < updatedProject.getAssignedOfficers().size(); i++) {
                            if (i > 0) {
                                updatedLine.append(",");
                            }
                            updatedLine.append(updatedProject.getAssignedOfficers().get(i).getUserID());
                        }
                        updatedLine.append("\"");
                    }
                    
                    bw.write(updatedLine.toString());
                } else {
                    // Not the project to update, write original line
                    bw.write(line);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error updating project in file: " + e.getMessage());
            return;
        }
        
        // Replace original file with updated temp file
        File originalFile = new File(filePath);
        File tempFile = new File("temp.txt");
        
        Path originalPath = Paths.get(originalFile.getPath());
        Path tempPath = Paths.get(tempFile.getPath());
        
        try {
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error replacing original file with updated data: " + e.getMessage());
        }
    }
    
    /**
     * Remove a project from the text file.
     * 
     * @param project The project to remove
     */
    public static void removeProject(Project project) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter("temp.txt"))) {
            
            String line;
            String header = br.readLine(); // Read header
            bw.write(header); // Write header to temp file
            bw.newLine();
            
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 1 && parts[0].equals(project.getProjectName())) {
                    // Skip this line (the project to remove)
                    continue;
                }
                // Write all other lines
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error removing project from file: " + e.getMessage());
            return;
        }
        
        // Replace original file with updated temp file
        File originalFile = new File(filePath);
        File tempFile = new File("temp.txt");
        
        Path originalPath = Paths.get(originalFile.getPath());
        Path tempPath = Paths.get(tempFile.getPath());
        
        try {
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error replacing original file with updated data: " + e.getMessage());
        }
    }
    
    /**
     * Toggle the visibility of a project.
     * 
     * @param project The project to toggle visibility
     * @param visible The new visibility state
     */
    public static void toggleProjectVisibility(Project project, boolean visible) {
        project.setVisible(visible);
        updateProject(project);
    }
    
    /**
     * Update officer assignments for a project.
     * 
     * @param project The project to update
     * @param officer The officer to assign or unassign
     * @param assign True to assign, false to unassign
     * @return True if successful, false otherwise
     */
    public static boolean updateOfficerAssignment(Project project, HDBOfficer officer, boolean assign) {
        if (assign) {
            // Check if project has available slots
            if (project.getAssignedOfficers().size() >= project.getMaxOfficerSlots()) {
                return false;
            }
            
            // Assign officer to project
            officer.setAssignedProject(project);
            officer.setRegistrationStatus(HDBOfficer.STATUS_APPROVED);
            project.getAssignedOfficers().add(officer);
        } else {
            // Unassign officer from project
            officer.setAssignedProject(null);
            officer.setRegistrationStatus(null);
            project.getAssignedOfficers().remove(officer);
        }
        
        // Update project in file
        updateProject(project);
        return true;
    }
}