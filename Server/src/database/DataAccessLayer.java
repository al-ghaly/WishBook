package database;

import java.sql.*;
import oracle.jdbc.OracleDriver;
import utilities.*;

public class DataAccessLayer {
  
      public static void connect() throws SQLException{
        DriverManager.registerDriver(new OracleDriver());
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:XE", 
                "WishBook", "123");
    }
      
    public static int addUser(Client client) throws SQLException{
        int results;
        DriverManager.registerDriver(new OracleDriver());
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:XE", 
                "WishBook", "123");

        PreparedStatement stmt = con.prepareStatement(
                "insert into users values(?, ?, ?, ?, ?)");
        stmt.setString(1, client.getUsername());
        stmt.setString(2, client.getPassword());
        stmt.setString(3, client.getEmail());
        stmt.setString(4, client.getPhone());
        stmt.setLong(5, client.getBalance());
        results = stmt.executeUpdate();         
        return results;
    }

    public static String getUser(String username) throws SQLException{
        ResultSet results;
        DriverManager.registerDriver(new OracleDriver());
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:XE",
                "WishBook", "123");

        PreparedStatement stmt = con.prepareStatement(
                "Select password, balance from users where username = ?");
        stmt.setString(1, username);

        results = stmt.executeQuery();
        results.next();
        return results.getString(1) + "-" + results.getLong(2);
    }

    public static ResultSet getFriends(String username) throws SQLException{
        ResultSet results;
        DriverManager.registerDriver(new OracleDriver());
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:XE",
                "WishBook", "123");

        PreparedStatement stmt = con.prepareStatement(
                "SELECT friend_name\n" +
                        "  FROM friends\n" +
                        " WHERE username = ?\n" +
                        "UNION\n" +
                        "SELECT username\n" +
                        "  FROM friends\n" +
                        " WHERE friend_name = ?");
        stmt.setString(1, username);
        stmt.setString(2, username);

        results = stmt.executeQuery();
        return results;
    }

    public static ResultSet getWishList(String username) throws SQLException{
        ResultSet results;
        DriverManager.registerDriver(new OracleDriver());
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:XE",
                "WishBook", "123");

        PreparedStatement stmt = con.prepareStatement(
                "select name from ");
        stmt.setString(1, username);
        stmt.setString(2, username);

        results = stmt.executeQuery();
        return results;
    }
}
