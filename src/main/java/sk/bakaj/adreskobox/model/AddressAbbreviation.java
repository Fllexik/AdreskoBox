package sk.bakaj.adreskobox.model;

/**
 * Model pre reprezentaciu skratky adresy.
 */

public class AddressAbbreviation
{
    private String originalText;
    private String abbreviation;

    public AddressAbbreviation(String originalText, String abbreviation)
    {
        this.originalText = originalText;
        this.abbreviation = abbreviation;
    }

    public String getOriginalText()
    {
        return originalText;
    }

    public void setOriginalText(String originalText)
    {
        this.originalText = originalText;
    }

    public String getAbbreviation()
    {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation)
    {
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString()
    {
        return originalText + " → " + abbreviation;
    }
}
