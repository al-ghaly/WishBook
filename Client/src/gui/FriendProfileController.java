package gui;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;

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
    String clientName;
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
        contBtn.setOnAction(e -> {
            Item selectedItem = wishList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (validate(client.getBalance(), selectedItem.getRemaining())){
                    int contribution = Integer.parseInt(valueTxt.getText());
                    String message = contribution
                            == selectedItem.getRemaining()?"complete":"contribute";
                    boolean updated =
                    contribute(clientName, username, selectedItem.getId(), selectedItem.getName(),
                            (long)contribution, message);
                    if(updated){
                        selectedItem.setPaid(selectedItem.getPaid() + contribution);
                        selectedItem.setRemaining(selectedItem.getRemaining() - contribution);
                        wishList.refresh();
                    }
                    else {
                        showAlert("An Error Happened");
                    }
                }
            }
        });
    }

    public boolean contribute(String clientName, String username, int id, String itemName,
                              Long contribution, String message) {
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", message);
            signUpData.put("client name", clientName);
            signUpData.put("username", username);
            signUpData.put("item name", itemName);
            signUpData.put("id", id);
            signUpData.put("contribution", contribution);
            // Send the JSON string to the server
            ps.println(signUpData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            dis.close();
            ps.close();
            // Close the socket
            socket.close();
            return response.equals("success");
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean validate(Long balance, int remaining) {
        try{
            String contributionTxt = valueTxt.getText();
            Long contribution = Long.parseLong(contributionTxt);
            if(contribution > balance){
                showAlert("You don't have enough balance!");
                return false;
            }
            else if (contribution > remaining){
                showAlert("You are giving too much money!");
                return false;
            }
            else
                return showConfirm("You are about to give: " + contributionTxt +
                        " $ To: " + username +
                        "\nAre you sure you want to contribute?").equals("Ok");
        }
        catch(Exception e){
            showAlert("Enter a Valid Contribution value!");
            return false;
        }
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

    public void setData(String username, String clientName) {
        this.username = username;
        this.clientName = clientName;
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
