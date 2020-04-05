package name.abuchen.portfolio.online;

import java.time.LocalDate;
import java.util.List;

import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityElement;

public abstract class EventFeed implements Feed
{

    /**
     * Update the latest data of the given security.
     * 
     * @param security
     *            the securities to be updated with the latest quote.
     * @param errors
     *            any errors that occur during the update of the quotes are
     *            added to this list.
     * @return true if at least one quote was updated.
     */
    abstract public boolean updateLatest(Security security, List<Exception> errors);

    abstract public boolean updateHistorical(Security security, List<Exception> errors);
    
    abstract public List<SecurityElement> get(Security security, LocalDate start, List<Exception> errors);

    abstract public List<SecurityElement> get(String response, List<Exception> errors);
}
