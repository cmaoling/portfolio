package name.abuchen.portfolio.online.impl;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.model.SecurityPrice;
import name.abuchen.portfolio.money.Values;

public class YahooFinanceAdjustedCloseQuoteFeed extends YahooFinanceQuoteFeed
{
    public static final String ID = "YAHOO-ADJUSTEDCLOSE"; //$NON-NLS-1$

    @Override
    public String getId()
    {
        return ID; //$NON-NLS-1$
    }

    @Override
    public String getName()
    {
        return Messages.LabelYahooFinanceAdjustedClose;
    }

    @Override
    protected <T extends SecurityPrice> void fillValues(String[] values, T price, DecimalFormat priceFormat,
                    DateTimeFormatter dateFormat) throws ParseException
    {
        // Date,Open,High,Low,Close,Volume,Adj Close
        super.fillValues(values, price, priceFormat, dateFormat);

        Number q = priceFormat.parse(values[6]);
        long v = (long) (q.doubleValue() * Values.Quote.factor());
        price.setValue(v);
    }

}
