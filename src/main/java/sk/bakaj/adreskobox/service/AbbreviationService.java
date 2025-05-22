package sk.bakaj.adreskobox.service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Služba pre správu skratiek adries s ukladaním do properties súboru
 */
public class AbbreviationService
{
    private Map<String, String> abbreviations = new HashMap<>();
    private static final String PROPERTIES_FILE_NAME = "abbreviations.properties";

    public AbbreviationService()
    {
        try
        {
            loadAbbreviationsFromProperties();
        } catch (Exception e)
        {
            System.err.println("Chyba pri načítaní skratiek: " + e.getMessage());
            //AK načitanie zlyha, inicializuje aspoň základne skratky
            initDefaultAbbreviations();
        }
    }

    /**
     * Inicializácia zakladných skratiek v pamäti
     */
    private void initDefaultAbbreviations()
    {
        abbreviations.put("námestie", "nám.");
        abbreviations.put("ulica", "ul.");
        abbreviations.put("trieda", "tr.");

        //Pokusime sa uložiť zakladné skrátky do súboru
        try
        {
            saveAbbreviationsToProperties();
        } catch (IOException e)
        {
            System.err.println("Nepodarilo sa uložiť základné skratky: " + e.getMessage());
        }
    }

    /**
     * Načítanie skratiek z properties súboru
     */

    private void loadAbbreviationsFromProperties() throws IOException
    {
        abbreviations.clear();
        File file = new File(PROPERTIES_FILE);

        if (file.exists())
        {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file))
            {
                props.load(fis);

                for (String key : props.stringPropertyNames())
                {
                    abbreviations.put(key, props.getProperty(key));
                }
            }
        } else
        {
            //Ak súbor neexistuje, vytvoríme ho so zakladnými skratkami
            initDefaultAbbreviations();
        }
    }

    /**
     * Uloženie skratiek do properties súboru
     */
    private void saveAbbreviationsToProperties() throws IOException
    {
        Properties props = new Properties();
        for (Map.Entry<String, String> entry : abbreviations.entrySet())
        {
            props.setProperty(entry.getKey(), entry.getValue());
        }

        try (FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE))
        {
            props.store(fos, "Address Abbreviations");
        }
    }

    /**
     * Uloženie skratky
     */

    public void saveAbbreviation(String originalText, String abbreviation)
    {
        if (originalText == null || abbreviation == null || originalText.trim().isEmpty() || abbreviation.trim().isEmpty())
        {
            return;
        }

        originalText = originalText.toLowerCase().trim();
        abbreviation = abbreviation.trim();

        //uložime do mapy
        abbreviations.put(originalText, abbreviation);

        //Uložime do súboru
        try
        {
            saveAbbreviationsToProperties();
        } catch (Exception e)
        {
            System.err.println("Chyba pri uložení skratky: " + e.getMessage());
        }
    }

    /**
     * Odstranenie skratky
     */
    public void removeAbbreviation(String originalText)
    {
        if (originalText == null || originalText.trim().isEmpty())
        {
            return;
        }

        originalText = originalText.toLowerCase().trim();

        //Odstranenie z mapy
        abbreviations.remove(originalText);

        //Uložime do súboru
        try
        {
            saveAbbreviationsToProperties();
        } catch (Exception e)
        {
            System.err.println("Chyba pri odstranení skratky: " + e.getMessage());
        }
    }

    /**
     * Získanie všetkých skratiek
     */
    public Map<String, String> getAbbreviations()
    {
        return new HashMap<>(abbreviations);
    }

    /**
     * Skratenie adresy s využitím dostupných skratiek
     */
    public String abbreviateAddress(String address)
    {
        if (address == null || address.trim().isEmpty())
        {
            return address;
        }

        //Skusime najprv nájsť skratku pre celu adresu
        String loweAddress = address.toLowerCase();
        if (abbreviations.containsKey(loweAddress))
        {
            return abbreviations.get(loweAddress);
        }

        //AK nemáme skratku pre celú adresu, skusíme nahradiť jednotlivé slová
        String result = address;

        //Najprv viaceroslovné výrazy (dlhšie výrazy majú prednosť)
        Map<String, String> multiWordAbbreviations = new HashMap<>();
        for (Map.Entry<String, String> entry : abbreviations.entrySet())
        {
            if (entry.getKey().contains(" "))
            {
                multiWordAbbreviations.put(entry.getKey(), entry.getValue());
            }
        }

        // Zoradime viaceroslovné výrazy podľa dĺžky (od najdlšieho po najkratši)
        multiWordAbbreviations.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()))
                .forEach(entry -> {
                    result = result.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue())
                });

        String afterMultiWord = result;

        //Potom jednoslovné výrazy
        for (Map.Entry<String, String> entry : abbreviations.entrySet())
        {
            if (!entry.getKey().contains(" "))
            {
                result = result.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue());
            }
        }

        return result;
    }

    /**
     * Ziskanie najlepšej skratky adresy, ktora sa  zmesti do danej dĺžky
     */
    public String getBestAbbreviation(String address, int maxLength, PDFService pdfService)
    {
        if (address == null || address.length() <= maxLength)
        {
            return address;
        }

        // Skusíme najprv použiť skratku pre celu adresu
        String lowerAddress = address.toLowerCase();
        if (abbreviations.containsKey(lowerAddress))
        {
            String abbr = abbreviations.get(lowerAddress);
            if (abbr.length() <= maxLength)
            {
                return abbr;
            }
        }

        // skúsime aplikovať všetky skratky
        String abbreviatedAddress = abbreviateAddress(address);

        // ak sa stale nezmestí, použijeme existujúcu metodu pre skratenie
        if (abbreviatedAddress.length() > maxLength)
        {
            return pdfService.abbreviateAddressIfNeeded(address, maxLength);
        }
        return abbreviatedAddress;
    }
}
