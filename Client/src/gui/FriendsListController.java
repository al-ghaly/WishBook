package gui;


import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import client.Client;
import client.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

public class FriendsListController implements Initializable {

    @javafx.fxml.FXML
    private Label usernameLbl;
    @javafx.fxml.FXML
    private TextField searchBar;
    @javafx.fxml.FXML
    private Button removeBtn;

    @javafx.fxml.FXML
    private ListView<String> friendsList;
     String username;

    ArrayList<String> friends = new ArrayList<>();
    int port = 4015;
    String ip = "127.0.0.1";


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usernameLbl.setText(username);
        ObservableList<String> friendsNames = FXCollections.observableArrayList(friends);
        friendsList.setItems(friendsNames);
        searchBar.textProperty().addListener(
                (observable, oldValue, newValue) -> filterFriendsList(newValue));
        // Disable the remove button if no items are selected
        removeBtn.disableProperty().bind(
                friendsList.getSelectionModel().selectedItemProperty().isNull());
        // Get The remove Button to Work
        removeBtn.setOnAction(e -> {
            String selectedFriend= friendsList.getSelectionModel().getSelectedItem();
            if (selectedFriend != null) {
                if (showConfirm("Are you sure you want to remove "
                        + selectedFriend).equals("Ok")) {
                    // Remove the selected item from the database
                    String status = deleteFriend(selectedFriend, username);
                    if (status.equals("success")) {
                        friendsList.getItems().remove(selectedFriend);
                        friends.remove(selectedFriend);
                    }
                    else
                        showAlert("Couldn't remove " + selectedFriend + " !");
                }
            }
        });

        // Get the mouse double click to work
        friendsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                // Double-click detected
                int selectedIndex = friendsList.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < friendsList.getItems().size()) {
                    String selectedItem = friendsList.getItems().get(selectedIndex);
                    try {
                        openUserProfile(selectedItem);
                    } catch (Exception ex) {
                        showAlert("An Error Happened");
                    }
                }
            }
        });
    }

    private void openUserProfile(String selectedItem) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/FriendProfile.fxml"));

        // Create an instance of your controller and set the data
        FriendProfileController friendProfileController = new FriendProfileController();
        friendProfileController.setData(selectedItem, username);

        loader.setControllerFactory(clazz -> {
            if (clazz == FriendProfileController.class) {
                return friendProfileController;
            } else {
                try {
                    return clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Stage popupStage = new Stage();
        Parent home = loader.load();
        Scene scene = new Scene(home);
        popupStage.setScene(scene);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    private void filterFriendsList(String searchText) {
        // Filter the friends list based on the entered text
        ObservableList<String> filteredFriends = FXCollections.observableArrayList();
        for (String friend : friends) {
            if (friend.toLowerCase().contains(searchText.toLowerCase())) {
                filteredFriends.add(friend);
            }
        }
        friendsList.setItems(filteredFriends);
    }

    public void setData(ArrayList<String> friends, String username) {
        this.friends = friends;
        this.username = username;
    }

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
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

    public String deleteFriend(String friendName, String username){
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject itemData = new JSONObject();
            itemData.put("Type", "delete friend");
            itemData.put("username", username);
            itemData.put("friend name", friendName);
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
}
