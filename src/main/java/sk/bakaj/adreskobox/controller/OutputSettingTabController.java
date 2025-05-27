package sk.bakaj.adreskobox.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;
import sk.bakaj.adreskobox.service.ExcelService;

import java.io.File;
import java.util.List;

public class OutputSettingTabController
{
    @FXML
    private TextField senderNameField;

    @FXML
    private TextField senderStreetField;

    @FXML
    private TextField senderCityField;

    @FXML
    private ComboBox<ExcelService.MailType> mailTypeComboBox;

    @FXML
    private TextField templateFileField;

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
        //Inicializácia comboboxu pre typ zásielky
        mailTypeComboBox.setItems(FXCollections.observableArrayList(ExcelService.MailType.valuees()));
        mailTypeComboBox.getSelectionModel().select(ExcelService.MailType.OFFICIAL); //Predvolená hodnota

        //Predvolená cesta k šablone
        File defaultTemplate = new File("templates/podaci-harok.xls");
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

        File file = fileChooser.showOpenDialog(senderNameField.getScene().getWindow());
        if (file != null)
        {
            templateFile = file;
            templatePathField.setText(file.getAbsolutePath());
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
            showAlert(Alert.AlertType.INFORMATION,"Šablóna vytvorená",
                    "Nová šablóna podacieho hárku bola úspešne vytvorená: " + newTemplate.getAbsolutePath());
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR,"Chyba",
                    "Nepodarilo sa vytvoriť šablonu: " + e.getMessage());
        }
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
        if (mailTypeComboBox.getValue() == null)
        {
            errors.append("- Vyberte typ zásielky\n");
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

    public ExcelService.MailType getMailType()
    {
        return mailTypeComboBox.getValue();
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
