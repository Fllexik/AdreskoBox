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

    private ObservableList<ParentEntry> parentList = FXCollections.observableArrayList();


    @FXML
    public void initialize()
    {
        System.out.println("ParentsTabController.initialize()");
        System.out.println("Stĺpce v parentsTable: " + parentsTable.getColumns().size());
        System.out.println("Riadkov v parentList: " + parentList.size());
        parentList.add(new ParentEntry("Test Student", "Test Parent", "Test Address", false));
        parentsTable.setItems(parentList);
        // DÔLEŽITÉ: Uložiť controller do properties root elementu
        // Toto je kľúčové pre načítanie controllera v MainController
        if (rootVbox != null)
        {
            rootVbox.getProperties().put("controller", this);
        }

        //Nastavenie stlpcov tabulky
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        studentColumn.setCellValueFactory( cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentName()));

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getParentName()));

        addressColumn.setCellValueFactory( cellData ->
                new SimpleStringProperty(cellData.getValue().getAddress()));

        //Nastavenie editovateľnosti tabuľky
        parentsTable.setEditable(true);

        //Nastavenie parentsTable pre zobrazenie udajov
        parentsTable.setItems(parentList);

        parentsTable.setPlaceholder(new Label("Tabuľka je prázdna"));
    }
    /**
     * Aktualizuje počet vybraných rodičov
     */
    private void updateSelectedCount()
    {
        long selectedCount = parentList.stream()
                .filter(ParentEntry::isSelected)
                .count();
        selectedCountLabel.setText(String.valueOf(selectedCount));
    }
    /**
     * Načita rodičov z importovaných dát
     */
    public void loadData(List<ImportedData> importedDataList) {
        System.out.println("Volám loadData, veľkosť dát: " + importedDataList.size());
        parentList.clear();
        int addedCount = 0; // Nový počítadlo

        for (ImportedData data : importedDataList) {
            String studentName = data.getStudentFirstName() + " " + data.getStudentLastName();

            // Pridať prveho rodiča
            if (data.getParent1Name() != null && !data.getParent1Name().isEmpty()) {
                ParentEntry entry = new ParentEntry(
                        studentName,
                        data.getParent1Name(),
                        data.getAddress1(),
                        false
                );
                parentList.add(entry);
                addedCount++; // Inkrementujeme počítadlo
                System.out.println("Pridaný Rodič 1 pre študenta: " + studentName + ", Meno: " + data.getParent1Name()); // Ladenie
                entry.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedCount());
            } else {
                System.out.println("Rodič 1 je prázdny pre študenta: " + studentName); // Ladnie
            }

            //Pridať druheho rodiča
            if (data.getParent2Name() != null && !data.getParent2Name().isEmpty()) {
                ParentEntry entry = new ParentEntry(
                        studentName,
                        data.getParent2Name(),
                        data.getAddress2(),
                        false
                );
                parentList.add(entry);
                addedCount++; // Inkrementujeme počítadlo
                System.out.println("Pridaný Rodič 2 pre študenta: " + studentName + ", Meno: " + data.getParent2Name()); // Ladnenie
                entry.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedCount());
            } else {
                System.out.println("Rodič 2 je prázdny pre študenta: " + studentName); // Ladnie
            }
        }
        System.out.println("Celkový počet riadkov pridaných do parentList: " + addedCount); // Nový výpis
        updateSelectedCount();
        // Požiadajte o explicitné obnovenie tabuľky, ak to automaticky nefunguje
        parentsTable.refresh();
    }

    /**
     * Spracuje kliknutie na tlačidlo vybrať všetkych
     */

    @FXML
    private void handleSelectAll()
    {
        for (ParentEntry entry : parentList)
        {
            entry.setSelected(true);
        }
        parentsTable.refresh();
        updateSelectedCount();
    }

    /**
     * Spracuje kliknutie na tlačidlo odznačiť všetkych
     */

    @FXML
    private void handleUnselectAll()
    {
        for (ParentEntry entry : parentList)
        {
            entry.setSelected(false);
        }
        parentsTable.refresh();
        updateSelectedCount();
    }

    /**
     * Ziskať zoznam vybraných rodičov
     */

    public List<Parent> getSelectedParents()
    {
        return parentList.stream()
                .filter(ParentEntry::isSelected)
                .map(entry -> new Parent(entry.getParentName(), entry.getAddress()))
                .collect(Collectors.toList());
    }

    /**
     * Trieda reprezentujuca riadok v tabuľke rodičov
     */

    public static class ParentEntry
    {
        private final String studentName;
        private final String parentName;
        private final String address;
        private final SimpleBooleanProperty selected;

        public ParentEntry(String studentName, String parentName, String address, boolean selected)
        {
            this.studentName = studentName;
            this.parentName = parentName;
            this.address = address;
            this.selected = new SimpleBooleanProperty(selected);
        }

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
