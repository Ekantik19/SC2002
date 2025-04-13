package service;

import model.Applicant;
import model.enums.FlatType;

/**
 * Service class for checking eligibility of applicants for BTO flats.
 * Centralizes business rules related to eligibility.
 * 
 * @author Your Name
 * @version 1.0
 */
public class EligibilityCheckerService {
    
    // Age thresholds
    private static final int MARRIED_MIN_AGE = 21;
    private static final int SINGLE_MIN_AGE = 35;
    
    /**
     * Checks if an applicant is eligible to apply for BTO in general.
     * 
     * @param applicant The applicant to check
     * @return true if the applicant is eligible, false otherwise
     */
    public boolean isEligibleForBTO(Applicant applicant) {
        if (applicant == null) {
            return false;
        }
        
        if (applicant.isMarried()) {
            return applicant.getAge() >= MARRIED_MIN_AGE;
        } else {
            return applicant.getAge() >= SINGLE_MIN_AGE;
        }
    }
    
    /**
     * Checks if an applicant is eligible for a specific flat type.
     * 
     * @param applicant The applicant to check
     * @param flatType The flat type to check eligibility for
     * @return true if the applicant is eligible, false otherwise
     */
    public boolean isEligibleForFlatType(Applicant applicant, FlatType flatType) {
        // Must be eligible for BTO first
        if (!isEligibleForBTO(applicant)) {
            return false;
        }
        
        // Singles can only apply for 2-Room
        if (!applicant.isMarried() && flatType != FlatType.TWO_ROOM) {
            return false;
        }
        
        return true;
    }
}