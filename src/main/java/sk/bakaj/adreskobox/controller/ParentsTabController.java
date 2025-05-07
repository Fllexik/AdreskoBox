package sk.bakaj.adreskobox.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import sk.bakaj.adreskobox.model.ImportedData;
import sk.bakaj.adreskobox.model.Parent;

import javax.swing.text.TabableView;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ParentsTabController
{
    @FXML
    private TabableView<ParentEntry> parentsTable;

    @FXML
    private TableColumn<ParentEntry, Boolean> selectColumn;

    @FXML
    private TableColumn<ParentEntry, String> studentColumn;

    @FXML
    private TableColumn<ParentEntry, String> nameColumn;

    @FXML
    private TableColumn<ParentEntry, String> adressColumn;

    @FXML
    private Label selectedCountLabel;

    private ObservableList<ParentEntry> parentList = FXCollections.observableArrayList();

    @FXML
    public void initialize()
    {
        //Nastavenie stlpcov tabulky
        selectColumn.setCellValueFactory(CellDataFeatures<ParentEntry, Boolean> cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        studentColumn.setCellValueFactory(CellDataFeatures<ParentEntry, String> cellData ->
                new SimpleStringProperty(cellData.getValue().getStudentName()));

        nameColumn.setCellValueFactory(CellDataFeatures<ParentEntry, String> cellData ->
                new SimpleStringProperty(cellData.getValue().getParentName()));

        adressColumn.setCellValueFactory(CellDataFeatures<ParentEntry, String> cellData ->
                new SimpleStringProperty(cellData.getValue().getAddress()));

        //Nastavenie editovateľnosti tabuľky
        parentsTable.setEditable(true);

        //Nastavenie parentsTable pre zobrazenie udajov
        parentsTable.setItems(parentList);
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
    public void loadData(List<ImportedData> importedDataList)
    {
        parentList.clear();

        for (ImportedData data : importedDataList)
        {
            String studentName = data.getStudentFirstName() + " " + data.getStudentLastName();

            // Pridať prveho rodiča
            if (data.getParent1Name() != null && !data.getParent1Name().isEmpty())
            {
                ParentEntry entry = new ParentEntry(
                        studentName,
                        data.getParent1Name(),
                        data.getAddress1(),
                        false
                );
                parentList.add(entry);

                //Pridaj listener na checkBox
                entry.selectedProperty().addListener((obs, oldVal, newVal) ->
                        {updateSelectedCount();}
                );
            }

            //Pridať druheho rodiča
            if (data.getParent2Name() != null && !data.getParent2Name().isEmpty())
            {
                ParetEntry entry = ParentEntry(
                        studentName,
                        data.getParent2Name(),
                        data.getAddress2(),
                        false
                );
                parentList.add(entry);

                //Pridaj listener na chcekBox
                entry.selectedProperty().addListener((obs, oldVal, newVal) ->
                {
                    updateSelectedCount();
                });
            }
        }

        //Aktualizujem počet vybraných rodičov
        updateSelectedCount();
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

}
