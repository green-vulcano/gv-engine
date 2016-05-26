package tests.unit.gvrules.bean.license;

public class Applicant
{
    private String name;
    private int    age;

    /**
     * @param name
     * @param age
     * @param valid
     */
    public Applicant(String name, int age)
    {
        this.name = name;
        this.age = age;
    }


    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name
     *        the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the age
     */
    public int getAge()
    {
        return this.age;
    }

    /**
     * @param age
     *        the age to set
     */
    public void setAge(int age)
    {
        this.age = age;
    }

    @Override
    public int hashCode()
    {
        return (name + age).hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Applicant)) {
            return false;
        }
        return (name + age).equals(((Applicant) obj).name + ((Applicant) obj).age);
    }
}
