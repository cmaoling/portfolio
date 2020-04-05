package name.abuchen.portfolio.online;

import java.util.List;

import name.abuchen.portfolio.model.Exchange;
import name.abuchen.portfolio.model.Security;

public interface Feed
{
    public static String ID = "FEED"; //$NON-NLS-1$    
    
    public static final String MANUAL = "MANUAL"; //$NON-NLS-1$
    public static final String YAHOO = "YAHOO"; //$NON-NLS-1$
    public static final String HTML = "GENERIC_HTML_TABLE"; //$NON-NLS-1$

    /**
     * Returns the technical identifier of the quote feed.
     */
    abstract public String getId();

    /**
     * Returns the display name of the quote feed.
     */
    abstract public String getName();
    
    abstract public List<Exchange> getExchanges(Security subject, List<Exception> errors);
}