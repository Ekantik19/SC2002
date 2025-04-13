package controller.abstracts;

/**
 * Abstract base class for controllers in the BTO Management System.
 * Provides common functionality such as error handling and result reporting.
 * 
 * @author Your Name
 * @version 1.0
 */
public abstract class ABaseController {
    
    /**
     * Enum for operation result types.
     */
    protected enum ResultType {
        SUCCESS,
        FAILURE,
        NOT_FOUND,
        UNAUTHORIZED,
        VALIDATION_ERROR,
        ALREADY_EXISTS,
        BUSINESS_RULE_VIOLATION
    }
    
    /**
     * Class for operation results.
     */
    protected static class OperationResult {
        private final ResultType type;
        private final String message;
        
        /**
         * Constructor for OperationResult.
         * 
         * @param type The result type
         * @param message The result message
         */
        public OperationResult(ResultType type, String message) {
            this.type = type;
            this.message = message;
        }
        
        /**
         * Gets the result type.
         * 
         * @return The result type
         */
        public ResultType getType() {
            return type;
        }
        
        /**
         * Gets the result message.
         * 
         * @return The result message
         */
        public String getMessage() {
            return message;
        }
        
        /**
         * Checks if the result is successful.
         * 
         * @return true if the result is successful, false otherwise
         */
        public boolean isSuccess() {
            return type == ResultType.SUCCESS;
        }
    }
    
    /**
     * Creates a success result.
     * 
     * @param message The success message
     * @return A success operation result
     */
    protected OperationResult success(String message) {
        return new OperationResult(ResultType.SUCCESS, message);
    }
    
    /**
     * Creates a failure result.
     * 
     * @param message The failure message
     * @return A failure operation result
     */
    protected OperationResult failure(String message) {
        return new OperationResult(ResultType.FAILURE, message);
    }
    
    /**
     * Creates a not found result.
     * 
     * @param message The not found message
     * @return A not found operation result
     */
    protected OperationResult notFound(String message) {
        return new OperationResult(ResultType.NOT_FOUND, message);
    }
    
    /**
     * Creates an unauthorized result.
     * 
     * @param message The unauthorized message
     * @return An unauthorized operation result
     */
    protected OperationResult unauthorized(String message) {
        return new OperationResult(ResultType.UNAUTHORIZED, message);
    }
    
    /**
     * Creates a validation error result.
     * 
     * @param message The validation error message
     * @return A validation error operation result
     */
    protected OperationResult validationError(String message) {
        return new OperationResult(ResultType.VALIDATION_ERROR, message);
    }
    
    /**
     * Creates an already exists result.
     * 
     * @param message The already exists message
     * @return An already exists operation result
     */
    protected OperationResult alreadyExists(String message) {
        return new OperationResult(ResultType.ALREADY_EXISTS, message);
    }
    
    /**
     * Creates a business rule violation result.
     * 
     * @param message The business rule violation message
     * @return A business rule violation operation result
     */
    protected OperationResult businessRuleViolation(String message) {
        return new OperationResult(ResultType.BUSINESS_RULE_VIOLATION, message);
    }
    
    /**
     * Validates that a parameter is not null.
     * 
     * @param parameter The parameter to validate
     * @param parameterName The name of the parameter
     * @return true if the parameter is not null, false otherwise
     */
    protected boolean validateNotNull(Object parameter, String parameterName) {
        if (parameter == null) {
            System.out.println("Validation error: " + parameterName + " cannot be null");
            return false;
        }
        return true;
    }
    
    /**
     * Validates that a string is not null or empty.
     * 
     * @param parameter The string to validate
     * @param parameterName The name of the parameter
     * @return true if the string is not null or empty, false otherwise
     */
    protected boolean validateNotNullOrEmpty(String parameter, String parameterName) {
        if (parameter == null || parameter.trim().isEmpty()) {
            System.out.println("Validation error: " + parameterName + " cannot be null or empty");
            return false;
        }
        return true;
    }
    
    /**
     * Handles an operation result by logging and returning the appropriate value.
     * 
     * @param result The operation result to handle
     * @param successValue The value to return if the operation was successful
     * @param failureValue The value to return if the operation failed
     * @param <T> The type of the return value
     * @return The appropriate return value based on the operation result
     */
    protected <T> T handleResult(OperationResult result, T successValue, T failureValue) {
        if (result.isSuccess()) {
            System.out.println("Operation successful: " + result.getMessage());
            return successValue;
        } else {
            System.out.println("Operation failed (" + result.getType() + "): " + result.getMessage());
            return failureValue;
        }
    }
}