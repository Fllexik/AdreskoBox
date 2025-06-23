package sk.bakaj.adreskobox.model;

/**
 * Trieda reprezentujúca skratku adresy.
 * Uchovává pôvodný text a jeho skrátenú verziu pre efektívne spracovanie adries.
 * Používa sa na definovanie pravidiel pre skracovanie častých slov v adresách
 * ako napríklad "ulica" → "ul.", "námestie" → "nám." atď.
 */

public class AddressAbbreviation
{
    // Pôvodný text ktorý sa má skrátiť
    private String originalText;
    // Skrátená verzia pôvodného textu
    private String abbreviation;

    /**
     * Konštruktor pre vytvorenie novej skratky adresy.
     *
     * @param originalText pôvodný text ktorý sa má skrátiť (napr. "ulica")
     * @param abbreviation skrátená verzia textu (napr. "ul.")
     */
    public AddressAbbreviation(String originalText, String abbreviation)
    {
        this.originalText = originalText;
        this.abbreviation = abbreviation;
    }

    /**
     * Getter metóda pre získanie pôvodného textu.
     *
     * @return pôvodný text pred skrátením
     */
    public String getOriginalText()
    {
        return originalText;
    }

    /**
     * Setter metóda pre nastavenie pôvodného textu.
     *
     * @param originalText nový pôvodný text
     */
    public void setOriginalText(String originalText)
    {
        this.originalText = originalText;
    }

    /**
     * Getter metóda pre získanie skrátenej verzie.
     *
     * @return skrátená verzia textu
     */
    public String getAbbreviation()
    {
        return abbreviation;
    }

    /**
     * Setter metóda pre nastavenie skrátenej verzie.
     *
     * @param abbreviation nová skrátená verzia textu
     */
    public void setAbbreviation(String abbreviation)
    {
        this.abbreviation = abbreviation;
    }
}
