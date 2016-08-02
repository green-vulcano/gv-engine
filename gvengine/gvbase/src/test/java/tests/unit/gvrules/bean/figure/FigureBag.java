/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
