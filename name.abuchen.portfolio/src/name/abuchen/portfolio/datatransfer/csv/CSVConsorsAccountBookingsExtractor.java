package name.abuchen.portfolio.datatransfer.csv;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.model.Client;

/* package */ class CSVConsorsAccountBookingExtractor extends CSVConsorsAccountTransactionExtractor
{
    /* package */ CSVConsorsAccountBookingExtractor(Client client)
    {
        super(client, Messages.CSVDefConsorsAccountBookings);
    }

    @Override
    public int getDefaultSkipLines()
    {
        return 0;
    }


    @Override
    public String getDefaultEncoding()
    {
        return "windows-1252";
    }
    
    @Override   
    public String[] getDefaultHeader()
    {
        String[] defaultHeader = {  "",  //0
                                    Messages.CSVColumn_Date, //1
                                    Messages.CSVColumn_PartnerName, //2
                                    Messages.CSVColumn_IBAN, //3
                                    "", //4
                                    Messages.CSVColumn_Type, //5
                                    Messages.CSVColumn_ISIN, //6
                                    Messages.CSVColumn_Value, //7
                                    "", //8
                                    "" //9
                                                                        };
        //System.err.println(">>>> CSVDibaAccountTransactionExtratctor:DefaultHeader: " + Arrays.toString(defaultHeader));
        return defaultHeader;
    }
    
}
