package sk.bakaj.adreskobox.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sk.bakaj.adreskobox.model.LabelFormat;

public class CustomLabelFormatController
{
    @FXML
    private TextField nameField;

    @FXML
    private TextField widthField;

    @FXML
    private TextField heightField;

    @FXML
    private TextField columnsField;

    @FXML
    private TextField rowsField;

    @FXML
    private TextField leftMarginField;

    @FXML
    private TextField rightMarginField;

    @FXML
    private TextField topMarginField;

    @FXML
    private TextField bottomMarginField;

    @FXML
    private TextField horizontalGapField;

    @FXML
    private TextField verticalGapField;

    @FXML
    private TextField maxAddressLengthField;

    @FXML
    private Canvas previewCanvas;

    @FXML
    private Label errorLabel;

    private LabelFormat customLabelFormat;

    @FXML
    public void initialize()
    {
        //Inicializácia predvolených hodnôt
        nameField.setText("Vlastný formát");
        widthField.setText("48.3");
        heightField.setText("16.9");
        columnsField.setText("4");
        rowsField.setText("16");
        leftMarginField.setText("8.4");
        rightMarginField.setText("8.4");
        topMarginField.setText("13.3");
        bottomMarginField.setText("13.3");
        horizontalGapField.setText("0");
        verticalGapField.setText("0");
        maxAddressLengthField.setText("24");

        //Pridam listener pre všetky textové polia na aktualizáciu náhľadu
        widthField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        heightField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        columnsField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        rowsField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        leftMarginField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        rightMarginField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        topMarginField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        bottomMarginField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        horizontalGapField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        verticalGapField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());

        //Inicializácia nahľadu
        updatePreview();
    }

    public void initWithFormat(LabelFormat format)
    {
        if (format != null)
        {
            nameField.setText(format.getName() != null ? format.getName() : "Vlastný formát");
            widthField.setText(String.valueOf(format.getWidth()));
            heightField.setText(String.valueOf(format.getHeight()));
            columnsField.setText(String.valueOf(format.getColumns()));
            rowsField.setText(String.valueOf(format.getRows()));
            leftMarginField.setText(String.valueOf(format.getLeftMargin()));
            rightMarginField.setText(String.valueOf(format.getRightMargin()));
            topMarginField.setText(String.valueOf(format.getTopMargin()));
            bottomMarginField.setText(String.valueOf(format.getBottomMargin()));
            horizontalGapField.setText(String.valueOf(format.getHorizontalGap()));
            verticalGapField.setText(String.valueOf(format.getVerticalGap()));
            maxAddressLengthField.setText(String.valueOf(format.getMaxAddressLength()));

            updatePreview();
        }
    }

    @FXML
    private void handleSave()
    {
        try
        {
            String name = nameField.getText().trim();
            double width = Double.parseDouble(widthField.getText());
            double height = Double.parseDouble(heightField.getText());
            int columns = Integer.parseInt(columnsField.getText());
            int rows = Integer.parseInt(rowsField.getText());
            double leftMargin = Double.parseDouble(leftMarginField.getText());
            double rightMargin = Double.parseDouble(rightMarginField.getText());
            double topMargin = Double.parseDouble(topMarginField.getText());
            double bottomMargin = Double.parseDouble(bottomMarginField.getText());
            double horizontalGap = Double.parseDouble(horizontalGapField.getText());
            double verticalGap = Double.parseDouble(verticalGapField.getText());
            int maxAddressLength = Integer.parseInt(maxAddressLengthField.getText());

            //Validácia hodnôt
            if (width <= 0 || height <= 0 || columns <= 0 || rows <= 0 || maxAddressLength <= 0)
            {
                showError("Rozmery, počty a max. dĺžka adresy musia byť kladné čísla.");
                return;
            }

            if (leftMargin < 0 || rightMargin < 0 || topMargin < 0 || bottomMargin < 0 || horizontalGap < 0 || verticalGap < 0)
            {
                showError("Okraje a medzery nemôžu byť záporné.");
                return;
            }

            //Kontrola či sa štítky zmestia na A4
            double totalWidth = leftMargin + (width * columns) + (horizontalGap * (columns - 1)) + rightMargin;
            double totalHeight = topMargin + (height * rows) + (verticalGap * (rows - 1)) + bottomMargin;

            if (totalWidth > 210.0)
            {
                showError("Štítky presahujú šírku strany A4 (210mm). Aktuálna výška: " + String.format("%.1f mm", totalWidth));
                return;
            }

            if (totalHeight > 297.0)
            {
                showError("Štítky presahujú výšku strany A4 (297mm). Aktuálna výška: " + String.format("%.1f mm", totalHeight));
                return;
            }

            //Vytvorenie nového formátu štítkov
            if (name.isEmpty())
            {
                name = String.format("Vlastný - %.1f x %.1f mm (%dx%d ks)", width, height, columns, rows);
            }

            customLabelFormat = new LabelFormat(name, width, height, columns, rows, leftMargin, rightMargin, topMargin, bottomMargin, horizontalGap, verticalGap, maxAddressLength);

            // Zatvorenie dialogu
            closeDialog();
        } catch (NumberFormatException e)
        {
            showError("Neplatný format čísla: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel()
    {
        closeDialog();
    }

    private void updatePreview()
    {
        try
        {
            //Ziskanie hodnôt polí
            double width = Double.parseDouble(widthField.getText());
            double height = Double.parseDouble(heightField.getText());
            int columns = Integer.parseInt(columnsField.getText());
            int rows = Integer.parseInt(rowsField.getText());
            double leftMargin = Double.parseDouble(leftMarginField.getText());
            double rightMargin = Double.parseDouble(rightMarginField.getText());
            double topMargin = Double.parseDouble(topMarginField.getText());
            double bottomMargin = Double.parseDouble(bottomMarginField.getText());
            double horizontalGap = Double.parseDouble(horizontalGapField.getText());
            double verticalGap = Double.parseDouble(verticalGapField.getText());

            // vykresľovanie náhľadu
            GraphicsContext gc = previewCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());

            //Okraje stránky A4
            gc.setStroke(Color.BLACK);
            gc.strokeRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());

            //Vykreslenie štítkkov
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);

            for (int row = 0; row < rows; row++)
            {
                for (int col = 0; col < columns; col++)
                {
                    double x = leftMargin + col * (width + horizontalGap);
                    double y = topMargin + row * (height + verticalGap);

                    gc.fillRect(x, y, width, height);
                    gc.strokeRect(x, y, width, height);
                }
            }
        } catch (NumberFormatException e)
        {
            //Ingnorujem pretože polia môžu byť prázdne počas úprav
        }
    }

    private void showError(String message)
    {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void closeDialog()
    {
        Stage stage = (Stage) widthField.getScene().getWindow();
        stage.close();
    }

    public LabelFormat getCustomLabelFormat()
    {
        return customLabelFormat;
    }
}
