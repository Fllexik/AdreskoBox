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

    private static final int MAX_HEADER_SEARCH_ROWS = 5; // Maximálny počet riadkov na hľadanie hlavičky

    /**
     * Detekuje typ a vrati ho ako reťazec: "CSV", "XLS", XLXS" alebo null
     */
    public String detectFileType(File file)
    {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".csv"))
        {
            return "CSV";
        }
        else if (fileName.endsWith(".xls"))
        {
            return "XLS";
        }
        else if (fileName.endsWith(".xlsx"))
        {
            return "XLSX";
        }
        else
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
     * Nájde riadok s hlavičkou v CSV súbore
     * @return HeaderInfo objekt obsahujúci informácie o hlavičke
     */
    private HeaderInfo findCSVHeaderRow(File file, String delimiter) throws IOException
    {
        List<String> allLines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            while ((line = br.readLine()) != null && lineCount < MAX_HEADER_SEARCH_ROWS) {
                allLines.add(line);
                lineCount++;
            }
        }

        // Hľadáme hlavičku v prvých MAX_HEADER_SEARCH_ROWS riadkoch
        for (int i = 0; i < allLines.size(); i++) {
            String[] headers = parseCSVLine(allLines.get(i), delimiter);

            if (containsExpectedColumns(headers)) {
                return new HeaderInfo(true, i, headers);
            }
        }

        // Ak sa hlavička nenašla, vrátime info že nie je hlavička
        return new HeaderInfo(false, -1, null);
    }

    /**
     * Nájde riadok s hlavičkou v Excel súbore
     */
    private HeaderInfo findExcelHeaderRow(File file) throws IOException
    {
        try(Workbook workbook = WorkbookFactory.create(file))
        {
            Sheet sheet = workbook.getSheetAt(0);

            // Hľadáme hlavičku v prvých MAX_HEADER_SEARCH_ROWS riadkoch
            for (int rowIndex = 0; rowIndex < Math.min(MAX_HEADER_SEARCH_ROWS, sheet.getLastRowNum() + 1); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    String[] headers = new String[row.getLastCellNum()];
                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i);
                        headers[i] = getCellValueAsString(cell);
                    }

                    if (containsExpectedColumns(headers)) {
                        return new HeaderInfo(true, rowIndex, headers);
                    }
                }
            }

            return new HeaderInfo(false, -1, null);
        }
    }

    /**
     * Overí či v súbore je hlavička
     * Kontroluje, či obsahuje očakávané hlavičky stlpcov
     */

    public boolean hasHeaderRow(File file) throws IOException
    {
        String fileType = detectFileType(file);

        if ("CSV".equals(fileType))
        {
            String delimiter = detectFileDelimiter(file);
            return findCSVHeaderRow(file, delimiter).hasHeader;
        }
        else if ("XLS".equals(fileType) || "XLSX".equals(fileType))
        {
            return findExcelHeaderRow(file).hasHeader;
        }
        return false;
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

        int matchedColumns = 0;
        for (String header : headers)
        {
            if (header == null) continue;

            String cleanHeader = cleanValue(header);

            if (cleanHeader.equalsIgnoreCase(STUDENT_FIRSTNAME_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(STUDENT_LASTNAME_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(PARENT1_NAME_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(PARENT2_NAME_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(ADDRESS1_COLUMN) ||
                    cleanHeader.equalsIgnoreCase(ADDRESS2_COLUMN))
            {
                matchedColumns++;
            }
        }

        // Požadujeme aspoň 2 rozpoznané stĺpce pre potvrdenie hlavičky
        return matchedColumns >= 2;
    }

    /**
     * Načíta data zo súboru s automatickou detekciou hlavičky
     */
    public List<ImportedData> readFile(File file) throws IOException
    {
        return readFile(file, hasHeaderRow(file));
    }

    /**
     * Načíta data zo súboru
     */
    public List<ImportedData> readFile(File file, boolean hasHeader) throws IOException
    {
        String fileType = detectFileType(file);

        if ("CSV".equals(fileType))
        {
            String delimiter = detectFileDelimiter(file);
            return readCSV(file, delimiter, hasHeader);
        }
        else if ("XLS".equals(fileType) || "XLSX".equals(fileType))
        {
            return readExcel(file, hasHeader);
        }
        else
        {
            throw new IOException("Nepodporovaný typ súboru");
        }
    }

    private List<ImportedData> readCSV(File file, String delimiter, boolean hasHeader) throws IOException
    {
        List<ImportedData> dataList = new ArrayList<>();
        List<String> allLines = new ArrayList<>();

        // Načítanie všetkých riadkov
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                allLines.add(line);
            }
        }

        if (allLines.isEmpty()) {
            throw new IOException("Súbor je prázdny.");
        }

        HeaderInfo headerInfo;
        if (hasHeader) {
            headerInfo = findCSVHeaderRow(file, delimiter);
        } else {
            // Ak nie je hlavička, použijeme prvý riadok ako vzor štruktúry
            String[] firstRowColumns = parseCSVLine(allLines.get(0), delimiter);
            headerInfo = new HeaderInfo(false, -1, firstRowColumns);
        }

        if (headerInfo.headers == null) {
            throw new IOException("Nepodarilo sa určiť štruktúru súboru.");
        }

        int[] columnIndexes = findColumnIndexes(headerInfo.headers);
        int startDataRowIndex = headerInfo.hasHeader ? headerInfo.rowIndex + 1 : 0;

        // Spracovanie dát
        for (int i = startDataRowIndex; i < allLines.size(); i++) {
            String line = allLines.get(i);
            if (line.trim().isEmpty()) continue;

            String[] values = parseCSVLine(line, delimiter);
            ImportedData data = createImportedData(values, columnIndexes);
            if (data != null) {
                dataList.add(data);
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

            HeaderInfo headerInfo;
            if (hasHeader) {
                headerInfo = findExcelHeaderRow(file);
            } else {
                // Ak nie je hlavička, použijeme prvý riadok ako vzor štruktúry
                Row firstRow = sheet.getRow(0);
                if (firstRow == null) {
                    throw new IOException("Súbor je prázdny alebo neobsahuje žiadne dáta.");
                }

                String[] firstRowColumns = new String[firstRow.getLastCellNum()];
                for (int i = 0; i < firstRow.getLastCellNum(); i++) {
                    Cell cell = firstRow.getCell(i);
                    firstRowColumns[i] = getCellValueAsString(cell);
                }
                headerInfo = new HeaderInfo(false, -1, firstRowColumns);
            }

            if (headerInfo.headers == null) {
                throw new IOException("Nepodarilo sa určiť štruktúru súboru.");
            }

            int[] columnIndexes = findColumnIndexes(headerInfo.headers);
            int startRowForData = headerInfo.hasHeader ? headerInfo.rowIndex + 1 : 0;

            for (int i = startRowForData; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row != null)
                {
                    String[] values = new String[headerInfo.headers.length];
                    for (int j = 0; j < headerInfo.headers.length; j++)
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

    /**
     * Bezpečné parsovanie CSV riadku s podporou úvodzoviek
     */
    private String[] parseCSVLine(String line, String delimiter) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter.charAt(0) && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());

        return result.toArray(new String[0]);
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

            String cleanHeader = cleanValue(header);

            if (cleanHeader.equalsIgnoreCase(STUDENT_FIRSTNAME_COLUMN))
            {
                indexes[0] = i;
            } else if (cleanHeader.equalsIgnoreCase(STUDENT_LASTNAME_COLUMN))
            {
                indexes[1] = i;
            } else if (cleanHeader.equalsIgnoreCase(PARENT1_NAME_COLUMN))
            {
                indexes[2] = i;
            } else if (cleanHeader.equalsIgnoreCase(PARENT2_NAME_COLUMN))
            {
                indexes[3] = i;
            } else if (cleanHeader.equalsIgnoreCase(ADDRESS1_COLUMN))
            {
                indexes[4] = i;
            } else if (cleanHeader.equalsIgnoreCase(ADDRESS2_COLUMN))
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
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((int) cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    return String.valueOf((int) cell.getNumericCellValue()).trim();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private ImportedData createImportedData(String[] values, int[] columnIndexes)
    {
        if (values == null || values.length == 0) return null;
        ImportedData data = new ImportedData();

        if (columnIndexes[0] != -1 && columnIndexes[0] < values.length) {
            data.setStudentFirstName(cleanValue(values[columnIndexes[0]]));
        }
        if (columnIndexes[1] != -1 && columnIndexes[1] < values.length) {
            data.setStudentLastName(cleanValue(values[columnIndexes[1]]));
        }
        if (columnIndexes[2] != -1 && columnIndexes[2] < values.length) {
            data.setParent1Name(cleanValue(values[columnIndexes[2]]));
        }
        if (columnIndexes[3] != -1 && columnIndexes[3] < values.length) {
            data.setParent2Name(cleanValue(values[columnIndexes[3]]));
        }
        if (columnIndexes[4] != -1 && columnIndexes[4] < values.length) {
            data.setAddress1(cleanValue(values[columnIndexes[4]]));
        }
        if (columnIndexes[5] != -1 && columnIndexes[5] < values.length) {
            data.setAddress2(cleanValue(values[columnIndexes[5]]));
        }

        // Ak sú mená študentov prázdne, vráťte null
        if ((data.getStudentFirstName() == null || data.getStudentFirstName().isEmpty()) &&
                (data.getStudentLastName() == null || data.getStudentLastName().isEmpty())) {
            return null;
        }

        return data;
    }

    /**
     * Vyčistí hodnotu - odstráni úvodzovky a zbytočné medzery
     */
    private String cleanValue(String value) {
        if (value == null) return "";
        return value.replace("\"", "").trim();
    }

    /**
     * Pomocná trieda pre informácie o hlavičke
     */
    private static class HeaderInfo {
        final boolean hasHeader;
        final int rowIndex;
        final String[] headers;

        HeaderInfo(boolean hasHeader, int rowIndex, String[] headers) {
            this.hasHeader = hasHeader;
            this.rowIndex = rowIndex;
            this.headers = headers;
        }
    }
}