package name.abuchen.portfolio.datatransfer.csv;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Header;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.AccountTransaction.Type;
import name.abuchen.portfolio.model.Client;

/* package */ class CSVDibaAccountTransactionExtractor extends CSVAccountTransactionExtractor
{
    private List<Pattern> matchPattern = new ArrayList<>();

    /* package */ CSVDibaAccountTransactionExtractor(Client client)
    {
        super(client, Messages.CSVDefDibaAccountTransactions);
        sharesOptional = true;
        matchPattern.add(Pattern.compile("^Umsatzanzeige_([A-Z0-9]{22})([_-].*)?\\.csv")); //$NON-NLS-1$
        matchPattern.add(Pattern.compile("^Umsatzanzeige_([0-9]{10})([_-].*)?\\.csv")); //$NON-NLS-1$
    }

    @Override
    public int getDefaultSkipLines()
    {
        return 13;
    }

    @Override
    public String getDefaultEncoding()
    {
        return "windows-1252"; //$NON-NLS-1$
    }

    @SuppressWarnings({ "unchecked", "nls" })
    @Override
    public <E extends Enum<E>> EnumMap<E, String> getDefaultEnum(Class<E> enumType)
    {

        if (enumType.equals(AccountTransaction.Type.class))
        {
            //System.err.println(">>>> CSVDibaAccountTransactionExtratctor:getDefaultEnum enumType IF " + enumType.toString());
            final EnumMap<E, String> enumMap = new EnumMap<>(enumType);
            enumMap.put((E) Type.BUY, "Wertpapierkauf");
            enumMap.put((E) Type.SELL, "Wertpapiergutschrift");
            enumMap.put((E) Type.DEPOSIT, "Gutschrift|Lastschrifteinzug|Überweisungsgutschrift");
            enumMap.put((E) Type.INTEREST, "Abschluss");
            enumMap.put((E) Type.FEES, "Gebühren");
            enumMap.put((E) Type.REMOVAL, "Überweisung|Dauerauftrag|Storno");
            enumMap.put((E) Type.INTEREST_CHARGE, "Zinsen");
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
        String[] defaultHeader = {  Messages.CSVColumn_IBAN, //0
                                    Messages.CSVColumn_Date, //1
                                    Messages.CSVColumn_Note, //2
                                    Messages.CSVColumn_Type, //3
                                    Messages.CSVColumn_ISIN, //4
                                    "", //5 //$NON-NLS-1$
                                    Messages.CSVColumn_TransactionCurrency, //6
                                    Messages.CSVColumn_Value,  //7
                                    "",  //8 //$NON-NLS-1$
                                    "",  //9 //$NON-NLS-1$
                                    ""  //10 //$NON-NLS-1$
                                    };
        return defaultHeader;
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
                        iban = "DE..50010517" + iban; //$NON-NLS-1$
                    return iban;
                }
            }
        }
        return null;
    }
}