package sk.bakaj.adreskobox.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import sk.bakaj.adreskobox.model.ImportedData;
import sk.bakaj.adreskobox.model.Parent;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller pre záložku správy rodičov.
 * Umožňuje zobrazenie, výber a správu zoznamu rodičov načítaných z importovaných dát.
 */
public class ParentsTabController
{
    @FXML
    private VBox rootVbox;

    @FXML
    private TableView<ParentEntry> parentsTable;

    @FXML
    private TableColumn<ParentEntry, Boolean> selectColumn;

    @FXML
    private TableColumn<ParentEntry, String> studentColumn;

    @FXML
    private TableColumn<ParentEntry, String> nameColumn;

    @FXML
    private TableColumn<ParentEntry, String> addressColumn;

    @FXML
    private Label selectedCountLabel;

    /** Observable zoznam všetkých rodičov pre tabuľku */
    private ObservableList<ParentEntry> parentList = FXCollections.observableArrayList();

    /**
     * Inicializácia controllera.
     * Nastavuje cell factories pre stĺpce tabuľky a základné vlastnosti.
     */
    @FXML
    public void initialize()
    {
        // Uloženie controllera do properties root elementu pre prístup z MainController
        if (rootVbox != null)
        {
            rootVbox.getProperties().put("controller", this);
        }

        setupTableColumns();
        setupTableProperties();

        // Nastavenie obsahu tabuľky
        parentsTable.setItems(parentList);
        updateSelectedCount();
    }

    /**
     * Nastavuje cell factories pre všetky stĺpce tabuľky.
     */
    private void setupTableColumns()
    {

        // Stĺpec pre výber rodičov (checkbox)
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        // Stĺpec pre meno študenta
        studentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentName()));

        // Stĺpec pre meno rodiča
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getParentName()));

        // Stĺpec pre adresu
        addressColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAddress()));
    }
    /**
     * Nastavuje základné vlastnosti tabuľky.
     */
    private void setupTableProperties()
    {
        parentsTable.setEditable(true);
        parentsTable.setPlaceholder(new Label("Žiadni rodičia nie su načítaní"));
    }

    /**
     * Aktualizuje počítadlo vybraných rodičov v používateľskom rozhraní.
     */
    private void updateSelectedCount()
    {
        long selectedCount = parentList.stream()
                .filter(ParentEntry::isSelected)
                .count();
        selectedCountLabel.setText(String.valueOf(selectedCount));
    }
    /**
     * Načíta zoznam rodičov z importovaných dát.
     * Pre každý záznam vytvorí záznamy pre rodiča 1 a rodiča 2 (ak existujú).
     *
     * @param importedDataList zoznam importovaných dát zo súboru
     */
    public void loadData(List<ImportedData> importedDataList)
    {
        parentList.clear();

        for (ImportedData data : importedDataList)
        {
            String studentName = data.getStudentFirstName() + " " + data.getStudentLastName();

            // Pridanie prvého rodiča ak existuje
            addParentIfExists(studentName, data.getParent1Name(), data.getAddress1());

            // Pridanie druhého rodiča ak existuje
            addParentIfExists(studentName, data.getParent2Name(), data.getAddress2());
        }

        updateSelectedCount();
        parentsTable.refresh();
    }

    /**
     * Pridá rodiča do zoznamu ak má platné údaje.
     *
     * @param studentName meno študenta
     * @param parentName meno rodiča
     * @param address adresa rodiča
     */
    private void addParentIfExists(String studentName, String parentName, String address)
    {
        if (parentName != null && !parentName.trim().isEmpty())
        {
            ParentEntry entry = new ParentEntry(studentName, parentName, address, false);
            parentList.add(entry);

            // Pridanie listener-a pre aktualizáciu počítadla pri zmene výberu
            entry.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedCount());
        }
    }

    /**
     * Označí všetkých rodičov ako vybraných.
     */
    @FXML
    private void handleSelectAll()
    {
        parentList.forEach(entry -> entry.setSelected(true));
        parentsTable.refresh();
        updateSelectedCount();
    }

    /**
     * Zruší výber všetkých rodičov.
     */
    @FXML
    private void handleUnselectAll()
    {
        parentList.forEach(entry -> entry.setSelected(false));
        parentsTable.refresh();
        updateSelectedCount();
    }

    /**
     * Vráti zoznam aktuálne vybraných rodičov.
     *
     * @return zoznam vybraných rodičov ako objekty typu Parent
     */
    public List<Parent> getSelectedParents()
    {
        return parentList.stream()
                .filter(ParentEntry::isSelected)
                .map(entry -> new Parent(entry.getParentName(), entry.getAddress()))
                .collect(Collectors.toList());
    }

    /**
     * Trieda reprezentujúca jeden riadok v tabuľke rodičov.
     * Obsahuje informácie o študentovi, rodičovi a jeho adrese.
     */
    public static class ParentEntry
    {
        private final String studentName;
        private final String parentName;
        private final String address;
        private final SimpleBooleanProperty selected;

        /**
         * Konštruktor pre vytvorenie nového záznamu rodiča.
         *
         * @param studentName meno študenta
         * @param parentName meno rodiča
         * @param address adresa rodiča
         * @param selected či je rodič označený ako vybraný
         */
        public ParentEntry(String studentName, String parentName, String address, boolean selected)
        {
            this.studentName = studentName;
            this.parentName = parentName;
            this.address = address;
            this.selected = new SimpleBooleanProperty(selected);
        }

        // Getter metódy
        public String getStudentName()
        {
            return studentName;
        }

        public String getParentName()
        {
            return parentName;
        }

        public String getAddress()
        {
            return address;
        }

        public boolean isSelected()
        {
            return selected.get();
        }

        public void setSelected(boolean selected)
        {
            this.selected.set(selected);
        }

        public SimpleBooleanProperty selectedProperty()
        {
            return selected;
        }
    }

}
