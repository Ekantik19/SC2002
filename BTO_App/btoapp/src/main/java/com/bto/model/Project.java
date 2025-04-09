package com.bto.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a BTO project in the system.
 */
public class Project {
    //private int projectID;
    private String projectName;
    private String neighborhood;
    private Map<String, Integer> flatTypes; // Flat type -> Number of units
    private Date openingDate;
    private Date closingDate;
    private boolean isVisible;
    private HDBManager managerInCharge;
    private List<HDBOfficer> assignedOfficers;
    private List<Application> applications;
    private List<Enquiry> enquiries;

    private int maxOfficerSlots = 10; // Default value
    
    /**
     * Constructor for Project.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param openingDate The opening date for applications
     * @param closingDate The closing date for applications
     * @param managerInCharge The HDB manager in charge of the project
     */
    public Project(String projectName, String neighborhood, 
                  Date openingDate, Date closingDate, HDBManager managerInCharge) {
        //this.projectID = projectID;
        this.projectName = projectName;
        this.neighborhood = neighborhood;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.managerInCharge = managerInCharge;
        
        this.flatTypes = new HashMap<>();
        this.isVisible = false;
        this.assignedOfficers = new ArrayList<>();
        this.applications = new ArrayList<>();
        this.enquiries = new ArrayList<>();
    }
    
    /**
     * Add a flat type to the project.
     * 
     * @param flatType The type of flat (e.g., "2-Room", "3-Room")
     * @param numUnits The number of units of this flat type
     */
    public void addFlatType(String flatType, int numUnits) {
        flatTypes.put(flatType, numUnits);
    }
    
    /**
     * Check if the project has a specific flat type.
     * 
     * @param flatType The flat type to check
     * @return true if the project has this flat type, false otherwise
     */
    public boolean hasFlatType(String flatType) {
        return flatTypes.containsKey(flatType) && flatTypes.get(flatType) > 0;
    }
    
    /**
     * Add an officer to the project.
     * 
     * @param officer The HDB officer to assign to the project
     * @return true if officer was added, false if maximum officers reached
     */
    public boolean addOfficer(HDBOfficer officer) {
        if (assignedOfficers.size() >= 10) {
            return false;
        }
        
        assignedOfficers.add(officer);
        return true;
    }
    
    /**
     * Add an application to the project.
     * 
     * @param application The application to add
     */
    public void addApplication(Application application) {
        applications.add(application);
    }
    
    /**
     * Add an enquiry to the project.
     * 
     * @param enquiry The enquiry to add
     */
    public void addEnquiry(Enquiry enquiry) {
        enquiries.add(enquiry);
    }
    
    /**
     * Check if the project is currently open for applications.
     * 
     * @return true if the current date is between opening and closing dates
     */
    public boolean isOpen() {
        Date now = new Date();
        return !now.before(openingDate) && !now.after(closingDate);
    }
    
    /**
     * Get the number of remaining officer slots.
     * 
     * @return The number of officer slots available
     */
    public int getRemainingOfficerSlots() {
        return 10 - assignedOfficers.size();
    }
    
    /**
     * Get the number of remaining units for a flat type.
     * 
     * @param flatType The flat type to check
     * @return The number of remaining units, or 0 if flat type doesn't exist
     */
    public int getRemainingUnits(String flatType) {
        return flatTypes.getOrDefault(flatType, 0);
    }
    
    /**
     * Decrease the number of units for a flat type when booked.
     * 
     * @param flatType The flat type that was booked
     * @return true if successful, false if no units remaining
     */
    public boolean decrementUnits(String flatType) {
        if (!hasFlatType(flatType)) {
            return false;
        }
        
        int currentUnits = flatTypes.get(flatType);
        if (currentUnits <= 0) {
            return false;
        }
        
        flatTypes.put(flatType, currentUnits - 1);
        return true;
    }
    
    // // Getters and setters
    // public int getProjectID() {
    //     return projectID;
    // }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getNeighborhood() {
        return neighborhood;
    }
    
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    
    public Map<String, Integer> getFlatTypes() {
        return flatTypes;
    }
    
    public Date getOpeningDate() {
        return openingDate;
    }
    
    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
    }
    
    public Date getClosingDate() {
        return closingDate;
    }
    
    public void setClosingDate(Date closingDate) {
        this.closingDate = closingDate;
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    public HDBManager getManagerInCharge() {
        return managerInCharge;
    }
    
    public List<HDBOfficer> getAssignedOfficers() {
        return assignedOfficers;
    }
    
    public List<Application> getApplications() {
        return applications;
    }
    
    public List<Enquiry> getEnquiries() {
        return enquiries;
    }
 ///////////////////
    /**
     * Get the HDB Manager in charge of this project.
     * This is an alias for getManagerInCharge() for API consistency.
     * 
     * @return The HDB Manager in charge
     */
    public HDBManager getHdbManagerInCharge() {
        return getManagerInCharge();
    }

    public int getMaxOfficerSlots() {
        return maxOfficerSlots;
    }
    
    public void setMaxOfficerSlots(int maxOfficerSlots) {
        this.maxOfficerSlots = maxOfficerSlots;
    }
    
    // For compatibility with ManagerView
    public List<HDBOfficer> getOfficers() {
        return getAssignedOfficers();
    }  

        /**
     * Get the number of available officer slots.
     * 
     * @return The number of available slots for HDB Officers
     */
    public int getAvailableOfficerSlots() {
        return getMaxOfficerSlots() - getAssignedOfficers().size();
    }

    /**
     * Increase the number of units for a flat type (for testing cleanup).
     * 
     * @param flatType The flat type to increment
     * @return true if successful, false otherwise
     */
    public boolean incrementUnits(String flatType) {
        if (!flatTypes.containsKey(flatType)) {
            return false;
        }
        flatTypes.put(flatType, flatTypes.get(flatType) + 1);
        return true;
    }
}