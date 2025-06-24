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

/**
 * Hlavný kontrolér aplikácie, ktorý spravuje navigáciu medzi záložkami
 * a koordinuje tok dát medzi jednotlivými kontrolérmi.
 */
public class MainController
{
    // FXML komponenty
    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button themeToggleButton;

    // Vnorené kontroléry pre jednotlivé záložky
    private ImportController importController;
    private ParentsTabController parentsTabController;
    private AdressCheckTabController adressCheckTabController;
    private PrintPreviewController printPreviewController;
    private OutputSettingTabController outputSettingTabController;
    private GenerateTabController generateTabController;

    // Dátové objekty pre tok informácií medzi záložkami
    private List<ImportedData> importedData;
    private List<Parent> selectedParents;
    private List<Parent> processedParents;

    // Služby
    private FileService fileService = new FileService();

    // Dark mode stav
    private boolean isDarkMode = false;

    /**
     * Inicializácia kontroléra - nastavenie event listenerov a načítanie vnorených kontrolérov
     */
    @FXML
    private void initialize()
    {
        setupEventListeners();
        setupThemeToggle();
        updateButtonStates();
        loadNestedControllers();
    }

    /**
     * Nastavenie event listenerov pre navigačné tlačidlá a zmenu záložiek
     */
    private void setupEventListeners()
    {

        //nastavenie listenerov pre tlačidlá
        prevButton.setOnAction(event -> navigateToPreviousTab());
        nextButton.setOnAction(event -> navigateToNextTab());

        // Listener pre zmenu záložky - aktualizuje stav tlačidiel
        tabPane.getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> updateButtonStates());
    }

    /**
     * Nastavenie dark mode tlačidla
     */
    private void setupThemeToggle()
    {
        themeToggleButton.setOnAction(event -> toggleDarkMode());
        updateThemeButtonText();
    }

    /**
     * Prepínanie medzi light a dark režimom
     */
    private void toggleDarkMode()
    {
        isDarkMode = !isDarkMode;

        // Získanie root node (BorderPane)
        Node root = tabPane.getScene().getRoot();

        if (isDarkMode)
        {
            // Aktivácia dark mode
            root.getStyleClass().add("dark-mode");
        }
        else
        {
            // Deaktivácia dark mode
            root.getStyleClass().remove("dark-mode");
        }

        updateThemeButtonText();
    }

    /**
     * Aktualizácia textu na theme toggle tlačidle
     */
    private void updateThemeButtonText()
    {
        if (isDarkMode)
        {
            themeToggleButton.setText("☀️ Light Mode");
        }
        else
        {
            themeToggleButton.setText("🌙 Dark Mode");
        }
    }

    /**
     * Načítanie vnorených kontrolérov z jednotlivých záložiek
     */
    private void loadNestedControllers()
    {
        Platform.runLater(() ->
        {
            try
            {
                loadImportController();
                loadParentsTabController();
                loadAddressCheckTabController();
                loadOutputSettingTabController();
                loadGenerateTabController();

            }
            catch (Exception e)
            {
                System.err.println("Chyba pri načítavaní kontrolérov: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Načítanie ImportController z prvej záložky
     */
    private void loadImportController()
    {
        Node importTabContent = tabPane.getTabs().get(0).getContent();
        if (importTabContent instanceof VBox)
        {
            VBox vbox = (VBox) importTabContent;
            importController = (ImportController) vbox.getProperties().get("controller");
        }
    }

    /**
     * Načítanie ParentsTabController z druhej záložky
     */
    private void loadParentsTabController()
    {
        Node parentsTabContent = tabPane.getTabs().get(1).getContent();
        if (parentsTabContent != null)
        {
            Object controller = parentsTabContent.getProperties().get("controller");
            if (controller instanceof ParentsTabController)
            {
                parentsTabController = (ParentsTabController) controller;
            }
        }
    }

    /**
     * Načítanie AddressCheckTabController z tretej záložky
     */
    private void loadAddressCheckTabController()
    {
        Node addressCheckContent = tabPane.getTabs().get(2).getContent();
        if (addressCheckContent != null)
        {
            Object controller = addressCheckContent.getProperties().get("controller");
            if (controller instanceof AdressCheckTabController)
            {
                adressCheckTabController = (AdressCheckTabController) controller;
            }
        }
    }

    /**
     * Načítanie OutputSettingTabController zo štvrtej záložky
     */
    private void loadOutputSettingTabController()
    {
        if (tabPane.getTabs().size() > 3)
        {
            Node outputSettingsContent = tabPane.getTabs().get(3).getContent();
            if (outputSettingsContent != null)
            {
                Object controller = outputSettingsContent.getProperties().get("controller");
                if (controller instanceof OutputSettingTabController)
                {
                    outputSettingTabController = (OutputSettingTabController) controller;
                }
            }
        }
    }

    /**
     * Načítanie GenerateTabController z piatej záložky
     */
    private void loadGenerateTabController()
    {
        if (tabPane.getTabs().size() > 4)
        {
            Node generateContent = tabPane.getTabs().get(4).getContent();
            if (generateContent != null)
            {
                Object controller = generateContent.getProperties().get("controller");
                if (controller instanceof GenerateTabController)
                {
                    generateTabController = (GenerateTabController) controller;
                }
            }
        }
    }

    /**
     * Navigácia na predchádzajúcu záložku
     */
    public void navigateToPreviousTab()
    {
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0)
        {
            tabPane.getSelectionModel().select(currentIndex - 1);
        }
    }

    /**
     * Navigácia na nasledujúcu záložku s validáciou dát
     */
    public void navigateToNextTab()
    {
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();

        // Validácia a spracovanie dát pre každú záložku
        switch (currentIndex)
        {
            case 0:
                if (!processImportTab()) return;
                break;
            case 1:
                if (!processParentsTab()) return;
                break;
            case 2:
                if (!processAddressCheckTab()) return;
                break;
            case 3:
                if (!processOutputSettingsTab()) return;
                break;
        }

        // Prechod na nasledujúcu záložku
        if (currentIndex < tabPane.getTabs().size() - 1)
        {
            tabPane.getSelectionModel().select(currentIndex + 1);
        }
    }

    /**
     * Spracovanie záložky importu dát
     * @return true ak je spracovanie úspešné, false inak
     */
    private boolean processImportTab()
    {
        if (importController == null)
        {
            showAlert(Alert.AlertType.ERROR, "Chyba", "ImportController nie je inicializovaný.");
            return false;
        }

        File selectedFile = importController.getSelectedFile();
        if (selectedFile == null)
        {
            showAlert(Alert.AlertType.WARNING, "Chýba súbor",
                    "Pred prechodom na ďalšiu kartu vyberte súbor.");
            return false;
        }

        if (importController.getSelectedLabelFormat() == null)
            {
                showAlert(Alert.AlertType.WARNING, "Chýba formát štítkov",
                        "Pred prechodom na ďalšiu kartu vyberte formát štítku.");
                return false;
            }

            try
            {
                importedData = fileService.readFile(selectedFile);

                // Posielanie načítaných dát do kontroléra záložky rodičov
                if (parentsTabController != null)
                {
                    parentsTabController.loadData(importedData);
                }
                return true;
            }
            catch (Exception e)
            {
                showAlert(Alert.AlertType.ERROR, "Chyba načítania",
                        "Nepodarilo sa načítať dáta: " + e.getMessage());
                return false;
            }
        }

    /**
     * Spracovanie záložky výberu rodičov
     * @return true ak je spracovanie úspešné, false inak
     */
    private boolean processParentsTab()
    {
        if (parentsTabController != null)
        {
            selectedParents = parentsTabController.getSelectedParents();

            if (selectedParents.isEmpty())
            {
                showAlert(Alert.AlertType.WARNING, "Žiadny výber",
                        "Vyberte aspoň jedného rodiča pred pokračovaním.");
                return false;
            }
            // Poslanie vybraných rodičov do AddressCheckTabController
            if (adressCheckTabController != null)
            {
                adressCheckTabController.setData(selectedParents, importController.getSelectedLabelFormat());
            }
        }
        return true;
    }

    /**
     * Spracovanie záložky kontroly adries
     * @return true ak je spracovanie úspešné, false inak
     */
    private boolean processAddressCheckTab()
    {
        if (adressCheckTabController != null)
        {
            processedParents = adressCheckTabController.getParentsWithAbbreviatedAddresses();

            if (processedParents.isEmpty())
            {
                showAlert(Alert.AlertType.WARNING, "Žiadne adresy",
                        "Neboli nájdené žiadne upravené adresy.");
                return false;
            }
            // Poslanie upravených adries do PrintPreviewController
            if (printPreviewController != null)
            {
                printPreviewController.setData(processedParents, importController.getSelectedLabelFormat());
            }
        }
        return true;
    }

    /**
     * Spracovanie záložky nastavení výstupu
     * @return true ak je spracovanie úspešné, false inak
     */
    private boolean processOutputSettingsTab()
    {
        if (outputSettingTabController != null)
        {
            if (!outputSettingTabController.validateInput())
            {
                return false;
            }

            // Poslanie údajov do GenerateTabController
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
        return true;
    }

    /**
     * Aktualizácia stavu navigačných tlačidiel na základe aktuálnej záložky
     */
    public void updateButtonStates()
    {
        int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
        prevButton.setDisable(currentIndex == 0);
        nextButton.setDisable(currentIndex == tabPane.getTabs().size() - 1);
    }

    /**
     * Zobrazenie alert dialógu s daným typom, titulkom a obsahom
     */
    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Gettery pre prístup k dátam z iných kontrolérov

    /**
     * Získanie zoznamu vybraných rodičov
     * @return zoznam vybraných rodičov
     */
    public List<Parent> getSelectedParents()
    {
        return selectedParents;
    }

    /**
     * Získanie zoznamu spracovaných rodičov s upravenými adresami
     * @return zoznam spracovaných rodičov
     */
    public List<Parent> getProcessedParents()
    {
        return processedParents;
    }

    /**
     * Získanie aktuálneho stavu dark mode
     * @return true ak je aktivovaný dark mode, false inak
     */
    public boolean isDarkMode()
    {
        return isDarkMode;
    }

    /**
     * Nastavenie dark mode programaticky
     * @param darkMode true pre aktiváciu dark mode, false pre deaktiváciu
     */
    public void setDarkMode(boolean darkMode)
    {
        if (this.isDarkMode != darkMode)
        {
            toggleDarkMode();
        }
    }
}