package name.abuchen.portfolio.online.impl;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.online.QuoteFeed;
import name.abuchen.portfolio.online.QuoteFeedData;

public final class ManualQuoteFeed implements QuoteFeed
{

    public static final String ID = MANUAL;

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getName()
    {
        return Messages.QuoteFeedManual;
    }

    @Override
    public QuoteFeedData getHistoricalQuotes(Security security, boolean collectRawResponse)
    {
        return new QuoteFeedData();
    }
}
