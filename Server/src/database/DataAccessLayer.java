package database;

import java.sql.*;

import oracle.jdbc.OracleDriver;
import utilities.*;

public class DataAccessLayer {
    private static DataAccessLayer instance;
    private Connection connection;

    // Private constructor to prevent instantiation outside the class
    private DataAccessLayer() {
        // Initialize the connection here
        try {
            DriverManager.registerDriver(new OracleDriver());
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:XE",
                    "WishBook", "123");
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    // Method to get the single instance of the class
    public static synchronized DataAccessLayer getInstance() {
        if (instance == null) {
            instance = new DataAccessLayer();
        }
        return instance;
    }

    // Connect method can now be an instance method
    public Connection getConnection() {
        return connection;
    }

    public void addUser(Client client) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "insert into users values(?, ?, ?, ?, ?)");
            stmt.setString(1, client.getUsername());
            stmt.setString(2, client.getPassword());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getPhone());
            stmt.setLong(5, client.getBalance());
            stmt.executeUpdate();
    }

    public String getUser(String username) throws SQLException {
        ResultSet results;
        PreparedStatement stmt = connection.prepareStatement(
                "Select password, balance, email, phone from users where username = ?");
            stmt.setString(1, username);
            results = stmt.executeQuery();
            results.next();
            return results.getString(1) + "-" + results.getLong(2)
                    + "-" + results.getString(3) + "-" + results.getString(4);
    }

    public  ResultSet getFriends(String username) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT friend_name\n" +
                        "  FROM friends\n" +
                        " WHERE username = ?\n" +
                        "UNION\n" +
                        "SELECT username\n" +
                        "  FROM friends\n" +
                        " WHERE friend_name = ?");
        stmt.setString(1, username);
        stmt.setString(2, username);
        return stmt.executeQuery();
    }

    public  ResultSet getWishList(String username) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT ui.id,\n" +
                        "       paid,\n" +
                        "       name,\n" +
                        "       category,\n" +
                        "       price\n" +
                        ", date_added" +
                        "  FROM user_items ui, items i\n" +
                        " WHERE ui.id = i.id and username = ?");
        stmt.setString(1, username);
        return stmt.executeQuery();
    }

    public int deleteItem(Long id, String username) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(
                "delete from user_items where username = ? and id = ?");
        stmt.setString(1, username);
        stmt.setLong(2, id);
        return stmt.executeUpdate();
    }

    public int deleteFriend(String username, String friendName) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(
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

    public boolean updateItem(String clientName, String username,
                                     Long itemId, String itemName,
                                     Long contribution,
                                     boolean completed){
        try {
            // Disable auto-commit to start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute your first delete statement
            PreparedStatement stmt1 = connection.prepareStatement(
                    "update users set balance = balance - ? where username = ?");
            stmt1.setString(2, clientName);
            stmt1.setLong(1, contribution);
            stmt1.executeUpdate();

            PreparedStatement stmt2 = connection.prepareStatement(
                    "insert into notifications values (?, 'User ' || ? || ' Contributed by ' || ? || ' To your item: ' || ?, SYSDATE)");
            stmt2.setString(1, username);
            stmt2.setString(2, clientName);
            stmt2.setLong(3, contribution);
            stmt2.setString(4, itemName);

            stmt2.executeUpdate();
            if (completed){
                PreparedStatement stmt3 = connection.prepareStatement(
                        "insert into notifications values (?, 'Item ' || ? || ' is completed! Go collect it.', SYSDATE)");
                stmt3.setString(1, username);
                stmt3.setString(2, itemName);
                stmt3.executeUpdate();

                PreparedStatement stmt4 = connection.prepareStatement(
                        "delete from user_items where username = ? and id = ?");
                stmt4.setString(1, username);
                stmt4.setLong(2, itemId);
                stmt4.executeUpdate();
            }
            else {
                PreparedStatement stmt5 = connection.prepareStatement(
                        "update user_items set paid = paid + ? where username = ? and id = ?");
                stmt5.setLong(1, contribution);
                stmt5.setString(2, username);
                stmt5.setLong(3, itemId);
                stmt5.executeUpdate();
            }
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            // Handle exceptions and rollback the transaction if an error occurs
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    return false;
                }
            }
            return false;
        }
    }

    public ResultSet getRequests(String username) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement("select username from friend_requests where friend_name = ?");
        stmt.setString(1, username);
        return stmt.executeQuery();
    }


    public void acceptFriend(String username, String friendname) throws SQLException {
        String query1 = "INSERT INTO friends VALUES(?, ?)";

        PreparedStatement pstmt = connection.prepareStatement(query1);
        // Set values for the parameters
        pstmt.setString(1, username);
        pstmt.setString(2, friendname);
        pstmt.executeUpdate();
    }

    public void rejectFriend(String username, String friendname) throws SQLException {
        String query = "DELETE FROM friend_requests WHERE username = ? and friend_name = ?";

        PreparedStatement pstmt = connection.prepareStatement(query);
        // Set values for the parameters
        pstmt.setString(2, username);
        pstmt.setString(1, friendname);
        pstmt.executeUpdate();
    }

    public ResultSet getNotifications(String username) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement("select description from notifications where username = ? order by date_added desc");
        stmt.setString(1, username);
        return stmt.executeQuery();
    }

    public int addItem(Long itemID_, String username) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(
                "insert into user_items values (?, ?, SYSDATE, 0)");
        stmt.setString(1, username);
        stmt.setLong(2, itemID_);
        return stmt.executeUpdate();
    }

    public int addCustomItem(String username, String itemName__,
                                    String itemCat, Long itemPrice) throws SQLException{
        connection.setAutoCommit(false);
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO items (name, category, price) VALUES (?, ?, ?)");
        stmt.setString(1, itemName__);
        stmt.setString(2, itemCat);
        stmt.setLong(3, itemPrice);
        int hasResults = stmt.executeUpdate();
        if (hasResults == 1){
            PreparedStatement stmt2 = connection.prepareStatement(
                    "insert into user_items values (?,ITEM_ID_SEQ.currval, SYSDATE, 0)");
            stmt2.setString(1, username);
            int results2 = stmt2.executeUpdate();
            connection.setAutoCommit(true);
            return results2;
        }
        else
            return -1;
    }

    public int recharge(String username, Long balance) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(
                "update users set balance = balance + ? where username = ?");
        stmt.setString(2, username);
        stmt.setLong(1, balance);
        return stmt.executeUpdate();
    }

    public ResultSet getUsers() throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(
                "select username, email from users");
        return stmt.executeQuery();
    }

    public int sendRequest(String username, String friendName__) throws SQLException{
        PreparedStatement stmt = connection.prepareStatement(
                "insert into FRIEND_REQUESTS values (?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, friendName__);
        return stmt.executeUpdate();
    }
}

























