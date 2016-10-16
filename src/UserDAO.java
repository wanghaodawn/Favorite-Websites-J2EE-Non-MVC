/**
 * This is the HW3 of 08672
 * UserDAO
 * @author Hao Wang (haow2)
 * 09/25/2016
 * */
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class UserDAO {
	/**
	 * Instance variables
	 * */
    private List<Connection> connectionPool = new ArrayList<Connection>();
    private String jdbcDriver;
    private String jdbcURL;
    private String tableName;

    /**
     * Constructor.
     * */
    public UserDAO(String jdbcDriver, String jdbcURL, String tableName)
            throws MyDAOException {
        this.jdbcDriver = jdbcDriver;
        this.jdbcURL = jdbcURL;
        this.tableName = tableName;

        if (!tableExists()) {
        	createTable();
        }
    }

    /**
     * Connect to the MySQL server.
     * */
    private synchronized Connection getConnection() throws MyDAOException {
        if (connectionPool.size() > 0) {
            return connectionPool.remove(connectionPool.size() - 1);
        }

        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new MyDAOException(e);
        }

        try {
            return DriverManager.getConnection(jdbcURL);
        } catch (SQLException e) {
            throw new MyDAOException(e);
        }
    }
    
    /**
     * Release the connection to the MySQL server.
     * */
    private synchronized void releaseConnection(Connection con) {
        connectionPool.add(con);
    }

    /**
     * Create a new user.
     */
    public void create(UserBean user) throws MyDAOException {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO " + tableName + 
            		" (emailAddress, firstName, lastName, password)" + " VALUES (?,?,?,?)");
            pstmt.setString(1, user.getEmailAddress());
            pstmt.setString(2, user.getFirstName());
            pstmt.setString(3, user.getLastName());
            pstmt.setString(4, user.getPassword());
            
            int count = pstmt.executeUpdate();
            // If not for Data Manipulation Language (DML)
            if (count != 1) {
            	throw new SQLException("Insert updated " + count + " rows");
            }

            pstmt.close();
            
            con.commit();
            con.setAutoCommit(true);
            
            releaseConnection(con);
        } catch (Exception e) {
            try {
                if (con != null) {
                	con.close();
                }
            } catch (SQLException e2) { /* ignore */
            }
            throw new MyDAOException(e);
        }
    }
    
    /**
     * Find user according to emailAddress.
     */
    public UserBean read(String emailAddress) throws MyDAOException {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "
                    + tableName + " WHERE emailAddress=?");
            
            pstmt.setString(1, emailAddress);
            
            ResultSet rs = pstmt.executeQuery();

            UserBean user;
            if (!rs.next()) {
                user = null;
            } else {
                user = new UserBean();
                user.setUserId(rs.getInt("userId"));
                user.setEmailAddress(emailAddress);
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setPassword(rs.getString("password"));
            }
            
            rs.close();
            pstmt.close();
            
            con.commit();
            con.setAutoCommit(true);
            
            releaseConnection(con);
            return user;
            
        } catch (Exception e) {
            try {
                if (con != null) {
                	con.close();
                }
            } catch (SQLException e2) { /* ignore */
            }
            throw new MyDAOException(e);
        }
    }
    
    /**
     * Whether the table exists or not.
     * */
    private boolean tableExists() throws MyDAOException {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet rs = metaData.getTables(null, null, tableName, null);

            boolean answer = rs.next();

            rs.close();
            
            con.commit();
            con.setAutoCommit(true);
            
            releaseConnection(con);

            return answer;
        } catch (SQLException e) {
            try {
                if (con != null) {
                	con.close();
                }
            } catch (SQLException e2) { /* ignore */
            }
            throw new MyDAOException(e);
        }
    }
    
    /**
     * Create a new table.
     * */
    private void createTable() throws MyDAOException {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            
            Statement stmt = con.createStatement();
            stmt.executeUpdate("CREATE TABLE " + tableName + 
                    " (userId INTEGER AUTO_INCREMENT, emailAddress VARCHAR(255), firstName VARCHAR(255), " + 
            		"lastName VARCHAR(255), password VARCHAR(255), PRIMARY KEY(userId));");
            stmt.close();
            
            con.commit();
            con.setAutoCommit(true);
            
            releaseConnection(con);
        } catch (SQLException e) {
            try {
                if (con != null) {
                	con.close();
                }
            } catch (SQLException e2) { /* ignore */
            }
            throw new MyDAOException(e);
        }
    }
}
