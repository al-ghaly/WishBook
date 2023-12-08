package gui;

import client.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AddItemController
{
    @javafx.fxml.FXML
    private TextField searchBar;
    @javafx.fxml.FXML
    private TableView<Item> market;
    @javafx.fxml.FXML
    private TextField nameTxt;
    @javafx.fxml.FXML
    private TextField catTxt;
    @javafx.fxml.FXML
    private TextField priceTxt;
    @javafx.fxml.FXML
    private Button addCutomBtn;
    @javafx.fxml.FXML
    private Button addMarketBtn;

    ObservableList<Item> items = FXCollections.observableArrayList();;
    String username;
    JSONArray wishItems = new JSONArray();
    int port = 4015;
    String ip = "127.0.0.1";

    @javafx.fxml.FXML
    public void initialize() {
        List<String> strColumns = Arrays.asList("Name",
                "Category");
        strColumns.forEach(i -> {
            TableColumn<Item, String> column = new TableColumn<>(i);
            column.setCellValueFactory(new PropertyValueFactory<>(i));
            market.getColumns().add(column);
        });
            TableColumn<Item, Integer> column = new TableColumn<>("Price");
            column.setCellValueFactory(new PropertyValueFactory<>("Price"));
            market.getColumns().add(column);

        // Disable the remove button if no items are selected
        addMarketBtn.disableProperty().bind(
                market.getSelectionModel().selectedItemProperty().isNull());

        fillTable(market);
        searchBar.textProperty().addListener(
                (observable, oldValue, newValue) -> filterWishList(newValue));

        // Get The add from market Button to Work
        addMarketBtn.setOnAction(e -> {
            Item selectedItem = market.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (showConfirm("Are you sure you want to add this item "
                        + selectedItem.getName()).equals("Ok")) {
                    addItem(selectedItem);
                }
            }
        });

        addCutomBtn.setOnAction(e -> {
            String name = nameTxt.getText().trim();
            String cat = catTxt.getText().trim();
            String price = priceTxt.getText().trim();
            if (validate(name, cat, price)){
                if (showConfirm("Are you sure you want to add this item "
                        + name).equals("Ok")) {
                    Long itemPrice
                            = Long.parseLong(price);
                    addCustomItem(username, name, cat, itemPrice);
                }
            }
        });
    }

    private void addCustomItem(String username, String name, String cat, Long price) {
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "add custom item");
            signUpData.put("username", username);
            signUpData.put("name", name);
            signUpData.put("cat", cat);
            signUpData.put("price", price);

            // Send the JSON string to the server
            ps.println(signUpData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            // Behave according to the server response
            dis.close();
            ps.close();
            // Close the socket
            socket.close();
            if (response.equals("success")) {
                showAlert("Item Added!", false);
            } else
                showAlert("An Error Happened!", true);
        } catch (Exception ex) {
            showAlert("An Error Happened!", true);
        }

    }

    private boolean validate(String name, String cat, String price) {
        if(name.isEmpty() || cat.isEmpty() || price.isEmpty()){
            showAlert("Enter all required data!", true);
            return false;
        }
        else {
            try{
                Long.parseLong(price);
                return true;
            }
            catch (Exception e){
                showAlert("Enter a valid price!", true);
                return false;
            }
        }
    }

    private void addItem(Item selectedItem) {
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "add item");
            signUpData.put("username", username);
            signUpData.put("id", selectedItem.getId());

            // Send the JSON string to the server
            ps.println(signUpData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            dis.close();
            ps.close();
            // Close the socket
            socket.close();
            if (response.equals("success")) {
                showAlert("Item Added!", false);
            } else
                showAlert("An Error Happened!", true);
        } catch (Exception ex) {
            showAlert("An Error Happened!", true);
        }
    }

    private void filterWishList(String searchText) {
        // Filter the friends list based on the entered text
        ObservableList<Item> filteredItems = FXCollections.observableArrayList();
        for (Item item : items) {
            if (item.getName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        market.setItems(filteredItems);
    }

    public void fillTable(TableView<Item> tableView) {
        if (getWishList().equals("success")) {
            for (Object itemData : wishItems) {
                Item item = new Item();
                String[] data = itemData.toString().split("--");
                int paid = Integer.parseInt(data[4]);
                int price = Integer.parseInt(data[3]);
                item.setId(Integer.parseInt(data[0]));
                item.setPrice(price);
                item.setPaid(paid);
                item.setName(data[1]);
                item.setCategory(data[2]);
                item.setUsername(username);
                item.setDate(data[5]);
                item.setRemaining(price - paid);
                items.add(item);            }
            tableView.setItems(items);
        }
        else
            showAlert("An Error happened loading market Items!", true);
    }

    public String getWishList() {
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "wishlist");
            signUpData.put("username", "SYSTEM");
            // Send the JSON string to the server
            ps.println(signUpData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            JSONObject serverMessage = (JSONObject) serverResponse;
            String status
                    = (String) serverMessage.get("Status");
            dis.close();
            ps.close();
            // Close the socket
            socket.close();
            if (status.equals("success")) {
                wishItems = (JSONArray) serverMessage.get("wishes");
                return "success";
            } else
                return "failed";

        } catch (Exception ex) {
            return "failed";
        }
    }

    public void setData(String username){
        this.username = username;
    }

    public void showAlert(String message, boolean error) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText(message);
            a.setHeaderText(error?"An Error Happened!":"Done");
            a.setTitle("Error");
            a.show();
        }

    public String showConfirm(String message) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setContentText(message);
            a.setHeaderText("Action is irreversible!");
            a.setTitle("Confirm");

            Optional<ButtonType> result = a.showAndWait();
            // Check the user's choice
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // User clicked OK
                return "Ok";
            } else {
                return "Cancel";
            }
        }
}
