package name.abuchen.portfolio.datatransfer.csv;

import java.text.ParseException;
import java.util.Map;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Header;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.AccountTransaction.Type;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.money.Money;

/* package */ class CSVAmazonCreditcardTransactionExtractor extends CSVAccountTransactionExtractor
{
    /* package */ CSVAmazonCreditcardTransactionExtractor(Client client)
    {
        super(client, Messages.CSVDefAmazonCreditcardTransactions);
    }

    CSVAmazonCreditcardTransactionExtractor(Client client, String label)
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
                                    "", //2 //$NON-NLS-1$
                                    "", //3 //$NON-NLS-1$
                                    Messages.CSVColumn_Note, //3
                                    Messages.CSVColumn_PartnerName, //3
                                    "", //6 //$NON-NLS-1$
                                    "", //7 //$NON-NLS-1$
                                    Messages.CSVColumn_Value, //6
                                    "", //9 //$NON-NLS-1$
                                    "", //10 //$NON-NLS-1$
                                    "", //11 //$NON-NLS-1$
                                    ""  //12 //$NON-NLS-1$
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

}
