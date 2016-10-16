/**
 * This is the HW3 of 08672
 * FavoriteDAO
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

public class FavoriteDAO {
	/**
	 * Instance variables
	 * */
    private List<Connection> connectionPool = new ArrayList<Connection>();
    private String jdbcDriver;
    private String jdbcURL;
    private String tableName;
    private String userTableName;
    
    /**
     * Constructor.
     * */
    public FavoriteDAO(String jdbcDriver, String jdbcURL, String tableName, String userTableName)
            throws MyDAOException {
        this.jdbcDriver = jdbcDriver;
        this.jdbcURL = jdbcURL;
        this.tableName = tableName;
        this.userTableName = userTableName;

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
     * Create a new favoriteBean.
     */
    public void create(FavoriteBean favoriteBean) throws MyDAOException {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            
            String sql = "INSERT INTO " + tableName + 
            		" (userId, URL, comment, clickCount)" + " VALUES (?,?,?,?);";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, favoriteBean.getUserId());
            pstmt.setString(2, favoriteBean.getURL());
            pstmt.setString(3, favoriteBean.getComment());
            pstmt.setInt(4, favoriteBean.getClickCount());

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
    
    public void incresaseClickCountByOne(int favoriteId) throws MyDAOException {
    	Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            Statement stmt = con.createStatement();
            stmt.execute("UPDATE " + tableName + " SET clickCount = clickCount + 1 WHERE favoriteId=" + favoriteId + ";");
            
            stmt.close();
            
            con.commit();
            con.setAutoCommit(true);
            
            releaseConnection(con);
        } catch (SQLException e) {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e2) { /* ignore */
            }
            throw new MyDAOException(e);
        }
    }
    
    /**
     * Return all the favorite beans of the target User
     * */
    public FavoriteBean[] getUserFavorites(int userId) throws MyDAOException {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE userId=" + userId + ";");
            
            List<FavoriteBean> list = new ArrayList<FavoriteBean>();
            while (rs.next()) {
                FavoriteBean favoriteBean = new FavoriteBean();
                favoriteBean.setFavoriteId(rs.getInt("favoriteId"));
                favoriteBean.setUserId(rs.getInt("userId"));
                favoriteBean.setURL(rs.getString("URL"));
                favoriteBean.setComment(rs.getString("comment"));
                favoriteBean.setClickCount(rs.getInt("clickCount"));
                list.add(favoriteBean);
            }
            stmt.close();
            
            con.commit();
            con.setAutoCommit(true);
            
            releaseConnection(con);

            return list.toArray(new FavoriteBean[list.size()]);
        } catch (SQLException e) {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e2) { /* ignore */
            }
            throw new MyDAOException(e);
        }
    }
    
    /**
     * Get the total number of favoriteId
     * */
    public int size() throws MyDAOException {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(favoriteId) FROM "
                    + tableName);

            rs.next();
            int count = rs.getInt("COUNT(id)");

            stmt.close();
            
            con.commit();
            con.setAutoCommit(true);
            
            releaseConnection(con);

            return count;
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
     * Whether the table exists or not.
     * */
    private boolean tableExists() throws MyDAOException {
    	Connection con = null;
        try {
            con = getConnection();
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet rs = metaData.getTables(null, null, tableName, null);

            boolean answer = rs.next();

            rs.close();
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
            String sql = "CREATE TABLE " + tableName + 
                    " (favoriteId INTEGER AUTO_INCREMENT, userId INTEGER, URL VARCHAR(255), " + 
            		"comment VARCHAR(255), clickCount INTEGER, PRIMARY KEY(favoriteId), " + 
                    "FOREIGN KEY (userId) REFERENCES " + userTableName + " (userId) ON DELETE CASCADE);";
            System.out.println(sql);
            stmt.executeUpdate(sql);
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
