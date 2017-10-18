package name.abuchen.portfolio.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import name.abuchen.portfolio.money.Values;

public class SecurityElement extends Object
{

    protected LocalDate date;
    protected long value;

    public static final long NOT_AVAILABLE = -1L;

    public SecurityElement()
    {
    }

    public LocalDate getDate()
    {
        return date;
    }

    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    public long getValue()
    {
        return value;
    }

    public void setValue(long value)
    {
        this.value = value;
    }

//    @Override
//    public int hashCode()
//    {
//        final int prime = 31;
//        int result = 1;
//        long value = getValue();
//        result = prime * result + ((date == null) ? 0 : date.hashCode());
//        result = prime * result + (int) (value ^ (value >>> 32));
//        return result;
//    }

    @Override
    @SuppressWarnings("nls")
    public String toString()
    {
        return String.format("%tF: %,10.2f", date, value / Values.Quote.divider());
    }

    public static <T extends Object> List<SecurityElement> cast2ElementList(List<T> oList)
    {
        if (oList != null)
        {
            List<SecurityElement> sList = new ArrayList<>();
            for (Object obj : oList)
            {
                    if (obj instanceof SecurityElement)
                        sList.add((SecurityElement) obj); // need to cast each object specifically
            }
            return sList;
        }
        else
        {
            return null;
        }
    }

}
