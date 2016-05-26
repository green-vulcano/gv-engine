package tests.unit.gvrules.bean.figure;

import java.util.HashSet;
import java.util.Set;

public class FigureBag
{
    private Set<Figure> figureSet = new HashSet<Figure>();

    public Set<Figure> getFigures()
    {
        return figureSet;
    }

    public void setFigures(Set<Figure> figureSet)
    {
        this.figureSet = figureSet;
    }

    public void add(Figure figure)
    {
        figureSet.add(figure);
    }

    @Override
    public String toString()
    {
        return "FigureBag: " + figureSet;
    }

    @Override
    public int hashCode()
    {
        return figureSet.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FigureBag)) {
            return false;
        }
        return figureSet.equals(((FigureBag) obj).figureSet);
    }
}
