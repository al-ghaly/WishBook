package database;

import java.sql.*;
import oracle.jdbc.OracleDriver;
import utilities.Client;

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
        //TODO: Check if the username is already there
        stmt.setString(1, client.getUsername());
        stmt.setString(2, client.getPassword());
        stmt.setString(3, client.getEmail());
        stmt.setString(4, client.getPhone());
        stmt.setLong(5, client.getBalance());
        results = stmt.executeUpdate();  
        System.out.println(results);        
        return results;
    }
   }
