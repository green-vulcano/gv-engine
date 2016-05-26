package tests.unit.gvrules.bean.figure;

public class Circle implements Figure
{
    private String color;

    public Circle(String color)
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
        return "Circle";
    }

    @Override
    public String toString()
    {
        return "Circle(" + color + ")";
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
        if (!(obj instanceof Circle)) {
            return false;
        }
        return toString().equals(obj.toString());
    }
}
