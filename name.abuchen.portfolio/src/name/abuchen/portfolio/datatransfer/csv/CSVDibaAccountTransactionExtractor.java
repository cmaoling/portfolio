package name.abuchen.portfolio.datatransfer.csv;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.AmountField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.DateField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Header;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.ISINField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.EnumField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Field;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.AccountTransaction.Type;
import name.abuchen.portfolio.model.AccountTransferEntry;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.Money;

/* package */ class CSVDibaAccountTransactionExtractor extends CSVAccountTransactionExtractor
{
    /* package */ CSVDibaAccountTransactionExtractor(Client client)
    {
        super(client, Messages.CSVDefDibaAccountTransactions);
    }

    @Override
    public int getDefaultSkipLines()
    {
        return 7;
    }

    @Override
    public String getDefaultEncoding()
    {
        return "windows-1252";
    }

    @Override
    public <E extends Enum<E>> EnumMap<E, String> getDefaultEnum(Class<E> enumType)
    {

        if (enumType.equals(AccountTransaction.Type.class))
        {
            //System.err.println(">>>> CSVDibaAccountTransactionExtratctor:getDefaultEnum enumType IF " + enumType.toString());
            final EnumMap<E, String> enumMap = new EnumMap<>(enumType);
            enumMap.put((E) Type.DEPOSIT, "Gutschrift");
            enumMap.put((E) Type.INTEREST, "Abschluss");
            enumMap.put((E) Type.REMOVAL, "Überweisung");
            return enumMap;
        }
        else
        {
            //System.err.println(">>>> CSVDibaAccountTransactionExtratctor:getDefaultEnum enumType ELSE " + enumType.toString());
            return null;
        }
    }

    @Override
    
    public Header.Type getDefaultHeadering()
    {
        return Header.Type.DEFAULT;
    }
    
    @Override   
    public String[] getDefaultHeader()
    {
        String[] defaultHeader = {  "",  //0
                                    Messages.CSVColumn_Date, //1
                                    Messages.CSVColumn_Note, //2
                                    Messages.CSVColumn_Type, //3
                                    Messages.CSVColumn_ISIN, //4
                                    Messages.CSVColumn_Value, //5
                                    Messages.CSVColumn_TransactionCurrency //6 
                                    };
        //System.err.println(">>>> CSVDibaAccountTransactionExtratctor:DefaultHeader: " + Arrays.toString(defaultHeader));
        return defaultHeader;
    }
    
}
