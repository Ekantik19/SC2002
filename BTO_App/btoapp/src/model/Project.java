package model;

import enquiry.Enquiry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.abstracts.AProject;
import model.enums.FlatType;

/**
 * Class representing a BTO project in the system.
 * Extends AProject abstract class and manages project details, applications, and enquiries.
 */
public class Project extends AProject {
    
    private Set<Application> applications;
    private Set<Enquiry> enquiries;
    
    /**
     * Basic constructor for Project.
     */
    public Project(String projectName, String neighborhood, Date applicationOpeningDate, 
                  Date applicationClosingDate, HDBManager managerInCharge, int officerSlots) {
        super(projectName, neighborhood, applicationOpeningDate, applicationClosingDate, managerInCharge, officerSlots);
        this.applications = new HashSet<>();
        this.enquiries = new HashSet<>();
        System.out.println("DEBUG: Created project with basic constructor: " + projectName);
    }
    
    /**
     * Full constructor for Project with flat type information.
     */
    public Project(String projectName, String neighborhood, List<FlatType> flatTypes, 
                  List<Integer> numberOfUnits, List<Double> sellingPrices,
                  Date applicationOpeningDate, Date applicationClosingDate, 
                  HDBManager managerInCharge, int officerSlots) {
        super(projectName, neighborhood, applicationOpeningDate, applicationClosingDate, 
              managerInCharge, officerSlots);
        this.applications = new HashSet<>();
        this.enquiries = new HashSet<>();
        
        System.out.println("DEBUG: Creating project with full constructor: " + projectName);
        
        // Add flat types
        if (flatTypes != null && numberOfUnits != null && sellingPrices != null) {
            int minSize = Math.min(flatTypes.size(), 
                         Math.min(numberOfUnits.size(), sellingPrices.size()));
            
            System.out.println("DEBUG: Adding " + minSize + " flat types to project " + projectName);
            for (int i = 0; i < minSize; i++) {
                if (flatTypes.get(i) != null) {
                    addFlatType(flatTypes.get(i), numberOfUnits.get(i), sellingPrices.get(i));
                    System.out.println("DEBUG: Added " + flatTypes.get(i) + " with " + 
                                      numberOfUnits.get(i) + " units at $" + sellingPrices.get(i));
                }
            }
        }
    }
    
    /**
     * Adds an application to the project.
     */
    public boolean addApplication(Application application) {
        if (application != null) {
            return applications.add(application);
        }
        return false;
    }
    
    /**
     * Removes an application from the project.
     */
    public boolean removeApplication(Application application) {
        if (application != null) {
            return applications.remove(application);
        }
        return false;
    }
    
    /**
     * Gets a list of all applications for the project.
     */
    public List<Application> getApplications() {
        return new ArrayList<>(applications);
    }
    
    /**
     * Adds an enquiry to the project.
     */
    public void addEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            enquiries.add(enquiry);
            System.out.println("DEBUG: Added enquiry " + enquiry.getEnquiryId() + 
                               " to project " + getProjectName());
        } else {
            System.out.println("DEBUG: Attempted to add null enquiry to project " + 
                               getProjectName());
        }
    }
    
    /**
     * Removes an enquiry from the project.
     */
    public void removeEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            enquiries.remove(enquiry);
        }
    }
    
    /**
     * Gets a list of all enquiries for the project.
     */
    public List<Enquiry> getEnquiries() {
        return new ArrayList<>(enquiries);
    }
    
    /**
     * Checks if the project is currently open for applications.
     */
    public boolean isOpenForApplications() {
        Date currentDate = new Date();
        return currentDate.after(getApplicationOpeningDate()) && 
               currentDate.before(getApplicationClosingDate()) &&
               isVisible();
    }
    
    /**
     * Gets the number of available units for a specific flat type.
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
     */
    public boolean updateFlatType(FlatType flatType, int numberOfUnits, double sellingPrice) {
        // Use the parent class's FlatTypeInfo list directly
        List<FlatTypeInfo> flatInfoList = getFlatTypeInfoList();
        
        for (FlatTypeInfo info : flatInfoList) {
            if (info.getFlatType() == flatType) {
                // Remove the old info
                flatInfoList.remove(info);
                // Add a new info with updated values
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
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project Name: ").append(getProjectName()).append("\n");
        sb.append("Neighborhood: ").append(getNeighborhood()).append("\n");
        sb.append("Application Period: ").append(getApplicationOpeningDate())
          .append(" to ").append(getApplicationClosingDate()).append("\n");
        
        if (getManagerInCharge() != null) {
            sb.append("Manager: ").append(getManagerInCharge().getName()).append("\n");
        } else {
            sb.append("Manager: Not assigned\n");
        }
        
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