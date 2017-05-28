package name.abuchen.portfolio.datatransfer.csv;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.AmountField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.DateField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.EnumField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Field;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.ISINField;
import name.abuchen.portfolio.model.MoneysuiteTransaction;
import name.abuchen.portfolio.model.MoneysuiteTransaction.Type;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.AccountTransferEntry;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.Money;

/* package */ class CSVMoneysuiteTransactionExtractor extends BaseCSVExtractor
{
    /* package */ CSVMoneysuiteTransactionExtractor(Client client)
    {
        super(client, Messages.CSVDefMoneysuiteTransactions);

        List<Field> fields = getFields();
        fields.add(new DateField(Messages.CSVColumn_Date));
        fields.add(new Field(Messages.CSVColumn_TransactionAccount));
        fields.add(new AmountField(Messages.CSVColumn_GrossAmount));
        fields.add(new EnumField<MoneysuiteTransaction.Type>(Messages.CSVColumn_Type, Type.class));
        fields.add(new Field(Messages.CSVColumn_SecurityName).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_WKN).setOptional(true));
        fields.add(new ISINField(Messages.CSVColumn_ISIN).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_TickerSymbol).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Value).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Fees).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_Amount).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_TransactionCurrency).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Shares).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_Note).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_TransactionPeerAccount).setOptional(true));
        fields.add(new Field(Messages.CSVColumn_Note).setOptional(true));
        fields.add(new AmountField(Messages.CSVColumn_Taxes).setOptional(true));
        
    }

    @Override
    protected Money getMoney(String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        Long amount = getAmount(Messages.CSVColumn_GrossAmount, rawValues, field2column);
        if (amount == null)
            throw new ParseException(MessageFormat.format(Messages.CSVImportMissingField, Messages.CSVColumn_GrossAmount), 0);
        String currencyCode = getCurrencyCode(Messages.CSVColumn_TransactionCurrency, rawValues, field2column);
        return Money.of(currencyCode, amount);
    }
    
    @Override
    void extract(List<Item> items, String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        // check if we have a security
        Security security = getSecurity(rawValues, field2column, s -> s.setCurrencyCode(
                        getCurrencyCode(Messages.CSVColumn_TransactionCurrency, rawValues, field2column)));

        System.err.println("CSVMoneysuiteTransactionExtractor.extract: security: " + security.toString());

        // check for the transaction amount
        Money amount = getMoney(rawValues, field2column);

        System.err.println("CSVMoneysuiteTransactionExtractor.extract: security: " + security.toString() + " Money: " + amount.toString());

        // determine type (if not explicitly given by import)
        Type type = inferType(rawValues, field2column, security, amount);

        // extract remaining fields
        LocalDate date = getDate(Messages.CSVColumn_Date, rawValues, field2column);
        if (date == null)
            throw new ParseException(MessageFormat.format(Messages.CSVImportMissingField, Messages.CSVColumn_Date), 0);
        String note = getText(Messages.CSVColumn_Note, rawValues, field2column);
        Long shares = getShares(Messages.CSVColumn_Shares, rawValues, field2column);
        Long taxes = getAmount(Messages.CSVColumn_Taxes, rawValues, field2column);

        switch (type)
        {
            case TRANSFER_IN:
            case TRANSFER_OUT:
                AccountTransferEntry entry = new AccountTransferEntry();
                entry.setAmount(Math.abs(amount.getAmount()));
                entry.setCurrencyCode(amount.getCurrencyCode());
                entry.setDate(date);
                entry.setNote(note);
                items.add(new AccountTransferItem(entry, type == Type.TRANSFER_OUT));
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
                    throw new ParseException(
                                    MessageFormat.format(Messages.CSVImportMissingField, Messages.CSVColumn_Shares), 0);

                BuySellEntry buySellEntry = new BuySellEntry();
                buySellEntry.setType(PortfolioTransaction.Type.valueOf(type.name()));
                buySellEntry.setAmount(Math.abs(amount.getAmount()));
                buySellEntry.setShares(Math.abs(shares));
                buySellEntry.setCurrencyCode(amount.getCurrencyCode());
                buySellEntry.setSecurity(security);
                buySellEntry.setDate(date);
                buySellEntry.setNote(note);
                items.add(new BuySellEntryItem(buySellEntry));
                break;
            case REINVEST:
                System.err.println("CSVMoneysuiteTransactionExtractor.extract: security: " + security.toString() + " [" + date + "," + shares + "]");
                break;
            case DIVIDENDS:
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
                AccountTransaction t = new AccountTransaction();
                switch (type)
                {
                    case DEPOSIT:
                        t.setType(AccountTransaction.Type.DEPOSIT);
                        break;
                    case TAXES:
                        t.setType(AccountTransaction.Type.TAXES);
                        break;
                    case TAX_REFUND:
                        t.setType(AccountTransaction.Type.TAX_REFUND);
                        break;
                    case FEES:
                        t.setType(AccountTransaction.Type.FEES);
                        break;
                    case FEES_REFUND:
                        t.setType(AccountTransaction.Type.FEES_REFUND);
                        break;
                    case INTEREST:
                        t.setType(AccountTransaction.Type.INTEREST);
                        break;
                    case INTEREST_CHARGE:
                        t.setType(AccountTransaction.Type.INTEREST_CHARGE);
                        break;
                    case REMOVAL:
                        t.setType(AccountTransaction.Type.REMOVAL);
                        break;
                    default:
                        throw new IllegalArgumentException(type.toString());                            
                }
                t.setAmount(Math.abs(amount.getAmount()));
                t.setCurrencyCode(amount.getCurrencyCode());
                if (type == Type.DIVIDENDS || type == Type.TAX_REFUND)
                    t.setSecurity(security);
                t.setDate(date);
                t.setNote(note);
                if (shares != null && type == Type.DIVIDENDS)
                    t.setShares(Math.abs(shares));
                if (type == Type.DIVIDENDS && taxes != null && taxes.longValue() != 0)
                    t.addUnit(new Unit(Unit.Type.TAX, Money.of(t.getCurrencyCode(), Math.abs(taxes))));
                items.add(new TransactionItem(t));
                break;
            default:
                throw new IllegalArgumentException(type.toString());
        }
    }

    private Type inferType(String[] rawValues, Map<String, Column> field2column, Security security, Money amount)
                    throws ParseException
    {
        Type type = getEnum(Messages.CSVColumn_Type, Type.class, rawValues, field2column);
        System.err.println("CSVMoneysuiteTransactionExtractor.inferType: type: " + type.toString());
        if (type == null)
            type = extracted();
        return type;
    }

    private Type extracted()
    {
        Type type;
        {
            type = MoneysuiteTransaction.Type.REINVEST;
//            if (security != null)
//                type = amount.isNegative() ? AccountTransaction.Type.REMOVAL : AccountTransaction.Type.DIVIDENDS;
//            else
//                type = amount.isNegative() ? AccountTransaction.Type.REMOVAL : AccountTransaction.Type.DEPOSIT;
        }
        return type;
    }

    public String[] getDefaultHeader()
    {
        String[] defaultHeader = {  "",  //0
                                    "",  //1
                                    "",  //2
                                    Messages.CSVColumn_Date, //3
                                    "",
                                    "",
                                    "",
                                    Messages.CSVColumn_Taxes, //7
                                    Messages.CSVColumn_Amount, //8
                                    "",
                                    "",
                                    Messages.CSVColumn_TransactionPeerAccount, //11
                                    Messages.CSVColumn_Note, //12
                                    Messages.CSVColumn_Type, //13
                                    Messages.CSVColumn_Fees, //14
                                    "",
                                    Messages.CSVColumn_Shares, //16
                                    Messages.CSVColumn_ISIN, //17
                                    "", //18 categories
                                    Messages.CSVColumn_GrossAmount, //19
                                    Messages.CSVColumn_TransactionCurrency, //20 
                                    "",
                                    "",
                                    "",
                                    Messages.CSVColumn_SecurityName, //24
                                    "",
                                    Messages.CSVColumn_CurrencyGrossAmount, //26
                                    "",
                                    "",
                                    "",
                                    "",
                                    Messages.CSVColumn_TransactionAccount, //31
                                    };
        System.err.println("MoneysuiteTransaction.DefaultHeader: " + Arrays.toString(defaultHeader));
        return defaultHeader;
    }
    
}