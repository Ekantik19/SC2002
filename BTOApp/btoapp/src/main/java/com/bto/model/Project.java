package com.bto.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bto.enquiry.Enquiry;
import com.bto.model.abstracts.AProject;
import com.bto.model.enums.FlatType;

/**
 * Class representing a BTO project in the system.
 * Manages project details, flat types, applications, and enquiries.
 * 
 * @author Your Name
 * @version 1.0
 */
public class Project extends AProject {
    
    private Set<Application> applications;
    private Set<Enquiry> enquiries;
    
    /**
     * Constructor for Project.
     * 
     * @param projectName The name of the project
     * @param neighborhood The neighborhood of the project
     * @param applicationOpeningDate The date when applications open
     * @param applicationClosingDate The date when applications close
     * @param managerInCharge The HDB manager in charge of the project
     * @param officerSlots The number of officer slots available for the project
     */
    public Project(String projectName, String neighborhood, Date applicationOpeningDate, 
                  Date applicationClosingDate, HDBManager managerInCharge, int officerSlots) {
        super(projectName, neighborhood, applicationOpeningDate, applicationClosingDate, managerInCharge, officerSlots);
        this.applications = new HashSet<>();
        this.enquiries = new HashSet<>();
    }
    
    /**
     * Adds an application to the project.
     * 
     * @param application The application to add
     */
    public void addApplication(Application application) {
        applications.add(application);
    }
    
    /**
     * Removes an application from the project.
     * 
     * @param application The application to remove
     */
    public void removeApplication(Application application) {
        applications.remove(application);
    }
    
    /**
     * Gets a list of all applications for the project.
     * 
     * @return A list of all applications
     */
    public List<Application> getApplications() {
        return new ArrayList<>(applications);
    }
    
    /**
     * Adds an enquiry to the project.
     * 
     * @param enquiry The enquiry to add
     */
    public void addEnquiry(Enquiry enquiry) {
        enquiries.add(enquiry);
    }
    
    /**
     * Removes an enquiry from the project.
     * 
     * @param enquiry The enquiry to remove
     */
    public void removeEnquiry(Enquiry enquiry) {
        enquiries.remove(enquiry);
    }
    
    /**
     * Gets a list of all enquiries for the project.
     * 
     * @return A list of all enquiries
     */
    public List<Enquiry> getEnquiries() {
        return new ArrayList<>(enquiries);
    }
    
    /**
     * Checks if the project is currently open for applications.
     * 
     * @return true if the project is open for applications, false otherwise
     */
    public boolean isOpenForApplications() {
        Date currentDate = new Date();
        return currentDate.after(getApplicationOpeningDate()) && 
               currentDate.before(getApplicationClosingDate()) &&
               isVisible();
    }
    
    /**
     * Gets the number of available units for a specific flat type.
     * 
     * @param flatType The flat type to check
     * @return The number of available units
     */
    public int getAvailableUnits(FlatType flatType) {
        for (FlatTypeInfo info : getFlatTypeInfoList()) {
            if (info.getFlatType() == flatType) {
                return info.getNumberOfUnits();
            }
        }
        return 0;
    }
    
    /**
     * Gets the selling price for a specific flat type.
     * 
     * @param flatType The flat type to check
     * @return The selling price
     */
    public double getSellingPrice(FlatType flatType) {
        for (FlatTypeInfo info : getFlatTypeInfoList()) {
            if (info.getFlatType() == flatType) {
                return info.getSellingPrice();
            }
        }
        return 0;
    }
    
    /**
     * Updates the flat type information.
     * 
     * @param flatType The flat type to update
     * @param numberOfUnits The new number of units
     * @param sellingPrice The new selling price
     * @return true if the update was successful, false otherwise
     */
    public boolean updateFlatType(FlatType flatType, int numberOfUnits, double sellingPrice) {
        for (FlatTypeInfo info : getFlatTypeInfoList()) {
            if (info.getFlatType() == flatType) {
                // Cannot directly modify numberOfUnits in FlatTypeInfo, so create a new one
                // and replace it in the list
                getFlatTypeInfoList().remove(info);
                addFlatType(flatType, numberOfUnits, sellingPrice);
                return true;
            }
        }
        // If the flat type doesn't exist yet, add it
        addFlatType(flatType, numberOfUnits, sellingPrice);
        return true;
    }
    
    /**
     * Returns a string representation of the project.
     * 
     * @return A string with basic project information
     */
    @Override
    public String toString() {
        return "Project: " + getProjectName() + 
               ", Neighborhood: " + getNeighborhood() + 
               ", Open: " + getApplicationOpeningDate() + 
               ", Close: " + getApplicationClosingDate() + 
               ", Visible: " + (isVisible() ? "Yes" : "No");
    }
    
    /**
     * Returns a detailed string representation of the project.
     * 
     * @return A detailed string with project information
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project Name: ").append(getProjectName()).append("\n");
        sb.append("Neighborhood: ").append(getNeighborhood()).append("\n");
        sb.append("Application Period: ").append(getApplicationOpeningDate())
          .append(" to ").append(getApplicationClosingDate()).append("\n");
        sb.append("Manager: ").append(getManagerInCharge().getName()).append("\n");
        sb.append("Visibility: ").append(isVisible() ? "Visible" : "Hidden").append("\n");
        sb.append("Officer Slots: ").append(getOfficerSlots())
          .append(" (").append(getRemainingOfficerSlots()).append(" remaining)\n");
        
        sb.append("\nFlat Types:\n");
        for (FlatTypeInfo info : getFlatTypeInfoList()) {
            sb.append("- ").append(info.getFlatType().getDisplayName())
              .append(": ").append(info.getNumberOfUnits()).append(" units, $")
              .append(String.format("%.2f", info.getSellingPrice())).append("\n");
        }
        
        sb.append("\nAssigned Officers:\n");
        List<HDBOfficer> officers = getAssignedOfficers();
        if (officers.isEmpty()) {
            sb.append("- No officers assigned\n");
        } else {
            for (HDBOfficer officer : officers) {
                sb.append("- ").append(officer.getName()).append("\n");
            }
        }
        
        return sb.toString();
    }
}