package gui;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import client.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class HomeController implements Initializable {

    @FXML
    private Button profileButton;
    @FXML
    private Button addItemButton;
    @FXML
    private Button addFriendButton;
    @FXML
    private Button friendListButton;
    @FXML
    private Button notificationButton;
    @FXML
    private Button logOutButton;
    @FXML
    private Button aboutButton;
    @FXML
    private Label usernameTxt;
    @FXML
    private Label balanceTxt;

    Client client;
    @FXML
    private Button requestButton;
    @FXML
    private GridPane homeContent;

    ArrayList<String> friendsList = new ArrayList<>();
    // success or failed depending on whether or not we retrieved the user's friends list successfully
    String listStatus;
    int port = 4015;
    String ip = "127.0.0.1";


    public void setData(Client client) {
        this.client = client;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
       usernameTxt.setText(client.getUsername());
       balanceTxt.setText(client.getBalance() + " $");

       aboutButton.setOnAction(e -> {
          popUpAbout();
       });

        logOutButton.setOnAction(e -> {
            try {
                switchToLogIn(e);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("An Error Happened");
            }
        });
        //TODO: This should happen in a separate thread
        listStatus = getFriends(client.getUsername());

        //TODO: Remove the comment (We don't need to bother show the home page during test!!)
        // Load the home page content
//        try {
//            // Create the home page content.
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("./WishLists.fxml"));
//            // Create an instance of your controller and set the data
//            WishListsController wishListsController = new WishListsController();
//            // Get the user's friends list here and pass an arraylist of usernames to the custom page
//            if(listStatus.equals("success"))
//                wishListsController.setData(friendsList);
//            else
//                showAlert("An Error Happened loading your Home Page");
//
//            loader.setControllerFactory(clazz -> {
//                if (clazz == WishListsController.class) {
//                    return wishListsController;
//                } else {
//                    try {
//                        return clazz.newInstance();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
//            Parent testPage = loader.load();
//            // Add the loaded page to the homeContent grid
//            homeContent.getChildren().add(testPage);
//
//        } catch (IOException e) {
//            showAlert("An Error Happened Loading your Home Page!!");
//        }

        profileButton.setOnAction(e -> {
            try {
                switchToUserProfile();
            } catch (Exception ex) {
                showAlert("An Error Happened");
            }
        });

        friendListButton.setOnAction(e -> {
            try {
                switchToFriendsList();
            } catch (Exception ex) {
                showAlert("An Error Happened");
            }
        });

        addItemButton.setOnAction(e -> {
            try {
                switchToAddItem();
            } catch (Exception ex) {
                showAlert("An Error Happened");
            }
        });
        requestButton.setOnAction(e -> {
            try {
                switchToRequests();
            } catch (Exception ex) {
                showAlert("An Error Happened");
            }
        });

        addFriendButton.setOnAction(e -> {
            try {
                switchToAddFriend();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("An Error Happened");
            }
        });

        notificationButton.setOnAction(e -> {
            try {
                switchToNotifications();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("An Error Happened");
            }
        });
    }

    public String getFriends(String username){
        try {
            Socket socket = new Socket(ip, port);
            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());

            // Send data to the server
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "friends list");
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

            if(status.equals("success")){
                JSONArray friends = (JSONArray) serverMessage.get("friends");
                for (Object friend : friends) {
                    friendsList.add((String) friend);
                }
                return "success";
            }
            else
                return "failed";

        } catch (Exception ex) {
            return "failed";
        }
    }

    public void popUpAbout(){
        Stage popupStage = new Stage();
        About root = new About();
        Scene aboutScene = new Scene(root);
        popupStage.setScene(aboutScene);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    public void switchToLogIn(ActionEvent event) throws Exception{
        Parent logIn = FXMLLoader.load(getClass().getResource("../gui/LogIn.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(logIn);
        stage.setScene(scene);
        stage.show();
    }

    public void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(message);
        a.setHeaderText("An Error Happened!");
        a.setTitle("Error!!");
        a.show();
    }

    public void switchToUserProfile() throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/UserProfile.fxml"));

        // Create an instance of your controller and set the data
        UserProfileController profileController = new UserProfileController();
        profileController.setData(client);

        loader.setControllerFactory(clazz -> {
            if (clazz == UserProfileController.class) {
                return profileController;
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

    public void switchToFriendsList() throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/FriendsList.fxml"));

        // Create an instance of your controller and set the data
        FriendsListController friendsListController = new FriendsListController();
        friendsListController.setData(friendsList, client.getUsername());

        loader.setControllerFactory(clazz -> {
            if (clazz == FriendsListController.class) {
                return friendsListController;
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

    public void switchToAddItem() throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/AddItem.fxml"));

        // Create an instance of your controller and set the data
        AddItemController addItemController = new AddItemController();
        addItemController.setData(client.getUsername());

        loader.setControllerFactory(clazz -> {
            if (clazz == AddItemController.class) {
                return addItemController;
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

    public void switchToRequests() throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/FriendsRequest.fxml"));

        // Create an instance of your controller and set the data
        FriendsRequestController friendsRequestController = new FriendsRequestController();
        friendsRequestController.setData(client.getUsername(), friendsList);

        loader.setControllerFactory(clazz -> {
            if (clazz == FriendsRequestController.class) {
                return friendsRequestController;
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

    public void switchToAddFriend() throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/AddFriend.fxml"));

        // Create an instance of your controller and set the data
        AddFriendController addFriendController = new AddFriendController();
        addFriendController.setData(client.getUsername());

        loader.setControllerFactory(clazz -> {
            if (clazz == AddFriendController.class) {
                return addFriendController;
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

    public void switchToNotifications() throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../gui/Notifications.fxml"));

        // Create an instance of your controller and set the data
        NotificationsController notificationsController = new NotificationsController();
        notificationsController.setData(client.getUsername());

        loader.setControllerFactory(clazz -> {
            if (clazz == NotificationsController.class) {
                return notificationsController;
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
}
