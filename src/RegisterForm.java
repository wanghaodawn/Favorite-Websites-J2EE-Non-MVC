/**
 * This is the HW3 of 08672
 * RegisterForm
 * @author Hao Wang (haow2)
 * 09/25/2016
 * */
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Inherited some instance variables and methods from LoginForm
 * */
public class RegisterForm extends LoginForm {
	/**
	 * Instance variables
	 * */
    private String firstName;
    private String lastName;

    /**
     * Constructor
     * */
    public RegisterForm(HttpServletRequest request) {
    	super(request);
        firstName = request.getParameter("firstName");
        lastName = request.getParameter("lastName");
    }
    
    /**
     * Get fields
     * */
    public String getFirstName() {
    	return firstName;
    }
    
    public String getLastName() {
    	return lastName;
    }
    
    @Override
    public List<String> getValidationErrors() {
        List<String> errors = super.getValidationErrors();
        
        // Validate fields null or zero length
        if (firstName == null || firstName.length() == 0) {
        	errors.add("First name is required.");
        }
        if (lastName == null || lastName.length() == 0) {
        	errors.add("Last name is required.");
        }
            
        return errors;
    }
}
