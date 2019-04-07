package name.abuchen.portfolio.datatransfer.csv;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.SecurityCache;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Field;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.FieldFormat;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Portfolio;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.Transaction;
import name.abuchen.portfolio.money.CurrencyConverter;
import name.abuchen.portfolio.money.CurrencyConverterImpl;
import name.abuchen.portfolio.money.ExchangeRateProviderFactory;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.snapshot.PortfolioSnapshot;
import name.abuchen.portfolio.snapshot.SecurityPosition;
import name.abuchen.portfolio.util.Iban;
import name.abuchen.portfolio.util.Isin;

public abstract class CSVExtractor implements Extractor
{
    public abstract List<Field> getFields();

    public abstract List<Item> extract(int skipLines, List<String[]> rawValues, Map<String, Column> field2column,
                    List<Exception> errors);

    public abstract String getCode();

    protected boolean sharesOptional = false;

    @Override
    public List<Item> extract(SecurityCache securityCache, Extractor.InputFile file, List<Exception> errors)
    {
        throw new UnsupportedOperationException();
    }

    protected String getText(String name, String[] rawValues, Map<String, Column> field2column)
    {
        Column column = field2column.get(name);
        if (column == null)
            return null;

        int columnIndex = column.getColumnIndex();

        if (columnIndex < 0 || columnIndex >= rawValues.length)
            return null;

        String value = rawValues[columnIndex];
        return value != null && value.trim().length() == 0 ? null : value;
    }

    protected String getPattern(String name, String[] rawValues, Map<String, Column> field2column, String pattern)
    {
        Column column = field2column.get(name);
        if (column == null)
            return null;

        int columnIndex = column.getColumnIndex();

        if (columnIndex < 0 || columnIndex >= rawValues.length)
            return null;

        String value = rawValues[columnIndex];
        if (value == null)
            return null;

        value = value.trim().toUpperCase();

        Pattern Ipattern = Pattern.compile("\\b(" + pattern + ")\\b"); //$NON-NLS-1$ //$NON-NLS-2$
        Matcher Imatcher = Ipattern.matcher(value);
        if (Imatcher.find())
            value = Imatcher.group(1);

        return value.length() == 0 ? null : value;
    }

    protected String getIBAN(String name, String[] rawValues, Map<String, Column> field2column)
    {
        return getPattern(name, rawValues, field2column, Iban.PATTERN);
    }

    protected String getISIN(String name, String[] rawValues, Map<String, Column> field2column)
    {
        return getPattern(name, rawValues, field2column, Isin.PATTERN);
    }

    protected Long getAmount(String name, String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        return getValue(name, rawValues, field2column, Values.Amount);
    }

    protected Long getQuote(String name, String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        return getValue(name, rawValues, field2column, Values.Quote);
    }

    protected Long getValue(String name, String[] rawValues, Map<String, Column> field2column, Values<Long> values)
                    throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;

        try
        {
            Number num = (Number) field2column.get(name).getFormat().getFormat().parseObject(value);
            return Long.valueOf((long) Math.round(num.doubleValue() * values.factor()));
        }
        catch (ParseException e)
        {
            // Improve error message by adding context
            throw new ParseException(MessageFormat.format(Messages.MsgErrorParseErrorWithGivenPattern, value,
                            field2column.get(name).getFormat().toPattern()), e.getErrorOffset());
        }

    }

    protected LocalDateTime getDate(String dateColumn, String timeColumn, String[] rawValues,
                    Map<String, Column> field2column) throws ParseException
    {
        String dateValue = getText(dateColumn, rawValues, field2column);
        if (dateValue == null)
            return null;

        LocalDateTime result;
        try
        {
            Date date = (Date) field2column.get(dateColumn).getFormat().getFormat().parseObject(dateValue);
            result = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        catch (ParseException e)
        {
            // Improve error message by adding context
            throw new ParseException(MessageFormat.format(Messages.MsgErrorParseErrorWithGivenPattern, dateValue,
                            field2column.get(dateColumn).getFormat().toPattern()), e.getErrorOffset());
        }

        if (timeColumn == null)
            return result;

        String timeValue = getText(timeColumn, rawValues, field2column);
        if (timeValue != null)
        {
            int p = timeValue.indexOf(':');
            if (p > 0)
            {
                try
                {
                    int hour = Integer.parseInt(timeValue.substring(0, p));
                    int minute = Integer.parseInt(timeValue.substring(p + 1));

                    result = result.withHour(hour).withMinute(minute);
                }
                catch (NumberFormatException | DateTimeException ignore)
                {
                    // ignore time, just use the date - not parseable
                }
            }
        }

        return result;
    }

    protected final BigDecimal getBigDecimal(String name, String[] rawValues, Map<String, Column> field2column)
                    throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;

        Number num;
        try
        {
            num = (Number) field2column.get(name).getFormat().getFormat().parseObject(value);
        }
        catch (ParseException | UnsupportedOperationException | IllegalArgumentException e)
        {
            if (e instanceof ParseException)
                throw new ParseException(e.getMessage() + ": " + MessageFormat.format(Messages.CSVImportGenericColumnLabel, field2column.get(name).getColumnIndex()), new Exception().getStackTrace()[0].getLineNumber()); //$NON-NLS-1$
            else
                throw e;
        }
        return BigDecimal.valueOf(num.doubleValue());
    }

    protected final Long getShares(String name, String[] rawValues, Map<String, Column> field2column)
                    throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;

        Number num;
        try
        {
            num = (Number) field2column.get(name).getFormat().getFormat().parseObject(value);
        }
        catch (ParseException | UnsupportedOperationException | IllegalArgumentException e)
        {
            if (e instanceof ParseException)
                throw new ParseException(e.getMessage() + ": " + MessageFormat.format(Messages.CSVImportGenericColumnLabel, field2column.get(name).getColumnIndex()), new Exception().getStackTrace()[0].getLineNumber()); //$NON-NLS-1$
            else
                throw e;
        }
        return Math.round(Math.abs(num.doubleValue()) * Values.Share.factor());
    }

    @SuppressWarnings("unchecked")
    protected final <E extends Enum<E>> E getEnum(String name, Class<E> type, String[] rawValues,
                    Map<String, Column> field2column) throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;
        FieldFormat ff = field2column.get(name).getFormat();

        if (ff != null && ff.getFormat() != null)
        {
            E parsedEnum;
            try
            {
                parsedEnum = (E) ff.getFormat().parseObject(value);
            }
            catch (ParseException | UnsupportedOperationException | IllegalArgumentException e)
            {
                if (e instanceof ParseException)
                    throw new ParseException(e.getMessage() + ": " + MessageFormat.format(Messages.CSVImportGenericColumnLabel, field2column.get(name).getColumnIndex()), new Exception().getStackTrace()[0].getLineNumber()); //$NON-NLS-1$
                else
                    throw e;
            }
            return parsedEnum;
        }
        else
            return Enum.valueOf(type, value);
    }

    public boolean proposeShares(Client client, Portfolio portfolio, Item item)
    {
        if (item.getSecurity() == null || item.getDate() == null)
            return false;

        if (item instanceof Extractor.BuySellEntryItem)
            return proposeShares((BuySellEntryItem) item);
        else if (item instanceof Extractor.TransactionItem)
            return proposeShares(client, portfolio, (TransactionItem) item);
        return false;
    }

    public void setSharesOptional(boolean sharesOptional)
    {
        this.sharesOptional = sharesOptional;
    }

    public boolean proposeShares(Client client, Portfolio portfolio, TransactionItem item)
    {
        if (portfolio == null)
            return false;

        if (item.getShares() == 0 || item.hasProposedShares())
        {
            Security security = item.getSecurity();
            LocalDateTime date = item.getDate();

            CurrencyConverter converter = new CurrencyConverterImpl(new ExchangeRateProviderFactory(client), client.getBaseCurrency()); // TODO: replace dummy ExchangeRateProvider
            for (Portfolio p : client.getPortfolios())
                if (portfolio.getUUID().equals(p.getUUID()))
                {
                    PortfolioSnapshot snapshot = PortfolioSnapshot.create(portfolio, converter, date.toLocalDate());
                    SecurityPosition position = snapshot.getPositionsBySecurity().get(security);
                    if (position == null)
                        return false;
                    item.getTransaction().setShares(position.getShares());
                    item.setProposedShares(true);
                    return true;
                }
        }
        return false;
    }

    public boolean proposeShares(BuySellEntryItem item)
    {
        if (item.getShares() == 0 || item.hasProposedShares())
        {
            Security security = item.getSecurity();
            LocalDateTime date = item.getDate();

            double  price      = (double) security.getSecurityPrice(date.toLocalDate()).getValue() / Values.Quote.divider();
            if (price <= 0)
                return false;
            double  amount     = (double) item.getAmount().getAmount() / Values.Amount.divider();
            double  shareCount = amount / price;
            long proposedShares = 0L;
            long proposedFees = 0L;
            if (security.getCurrencyCode().equals(item.getAmount().getCurrencyCode()))
            {
                PortfolioTransaction pTransaction = item.getEntry().getPortfolioTransaction();
                if (pTransaction.getType().equals(PortfolioTransaction.Type.BUY))
                    proposedShares = (long) Math.floor(shareCount);
                else if (pTransaction.getType().equals(PortfolioTransaction.Type.SELL))
                    proposedShares = (long) Math.ceil(shareCount);
                pTransaction.setShares(proposedShares * Values.Share.factor());
                item.setProposedShares(true);
                if (pTransaction.getUnit(Transaction.Unit.Type.FEE).equals(Optional.empty()))
                {
                    double value = (double) proposedShares * price;
                    proposedFees = Math.round(Math.abs(amount - value) * Values.Amount.factor());
                    Transaction.Unit fee = new Transaction.Unit(Transaction.Unit.Type.FEE, Money.of(security.getCurrencyCode(), proposedFees));
                    pTransaction.addUnit(fee);
                    item.setProposedFees(true);
                }
                return true;
            }
            else
            {
            //     TODO: else-case w/ handling of convertion between currencies
            }
        }
        return false;
    }
}
