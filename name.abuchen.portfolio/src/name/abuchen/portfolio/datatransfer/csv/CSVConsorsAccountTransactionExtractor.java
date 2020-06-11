package name.abuchen.portfolio.datatransfer.csv;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Header;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.AccountTransaction.Type;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.money.Money;

/* package */ class CSVConsorsAccountTransactionExtractor extends CSVAccountTransactionExtractor
{
    private List<Pattern> matchPattern = new ArrayList<>();

    /* package */ CSVConsorsAccountTransactionExtractor(Client client)
    {
        super(client, Messages.CSVDefConsorsAccountTransactions);
        matchPattern.add(Pattern.compile("^Umsatzuebersicht_([0-9]{8,10})([_-].*)?\\.csv")); //$NON-NLS-1$
        matchPattern.add(Pattern.compile("^Umsatz.bersicht_([0-9]{8,10})([_-].*)?\\.csv")); //$NON-NLS-1$
    }

    CSVConsorsAccountTransactionExtractor(Client client, String label)
    {
        super(client, label);
    }

    @Override
    public int getDefaultSkipLines()
    {
        return 0;
    }

    @Override
    public String getDefaultEncoding()
    {
        return "UTF-8"; //$NON-NLS-1$
    }

    @SuppressWarnings({ "unchecked", "nls" })
    @Override
    public <E extends Enum<E>> EnumMap<E, String> getDefaultEnum(Class<E> enumType)
    {

        if (enumType.equals(AccountTransaction.Type.class))
        {
            final EnumMap<E, String> enumMap = new EnumMap<>(enumType);
            enumMap.put((E) Type.BUY, "Wertpapierkauf");
            enumMap.put((E) Type.SELL, "Wertpapiergutschrift|Wertpapierverkauf");
            enumMap.put((E) Type.DEPOSIT, "Gutschrift|Gehalt/Rente|Überweisung");
            enumMap.put((E) Type.INTEREST, "Abschluss");
            enumMap.put((E) Type.REMOVAL, "Lastschrift|Dauerauftrag");
            enumMap.put((E) Type.TAXES, "Steuer");
            enumMap.put((E) Type.INTEREST_CHARGE, "Zinsen");
            enumMap.put((E) Type.FEES, "Gebühren");
            return enumMap;
        }
        else
            return null;
    }

    @Override
    public Header.Type getDefaultHeadering()
    {
        return Header.Type.DEFAULT;
    }
    
    @Override   
    public String[] getDefaultHeader()
    {
        String[] defaultHeader = {  "",  //0 //$NON-NLS-1$
                                    Messages.CSVColumn_Date, //1
                                    Messages.CSVColumn_PartnerName, //2
                                    Messages.CSVColumn_IBAN, //3
                                    "", //4 //$NON-NLS-1$
                                    Messages.CSVColumn_Type, //5
                                    Messages.CSVColumn_Note, //6
                                    "", //7 //$NON-NLS-1$
                                    "", //8 //$NON-NLS-1$
                                    "", //9 //$NON-NLS-1$
                                    Messages.CSVColumn_Value, //10
                                    "", //11 //$NON-NLS-1$
                                    "" //12 //$NON-NLS-1$
                                };
        return defaultHeader;
    }

    @Override
    protected  Type inferType(String[] rawValues, Map<String, Column> field2column, Security security, Money amount)
                    throws ParseException
    {
        Type type   = super.inferType(rawValues, field2column, security, amount);
        if (type != null)
        {
            if (amount.isNegative())
                switch (type)
                {
                    case DEPOSIT:
                    case DIVIDENDS:
                    case FEES_REFUND:
                    case INTEREST:
                    case TAX_REFUND:
                        type = type.getSibling();
                        break;
                    default:
                }
            else
                switch (type)
                {
                    case REMOVAL:
                    case DIVIDEND_CHARGE:
                    case FEES:
                    case INTEREST_CHARGE:
                    case TAXES:
                        type = type.getSibling();
                        break;
                    default:
                }
        }
        return type;
    }

    @Override
    public boolean knownFilename(String filename)
    {
        for (Pattern p : matchPattern)
        {
            Matcher m = p.matcher(filename);
            if (m.matches())
                return true;
        }
        return false;
    }

    @Override
    public String extractIban(String filename)
    {
        if (knownFilename(filename))
        {
            for (Pattern p : matchPattern)
            {
                Matcher m = p.matcher(filename);
                if (m.matches())
                {
                    String iban = m.group(1);
                    if (iban.length() < 22)
                        iban = "DE..76030080" + ("0000" + iban).substring(12 - iban.length()); //$NON-NLS-1$ //$NON-NLS-2$
                    return iban;
                }
            }
        }
        return null;
    }
}