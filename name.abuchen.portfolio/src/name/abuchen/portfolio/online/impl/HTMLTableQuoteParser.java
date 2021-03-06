package name.abuchen.portfolio.online.impl;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;

import name.abuchen.portfolio.model.LatestSecurityPrice;
import name.abuchen.portfolio.model.SecurityElement;
import name.abuchen.portfolio.online.FeedData;
import name.abuchen.portfolio.util.Pair;
import name.abuchen.portfolio.util.TextUtil;

public class HTMLTableQuoteParser extends HTMLTableParser
{
    public HTMLTableQuoteParser()
    {        
        COLUMNS = new Column[] { new DateColumn(), new TimeColumn() , new CloseColumn(), new HighColumn(),
                        new LowColumn(), new VolumeColumn()};
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object newRowObject()
    {
        LatestSecurityPrice price = new LatestSecurityPrice(); 
        return (LatestSecurityPrice) price;
    }

    @SuppressWarnings("unchecked")
    private <T extends SecurityElement> List<T> castList(List<Object> Olist, Class<T> clazz, FeedData data)
    {
        List<T> Tlist = new ArrayList<>();
        for (Object obj : Olist)
        {
            if (obj instanceof SecurityElement)
                Tlist.add((T) obj); // need to cast each object specifically
            else
                data.addError(new ClassCastException());
        }
        return Tlist;
    }
 
    @SuppressWarnings("unchecked")
    private <T extends SecurityElement> Pair<String, List<T>> castPair(Pair<String, List<Object>> pair, Class<T> clazz, FeedData data)
    {
        List<T> Tlist = new ArrayList<>();
        List<Object> Olist = pair.getValue();
        for (Object obj : Olist)
        {
            if (obj instanceof SecurityElement)
                Tlist.add((T) obj); // need to cast each object specifically
            else
                data.addError(new ClassCastException());
        }
        return new Pair<>(pair.getKey(), Tlist);
    }
    
    public Pair<String, List<LatestSecurityPrice>> parseFromURL(String url, FeedData data)
    {
        return parseFromURL(url, false, data);
    }

    public Pair<String, List<LatestSecurityPrice>> parseFromURL(String url, boolean collectRawResponse, FeedData data)
    {
        return castPair(super._parseFromURL(url, collectRawResponse, data), LatestSecurityPrice.class, data);
    }

    public List<LatestSecurityPrice> parseFromHTML(String html, FeedData data)
    {
        return castList(super._parseFromHTML(html, data), LatestSecurityPrice.class, data);
    }
    
    protected static class CloseColumn extends Column
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        @SuppressWarnings("nls")
        public CloseColumn()
        {
            super(new String[] { "Schluss.*", "Schluß.*", "Rücknahmepreis.*", "Close.*", "Zuletzt", "Price",
                            "akt. Kurs", "Dernier", "Kurs"});
        }

        public CloseColumn(String[] patterns)
        {
            super(patterns);
        }

        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            LatestSecurityPrice price = (LatestSecurityPrice) obj;
            price.setValue(asQuote(value, languageHint));
        }
    }

    protected static class HighColumn extends Column
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        @SuppressWarnings("nls")
        public HighColumn()
        {
            super(new String[] { "Hoch.*", "Tageshoch.*", "Max.*", "High.*" });
        }

        public HighColumn(String[] patterns)
        {
            super(patterns);
        }

        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            LatestSecurityPrice price = (LatestSecurityPrice) obj;
            if ("-".equals(value.text().replace("\u00a0", "").trim())) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                price.setHigh(LatestSecurityPrice.NOT_AVAILABLE);
            else
                price.setHigh(asQuote(value, languageHint));
        }
    }

    protected static class LowColumn extends Column
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        @SuppressWarnings("nls")
        public LowColumn()
        {
            super(new String[] { "Tief.*", "Tagestief.*", "Low.*" });
        }

        public LowColumn(String[] patterns)
        {
            super(patterns);
        }

        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            LatestSecurityPrice price = (LatestSecurityPrice) obj;
            if ("-".equals(value.text().replace("\u00a0", "").trim())) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                price.setLow(LatestSecurityPrice.NOT_AVAILABLE);
            else
                price.setLow(asQuote(value, languageHint));
        }
    }

    private static class VolumeColumn extends Column
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        @SuppressWarnings("nls")
        public VolumeColumn()
        {
            super(new String[] { "Volume.*", "Umsatz" , "Stücke" });
        }

        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            LatestSecurityPrice price = (LatestSecurityPrice) obj;
            if ("-".equals(value.text().replace("\u00a0", "").trim())) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                price.setVolume((int) LatestSecurityPrice.NOT_AVAILABLE);
            else
                price.setVolume(super.asInt(value, languageHint));
        }
    }

    private static class TimeColumn extends Column
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        private DateTimeFormatter[] formatters = new DateTimeFormatter[] { DateTimeFormatter.ISO_LOCAL_TIME };
 
        @SuppressWarnings("nls")
        public TimeColumn()
        {
            super(new String[] { "Zeit.*" });
        }
 
        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            String text = TextUtil.strip(value.text());
            for (DateTimeFormatter formatter : formatters)
            {
                try
                {
                    LocalTime time = LocalTime.parse(text, formatter);
                    LatestSecurityPrice price = (LatestSecurityPrice) obj;
                    price.setTime(time);
                    return;
                }
                catch (DateTimeParseException e) // NOSONAR
                {
                    // continue with next pattern
                }

            }
 
            throw new ParseException(text, 0);
        }
    }
    
    @Override
    protected boolean isSpecValid(List<Spec> specs)
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        if (specs == null || specs.isEmpty())
            return false;

        boolean hasDate = false;
        boolean hasTime = false;
        boolean hasClose = false;

        for (Spec spec : specs)
        {
            hasDate = hasDate || spec.getColumn() instanceof DateColumn;
            hasTime = hasTime || spec.getColumn() instanceof TimeColumn;
            hasClose = hasClose || spec.getColumn() instanceof CloseColumn;
        }

        return (hasDate || hasTime) && hasClose;
    }

}