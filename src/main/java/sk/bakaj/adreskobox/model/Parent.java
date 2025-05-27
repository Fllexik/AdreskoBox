package sk.bakaj.adreskobox.model;

public class Parent
{
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String zipCode;
    //private String fullName;
    //private String fullAddress;


    public Parent(String firstName, String lastName, String address, String city, String zipCode)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
        this.zipCode = zipCode;
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
        return firstName + " " + lastName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getFullAddress()
    {
        return address + ", " + city + " " + zipCode;
    }

    public void setFullAddress(String fullAddress)
    {
        this.fullAddress = fullAddress;
    }
}
