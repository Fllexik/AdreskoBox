package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;

public class MainController
{
    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private void initialize() {
        //nastavvenie listenerov pre tlačidlá
        prevButton.setOnAction(event -> navigateToPreviousTab());
        nextButton.setOnAction(event -> navigateToNextTab());

        //Aktualizácia stavu tlačidiel na začiatku
        updateButtonStates();

        //Listener pre zmenu záložky
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> updateButtonStates());
    }

    public void navigateToPreviousTab(){
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0){
            tabPane.getSelectionModel().select(currentIndex -1);
        }
    }

    public void navigateToNextTab(){
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (currentIndex < tabPane.getTabs().size() -1) {
            tabPane.getSelectionModel().select(currentIndex + 1);
        }
    }

    public void updateButtonStates(){
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        prevButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == tabPane.getTabs().size() - 1);
    }
}
