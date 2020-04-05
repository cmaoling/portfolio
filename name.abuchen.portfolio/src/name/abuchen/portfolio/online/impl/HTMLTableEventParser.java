package name.abuchen.portfolio.online.impl;

import java.text.ParseException;
import java.util.List;

import org.jsoup.nodes.Element;

import name.abuchen.portfolio.model.SecurityElement;
import name.abuchen.portfolio.model.SecurityEvent;
import name.abuchen.portfolio.money.Monetary;
import name.abuchen.portfolio.online.QuoteFeedData;

public class HTMLTableEventParser extends HTMLTableParser
{
    public HTMLTableEventParser()
    {        
        COLUMNS = new Column[] { new DateColumn(), new TypeColumn() , new ValueColumn()  , new RatioColumn()};
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object newRowObject()
    {
        SecurityEvent event = new SecurityEvent();
        return (SecurityEvent) event;
    }

    public List<SecurityElement> parseFromURL(String url, QuoteFeedData data)
    {
        return SecurityElement.cast2ElementList(super._parseFromURL(url, data).getValue());
    }

    public List<SecurityElement> parseFromHTML(String html, QuoteFeedData data)
    {
        return SecurityElement.cast2ElementList(super._parseFromHTML(html, data));
    }

    private static class TypeColumn extends Column
    {
        @SuppressWarnings("nls")
        public TypeColumn()
        {
            super(new String[] { "Ereignis"});
        }
        
        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            SecurityEvent event = (SecurityEvent) obj;
            String type = value.text().trim();
            
            if (type.matches("Dividende") || type.matches("Ausschüttung")) //$NON-NLS-1$ //$NON-NLS-2$
                event.setType(SecurityEvent.Type.STOCK_DIVIDEND);
            else if (type.matches("Split") || type.matches("Reverse Split"))  //$NON-NLS-1$ //$NON-NLS-2$
                event.setType(SecurityEvent.Type.STOCK_SPLIT);                
            else if (type.matches("Bezugsrecht")) //$NON-NLS-1$
                event.setType(SecurityEvent.Type.STOCK_RIGHT);
            else
            {
                event.setType(SecurityEvent.Type.STOCK_OTHER);                
                event.setTypeStr(type);
            }
        }
    }
    
    private static class RatioColumn extends Column
    {
        @SuppressWarnings("nls")
        public RatioColumn()
        {
            super(new String[] { "Verhältnis"});
        }
        
        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            SecurityEvent event = (SecurityEvent) obj;
            if (value.text().matches("^[0-9,.]+:[0-9,.]+$")) //$NON-NLS-1$
            {
                String[] elements = value.text().trim().split(":"); //$NON-NLS-1$
                if (elements.length > 2)
                    throw new ParseException(value.toString(), 0);
                event.setRatio(asDouble(elements[0], languageHint), asDouble(elements[1], languageHint));
            }
            else if (value.text().matches("^[0-9,.]+$")) //$NON-NLS-1$
                event.setRatio(asDouble(value.text().trim(), languageHint));
            else
                event.clearRatio();
        }
    }

    private static class ValueColumn extends Column
    {
        @SuppressWarnings("nls")
        public ValueColumn()
        {
            super(new String[] { "Betrag"});
        }
        
        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            SecurityEvent event = (SecurityEvent) obj;
            Monetary money = new Monetary().parse(value.text().trim(), languageHint);
            if (money.getValue() != null)
                event.setAmount(money);
            else
                event.clearAmount();
        }
    }

    
    @Override
    protected final boolean isSpecValid(List<Spec> specs)
    {
        if (specs == null || specs.isEmpty())
            return false;

        boolean hasDate = false;
        boolean hasType = false;

        for (Spec spec : specs)
        {
            hasDate = hasDate || spec.getColumn() instanceof DateColumn;
            hasType = hasType || spec.getColumn() instanceof TypeColumn;
        }

        return hasDate && hasType;
    }

  
}