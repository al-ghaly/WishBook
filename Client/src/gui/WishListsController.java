package gui;

import client.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class WishListsController implements Initializable {

    @FXML
    private VBox tablesContainer;
    ArrayList<String> usernamesList = new ArrayList<>();
    JSONArray wishItems = new JSONArray();

    int port = 4015;
    String ip = "127.0.0.1";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Create a wishlist table for each username
        for (String username : usernamesList) {
            TableView<Item> wishlist = createTableView(username);
            tablesContainer.getChildren().add(wishlist);
        }
    }

    private TableView<Item> createTableView(String username) {
        // Create the empty table
        TableView<Item> tableView = new TableView<>();

        List<String> strColumns = Arrays.asList("Username", "Name",
                "Category", "Date");
        strColumns.forEach(i -> {
            TableColumn<Item, String> column = new TableColumn<>(i);
            column.setCellValueFactory(new PropertyValueFactory<>(i));
            tableView.getColumns().add(column);
        });
        List<String> intColumns = Arrays.asList("Price", "Paid");
        intColumns.forEach(i -> {
            TableColumn<Item, Integer> column = new TableColumn<>(i);
            column.setCellValueFactory(new PropertyValueFactory<>(i));
            tableView.getColumns().add(column);
        });
        TableColumn<Item, Integer> column = new TableColumn<>("ID");
        column.setCellValueFactory(new PropertyValueFactory<>("ID"));
        tableView.getColumns().add(1, column);

        // Fill the table
        fillTable(tableView, username);
        return tableView;
    }

    public void fillTable(TableView<Item> tableView, String username){
        if(getWishList(username).equals("success"))
            // TODO: Fill the observable list from the json array
            System.out.println("Success" + wishItems);
        //else
            //showAlert("An Error Happened loading " + username +" Wishes!");
//        Item item = new Item();
//        item.setId(10);
//        item.setPrice(100);
//        item.setPaid(50);
//        item.setCategory("cat");
//        item.setName("Item");
//        item.setUsername("Alghaly");
//        item.setDate("11/11/2011");
//        ObservableList<Item> items =
//                FXCollections.observableArrayList(item);
//        tableView.setItems(items);
    }

    public String getWishList(String username){
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "wishlist");
            signUpData.put("username", username);
            // Send the JSON string to the server
            ps.println(signUpData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            System.out.println(response);
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            JSONObject serverMessage = (JSONObject) serverResponse;
            String status
                    = (String) serverMessage.get("Status");

            dis.close();
            ps.close();
            // Close the socket
            socket.close();

            if(status.equals("success")){
                wishItems = (JSONArray) serverMessage.get("wishes");
                return "success";
            }
            else
                return "failed";

        } catch (Exception ex) {
            ex.printStackTrace();
            return "failed";
        }
    }

    public void setData(ArrayList<String> usernamesList){
        this.usernamesList = usernamesList;
    }

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
        a.setTitle("Error!!");
        a.show();
    }
}
