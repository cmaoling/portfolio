package name.abuchen.portfolio.online.impl;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.junit.Test;

import name.abuchen.portfolio.TestUtilities;
import name.abuchen.portfolio.model.LatestSecurityPrice;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityPrice;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.online.FeedData;

@SuppressWarnings("nls")
public class HTMLTableQuoteFeedTest
{

    @Test
    public void testParsingHtml() throws IOException
    {
        // search: http://www.ariva.de/commerzbank-aktie/historische_ereignisse

        try (Scanner scanner = new Scanner(getClass().getResourceAsStream("response_html_quotes.txt"), "UTF-8"))
        {
            String html = scanner.useDelimiter("\\A").next();

            FeedData data = new FeedData();

            List<LatestSecurityPrice> elements = new HTMLTableQuoteParser().parseFromHTML(html, data);

            List<Exception> errors = data.getErrors(); 
            
            if (!errors.isEmpty())
                System.err.println("HTMLTableQuoteFeedTest.testParsingHtml - errors: " + errors.toString());

            assertThat(errors.size(), equalTo(0));

            //System.err.println("HTMLTableEventTest.testParsingHtml - elements: " + elements.toString());
            assertThat(elements.size(), equalTo(23));

            assertThat(elements.get( 0), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.JUNE   , 4), 2548000L, 2577000L, 2502000L, 1355534L)));
            assertThat(elements.get( 2), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.JUNE   ,  2), 2411000L, 2434000L, 2390000L, 1136776L)));
            assertThat(elements.get( 3), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.MAY    , 29), 2372000L, 2397000L, 2348000L, 1803669L)));
            assertThat(elements.get( 8), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.MAY    , 22), 2178000L, 2185000L, 2116000L,  648062L)));
            assertThat(elements.get(14), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.MAY    , 14), 1952500L, 1971500L, 1894000L,  918512L)));
            assertThat(elements.get(22), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.MAY    ,  4), 1938500L, 2036000L, 1931000L, 1494144L)));

        }
    }

    @Test
    public void testHistorical()
    {
        HTMLTableQuoteFeed feed = new HTMLTableQuoteFeed();

        String html = TestUtilities.read(getClass(), "HTMLTableQuoteFeedHistoricalSample.html");

        FeedData data = feed.getHistoricalQuotes(html);

        List<LatestSecurityPrice> prices = data.getLatestPrices();

        List<Exception> errors = data.getErrors(); 
        
        if (!errors.isEmpty())
            System.err.println("HTMLTableQuoteFeedTest.testHistorical - errors: " + errors.toString());

        assertThat(prices.size(), is(3));

        Collections.sort(prices, new SecurityPrice.ByDate());

        assertPrice(prices.get(0), "2020-12-03", 6750000, 6750000, 6658900);
        assertPrice(prices.get(1), "2020-12-04", 6753700, 6753700, 6674600);
        assertPrice(prices.get(2), "2020-12-07", 6771300, 6803100, 6771300);
    }

    @Test
    public void testGetLatestQuote()    
    {
        HTMLTableQuoteFeed feed = new HTMLTableQuoteFeed();
        Security security = new Security("foo", "EUR");
        security.setLatestFeed("Tabelle");
        security.setLatestFeedURL("file://HTMLTableQuoteFeedNonHistoricalSample.html");

        FeedData data = feed.getLatestData(security);

        List<Exception> errors = data.getErrors(); 
        
        if (!errors.isEmpty())
            System.err.println("HTMLTableQuoteFeedTest.testGetLatestQuote - errors: " + errors.toString());

        Optional<LatestSecurityPrice> quote = feed.getLatestQuote(data);

        assertThat(quote.isPresent(), is(true));

        LatestSecurityPrice price = quote.orElseThrow(IllegalArgumentException::new);
        assertThat(price.getValue(), is(Values.Quote.factorize(671.4)));

    }

    private void assertPrice(LatestSecurityPrice price, String date, long value, long high, long low)
    {
        assertThat(price.getDate(), is(LocalDate.parse(date)));
        assertThat(price.getValue(), is(value));
        assertThat(price.getHigh(), is(high));
        assertThat(price.getLow(), is(low));
    }
}
