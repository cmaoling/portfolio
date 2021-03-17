package name.abuchen.portfolio.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import name.abuchen.portfolio.money.Values;

public class LatestSecurityPrice extends SecurityPrice
{
    private long high;
    private long low;
    private long volume;

    private long previousClose;

    // cmaoling: combine ExtractedPrice from name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java into LatestSecurityPrice
    private LocalTime time;

    public static final long NOT_AVAILABLE = -1L;

    public LatestSecurityPrice()
    {
    }

    public LatestSecurityPrice(LocalDate date, long price)
    {
        super(date, price);
    }

    public LatestSecurityPrice(LocalDate date, long price, long high, long low, long volume)
    {
        super(date, price);

        this.high = high;
        this.low = low;
        this.volume = volume;
    }

    public long getHigh()
    {
        return high;
    }

    public void setHigh(long high)
    {
        this.high = high;
    }

    public long getLow()
    {
        return low;
    }

    public void setLow(long low)
    {
        this.low = low;
    }

    public LocalTime getTime()
    {
        return time;
    }

    public void setTime(LocalTime time)
    {
        this.time = time;
    }

    public long getVolume()
    {
        return volume;
    }

    public void setVolume(long volume)
    {
        this.volume = volume;
    }

    @Deprecated
    public long getPreviousClose()
    {
        return previousClose;
    }

    public void setPreviousClose(long previousClose)
    {
        this.previousClose = previousClose;
    }

    @Override
    public int hashCode()
    {
        return 31 * super.hashCode() + Objects.hash(high, low, previousClose, volume, time);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LatestSecurityPrice other = (LatestSecurityPrice) obj;
        return super.equals(other) && high == other.high && low == other.low && previousClose == other.previousClose && volume == other.volume && time == other.time;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString()
    {
        return String.format("%tF: %,10.4f high: %d low: %d volume: %10d", date, value / Values.Quote.divider(), high, low, volume);
    }
}
