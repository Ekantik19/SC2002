package com.bto.enquiry;

import com.bto.model.Applicant;
import java.util.List;

/**
 * The {@code RepliableEditorInterface} is the interface for enquiry editors in the BTO system.
 * It provides the basic functionality that a repliable editor should have.
 *
 * @author Your Name
 * @version 1.0
 */
public interface RepliableEditorInterface {
    /**
     * Creates a new repliable.
     *
     * @param content the content of the repliable being created
     * @param applicant the applicant that created the repliable
     * @return the created repliable
     */
    Repliable create(String content, Applicant applicant);

    /**
     * Edits a repliable.
     *
     * @param repliable  the repliable that is being edited
     * @param newContent the new content that replaces the repliable's old content
     * @return true if editing was successful, false otherwise
     */
    boolean edit(Repliable repliable, String newContent);

    /**
     * Deletes a repliable.
     *
     * @param repliable the repliable that is being deleted
     * @return true if deletion was successful, false otherwise
     */
    boolean delete(Repliable repliable);

    /**
     * Replies to a repliable with a reply message.
     *
     * @param repliable    the repliable that is being replied to
     * @param replyMessage the reply message
     * @param responderNric the NRIC of the responder
     * @return true if reply was successful, false otherwise
     */
    boolean reply(Repliable repliable, String replyMessage, String responderNric);

    /**
     * Views a list of repliable.
     *
     * @return list of repliables
     */
    List<Repliable> viewAll();
    
    /**
     * Views a list of repliables by a specific applicant.
     *
     * @param applicant the applicant whose repliables to view
     * @return list of repliables by the applicant
     */
    List<Repliable> viewByApplicant(Applicant applicant);
}