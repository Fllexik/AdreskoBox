package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sk.bakaj.adreskobox.model.ImportedData;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.service.FileService;
import java.io.File;
import java.util.List;

/**
 * Kontrolér pre záložku importu dát.
 * Spravuje výber súboru, detekciu formátu a nastavenie formátu štítkov.
 */
public class ImportController
{
    // FXML komponenty
    @FXML
    private VBox rootVbox;

    @FXML
    private TextField filePathField;

    @FXML
    private Label fileTypeLabel;

    @FXML
    private Label delimiterLabel;

    @FXML
    private Label headerStatusLabel;

    @FXML
    private CheckBox hasHeaderCheckBox;

    @FXML
    private ComboBox<LabelFormat> predefinedFormatsComboBox;

    @FXML
    private Label selectedFormatLabel;

    // Služby a dátové objekty
    private FileService fileService = new FileService();
    private File selectedFile;
    private String detectedFileType;
    private String detectedDelimiter;
    private boolean hasHeader = true;
    private LabelFormat selectedLabelFormat;

    /**
     * Inicializácia kontroléra - nastavenie ComboBox a event listenerov
     */
    @FXML
    public void initialize()
    {
        // Uloženie kontroléra do properties pre prístup z MainController
        rootVbox.getProperties().put("controller", this);

        // Naplnenie ComboBox predefinovanými formátmi
        predefinedFormatsComboBox.setItems(LabelFormat.getPredefinedFormats());

        // Nastavenie zobrazenia položiek v ComboBox
        setupComboBoxCellFactory();

        // Na začiatku predpokladáme, že súbor má hlavičku
        hasHeaderCheckBox.setSelected(true);

        // Listener pre zmenu stavu checkbox-u
        hasHeaderCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
            hasHeader = newValue;
            if (selectedFile != null)
            {
                updateHeaderStatusLabel();
            }
        });
    }

    /**
     * Nastavenie zobrazenia položiek v ComboBox
     */
    private void setupComboBoxCellFactory()
    {
        predefinedFormatsComboBox.setCellFactory(comboBox -> new ListCell<>()
        {
            @Override
            protected void updateItem(LabelFormat format, boolean empty)
            {
                super.updateItem(format, empty);
                setText((empty || format == null) ? null : format.getName());
            }
        });

        predefinedFormatsComboBox.setButtonCell(new ListCell<>()
        {
            @Override
            protected void updateItem(LabelFormat format, boolean empty)
            {
                super.updateItem(format, empty);
                setText((empty || format == null) ? null : format.getName());
            }
        });
    }
    /**
     * Obsluha výberu súboru cez FileChooser
     */
    @FXML
    private void handleSelectFile()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Výber zdrojového súboru");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Všetky podporované súbory", "*.csv", "*.xls", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx")
        );

        File file = fileChooser.showOpenDialog(filePathField.getScene().getWindow());
        if (file != null)
        {
            selectedFile = file;
            filePathField.setText(selectedFile.getAbsolutePath());
            detectedFileTypeAndDelimiter(file);
            detectHeaderRow(file);
        }
    }

    /**
     * Detekcia typu súboru a oddeľovača
     * @param file vybraný súbor
     */
    private void  detectedFileTypeAndDelimiter(File file)
    {
        detectedFileType = fileService.detectFileType(file);
        try
        {
            // Automatická detekcia typu súboru
            detectedDelimiter = fileService.detectFileDelimiter(file);

            // Nastavenie detekovaného typu súboru
            if (detectedFileType != null)
            {
                fileTypeLabel.setText(getFileTypeName(detectedFileType));

                // Pre CSV súbory detekuje aj oddeľovač
                if ("CSV".equals(detectedFileType))
                {
                    detectedDelimiter = fileService.detectFileDelimiter(file);
                    delimiterLabel.setText(getDelimiterName(detectedDelimiter));
                }
                else
                {
                    detectedDelimiter = null;
                    delimiterLabel.setText("N/A (Excel súbor)");
                }
            }
            else
            {
                fileTypeLabel.setText("Nepodporovaný formát");
                delimiterLabel.setText("N/A");
                showAlert(Alert.AlertType.ERROR, "Nepodporovaný súbor",
                        "Tento typ súboru nie je podporovaný. Podporované súbory sú len .csv, .xls, xlsx súbory");
            }
        }
        catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR, "Chyba detekcie",
                    "Nepodarilo sa detekovať formát alebo oddeľovač: " + e.getMessage());
            fileTypeLabel.setText("Chyba");
            delimiterLabel.setText("Chyba");
            detectedFileType = null;
            detectedDelimiter = null;
        }
    }

    /**
     * Detekcia hlavičkového riadku v súbore
     * @param file vybraný súbor
     */
    private void detectHeaderRow(File file)
    {
        try
        {
            boolean detectedHeader = fileService.hasHeaderRow(file);
            hasHeader = detectedHeader;
            hasHeaderCheckBox.setSelected(detectedHeader);
            updateHeaderStatusLabel();
        }
        catch (Exception e)
        {
            headerStatusLabel.setText("Nepodarilo sa detekovať hlavičku");
            headerStatusLabel.setStyle("-fx-text-fill: red");
        }
    }

    /**
     * Aktualizácia zobrazenia stavu hlavičky
     */
    private void updateHeaderStatusLabel()
    {
        if (hasHeader)
        {
            headerStatusLabel.setText("Súbor obsahuje hlavičku");
            headerStatusLabel.setStyle("-fx-text-fill: green");
        }
        else
        {
            headerStatusLabel.setText("Súbor neobsahuje hlavičku alebo nebola rozpoznaná");
            headerStatusLabel.setStyle("-fx-text-fill: orange");
        }
    }

    /**
     * Získanie popisného názvu typu súboru
     * @param fileType detekovaný typ súboru
     * @return popisný názov typu súboru
     */
    private String getFileTypeName(String fileType)
    {
        if(fileType == null) return "Neznámy";
        return switch (fileType)
        {
            case "CSV" -> "CSV (textový súbor)";
            case "XLS" -> "XLS (Excel 97-2003)";
            case "XLSX" -> "XLSX (Excel súbor)";
            default -> fileType;
        };
    }

    /**
     * Získanie popisného názvu oddeľovača
     * @param delimiter detekovaný oddeľovač
     * @return popisný názov oddeľovača
     */
    private String getDelimiterName(String delimiter)
    {
        if (delimiter == null) return "Neznámy";
        return switch (delimiter)
        {
            case "," -> "Čiarka (,)";
            case ";" -> "Bodkočiarka (;)";
            case "\t" -> "Tabulátor";
            case "|" -> "Zvislá čiarka (|)";
            default -> "Vlastný (" + delimiter + ")";
        };
    }

    /**
     * Obsluha výberu predefinovaného formátu štítku
     */
    @FXML
    private void handlePredefinedFormatSelection()
    {
        selectedLabelFormat = predefinedFormatsComboBox.getValue();
        if (selectedLabelFormat != null)
        {
            selectedFormatLabel.setText("Vybraný formát: " + selectedLabelFormat.getName());
        }
    }

    /**
     * Obsluha vytvorenia vlastného formátu štítku
     */
    @FXML
    private void handleCustomFormat()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CustomLabelFormatDialog.fxml"));
            Scene scene = new Scene(loader.load());

            CustomLabelFormatController controller = loader.getController();

            // Ak je už nastavený nejaký formát, použijeme ho ako základ
            if (selectedLabelFormat != null)
            {
                controller.initWithFormat(selectedLabelFormat);
            }

            Stage stage = new Stage();
            stage.setTitle("Vlastný formát štítku");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Po zatvorení dialógu získame nastavený formát
            LabelFormat customFormat = controller.getCustomLabelFormat();
            if (customFormat != null)
            {
                selectedLabelFormat = customFormat;
                predefinedFormatsComboBox.getSelectionModel().clearSelection();
                selectedFormatLabel.setText("Vlastný Formát: " +
                        String.format("%.1f x %.1f mm(%dx%d ks)",
                                customFormat.getWidth(),
                                customFormat.getHeight(),
                                customFormat.getColumns(),
                                customFormat.getRows()));
            }
        }
        catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR,"Chyba",
                    "Nepodarilo sa otvoriť dialog vlastného formátu: " + e.getMessage());
        }
    }

    /**
     * Obsluha náhľadu dát (zatiaľ len validácia)
     */
    @FXML
    private void handlePreviewData()
    {
        if (!isReadyToProceed())
        {
            showAlert(Alert.AlertType.WARNING, "Neuplné údaje",
                    "Pred zobrazením náhľadu vyberte súbor a formát štítkov.");
            return;
        }

        try
        {
            // Tu môžete implementovať logiku pre zobrazenie náhľadu
            // Napríklad môžete otvoriť nové okno s náhľadom dát
            showAlert(Alert.AlertType.INFORMATION, "Náhľad",
                    "Funkcia náhľadu dát bude implementovaná v budúcej verzii.");
        }
        catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR,"Chyba",
                    "Nepodarilo sa zobraziť náhľad:" + e.getMessage());
        }
    }

    // Gettery pre prístup k dátam z MainController

    /**
     * Získanie vybraného súboru
     * @return vybraný súbor alebo null
     */
    public File getSelectedFile()
    {
        return selectedFile;
    }

    /**
     * Získanie detekovaného typu súboru
     * @return typ súboru alebo null
     */
    public String getDetectedFileType()
    {
        return detectedFileType;
    }

    /**
     * Získanie detekovaného oddeľovača
     * @return oddeľovač alebo null
     */
    public String getDetectedDelimiter ()
    {
        return detectedDelimiter;
    }

    /**
     * Zistenie, či súbor má hlavičku
     * @return true ak má hlavičku, false inak
     */
    public boolean hasHeader()
    {
        return hasHeader;
    }

    /**
     * Získanie vybraného formátu štítku
     * @return vybraný formát štítku alebo null
     */
    public LabelFormat getSelectedLabelFormat()
    {
        return selectedLabelFormat;
    }

    /**
     * Kontrola, či sú všetky potrebné údaje nastavené
     * @return true ak je možné pokračovať, false inak
     */
    public boolean isReadyToProceed()
    {
        return selectedFile != null &&
                detectedFileType != null &&
                selectedLabelFormat != null;
    }

    /**
     * Zobrazenie alert dialógu
     * @param type typ alertu
     * @param title titulok alertu
     * @param content obsah alertu
     */
    private void showAlert(Alert.AlertType type, String title, String content)
        {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        }
}