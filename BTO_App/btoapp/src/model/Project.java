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
}