package name.abuchen.portfolio.online;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import name.abuchen.portfolio.PortfolioLog;
import name.abuchen.portfolio.model.Exchange;
import name.abuchen.portfolio.model.LatestSecurityPrice;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityPrice;

public interface QuoteFeed extends Feed // NOSONAR
{
    String ID = "QUOTE"; //$NON-NLS-1$

    /**
     * Returns the technical identifier of the quote feed.
     */
    @Override
    default String getId()
    {
        return ID;
    }

    /**
     * Returns the display name of the quote feed.
     */
    @Override
    default String getName()
    {
        return "QuoteFeed"; //$NON-NLS-1$
    }

    /**
     * Returns the help URL to be shown to the user.
     */
    default Optional<String> getHelpURL()
    {
        return Optional.empty();
    }

    /**
     * Update the latest quote of the given securities.
     */
    default Optional<LatestSecurityPrice> getLatestQuote(Security security)
    {
        FeedData data = getHistoricalQuotes(security, false);

        if (!data.getErrors().isEmpty())
            PortfolioLog.error(data.getErrors());

        List<LatestSecurityPrice> prices = data.getLatestPrices();
        if (prices.isEmpty())
            return Optional.empty();

        Collections.sort(prices, new SecurityPrice.ByDate());

        return Optional.of(prices.get(prices.size() - 1));
    }

    /**
     * Retrieves the historical quotes of the given security. The quote provider
     * may reduce the response to only include newly updated quotes.
     */
    FeedData getHistoricalQuotes(Security security, boolean collectRawResponse);

    /**
     * Retrieves a sample of historical quotes of the given security. The list
     * of quotes may be reduced to the last 2 months or latest 100 entries.
     */
    default FeedData previewHistoricalQuotes(Security security)
    {
        return getHistoricalQuotes(security, true);
    }

    @Override
    default List<Exchange> getExchanges(Security subject, List<Exception> errors)
    {
        return Collections.emptyList();
    }
}
