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
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.JUNE   , 4), Values.Quote.factorize(254.8000), Values.Quote.factorize(257.7000), Values.Quote.factorize(250.2000), 1355534L)));
            assertThat(elements.get( 2), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.JUNE   ,  2), Values.Quote.factorize(241.1000), Values.Quote.factorize(243.4000), Values.Quote.factorize(239.0000), 1136776L)));
            assertThat(elements.get( 3), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.MAY    , 29), Values.Quote.factorize(237.2000), Values.Quote.factorize(239.7000), Values.Quote.factorize(234.8000), 1803669L)));
            assertThat(elements.get( 8), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.MAY    , 22), Values.Quote.factorize(217.8000), Values.Quote.factorize(218.5000), Values.Quote.factorize(211.6000),  648062L)));
            assertThat(elements.get(14), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.MAY    , 14), Values.Quote.factorize(195.2500), Values.Quote.factorize(197.1500), Values.Quote.factorize(189.4000),  918512L)));
            assertThat(elements.get(22), //
                            is(new LatestSecurityPrice(LocalDate.of(2020, Month.MAY    ,  4), Values.Quote.factorize(193.8500), Values.Quote.factorize(203.6000), Values.Quote.factorize(193.1000), 1494144L)));

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

        assertPrice(prices.get(0), "2020-12-03", 675, 675, 665.89);
        assertPrice(prices.get(1), "2020-12-04", 675.37, 675.37, 667.46);
        assertPrice(prices.get(2), "2020-12-07", 677.13, 680.31, 677.13);
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

    private void assertPrice(LatestSecurityPrice price, String date, double value, double high, double low)
    {
        assertThat(price.getDate(), is(LocalDate.parse(date)));
        assertThat(price.getValue(), is(Values.Quote.factorize(value)));
        assertThat(price.getHigh(), is(Values.Quote.factorize(high)));
        assertThat(price.getLow(), is(Values.Quote.factorize(low)));
    }
}
