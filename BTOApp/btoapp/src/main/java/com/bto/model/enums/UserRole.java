package com.bto.model.enums;

/**
 * Enumeration for the roles of users in the BTO Management System.
 * Each role has specific permissions and capabilities within the system.
 * 
 * @author Your Name
 * @version 1.0
 */
public enum UserRole {
    /**
     * Regular user who can apply for BTO projects.
     */
    APPLICANT("Applicant"),
    
    /**
     * HDB Officer who can manage BTO applications and bookings.
     */
    OFFICER("HDB Officer"),
    
    /**
     * HDB Manager who can manage BTO projects and approve applications.
     */
    MANAGER("HDB Manager");
    
    private final String displayName;
    
    /**
     * Constructor for UserRole enum.
     * 
     * @param displayName The human-readable name to display for this role
     */
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name of the user role.
     * 
     * @return The human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns the string representation of this enum value.
     * 
     * @return The display name of this role
     */
    @Override
    public String toString() {
        return displayName;
    }
}