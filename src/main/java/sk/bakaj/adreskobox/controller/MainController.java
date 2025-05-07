package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import sk.bakaj.adreskobox.model.ImportedData;
import sk.bakaj.adreskobox.service.FileService;

import java.io.File;
import java.util.List;

public class MainController
{
    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private PrintPreviewController printPreviewController;

    private List<ImportedData> importedData;
    private FileService fileService = new FileService();

    @FXML
    private void initialize()
    {
        //nastavenie listenerov pre tlačidlá
        prevButton.setOnAction(event -> navigateToPreviousTab());
        nextButton.setOnAction(event -> navigateToNextTab());

        //Aktualizácia stavu tlačidiel na začiatku
        updateButtonStates();

        //Listener pre zmenu záložky
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> updateButtonStates());
    }

    public void navigateToPreviousTab()
    {
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0)
        {
            tabPane.getSelectionModel().select(currentIndex - 1);
        }
    }

    public void navigateToNextTab()
    {
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();

        //Pokiaľ je na tabulatore "import dát", je potrebné spracovať dáta
        if (currentIndex == 0)
        {
            File selecteFile = importController.getSelectedFile();
            String detectedDelimiter = importController.getDetectedDelimiter();
            if (selecteFile == null || detectedDelimiter == null)
            {
                showAlert(Alert.AlertType.WARNING, "Chýba súbor",
                        "Pred prechodom na dalšiu kartu výberte súbor.");
                return;
            }

            try
            {
                importedData = fileService.readFile(selecteFile, detectedDelimiter);
            }
            catch (Exception e)
            {
                showAlert(Alert.AlertType.ERROR, "Chyba načitania",
                         "Nepodarilo sa načítať data: " + e.getMessage());
                return;
            }
        }
        if (currentIndex < tabPane.getTabs().size() - 1)
        {
            tabPane.getSelectionModel().select(currentIndex + 1);
        }
    }

    public void updateButtonStates()
    {
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        prevButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == tabPane.getTabs().size() - 1);
    }

    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
