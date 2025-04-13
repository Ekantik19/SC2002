package enquiry;

import java.util.Date;

import model.Applicant;
import model.Project;

/**
 * The {@code Enquiry} class represents an inquiry made by an applicant regarding a BTO project.
 * It extends the {@link Repliable} class, inheriting functionality related to replies.
 * Each enquiry has a question and can have a corresponding reply.
 *
 * @author Your Name
 * @version 1.0
 */
public class Enquiry extends Repliable {
    /**
     * The unique identifier for the enquiry.
     */
    private String enquiryId;
    
    /**
     * The project the enquiry is about.
     */
    private Project project;
    
    /**
     * The question associated with this enquiry.
     */
    private String question;

    /**
     * The reply to the enquiry.
     */
    private String reply;
    
    /**
     * The date when the enquiry was submitted.
     */
    private Date submissionDate;

    /**
     * Constructs a new Enquiry object with the specified question, applicant, and project.
     *
     * @param enquiryId Enquiry ID assoiciated with enquiry
     * @param reply reply from manager or officer
     * @param question  the question associated with this enquiry
     * @param applicant the applicant making the enquiry
     * @param project   the project the enquiry is about
     */
    public Enquiry(String enquiryId, Applicant applicant, Project project, String question, Date submissionDate) {
        super(applicant);
        this.enquiryId = enquiryId;
        this.project = project;
        this.question = question;
        this.submissionDate = submissionDate;
        this.reply = "";
    }

    /**
     * Gets the unique identifier for the enquiry.
     * 
     * @return the enquiry ID
     */
    public String getEnquiryId() {
        return enquiryId;
    }

    /**
     * Gets the project the enquiry is about.
     * 
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the question associated with this enquiry.
     *
     * @param question the new question for this enquiry
     */
    public void setEnquiryText(String text) {
        this.question = text;
    }

    /**
     * Gets the question associated with this enquiry.
     *
     * @return the question of this enquiry
     */
    public String getEnquiryText() {
        return this.question;
    }

    /**
     * Gets the reply to this enquiry.
     *
     * @return the reply to this enquiry
     */
    public String getReply() {
        return reply;
    }

    /**
     * Sets the reply to this enquiry.
     *
     * @param reply the new reply for this enquiry
     */
    public void setReply(String reply) {
        this.reply = reply;
    }
    
    /**
     * Gets the submission date of the enquiry.
     * 
     * @return the submission date
     */
    public Date getSubmissionDate() {
        return submissionDate;
    }
    
    /**
     * Checks if the enquiry has been answered.
     * 
     * @return true if the enquiry has been answered, false otherwise
     */
    public boolean isAnswered() {
        return reply != null && !reply.trim().isEmpty();
    }
    
    /**
     * Returns a string representation of the enquiry.
     * 
     * @return a string with basic enquiry information
     */
    @Override
    public String toString() {
        return "Enquiry ID: " + enquiryId + 
               ", Project: " + project.getProjectName() + 
               ", Submitted: " + submissionDate + 
               ", Answered: " + (isAnswered() ? "Yes" : "No");
    }
}