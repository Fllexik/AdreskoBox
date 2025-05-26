package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.service.FileService;
import java.io.File;

public class ImportController
{
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

    private FileService fileService = new FileService();
    private File selectedFile;
    private String detectedFileType;
    private String detectedDelimiter;
    private boolean hasHeader = true;
    private LabelFormat selectedLabelFormat;

    @FXML
    public void initialize()
    {
        //naplnenie Comboboxu predefinovanymi formatami
        predefinedFormatsComboBox.setItems(LabelFormat.getPredefinedFormats());

        //Na začiatku predpokladame, že subor ma hlavičku
        hasHeaderCheckBox.setSelected(true);

        //Listener pre zmenu stavu checkboxu
        hasHeaderCheckBox.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
            hasHeader = newValue;
            if (selectedFile != null)
            {
                updateHeaderStatusLabel();
            }
        });
    }

    @FXML
    private void handleSelectFile()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vyber zdrojový suboru");
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
    private void  detectedFileTypeAndDelimiter(File file)
    {
        detectedFileType = fileService.detectFileType(file);
        try
        {
            //Automatická detekcia typu súboru
            detectedDelimiter = fileService.detectFileDelimiter(file);

            //Nastavenie detekovaného typu súboru
            if (detectedFileType != null)
            {
                fileTypeLabel.setText(getFileTypeName(detectedFileType));

                //pre CSV subory detekuje aj oddeľovač
                if ("CSV".equals(detectedFileType))
                {
                    detectedDelimiter = fileService.detectFileDelimiter(file);
                    delimiterLabel.setText(getDelimiterName(detectedDelimiter));
                }else {
                    detectedDelimiter = null;
                    delimiterLabel.setText("N/A (Excel súbor)");
                }
            }else {
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

    private void detectHeaderRow(File file)
    {
        try
        {
            boolean detectedHeader = fileService.hasHeaderRow(file);
            hasHeader = detectedHeader;
            hasHeaderCheckBox.setSelected(detectedHeader);
            updateHeaderStatusLabel();
        } catch (Exception e)
        {
            headerStatusLabel.setText("Nepodarilo sa detekovať hlavičku");
            headerStatusLabel.setStyle("-fx-text-fill: red");
        }
    }

    private void updateHeaderStatusLabel()
    {
        if (hasHeader)
        {
            headerStatusLabel.setText("Súbor obsahuje hlavičku");
            headerStatusLabel.setStyle("-fx-text-fill: green");
        }else {
            headerStatusLabel.setText("Súbor neobsahuje hlavičku alebo nebola rozpoznaná");
            headerStatusLabel.setStyle("-fx-text-fill: orange");
        }
    }

    private String getFileTypeName(String fileType)
    {
        if(fileType == null) return "Neznámy";
        switch (fileType)
        {
            case "CSV": return "CSV (textový súbor)";
            case "XLS": return "XLS (Excel 97-2003)";
            case "XLSX": return "XLSX (Excel súbor)";
            default: return fileType;
        }
    }

    private String getDelimiterName(String delimiter)
    {
        if (delimiter == null) return "Neznámy";
        switch (delimiter)
        {
            case ",": return "Čiarka (,)";
            case ";": return "Bodkočiarka (;)";
            case "\t": return "Tabulátor";
            case "|": return "Zvislá čiarka (|)";
            default: return "Vlastný (" + delimiter + ")";
        }
    }

    @FXML
    private void handlePredefinedFormatSelection()
    {
        selectedLabelFormat = predefinedFormatsComboBox.getValue();
        if (selectedLabelFormat != null)
        {
            selectedFormatLabel.setText("Vybraný formát: " + selectedLabelFormat.getName());
        }
    }

    @FXML
    private void handleCustomFormat()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CustomLabelFormatDialog.fxml"));
            Scene scene = new Scene(loader.load());

            CustomLabelFormatController controler = loader.getController();

            //Ak je už nastavený nejaký formát, použijeme ho ako základ
            if (selectedLabelFormat != null)
            {
                controler.initWithFormat(selectedLabelFormat);
            }

            Stage stage = new Stage();
            stage.setTitle("Vlastný formát štítku");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            //Po zatvorení dialogu získame nastavený formát
            LabelFormat customFormat = controler.getCustomLabelFormat();
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
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR,"Chyba",
                    "Nepodarilo sa otvoriť dialog vlastného formátu: " + e.getMessage());
        }
    }

    public File getSelectedFile()
    {
        return selectedFile;
    }

    public String getDetectedFileType() {return detectedFileType;}

    public String getDetectedDelimiter () {return detectedDelimiter;}

    public boolean hasHeader()
    {
        return hasHeader;
    }

    public LabelFormat getSelectedLabelFormat(){return selectedLabelFormat;}

    public boolean isReadyToProceed(){
        return selectedFile != null &&
                detectedFileType != null &&
                selectedLabelFormat != null;
    }

    private void showAlert(Alert.AlertType type, String title, String content)
        {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        }
    @FXML
    private void handlePreviewData()
    {
        //implementacia pre nahlad dat
        if (!isReadyToProceed())
        {
            showAlert(Alert.AlertType.WARNING, "Neuplné údaje",
                    "Pred zobrazením náhľadu vyberte súbor a formát štítkov.");
            return;
        }
        //Tu može iplementovať logiku pre zobrazenie náhľadu
        // Napriklád môžete otvoriť nove okno s náhľadom dat
        try
        {
            //Implementacia pre zobrazenie náhľadu
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR,"Chyba",
                    "Nepodarilo sa zobraziť náhľad:" + e.getMessage());
        }
    }

    @FXML
    private void handleImportData()
    {
        //implementacia pre import dat
        if (!isReadyToProceed())
        {
            showAlert(Alert.AlertType.WARNING, "Neúplné údaje",
                    "Pred importom vyberte súbor a formát štítkov.");
            return;
        }

        try
        {
            //Implementácia importu dát
            showAlert(Alert.AlertType.INFORMATION, "Úspech",
                    "Údaje boli úspešne importované.");
        } catch (Exception e)
        {
            showAlert(Alert.AlertType.ERROR,"Chyba",
                    "Nepodarilo sa importovať údaje:" + e.getMessage());
        }
    }
}