package gui;

import client.Client;
import client.ClientSide;
import client.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //tablesContainer.setMaxWidth(250);
        // Create a wishlist table for each username
        if(usernamesList.size() == 0){
            Label header = new Label("You are So lonely\nMake new Friends");
            // Set center alignment
            header.setAlignment(Pos.CENTER);
            // Optionally, you can set the label to stretch its width
            header.setMaxWidth(Double.MAX_VALUE);
            header.setMaxHeight(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            header.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");
            tablesContainer.getChildren().add(header);
        }
        for (String username : usernamesList) {
            Label header = new Label(username);
            // Set center alignment
            header.setAlignment(Pos.CENTER);
            // Optionally, you can set the label to stretch its width
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            tablesContainer.getChildren().add(header);
            TableView<Item> wishlist = createTableView(username);
            tablesContainer.getChildren().add(wishlist);
        }
    }

    private TableView<Item> createTableView(String username) {
        // Create the empty table
        TableView<Item> tableView = new TableView<>();

        List<String> strColumns = Arrays.asList("Name",
                "Category", "Date");
        strColumns.forEach(i -> {
            TableColumn<Item, String> column = new TableColumn<>(i);
            column.setCellValueFactory(new PropertyValueFactory<>(i));
            tableView.getColumns().add(column);
        });
        List<String> intColumns = Arrays.asList("Price", "Paid", "Remaining");
        intColumns.forEach(i -> {
            TableColumn<Item, Integer> column = new TableColumn<>(i);
            column.setCellValueFactory(new PropertyValueFactory<>(i));
            tableView.getColumns().add(column);
        });
        TableColumn<Item, Integer> column = new TableColumn<>("Item Id");
        column.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tableView.getColumns().add(0, column);

        // Fill the table
        fillTable(tableView, username);
        return tableView;
    }

    public void fillTable(TableView<Item> tableView, String username){
        if(getWishList(username).equals("success")) {
            ObservableList<Item> items =
                    FXCollections.observableArrayList();
            for (Object itemData : wishItems) {
                Item item = new Item();
                String[] data = itemData.toString().split("--");
                item.setId(Integer.parseInt(data[0]));
                item.setPrice(Integer.parseInt(data[3]));
                item.setPaid(Integer.parseInt(data[4]));
                item.setName(data[1]);
                item.setCategory(data[2]);
                item.setUsername(username);
                item.setDate(data[5]);
                item.setRemaining(item.getPrice() - item.getPaid());
                items.add(item);
            }
            tableView.setItems(items);
        }
    }

    public String getWishList(String username){
        try {
            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "wishlist");
            signUpData.put("username", username);
            // Send the JSON string to the server
            ClientSide.ps.println(signUpData);
            ClientSide.ps.flush();

            // Read the server response
            String response = ClientSide.dis.readLine();
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            JSONObject serverMessage = (JSONObject) serverResponse;
            String status
                    = (String) serverMessage.get("Status");
            if(status.equals("success")){
                wishItems = (JSONArray) serverMessage.get("wishes");
                return "success";
            }
            else
                return "failed";

        } catch (Exception ex) {
            return "failed";
        }
    }

    public void setData(ArrayList<String> usernamesList){
        this.usernamesList = usernamesList;
    }
}
