package tests.unit.gvrules.bean.figure;


public class Square implements Figure
{
    private String color;

    public Square(String color)
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
        return "Square";
    }

    @Override
    public String toString()
    {
        return "Square(" + color + ")";
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
        if (!(obj instanceof Square)) {
            return false;
        }
        return toString().equals(obj.toString());
    }
}
