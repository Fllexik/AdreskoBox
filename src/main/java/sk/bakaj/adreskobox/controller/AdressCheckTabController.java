package sk.bakaj.adreskobox.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sk.bakaj.adreskobox.model.LabelFormat;
import sk.bakaj.adreskobox.model.Parent;
import sk.bakaj.adreskobox.service.AbbreviationService;
import sk.bakaj.adreskobox.service.PDFService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Kontroler pre kartu kontroly a uprav adries
 */
public class AdressCheckTabController
{
    @FXML
    private VBox rootVbox;
    @FXML
    private TableView<AddressPreviewItem> addressTable;
    @FXML
    private TableColumn<AddressPreviewItem, String> nameColumn;
    @FXML
    private TableColumn<AddressPreviewItem, String> originalAddressColumn;
    @FXML
    private TableColumn<AddressPreviewItem, String> abbreviatedAddressColumn;
    @FXML
    private TableColumn<AddressPreviewItem, String> statusColumn;
    @FXML
    private Button addAbbreviationButton;
    @FXML
    private Button manageAbbreviationsButton;

    private List<Parent> parents;
    private LabelFormat labelFormat;
    private AbbreviationService abbreviationService = new AbbreviationService();
    private PDFService pdfService = new PDFService();

    private ObservableList<AddressPreviewItem> addressItems = FXCollections.observableArrayList();

    @FXML
    public void initialize()
    {
        // DÔLEŽITÉ: Uložiť controller do properties root elementu
        // Toto je kľúčové pre načítanie controllera v MainController
        if (rootVbox != null)
        {
            rootVbox.getProperties().put("controller", this);
        }
        //Nastavenie stĺpcov tabuľky
        nameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getName()));
        originalAddressColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getOriginalAddress()));
        abbreviatedAddressColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAbbreviatedAddress()));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus()));

        //Nastavenie Obsahu tabuľky
        addressTable.setItems(addressItems);

        //Nastavenie CSS pre riadky s neplatnými adresami
        addressTable.setRowFactory(tv -> {
            TableRow<AddressPreviewItem> row = new TableRow<AddressPreviewItem>() {
                @Override
                protected void updateItem(AddressPreviewItem item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setStyle("");
                    } else if (!checkIfAddressFits(item.getAbbreviatedAddress())) {
                        setStyle("-fx-background-color: #ffcccc;");
                    } else {
                        setStyle("");
                    }
                }
            };

            //Pridame kontext menu k riadku
            ContextMenu contextMenu = new ContextMenu();
            MenuItem addAbbreviationItem = new MenuItem("Pridať skratku");
            addAbbreviationItem.setOnAction(event -> showAddAbbreviationDialog(row.getItem()));

            MenuItem editAddressItem = new MenuItem("Upraviť adresu");
            editAddressItem.setOnAction(event -> showEditAddressDialog(row.getItem()));

            contextMenu.getItems().addAll(addAbbreviationItem, editAddressItem);

            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        //Nastavenie akcie pre tlačidlo na pridanie skratky
        addAbbreviationButton.setOnAction(event -> showAddAbbreviationDialog(null));

        //Nastavenie akcie pre tlačidlo na spravu skratiek
        if (manageAbbreviationsButton != null) {
            manageAbbreviationsButton.setOnAction(event -> showManageAbbreviationsDialog());
        }
    }

    /**
     * Nastavenie dát z predchadzajúcej karty
     */
    public void setData(List<Parent> parents, LabelFormat labelFormat) {
        this.parents = parents;
        this.labelFormat = labelFormat;
        processAddresses();
    }

    /**
     * Spracovanie a vyhodnotenie adries
     */
    private void processAddresses() {
        addressItems.clear();

        if (parents == null || labelFormat == null) {
            return;
        }

        for (Parent parent : parents) {
            String originalAddress = parent.getFullAddress();
            //Použitie abbreviationService na ziskanie najlepšej skratky
            String abbreviatedAddress = abbreviationService.getBestAbbreviation(
                    originalAddress,
                    labelFormat.getMaxAddressLength(),
                    pdfService
            );

            boolean fits = checkIfAddressFits(abbreviatedAddress);

            AddressPreviewItem item = new AddressPreviewItem(
                    parent.getFullName(),
                    originalAddress,
                    abbreviatedAddress,
                    fits ? "Vyhovuje" : "Nevyhovuje - príliš dlhá"
            );

            addressItems.add(item);
        }
    }

    /**
     * Kontrola, či sa adresa zmesti na štítok
     */
    private boolean checkIfAddressFits(String address) {
        return address != null && labelFormat != null && address.length() <= labelFormat.getMaxAddressLength();
    }

    /**
     * Zobrazenie dialogu pre pridanie novej skratky
     */
    private void showAddAbbreviationDialog(AddressPreviewItem item) {
        try {
            Dialog<Map.Entry<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Pridať skratku");
            dialog.setHeaderText("Zadajte novú skratku pre adresu");

            //Pridanie tlačidiel
            ButtonType saveButtonType = new ButtonType("Uložiť", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            //Vytvorenie Formulara
            TextField originalTextField = new TextField();
            TextField abbreviatedTextField = new TextField();

            // AK máme vybranú položku, predvyplníme originálny text
            if (item != null) {
                originalTextField.setText(item.getOriginalAddress());
            }

            //Vytvorenie Layoutu
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Pôvodný text:"), 0, 0);
            grid.add(originalTextField, 1, 0);
            grid.add(new Label("Skratka:"), 0, 1);
            grid.add(abbreviatedTextField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            //Konvertor výsledku
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    String original = originalTextField.getText().trim();
                    String abbreviation = abbreviatedTextField.getText().trim();

                    if (!original.isEmpty() && !abbreviation.isEmpty()) {
                        return Map.entry(original, abbreviation);
                    }
                }
                return null;
            });

            //Zobrazenie dialogu a spracovanie výsledku
            Optional<Map.Entry<String, String>> result = dialog.showAndWait();

            result.ifPresent(entry -> {
                //Pridanie novej skratky a uloženie do súboru
                abbreviationService.saveAbbreviation(entry.getKey(), entry.getValue());

                //Obnovenie zobrazenia
                processAddresses();

                //Zobraziť potvrdenie
                showAlert(Alert.AlertType.INFORMATION, "Skratka pridaná",
                        "Skratka bola úspešne pridaná a bude automaticky použitá.");
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba",
                    "Nepodarilo sa zobraziť dialog: " + e.getMessage());
        }
    }

    /**
     * Zobrazenie dialogu pre úpravu skratenej adresy
     */
    private void showEditAddressDialog(AddressPreviewItem item) {
        if (item == null) return;

        try {
            TextInputDialog dialog = new TextInputDialog(item.getAbbreviatedAddress());
            dialog.setTitle("Upraviť adresu");
            dialog.setHeaderText("Upravte skatenú adresu pre: " + item.getName());
            dialog.setContentText("Adresa");

            dialog.showAndWait().ifPresent(newAddress -> {
                if (!newAddress.isEmpty()) {
                    item.setAbbreviatedAddress(newAddress);

                    //Aktualizácia stavu
                    boolean fits = checkIfAddressFits(newAddress);
                    item.setStatus(fits ? "Vyhovuje" : "Nevyhovuje - príliš dlhá");

                    //Obnovenie tabuľky
                    addressTable.refresh();
                }
            });
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba",
                    "Nepodarilo sa zobraziť dialog: " + e.getMessage());
        }
    }

    /**
     * Zobrazenie dialogu pre správu všetkých skratiek
     */
    private void showManageAbbreviationsDialog() {
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Správa skratiek");
            dialog.setHeaderText("Správa existujucích skratiek");

            //Pridanie tlačidiel
            ButtonType closeButtonType = new ButtonType("Zavrieť", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButtonType);

            //Vytvorenie tabuľky skratiek
            TableView<Map.Entry<String, String>> abbreviationsTable = new TableView<>();
            abbreviationsTable.setPrefHeight(300);
            abbreviationsTable.setPrefWidth(400);

            TableColumn<Map.Entry<String, String>, String> originalColumn = new TableColumn<>("Pôvodný text");
            originalColumn.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getKey()));
            originalColumn.setPrefWidth(200);

            TableColumn<Map.Entry<String, String>, String> abbreviationColumn = new TableColumn<>("Skretka");
            abbreviationColumn.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getValue()));
            abbreviationColumn.setPrefWidth(100);

            abbreviationsTable.getColumns().addAll(originalColumn, abbreviationColumn);

            //Naplnenie tabuľky datami
            ObservableList<Map.Entry<String, String>> abbreviationItems =
                    FXCollections.observableArrayList(abbreviationService.getAllAbbreviations().entrySet());
            abbreviationsTable.setItems(abbreviationItems);

            //Pridanie tlačidiel pre správu
            Button addButton = new Button("Pridať");
            Button editButton = new Button("Upraviť");
            Button deleteButton = new Button("Odstraniť");

            addButton.setOnAction(event -> {
                //Použijeme existujeci dialog pre pridanie skratky
                Dialog<Map.Entry<String, String>> addDialog = new Dialog<>();
                addDialog.setTitle("Pridať skratku");
                addDialog.setHeaderText("Zadajte novú skratku");

                //Pridanie tlačidiel
                ButtonType saveButtonType = new ButtonType("Uložiť", ButtonBar.ButtonData.OK_DONE);
                addDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                //Vytvarenie Formulara
                TextField originalTextField = new TextField();
                TextField abbreviatedTextField = new TextField();

                //Vytvorenie Layoutu
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.add(new Label("Pôvodný text:"), 0, 0);
                grid.add(originalTextField, 1, 0);
                grid.add(new Label("Skratka:"), 0, 1);
                grid.add(abbreviatedTextField, 1, 1);

                addDialog.getDialogPane().setContent(grid);

                //Konvertor výsledku
                addDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        String original = originalTextField.getText().trim();
                        String abbreviation = abbreviatedTextField.getText().trim();

                        if (!original.isEmpty() && !abbreviation.isEmpty()) {
                            return Map.entry(original, abbreviation);
                        }
                    }
                    return null;
                });

                //Zobrazenie dialogu a spracovanie výsledku
                Optional<Map.Entry<String, String>> result = addDialog.showAndWait();

                result.ifPresent(entry -> {
                    //Pridanie novej skratky a uloženie do súboru
                    abbreviationService.saveAbbreviation(entry.getKey(), entry.getValue());

                    //Obnovime zoznam v tabuľke
                    abbreviationItems.clear();
                    abbreviationItems.addAll(abbreviationService.getAllAbbreviations().entrySet());
                    abbreviationsTable.refresh();
                });
            });

            editButton.setOnAction(event -> {
                Map.Entry<String, String> selected = abbreviationsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    TextInputDialog editDialog = new TextInputDialog(selected.getValue());
                    editDialog.setTitle("Upraviť skratku");
                    editDialog.setHeaderText("Upravte skratku pre: " + selected.getKey());
                    editDialog.setContentText("Skratka");

                    editDialog.showAndWait().ifPresent(newAbbreviation -> {
                        if (!newAbbreviation.isEmpty()) {
                            abbreviationService.saveAbbreviation(selected.getKey(), newAbbreviation);

                            //Obnovime zoznam v tabulke
                            abbreviationItems.clear();
                            abbreviationItems.addAll(abbreviationService.getAllAbbreviations().entrySet());
                            abbreviationsTable.refresh();
                        }
                    });
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Informácia",
                            "Vyberte skratku, ktoru chcete upraviť.");
                }
            });

            deleteButton.setOnAction(event -> {
                Map.Entry<String, String> selected = abbreviationsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Potvrdenie");
                    confirmAlert.setHeaderText("Odstranenie skratky");
                    confirmAlert.setContentText("Naozaj chcete odstraniť skratku '" + selected.getKey() + "'?");

                    confirmAlert.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            abbreviationService.removeAbbreviation(selected.getKey());

                            //Obnovime zoznam v tabulke
                            abbreviationItems.clear();
                            abbreviationItems.addAll(abbreviationService.getAllAbbreviations().entrySet());
                            abbreviationsTable.refresh();
                        }
                    });
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Informácia",
                            "Vyberte skratku, ktoru chcete odstrániť.");
                }
            });

            //Vytvorenie layout
            VBox vbox = new VBox(10);
            HBox buttonBox = new HBox(10);
            buttonBox.getChildren().addAll(addButton, editButton, deleteButton);
            vbox.getChildren().addAll(abbreviationsTable, buttonBox);

            dialog.getDialogPane().setContent(vbox);
            dialog.getDialogPane().setPrefSize(500, 400);
            dialog.showAndWait();

            //PO zatvorení dialogu obnovíme adresy
            processAddresses();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Chyba",
                    "Nepodarilo sa zobraziť dialog: " + e.getMessage());
        }
    }

    /**
     * Ziskanie zoznamu rodičov s upavenými adresami
     */
    public List<Parent> getParentsWithAbbreviatedAddresses() {
        List<Parent> result = new ArrayList<>();

        if (parents == null || addressItems.isEmpty()) {
            return result;
        }

        for (int i = 0; i < parents.size() && i < addressItems.size(); i++) {
            Parent parent = parents.get(i);
            AddressPreviewItem item = addressItems.get(i);

            result.add(new Parent(parent.getFullName(), item.getAbbreviatedAddress()));
        }
        return result;
    }

    /**
     * Zobrazenie alertu
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Trieda reprezentuje položku v tabuľke adries
     */
    public static class AddressPreviewItem {
        private final String name;
        private final String originalAddress;
        private String abbreviatedAddress;
        private String status;

        public AddressPreviewItem(String name, String originalAddress, String abbreviatedAddress, String status) {
            this.name = name;
            this.originalAddress = originalAddress;
            this.abbreviatedAddress = abbreviatedAddress;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public String getOriginalAddress() {
            return originalAddress;
        }

        public String getAbbreviatedAddress() {
            return abbreviatedAddress;
        }

        public void setAbbreviatedAddress(String abbreviatedAddress) {
            this.abbreviatedAddress = abbreviatedAddress;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}