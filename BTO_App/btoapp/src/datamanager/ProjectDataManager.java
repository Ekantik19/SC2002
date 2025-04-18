package datamanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
 */
public class ProjectDataManager extends DataManager {
    
    private Map<String, Project> projectMap;
    private Map<String, HDBManager> managerMap;
    private Map<String, HDBOfficer> officerMap;
    private String filePath;
    private static final String DELIMITER = "\t";
    
    /**
     * Constructor for ProjectDataManager.
     */
    public ProjectDataManager(Map<String, HDBManager> managerMap, Map<String, HDBOfficer> officerMap) {
        this.projectMap = new HashMap<>();
        this.managerMap = managerMap;
        this.officerMap = officerMap;
        this.filePath = FilePathConfig.PROJECT_LIST_PATH;
        
        // Debug output
        System.out.println("DEBUG: ProjectDataManager initialized with " + 
                          (managerMap != null ? managerMap.size() : 0) + " managers and " +
                          (officerMap != null ? officerMap.size() : 0) + " officers");
        
        // Log manager information
        if (managerMap != null) {
            System.out.println("DEBUG: Available managers: " + String.join(", ", managerMap.keySet()));
        }
        
        // Load projects on initialization
        loadProjects();
    }
        
    /**
     * Loads projects from file.
     */
    public List<Project> loadProjects() {
        System.out.println("DEBUG: Loading projects from: " + filePath);
        File file = new File(filePath);
        System.out.println("DEBUG: File exists: " + file.exists());
        
        if (!file.exists()) {
            System.out.println("DEBUG: ⚠️ WARNING: Project file not found at: " + filePath);
            System.out.println("DEBUG: Current working directory: " + System.getProperty("user.dir"));
            return new ArrayList<>();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    System.out.println("DEBUG: Project header: " + line);
                    isHeader = false;
                    continue; // Skip the header row
                }
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                System.out.println("DEBUG: Processing project line: " + line);
                
                try {
                    Project project = parseProjectFromLine(line);
                    if (project != null) {
                        System.out.println("DEBUG: Created project: " + project.getProjectName());
                        projectMap.put(project.getProjectName(), project);
                        System.out.println("DEBUG: Added project to map with key: '" + 
                                        project.getProjectName() + "'");
                        
                        // Fix the officer-project relationship
                        List<HDBOfficer> officers = project.getAssignedOfficers();
                        for (HDBOfficer officer : officers) {
                            // Update the officer's assigned project
                            officer.setAssignedProject(project); 
                            officer.setRegistrationApproved(true);
                            System.out.println("DEBUG: Updated officer " + officer.getName() + 
                                            " with project assignment to " + project.getProjectName());
                            
                            // Also update in officer map
                            if (officerMap.containsKey(officer.getNric())) {
                                officerMap.put(officer.getNric(), officer);
                            }
                        }
                    } else {
                        System.out.println("DEBUG: Failed to parse project from line");
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: Error parsing project: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("DEBUG: Loaded " + projectMap.size() + " projects");
            System.out.println("DEBUG: Project map keys: " + String.join(", ", projectMap.keySet()));
            
            return new ArrayList<>(projectMap.values());
        } catch (IOException e) {
            System.out.println("DEBUG: ⚠️ ERROR reading project data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Parses a Project from a line of text.
     */
    private Project parseProjectFromLine(String line) {
        try {
            String[] parts = line.split(DELIMITER);
            
            if (parts.length < 12) {
                System.out.println("DEBUG: Invalid project data format (not enough fields): " + parts.length);
                System.out.println("DEBUG: Expected at least 12 fields, got: " + parts.length);
                return null;
            }
            
            String projectName = parts[0].trim();
            String neighborhood = parts[1].trim();
            
            // Parse flat types
            List<FlatType> flatTypes = new ArrayList<>();
            List<Integer> numberOfUnits = new ArrayList<>();
            List<Double> sellingPrices = new ArrayList<>();
            
            // First flat type
            FlatType type1 = FlatType.fromString(parts[2].trim());
            int units1 = Integer.parseInt(parts[3].trim());
            double price1 = Double.parseDouble(parts[4].trim());
            
            if (type1 != null) {
                flatTypes.add(type1);
                numberOfUnits.add(units1);
                sellingPrices.add(price1);
            }
            
            // Second flat type
            FlatType type2 = FlatType.fromString(parts[5].trim());
            int units2 = Integer.parseInt(parts[6].trim());
            double price2 = Double.parseDouble(parts[7].trim());
            
            if (type2 != null) {
                flatTypes.add(type2);
                numberOfUnits.add(units2);
                sellingPrices.add(price2);
            }
            
            // Parse dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date openingDate = null;
            Date closingDate = null;
            try {
                openingDate = dateFormat.parse(parts[8].trim());
                closingDate = dateFormat.parse(parts[9].trim());
            } catch (ParseException e) {
                // Try alternative format with 1-digit day/month
                dateFormat = new SimpleDateFormat("d/M/yyyy");
                try {
                    openingDate = dateFormat.parse(parts[8].trim());
                    closingDate = dateFormat.parse(parts[9].trim());
                } catch (ParseException e2) {
                    System.out.println("DEBUG: Error parsing dates: " + e2.getMessage());
                    System.out.println("DEBUG: Opening date string: '" + parts[8].trim() + "'");
                    System.out.println("DEBUG: Closing date string: '" + parts[9].trim() + "'");
                    return null;
                }
            }
            
            // Get manager
            String managerNric = parts[10].trim();
            HDBManager manager = null;
            
            // First try to find by NRIC
            if (managerMap != null && managerMap.containsKey(managerNric)) {
                manager = managerMap.get(managerNric);
            } 
            // If not found, try to find by name
            else {
                for (HDBManager m : managerMap.values()) {
                    if (m.getName().equalsIgnoreCase(managerNric)) {
                        manager = m;
                        break;
                    }
                }
            }
            
            if (manager == null) {
                System.out.println("DEBUG: Manager not found for: " + managerNric);
                System.out.println("DEBUG: Available managers: " + 
                                  (managerMap != null ? String.join(", ", managerMap.keySet()) : "none"));
                return null;
            }
            
            // Parse officer slots
            int officerSlots = Integer.parseInt(parts[11].trim());
            
            // Create project
            Project project = new Project(projectName, neighborhood, flatTypes, numberOfUnits, 
                                         sellingPrices, openingDate, closingDate, manager, officerSlots);
            
            // Add officers if specified
            if (parts.length > 12 && parts[12] != null && !parts[12].trim().isEmpty()) {
                String officersList = parts[12].trim();
                // Handle case where officers are in quotes
                if (officersList.startsWith("\"") && officersList.endsWith("\"")) {
                    officersList = officersList.substring(1, officersList.length() - 1);
                }
                
                String[] officerNames = officersList.split(",");
                for (String officerName : officerNames) {
                    HDBOfficer officer = findOfficerByName(officerName.trim());
                    if (officer != null) {
                        project.addOfficer(officer);
                    }
                }
            }
            
            return project;
        } catch (Exception e) {
            System.out.println("DEBUG: Error in parseProjectFromLine: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Finds an officer by name.
     */
    private HDBOfficer findOfficerByName(String name) {
        if (officerMap == null) return null;
        
        for (HDBOfficer officer : officerMap.values()) {
            if (officer.getName().equalsIgnoreCase(name)) {
                return officer;
            }
        }
        System.out.println("DEBUG: Officer not found by name: " + name);
        return null;
    }
    
    /**
     * Gets all projects.
     */
    public List<Project> getAllProjects() {
        System.out.println("DEBUG: getAllProjects returning " + projectMap.size() + " projects");
        return new ArrayList<>(projectMap.values());
    }
    
    /**
     * Gets a project by name.
     */
    public Project getProjectByName(String projectName) {
        if (projectName == null) {
            return null;
        }
        
        Project project = projectMap.get(projectName);
        
        if (project == null) {
            System.out.println("DEBUG: Project not found by name: " + projectName);
            System.out.println("DEBUG: Available projects: " + String.join(", ", projectMap.keySet()));
        }
        
        return project;
    }
    
    /**
     * Adds a project to the data manager.
     */
    public boolean addProject(Project project) {
        if (project != null && project.getProjectName() != null) {
            projectMap.put(project.getProjectName(), project);
            
            System.out.println("DEBUG: Added project to projectMap: " + project.getProjectName());
            System.out.println("DEBUG: projectMap now contains " + projectMap.size() + " projects");
            return saveProjects();
        }
        return false;
    }
    
    /**
     * Updates a project in the data manager.
     */
    public boolean updateProject(Project project) {
        if (project != null && project.getProjectName() != null) {
            projectMap.put(project.getProjectName(), project);
            return saveProjects();
        }
        return false;
    }

    /**
     * Saves all projects to the project data file.
     */
    private boolean saveProjects() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("Project Name\tNeighborhood\tType 1\tNumber of units for Type 1\tSelling price for Type 1\tType 2\tNumber of units for Type 2\tSelling price for Type 2\tApplication opening date\tApplication closing date\tManager\tOfficer Slot\tOfficer");
            writer.newLine();
            
            // Write project data
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            for (Project project : projectMap.values()) {
                // Project name and neighborhood
                writer.write(project.getProjectName() + "\t");
                writer.write(project.getNeighborhood() + "\t");
                
                // Flat types information
                List<Project.FlatTypeInfo> flatTypeInfoList = project.getFlatTypeInfoList();
                
                // Add 2-Room info if available
                boolean hasTwoRoom = false;
                for (Project.FlatTypeInfo info : flatTypeInfoList) {
                    if (info.getFlatType() == FlatType.TWO_ROOM) {
                        writer.write("2-Room\t" + info.getNumberOfUnits() + "\t" + info.getSellingPrice() + "\t");
                        hasTwoRoom = true;
                        break;
                    }
                }
                if (!hasTwoRoom) {
                    writer.write("\t0\t0\t");
                }
                
                // Add 3-Room info if available
                boolean hasThreeRoom = false;
                for (Project.FlatTypeInfo info : flatTypeInfoList) {
                    if (info.getFlatType() == FlatType.THREE_ROOM) {
                        writer.write("3-Room\t" + info.getNumberOfUnits() + "\t" + info.getSellingPrice() + "\t");
                        hasThreeRoom = true;
                        break;
                    }
                }
                if (!hasThreeRoom) {
                    writer.write("\t0\t0\t");
                }
                
                // Application dates
                writer.write(dateFormat.format(project.getApplicationOpeningDate()) + "\t");
                writer.write(dateFormat.format(project.getApplicationClosingDate()) + "\t");
                
                // Manager and officer slots
                writer.write(project.getManagerInCharge().getName() + "\t");
                writer.write(project.getOfficerSlots() + "\t");
                
                // Officer list
                List<HDBOfficer> officers = project.getAssignedOfficers();
                if (!officers.isEmpty()) {
                    writer.write("\"");
                    for (int i = 0; i < officers.size(); i++) {
                        if (i > 0) {
                            writer.write(",");
                        }
                        writer.write(officers.get(i).getName());
                    }
                    writer.write("\"");
                }
                
                writer.newLine();
            }
            
            System.out.println("DEBUG: Saved " + projectMap.size() + " projects to file: " + filePath);
            return true;
        } catch (IOException e) {
            System.out.println("ERROR: Failed to save projects to file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Removes a project from the data manager.
     */
    public boolean removeProject(String projectId) {
        if (projectId != null && projectMap.containsKey(projectId)) {
            projectMap.remove(projectId);
            return saveProjects();
        }
        return false;
    }
}