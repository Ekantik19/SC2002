package model.abstracts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.HDBManager;
import model.HDBOfficer;
import model.enums.FlatType;

/**
 * Abstract class representing a BTO project in the system.
 * Contains common attributes and methods for all types of projects.
 * 
 * @author Your Name
 * @version 1.0
 */
public abstract class AProject {
    
    private String projectName;
    private String neighborhood;
    private Date applicationOpeningDate;
    private Date applicationClosingDate;
    private HDBManager managerInCharge;
    private int officerSlots;
    private List<HDBOfficer> assignedOfficers;
    private boolean isVisible;
    private List<FlatTypeInfo> flatTypeInfoList;
    
    /**
     * Inner class to store information about each flat type in the project.
     */
    public static class FlatTypeInfo {
        private FlatType flatType;
        private int numberOfUnits;
        private double sellingPrice;
        
        /**
         * Constructor for FlatTypeInfo.
         * 
         * @param flatType The type of flat
         * @param numberOfUnits The number of units available for this flat type
         * @param sellingPrice The selling price for this flat type
         */
        public FlatTypeInfo(FlatType flatType, int numberOfUnits, double sellingPrice) {
            this.flatType = flatType;
            this.numberOfUnits = numberOfUnits;
            this.sellingPrice = sellingPrice;
        }
        
        // Getters and Setters
        
        public FlatType getFlatType() {
            return flatType;
        }
        
        public int getNumberOfUnits() {
            return numberOfUnits;
        }
        
        public void decrementUnits() {
            if (numberOfUnits > 0) {
                numberOfUnits--;
            }
        }
        
        public double getSellingPrice() {
            return sellingPrice;
        }
        
        public void setSellingPrice(double sellingPrice) {
            this.sellingPrice = sellingPrice;
        }
    }
    
    /**
     * Constructor for AProject.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param applicationOpeningDate The date when applications open
     * @param applicationClosingDate The date when applications close
     * @param managerInCharge The HDB manager in charge of the project
     * @param officerSlots The number of officer slots available for the project
     */
    public AProject(String projectName, String neighborhood, Date applicationOpeningDate, 
                    Date applicationClosingDate, HDBManager managerInCharge, int officerSlots) {
        this.projectName = projectName;
        this.neighborhood = neighborhood;
        this.applicationOpeningDate = applicationOpeningDate;
        this.applicationClosingDate = applicationClosingDate;
        this.managerInCharge = managerInCharge;
        this.officerSlots = officerSlots;
        this.assignedOfficers = new ArrayList<>();
        this.isVisible = false;
        this.flatTypeInfoList = new ArrayList<>();
    }
    
    /**
     * Adds a flat type to the project.
     * 
     * @param flatType The type of flat
     * @param numberOfUnits The number of units available for this flat type
     * @param sellingPrice The selling price for this flat type
     */
    public void addFlatType(FlatType flatType, int numberOfUnits, double sellingPrice) {
        flatTypeInfoList.add(new FlatTypeInfo(flatType, numberOfUnits, sellingPrice));
    }
    
    /**
     * Checks if there are available units for a specific flat type.
     * 
     * @param flatType The type of flat to check
     * @return true if there are available units, false otherwise
     */
    public boolean hasAvailableUnits(FlatType flatType) {
        for (FlatTypeInfo info : flatTypeInfoList) {
            if (info.getFlatType() == flatType && info.getNumberOfUnits() > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Decrements the number of available units for a specific flat type.
     * 
     * @param flatType The type of flat to decrement
     * @return true if a unit was successfully decremented, false otherwise
     */
    public boolean decrementUnit(FlatType flatType) {
        for (FlatTypeInfo info : flatTypeInfoList) {
            if (info.getFlatType() == flatType && info.getNumberOfUnits() > 0) {
                info.decrementUnits();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds an officer to the project if there are slots available.
     * 
     * @param officer The HDB officer to add
     * @return true if the officer was added, false if no slots are available
     */
    public boolean addOfficer(HDBOfficer officer) {
        if (assignedOfficers.size() < officerSlots) {
            assignedOfficers.add(officer);
            return true;
        }
        return false;
    }
    
    /**
     * Sets the number of officer slots for this project.
     * 
     * @param officerSlots The new number of officer slots
     */
    public void setOfficerSlots(int officerSlots) {
        if (officerSlots >= 0 && officerSlots <= 10) { // Maximum 10 slots as per requirements
            if (officerSlots >= assignedOfficers.size()) {
                this.officerSlots = officerSlots;
            } else {
                System.out.println("Cannot set officer slots lower than current assigned officers count.");
            }
        } else {
            System.out.println("Invalid number of officer slots. Must be between 0 and 10.");
        }
    }
    
    // Getters and Setters
    
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
    
    public Date getApplicationOpeningDate() {
        return applicationOpeningDate;
    }
    
    public void setApplicationOpeningDate(Date applicationOpeningDate) {
        this.applicationOpeningDate = applicationOpeningDate;
    }
    
    public Date getApplicationClosingDate() {
        return applicationClosingDate;
    }
    
    public void setApplicationClosingDate(Date applicationClosingDate) {
        this.applicationClosingDate = applicationClosingDate;
    }
    
    public HDBManager getManagerInCharge() {
        return managerInCharge;
    }
    
    public void setManagerInCharge(HDBManager managerInCharge) {
        this.managerInCharge = managerInCharge;
    }
    
    public int getOfficerSlots() {
        return officerSlots;
    }
    
    public int getRemainingOfficerSlots() {
        return officerSlots - assignedOfficers.size();
    }
    
    public List<HDBOfficer> getAssignedOfficers() {
        return assignedOfficers;
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    public List<FlatTypeInfo> getFlatTypeInfoList() {
        return flatTypeInfoList;
    }
}