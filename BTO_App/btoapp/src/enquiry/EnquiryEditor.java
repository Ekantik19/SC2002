package enquiry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import model.Applicant;
import model.HDBOfficer;
import model.Project;

/**
 * The {@code EnquiryEditor} class manages enquiries for a specific BTO project.
 * It implements {@link RepliableEditorInterface} interface, providing methods to create, edit, delete, and reply to enquiries.
 *
 * @author Your Name
 * @version 1.0
 */
public class EnquiryEditor implements RepliableEditorInterface {
    /**
     * The project associated with the enquiries.
     */
    private final Project project;
    
    /**
     * The list of enquiries for the project.
     */
    private final List<Enquiry> enquiries;

    /**
     * Constructs a new enquiry editor for the specified project.
     *
     * @param project the project where the enquiries are found
     */
    public EnquiryEditor(Project project) {
        this.project = project;
        this.enquiries = new ArrayList<>();
    }

    /**
     * Helper method to generate an enquiry ID.
     * 
     * @param nric The NRIC of the applicant
     * @param projectName The name of the project
     * @return A unique enquiry ID
     */
    private String generateEnquiryId(String nric, String projectName) {
        // Simple ID generation - in a real system, this would be more sophisticated
        return "ENQ-" + nric.substring(1, 8) + "-" + 
               projectName.substring(0, Math.min(3, projectName.length())).toUpperCase() + 
               "-" + System.currentTimeMillis() % 10000;
    }

    /**
     * Creates a new enquiry for the project.
     *
     * @param content the question for the enquiry
     * @param applicant the applicant creating the enquiry
     * @return the created enquiry
     */
    @Override
    public Repliable create(String content, Applicant applicant) {

        String enquiryId = generateEnquiryId(applicant.getNric(), project.getProjectName());
        Enquiry enquiry = new Enquiry(enquiryId,applicant, project,content,null);
        enquiries.add(enquiry);
        applicant.addEnquiry(enquiry);
        return enquiry;
    }

    /**
     * Edits an enquiry with a new question.
     *
     * @param repliable the enquiry being edited
     * @param newContent the new question for the enquiry
     * @return true if editing was successful, false otherwise
     */
    @Override
    public boolean edit(Repliable repliable, String newContent) {
        if (!(repliable instanceof Enquiry)) {
            return false;
        }
        
        Enquiry enquiry = (Enquiry) repliable;
        
        // Only allow editing if:
        // 1. The enquiry belongs to this project
        // 2. The enquiry has not been answered yet
        if (enquiry.getProject().equals(project) && !enquiry.isAnswered()) {
            enquiry.setEnquiryText(newContent);
            return true;
        }
        
        return false;
    }

    /**
     * Deletes an enquiry.
     *
     * @param repliable the enquiry being deleted
     * @return true if deletion was successful, false otherwise
     */
    @Override
    public boolean delete(Repliable repliable) {
        if (!(repliable instanceof Enquiry)) {
            return false;
        }
        
        Enquiry enquiry = (Enquiry) repliable;
        
        // Only allow deletion if the enquiry belongs to this project
        if (enquiry.getProject().equals(project)) {
            boolean removed = enquiries.remove(enquiry);
            if (removed) {
                // Also remove the enquiry from the applicant's list of enquiries
                enquiry.getApplicant().removeEnquiry(enquiry);
            }
            return removed;
        }
        
        return false;
    }

    /**
     * Replies to an enquiry.
     *
     * @param repliable the enquiry being replied to
     * @param replyMessage the reply message
     * @param responderNric the NRIC of the responder
     * @return true if reply was successful, false otherwise
     */
    @Override
    public boolean reply(Repliable repliable, String replyMessage, String responderNric) {
        if (!(repliable instanceof Enquiry)) {
            return false;
        }
        
        Enquiry enquiry = (Enquiry) repliable;
        
        // Check if the enquiry belongs to this project
        if (!enquiry.getProject().equals(project)) {
            return false;
        }
        
        // Check if the responder is authorized to reply
        // Only HDB Officers assigned to this project or the Manager in charge can reply
        boolean isAuthorized = false;
        
        // Check if responder is an officer assigned to this project
        for (HDBOfficer officer : project.getAssignedOfficers()) {
            if (officer.getNric().equals(responderNric)) {
                isAuthorized = true;
                break;
            }
        }
        
        // Check if responder is the manager in charge
        if (!isAuthorized && project.getManagerInCharge() != null && 
            project.getManagerInCharge().getNric().equals(responderNric)) {
            isAuthorized = true;
        }
        
        if (isAuthorized) {
            enquiry.setReply(replyMessage);
            return true;
        }
        
        return false;
    }

    /**
     * Views all enquiries for the project.
     *
     * @return list of all enquiries for the project
     */
    @Override
    public List<Repliable> viewAll() {
        return new ArrayList<>(enquiries);
    }
    
    /**
     * Views enquiries by a specific applicant for the project.
     *
     * @param applicant the applicant whose enquiries to view
     * @return list of enquiries by the applicant for this project
     */
    @Override
    public List<Repliable> viewByApplicant(Applicant applicant) {
        return enquiries.stream()
            .filter(e -> e.getApplicant().equals(applicant))
            .collect(Collectors.toList());
    }
    
    /**
     * Views all answered enquiries for the project.
     * 
     * @return list of answered enquiries
     */
    public List<Enquiry> viewAnsweredEnquiries() {
        return enquiries.stream()
            .filter(Enquiry::isAnswered)
            .collect(Collectors.toList());
    }
    
    /**
     * Views all unanswered enquiries for the project.
     * 
     * @return list of unanswered enquiries
     */
    public List<Enquiry> viewUnansweredEnquiries() {
        return enquiries.stream()
            .filter(e -> !e.isAnswered())
            .collect(Collectors.toList());
    }
}