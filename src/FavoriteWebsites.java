/**
 * This is the HW3 of 08672
 * @author Hao Wang (haow2)
 * 09/25/2016
 * */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class FavoriteWebsites
 */
@WebServlet("/servlet/FavoriteWebsites")
public class FavoriteWebsites extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instance Variables
	 * */
	private UserDAO userDAO;
	private FavoriteDAO favoriteDAO;
	
	/**
	 * Constant vairables
	 * */
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_NAME = "test";
    private static final String JDBC_URL = "jdbc:mysql://localhost/" + DB_NAME + "?useSSL=false";
	
	private final String USER_TABLE_NAME = "haow2_user";
	private final String FAVORITE_TABLE_NAME = "haow2_favorite";

	/**
	 * Init stage
	 * */
	public void init() throws ServletException {
		System.out.println("[Begin] Init");
		try {
			userDAO = new UserDAO(JDBC_DRIVER, JDBC_URL, USER_TABLE_NAME);
			favoriteDAO = new FavoriteDAO(JDBC_DRIVER, JDBC_URL, FAVORITE_TABLE_NAME, USER_TABLE_NAME);
		} catch (MyDAOException e) {
			throw new ServletException(e);
		}
		System.out.println("[End] Init");
	}
	
	/**
	 * Handle GET requests
	 * */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("[Begin] GET");
		
		HttpSession session = request.getSession();
		String register = request.getParameter("register");
		
//		System.out.println(register);
		
		if (session.getAttribute("user") != null) {
			// If session is modified by user and is a invalid number, then it will be automatically logged off
			// Logged in, show favorite websites
			favoriteList(request, response);
//			PrintWriter out = response.getWriter();
//			out.println("Success");
//			manageList(request, response);
		} else if (session.getAttribute("user") == null && register != null && register.equals("true")) {
			// go to register
			register(request, response);
		} else {
			// If register has invalid value, then goto login
			// Go to login
			login(request, response);
		}
		System.out.println("[End] GET\n");
	}
	
	/**
	 * Handle POST requests
	 * */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("[Begin] POST");
		doGet(request, response);
		System.out.println("[End] POST");
	}

	/**
	 * Login page
	 * */
	private void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("[Begin] login");
		
		List<String> errors = new ArrayList<String>();

		LoginForm loginForm = new LoginForm(request);

		if (!loginForm.isPresent()) {
			outputLoginPage(response, loginForm, null);
			System.out.println("[End] login");
			return;
		}

		errors.addAll(loginForm.getValidationErrors());
		if (errors.size() != 0) {
			outputLoginPage(response, loginForm, errors);
			System.out.println("[End] login");
			return;
		}

		try {
			UserBean userBean = null;
			if (loginForm.getButton().equals("Register")) {
				// Go to register page
				register(request, response);
			} else {
				// Stay at login page
				userBean = userDAO.read(loginForm.getEmailAddress());
				if (userBean == null) {
					errors.add("No such user.");
					outputLoginPage(response, loginForm, errors);
					return;
				}
				if (!loginForm.getPassword().equals(userBean.getPassword())) {
					errors.add("Incorrect password");
					outputLoginPage(response, loginForm, errors);
					return;
				}
			}
			
			// Add user to session
			HttpSession session = request.getSession();
			session.setAttribute("user", userBean);
			
			// Go to logged in page
			favoriteList(request, response);
//			manageList(request, response);
		} catch (MyDAOException e) {
			errors.add(e.getMessage());
			outputLoginPage(response, loginForm, errors);
		}
		System.out.println("[End] login");
	}
	
	/**
	 * Register Page
	 * */
	private void register(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("[Begin] register");
		List<String> errors = new ArrayList<String>();

		RegisterForm registerForm = new RegisterForm(request);

		if (!registerForm.isPresent()) {
			outputRegisterPage(response, registerForm, null);
			System.out.println("[End] register");
			return;
		}

		errors.addAll(registerForm.getValidationErrors());
		// Check email exists or not
		try {
			UserBean userBean = userDAO.read(registerForm.getEmailAddress());
			if (userBean != null) {
				errors.add("User already exists");
			}
		} catch (MyDAOException e) {
			errors.add(e.getMessage());
			outputRegisterPage(response, registerForm, errors);
		}
		
		if (errors.size() != 0) {
			outputRegisterPage(response, registerForm, errors);
			System.out.println("[End] register");
			return;
		}
		
		try {
			UserBean userBean = new UserBean();
			userBean.setEmailAddress(registerForm.getEmailAddress());
			userBean.setFirstName(registerForm.getFirstName());
			userBean.setLastName(registerForm.getLastName());
			userBean.setPassword(registerForm.getPassword());
			userDAO.create(userBean);
			
			// Otherwise, no userId is provided, so cannot create urls
			userBean = userDAO.read(registerForm.getEmailAddress());
			
			// Add user to session
			HttpSession session = request.getSession();
			session.setAttribute("user", userBean);
			
			// Go to logged in page
			favoriteList(request, response);
			
//			PrintWriter out = response.getWriter();
//			out.println("Success");
//			manageList(request, response);
		} catch (MyDAOException e) {
			errors.add(e.getMessage());
			outputRegisterPage(response, registerForm, errors);
		}
		System.out.println("[End] register");
	}
	

	private void favoriteList(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[Begin] favoriteList");
		
		List<String> errors = new ArrayList<String>();
		
		// Look at the action parameter to see what we're doing to the list
		String buttonVal = request.getParameter("button");
		FavoriteForm favoriteForm = new FavoriteForm(request);
		
		// ClickCount + 1
		String favoriteId = request.getParameter("favoriteId");
		if (favoriteId != null && favoriteId.length() != 0) {
			try {
				int id = Integer.parseInt(favoriteId);
				favoriteDAO.incresaseClickCountByOne(id);
			} catch (NumberFormatException e) {
				errors.add(e.getMessage());
			} catch (MyDAOException e) {
				errors.add(e.getMessage());
			}
		}
		
		UserBean user = (UserBean) request.getSession().getAttribute("user");
//		System.out.println(request.getSession());
//		System.out.println(user.getFirstName());
//		System.out.println(user.getLastName());
		
		// Add a new item
		if (buttonVal != null && buttonVal.equals("Add Favorite")) {
			addFavoriteItem(request, response, favoriteForm);
			return;
		}
		
		// Logout
		if (buttonVal != null && buttonVal.equals("Logout")) {
			HttpSession session = request.getSession();
			session.setAttribute("user", null);
//			doGet(request, response);
			outputLoginPage(response, null, null);
			return;
		}

		// No change to list requested
		printFavoriteList(response, favoriteForm, errors, user);
		
		System.out.println("[End] favoriteList");
	}

	private void addFavoriteItem(HttpServletRequest request, HttpServletResponse response, 
			FavoriteForm favoriteForm) throws ServletException, IOException {
		System.out.println("[Begin] addFavoriteItem");
		
		List<String> errors = new ArrayList<String>();
		
		UserBean user = (UserBean) request.getSession().getAttribute("user");

		errors.addAll(favoriteForm.getValidationErrors());
		if (errors.size() > 0) {
			printFavoriteList(response, favoriteForm, errors, user);
			return;
		}

		try {
			FavoriteBean favoriteBean = new FavoriteBean();
			
			favoriteBean.setUserId(user.getUserId());
			favoriteBean.setURL(favoriteForm.getURL());
			favoriteBean.setComment(favoriteForm.getComment());
			favoriteBean.setClickCount(0);
			
			favoriteDAO.create(favoriteBean);
			printFavoriteList(response, favoriteForm, errors, user);
		} catch (MyDAOException e) {
			errors.add(e.getMessage());
			printFavoriteList(response, favoriteForm, errors, user);
		}
		System.out.println("[End] addFavoriteIterm");
	}

	// Methods that generate & output HTML
	/**
	 * Print the Login page
	 * */
	private void outputLoginPage(HttpServletResponse response, LoginForm form,
			List<String> errors) throws IOException {
		System.out.println("[Begin] output login page");
		
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		// Header
		out.println("<!doctype html>");
		out.println("<html>");
		out.println("    <head>");
		out.println("        <meta charset=\"UTF-8\">");
		out.println("        <title>Login - HW 3 from Hao Wang (haow2)</title>");
		out.println("    </head>");
		out.println("    <body>");
		out.println("<h2> Favorite Website Login </h2>");

		// Errors
		if (errors != null && errors.size() > 0) {
			for (String error : errors) {
				out.println("<p style=\"font-size: large; color: red\">");
				out.println(error);
				out.println("</p>");
			}
		}

		// Generate an HTML <form> to get data from the user
		out.println("        <form method=\"POST\">");
		out.println("            <table>");
		out.println("                <tr>");
		out.println("                    <td>Email Address:</td>");
		
		if (form != null && form.getEmailAddress() != null) {
			out.println("<td><input type=\"text\" name=\"emailAddress\" value=\"" + form.getEmailAddress() + "\"/></td>");
		} else {
			out.println("<td><input type=\"text\" name=\"emailAddress\"/></td>");
		}
		
		out.println("                </tr>");
		out.println("                <tr>");
		out.println("                    <td>Password:</td>");
		
		out.println("                    <td><input type=\"text\" name=\"password\"/></td>");
		out.println("                </tr>");
		out.println("                <tr>");
		out.println("                    <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//		out.println("                        <input type=\"submit\" name=\"button\" value=\"Register\"/>");
		out.println("                    <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("                        <input type=\"submit\" name=\"button\" value=\"Login\"/></td>");
		out.println("                    </td>");
		out.println("                </tr>");
		out.println("            </table>");
		out.println("        </form>");
		out.println("        <a href=\"?register=true\"><p>Register</p></a>");
		out.println("    </body>");
		out.println("</html>");
		
		System.out.println("[End] output login page");
	}
	
	/**
	 * Print the register page
	 * */
	private void outputRegisterPage(HttpServletResponse response, RegisterForm form,
			List<String> errors) throws IOException {
		System.out.println("[Begin] output register page");
		
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		// Header
		out.println("<!doctype html>");
		out.println("<html>");
		out.println("    <head>");
		out.println("        <meta charset=\"UTF-8\">");
		out.println("        <title>Register - HW 3 from Hao Wang (haow2)</title>");
		out.println("    </head>");
		out.println("        <h2>Favorite Website Registration</h2>");
		
		// Errors
		if (errors != null && errors.size() > 0) {
			for (String error : errors) {
				out.println("<p style=\"font-size: large; color: red\">");
				out.println(error);
				out.println("</p>");
			}
		}
		
		out.println("        <form method=\"POST\">");
		out.println("            <table>");
		out.println("                <tr>");
		out.println("                    <td>Email Address:</td>");
		
		// Email
		if (form != null && form.getEmailAddress() != null) {
			out.println("<td><input type=\"text\" name=\"emailAddress\" value=\"" + form.getEmailAddress() + "\"/></td>");
		} else {
			out.println("<td><input type=\"text\" name=\"emailAddress\"/></td>");
		}
		
		out.println("                </tr>");
		out.println("                <tr>");
		out.println("                    <td>First Name:</td>");
		
		// First Name
		if (form != null && form.getFirstName() != null) {
			out.println("<td><input type=\"text\" name=\"firstName\" value=\"" + form.getFirstName() + "\"/></td>");
		} else {
			out.println("<td><input type=\"text\" name=\"firstName\"/></td>");
		}
		
		out.println("                </tr>");
		out.println("                <tr>");
		out.println("                    <td>Last Name:</td>");
		
		// Last Name
		if (form != null && form.getLastName() != null) {
			out.println("<td><input type=\"text\" name=\"lastName\" value=\"" + form.getLastName() + "\"/></td>");
		} else {
			out.println("<td><input type=\"text\" name=\"lastName\"/></td>");
		}
		
		out.println("                </tr>");
		out.println("                <tr>");
		out.println("                    <td>Password:</td>");
		out.println("                    <td><input type=\"text\" name=\"password\"/></td>");
		out.println("                </tr>");
		out.println("                <tr>");
		out.println("                    <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//		out.println("                        <input type=\"submit\" name=\"button\" value=\"Login\"/>");
		out.println("                    </td>");
		out.println("                    <td>");
		out.println("                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("                        <input type=\"submit\" name=\"button\" value=\"Register\"/>");
		out.println("                    </td>");
		out.println("                </tr>");
		out.println("            </table>");
		out.println("        </form>");
		out.println("        <a href=\"?register=false\"><p>Login</p></a>");
		out.println("    </body>");
		out.println("</html>");
		
		System.out.println("[End] output register page");
	}

	/**
	 * Print the favorite list
	 * */
	private void printFavoriteList(HttpServletResponse response, FavoriteForm favoriteForm,
			List<String> errors, UserBean user) throws IOException {
		System.out.println("[Begin] printFavoriteList");
		
		// Get the list of items to display at the end
		FavoriteBean[] favoriteBeans;
		int userId = user.getUserId();
		
		// Get all errors
		try {
			favoriteBeans = favoriteDAO.getUserFavorites(userId);
		} catch (MyDAOException e) {
			// If there's an access error, add the message to our list of
			// messages
			errors.add(e.getMessage());
			favoriteBeans = new FavoriteBean[0];
		}

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		// Header
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("    <head>");
		out.println("        <meta charset=\"UTF-8\">");
		out.println("        <title>Favorite Page - HW 3 from Hao Wang (haow2)</title>");
		out.println("    </head>");
		
		// Print first name and last name
		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		
		out.println("        <h2>Favorites for&nbsp;" + firstName + "&nbsp;" + lastName + "</h2>");
		
		// Print the form for adding favorite websites
		out.println("        <form method=\"POST\">");
		out.println("            <table>");
		out.println("                <tr>");
		out.println("                    <td>URL:</td>");
		out.println("                    <td><input type=\"text\" name=\"URL\"/></td>");
		out.println("                </tr>");
		out.println("                <tr>");
		out.println("                    <td>Comment:</td>");
		out.println("                    <td><input type=\"text\" name=\"comment\"/></td>");
		out.println("                </tr>");
		out.println("                <tr>");
		out.println("                    <td><input type=\"submit\" name=\"button\" value=\"Logout\"/></td>");
		out.println("                    <td><input type=\"submit\" name=\"button\" value=\"Add Favorite\"/></td>");
		out.println("                </tr>");
		out.println("            </table>");
		out.println("        </form>");
		
		// Print all errors
		for (String error : errors) {
			out.println("<p style=\"font-size: large; color: red\">");
			out.println(error);
			out.println("</p>");
		}
		
		out.println("        <ul>");
		
		// Print the list that the user created
		for (int i = 0; i < favoriteBeans.length; i++) {
			int favoriteId = favoriteBeans[i].getFavoriteId();
			String URL = favoriteBeans[i].getURL();
			String comment = favoriteBeans[i].getComment();
			int clickCount = favoriteBeans[i].getClickCount();
			
			out.println("            <li>");
			out.println("                <a href=\"?favoriteId=" + favoriteId + "\">" + URL + "</a>");
			out.println("                <p>" + comment + "</p>");
			out.println("                <p>" + clickCount + " Clicks</p>");
			out.println("            </li>");
		}
		
		out.println("        </ul>");
		out.println("    </body>");
		out.println("</html>");
		
		System.out.println("[End] printFavoriteList");
	}
}
