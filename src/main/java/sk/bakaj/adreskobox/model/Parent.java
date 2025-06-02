package sk.bakaj.adreskobox.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parent
{
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zipCode;
    private String fullName;
    private String fullAddress;


    public Parent(String firstName, String lastName, String address, String city, String zipCode)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.zipCode = zipCode;
    }

    public Parent(String fullName, String fullAddress)
    {
        this.fullName = fullName;
        this.fullAddress = fullAddress;

        // Pokus o rozdelenie celého mena na meno a priezvisko
        if (fullName != null)
        {
            String[] nameParts = fullName.split(" ", 2);
            this.firstName = nameParts.length > 0 ? nameParts[0] : "";
            this.lastName = nameParts.length > 1 ? nameParts[1] : "";
        }

        // Pokus o rozdelenie adresy na komponenty
        parseFullAddress(fullAddress);
    }

    /**
     * Pokus o inteligentné rozdelenie plnej adresy na komponenty
     */
    private void parseFullAddress(String fullAddr) {
        if (fullAddr == null || fullAddr.trim().isEmpty()) {
            this.address = "";
            this.city = "";
            this.zipCode = "";
            return;
        }

        String trimmedAddr = fullAddr.trim();

        // Vzor pre PSČ (slovenské: 123 45 alebo 12345)
        Pattern zipPattern = Pattern.compile("\\b(\\d{3}\\s?\\d{2})\\b");
        Matcher zipMatcher = zipPattern.matcher(trimmedAddr);

        if (zipMatcher.find()) {
            this.zipCode = zipMatcher.group(1).trim();

            // Rozdelenie na časti pred a po PSČ
            String beforeZip = fullAddr.substring(0, zipMatcher.start()).trim();
            String afterZip = fullAddr.substring(zipMatcher.end()).trim();

            // Ulica je všetko pred PSČ
            if (beforeZip.endsWith(",")) {
                this.address = beforeZip.substring(0, beforeZip.length() - 1).trim();
            } else {
                this.address = beforeZip;
            }

            // Mesto je všetko po PSČ
            this.city = afterZip;

        } else {
            // Ak sa PSČ nenašlo, pokus o rozdelenie podľa čiarok
            String[] parts = trimmedAddr.split(",");
            if (parts.length >= 2) {
                this.address = parts[0].trim();
                // Posledná časť by mala byť mesto
                this.city = parts[parts.length - 1].trim();
                this.zipCode = "";

                // Ak je v poslednej časti číselný kód na začiatku, rozdeľ ho
                String lastPart = this.city;
                Pattern zipInCityPattern = Pattern.compile("^(\\d{3}\\s?\\d{2})\\s+(.+)$");
                Matcher cityMatcher = zipInCityPattern.matcher(lastPart);
                if (cityMatcher.find()) {
                    this.zipCode = cityMatcher.group(1);
                    this.city = cityMatcher.group(2);
                }
            } else {
                // Ak nie sú čiarky, celá adresa ide do address
                this.address = trimmedAddr;
                this.city = "";
                this.zipCode = "";
            }
        }
    }

    public String getFirstName()
    {
        return firstName != null ? firstName : "";
    }

    public String getLastName()
    {
        return lastName != null ? lastName : "";
    }

    public String getAddress()
    {
        return address != null ? address : "";
    }

    public String getCity()
    {
        return city != null ? city : "";
    }

    public String getZipCode()
    {
        return zipCode != null ? zipCode : "";
    }

    public String getFullName()
    {
        if (fullName != null)
        {
            return fullName;
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getFullAddress()
    {
        if (fullAddress != null && !fullAddress.trim().isEmpty())
        {
            return fullAddress.trim();
        }

        StringBuilder sb = new StringBuilder();
        String addr = getAddress();
        String zip = getZipCode();
        String cty = getCity();

        if (!addr.isEmpty())
        {
            sb.append(addr);
        }

        if (!zip.isEmpty() || !cty.isEmpty())
        {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            if (!zip.isEmpty()) {
                sb.append(zip);
                if (!cty.isEmpty()) {
                    sb.append(" ");
                }
            }

            if (!cty.isEmpty()) {
                sb.append(cty);
            }
        }

        return sb.toString();
    }

    /**
     * Vráti formátovaný štítok s 3 riadkami:
     * Riadok 1: Meno a priezvisko
     * Riadok 2: Ulica a číslo
     * Riadok 3: PSČ a mesto
     */

    public String getFormattedLabel() {
        StringBuilder sb = new StringBuilder();

        // Riadok 1: Meno a priezvisko
        String fullNameStr = getFullName();
        sb.append(fullNameStr);
        sb.append("\n");

        // Riadok 2: Ulica a číslo
        String addressStr = getAddress();
        sb.append(addressStr);
        sb.append("\n");

        // Riadok 3: PSČ a mesto
        String zipStr = getZipCode();
        String cityStr = getCity();

        if (!zipStr.isEmpty()) {
            sb.append(zipStr);
            if (!cityStr.isEmpty()) {
                sb.append(" ");
            }
        }
        if (!cityStr.isEmpty()) {
            sb.append(cityStr);
        }

        return sb.toString();
    }
    /**
     * Získa jednotlivé riadky štítka ako array
     * Index 0: Meno a priezvisko
     * Index 1: Ulica a číslo
     * Index 2: PSČ a mesto
     */
    public String[] getLabelLines() {
        String[] lines = new String[3];

        // Riadok 1: Meno a priezvisko
        lines[0] = getFullName();

        // Riadok 2: Ulica a číslo
        lines[1] = getAddress();

        // Riadok 3: PSČ a mesto
        String zipStr = getZipCode();
        String cityStr = getCity();
        StringBuilder line3 = new StringBuilder();

        if (!zipStr.isEmpty()) {
            line3.append(zipStr);
            if (!cityStr.isEmpty()) {
                line3.append(" ");
            }
        }
        if (!cityStr.isEmpty()) {
            line3.append(cityStr);
        }

        lines[2] = line3.toString();

        return lines;
    }
}
