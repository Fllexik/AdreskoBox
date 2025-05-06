package sk.bakaj.adreskobox.service;

import org.apache.poi.ss.usermodel.*;
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

    public String detectFileDelimiter(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".csv")) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();
                if (line != null) {
                    if (line.contains(",")) return ",";
                    if (line.contains(";")) return ";";
                    if (line.contains("\t")) return "\t";
                }
            }
        }
        return ",";
    }


    public List<ImportedData> readFile(File file, String delimiter) throws IOException
    {
        if (file.getName().toLowerCase().endsWith(".csv"))
        {
            return readCSV(file, delimiter);
        } else
        {
            return readExcel(file);
        }
    }

    private List<ImportedData> readCSV(File file, String delimiter) throws IOException
    {
        List<ImportedData> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String headerLine = br.readLine();
            if (headerLine == null)
            {
                throw new IOException("Súbor je prázdny");
            }

            String[] headers = headerLine.split(delimiter);
            int[] columnIndexes = findColumnIndexes(headers);

            String line;
            while ((line = br.readLine()) != null)
            {
                String[] values = line.split(delimiter);
                ImportedData data = createImportedData(values, columnIndexes);
                if (data != null)
                {
                    dataList.add(data);
                }
            }
        }
        return dataList;
    }

    private List<ImportedData> readExcel(File file) throws IOException
    {
        List<ImportedData> dataList = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null)
            {
                throw new IOException("Súbor je prázdny alebo neobsahuje hlavičku");
            }

            String[] headers = new String[headerRow.getLastCellNum()];
            for (int i = 0; i < headerRow.getLastCellNum(); i++)
            {
                Cell cell = headerRow.getCell(i);
                headers[i] = getCellValueAsString(cell);
            }
            int[] columnIndexes = findColumnIndexes(headers);

            for (int i = 0; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row != null)
                {
                    String[] values = new String[headers.length];
                    for (int j = 0; j < headers.length; j++)
                    {
                        Cell cell = row.getCell(j);
                        values[j] = getCellValueAsString(cell);
                    }
                    ImportedData data = createImportedData(values, columnIndexes);
                    if (data != null)
                    {
                        dataList.add(data);
                    }
                }
            }
        }
        return dataList;
    }

    private String[] findColumnIndexes(String[] headers)
    {
        int[] indexes = new int[6];
        for (int i = 0; i < indexes.length; i++)
        {
            indexes[i] = -1;
        }

        for (int i = 0; i < headers.length; i++)
        {
            String header = headers[i];
            if (header == null) continue;

            if (header.equalsIgnoreCase(STUDENT_FIRSTNAME_COLUMN))
            {
                indexes[0] = i;
            } else if (header.equalsIgnoreCase(STUDENT_LASTNAME_COLUMN))
            {
                indexes[1] = i;
            } else if (header.equalsIgnoreCase(PARENT1_NAME_COLUMN))
            {
                indexes[2] = i;
            } else if (header.equalsIgnoreCase(PARENT2_NAME_COLUMN))
            {
                indexes[3] = i;
            } else if (header.equalsIgnoreCase(ADDRESS1_COLUMN))
            {
                indexes[4] = i;
            } else if (header.equalsIgnoreCase(ADDRESS2_COLUMN))
            {
                indexes[5] = i;
            }
        }
        return indexes;
    }

    private String getCellValueAsString(Cell cell)
    {
        if (cell == null) return "";
        switch (cell.getCellType())
        {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue()).trim();
            default:
                return "";
        }
    }

    private ImportedData createImportedData(String[] values, int[] columnIndexes)
    {
        if (values.length == 0) return null;
        ImportedData data = new ImportedData();
        if (columnIndexes[0] != -1 && columnIndexes[0] < values.length)
        {
            data.setStudentFirstName(values[columnIndexes[0]]);
        }
        if (columnIndexes[1] != -1 && columnIndexes[1] < values.length)
        {
            data.setStudentLastName(values[columnIndexes[1]]);
        }
        if (columnIndexes[2] != -1 && columnIndexes[2] < values.length)
        {
            data.setParent1Name(values[columnIndexes[2]]);
        }
        if (columnIndexes[3] != -1 && columnIndexes[3] < values.length)
        {
            data.setParent2Name(values[columnIndexes[3]]);
        }
        if (columnIndexes[4] != -1 && columnIndexes[4] < values.length)
        {
            data.setAddress1(values[columnIndexes[4]]);
        }
        if (columnIndexes[5] != -1 && columnIndexes[5] < values.length)
        {
            data.setAddress2(values[columnIndexes[5]]);
        }

        return data;
    }
}