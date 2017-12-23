package name.abuchen.portfolio.datatransfer.pdf.targobank;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import name.abuchen.portfolio.datatransfer.Extractor.BuySellEntryItem;
import name.abuchen.portfolio.datatransfer.Extractor.Item;
import name.abuchen.portfolio.datatransfer.Extractor.SecurityItem;
//import name.abuchen.portfolio.datatransfer.Extractor.TransactionItem;
import name.abuchen.portfolio.datatransfer.actions.AssertImportActions;
import name.abuchen.portfolio.datatransfer.pdf.TargobankPDFExtractor;
import name.abuchen.portfolio.datatransfer.pdf.PDFInputFile;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.CurrencyUnit;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;

@SuppressWarnings("nls")
public class TargobankPDFExtractorTest
{

    @Test
    public void testSanityCheckForBankName() throws IOException
    {
        TargobankPDFExtractor extractor = new TargobankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<Exception>();

        List<Item> results = extractor.extract(PDFInputFile.createTestCase("some.pdf", "some text"), errors);

        assertThat(results, empty());
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), instanceOf(UnsupportedOperationException.class));
    }

//    private Security assertSecurity(List<Item> results)
//    {
//        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
//        assertThat(item.isPresent(), is(true));
//        Security security = ((SecurityItem) item.get()).getSecurity();
//        assertThat(security.getIsin(), is("DE000BASF111"));
//        assertThat(security.getWkn(), is("BASF11"));
//        assertThat(security.getName(), is("BASF SE"));
//        assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));
//
//        return security;
//    }
//
//    @Test
//    public void testErtragsgutschrift() throws IOException
//    {
//        TargobankPDFExtractor extractor = new TargobankPDFExtractor(new Client());
//
//        List<Exception> errors = new ArrayList<Exception>();
//
//        List<Item> results = extractor
//                        .extract(PDFInputFile.loadTestCase(getClass(), "TargobankErtragsgutschrift.txt"), errors);
//
//        assertThat(errors, empty());
//        assertThat(results.size(), is(2));
//        new AssertImportActions().check(results, CurrencyUnit.EUR);
//
//        // check security
//        Security security = assertSecurity(results);
//
//        // check transaction
//        Optional<Item> item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
//        assertThat(item.isPresent(), is(true));
//        assertThat(item.get().getSubject(), instanceOf(AccountTransaction.class));
//        AccountTransaction transaction = (AccountTransaction) item.get().getSubject();
//        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
//        assertThat(transaction.getSecurity(), is(security));
//        assertThat(transaction.getDate(), is(LocalDate.parse("2014-12-15")));
//        assertThat(transaction.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 14_95L)));
//        assertThat(transaction.getUnitSum(Unit.Type.TAX), is(Money.of(CurrencyUnit.EUR, 4_52)));
//        assertThat(transaction.getGrossValue(), is(Money.of(CurrencyUnit.EUR, 19_47)));
//        assertThat(transaction.getShares(), is(Values.Share.factorize(123)));
//    }
//
//    @Test
//    public void testErtragsgutschriftWhenSecurityExists() throws IOException
//    {
//        Client client = new Client();
//        Security security = new Security("BASF", "DE000BASF111", null, null);
//        client.addSecurity(security);
//
//        TargobankPDFExtractor extractor = new TargobankPDFExtractor(client);
//
//        List<Exception> errors = new ArrayList<Exception>();
//
//        List<Item> results = extractor
//                        .extract(PDFInputFile.loadTestCase(getClass(), "TargobankErtragsgutschrift.txt"), errors);
//
//        assertThat(errors, empty());
//        assertThat(results.size(), is(1));
//        new AssertImportActions().check(results, CurrencyUnit.EUR);
//
//        // check transaction
//        AccountTransaction transaction = (AccountTransaction) results.get(0).getSubject();
//        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
//        assertThat(transaction.getSecurity(), is(security));
//    }
//
//    @Test
//    public void testDividendengutschriftWhenSecurityExists() throws IOException
//    {
//        Client client = new Client();
//        Security security = new Security("CISCO", "US17275R1023", null, null);
//        client.addSecurity(security);
//
//        TargobankPDFExtractor extractor = new TargobankPDFExtractor(client);
//
//        List<Exception> errors = new ArrayList<Exception>();
//
//        List<Item> results = extractor
//                        .extract(PDFInputFile.loadTestCase(getClass(), "TargobankDividendengutschrift.txt"), errors);
//
//        assertThat(errors, empty());
//        assertThat(results.size(), is(1));
//        new AssertImportActions().check(results, CurrencyUnit.EUR);
//
//        // check transaction
//        AccountTransaction transaction = (AccountTransaction) results.get(0).getSubject();
//        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
//        assertThat(transaction.getSecurity(), is(security));
//        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
//        assertThat(transaction.getSecurity(), is(security));
//        assertThat(transaction.getDate(), is(LocalDate.parse("2014-12-15")));
//        assertThat(transaction.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 64_88L)));
//        assertThat(transaction.getUnitSum(Unit.Type.TAX), is(Money.of(CurrencyUnit.EUR, 8_71 + 47 + 13_07)));
//        assertThat(transaction.getGrossValue(), is(Money.of(CurrencyUnit.EUR, 87_13)));
//        assertThat(transaction.getShares(), is(Values.Share.factorize(380)));
//    }
//
//    @Test
//    public void testErtragsgutschrift2() throws IOException
//    {
//        TargobankPDFExtractor extractor = new TargobankPDFExtractor(new Client());
//
//        List<Exception> errors = new ArrayList<Exception>();
//
//        List<Item> results = extractor
//                        .extract(PDFInputFile.loadTestCase(getClass(), "TargobankErtragsgutschrift2.txt"), errors);
//
//        assertThat(errors, empty());
//        assertThat(results.size(), is(2));
//        new AssertImportActions().check(results, CurrencyUnit.EUR);
//
//        // check security
//        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
//        assertThat(security.getName(), is("ISHS-MSCI N. AMERIC.UCITS ETF BE.SH.(DT.ZT.)"));
//        assertThat(security.getIsin(), is("DE000A0J2060"));
//        assertThat(security.getWkn(), is("A0J206"));
//        assertThat(security.getCurrencyCode(), is("USD"));
//
//        // check transaction
//        Optional<Item> item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
//        assertThat(item.isPresent(), is(true));
//        assertThat(item.get().getSubject(), instanceOf(AccountTransaction.class));
//
//        AccountTransaction transaction = (AccountTransaction) item.get().getSubject();
//        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
//        assertThat(transaction.getSecurity(), is(security));
//        assertThat(transaction.getDate(), is(LocalDate.parse("2015-03-24")));
//        assertThat(transaction.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, 16_17L)));
//        assertThat(transaction.getShares(), is(Values.Share.factorize(123)));
//
//        Optional<Unit> grossValue = transaction.getUnit(Unit.Type.GROSS_VALUE);
//        assertThat(grossValue.isPresent(), is(true));
//        assertThat(grossValue.get().getAmount(), is(Money.of("EUR", 16_17L)));
//        assertThat(grossValue.get().getForex(), is(Money.of("USD", 17_38L)));
//        assertThat(grossValue.get().getExchangeRate().doubleValue(), IsCloseTo.closeTo(0.930578, 0.000001));
//    }

    @Test
    public void testWertpapierKauf() throws IOException
    {
        TargobankPDFExtractor extractor = new TargobankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<Exception>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "TargobankKauf1.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getName(), is("DEUTSCHE POST AG NAMENS-AKTIEN O.N."));
        assertThat(security.getIsin(), is("DE0005552004"));
        assertThat(security.getWkn(), is("555200"));
        assertThat(security.getCurrencyCode(), is("EUR"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.get().getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));

        assertThat(entry.getPortfolioTransaction().getMonetaryAmount(),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(5707.43))));
        assertThat(entry.getPortfolioTransaction().getDate(), is(LocalDate.parse("2015-02-17")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(200)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, 14_23L)));
    }

    @Test
    public void testWertpapierKauf2() throws IOException
    {
        TargobankPDFExtractor extractor = new TargobankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<Exception>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "TargobankKauf2.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getName(), is("TESLA MOTORS INC.REGISTERED SHARES DL-"));
        assertThat(security.getIsin(), is("US88160R1014"));
        assertThat(security.getWkn(), is("A1CX3T"));
        assertThat(security.getCurrencyCode(), is("EUR"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.get().getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));

        assertThat(entry.getPortfolioTransaction().getMonetaryAmount(),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(6110.38))));
        assertThat(entry.getPortfolioTransaction().getDate(), is(LocalDate.parse("2016-08-15")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(30)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(15.22 + 4.72 + 3.50))));
    }

    @Test
    public void testWertpapierVerkauf() throws IOException
    {
        TargobankPDFExtractor extractor = new TargobankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<Exception>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "TargobankVerkauf1.txt"),
                        errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getName(), is("ALIBABA GROUP HOLD.REG.SH.ADR./1 DL-,0"));
        assertThat(security.getIsin(), is("US01609W1027"));
        assertThat(security.getWkn(), is("A117ME"));
        assertThat(security.getCurrencyCode(), is("EUR"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.get().getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(entry.getPortfolioTransaction().getMonetaryAmount(),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(2788.70))));
        assertThat(entry.getPortfolioTransaction().getDate(), is(LocalDate.parse("2015-08-24")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(50)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, 8_90L)));
    }

    @Test
    public void testWertpapierVerkauf2() throws IOException
    {
        TargobankPDFExtractor extractor = new TargobankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<Exception>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "TargobankVerkauf2.txt"),
                        errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getName(), is("UNISECTOR: HIGHTECH INHABER-ANTEILE A"));
        assertThat(security.getIsin(), is("LU0101441672"));
        assertThat(security.getWkn(), is("921559"));
        assertThat(security.getCurrencyCode(), is("EUR"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.get().getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(entry.getPortfolioTransaction().getMonetaryAmount(),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(13.49))));
        assertThat(entry.getPortfolioTransaction().getDate(), is(LocalDate.parse("2014-10-10")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(0.2496)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.TAX),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(0))));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(0))));
    }

    @Test
    public void testWertpapierVerkauf3() throws IOException
    {
        TargobankPDFExtractor extractor = new TargobankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<Exception>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "TargobankVerkauf3.txt"),
                        errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Security security = results.stream().filter(i -> i instanceof SecurityItem).findFirst().get().getSecurity();
        assertThat(security.getName(), is("DJE REAL ESTATE INHABER-ANTEILE P O.N."));
        assertThat(security.getIsin(), is("LU0188853955"));
        assertThat(security.getWkn(), is("A0B9GC"));
        assertThat(security.getCurrencyCode(), is("EUR"));

        // check buy sell transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));
        BuySellEntry entry = (BuySellEntry) item.get().getSubject();

        assertThat(entry.getPortfolioTransaction().getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(entry.getPortfolioTransaction().getMonetaryAmount(),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(913.58))));
        assertThat(entry.getPortfolioTransaction().getDate(), is(LocalDate.parse("2014-10-10")));
        assertThat(entry.getPortfolioTransaction().getShares(), is(Values.Share.factorize(422)));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.TAX),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(0))));
        assertThat(entry.getPortfolioTransaction().getUnitSum(Unit.Type.FEE),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(8.90 + 0.75 + 1.67 + 3.50))));
    }
}
