package sk.bakaj.adreskobox.service;

import org.apache.poi.ss.usermodel.*;
import sk.bakaj.adreskobox.model.ImportedData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileService
{
    public static final String STUDENT_FIRSTNAME_COLUMN = "Meno";
    public static final String STUDENT_LASTNAME_COLUMN = "Priezvisko";
    public static final String PARENT1_NAME_COLUMN = "Rodič 1.";
    public static final String PARENT2_NAME_COLUMN = "Rodič 2.";
    public static final String ADDRESS1_COLUMN = "Adresa 1.";
    public static final String ADDRESS2_COLUMN = "Adresa 2.";

    /**
     * Detekuje typ a vrati ho ako reťazec: "CSV", "XLS", XLXS" alebo null
     */
    public String detectFileType(File file)
    {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".csv"))
        {
            return "CSV";
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))
        {
            return "XLS";
        } else
        {
            return null;
        }
    }

    /**
     * Detekuje oddeľovač pre CSV súbor
     */

    public String detectFileDelimiter(File file) throws IOException
    {
        String fileName = file.getName().toLowerCase();

        //Ak to nie je CSV súbor, vratime null
        if (!fileName.endsWith(".csv"))
        {
            return null;
        }

        //Definujeme potencialne oddeľovače a ich počty v súbore
        String[] commonDelimiters = {",", ";", "\t", "|"};
        Map<String, Integer> delimiterCounts = new HashMap<>();

        for (String delimiter : commonDelimiters)
        {
            delimiterCounts.put(delimiter, 0);
        }

        //Prečítame prvých 10 riadkov(alebo menej, ak je súbor kratši)
        try(BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null && lineCount < 10)
            {
                for (String delimiter : commonDelimiters)
                {
                    int count = countOccurrences(line, delimiter);
                    delimiterCounts.put(delimiter, delimiterCounts.get(delimiter) + count);
                }
                lineCount++;
            }
        }
        //Vyberiem oddeľovač s najväčším počtom vyskytov
        String bestDelimiter = ","; //Predvolený oddeľovač je čiarka
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : delimiterCounts.entrySet())
        {
            if (entry.getValue() > maxCount)
            {
                maxCount = entry.getValue();
                bestDelimiter = entry.getKey();
            }
        }
        // AK sme nenašli žiadený oddeľovač, vratime predvolený
        return maxCount > 0 ? bestDelimiter : ",";
    }

    /**
     * Počíta výskyty oddeľovača v reťazci
     */

    private int countOccurrences(String text, String delimiter)
    {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(delimiter, index)) != -1)
        {
            count++;
            index += delimiter.length();
        }
        return count;
    }

    /**
     * Overí či prvý riadok v súbore je hlavička
     * Kontroluje, či obsahuje očakávané hlavičky stlpcov
     */

    public boolean hasHeaderRow(File file) throws IOException
    {
        String fileType = detectFileType(file);

        if ("CSV".equals(fileType))
        {
            String delimiter = detectFileDelimiter(file);
            return checkCSVHeaderRow(file, delimiter);
        } else if ("XLS".equals(fileType) || "XLSX".equals(fileType))
        {
            return checkExcelHeaderRow(file);
        }
        return false;
    }

    /**
     * Kontroluje, či prvý riadok CSV súboru obsahuje očakavané stlpce
     */

    private boolean checkCSVHeaderRow(File file, String delimiter) throws IOException
    {
        try(BufferedReader br = new BufferedReader(new FileReader(file)))
        {
            String headerLine = br.readLine();
            if (headerLine == null)
            {
                return false;//Prazdný subor
            }
            String[] headers = headerLine.split(delimiter);
            return containsExpectedColumns(headers);
        }
    }

    /**
     * Kontroluje , či prvý riadok Excel suboru obsahuje očakávane stlpce
     */

    private boolean checkExcelHeaderRow(File file) throws IOException
    {
        try(Workbook workbook = WorkbookFactory.create(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null)
            {
                return false;//Prazdný hárok
            }

            String[] headers = new String[headerRow.getLastCellNum()];
            for (int i = 0; i < headerRow.getLastCellNum(); i++)
            {
                Cell cell = headerRow.getCell(i);
                headers[i] = getCellValueAsString(cell);
            }
            return containsExpectedColumns(headers);
        }
    }

    /**
     * Kontroluje, či pole hlavičiek obsahuje aspoň jeden očakávany stlpec
     */
    private boolean containsExpectedColumns(String[] headers)
    {
        if (headers == null || headers.length == 0)
        {
            return false;
        }
        //Kontroluje ci  hlavička obsahuje aspoň jeden z očakávanych stlpcov
        for (String header : headers)
        {
            if (header == null) continue;

            if (header.equalsIgnoreCase(STUDENT_FIRSTNAME_COLUMN) ||
                    header.equalsIgnoreCase(STUDENT_LASTNAME_COLUMN) ||
                    header.equalsIgnoreCase(PARENT1_NAME_COLUMN) ||
                    header.equalsIgnoreCase(PARENT2_NAME_COLUMN) ||
                    header.equalsIgnoreCase(ADDRESS1_COLUMN) ||
                    header.equalsIgnoreCase(ADDRESS2_COLUMN))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Načíta data zo súboru, automaticky detekuje typ a oddeľovač
     *  Ak prvý riadok nie je hlavička, može volajúci špecifikovať hasHeader=false
     */
    public List<ImportedData> readFile(File file, boolean hasHeader) throws IOException
    {
        String fileType = detectFileType(file);

        if ("CSV".equals(fileType))
        {
            String delimiter = detectFileDelimiter(file);
            return readCSV(file, delimiter, hasHeader);
        } else if ("XLS".equals(fileType) || "XLSX".equals(fileType))
        {
            return readExcel(file, hasHeader);
        } else
        {
            throw new IOException("Nepodporovaný typ súboru");
        }
    }

    /**
     * Načíta data zo súboru s automatickou detekciou hlavičky
     */
    public List<ImportedData> readFile(File file) throws IOException
    {
        boolean hasHeader = hasHeaderRow(file);
        return readFile(file, hasHeader);
    }

    private List<ImportedData> readCSV(File file, String delimiter, boolean hasHeader) throws IOException
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

            if (!hasHeader)
            {
                String[] values = headerLine.split(delimiter);
                ImportedData data = createImportedData(values, columnIndexes);
                if (data != null)
                {
                    dataList.add(data);
                }
            }

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

    private List<ImportedData> readExcel(File file, boolean hasHeader) throws IOException
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

            int startRow = hasHeader ? 1 : 0;

            for (int i = startRow; i <= sheet.getLastRowNum(); i++)// zmena začina od 1 aby sme preskočili hlavičku
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

    private int[] findColumnIndexes(String[] headers)
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