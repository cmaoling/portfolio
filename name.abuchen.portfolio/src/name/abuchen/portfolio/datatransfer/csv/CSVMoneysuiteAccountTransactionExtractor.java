package name.abuchen.portfolio.datatransfer.csv;

import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.AmountField;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.DateField;
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

/* package */ class CSVMoneysuiteAccountTransactionExtractor extends CSVAccountTransactionExtractor
{
    /* package */ CSVMoneysuiteAccountTransactionExtractor(Client client)
    {
        super(client, Messages.CSVDefMoneysuiteTransactions);
    }
    
    public String[] getDefaultHeader()
    {
        String[] defaultHeader = {  "",  //0
                                    "",  //1
                                    "",  //2
                                    Messages.CSVColumn_Date, //3
                                    "",  //4
                                    "",  //5
                                    "",  //6
                                    "",  //7
                                    "",  //8
                                    "",  //9
                                    "",  //10
                                    "",  //11
                                    Messages.CSVColumn_ISIN, //12
                                    Messages.CSVColumn_Type, //13
                                    "",  //14
                                    Messages.CSVColumn_Note, //15
                                    "",  //16
                                    "",  //17
                                    "",  //18
                                    Messages.CSVColumn_Value, //19
                                    Messages.CSVColumn_TransactionCurrency //20 
                                    };
        System.err.println("DIBAAccount.DefaultHeader: " + Arrays.toString(defaultHeader));
        return defaultHeader;
    }
    
}
