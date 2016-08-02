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
package tests.unit.gvrules.bean.license;

import java.util.Date;

public class Application
{
    private Date    dateApplied;
    private boolean valid = true;

    /**
     * @param dateApplied
     */
    public Application(Date dateApplied)
    {
        this.dateApplied = dateApplied;
    }

    /**
     * @return the dateApplied
     */
    public Date getDateApplied()
    {
        return this.dateApplied;
    }

    /**
     * @param dateApplied
     *        the dateApplied to set
     */
    public void setDateApplied(Date dateApplied)
    {
        this.dateApplied = dateApplied;
    }

    /**
     * @return the valid
     */
    public boolean isValid()
    {
        return this.valid;
    }

    /**
     * @param valid
     *        the valid to set
     */
    public void setValid(boolean valid)
    {
        this.valid = valid;
    }


    @Override
    public int hashCode()
    {
        return dateApplied.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Application)) {
            return false;
        }
        return dateApplied.equals(((Application) obj).dateApplied);
    }
}
