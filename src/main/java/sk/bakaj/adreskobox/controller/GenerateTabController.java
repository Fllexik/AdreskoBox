package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;
import sk.bakaj.adreskobox.service.ExcelService;
import sk.bakaj.adreskobox.service.PDFService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GenerateTabController
{
    private static final Log log = LogFactory.getLog(GenerateTabController.class);

    @FXML
    private VBox rootVbox;

    @FXML
    private TextField outputDirField;

    @FXML
    private Label labelPagesCountLabel;

    @FXML
    private Label submissionSheetsCountLabel;

    @FXML
    private Button browseOutpuDirButton;

    @FXML
    private Button generateButton;

    @FXML
    private Button startOverButton;

    @FXML
    private TextArea generationLogArea;

    private List<Parent> selectedParents;
    private LabelFormat selectedLabelFormat;
    private String senderName;
    private String senderStreet;
    private String senderCity;
    private ExcelService.MailType mailType;
    private File templateFile;

    private File outputDirectory;
    private PDFService pdfService = new PDFService();
    private ExcelService excelService = new ExcelService();

    private List<File> generatedFiles = new ArrayList<>();

    @FXML
    public void initialize()
    {
        // DÔLEŽITÉ: Uložiť controller do properties root elementu
        // Toto je kľúčové pre načítanie controllera v MainController
        if (rootVbox != null)
        {
            rootVbox.getProperties().put("controller", this);
        }

        //Predvolená cesta k výstupnému adresáru
        outputDirectory = new File(System.getProperty("user.home") + "/Documents/AdreskoBox");
        if (!outputDirectory.exists())
        {
            outputDirectory.mkdir();
        }
        outputDirField.setText(outputDirectory.getAbsolutePath());
    }

    @FXML
    private void handleBrowseOutputDir()
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Vyberte výstupný adresár");

        if (outputDirectory != null && outputDirectory.exists())
        {
            directoryChooser.setInitialDirectory(outputDirectory);
        }

        File dir = directoryChooser.showDialog(outputDirField.getScene().getWindow());
        if (dir != null)
        {
            outputDirectory = dir;
            outputDirField.setText(outputDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleGenerate()
    {
        if (outputDirectory == null || !outputDirectory.exists())
        {
            showAlert(Alert.AlertType.ERROR, "Chyba", "Výstupný adresár neexistuje.");
            return;
        }

        generationLogArea.clear();
        generatedFiles.clear();

        try
        {
            log("Začínam generovanie výstupných súborov...");

            //Generovanie štitkov
            int labelsPerPage = selectedLabelFormat.getColumns() * selectedLabelFormat.getRows();
            int totalPages = (int) Math.ceil(selectedParents.size() / (double) labelsPerPage);

            log("Generujem štítky:");
            log("- počet štítkov:" + selectedParents.size());
            log("- Štítkov na stranu: " + labelsPerPage);
            log("- Celkový počet strán: " + totalPages);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File labelsFile = new File(outputDirectory, "Stitky_" + timestamp + ".pdf");

            pdfService.generateLabels(selectedParents, selectedLabelFormat, labelsFile);
            generatedFiles.add(labelsFile);

            log("- Štítky boli úspešne vygenerované do súboru: " + labelsFile.getName());

            //Generovanie podacích hárkov
            log("\nGenerujem podacie hárky:");
            log("- Počet príjemcov: " + selectedParents.size());

            List<File> submissionSheets = excelService.createSubmissionSheets(
                    selectedParents, senderName, senderStreet, senderCity,
                    mailType, templateFile.getAbsolutePath());

            generatedFiles.addAll(submissionSheets);

            log("- Počet vygenerovaných podacích hárkov: " + submissionSheets.size());
            for (int i = 0; i < submissionSheets.size(); i++)
            {
                log("  " + (i + 1) + ". " + submissionSheets.get(i).getName());
            }

            log("\nGenerovanie dokončené. Všetky súbory boli uložené do: " + outputDirectory.getAbsolutePath());

            //Aktualizácia počítadiel
            labelPagesCountLabel.setText(String.valueOf(totalPages));
            submissionSheetsCountLabel.setText(String.valueOf(submissionSheets.size()));

            showAlert(Alert.AlertType.INFORMATION, "Generovnanie dokončené",
                    "Všetky súbory boli úspešne vygenerované do vystupného adresára.");
        } catch (Exception e)
        {
            log("\nCHYBA: pri generovaní: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Chyba pri generovaní",
                    "Nastala chyba pri generovaní súborov: " + e.getMessage());
        }
    }

    @FXML
    private void handleStartOver()
    {
        // TODO: Implementovať reset aplikácie
        showAlert(Alert.AlertType.INFORMATION, "Začať odznova",
                "Táto funkcionalita bude implementovaná v ďalšej verzii.");
    }

    @FXML
    private void handleOpenOutputDir()
    {
        if (outputDirectory != null && outputDirectory.exists())
        {
            try
            {
                java.awt.Desktop.getDesktop().open(outputDirectory);
            } catch (IOException e)
            {
                showAlert(Alert.AlertType.ERROR, "Chyba",
                        "Nepodarilo sa otvoriť výstupný adresár: " + e.getMessage());
            }
        }
    }

    public void setData(List<Parent> parents, LabelFormat labelFormat,
                        String senderName, String senderStreet, String senderCity,
                        ExcelService.MailType mailType, File templateFile)
    {
        this.selectedParents = parents;
        this.selectedLabelFormat = labelFormat;
        this.senderName = senderName;
        this.senderStreet = senderStreet;
        this.senderCity = senderCity;
        this.mailType = mailType;
        this.templateFile = templateFile;

        //Aktualizácia počitadiel
        int labelsPerPage = selectedLabelFormat.getColumns() * selectedLabelFormat.getRows();
        int totalPages = (int) Math.ceil(selectedParents.size() / (double) labelsPerPage);
        labelPagesCountLabel.setText(String.valueOf(totalPages));

        int sheetsCount = (int) Math.ceil(selectedParents.size() / 12.0);
        submissionSheetsCountLabel.setText(String.valueOf(sheetsCount));
    }

    private void log(String message)
    {
        generationLogArea.appendText(message + "\n");
    }

    private void showAlert(Alert.AlertType type, String title, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
