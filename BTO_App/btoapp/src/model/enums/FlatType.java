package model.enums;

/**
 * Enumeration for the types of flats available in BTO projects.
 * 
 * @author Your Name
 * @version 1.0
 */
public enum FlatType {
    /**
     * 2-Room flat type.
     * Available to all applicants (Singles 35+ and married couples).
     */
    TWO_ROOM("2-Room"),
    
    /**
     * 3-Room flat type.
     * Available only to married couples.
     */
    THREE_ROOM("3-Room");
    
    private final String displayName;
    
    /**
     * Constructor for FlatType enum.
     * 
     * @param displayName The human-readable name to display for this flat type
     */
    FlatType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name of the flat type.
     * 
     * @return The human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns the string representation of this enum value.
     * 
     * @return The display name of this flat type
     */
    @Override
    public String toString() {
        return displayName;
    }
    
    /**
     * Parse a string representation to get the corresponding FlatType enum.
     * 
     * @param flatTypeStr The string representation of the flat type
     * @return The matching FlatType enum value, or null if no match
     */
    public static FlatType fromString(String flatTypeStr) {
        if (flatTypeStr == null) {
            return null;
        }
        
        String normalizedStr = flatTypeStr.trim();
        for (FlatType type : FlatType.values()) {
            if (type.getDisplayName().equalsIgnoreCase(normalizedStr)) {
                return type;
            }
        }
        return null;
    }
}