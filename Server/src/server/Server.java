package server;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
       tempUI root = new tempUI();
       Server server = new Server();
        root.startButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                server.listen();
                }
        });
        Scene scene = new Scene(root);
        
       stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                closeResources();
            }
        });

        
        stage.setScene(scene);
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
            e.printStackTrace();
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
                 System.out.println(message);
                 Object clientData = JSONValue.parse(message);
                 JSONObject clinetMessage = (JSONObject)clientData;
                 
                 String type
                    = (String)clinetMessage.get("Type");
                 
                 boolean status;
                 switch (type) {
                    case "sign in":
                        status = handleSignIn();
                    break;
                    case "sign up":
                        status = handleSignUp();
                    break;
                    // additional cases as needed
                    default:
                    break;
}
                 // Respond to the server: according to the status update the user
                 this.outputData.println(message + " From Server!");
                 this.outputData.flush(); // Added1
                }
            }
        
            catch(Exception e){
            e.printStackTrace();
            }
    }
    }
    
    public boolean handleSignIn(){
        return true;
    }
    
    public boolean handleSignUp(){
        return true;
    }    
}