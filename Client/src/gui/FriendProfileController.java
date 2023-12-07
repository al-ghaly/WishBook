package gui;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import client.Client;
import client.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class FriendProfileController implements Initializable {

    @FXML
    private Label usernameTxt;
    @FXML
    private Label emailTxt;
    @FXML
    private Label phoneTxt;
    @FXML
    private Label balanceTxt;
    @FXML
    private Button contBtn;
    @FXML
    private TextField valueTxt;
    @FXML
    private TableView<Item> wishList;

    String username;
    Client client = new Client();
    JSONArray wishItems = new JSONArray();

    int port = 4015;
    String ip = "127.0.0.1";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usernameTxt.setText(username);
        String status = getClientData(username);
        if(status.equals("success")){
            balanceTxt.setText(client.getBalance() + " $");
            phoneTxt.setText(client.getPhone());
            emailTxt.setText(client.getEmail());
            setUpWishList();
        }
        else
            showAlert("An Error happened trying to load " +
                    username + "'s profile!");

        // Disable the contribute button if no items are selected
        contBtn.disableProperty().bind(
                wishList.getSelectionModel().selectedItemProperty().isNull());
        valueTxt.disableProperty().bind(
                wishList.getSelectionModel().selectedItemProperty().isNull());
        // Get The remove Button to Work
//        contBtn.setOnAction(e -> {
//            Item selectedItem = wishList.getSelectionModel().getSelectedItem();
//            if (selectedItem != null) {
//                if (showConfirm("Are you sure you want to remove "
//                        + selectedItem.getName()).equals("Ok")) {
//                    // Remove the selected item from the database
//                    String status = deleteItem(selectedItem.getId(),
//                            client.getUsername());
//                    if (status.equals("success"))
//                        wishList.getItems().remove(selectedItem);
//                    else
//                        showAlert("The item " + selectedItem.getName() + " is not deleted");
//                }
//            }
//        });

    }

    private void setUpWishList() {
        List<String> strColumns = Arrays.asList("Username", "Name",
                "Category", "Date");
        strColumns.forEach(i -> {
            TableColumn<Item, String> column = new TableColumn<>(i);
            column.setCellValueFactory(new PropertyValueFactory<>(i));
            wishList.getColumns().add(column);
        });
        List<String> intColumns = Arrays.asList("Price", "Paid", "Remaining");
        intColumns.forEach(i -> {
            TableColumn<Item, Integer> column = new TableColumn<>(i);
            column.setCellValueFactory(new PropertyValueFactory<>(i));
            wishList.getColumns().add(column);
        });
        TableColumn<Item, Integer> column = new TableColumn<>("Id");
        column.setCellValueFactory(new PropertyValueFactory<>("Id"));
        wishList.getColumns().add(1, column);

        ObservableList<Item> items =
                FXCollections.observableArrayList();
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
            items.add(item);
        }
        wishList.setItems(items);
    }

    private String getClientData(String username) {
        try {
            Socket socket = new Socket(ip, port);

            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());
            JSONObject logInData = new JSONObject();
            logInData.put("Type", "friend profile");
            logInData.put("username", username);

            // Send the JSON string to the server
            ps.println(logInData);
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

            if(status.equals("success")) {
                String balance = (String) serverMessage.get("balance");
                client.setBalance(Long.parseLong(balance));
                String email = (String) serverMessage.get("email");
                client.setEmail(email);
                String phone = (String) serverMessage.get("phone");
                client.setPhone(phone);
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

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
        a.setTitle("Error");
        a.show();
    }

    public void setData(String username) {
        this.username = username;
    }
}
