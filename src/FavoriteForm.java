/**
 * This is the HW3 of 08672
 * FavoriteForm
 * @author Hao Wang (haow2)
 * 09/25/2016
 * */
import java.util.*;
import javax.servlet.http.HttpServletRequest;

public class FavoriteForm {
	private String URL;
	private String comment;

    public FavoriteForm(HttpServletRequest request) {
    	URL = request.getParameter("URL");
    	comment = request.getParameter("comment");
    	if (URL == null) {
    		URL = "";
    	}
    	if (comment == null) {
    		comment = "";
    	}
        URL = sanitize(URL);
        comment = sanitize(comment);
    }

    public String getURL() {
        return URL;
    }
    
    public String getComment() {
    	return comment;
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<String>();
        if (URL == null || URL.length() == 0) {
            errors.add("URL is required");
        }
        return errors;
    }

    private String sanitize(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
