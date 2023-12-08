package gui;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import client.Client;
import client.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class UserProfileController implements Initializable {

    @FXML
    private Label usernameTxt;
    @FXML
    private Label emailTxt;
    @FXML
    private Label phoneTxt;
    @FXML
    private Label balanceTxt;
    @FXML
    private Button removeBtn;
    @FXML
    private TableView<Item> wishList;
    @FXML
    private TextField value;
    @FXML
    private Button rechargeBtn;

    Client client;
    JSONArray wishItems = new JSONArray();

    int port = 4015;
    String ip = "127.0.0.1";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usernameTxt.setText(client.getUsername());
        balanceTxt.setText(client.getBalance() + " $");
        phoneTxt.setText(client.getPhone());
        emailTxt.setText(client.getEmail());

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

        // Disable the remove button if no items are selected
        removeBtn.disableProperty().bind(
                wishList.getSelectionModel().selectedItemProperty().isNull());
        // Fill the table
        fillTable(wishList, client.getUsername());
        // Get The remove Button to Work
        removeBtn.setOnAction(e -> {
            Item selectedItem = wishList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (showConfirm("Are you sure you want to remove "
                        + selectedItem.getName()).equals("Ok")) {
                    // Remove the selected item from the database
                    String status = deleteItem(selectedItem.getId(),
                            client.getUsername());
                    if (status.equals("success"))
                        wishList.getItems().remove(selectedItem);
                    else
                        showAlert("The item " + selectedItem.getName() + " is not deleted", true);
                }
            }
        });
        rechargeBtn.setOnAction(e -> {
            try {
                Long balance = Long.parseLong(value.getText());
                if (showConfirm("Are you sure you want to recharge your balance with  "
                        + balance + " $").equals("Ok")) {
                    recharge(client.getUsername(), balance);
                }
            }
            catch (Exception ex){
                showAlert("Enter a valid balance Number!", true);
            }
        });
    }

    private void recharge(String username, Long balance) {
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "recharge");
            signUpData.put("username", username);
            signUpData.put("balance", balance);
            // Send the JSON string to the server
            ps.println(signUpData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            dis.close();
            ps.close();
            // Close the socket
            socket.close();
            if (response.equals("success")) {
                client.setBalance(client.getBalance() + balance);
                balanceTxt.setText(client.getBalance() + " $");
                showAlert("Done", false);
            } else
                showAlert("An Error Happened!", true);

        } catch (Exception ex) {
            showAlert("An Error Happened!", true);
        }
    }

    public void fillTable(TableView<Item> tableView, String username) {
        if (getWishList(username).equals("success")) {
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
            tableView.setItems(items);
        }
    }

    public String getWishList(String username) {
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

    public String deleteItem(int id, String username){
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject itemData = new JSONObject();
            itemData.put("Type", "delete item");
            itemData.put("username", username);
            itemData.put("id", id);
            // Send the JSON string to the server
            ps.println(itemData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            // Behave according to the server response
            dis.close();
            ps.close();
            // Close the socket
            socket.close();
           return response;
        } catch (Exception ex) {
            return "failed";
        }
    }

    public void setData(Client client) {
        this.client = client;
    }

    public void showAlert(String message, boolean error) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(message);
        a.setHeaderText(error?"An Error Happened!":"Done!");
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
