/**
 * This is the HW3 of 08672
 * MyDAOException
 * @author Hao Wang (haow2)
 * 09/25/2016
 * */

public class MyDAOException extends Exception {
    private static final long serialVersionUID = 1L;

    public MyDAOException(Exception e) {
        super(e);
    }

    public MyDAOException(String s) {
        super(s);
    }
}

