/**
 * This is the HW3 of 08672
 * LoginForm
 * @author Hao Wang (haow2)
 * 09/25/2016
 * */
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

public class LoginForm {
	/**
	 * Instance variables
	 * */
    private String emailAddress;
    private String password;
    private String button;

    /**
     * Constructor
     * */
    public LoginForm(HttpServletRequest request) {
        emailAddress = request.getParameter("emailAddress");
        password = request.getParameter("password");
        button = request.getParameter("button");
    }
    
    /**
     * Get fields
     * */
    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public String getButton() {
        return button;
    }
    
    /**
     * validate button is null or not
     * */
    public boolean isPresent() {
        return button != null;
    }
    
    /**
     * Validate email address
     * */
    public boolean isValidEmailAddress(String email) {
    	boolean result = true;
    	try {
    		InternetAddress emailAddr = new InternetAddress(email);
    		emailAddr.validate();
    	} catch (AddressException ex) {
		   result = false;
    	}
    	return result;
	}

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<String>();
        
        // Validate fields null or zero length
        if (emailAddress == null || emailAddress.length() == 0) {
        	errors.add("Email address is required");
        } else {
        	// Validate email address
            if (!isValidEmailAddress(emailAddress)) {
            	errors.add("Invalid email address.");
            }
        }
        if (password == null || password.length() == 0) {
        	errors.add("Password is required");
        }
        if (button == null) {
        	errors.add("Button is required");
        }
            
        return errors;
    }
}
