package com.bto.enquiry;

import com.bto.model.Applicant;

/**
 * The {@code Repliable} is the base class for enquiries in the BTO Management System.
 * It provides the basic functionality (methods and attributes) that a repliable should have.
 *
 * @author Your Name
 * @version 1.0
 */
public abstract class Repliable {
    /**
     * The applicant associated with the repliable.
     */
    private Applicant applicant;

    /**
     * Constructs a new repliable associated with an applicant.
     *
     * @param applicant the applicant that created the repliable
     */
    public Repliable(Applicant applicant) {
        this.applicant = applicant;
    }

    /**
     * Gets the applicant that created the repliable.
     *
     * @return the applicant that created the repliable
     */
    public Applicant getApplicant() {
        return applicant;
    }

    /**
     * Sets the applicant that created the repliable.
     *
     * @param applicant the applicant that created the repliable
     */
    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }
}