package sk.bakaj.adreskobox.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
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


    private ImportController importController;
    private ParentsTabController parentsTabController;
    private AdressCheckTabController adressCheckTabController;
    private PrintPreviewController printPreviewController;
    private OutputSettingTabController outputSettingTabController;
    private GenerateTabController generateTabController;


    private List<ImportedData> importedData;
    private List<Parent> selectedParents;
    private List<Parent> processedParents;
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

        //Načitanie vnorených controllerov
        loadNestedControllers();
    }

    private void loadNestedControllers()
    {
        Platform.runLater(() -> {
            try {
                // Načítanie ImportController z prvej záložky (index 0)
                Node importTabContent = tabPane.getTabs().get(0).getContent();
                if (importTabContent instanceof VBox) {
                    VBox vbox = (VBox) importTabContent;
                    importController = (ImportController) vbox.getProperties().get("controller");
                }

                // Načítanie ParentsTabController z druhej záložky (index 1)
                Node parentsTabContent = tabPane.getTabs().get(1).getContent();
                if (parentsTabContent != null) {
                    Object controller = parentsTabContent.getProperties().get("controller");
                    if (controller instanceof ParentsTabController) {
                        parentsTabController = (ParentsTabController) controller;
                    }
                }

                // Načítanie AdressCheckTabController z tretej záložky (index 2)
                Node addressCheckContent = tabPane.getTabs().get(2).getContent();
                if (addressCheckContent != null) {
                    Object controller = addressCheckContent.getProperties().get("controller");
                    if (controller instanceof AdressCheckTabController) {
                        adressCheckTabController = (AdressCheckTabController) controller;
                    }
                }

                // Načítanie OutputSettingTabController zo štvrtej záložky (index 3) - OPRAVA!
                if (tabPane.getTabs().size() > 3) {
                    Node outputSettingsContent = tabPane.getTabs().get(3).getContent();
                    if (outputSettingsContent != null) {
                        Object controller = outputSettingsContent.getProperties().get("controller");
                        if (controller instanceof OutputSettingTabController) {
                            outputSettingTabController = (OutputSettingTabController) controller;
                        }
                    }
                }

                // Načítanie GenerateTabController z piatej záložky (index 4) - OPRAVA!
                if (tabPane.getTabs().size() > 4) {
                    Node generateContent = tabPane.getTabs().get(4).getContent();
                    if (generateContent != null) {
                        Object controller = generateContent.getProperties().get("controller");
                        if (controller instanceof GenerateTabController) {
                            generateTabController = (GenerateTabController) controller;
                        }
                    }
                }

                System.out.println("ImportController načítaný: " + (importController != null));
                System.out.println("ParentsTabController načítaný: " + (parentsTabController != null));
                System.out.println("AdressCheckTabController načítaný: " + (adressCheckTabController != null));
                System.out.println("OutputSettingTabController načítaný: " + (outputSettingTabController != null));
                System.out.println("GenerateTabController načítaný: " + (generateTabController != null));
            } catch (Exception e) {
                System.err.println("Chyba pri načítavaní controllerov: " + e.getMessage());
                e.printStackTrace();
            }
        });
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

        // Pokiaľ je na tabulatore "import dát", je potrebné spracovať dáta
        if (currentIndex == 0)
        {
            if (importController == null) {
                showAlert(Alert.AlertType.ERROR, "Chyba", "ImportController nie je inicializovaný.");
                return;
            }

            File selectedFile = importController.getSelectedFile();
            if (selectedFile == null)
            {
                showAlert(Alert.AlertType.WARNING, "Chýba súbor",
                        "Pred prechodom na ďalšiu kartu vyberte súbor.");
                return;
            }

            if (importController.getSelectedLabelFormat() == null)
            {
                showAlert(Alert.AlertType.WARNING, "Chýba formát štítkov",
                        "Pred prechodom na ďalšiu kartu vyberte formát štítku.");
                return;
            }

            try
            {
                importedData = fileService.readFile(selectedFile);

                // Posielame načítané dáta do kontrolera záložky rodičov
                if (parentsTabController != null)
                {
                    parentsTabController.loadData(importedData);
                }
            }
            catch (Exception e)
            {
                showAlert(Alert.AlertType.ERROR, "Chyba načítania",
                        "Nepodarilo sa načítať dáta: " + e.getMessage());
                return;
            }
        }
        else if (currentIndex == 1) // Výber rodičov
        {
            if (parentsTabController != null)
            {
                selectedParents = parentsTabController.getSelectedParents();

                if (selectedParents.isEmpty())
                {
                    showAlert(Alert.AlertType.WARNING, "Žiadny výber",
                            "Vyberte aspoň jedného rodiča pred pokračovaním.");
                    return;
                }
                // Poslať vybraných rodičov do AdressCheckTabController
                if (adressCheckTabController != null)
                {
                    adressCheckTabController.setData(selectedParents, importController.getSelectedLabelFormat());
                }
            }
        }
        else if (currentIndex == 2) // Kontrola adries
        {
            if (adressCheckTabController != null)
            {
                processedParents = adressCheckTabController.getParentsWithAbbreviatedAddresses();

                if (processedParents.isEmpty())
                {
                    showAlert(Alert.AlertType.WARNING, "Žiadne adresy",
                            "Neboli nájdené žiadne upravené adresy.");
                    return;
                }
                // Poslať upravené adresy do PrintPreviewControler (ak existuje)
                if (printPreviewController != null)
                {
                    printPreviewController.setData(processedParents, importController.getSelectedLabelFormat());
                }
            }
        }
        else if (currentIndex == 3) // Output Settings Tab - OPRAVA indexu z 4 na 3!
        {
            if (outputSettingTabController != null)
            {
                if (!outputSettingTabController.validateInput())
                {
                    return;
                }

                // Pošlem údaje do GenerateTabController
                if (generateTabController != null)
                {
                    generateTabController.setData(
                            processedParents,
                            importController.getSelectedLabelFormat(),
                            outputSettingTabController.getSenderName(),
                            outputSettingTabController.getSenderStreet(),
                            outputSettingTabController.getSenderCity(),
                            outputSettingTabController.getTemplateFile()
                    );
                }
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

    //Metoda pre ziskanie upravených rodičov(može byť použota v iných controlleroch)
    public List<Parent> getProcessedParents()
    {
        return processedParents;
    }
}
/**
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

    //Metoda pre ziskanie upravených rodičov(može byť použota v iných controlleroch)
    public List<Parent> getProcessedParents()
    {
        return processedParents;
    }
}*/
