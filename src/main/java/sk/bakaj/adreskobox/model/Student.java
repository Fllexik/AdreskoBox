package sk.bakaj.adreskobox.model;

public class Student
{
    private String firstName;
    private String lastName;
    private Parent parent1;
    private  Parent parent2;

    public Student(String firstName, String lastName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public Parent getParent1()
    {
        return parent1;
    }

    public void setParent1(Parent parent1)
    {
        this.parent1 = parent1;
    }

    public Parent getParent2()
    {
        return parent2;
    }

    public void setParent2(Parent parent2)
    {
        this.parent2 = parent2;
    }
}
