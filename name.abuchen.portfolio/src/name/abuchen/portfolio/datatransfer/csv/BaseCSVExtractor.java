package name.abuchen.portfolio.datatransfer.csv;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.PeerCache;
import name.abuchen.portfolio.datatransfer.SecurityCache;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Field;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Peer;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityPrice;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.CurrencyUnit;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.util.TextUtil;

/* package */ abstract class BaseCSVExtractor extends CSVExtractor
{
    private Client client;
    private PeerCache peerCache;
    private SecurityCache securityCache;
    private String label;
    private List<Field> fields;

    /* package */ BaseCSVExtractor(Client client, String label)
    {
        this.client = client;
        this.label = label;
        this.fields = new ArrayList<>();
    }

    @Override
    public final String getLabel()
    {
        return label;
    }

    @Override
    public final List<Field> getFields()
    {
        return fields;
    }

    @Override
    public final String toString()
    {
        return label;
    }

    public Client getClient()
    {
        return client;
    }

    @Override
    public List<Item> extract(int skipLines, List<String[]> rawValues, Map<String, Column> field2column,
                    List<Exception> errors)
    {
        // careful: the peer/security cache makes the extractor stateful because
        // peers/securities extracted during a previous run will not be created again
        peerCache     = new PeerCache(client);
        securityCache = new SecurityCache(client);

        List<Item> result = new ArrayList<>();
        int lineNo = 1 + skipLines; // +1 because of end user
        for (String[] strings : rawValues)
        {
            String[] trimmed = TextUtil.strip(strings);

            try
            {
                extract(result, trimmed, field2column);
            }
            catch (ParseException | UnsupportedOperationException | IllegalArgumentException e)
            {
                errors.add(new IOException(MessageFormat.format(Messages.CSVLineXwithMsgY, lineNo, e.getMessage(),
                                Arrays.toString(trimmed)), e));
            }
            lineNo++;
        }

        Map<Extractor, List<Item>> PeerItemsByExtractor = new HashMap<>();
        PeerItemsByExtractor.put(this, result);
        peerCache.addMissingPeerItems(PeerItemsByExtractor);

        Map<Extractor, List<Item>> SecurityItemsByExtractor = new HashMap<>();
        SecurityItemsByExtractor.put(this, result);
        securityCache.addMissingSecurityItems(SecurityItemsByExtractor);

        peerCache = null;
        securityCache = null;

        return result;
    }

    /* package */ abstract void extract(List<Item> items, String[] rawValues, Map<String, Column> field2column)
                    throws ParseException;

    protected Security getSecurity(String[] rawValues, Map<String, Column> field2column,
                    Consumer<Security> onSecurityCreated)
    {
        Security security = null;

        String isin = getISIN(Messages.CSVColumn_ISIN, rawValues, field2column);
        String tickerSymbol = getText(Messages.CSVColumn_TickerSymbol, rawValues, field2column);
        String wkn = getText(Messages.CSVColumn_WKN, rawValues, field2column);
        String name = getText(Messages.CSVColumn_SecurityName, rawValues, field2column);

        if (isin != null || tickerSymbol != null || wkn != null || name != null)
        {
            name = constructName(isin, tickerSymbol, wkn, name);
            security = securityCache.lookup(isin, tickerSymbol, wkn, name, () -> {
                Security s = new Security();
                s.setCurrencyCode(client.getBaseCurrency());

                onSecurityCreated.accept(s);

                return s;
            });
        }

        return security;
    }

    protected Peer getPeer(String[] rawValues, Map<String, Column> field2column, Consumer<Peer> onPeerCreated)
    {
        Peer peer = null;

        String iban = getIBAN(Messages.CSVColumn_IBAN, rawValues, field2column);
        String partnerName = getText(Messages.CSVColumn_PartnerName, rawValues, field2column);

        if (iban != null || partnerName != null)
        {
            peer = peerCache.lookup(iban, partnerName, () -> {
                Peer p = new Peer();
                onPeerCreated.accept(p);
                return p;
            });

        }
        return peer;
    }

    private String constructName(String isin, String tickerSymbol, String wkn, String name)
    {
        if (name != null && !name.isEmpty())
        {
            return name;
        }
        else
        {
            String key = isin != null ? isin : tickerSymbol != null ? tickerSymbol : wkn;
            return MessageFormat.format(Messages.CSVImportedSecurityLabel, key);
        }
    }

    protected String getCurrencyCode(String name, String[] rawValues, Map<String, Column> field2column)
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return client.getBaseCurrency();

        CurrencyUnit unit = CurrencyUnit.getInstance(value.trim());
        return unit == null ? client.getBaseCurrency() : unit.getCurrencyCode();
    }

    protected Money getMoney(String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        return getMoney(Messages.CSVColumn_Value, Messages.CSVColumn_TransactionCurrency, rawValues, field2column);
    }

    protected Money getMoney(String value, String currency, String[] rawValues, Map<String, Column> field2column)
                    throws ParseException
    {
          Long amount = getAmount(value, rawValues, field2column);
          if (amount == null)
              throw new ParseException(MessageFormat.format(Messages.CSVImportMissingField, Messages.CSVColumn_Value), 0);
          String currencyCode = getCurrencyCode(currency, rawValues, field2column);
          return Money.of(currencyCode, amount);
    }

    protected Optional<SecurityPrice> getSecurityPrice(String dateField, String[] rawValues,
                    Map<String, Column> field2column) throws ParseException
    {
        Long amount = getQuote(Messages.CSVColumn_Quote, rawValues, field2column);
        if (amount == null)
            return Optional.empty();

        LocalDateTime date = getDate(dateField, null, rawValues, field2column);
        if (date == null)
            date = LocalDate.now().atStartOfDay();

        return Optional.of(new SecurityPrice(date.toLocalDate(), Math.abs(amount)));
    }

    protected Optional<Unit> extractGrossAmount(String[] rawValues, Map<String, Column> field2column, Money amount)
                    throws ParseException
    {
        Long grossAmount = getAmount(Messages.CSVColumn_GrossAmount, rawValues, field2column);
        String currencyCode = getCurrencyCode(Messages.CSVColumn_CurrencyGrossAmount, rawValues, field2column);
        BigDecimal exchangeRate = getBigDecimal(Messages.CSVColumn_ExchangeRate, rawValues, field2column);

        // if no currency code is given, let's assume the gross amount is in the
        // same currency as the transaction itself. Either way, if the gross
        // amount currency equals the transaction currency, no unit is created
        if (currencyCode == null || amount.getCurrencyCode().equals(currencyCode))
            return Optional.empty();

        // if no gross amount is given at all, no unit
        if (grossAmount == null || grossAmount.longValue() == 0)
            return Optional.empty();

        // if no exchange rate is available, not unit to create
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) == 0)
            return Optional.empty();

        Money forex = Money.of(currencyCode, Math.abs(grossAmount.longValue()));
        BigDecimal grossAmountConverted = exchangeRate.multiply(BigDecimal.valueOf(grossAmount));
        Money converted = Money.of(amount.getCurrencyCode(), Math.round(grossAmountConverted.doubleValue()));

        return Optional.of(new Unit(Unit.Type.GROSS_VALUE, converted, forex, exchangeRate));
    }
}
