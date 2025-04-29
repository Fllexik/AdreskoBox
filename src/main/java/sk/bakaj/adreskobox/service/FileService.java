package sk.bakaj.adreskobox.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import sk.bakaj.adreskobox.model.ImportedData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileService
{
    public static final String STUDENT_FIRSTNAME_COLUMN = "Meno";
    public static final String STUDENT_LASTNAME_COLUMN = "Priezvisko";
    public static final String PARENT1_NAME_COLUMN = "Rodič 1.";
    public static final String PARENT2_NAME_COLUMN = "Rodič 2.";
    public static final String ADDRESS1_COLUMN = "Adresa 1.";
    public static final String ADDRESS2_COLUMN = "Adresa 2.";

    public List<ImportedData> readFile(File file, String delimiter) throws IOException
    {
        if (file.getName().toLowerCase().endsWith(".csv"))
        {
            return readCSV(file, delimiter);
        }else
        {
            return readExcel(file);
        }
    }
}

private List<ImportedData> readCSV(File file, String delimiter) throws IOException
{
    List<ImportedData> dataList = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader()))
    {
        String headliner = br.readLine();
        String[] headers = headliner.split(delimiter);
        int[] columnIndexes = findColumnIndexes(headers);

        String line;
        while ((line = br.readLine()) != null)
        {
            String[] values = line.split(delimiter);
            dataList.add(createImportedData(values, columnIndexes));
        }
    }
    return dataList;
}

private List<ImportedData> readExcel(File file) throws IOException
{
    List<ImportedData> dataList = new ArrayList<>();
    try(Workbook workbook = WorkbookFactory.create(file))
    {
        Sheet sheet = workbook.getSheet(0);
        Row headerRow = sheet.getRow(0);

        String[] headers = new String[headerRow.getLastCellNum()];
        for (int i = 0; i < headerRow.getLastCellNum(); i++)
        {
            headers[i] = getCellValueAsString(headerRow.getCell(i));
        }
        int[] columnIdexes = findColumnIndexes(headers);

        for (int i = 0; i <= sheet.getLastRowNum(); i++)
        {
            Row row = sheet.getRow(i);
            if (row != null)
            {
                String[] values = new String[columnIdexes.length];
                for (int j = 0; j < columnIdexes.length; j++)
                {
                    if (columnIdexes[j] != -1)
                    {
                        values[j] = getCellValueAsString(row.getCell(columnIdexes[j]));
                    }
                }
                dataList.add(createImportedData(values, columnIdexes));
            }
        }
    }
    return dataList;
}
private String[] findColumnIndexes(String[] headers) {
    // implementácia
}

private String getCellValueAsString(Cell cell) {
    if (cell == null) return "";
    switch (cell.getCellType()) {
        case STRING: return cell.getStringCellValue().trim();
        case NUMERIC: return String.valueOf(cell.getNumericCellValue()).trim();
        default: return "";
    }
}


private ImportedData createImportedData(String[] values, int[] columnIndexes)
{
    ImportedData data = new ImportedData();
    if (columnIndexes[0] != -1) data.setStudentFirstName(values[columnIndexes[0]]);
    if (columnIndexes[1] != -1) data.setStudentLastName(values[columnIndexes[1]]);
    if (columnIndexes[2] != -1) data.setParent1Name(values[columnIndexes[2]]);
    if (columnIndexes[3] != -1) data.setParent2Name(values[columnIndexes[3]]);
    if (columnIndexes[4] != -1) data.setAddress1(values[columnIndexes[4]]);
    if (columnIndexes[5] != -1) data.setAddress2(values[columnIndexes[5]]);
    return data;
}