package com.bto.controller.interfaces;

import com.bto.model.Applicant;
import com.bto.model.Application;
import com.bto.model.Project;
import com.bto.model.enums.FlatType;

/**
 * Interface defining the contract for Applicant Controller in the BTO Management System.
 * 
 * @author Your Name
 * @version 1.0
 */
public interface IApplicantController {
    
    /**
     * Checks if an applicant is eligible to apply for a specific project.
     * 
     * @param applicant The applicant to check
     * @param project The project to apply for
     * @param flatType The flat type selected
     * @return true if the applicant is eligible, false otherwise
     */
    boolean checkApplicantEligibility(Applicant applicant, Project project, FlatType flatType);
    
    /**
     * Submits an application for a project.
     * 
     * @param applicant The applicant submitting the application
     * @param project The project to apply for
     * @param flatType The flat type selected
     * @return The created application, or null if submission fails
     */
    Application submitApplication(Applicant applicant, Project project, FlatType flatType);
    
    /**
     * Requests withdrawal of an application.
     * 
     * @param applicant The applicant requesting withdrawal
     * @return true if withdrawal request is successful, false otherwise
     */
    boolean requestApplicationWithdrawal(Applicant applicant);
    
    /**
     * Checks if an applicant is eligible for a specific flat type.
     * 
     * @param applicant The applicant to check
     * @param flatType The flat type to check
     * @return true if the applicant is eligible for the flat type, false otherwise
     */
    boolean isEligibleForFlatType(Applicant applicant, FlatType flatType);
}