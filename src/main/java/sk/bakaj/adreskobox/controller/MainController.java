package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import sk.bakaj.adreskobox.model.ImportedData;
import sk.bakaj.adreskobox.model.Parent;
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
    private ImportController importController;

    @FXML
    private ParentsTabController parentsTabController;

    private List<ImportedData> importedData;
    private List<Parent> selectedParents;
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
            File selectedFile = importController.getSelectedFile();
            String detectedDelimiter = importController.getDetectedDelimiter();
            if (selectedFile == null || detectedDelimiter == null)
            {
                showAlert(Alert.AlertType.WARNING, "Chýba súbor",
                        "Pred prechodom na dalšiu kartu výberte súbor.");
                return;
            }

            try
            {
                importedData = fileService.readFile(selectedFile, detectedDelimiter);

                //Posielame načitane data do kontrolera záložky rodičov
                if (parentsTabController != null)
                {
                    parentsTabController.loadData(importedData);
                }
            }
            catch (Exception e)
            {
                showAlert(Alert.AlertType.ERROR, "Chyba načitania",
                         "Nepodarilo sa načítať data: " + e.getMessage());
                return;
            }
        } else if (currentIndex ==1)// Výber rodičov
        {
            if (parentsTabController != null)
            {
                selectedParents = parentsTabController.getSelectedParents();

                if (selectedParents.isEmpty())
                {
                    showAlert(Alert.AlertType.WARNING, "Žiadný výber",
                            "Výber aspoň jedného rodiča pred pokračovaním.");
                    return;
                }
                // Tu môžete poslať vybraných rodičov do ďalšieho controllera
                // Napríklad, ak by ste mali AdressCheckTabController:
                // if (adressCheckTabController != null) {
                //     adressCheckTabController.setParents(selectedParents);
                // }
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

    //Metoda pre ziskanie vybraných rodičov(može byť použota v iných controlleroch)
    public List<Parent> getSelectedParents()
    {
        return selectedParents;
    }
}
