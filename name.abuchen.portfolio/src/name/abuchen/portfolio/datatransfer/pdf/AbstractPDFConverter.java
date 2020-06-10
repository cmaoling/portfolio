package name.abuchen.portfolio.datatransfer.pdf;

import java.io.IOException;
import java.io.InputStream;

import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.Portfolio;

public interface AbstractPDFConverter
{
    abstract public String getPDFAuthor();

    abstract public String getLabel();

    abstract public void setFile(PDFInputFile PDFfile) throws IOException;

    abstract public boolean process();

    abstract public String getDefaultExtractorName();

    default public Account getDefaultAccount()
    {
        return null;
    }

    default public Portfolio getDefaultPortfolio()
    {
        return null;
    }

    abstract public InputStream getStream();
    
}