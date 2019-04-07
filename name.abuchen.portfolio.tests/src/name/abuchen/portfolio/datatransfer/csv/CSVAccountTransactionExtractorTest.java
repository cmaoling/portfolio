package name.abuchen.portfolio.datatransfer.csv;

import static name.abuchen.portfolio.datatransfer.csv.CSVExtractorTestUtil.buildField2Column;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.Extractor.Item;
import name.abuchen.portfolio.datatransfer.Extractor.SecurityItem;
import name.abuchen.portfolio.datatransfer.Extractor.TransactionItem;
import name.abuchen.portfolio.datatransfer.Extractor.BuySellEntryItem;
import name.abuchen.portfolio.datatransfer.actions.AssertImportActions;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.EnumField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.EnumMapFormat;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.FieldFormat;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.AccountTransaction.Type;
import name.abuchen.portfolio.model.AccountTransferEntry;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Portfolio;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityPrice;
import name.abuchen.portfolio.model.Transaction;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.CurrencyUnit;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;

@SuppressWarnings("nls")
public class CSVAccountTransactionExtractorTest
{
    @Test
    public void testDividendTransactionPlusSecurityCreation() throws ParseException
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0,
                        Arrays.<String[]>asList(new String[] { "2013-01-01", "", "DE0007164600", "SAP.DE", "", "100",
                                        "EUR", "DIVIDENDS", "SAP SE", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        Security security = results.stream().filter(i -> i instanceof SecurityItem).findAny().get().getSecurity();
        assertThat(security.getName(), is("SAP SE"));
        assertThat(security.getIsin(), is("DE0007164600"));
        assertThat(security.getWkn(), is(nullValue()));
        assertThat(security.getTickerSymbol(), is("SAP.DE"));

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findAny()
                        .get().getSubject();
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 100_00)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2013-01-01T00:00")));
        assertThat(t.getShares(), is(Values.Share.factorize(10)));
        assertThat(t.getSecurity(), is(security));
    }

    @Test
    public void testDividendTransaction()
    {
        Client client = new Client();
        Security security = new Security();
        security.setIsin("DE0007164600");
        client.addSecurity(security);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0,
                        Arrays.<String[]>asList(new String[] { "2013-02-01", "", "DE0007164600", "SAP.DE", "", "100",
                                        "EUR", "DIVIDENDS", "SAP SE", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findAny()
                        .get().getSubject();
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 100_00)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2013-02-01T00:00")));
        assertThat(t.getShares(), is(Values.Share.factorize(10)));
        assertThat(t.getSecurity(), is(security));
    }

    @Test
    public void testDividendTransaction_whenSecurityIsMissing()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(
                        new String[] { "2013-03-01", "", "", "", "", "100", "EUR", "DIVIDENDS", "", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(errors.size(), is(1));
        assertThat(results, empty());
    }

    @Test
    public void testDividendChargeTransaction()
    {
        Client client = new Client();
        Security security = new Security();
        security.setIsin("DE0123456781");
        client.addSecurity(security);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0,
                        Arrays.<String[]>asList(new String[] {"2015-03-11", "", "DE0123456781", "Anleihe", "", "-200", "EUR", "DIVIDEND_CHARGE", "", "100", "Notiz"}),
                        buildField2Column(extractor), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findAny()
                        .get().getSubject();
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDEND_CHARGE));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 200_00)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2015-03-11T00:00")));
        assertThat(t.getShares(), is(Values.Share.factorize(100)));
        assertThat(t.getSecurity(), is(security));
    }

    @Test
    public void testDividendChargeTransaction_whenSecurityIsMissing()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(
                        new String[] { "2013-04-01", "", "", "", "100", "EUR", "DIVIDEND_CHARGE", "", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        System.err.println("testDividendChargeTransaction_whenSecurityIsMissing errors: " +  errors.toString());

        assertThat(errors.size(), is(1));
        assertThat(results, empty());
    }

    @Test
    public void testIfMultipleSecuritiesWithSameISINExist()
    {
        Client client = new Client();
        Security security = new Security();
        security.setIsin("DE0007164600");
        client.addSecurity(security);
        Security security2 = new Security();
        security2.setIsin("DE0007164600");
        client.addSecurity(security2);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0,
                        Arrays.<String[]>asList(new String[] { "2013-05-01", "", "DE0007164600", "SAP.DE", "", "100",
                                        "EUR", "DIVIDENDS", "SAP SE", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(errors.size(), is(1));
        assertThat(results, empty());
    }

    @Test
    public void testTypeIsDeterminedByPositiveAmount()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(
                        new String[] { "2013-06-02", "", "", "", "", "100", "EUR", "", "", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));

        AccountTransaction t = (AccountTransaction) results.get(0).getSubject();
        assertThat(t.getType(), is(AccountTransaction.Type.DEPOSIT));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 100_00)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2013-06-02T00:00")));
        assertThat(t.getShares(), is(0L));
        assertThat(t.getSecurity(), is(nullValue()));
    }

    @Test
    public void testTypeIsDeterminedByNegativeUnaryMinusOperator()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(
                        new String[] { "2013-07-01", "10:00", "", "", "", "-100", "EUR", "", "", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));

        AccountTransaction t = (AccountTransaction) results.get(0).getSubject();
        assertThat(t.getType(), is(AccountTransaction.Type.REMOVAL));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 100_00)));
        assertThat(t.getNote(), is("Notiz"));

        // asset that time is removed --> not supported for removal
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2013-07-01T00:00")));
        assertThat(t.getShares(), is(0L));
        assertThat(t.getSecurity(), is(nullValue()));
    }

    @Test
    public void testTypeIsDeterminedByUnaryMinusOperatorAndSecurity()
    {
        Client client = new Client();
        Security security = new Security();
        security.setIsin("DE0007164600");
        client.addSecurity(security);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(
                        new String[] { "2013-08-01", "", "DE0007164600", "", "", "100", "EUR", "", "", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findAny()
                        .get().getSubject();
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        TransactionItem item = (TransactionItem) results.get(0);
        assertThat(item.hasProposedShares(), is(false));
    }

    @Test
    public void testThatSecurityIsAddedOnlyOnce()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList( //
                        new String[] { "2013-09-01", "", "DE0007164600", "", "", "100", "EUR", "", "", "", "Notiz" },
                        new String[] { "2013-09-02", "", "DE0007164600", "", "", "200", "EUR", "", "", "", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(3));
    }

    @Test
    public void testBuyTransaction()
    {
        Client client = new Client();
        Security security = new Security();
        security.setIsin("DE0007164600");
        client.addSecurity(security);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(new String[] { "2013-10-01", "10:00",
                        "DE0007164600", "", "", "100", "EUR", "BUY", "", "10", "Notiz" }), buildField2Column(extractor),
                        errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));

        BuySellEntryItem i = (BuySellEntryItem) results.get(0);
        BuySellEntry e = (BuySellEntry) i.getSubject();
        AccountTransaction t = e.getAccountTransaction();
        assertThat(t.getType(), is(AccountTransaction.Type.BUY));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 100_00)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2013-10-01T10:00")));
        assertThat(t.getShares(), is(0L));
        assertThat(t.getSecurity(), is(security));
        assertThat(i.hasProposedShares(), is(false));
        assertThat(i.hasProposedFees(), is(false));
    }

    @Test
    public void testBuyTransactionFailsWhenSharesAreMissing()
    {
        Client client = new Client();
        Security security = new Security();
        security.setIsin("DE0007164600");
        client.addSecurity(security);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(new String[] { "2013-11-01", "",
                        "DE0007164600", "", "", "100", "EUR", "BUY", "", "", "Notiz" }), buildField2Column(extractor),
                        errors);

        assertThat(results, empty());
        assertThat(errors.size(), is(1));
    }

    @Test
    public void testBuySellSucceedsWhenSharesAreOptionalWithFee()
    {
        Client client = new Client();

        Security security = new Security();
        security.setIsin("US01609W1027");
        security.setName("ALIBABA GR.HLDG SP.ADR");
        security.setCurrencyCode(CurrencyUnit.EUR);
        LocalDate date = LocalDate.parse("2018-09-11");
        long price = 123_4501L;
        security.addPrice(new SecurityPrice(date, price));
        client.addSecurity(security);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);
        extractor.setSharesOptional(true);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(new String[] { "2018-09-11", "",
                        "US01609W1027", "ALIBABA GR.HLDG SP.ADR", "", "4.384,32", "EUR", "BUY", "", "", "Notiz" }), buildField2Column(extractor),
                        errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));

        BuySellEntryItem item = (BuySellEntryItem) results.get(0);
        BuySellEntry entry = (BuySellEntry) item.getSubject();
        PortfolioTransaction t = entry.getPortfolioTransaction();
        assertThat(t.getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 4384_32)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2018-09-11T00:00")));
        assertThat(t.getSecurity(), is(security));
        assertThat(security.getSecurityPrice(date).getValue(), is(price));
        assertThat(security.getSecurityPrice(date)           , is(new SecurityPrice(date, price)));
        assertThat(t.getShares(), is(0L));
        assertThat(t.getUnit(Transaction.Unit.Type.FEE), is(Optional.empty()));
        assertThat(item.hasProposedShares(), is(false));
        assertThat(item.hasProposedFees(), is(false));

        assertThat(extractor.proposeShares(null, null, item), is(true)); // Neither Client nor Portfolio needed for BuySellEntry, will use Security.

        assertThat(t.getShares(), is(35000000L));
        assertThat(t.getUnitSum(Transaction.Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, 63_57)));
        assertThat(item.hasProposedShares(), is(true));
        assertThat(item.hasProposedFees(), is(true));
    }

    @Test
    public void testBuySellSucceedsWhenSharesAreOptionalIgnoringPrice()
    {
        Client client = new Client();

        Security security = new Security();
        security.setIsin("US01609W1027");
        security.setName("ALIBABA GR.HLDG SP.ADR");
        security.setCurrencyCode(CurrencyUnit.EUR);
        LocalDate date = LocalDate.parse("2018-10-11");
        long price = 123_4501L;
        security.addPrice(new SecurityPrice(date, price));
        client.addSecurity(security);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);
        extractor.setSharesOptional(true);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(new String[] { "2018-10-11", "",
                        "US01609W1027", "ALIBABA GR.HLDG SP.ADR", "", "4.384,32", "EUR", "BUY", "", "", "Notiz" }), buildField2Column(extractor),
                        errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));

        BuySellEntryItem item = (BuySellEntryItem) results.get(0);
        BuySellEntry entry = (BuySellEntry) item.getSubject();
        PortfolioTransaction t = entry.getPortfolioTransaction();
        assertThat(t.getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 4384_32)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2018-10-11T00:00")));
        assertThat(t.getSecurity(), is(security));
        assertThat(security.getSecurityPrice(date).getValue(), is(price));
        assertThat(security.getSecurityPrice(date)           , is(new SecurityPrice(date, price)));
        assertThat(t.getShares(), is(0L));
        assertThat(t.getUnit(Transaction.Unit.Type.FEE), is(Optional.empty()));
        assertThat(item.hasProposedShares(), is(false));
        assertThat(item.hasProposedFees(), is(false));

        t.addUnit(new Transaction.Unit(Transaction.Unit.Type.FEE, Money.of(CurrencyUnit.EUR, 32_00)));

        assertThat(extractor.proposeShares(null, null, item), is(true)); // Neither Client nor Portfolio needed for BuySellEntry, will use Security.
        
        assertThat(t.getShares(), is(35000000L));
        assertThat(t.getUnitSum(Transaction.Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, 32_00)));
        assertThat(item.hasProposedShares(), is(true));
        assertThat(item.hasProposedFees(), is(false));
    }

    @Test
    public void testTransactionSucceedsWhenSharesAreOptional()
    {
        Client client = new Client();

        Security security = new Security();
        security.setIsin("US4592001014");
        security.setName("INTL BUS.");
        security.setCurrencyCode(CurrencyUnit.USD);
        LocalDate date = LocalDate.parse("2018-09-10");
        long price = 123_4501L;
        security.addPrice(new SecurityPrice(date, price));
        client.addSecurity(security);

        Portfolio portfolio = new Portfolio();

        portfolio.addTransaction(new PortfolioTransaction(LocalDateTime.of(2010, Month.JANUARY, 1, 0, 0), CurrencyUnit.USD,
                        100_00, security, Values.Share.factorize(10), PortfolioTransaction.Type.BUY, 0, 0));
        client.addPortfolio(portfolio);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);
        extractor.setSharesOptional(true);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(new String[] { "2018-09-13", "",
                        "US4592001014", "INTL BUS.", "", "20,10", "EUR", "DIVIDENDS", "", "", "Notiz" }), buildField2Column(extractor),
                        errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));

        TransactionItem item = (TransactionItem) results.get(0);
        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findAny()
                        .get().getSubject();
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 20_10)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2018-09-13T00:00")));
        assertThat(t.getSecurity(), is(security));
        assertThat(t.getShares()  , is(0L));
        assertThat(item.hasProposedShares(), is(false));

        assertThat(extractor.proposeShares(client, portfolio, item), is(true));

        assertThat(t.getShares(), is(10000000L));
        assertThat(item.hasProposedShares(), is(true));
    }

    @Test
    public void testBuyTransactionFailsWhenSecurityIsMissing()
    {
        Client client = new Client();
        Security security = new Security();
        security.setIsin("DE0007164600");
        client.addSecurity(security);

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList(
                        new String[] { "2013-12-01", "", "", "", "", "100", "EUR", "BUY", "", "10", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(results, empty());
        assertThat(errors.size(), is(1));
    }

    @Test
    public void testTransferTransaction()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList( //
                        new String[] { "2013-12-15", "", "", "", "", "100", "EUR", "TRANSFER_OUT", "", "", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(results.size(), is(1));
        assertThat(errors, empty());

        AccountTransferEntry entry = (AccountTransferEntry) results.get(0).getSubject();
        AccountTransaction t = entry.getSourceTransaction();
        assertThat(t.getType(), is(AccountTransaction.Type.TRANSFER_OUT));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 100_00)));
        assertThat(t.getNote(), is("Notiz"));
        assertThat(t.getDateTime(), is(LocalDateTime.parse("2013-12-15T00:00")));
        assertThat(t.getShares(), is(0L));
        assertThat(t.getSecurity(), is(nullValue()));
    }

    @Test
    public void testRequiredFieldDate()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList( //
                        new String[] { "", "", "", "", "", "100", "EUR", "", "", "", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(results, empty());
        assertThat(errors.size(), is(1));
    }

    @Test
    public void testRequiredFieldAmount()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList( //
                        new String[] { "2015-01-01", "", "", "", "", "", "EUR", "", "", "", "Notiz" }),
                        buildField2Column(extractor), errors);

        assertThat(results, empty());
        assertThat(errors.size(), is(1));
    }

    @Test
    public void testTaxesOnDividends()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList( //
                        new String[] { "2015-02-01", "", "DE0007164600", "SAP.DE", "", "100", "EUR", "DIVIDENDS",
                                        "SAP SE", "10", "Notiz", "10" }),
                        buildField2Column(extractor), errors);

        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        AccountTransaction t = (AccountTransaction) results.stream().filter(i -> i instanceof TransactionItem).findAny()
                        .get().getSubject();
        assertThat(t.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(t.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 100_00)));
        assertThat(t.getUnitSum(Unit.Type.TAX), is(Money.of(CurrencyUnit.EUR, 10_00)));
    }

    @Test
    public void testDetectionOfFeeRefunds()
    {
        Client client = new Client();

        CSVExtractor extractor = new CSVAccountTransactionExtractor(client);

        // setup custom mapping from string -> type

        Map<String, Column> field2column = buildField2Column(extractor);
        Column typeColumn = field2column.get(Messages.CSVColumn_Type);
        @SuppressWarnings("unchecked")
        EnumField<AccountTransaction.Type> field = (EnumField<AccountTransaction.Type>) typeColumn.getField();

        @SuppressWarnings("unchecked")
        EnumMapFormat<AccountTransaction.Type> format = (EnumMapFormat<Type>) field.guessFormat(new Client(), null)
                        .getFormat();
        format.map().put(AccountTransaction.Type.FEES_REFUND, "Gebührenerstattung");
        format.map().put(AccountTransaction.Type.FEES, "Gebühren");
        typeColumn.setFormat(new FieldFormat(Messages.CSVColumn_Type, format));

        List<Exception> errors = new ArrayList<Exception>();
        List<Item> results = extractor.extract(0, Arrays.<String[]>asList( //
                        new String[] { "2017-04-21", "", "", "", "", "10", "", "Gebührenerstattung", "", "", "", "" },
                        new String[] { "2017-04-21", "", "", "", "", "20", "", "Gebühren", "", "", "", "" }),
                        field2column, errors);

        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        AccountTransaction t1 = (AccountTransaction) results.stream() //
                        .filter(i -> i instanceof TransactionItem) //
                        .filter(i -> ((AccountTransaction) ((TransactionItem) i).getSubject())
                                        .getType() == AccountTransaction.Type.FEES_REFUND)
                        .findAny().get().getSubject();

        assertThat(t1.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(10))));

        AccountTransaction t2 = (AccountTransaction) results.stream() //
                        .filter(i -> i instanceof TransactionItem) //
                        .filter(i -> ((AccountTransaction) ((TransactionItem) i).getSubject())
                                        .getType() == AccountTransaction.Type.FEES)
                        .findAny().get().getSubject();

        assertThat(t2.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(20))));
    }
}
