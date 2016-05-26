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
