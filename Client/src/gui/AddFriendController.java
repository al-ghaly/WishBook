package gui;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;

import client.Client;
import client.ClientSide;
import client.ClientTable;
import client.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class AddFriendController implements Initializable {

    String username;
    @javafx.fxml.FXML
    private TableView<ClientTable> tableAddFriend;
    @javafx.fxml.FXML
    private Button buttonAddFriend;
    @javafx.fxml.FXML
    private TextField friendsearch;

    ArrayList<ClientTable> users = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadUsers();
        List<String> strColumns = Arrays.asList("Username", "email");
        strColumns.forEach(i -> {
            TableColumn<ClientTable, String> column = new TableColumn<>(i);
            column.setCellValueFactory(new PropertyValueFactory<>(i));
            tableAddFriend.getColumns().add(column);
        });
        // Disable the remove button if no items are selected
        buttonAddFriend.disableProperty().bind(
                tableAddFriend.getSelectionModel().selectedItemProperty().isNull());
        fillTable(tableAddFriend);
        friendsearch.textProperty().addListener(
                (observable, oldValue, newValue) -> filterUsers(newValue));

        buttonAddFriend.setOnAction(e -> {
            ClientTable selectedUser = tableAddFriend.getSelectionModel().getSelectedItem();
            String friendName = selectedUser.getUsername();
            if (selectedUser != null) {
                if (showConfirm("Are you sure you want to add  "
                        + friendName + " as a friend?").equals("Ok")) {
                    addFriend(username, friendName);
                }
            }
        });
    }

    private void addFriend(String username, String friendName) {
        try {
            JSONObject logInData = new JSONObject();
            logInData.put("Type", "add friend");
            logInData.put("username", username);
            logInData.put("friend name", friendName);

            // Send the JSON string to the server
            ClientSide.ps.println(logInData);
            ClientSide.ps.flush();

            // Read the server response
            String response = ClientSide.dis.readLine();

            if (response.equals("success")) {
                showAlert("Request sent ^_^!", false);
            }
            else{
                showAlert("An error happened connecting to server!", true);
            }

        } catch (Exception ex) {
            // Provide feedback to the user about the error
            showAlert("An error happened connecting to server!", true);
        }
    }

    private void filterUsers(String newValue) {
        // Filter the friends list based on the entered text
        ObservableList<ClientTable> filteredUsers = FXCollections.observableArrayList();
        for (ClientTable client : users) {
            if (client.getUsername().toLowerCase().contains(newValue.toLowerCase())) {
                filteredUsers.add(client);
            }
        }
        tableAddFriend.setItems(filteredUsers);
    }

    private void fillTable(TableView<ClientTable> tableAddFriend) {
        ObservableList<ClientTable> clients =
                FXCollections.observableArrayList(users);
        tableAddFriend.setItems(clients);
    }

    private void loadUsers() {
        try {
            JSONObject logInData = new JSONObject();
            logInData.put("Type", "load users");

            // Send the JSON string to the server
            ClientSide.ps.println(logInData);
            ClientSide.ps.flush();

            // Read the server response
            String response = ClientSide.dis.readLine();
            // Behave according to the server response
            Object serverResponse = JSONValue.parse(response);
            JSONObject serverMessage = (JSONObject) serverResponse;

            String status
                    = (String) serverMessage.get("Status");
            if (status.equals("success")) {
                JSONArray usersData = (JSONArray) serverMessage.get("usersData");
                for (Object user : usersData) {
                    String[] parts = user.toString().split("--");
                    String username = parts[0];
                    String email = parts[1];
                    ClientTable client = new ClientTable();
                    client.setUsername(username);
                    client.setEmail(email);
                    users.add(client);
                }
            }
            else{
                showAlert("An error happened connecting to server!", true);
            }
        } catch (Exception ex) {
            // Provide feedback to the user about the error
            showAlert("An error happened connecting to server!", true);
        }
    }

    public void setData(String username){
        this.username = username;
    }

    public void showAlert(String message, boolean error) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.setHeaderText(error?"An Error Happened!":"Done!");
        a.setTitle("Error!!");
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
