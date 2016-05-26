package tests.unit.gvrules.bean.figure;

public class Triangle implements Figure
{
    private String color;

    public Triangle(String color)
    {
        this.color = color;
    }

    @Override
    public void setColor(String color)
    {
        this.color = color;
    }

    @Override
    public String getColor()
    {
        return this.color;
    }

    @Override
    public String getFigureType()
    {
        return "Triangle";
    }

    @Override
    public String toString()
    {
        return "Triangle(" + color + ")";
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Triangle)) {
            return false;
        }
        return toString().equals(obj.toString());
    }
}
