package name.abuchen.portfolio.datatransfer.csv;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.AmountField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.DateField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.EnumField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Field;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.IBANField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.ISINField;
import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.AccountTransaction.Type;
import name.abuchen.portfolio.model.AccountTransferEntry;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Peer;
import name.abuchen.portfolio.model.Portfolio;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.util.Iban;

/* package */ public class CSVAccountTransactionExtractor extends BaseCSVExtractor
{
    protected boolean sharesOptional;

    /* package */ CSVAccountTransactionExtractor(Client client)
    {
        super(client, Messages.CSVDefAccountTransactions);

        addFields();
        sharesOptional = false;
    }

    CSVAccountTransactionExtractor(Client client, String label)
    {
        super(client, label);
        addFields();
    }
    
    List<Field> addFields()
    {
        List<Field> fields = getFields();
        fields.add(new DateField(Messages.CSVColumn_Date));
        fields.add(new Field(Messages.CSVColumn_Time).setOptional(true));
        fields.add(new ISINField(Messages.CSVColumn_ISIN).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_TickerSymbol).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_WKN).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Value));
        fields.add(new Field(Messages.CSVColumn_TransactionCurrency).setOptional(true));
        fields.add(new EnumField<AccountTransaction.Type>(Messages.CSVColumn_Type, Type.class).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_SecurityName).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Shares).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_Note).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Taxes).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Fees).setOptional(true));
        fields.add(new IBANField(Messages.CSVColumn_IBAN).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_PartnerName).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_AccountName).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_AccountName2nd).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_PortfolioName).setOptional(true));

        fields.add(new AmountField(Messages.CSVColumn_GrossAmount).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_CurrencyGrossAmount).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_ExchangeRate).setOptional(true));
        return fields;
    }

    @Override
    void extract(List<Item> items, String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        // check if we have a security
        Security security = getSecurity(rawValues, field2column, s -> s.setCurrencyCode(
                        getCurrencyCode(Messages.CSVColumn_TransactionCurrency, rawValues, field2column)));

        // check for the transaction amount
        Money amount = getMoney(rawValues, field2column);

        // determine type (if not explicitly given by import)
        Type type = inferType(rawValues, field2column, security, amount);

        // extract remaining fields
        LocalDateTime date = getDate(Messages.CSVColumn_Date, Messages.CSVColumn_Time, rawValues, field2column);
        if (date == null)
            throw new ParseException(MessageFormat.format(Messages.CSVImportMissingField, Messages.CSVColumn_Date), 0);
        String note = getText(Messages.CSVColumn_Note, rawValues, field2column);
        Long shares = getShares(Messages.CSVColumn_Shares, rawValues, field2column);
        Long taxes = getAmount(Messages.CSVColumn_Taxes, rawValues, field2column);
        Long fees = getAmount(Messages.CSVColumn_Fees, rawValues, field2column);

        Optional<Unit> grossAmount = extractGrossAmount(rawValues, field2column, amount);

        Account account = getAccount(getClient(), rawValues, field2column);
        Account account2nd = getAccount(getClient(), rawValues, field2column, true);
        Portfolio portfolio = getPortfolio(getClient(), rawValues, field2column);

        Extractor.Item item = null;

        Peer peer = getPeer(rawValues, field2column, p -> {});
        // DEBUG: System.err.println("CSVAccountTransactionExtratctor:extract peer: " + (peer != null?peer.toString():"")); //$NON-NLS-1$  //$NON-NLS-2$
        if (peer != null && peer.links2Account())
        {
            if (type == Type.DEPOSIT)
                type = Type.TRANSFER_IN;
            else if (type == Type.REMOVAL)
                type = Type.TRANSFER_OUT;
        }

        switch (type)
        {
            case TRANSFER_IN:
            case TRANSFER_OUT:
                AccountTransferEntry entry = new AccountTransferEntry();
                entry.setAmount(Math.abs(amount.getAmount()));
                entry.setCurrencyCode(amount.getCurrencyCode());
                entry.setDate(date.withHour(0).withMinute(0));
                entry.setNote(note);

                if (peer != null)
                {
                    if (type == Type.TRANSFER_OUT)
                        entry.getSourceTransaction().setPeer(peer);
                    else if (type == Type.TRANSFER_IN)
                        entry.getTargetTransaction().setPeer(peer);
                }
                item = new AccountTransferItem(entry, type == Type.TRANSFER_OUT);
                break;
            case BUY:
            case SELL:
                if (security == null)
                    throw new ParseException(MessageFormat.format(Messages.CSVImportMissingSecurity,
                                    new StringJoiner(", ").add(Messages.CSVColumn_ISIN) //$NON-NLS-1$
                                                    .add(Messages.CSVColumn_TickerSymbol).add(Messages.CSVColumn_WKN)
                                                    .toString()),
                                    0);
                if (shares == null)
                    if (!sharesOptional)
                        throw new ParseException(
                                   MessageFormat.format(Messages.CSVImportMissingField, Messages.CSVColumn_Shares), 0);

                BuySellEntry buySellEntry = new BuySellEntry();
                buySellEntry.setType(PortfolioTransaction.Type.valueOf(type.name()));
                buySellEntry.setAmount(Math.abs(amount.getAmount()));
                if (shares != null)
                    buySellEntry.setShares(Math.abs(shares));
                buySellEntry.setCurrencyCode(amount.getCurrencyCode());
                buySellEntry.setSecurity(security);
                buySellEntry.setDate(date);
                buySellEntry.setNote(note);

                if (taxes != null && taxes.longValue() != 0)
                    buySellEntry.getPortfolioTransaction().addUnit(new Unit(Unit.Type.TAX, Money
                                    .of(buySellEntry.getPortfolioTransaction().getCurrencyCode(), Math.abs(taxes))));

                if (fees != null && fees.longValue() != 0)
                    buySellEntry.getPortfolioTransaction().addUnit(new Unit(Unit.Type.FEE, Money
                                    .of(buySellEntry.getPortfolioTransaction().getCurrencyCode(), Math.abs(fees))));

                if (buySellEntry.getPortfolioTransaction().getAmount() == 0L
                                && buySellEntry.getPortfolioTransaction().getType() == PortfolioTransaction.Type.SELL)
                {
                    // convert to outbound delivery if amount is 0
                    PortfolioTransaction tx = buySellEntry.getPortfolioTransaction();
                    item = new TransactionItem(convertToOutboundDelivery(tx));
                }
                else
                {
                    item = new BuySellEntryItem(buySellEntry);
                }

                break;
            case DIVIDENDS:
            case DIVIDEND_CHARGE:
                // dividends must have a security
                if (security == null)
                    throw new ParseException(MessageFormat.format(Messages.CSVImportMissingSecurity,
                                    new StringJoiner(", ").add(Messages.CSVColumn_ISIN) //$NON-NLS-1$
                                                    .add(Messages.CSVColumn_TickerSymbol).add(Messages.CSVColumn_WKN)
                                                    .toString()),
                                    0);
            case DEPOSIT:
            case TAXES:
            case TAX_REFUND:
            case FEES:
            case FEES_REFUND:
            case INTEREST:
            case INTEREST_CHARGE:
            case REMOVAL:
                boolean dividendType = (type == Type.DIVIDENDS || type == Type.DIVIDEND_CHARGE);
                AccountTransaction t = new AccountTransaction();
                t.setType(type);
                t.setAmount(Math.abs(amount.getAmount()));
                t.setCurrencyCode(amount.getCurrencyCode());
                if (dividendType || type == Type.TAXES || type == Type.TAX_REFUND || type == Type.FEES || type == Type.FEES_REFUND)
                    t.setSecurity(security);
                t.setDateTime(date.withHour(0).withMinute(0));
                String isinNote = getText(Messages.CSVColumn_ISIN, rawValues, field2column);
                if (isinNote != null && security.getIsin().equals("")) //$NON-NLS-1$
                {
                    if (note == null)
                        note = Messages.LabelNothing;
                    else if (!note.equals("")) //$NON-NLS-1$
                        note += " - "; //$NON-NLS-1$
                    note += isinNote;
                }
                String ibanNote = getText(Messages.CSVColumn_IBAN, rawValues, field2column);
                // DEBUG: System.err.println("CSVAccountTransactionExtratctor:extract ibanNote: " + (ibanNote != null?ibanNote:"")); //$NON-NLS-1$  //$NON-NLS-2$
                if (!(ibanNote == null || Iban.isValid(ibanNote)))
                {
                    note = ibanNote + (note != null?" - "+note:"") ; //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (dividendType)
                {
                    if (shares != null)
                        t.setShares(Math.abs(shares));
                }
                if (dividendType && taxes != null && taxes.longValue() != 0)
                    t.addUnit(new Unit(Unit.Type.TAX, Money.of(t.getCurrencyCode(), Math.abs(taxes))));

                if (dividendType && fees != null && fees.longValue() != 0)
                    t.addUnit(new Unit(Unit.Type.FEE, Money.of(t.getCurrencyCode(), Math.abs(fees))));

                t.setNote(note);
                if ((type == Type.DEPOSIT || type == Type.REMOVAL) && peer != null)
                    t.setPeer(peer);

                if (type == Type.INTEREST)
                {
                    if (taxes != null && taxes.longValue() != 0)
                        t.addUnit(new Unit(Unit.Type.TAX, Money.of(t.getCurrencyCode(), Math.abs(taxes))));
                }

                if (security != null && grossAmount.isPresent())
                {
                    // gross amount can only be relevant if a transaction is
                    // linked to a security (dividend, taxes, fees, and refunds)

                    t.addUnit(grossAmount.get());
                }

                item = new TransactionItem(t);
                break;
            default:
                throw new IllegalArgumentException(type.toString());
        }
        item.setAccountPrimary(account);
        item.setAccountSecondary(account2nd);
        item.setPortfolioPrimary(portfolio);

        items.add(item);

        // TODO: still needed for debug? for (Item item : items)
        // TODO: still needed for debug?     System.err.println("CSVAccountTransactionExtratctor:extract items: " + item.getClass().toString() + " = " + item.toString() );         //$NON-NLS-1$ //$NON-NLS-2$
    }

    private PortfolioTransaction convertToOutboundDelivery(PortfolioTransaction tx)
    {
        PortfolioTransaction delivery = new PortfolioTransaction();
        delivery.setType(PortfolioTransaction.Type.DELIVERY_OUTBOUND);
        delivery.setDateTime(tx.getDateTime());
        delivery.setAmount(tx.getAmount());
        delivery.setCurrencyCode(tx.getCurrencyCode());
        delivery.setShares(tx.getShares());
        delivery.setSecurity(tx.getSecurity());
        delivery.setNote(tx.getNote());
        delivery.addUnits(tx.getUnits());
        return delivery;
    }

    protected Type inferType(String[] rawValues, Map<String, Column> field2column, Security security, Money amount)
                    throws ParseException
    {
        Type type = getEnum(Messages.CSVColumn_Type, Type.class, rawValues, field2column);
        if (type == null)
        {
            if (security != null)
                type = amount.isNegative() ? AccountTransaction.Type.REMOVAL : AccountTransaction.Type.DIVIDENDS;
            else
                type = amount.isNegative() ? AccountTransaction.Type.REMOVAL : AccountTransaction.Type.DEPOSIT;
        }
        return type;
    }
}
