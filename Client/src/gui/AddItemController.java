package gui;

import client.Item;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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

    String username;

    @javafx.fxml.FXML
    public void initialize() {
    }

    public void setData(String username){
        this.username = username;
    }
}