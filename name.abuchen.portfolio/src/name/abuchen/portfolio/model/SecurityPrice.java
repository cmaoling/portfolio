package name.abuchen.portfolio.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Comparator;

import name.abuchen.portfolio.money.Values;

public class SecurityPrice extends SecurityElement implements Comparable<SecurityPrice>
{
    public static final class ByDate implements Comparator<SecurityPrice>, Serializable
    {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(SecurityPrice p1, SecurityPrice p2)
        {
            return p1.time.compareTo(p2.time);
        }
    }

    public SecurityPrice()
    {}

    public SecurityPrice(LocalDate time, long price)
    {
        this.value = price;
        this.time = time;
    }

    @Override
    public int compareTo(SecurityPrice o)
    {
        return time.compareTo(o.time);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + (int) (value ^ (value >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SecurityPrice other = (SecurityPrice) obj;
        if (time == null)
        {
            if (other.time != null)
                return false;
        }
        else if (!time.equals(other.time))
            return false;
        if (value != other.value)
            return false;
        return true;
    }

}
