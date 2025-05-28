package sk.bakaj.adreskobox.model;

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

        //Pokusiť sa rozdeliť celé mená na meno a priezvisko
        if (fullName != null)
        {
            String[] nameParts = fullName.split(" ", 2);
            this.firstName = nameParts.length > 0 ? nameParts[0] : "";
            this.lastName = nameParts.length > 1 ? nameParts[1] : "";
        }

        //Pokusiť sa rozdeliť adresu
        if (fullAddress != null)
        {
            //Jednoducha logika pre rozdelenie adresy
            this.address = fullAddress;//Zatia pouzije e celu adresu
            this.city = "";
            this.zipCode = ";";
        }

    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getAddress()
    {
        return address;
    }

    public String getCity()
    {
        return city;
    }

    public String getZipCode()
    {
        return zipCode;
    }

    public String getFullName()
    {
        if (fullName != null)
        {
            return fullName;
        }
        return (fullName != null ? firstName : "") + "" + (lastName != null ? lastName : "");
    }

    public String getFullAddress()
    {
        if (fullAddress != null)
        {
            return fullAddress;
        }

        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isEmpty())
        {
            sb.append(address);
        }
        if (city != null && !city.isEmpty())
        {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (zipCode != null && !zipCode.isEmpty())
        {
            if (sb.length() > 0) sb.append(" ");
            sb.append(zipCode);
        }
        return sb.toString();
    }
}
