package client;

import gui.LogInBase;
import gui.SignUpBase;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

public class ClientSide extends Application {
    Socket mySocket ;
    DataInputStream dis ;
    PrintStream ps;
    int port = 4015;
    String ip = "127.0.0.1";
    
    @Override
    public void start(Stage stage) throws Exception {
        LogInBase root = new LogInBase();
        root.loginButton.setOnAction(e -> {
            // Get username and password
            String username = root.usernameTxt.getText();
            String password = root.passwordTxt.getText();
            root.usernameTxt.clear();
            root.passwordTxt.clear();
            
            Client client = new Client();
            client.setUsername(username);
            client.setPassword(password);

            // Perform login asynchronously in a separate thread
            Thread loginThread = new Thread(() -> handleLogin(client));
            loginThread.start();
        });
        
        root.signUpButton.setOnAction(e -> {

            // Redirect the user to the signup page.
            Scene newScene = signUpScene();

            // Set the new scene on the stage
            stage.setScene(newScene);
        });
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
    }

    private Scene signUpScene(){
        SignUpBase root = new SignUpBase();
        
        root.signUpButton.setOnAction(e -> {
            // Get username and password
            String username = root.usernameTxt.getText();
            String password = root.passwordTxt.getText();
            String conPass = root.conPasswordTxt.getText();
            String email = root.emailTxt.getText();
            String phone = root.phoneTxt.getText();
            int balance = Integer.parseInt(root.balanceTxt.getText());
            root.usernameTxt.clear();
            root.passwordTxt.clear();
            root.conPasswordTxt.clear();
            root.emailTxt.clear();
            root.phoneTxt.clear();
            root.balanceTxt.clear();
            
            Client client = new Client();
            client.setBalance(balance);
            client.setEmail(email);
            client.setPassword(password);
            client.setUsername(username);
            client.setPhone(phone);

            // Perform login asynchronously in a separate thread
            Thread  signUpThread = new Thread(() -> handleSignUp(client));
            signUpThread.start();
        });
        
        Scene signUpScene = new Scene(root);
        return signUpScene;
    }
    
    public static void main(String[] args) {
        launch(args);
    }  
    
        // Handle login action
    private void handleLogin(Client client) {
        try {
            // Replace the IP address and port with your server's IP and port
            Socket socket = new Socket(ip, port);

            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());
            JSONObject logInData = new JSONObject();
            logInData.put("Type", "sign in");
            logInData.put("username", client.getUsername());
            logInData.put("password", client.getPassword());

            // Send the JSON string to the server
            ps.println(logInData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            System.out.println(response);
            
            // Process the response (you can customize this part)
            System.out.println("Server Response: " + response);
            dis.close();
            ps.close(); 
            // Close the socket
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Provide feedback to the user about the error
            System.out.println("An error occurred during login.");
        }
    }
    
     private void handleSignUp(Client client) {
        try {
            // Replace the IP address and port with your server's IP and port
            Socket socket = new Socket(ip, port);

            // Create data input and output streams
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            PrintStream ps = new PrintStream(socket.getOutputStream());
            
            JSONObject signUpData = new JSONObject();
            signUpData.put("Type", "sign up");
            signUpData.put("username", client.getUsername());
            signUpData.put("password", client.getPassword());
            signUpData.put("email", client.getEmail());
            signUpData.put("phone", client.getPhone()); 
            signUpData.put("balance", new Integer(client.getBalance()));
            // Send the JSON string to the server
            ps.println(signUpData);
            ps.flush();

            // Read the server response
            String response = dis.readLine();
            System.out.println(response);
            
            // Process the response (you can customize this part)
            System.out.println("Server Response: " + response);
            dis.close();
            ps.close(); 
            // Close the socket
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Provide feedback to the user about the error
            System.out.println("An error occurred during login.");
        }
    }
}
