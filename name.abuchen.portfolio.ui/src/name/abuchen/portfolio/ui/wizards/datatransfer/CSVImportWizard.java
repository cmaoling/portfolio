package name.abuchen.portfolio.ui.wizards.datatransfer;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.SecurityCache;
import name.abuchen.portfolio.datatransfer.actions.InsertAction;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter;
import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Portfolio;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityPrice;
import name.abuchen.portfolio.ui.Images;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.jobs.ConsistencyChecksJob;
import name.abuchen.portfolio.ui.wizards.AbstractWizardPage;

public class CSVImportWizard extends Wizard
{
    private static class ExtractorProxy implements Extractor
    {
        private final CSVImporter importer;

        public ExtractorProxy(CSVImporter importer)
        {
            this.importer = importer;
        }

        @Override
        public String getLabel()
        {
            return this.importer.getExtractor().getLabel();
        }

        @Override
        public List<Item> extract(SecurityCache securityCache, Extractor.InputFile file, List<Exception> errors)
        {
            return this.importer.createItems(errors);
        }

        @Override
        public List<Item> extract(List<InputFile> file, List<Exception> errors)
        {
            return this.importer.createItems(errors);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends Extractor> T getSubject()
        {
            return (T) this.importer.getExtractor();
        }
    }

    /* package */static final String REVIEW_PAGE_ID = "reviewitems"; //$NON-NLS-1$

    private Client client;
    private IPreferenceStore preferences;
    private CSVImporter importer;

    /**
     * If a target security is given, then only security prices are imported
     * directly into that security.
     */
    private Security security;

    /**
     * If a target account is given, then account is preselected to be imported
     */
    private Account account;

    /**
     * If a target portfolio is given, then portfolio is preselected to be imported
     */
    private Portfolio portfolio;

    private CSVImportDefinitionPage definitionPage;
    private ReviewExtractedItemsPage reviewPage;
    private SelectSecurityPage selectSecurityPage;

    public CSVImportWizard(Client client, IPreferenceStore preferences, File inputFile)
    {
        this.client = client;
        this.preferences = preferences;
        this.importer = new CSVImporter(client, inputFile);
        setWindowTitle(MessageFormat.format(Messages.CSVImportWizardTitle, inputFile.toString()));
    }

    public void setTarget(Portfolio target)
    {
        this.portfolio = target;
    }

    public void setTarget(Security target)
    {
        this.security = target;
    }

    public void setTarget(Account target)
    {
        this.account = target;
    }

    @Override
    public Image getDefaultPageImage()
    {
        return Images.BANNER.image();
    }

    @Override
    public void addPages()
    {
        definitionPage = new CSVImportDefinitionPage(client, importer, security != null);
        definitionPage.setAccount(account);
        addPage(definitionPage);

        selectSecurityPage = new SelectSecurityPage(client);
        addPage(selectSecurityPage);
        
        reviewPage = new ReviewExtractedItemsPage(client, new ExtractorProxy(importer), preferences,
                        Arrays.asList(new Extractor.InputFile(importer.getInputFile())), REVIEW_PAGE_ID);
        if (account != null)
            reviewPage.setAccount(account);
        if (portfolio != null)
            reviewPage.setPortfolio(portfolio);
        reviewPage.setDoExtractBeforeEveryPageDisplay(true);
        addPage(reviewPage);


        AbstractWizardPage.attachPageListenerTo(getContainer());
    }

    @Override
    public boolean canFinish()
    {
        return super.canFinish() && (security != null || getContainer().getCurrentPage() != definitionPage);
    }

    @Override
    public boolean performFinish()
    {
        ((AbstractWizardPage) getContainer().getCurrentPage()).afterPage();

        boolean isDirty = false;

        if (importer.getExtractor() == importer.getSecurityPriceExtractor())
            isDirty = importSecurityPrices();
        else
            isDirty = importItems();

        if (isDirty)
        {
            client.markDirty();
            new ConsistencyChecksJob(client, false).schedule();
        }

        return true;
    }

    private boolean importSecurityPrices()
    {
        Security s = security != null ? security : selectSecurityPage.getSelectedSecurity();

        List<SecurityPrice> prices = importer.createItems(new ArrayList<>()).get(0).getSecurity().getPrices();

        boolean isDirty = false;
        for (SecurityPrice p : prices)
        {
            if (s.addPrice(p))
                isDirty = true;
        }
        return isDirty;
    }

    private boolean importItems()
    {
        InsertAction action = new InsertAction(client);
        action.setConvertBuySellToDelivery(reviewPage.doConvertToDelivery());

        boolean isDirty = false;
        for (ExtractedEntry entry : reviewPage.getEntries())
        {
            if (entry.isImported())
            {
                entry.getItem().apply(action, reviewPage);
                isDirty = true;
            }
        }

        return isDirty;
    }
}
