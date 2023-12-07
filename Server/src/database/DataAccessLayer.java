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
                "Select password, balance, email, phone from users where username = ?");
        stmt.setString(1, username);

        results = stmt.executeQuery();
        results.next();
        return results.getString(1) + "-" + results.getLong(2)
                 + "-" + results.getString(3) + "-" + results.getString(4);
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
                "SELECT ui.id,\n" +
                        "       paid,\n" +
                        "       name,\n" +
                        "       category,\n" +
                        "       price\n" +
                        ", date_added" +
                        "  FROM user_items ui, items i\n" +
                        " WHERE ui.id = i.id and username = ?");
        stmt.setString(1, username);

        results = stmt.executeQuery();
        return results;
    }

    public static int deleteItem(Long id, String username) throws SQLException{
        DriverManager.registerDriver(new OracleDriver());
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:XE",
                "WishBook", "123");

        PreparedStatement stmt = con.prepareStatement(
                "delete from user_items where username = ? and id = ?");
        stmt.setString(1, username);
        stmt.setLong(2, id);
        return stmt.executeUpdate();
    }

    public static int deleteFriend(String username, String friendName) throws SQLException{
        DriverManager.registerDriver(new OracleDriver());
        Connection con = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:XE",
                "WishBook", "123");

        PreparedStatement stmt = con.prepareStatement(
                "DELETE FROM\n" +
                        "    friends\n" +
                        "      WHERE    (username = ? AND friend_name = ?)\n" +
                        "            OR username = ? AND friend_name = ?\n");
        stmt.setString(1, username);
        stmt.setString(2, friendName);
        stmt.setString(3, friendName);
        stmt.setString(4, username);
        return stmt.executeUpdate();
    }

    public static boolean updateItem(String clientName, String username,
                                  Long itemId, String itemName,
                                  Long contribution,
                                  boolean completed){
        Connection con = null;
        try {
            DriverManager.registerDriver(new OracleDriver());
            con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:XE",
                    "WishBook", "123");

            // Disable auto-commit to start a transaction
            con.setAutoCommit(false);

            // Prepare and execute your first delete statement
            PreparedStatement stmt1 = con.prepareStatement(
                    "update users set balance = balance - ? where username = ?");
            stmt1.setString(2, clientName);
            stmt1.setLong(1, contribution);
            stmt1.executeUpdate();

            PreparedStatement stmt2 = con.prepareStatement(
                    "insert into notifications values (?, 'User ' || ? || ' Contributed by ' || ? || ' To your item: ' || ?)");
            stmt2.setString(1, username);
            stmt2.setString(2, clientName);
            stmt2.setLong(3, contribution);
            stmt2.setString(4, itemName);

            stmt2.executeUpdate();
            if (completed){
                PreparedStatement stmt3 = con.prepareStatement(
                        "insert into notifications values (?, 'Item ' || ? || ' is completed! Go collect it.')");
                stmt3.setString(1, username);
                stmt3.setString(2, itemName);
                stmt3.executeUpdate();

                PreparedStatement stmt4 = con.prepareStatement(
                        "delete from user_items where username = ? and id = ?");
                stmt4.setString(1, username);
                stmt4.setLong(2, itemId);
                stmt4.executeUpdate();
            }
            else {
                PreparedStatement stmt5 = con.prepareStatement(
                        "update user_items set paid = paid + ? where username = ? and id = ?");
                stmt5.setLong(1, contribution);
                stmt5.setString(2, username);
                stmt5.setLong(3, itemId);
                stmt5.executeUpdate();
            }
            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            // Handle exceptions and rollback the transaction if an error occurs
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    return false;
                }
            }
            return false;
        }
    }
}
