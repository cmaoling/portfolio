package name.abuchen.portfolio.datatransfer.pdf;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.PortfolioLog;
import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.Peer;
import name.abuchen.portfolio.model.Client;

public class AmazonPDFConverter implements AbstractPDFConverter
{
    private final Client client;

    private String[] lines;
    static private String    startTrigger   = "Der Betrag von"; //$NON-NLS-1$
    static private String    stopTrigger    = "Neuer Kontostand"; //$NON-NLS-1$
    static private String    emptyStr       = ""; //$NON-NLS-1$
    static private String    delimiterStr   = ";"; //$NON-NLS-1$
    static private String    newLineStr     = "\n"; //$NON-NLS-1$
    static private String    newLinePattern = "\\r?\\n"; //$NON-NLS-1$
    static private Charset   charset        = Charset.forName("UTF-8"); //$NON-NLS-1$
           private Account   account;
    
    public AmazonPDFConverter(Client client)
    {
        this.client = client;
        this.account = null;
    }

    @Override
    public String getPDFAuthor()
    {
        return "Landesbank Berlin"; //$NON-NLS-1$
    }

    @Override
    public String getLabel()
    {
        return "Amazon"; //$NON-NLS-1$
    }

    @Override
    public void setFile(PDFInputFile PDFfile) throws IOException
    {
        PDFfile.convertPDFtoText();
        lines = PDFfile.getText().split(newLinePattern);   
    }

    @Override
    public boolean process()
    {
        // extract area of interest
        int start = -1;
        int stop  = -1;
        for (int i = 0; i < lines.length; i++)
        {            
            if (lines[i].contains(startTrigger) && start < 0)
                start = i;
            if (lines[i].contains(stopTrigger) && start >= 0)
                stop  = i;
        }
        if ((start >= 0) && (stop >= 0))
        {
            List<String> csv = new ArrayList<>();
            String[] attributes = {"Nummer", "Buchungsdatum", "Kaufdatum", "Umsatz", "currency", "foreign", "Kurs", "amount", "direction", "account"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
            String[] columns = {"Nummer", "Buchungsdatum", "Kaufdatum", "Umsatz", "Fremdwaehrung", "Kurs", "Betrag"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
            csv.add(String.join(delimiterStr, columns));
           
            List<Pattern> skipPattern = new ArrayList<>();
            // None
            
            List<Pattern> matchPattern = new ArrayList<>();
            matchPattern.add(Pattern.compile("^Der Betrag von [\\d.,]{4,} Euro wird am [\\d.]{10} von Ihrem Konto abgebucht. IBAN: (?<Nummer>[A-Z0-9]{22})")); //$NON-NLS-1$
            matchPattern.add(Pattern.compile("^Visa Karte (?<Nummer>[\\d]{4} [\\d]{2}XX XXXX [\\d]{4}) .*")); //$NON-NLS-1$
            matchPattern.add(Pattern.compile("^Kundennummer: (?<account>[\\d]{10})")); //$NON-NLS-1$
            matchPattern.add(Pattern.compile("^(?<Umsatz>ZAHLUNG-LASTSCHRIFT) (?<Buchungsdatum>[\\d.]{10}) (?<amount>[\\d.,]{4,}) (?<direction>[+-])")); //$NON-NLS-1$
            matchPattern.add(Pattern.compile("^(?<Kaufdatum>[\\d.]{10}) (?<Umsatz>.*) (?<Buchungsdatum>[\\d.]{10}) (?<currency>[A-Z]{2,4}) (?<foreign>[\\\\d.,]{4,}) (?<Kurs>[\\\\d.,]{4,}) (?<amount>[\\\\d.,]{4,}) (?<direction>[+-])")); //$NON-NLS-1$
            matchPattern.add(Pattern.compile("^(?<Kaufdatum>[\\d.]{10}) (?<Umsatz>.*) (?<Buchungsdatum>[\\d.]{10}) (?<amount>[\\d.,]{4,}) (?<direction>[+-])")); //$NON-NLS-1$
            matchPattern.add(Pattern.compile("^(?<Umsatz>.* AMAZON PUNKTE).*")); //$NON-NLS-1$
            matchPattern.add(Pattern.compile("^(?<Umsatz>.* AMAZON PRIME PUNKTE).*")); //$NON-NLS-1$

            //convert text to csv
            String[] line = new String[columns.length];
            Arrays.fill(line, emptyStr);
            for (int ii = start; ii <= stop; ii++)
            {
                boolean skip = false;
                for (Pattern p : skipPattern)
                {
                    Matcher m = p.matcher(lines[ii]);
                    if (m.matches())
                    {
                        skip = true;
                        break;
                    }
                }
                if (!skip)
                {
                    boolean match = false;
                    for (Pattern p : matchPattern)
                    {
                        Matcher m = p.matcher(lines[ii]);
                        if (m.matches())
                        {
                            for (String attribute : attributes)
                            {
                                if (p.pattern().contains("<" + attribute + ">")) //$NON-NLS-1$ //$NON-NLS-2$
                                {
                                    String v = m.group(attribute);
                                    if (v != null)
                                    {
                                        boolean found = false;
                                        // combine special elements
                                        if (attribute.equals("amount") || attribute.equals("direction")) //$NON-NLS-1$ //$NON-NLS-2$
                                            attribute = "Betrag"; //$NON-NLS-1$
                                        if (attribute.equals("currency") || attribute.equals("foreign")) //$NON-NLS-1$ //$NON-NLS-2$
                                            attribute = "Fremdwaehrung"; //$NON-NLS-1$
                                        if (attribute.equals("account")) //$NON-NLS-1$
                                        {
                                            for (Account account : client.getAccounts())
                                            {
                                                if (account.getIban() != null)
                                                {
                                                    if (Pattern.matches(v, account.getIban()))
                                                    {
                                                        this.account = account;
                                                    }
                                                }
                                            }
                                        }
                                        else
                                        {
                                            // https://stackoverflow.com/questions/23160832/how-to-find-index-of-string-array-in-java-from-a-given-value
                                            int index = -1;
                                            for (int i=0;i<columns.length;i++)
                                            {
                                                if (columns[i].equals(attribute))
                                                {
                                                    index = i;
                                                    break;
                                                }
                                            }
                                            if (index < columns.length)
                                            {
                                                if (attribute.equals("Betrag")) //$NON-NLS-1$
                                                {
                                                    if (!v.equals("+")) //$NON-NLS-1$
                                                        line[index] = v.concat(line[index]);
                                                }
                                                else if (attribute.equals("Fremdwaehrung")) //$NON-NLS-1$
                                                    line[index] = line[index].concat(v);
                                                else if (attribute.equals("Nummer")) //$NON-NLS-1$
                                                {
                                                    String numberPattern = v.replace("X", ".");  //$NON-NLS-1$ //$NON-NLS-2$
                                                    for (Account account : client.getAccounts())
                                                    {
                                                        if (account.getIban() != null)
                                                        {
                                                            if (Pattern.matches(numberPattern, account.getIban()))
                                                            {
                                                                v = account.getIban();
                                                                found = true;
                                                            }
                                                        }
                                                    }
                                                    if (!found)
                                                    {
                                                        for (Peer peer: client.getPeers())
                                                        {
                                                            if (peer.getIban() != null && Pattern.matches(numberPattern, peer.getIban()))
                                                                v = peer.getIban();
                                                        }
                                                    }
                                                    line[index] = v;
                                                }
                                                else
                                                    line[index] = v;
    
                                                if (!attribute.equals("Nummer")) //$NON-NLS-1$
                                                    match = true;
                                            }
                                            else
                                            {
                                                PortfolioLog.error(new IllegalStateException("attribute " + attribute + " was not found in " + Arrays.toString(columns) + " index received: " + index + " looked up to " + columns.length));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (match)
                        csv.add(String.join(delimiterStr, line));
                }
                // erase all non global elements (first three should stick)
                for (int jj = 3; jj < columns.length; jj++)
                    line[jj] = emptyStr;
            }
            String[] tmp = new String[csv.size()];
            csv.toArray(tmp);
            lines = tmp;
            return true;
        }
        else
        {
            PortfolioLog.error(new IllegalStateException(MessageFormat.format(Messages.PDFImportBlockError, getLabel(), start, stop, Arrays.toString(lines))));
            return false;
        }   
    }

    @Override
    public String getDefaultExtractorName()
    {
        return "name.abuchen.portfolio.datatransfer.csv.CSVAmazonCreditcardTransactionExtractor"; //$NON-NLS-1$
    }

    @Override
    public Account getDefaultAccount()
    {
        return account;
    }

    @Override
    public InputStream getStream()
    {
        String csv  = String.join(newLineStr, lines);
        return new ByteArrayInputStream(csv.getBytes(charset));
    }

}
