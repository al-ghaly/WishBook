package server;

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;

public class ServerUI extends GridPane {

    protected final ColumnConstraints columnConstraints;
    protected final RowConstraints rowConstraints;
    protected final ToggleButton startButton;

    public ServerUI() {

        columnConstraints = new ColumnConstraints();
        rowConstraints = new RowConstraints();
        startButton = new ToggleButton();

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(250.0);
        setPrefWidth(250.0);

        columnConstraints.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        columnConstraints.setMinWidth(10.0);
        columnConstraints.setPrefWidth(100.0);

        rowConstraints.setMinHeight(10.0);
        rowConstraints.setPrefHeight(30.0);
        rowConstraints.setVgrow(javafx.scene.layout.Priority.SOMETIMES);

        GridPane.setHgrow(startButton, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setVgrow(startButton, javafx.scene.layout.Priority.ALWAYS);
        startButton.setMinHeight(USE_PREF_SIZE);
        startButton.setMinWidth(USE_PREF_SIZE);
        startButton.setMnemonicParsing(false);
        startButton.setPrefHeight(250.0);
        startButton.setPrefWidth(250.0);
        startButton.setText("Turn On");
        startButton.setTextFill(javafx.scene.paint.Color.valueOf("#16ae3f"));
        startButton.setFont(new Font("System Bold", 36.0));

        getColumnConstraints().add(columnConstraints);
        getRowConstraints().add(rowConstraints);
        getChildren().add(startButton);

    }
}
