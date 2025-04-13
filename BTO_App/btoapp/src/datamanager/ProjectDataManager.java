package datamanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.enums.FlatType;
import utils.FilePathConfig;

/**
 * Data manager for Project entities in the BTO Management System.
 * Handles loading and storing project data from/to external files.
 * 
 * @author Your Name
 * @version 1.0
 */
public class ProjectDataManager extends DataManager {
    
    private String filePath;
    private static final String DELIMITER = "\t";
    private Map<String, Project> projectsMap;
    private Map<String, HDBManager> managersMap;
    private Map<String, HDBOfficer> officersMap;
    
    /**
     * Constructor for ProjectDataManager.
     * 
     * @param managersMap Map of HDBManagers by NRIC
     * @param officersMap Map of HDBOfficers by NRIC
     */
    public ProjectDataManager(Map<String, HDBManager> managersMap, Map<String, HDBOfficer> officersMap) {
        this.filePath = FilePathConfig.PROJECT_LIST_PATH;
        this.projectsMap = new HashMap<>();
        this.managersMap = managersMap;
        this.officersMap = officersMap;
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
     * Sets a new file path for the project data file.
     * 
     * @param filePath The new file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Loads project data from file.
     * 
     * @return true if data was successfully loaded, false otherwise
     */
    public boolean loadData() {
        System.out.println("DEBUG: Loading projects from: " + filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            // Skip header line
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                Project project = parseProjectFromLine(line);
                if (project != null) {
                    projectsMap.put(project.getProjectName(), project);
                }
            }
            
            System.out.println("DEBUG: Loaded " + projectsMap.size() + " projects");
            return true;
        } catch (IOException | ParseException e) {
            System.out.println("Error loading project data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Parses a Project from a line of text.
     * 
     * @param line The line to parse
     * @return The parsed Project, or null if parsing failed
     * @throws ParseException If date parsing fails
     */
    private Project parseProjectFromLine(String line) throws ParseException {
        String[] parts = line.split(DELIMITER);
        
        if (parts.length < 10) {
            System.out.println("Invalid project data format: " + line);
            return null;
        }
        
        String projectName = parts[0];
        String neighborhood = parts[1];
        
        // Parse flat types, units, and prices
        List<FlatType> flatTypes = new ArrayList<>();
        List<Integer> numberOfUnits = new ArrayList<>();
        List<Double> sellingPrices = new ArrayList<>();
        
        // First flat type
        String flatType1 = parts[2];
        int units1 = Integer.parseInt(parts[3]);
        double price1 = Double.parseDouble(parts[4]);
        
        flatTypes.add(FlatType.fromString(flatType1));
        numberOfUnits.add(units1);
        sellingPrices.add(price1);
        
        // Second flat type
        String flatType2 = parts[5];
        int units2 = Integer.parseInt(parts[6]);
        double price2 = Double.parseDouble(parts[7]);
        
        flatTypes.add(FlatType.fromString(flatType2));
        numberOfUnits.add(units2);
        sellingPrices.add(price2);
        
        // Parse dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date openingDate = dateFormat.parse(parts[8]);
        Date closingDate = dateFormat.parse(parts[9]);
        
        // Get manager
        String managerName = parts[10];
        HDBManager manager = findManagerByName(managerName);
        
        if (manager == null) {
            System.out.println("Manager not found: " + managerName);
            return null;
        }
        
        // Create project
        int officerSlots = Integer.parseInt(parts[11]);
        Project project = manager.createProject(projectName, neighborhood, flatTypes, 
                                              numberOfUnits, sellingPrices, 
                                              openingDate, closingDate, officerSlots);
        
        // Assign officers if specified
        if (parts.length > 12 && parts[12] != null && !parts[12].isEmpty()) {
            String officersStr = parts[12].replace("\"", "");
            String[] officerNames = officersStr.split(",");
            
            for (String officerName : officerNames) {
                HDBOfficer officer = findOfficerByName(officerName.trim());
                if (officer != null) {
                    // Register and approve officer automatically on data load
                    if (officer.registerForProject(project)) {
                        manager.approveOfficerRegistration(officer);
                    }
                }
            }
        }
        
        return project;
    }
    
    /**
     * Finds a manager by name.
     * 
     * @param name The name to search for
     * @return The found manager, or null if not found
     */
    private HDBManager findManagerByName(String name) {
        for (HDBManager manager : managersMap.values()) {
            if (manager.getName().equalsIgnoreCase(name)) {
                return manager;
            }
        }
        return null;
    }
    
    /**
     * Finds an officer by name.
     * 
     * @param name The name to search for
     * @return The found officer, or null if not found
     */
    private HDBOfficer findOfficerByName(String name) {
        for (HDBOfficer officer : officersMap.values()) {
            if (officer.getName().equalsIgnoreCase(name)) {
                return officer;
            }
        }
        return null;
    }
    
    /**
     * Saves project data to file.
     * 
     * @return true if data was successfully saved, false otherwise
     */
    public boolean saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Project Name\tNeighborhood\tType 1\tNumber of units for Type 1\tSelling price for Type 1\t" +
                        "Type 2\tNumber of units for Type 2\tSelling price for Type 2\t" +
                        "Application opening date\tApplication closing date\tManager\tOfficer Slot\tOfficer");
            writer.newLine();
            
            // Write projects
            for (Project project : projectsMap.values()) {
                StringBuilder sb = new StringBuilder();
                
                // Project Name and Neighborhood
                sb.append(project.getProjectName()).append(DELIMITER);
                sb.append(project.getNeighborhood()).append(DELIMITER);
                
                // Flat Type Info
                List<Project.FlatTypeInfo> flatTypeInfos = project.getFlatTypeInfoList();
                if (flatTypeInfos.size() >= 2) {
                    // First flat type
                    sb.append(flatTypeInfos.get(0).getFlatType().getDisplayName()).append(DELIMITER);
                    sb.append(flatTypeInfos.get(0).getNumberOfUnits()).append(DELIMITER);
                    sb.append(flatTypeInfos.get(0).getSellingPrice()).append(DELIMITER);
                    
                    // Second flat type
                    sb.append(flatTypeInfos.get(1).getFlatType().getDisplayName()).append(DELIMITER);
                    sb.append(flatTypeInfos.get(1).getNumberOfUnits()).append(DELIMITER);
                    sb.append(flatTypeInfos.get(1).getSellingPrice()).append(DELIMITER);
                } else if (flatTypeInfos.size() == 1) {
                    // If only one flat type, duplicate it for the format
                    sb.append(flatTypeInfos.get(0).getFlatType().getDisplayName()).append(DELIMITER);
                    sb.append(flatTypeInfos.get(0).getNumberOfUnits()).append(DELIMITER);
                    sb.append(flatTypeInfos.get(0).getSellingPrice()).append(DELIMITER);
                    sb.append("").append(DELIMITER); // Empty flat type 2
                    sb.append("0").append(DELIMITER); // 0 units
                    sb.append("0").append(DELIMITER); // 0 price
                } else {
                    // No flat types defined
                    sb.append("").append(DELIMITER).append("0").append(DELIMITER).append("0").append(DELIMITER);
                    sb.append("").append(DELIMITER).append("0").append(DELIMITER).append("0").append(DELIMITER);
                }
                
                // Dates
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                sb.append(dateFormat.format(project.getApplicationOpeningDate())).append(DELIMITER);
                sb.append(dateFormat.format(project.getApplicationClosingDate())).append(DELIMITER);
                
                // Manager and Officer slots
                sb.append(project.getManagerInCharge().getName()).append(DELIMITER);
                sb.append(project.getOfficerSlots()).append(DELIMITER);
                
                // Officers
                List<HDBOfficer> officers = project.getAssignedOfficers();
                if (!officers.isEmpty()) {
                    sb.append("\"");
                    for (int i = 0; i < officers.size(); i++) {
                        if (i > 0) sb.append(",");
                        sb.append(officers.get(i).getName());
                    }
                    sb.append("\"");
                }
                
                writer.write(sb.toString());
                writer.newLine();
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error saving project data: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Adds a project to the data manager.
     * 
     * @param project The project to add
     * @return true if the project was successfully added, false otherwise
     */
    public boolean addProject(Project project) {
        if (project != null) {
            projectsMap.put(project.getProjectName(), project);
            return saveData();
        }
        return false;
    }
    
    /**
     * Updates a project in the data manager.
     * 
     * @param project The project to update
     * @return true if the project was successfully updated, false otherwise
     */
    public boolean updateProject(Project project) {
        if (project != null && projectsMap.containsKey(project.getProjectName())) {
            projectsMap.put(project.getProjectName(), project);
            return saveData();
        }
        return false;
    }
    
    /**
     * Removes a project from the data manager.
     * 
     * @param projectName The name of the project to remove
     * @return true if the project was successfully removed, false otherwise
     */
    public boolean removeProject(String projectName) {
        if (projectsMap.containsKey(projectName)) {
            projectsMap.remove(projectName);
            return saveData();
        }
        return false;
    }
    
    /**
     * Gets a project by name.
     * 
     * @param projectName The name of the project to get
     * @return The project with the specified name, or null if not found
     */
    public Project getProjectByName(String projectName) {
        return projectsMap.get(projectName);
    }
    
    /**
     * Gets all projects.
     * 
     * @return A list of all projects
     */
    public List<Project> getAllProjects() {
        return new ArrayList<>(projectsMap.values());
    }
}