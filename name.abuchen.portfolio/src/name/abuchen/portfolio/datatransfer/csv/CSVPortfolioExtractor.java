package name.abuchen.portfolio.datatransfer.csv;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.AmountField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.DateField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Field;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.ISINField;
import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Portfolio;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.money.CurrencyUnit;
import name.abuchen.portfolio.money.Money;

/* package */class CSVPortfolioExtractor extends BaseCSVExtractor
{
    /* package */ CSVPortfolioExtractor(Client client)
    {
        super(client, Messages.CSVDefPortfolio);

        List<Field> fields = getFields();
        fields.add(new DateField(Messages.CSVColumn_DateValue).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_Time).setOptional(true));

        fields.add(new ISINField(Messages.CSVColumn_ISIN).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_TickerSymbol).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_WKN).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_SecurityName).setOptional(true));

        fields.add(new AmountField(Messages.CSVColumn_Value));
        fields.add(new Field(Messages.CSVColumn_Currency).setOptional(true));

        fields.add(new AmountField(Messages.CSVColumn_Shares));
        fields.add(new Field(Messages.CSVColumn_Note).setOptional(true));

        fields.add(new DateField(Messages.CSVColumn_DateQuote).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Quote, "Schluss", "Schlusskurs", "Close") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        .setOptional(true));
        fields.add(new Field(Messages.CSVColumn_AccountName).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_PortfolioName).setOptional(true));
    }

    @Override
    void extract(List<Item> items, String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        // check if we have a security
        Security security = getSecurity(rawValues, field2column, s -> {
            String currency = getText(Messages.CSVColumn_Currency, rawValues, field2column);
            if (currency != null)
            {
                CurrencyUnit unit = CurrencyUnit.getInstance(currency.trim());
                s.setCurrencyCode(unit == null ? getClient().getBaseCurrency() : unit.getCurrencyCode());
            }
        });

        if (security == null)
            throw new ParseException(MessageFormat.format(Messages.CSVImportMissingSecurity,
                            new StringJoiner(", ").add(Messages.CSVColumn_ISIN).add(Messages.CSVColumn_TickerSymbol) //$NON-NLS-1$
                                            .add(Messages.CSVColumn_WKN).toString()),
                            0);

        // check for valuation (either current or historic)
        Money valuation = getMoney(Messages.CSVColumn_Value, Messages.CSVColumn_Currency, rawValues, field2column);

        // check for the number of shares
        Long shares = getShares(Messages.CSVColumn_Shares, rawValues, field2column);
        if (shares == null)
            throw new ParseException(MessageFormat.format(Messages.CSVImportMissingField, Messages.CSVColumn_Shares),
                            0);

        // determine remaining fields
        LocalDateTime date = getDate(Messages.CSVColumn_DateValue, Messages.CSVColumn_Time, rawValues, field2column);
        if (date == null)
            date = LocalDate.now().atStartOfDay();

        String note = getText(Messages.CSVColumn_Note, rawValues, field2column);

        Account account = getAccount(getClient(), rawValues, field2column);
        Portfolio portfolio = getPortfolio(getClient(), rawValues, field2column);

        BuySellEntry entry = new BuySellEntry();
        entry.setType(PortfolioTransaction.Type.BUY);
        entry.setSecurity(security);
        entry.setDate(date);
        entry.setAmount(Math.abs(valuation.getAmount()));
        entry.setCurrencyCode(valuation.getCurrencyCode());
        entry.setShares(shares);
        entry.setNote(note);

        Extractor.Item item = new BuySellEntryItem(entry);

        item.setAccountPrimary(account);
        item.setPortfolioPrimary(portfolio);

        items.add(item);

        // check if the data contains price

        getSecurityPrice(Messages.CSVColumn_DateQuote, rawValues, field2column)
                        .ifPresent(price -> items.add(new SecurityPriceItem(security, price)));
    }
}
