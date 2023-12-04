package server;

import utilities.*;
import database.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class Server extends Application implements Runnable{
    ServerSocket serverSocket;
    Thread listenThread;
    int port = 4015;
        
    public void listen(){
        listenThread = new Thread(this);
        listenThread.start();
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        ServerUI root = new ServerUI();
        Server server = new Server();
        root.startButton.setOnAction(event ->{
            if(root.startButton.isSelected()){
                root.startButton.setText("Turn Off");
                server.listen();
                root.startButton.setDisable(true);
            }
            else{
               root.startButton.setText("Turn On");
            }
        });
        Scene scene = new Scene(root, 240, 240);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                closeResources();
            }
        });
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void run() {
        try{
        serverSocket = new ServerSocket(port);
        }
        catch(Exception e){
             e.printStackTrace();
        }
        while (true){
            try{      
                Socket socket = serverSocket.accept();
                new Listener(socket);
            }
            catch(Exception e){
                     e.printStackTrace();
            }
        }
    }
    
    private void closeResources() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            stopApplication();
        } catch (Exception e) {
            System.out.println("An Error happened!");
        }
    }
    
     private void stopApplication() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Platform.exit();
                System.exit(0);
            }
        });
     }
}

class Listener extends Thread{
        DataInputStream inputData;
        PrintStream outputData;
        String message;
        String status;
          
    public Listener(Socket socket){
        try{
            inputData = new DataInputStream(socket.getInputStream());
            outputData = new PrintStream(socket.getOutputStream());
            start();
    }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        while (true){
            try{
                message = inputData.readLine();
            
                if (message != null){
                 // Listen to the client
                 Object clientData = JSONValue.parse(message);
                 JSONObject clinetMessage = (JSONObject)clientData;
                 
                 String type
                    = (String)clinetMessage.get("Type");
                    String username
                            = (String)clinetMessage.get("username");

                 switch (type) {
                    case "sign in":
                        status = handleSignIn(username);
                        break;
                    case "sign up":
                        status = handleSignUp(clinetMessage);
                        break;
                    default:
                    break;
}
                 // Respond to the client: according to the status update the user
                 this.outputData.println(status);
                 this.outputData.flush();
                }
            }
            catch(Exception e){
                System.out.println("Error in the Database Server");
            }
        }
    }
    
    public String handleSignIn(String username){
        JSONObject response = new JSONObject();

        try {
            String results = DataAccessLayer.getUser(username);
            response.put("Status", "success");
            String[] parts = results.split("-");
            response.put("password", parts[0]);
            response.put("balance", parts[1]);

            return response.toString();
            } catch (SQLException ex) {
            if(ex.getErrorCode() == 17289) {
                response.put("Status", "username not found");
                return response.toString();
            }
            else {
                response.put("Status",  "An Error Happened");
                return response.toString();
            }
            }
  }
    
    public String handleSignUp(JSONObject message){
        Client client = new Client();

        String username
            = (String)message.get("username");
        String password
            = (String)message.get("password");
        String email
            = (String)message.get("email");
        String phone
            = (String)message.get("phone");
        Long balance
            = (Long)message.get("balance");

        client.setUsername(username);
        client.setPassword(password);
        client.setEmail(email);
        client.setPhone(phone);
        client.setBalance(balance);

            try {
                DataAccessLayer.addUser(client);
                return "success";
            } catch (SQLException ex) {
                if(ex.getErrorCode() == 1)
                        return "duplicate username";
                else
                    return "error";
            }
    }
}
