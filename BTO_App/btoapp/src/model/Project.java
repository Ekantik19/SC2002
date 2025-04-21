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
     * 
     * @param projectName             the name of the project
     * @param neighborhood            the neighborhood where the project is located
     * @param applicationOpeningDate  the date when applications open
     * @param applicationClosingDate  the date when applications close
     * @param managerInCharge         the HDB manager in charge of the project
     * @param officerSlots            the number of officer slots for the project
     */
    public Project(String projectName, String neighborhood, Date applicationOpeningDate, 
                  Date applicationClosingDate, HDBManager managerInCharge, int officerSlots) {
        super(projectName, neighborhood, applicationOpeningDate, applicationClosingDate, managerInCharge, officerSlots);
        this.applications = new HashSet<>();
        this.enquiries = new HashSet<>();
        System.out.println("DEBUG: Created project with basic constructor: " + projectName);
    }
    
    /**
     * Full constructor for Project with each type information.
     * 
     * @param projectName             the name of the project
     * @param neighborhood            the neighborhood where the project is located
     * @param flatTypes               the list of flat types offered
     * @param numberOfUnits           the list of units for each flat type
     * @param sellingPrices           the list of selling prices for each flat type
     * @param applicationOpeningDate  the date when applications open
     * @param applicationClosingDate  the date when applications close
     * @param managerInCharge         the HDB manager in charge of the project
     * @param officerSlots            the number of officer slots for the project
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
     *
     * @param application the application to add
     * @return {@code true} if the application was added, {@code false} otherwise
     */
    public boolean addApplication(Application application) {
        if (application != null) {
            return applications.add(application);
        }
        return false;
    }

    /**
     * Returns a list of all applications for this project.
     *
     * @return list of applications
     */
    public List<Application> getApplications() {
        return new ArrayList<>(applications);
    }

    /**
     * Adds an enquiry to the project.
     *
     * @param enquiry the enquiry to add
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
     *
     * @param enquiry the enquiry to remove
     */
    public void removeEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
            enquiries.remove(enquiry);
        }
    }
    
    /**
     * Returns a list of all enquiries for this project.
     *
     * @return list of enquiries
     */
    public List<Enquiry> getEnquiries() {
        return new ArrayList<>(enquiries);
    }
    
    /**
     * Checks if the project is currently open for applications.
     *
     * @return {@code true} if applications are open, {@code false} otherwise
     */
    public boolean isOpenForApplications() {
        Date currentDate = new Date();
        return currentDate.after(getApplicationOpeningDate()) && 
               currentDate.before(getApplicationClosingDate()) &&
               isVisible();
    }
}