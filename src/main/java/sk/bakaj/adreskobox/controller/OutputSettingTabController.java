package sk.bakaj.adreskobox.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;
import sk.bakaj.adreskobox.service.ExcelService;

import java.io.File;
import java.util.List;

public class OutputSettingTabController
{
    @FXML
    private VBox rootVbox;

    @FXML
    private TextField senderNameField;

    @FXML
    private TextField senderStreetField;

    @FXML
    private TextField senderCityField;


    @FXML
    private TextField templatePathField;

    @FXML
    private Button browseTemplateButton;

    @FXML
    private Button createTemplateButton;

    private File templateFile;
    private ExcelService excelService = new ExcelService();
    private List<Parent> selectedParents;
    private LabelFormat selectedLabelFormat;

    @FXML
    public void initialize()
    {
        // DÔLEŽITÉ: Uložiť controller do properties root elementu
        // Toto je kľúčové pre načítanie controllera v MainController
        if (rootVbox != null)
        {
            rootVbox.getProperties().put("controller", this);
        }

        // Načítať posledné údaje odosielateľa
        loadSenderData();

        //Predvolená cesta k šablone
        File defaultTemplate = new File("templates/podaci-harok.xlsx");
        if (defaultTemplate.exists())
        {
            templateFile = defaultTemplate;
            templatePathField.setText(defaultTemplate.getAbsolutePath());
        }
    }

    @FXML
    private void handleBrowseTemplate()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vybrať šablonu podacieho hárku");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel súbory", "*.xls", "*xlsx"));

        // Nastaviť počiatočný adresár na posledne použitý adresár
        if (templateFile != null && templateFile.getParentFile() != null) {
            fileChooser.setInitialDirectory(templateFile.getParentFile());
        } else {
            // Ak neexistuje predchádzajúci súbor, nastaviť na templates folder
            File templatesDir = new File("templates");
            if (templatesDir.exists() && templatesDir.isDirectory()) {
                fileChooser.setInitialDirectory(templatesDir);
            }
        }

        File file = fileChooser.showOpenDialog(senderNameField.getScene().getWindow());
        if (file != null)
        {
            templateFile = file;
            templatePathField.setText(file.getAbsolutePath());

            // Automaticky nastaviť FileChooser na tento adresár pre ďalšie použitie
            updateFileChooserDirectory(file.getParentFile());
        }
    }

    @FXML
    private void handleCreateTemplate()
    {
        try
        {
            File newTemplate = excelService.createNewSubmissionTemplate();
            templateFile = newTemplate;
            templatePathField.setText(newTemplate.getAbsolutePath());

            // Automaticky nastaviť adresár na adresár novo vytvorenej šablóny
            updateFileChooserDirectory(newTemplate.getParentFile());

            showAlert(Alert.AlertType.INFORMATION,"Šablóna vytvorená",
                    "Nová šablóna podacieho hárku bola úspešne vytvorená: " + newTemplate.getAbsolutePath());
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR,"Chyba",
                    "Nepodarilo sa vytvoriť šablonu: " + e.getMessage());
        }
    }

    /**
     * Načítanie posledných údajov odosielateľa
     */
    private void loadSenderData() {
        String lastName = System.getProperty("senderName", "");
        String lastStreet = System.getProperty("senderStreet", "");
        String lastCity = System.getProperty("senderCity", "");

        senderNameField.setText(lastName);
        senderStreetField.setText(lastStreet);
        senderCityField.setText(lastCity);
    }

    /**
     * Uloženie údajov odosielateľa
     */
    private void saveSenderData() {
        System.setProperty("senderName", senderNameField.getText().trim());
        System.setProperty("senderStreet", senderStreetField.getText().trim());
        System.setProperty("senderCity", senderCityField.getText().trim());
    }

    /**
     * Pomocná metóda na aktualizáciu posledne použitého adresára
     */
    private void updateFileChooserDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            // Tu môžete uložiť adresár do properties alebo konfiguračného súboru
            // pre zachovanie medzi reštartmi aplikácie
            System.setProperty("lastUsedTemplateDirectory", directory.getAbsolutePath());
        }
    }

    /**
     * Získanie posledne použitého adresára
     */
    private File getLastUsedDirectory() {
        String lastDir = System.getProperty("lastUsedTemplateDirectory");
        if (lastDir != null) {
            File dir = new File(lastDir);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return null;
    }

    /**
     * Validácia vstupných údajov
     */
    public boolean validateInput()
    {
        StringBuilder errors = new StringBuilder();

        if (senderNameField.getText().trim().isEmpty())
        {
            errors.append("- Meno odosielateľa nemôže byť prázdne\n");
        }
        if (senderStreetField.getText().trim().isEmpty())
        {
            errors.append("- Ulica odosielateľa nemôže byť prázdna\n");
        }
        if (senderCityField.getText().trim().isEmpty())
        {
            errors.append("- PSČ a mesto odosielateľa nemôže byť prázdne\n");
        }
        if (templateFile == null || !templateFile.exists())
        {
            errors.append("- Šablóna podacieho hárku nebola najdená\n");
        }
        if (errors.length() > 0)
        {
            showAlert(Alert.AlertType.ERROR, "Neplatné údaje", errors.toString());
            return false;
        }
        // Uložiť údaje odosielateľa po úspešnej validácii
        saveSenderData();
        return true;
    }

    public void setData(List<Parent> parents, LabelFormat labelFormat)
    {
        this.selectedParents = parents;
        this.selectedLabelFormat = labelFormat;
    }

    public String getSenderName()
    {
        return senderNameField.getText().trim();
    }

    public String getSenderStreet()
    {
        return senderStreetField.getText().trim();
    }

    public String getSenderCity()
    {
        return senderCityField.getText().trim();
    }

    public File getTemplateFile()
    {
        return templateFile;
    }

    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
