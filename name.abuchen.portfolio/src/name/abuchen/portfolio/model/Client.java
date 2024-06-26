package name.abuchen.portfolio.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.model.AttributeType.PeriodConverter;
import name.abuchen.portfolio.model.ClientAttribute.AttributeFieldOption;
import name.abuchen.portfolio.model.Classification.Assignment;
import name.abuchen.portfolio.money.CurrencyUnit;

public class Client implements Attributable
{
    /* package */static final int MAJOR_VERSION = 1;

     public interface Properties // NOSONAR
    {
        String TAXONOMIES = "taxonomies"; //$NON-NLS-1$
        String WATCHLISTS = "watchlists"; //$NON-NLS-1$
    }

    public static final int CURRENT_VERSION = 59;
    public static final int VERSION_WITH_CURRENCY_SUPPORT = 29;
    public static final int VERSION_WITH_UNIQUE_FILTER_KEY = 57;

    private transient PropertyChangeSupport propertyChangeSupport; // NOSONAR

    /**
     * The (minor) version of the file format. If it is lower than the current
     * version, then {@link ClientFactory#upgradeModel} will upgrade the model
     * and set the version number to the current version.
     */
    private int version = CURRENT_VERSION;

    /**
     * The (minor) version of the file format as it has been read from file.
     */
    private transient int fileVersionAfterRead = CURRENT_VERSION; // NOSONAR

    private String baseCurrency = CurrencyUnit.EUR;
    private String backupDirectory;

    private List<Security> securities = new ArrayList<>();
    private List<Watchlist> watchlists;

    // keep typo -> xstream deserialization
    @Deprecated
    private List<ConsumerPriceIndex> consumerPriceIndeces;

    private List<Account> accounts = new ArrayList<>();
    private List<Portfolio> portfolios = new ArrayList<>();
    private List<InvestmentPlan> plans;
    private List<Taxonomy> taxonomies;
    private List<Dashboard> dashboards;
    private PeerList peers = new PeerList();

    private Map<String, String> properties;
    private ClientSettings settings;

    private Boolean autosaveWithDatestamp;
    private Long autosavePeriod;
    private Boolean loadSettingsNextToFile;

    @Deprecated
    private String industryTaxonomyId;

    @Deprecated
    private Category rootCategory;

    private transient SecretKey secret; // NOSONAR

    public Client()
    {
        doPostLoadInitialization();
    }

    /* package */final void doPostLoadInitialization()
    {
        // when loading the Client from XML, attributes that are not (yet)
        // persisted in that version are not initialized

        if (watchlists == null)
            watchlists = new ArrayList<>();

        if (consumerPriceIndeces == null)
            consumerPriceIndeces = new ArrayList<>();

        if (properties == null)
            properties = new HashMap<>();

        if (propertyChangeSupport == null)
            propertyChangeSupport = new PropertyChangeSupport(this);

        if (plans == null)
            plans = new ArrayList<>();

        if (taxonomies == null)
            taxonomies = new ArrayList<>();

        if (dashboards == null)
            dashboards = new ArrayList<>();

        if (peers == null)
        {
            peers = new PeerList();
        }

        if (settings == null)
            settings = new ClientSettings();
        else
        {
            settings.doPostLoadInitialization();
            addDefaultClientAttributes();
        }
        if (backupDirectory == null)
            setBackupDirectory(""); //$NON-NLS-1$

        if (autosaveWithDatestamp == null)
            autosaveWithDatestamp = false;

        if (autosavePeriod == null)
            autosavePeriod = Long.valueOf(5);

        if (loadSettingsNextToFile == null)
            loadSettingsNextToFile = false;
    }

    private void addDefaultClientAttributes()
    {
        ClientAttribute backupDir = new ClientAttribute("backupDirectory"); //$NON-NLS-1$
        backupDir.setColumnLabel(Messages.LabelClientAttributeBackupDirectory);
        backupDir.setParent(this);
        backupDir.setType(Path.class);
        backupDir.setEdit(true);
        settings.addClientAttributes(backupDir);

        ClientAttribute autosavePeriod = new ClientAttribute("autosavePeriod"); //$NON-NLS-1$
        autosavePeriod.setColumnLabel(Messages.LabelClientAttributeAutosavePeriod);
        autosavePeriod.setParent(this);
        autosavePeriod.setType(Long.class);
        autosavePeriod.setConverter(PeriodConverter.class);
        autosavePeriod.setEdit(true);
        settings.addClientAttributes(autosavePeriod);

        ClientAttribute autosaveWithDatestamp = new ClientAttribute("autosaveWithDatestamp"); //$NON-NLS-1$
        autosaveWithDatestamp.setColumnLabel(Messages.LabelClientAttributeAutosaveDatestamp);
        autosaveWithDatestamp.setParent(this);
        autosaveWithDatestamp.setType(Boolean.class);
        autosaveWithDatestamp.addOptions(new AttributeFieldOption(Messages.LabelBooleanFalse, false));
        autosaveWithDatestamp.addOptions(new AttributeFieldOption(Messages.LabelBooleanTrue, true));
        autosaveWithDatestamp.setEdit(true);
        settings.addClientAttributes(autosaveWithDatestamp);

        ClientAttribute loadSettingsNextToFile = new ClientAttribute("loadSettingsNextToFile"); //$NON-NLS-1$
        loadSettingsNextToFile.setColumnLabel(Messages.LabelClientAttributeLoadSettings);
        loadSettingsNextToFile.setParent(this);
        loadSettingsNextToFile.setType(Boolean.class);
        loadSettingsNextToFile.addOptions(new AttributeFieldOption(Messages.LabelBooleanFalse, false));
        loadSettingsNextToFile.addOptions(new AttributeFieldOption(Messages.LabelBooleanTrue, true));
        loadSettingsNextToFile.setEdit(true);
        settings.addClientAttributes(loadSettingsNextToFile);

    }
 
    /* package */int getVersion()
    {
        return version;
    }

    /* package */void setVersion(int version)
    {
        this.version = version;
    }

    public int getFileVersionAfterRead()
    {
        return fileVersionAfterRead;
    }

    /* package */
    void setFileVersionAfterRead(int fileVersionAfterRead)
    {
        this.fileVersionAfterRead = fileVersionAfterRead;
    }

    public boolean shouldDoFilterMigration()
    {
        return getFileVersionAfterRead() < Client.VERSION_WITH_UNIQUE_FILTER_KEY;
    }

    public String getBaseCurrency()
    {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency)
    {
        propertyChangeSupport.firePropertyChange("baseCurrency", this.baseCurrency, this.baseCurrency = baseCurrency); //$NON-NLS-1$ //NOSONAR
    }

    @Override
    public Attributes getAttributes()
    {
        Attributes attributes = new Attributes();
        List<ClientAttribute> clientAttributeList = settings.getClientAttributes();
        for (ClientAttribute attrib : clientAttributeList)
            attributes.put(attrib, attrib.getValue());
        return attributes;
    }

    @Override
    public void setAttributes(Attributes attributes)
    {
        List<ClientAttribute> clientAttributeList = settings.getClientAttributes();
        for (ClientAttribute attrib : clientAttributeList)
            attrib.setValue(attributes.get(attrib));
    }

    public ClientAttribute getAttribute(String attribute)
    {
        List<ClientAttribute> clientAttributeList = settings.getClientAttributes();
        for (ClientAttribute attrib : clientAttributeList)
            if (attrib.getId().equals(attribute))
                return attrib;
        return null;
    }
    
    public Path getBackupDirectory()
    {
        try
        {
            return Paths.get(new URI(backupDirectory));
        }
        catch (URISyntaxException | IllegalArgumentException e)
        {
            // If given file string isn't an URL, fall back to using a normal file 
            return Paths.get(backupDirectory);
        }
    }

    public void setBackupDirectory(String directory)
    {
        backupDirectory = directory;
    }

    public void setBackupDirectory(Path path)
    {
        setBackupDirectory(path.toUri().toString());
    }

    public long getAutosavePeriod()
    {
        return autosavePeriod;
    }

    public void setAutosavePeriod(long period)
    {
        autosavePeriod = period;
        if (period > 0)
            getAttribute("autosaveWithDatestamp").setEdit(true); //$NON-NLS-1$
        else
            getAttribute("autosaveWithDatestamp").setEdit(false); //$NON-NLS-1$
    }

    public Boolean getAutosaveWithDatestamp()
    {
        return autosaveWithDatestamp;
    }

    public void setAutosaveWithDatestamp(Boolean enable)
    {
        autosaveWithDatestamp = enable;
    }

    public Boolean getLoadSettingsNextToFile()
    {
        return loadSettingsNextToFile;
    }

    public void setLoadSettingsNextToFile(Boolean enable)
    {
        loadSettingsNextToFile = enable;
    }

    public List<InvestmentPlan> getPlans()
    {
        return Collections.unmodifiableList(plans);
    }

    public void addPlan(InvestmentPlan plan)
    {
        plans.add(plan);
    }

    public void removePlan(InvestmentPlan plan)
    {
        plans.remove(plan);
    }

    public PeerList getPeers()
    {
        return peers;
    }

    public boolean addPeer(Peer peer)
    {
        return peers.add(peer);
    }

    public boolean removePeer(Peer peer)
    {
        return peers.remove(peer);
    }

    public List<Security> getSecurities()
    {
        return Collections.unmodifiableList(securities);
    }

    /**
     * Returns a sorted list of active securities, i.e. securities that are not
     * marked as retired.
     */
    public List<Security> getActiveSecurities()
    {
        return securities.stream() //
                        .filter(s -> s.getCurrencyCode() != null) //
                        .filter(s -> !s.isRetired()) //
                        .sorted(new Security.ByName()) //
                        .collect(Collectors.toList());
    }

    public void addSecurity(Security security)
    {
        Objects.requireNonNull(security);

        securities.add(security);

        propertyChangeSupport.firePropertyChange("securities", null, security); //$NON-NLS-1$
    }

    public void removeSecurity(final Security security)
    {
        for (Watchlist w : watchlists)
            w.getSecurities().remove(security);
        deleteInvestmentPlans(security);
        deleteTaxonomyAssignments(security);
        deleteAccountTransactions(security);
        deletePortfolioTransactions(security);

        securities.remove(security);

        propertyChangeSupport.firePropertyChange("securities", security, null); //$NON-NLS-1$
    }

    /**
     * Gets a list of used {@link CurrencyUnit}s.
     * 
     * @return list
     */
    public List<CurrencyUnit> getUsedCurrencies()
    {
        // collect all used currency codes
        HashSet<String> hsUsedCodes = new HashSet<>();
        // first client and all accounts
        hsUsedCodes.add(baseCurrency);
        for (Account account : accounts)
        {
            hsUsedCodes.add(account.getCurrencyCode());
        }
        // then portfolios
        for (Portfolio portfolio : portfolios)
        {
            for (PortfolioTransaction t : portfolio.getTransactions())
            {
                hsUsedCodes.add(t.getCurrencyCode());
            }
        }
        // then from all securities
        for (Security security : securities)
        {
            hsUsedCodes.add(security.getCurrencyCode());
        }
        // now get the currency units
        List<CurrencyUnit> lUnits = new ArrayList<>();
        for (String code : hsUsedCodes)
        {
            CurrencyUnit unit = CurrencyUnit.getInstance(code);
            if (unit != null)
            {
                lUnits.add(unit);
            }
        }
        // sort list to allow using it as a favorite list
        Collections.sort(lUnits);
        return lUnits;
    }

    public List<Watchlist> getWatchlists()
    {
        return watchlists;
    }

    @Deprecated
    /* package */ List<ConsumerPriceIndex> getConsumerPriceIndices() // NOSONAR
    {
        return Collections.unmodifiableList(consumerPriceIndeces); // NOSONAR
    }

    public void addAccount(Account account)
    {
        accounts.add(account);
    }

    public void removeAccount(Account account)
    {
        deleteReferenceAccount(account);
        deleteTransactions(account);
        deleteInvestmentPlans(account);
        deleteTaxonomyAssignments(account);
        accounts.remove(account);
    }

    public List<Account> getAccounts()
    {
        return getAccounts(true);
    }

    public List<Account> getAccounts(boolean readonly)
    {
        if (readonly)
            return Collections.unmodifiableList(accounts);
        else
            return accounts.stream() //
                            .sorted(new Account.ByName()) //
                            .collect(Collectors.toList());
    }

    /**
     * Returns a sorted list of active accounts, i.e. accounts that are not
     * marked as retired.
     */
    public List<Account> getActiveAccounts()
    {
        return accounts.stream() //
                        .filter(a -> !a.isRetired()) //
                        .sorted(new Account.ByName()) //
                        .collect(Collectors.toList());
    }

    /**
     * Returns a sorted list of accounts, which seem to match w/ the IBAN/Account.
     */
    public List<Account> getProposedAccount(String identifier)
    {
        List<Account> accounts = new ArrayList<>();
        if (identifier == null)
            return accounts;
        String numberPattern = identifier.replace("X", "."); //$NON-NLS-1$ //$NON-NLS-2$
        for (Account account : getAccounts())
            if (account.getIban() != null && Pattern.matches(numberPattern, account.getIban()))
                accounts.add(account);
        return accounts;
    }

    public void addPortfolio(Portfolio portfolio)
    {
        portfolios.add(portfolio);
    }

    public void removePortfolio(Portfolio portfolio)
    {
        deleteTransactions(portfolio);
        deleteInvestmentPlans(portfolio);
        portfolios.remove(portfolio);
    }

    public List<Portfolio> getPortfolios()
    {
        return getPortfolios(true);
    }

    public List<Portfolio> getPortfolios(boolean readonly)
    {
        if (readonly)
            return Collections.unmodifiableList(portfolios);
        else
            return portfolios;
    }

    /**
     * Returns a sorted list of active portfolios, i.e. portfolios that are not
     * marked as retired.
     */
    public List<Portfolio> getActivePortfolios()
    {
        return portfolios.stream() //
                        .filter(p -> !p.isRetired()) //
                        .sorted(new Portfolio.ByName()) //
                        .collect(Collectors.toList());
    }

    @Deprecated
    /* package */
    Category getRootCategory()
    {
        return this.rootCategory;
    }

    @Deprecated
    /* package */
    void setRootCategory(Category rootCategory)
    {
        this.rootCategory = rootCategory;
    }

    @Deprecated
    /* package */
    String getIndustryTaxonomy()
    {
        return industryTaxonomyId;
    }

    @Deprecated
    /* package */
    void setIndustryTaxonomy(String industryTaxonomyId)
    {
        this.industryTaxonomyId = industryTaxonomyId;
    }

    public List<Taxonomy> getTaxonomies()
    {
        return Collections.unmodifiableList(taxonomies);
    }

    public void addTaxonomy(Taxonomy taxonomy)
    {
        taxonomies.add(taxonomy);
        propertyChangeSupport.firePropertyChange(Properties.TAXONOMIES, null, taxonomy);
    }

    public void addTaxonomy(int index, Taxonomy taxonomy)
    {
        taxonomies.add(index, taxonomy);
    }

    public void swapTaxonomy(Taxonomy first, Taxonomy second)
    {
        int p1 = taxonomies.indexOf(first);
        int p2 = taxonomies.indexOf(second);

        if (p1 >= 0 && p2 >= 0)
            Collections.swap(taxonomies, p1, p2);
    }

    public void removeTaxonomy(Taxonomy taxonomy)
    {
        if (taxonomies.remove(taxonomy))
            propertyChangeSupport.firePropertyChange(Properties.TAXONOMIES, taxonomy, null);
    }

    public Taxonomy getTaxonomy(String id)
    {
        return taxonomies.stream() //
                        .filter(t -> id.equals(t.getId())) //
                        .findAny().orElse(null);
    }

    public Stream<Dashboard> getDashboards()
    {
        return dashboards.stream();
    }

    public void addDashboard(Dashboard dashboard)
    {
        this.dashboards.add(dashboard);
    }

    public void addDashboard(int index, Dashboard dashboard)
    {
        this.dashboards.add(index, dashboard);
    }

    public void removeDashboard(Dashboard dashboard)
    {
        this.dashboards.remove(dashboard);
    }

    public ClientSettings getSettings()
    {
        return settings;
    }

    public void setProperty(String key, String value)
    {
        String oldValue = properties.put(key, value);
        propertyChangeSupport.firePropertyChange("properties", oldValue, value); //$NON-NLS-1$
    }

    public String removeProperty(String key)
    {
        String oldValue = properties.remove(key);
        propertyChangeSupport.firePropertyChange("properties", oldValue, null); //$NON-NLS-1$
        return oldValue;
    }

    public String getProperty(String key)
    {
        return properties.get(key);
    }

    public void voidConsumerPriceIndeces()
    {
       consumerPriceIndeces = null; //NOSONAR
    }

    /**
     * Returns the current value of the integer-valued state with the given
     * name. Returns the value <code>0</code> if there is no value with the
     * given name, or if the current value cannot be treated as an integer.
     */
    public int getPropertyInt(String key)
    {
        try
        {
            String v = properties.get(key);
            return v == null ? 0 : Integer.parseInt(v);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    /* package */Map<String, String> getProperties()
    {
        return properties;
    }

    /* package */void clearProperties()
    {
        properties.clear();
    }

    /**
     * Returns all transactions. Transactions are "de-duplicated", i.e. the list
     * only includes the PortfolioTransaction of buy and sell transactions and
     * it includes only the outbound transactions of cash or security transfers.
     */
    public List<TransactionPair<?>> getAllTransactions()
    {
        List<TransactionPair<?>> transactions = new ArrayList<>();

        for (Portfolio portfolio : portfolios)
            portfolio.getTransactions().stream().filter(t -> t.getType() != PortfolioTransaction.Type.TRANSFER_IN)
                            .map(t -> new TransactionPair<>(portfolio, t)).forEach(transactions::add);

        EnumSet<AccountTransaction.Type> exclude = EnumSet.of(AccountTransaction.Type.TRANSFER_IN,
                        AccountTransaction.Type.BUY, AccountTransaction.Type.SELL);

        for (Account account : accounts)
        {
            account.getTransactions().stream().filter(t -> !exclude.contains(t.getType()))
                            .map(t -> new TransactionPair<>(account, t)).forEach(transactions::add);
        }

        return transactions;
    }

    /* package */ SecretKey getSecret()
    {
        return secret;
    }

    /* package */ void setSecret(SecretKey secret)
    {
        this.secret = secret;
    }

    /**
     * Removes the given account as reference account from any portfolios. As
     * the model expects that there is always a reference account, an arbitrary
     * other account is picked as reference account instead. Or, if no other
     * account exists, a new account is created and used as reference account.
     */
    private void deleteReferenceAccount(Account account)
    {
        for (Portfolio portfolio : portfolios)
        {
            if (account.equals(portfolio.getReferenceAccount()))
            {
                portfolio.setReferenceAccount(null);

                accounts.stream().filter(a -> !account.equals(a)).findAny().ifPresent(portfolio::setReferenceAccount);

                if (portfolio.getReferenceAccount() == null)
                {
                    Account referenceAccount = new Account();
                    referenceAccount.setName(MessageFormat.format(Messages.LabelDefaultReferenceAccountName,
                                    portfolio.getName()));
                    addAccount(referenceAccount);
                    portfolio.setReferenceAccount(referenceAccount);
                }
            }
        }
    }

    /**
     * Delete all transactions including cross entries and transactions created
     * by an investment plan.
     */
    private <T extends Transaction> void deleteTransactions(TransactionOwner<T> owner)
    {
        // use a copy because #removeTransaction modifies the list
        for (T t : new ArrayList<T>(owner.getTransactions()))
            owner.deleteTransaction(t, this);
    }

    private void deleteInvestmentPlans(Portfolio portfolio)
    {
        for (InvestmentPlan plan : new ArrayList<InvestmentPlan>(plans))
        {
            if (portfolio.equals(plan.getPortfolio()))
                removePlan(plan);
        }
    }

    private void deleteInvestmentPlans(Account account)
    {
        for (InvestmentPlan plan : new ArrayList<InvestmentPlan>(plans))
        {
            if (account.equals(plan.getAccount()))
                removePlan(plan);
        }
    }

    private void deleteInvestmentPlans(Security security)
    {
        for (InvestmentPlan plan : new ArrayList<InvestmentPlan>(plans))
        {
            if (security.equals(plan.getSecurity()))
                removePlan(plan);
        }
    }

    private void deleteTaxonomyAssignments(final InvestmentVehicle vehicle)
    {
        for (Taxonomy taxonomy : taxonomies)
        {
            taxonomy.foreach(new Taxonomy.Visitor()
            {
                @Override
                public void visit(Classification classification, Assignment assignment)
                {
                    if (vehicle.equals(assignment.getInvestmentVehicle()))
                        classification.removeAssignment(assignment);
                }
            });
        }
    }

    private void deleteAccountTransactions(Security security)
    {
        for (Account account : accounts)
        {
            for (AccountTransaction t : new ArrayList<AccountTransaction>(account.getTransactions()))
            {
                if (t.getSecurity() == null || !security.equals(t.getSecurity()))
                    continue;

                account.deleteTransaction(t, this);
            }

        }
    }

    private void deletePortfolioTransactions(Security security)
    {
        for (Portfolio portfolio : portfolios)
        {
            for (PortfolioTransaction t : new ArrayList<PortfolioTransaction>(portfolio.getTransactions()))
            {
                if (!security.equals(t.getSecurity()))
                    continue;

                portfolio.deleteTransaction(t, this);
            }

        }
    }

    /**
     * Marks the client as dirty and triggers a re-calculation of all views.
     * Consider using {@link Client#touch} if only properties changed that are
     * not relevant for calculations - such as preferences.
     */
    public void markDirty()
    {
        propertyChangeSupport.firePropertyChange("dirty", false, true); //$NON-NLS-1$
    }

    /**
     * Touches the client, i.e. marks it as dirty but does <strong>not</strong>
     * trigger a re-calculation of views.
     */
    public void touch()
    {
        propertyChangeSupport.firePropertyChange("touch", false, true); //$NON-NLS-1$
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public String debugTransactionsToString()
    {
        StringBuilder answer = new StringBuilder();

        for (Portfolio portfolio : portfolios)
        {
            answer.append(portfolio.getName()).append('\n');
            portfolio.getTransactions().stream().sorted(new Transaction.ByDate())
                            .forEach(t -> answer.append(t).append('\n'));
        }

        for (Account account : accounts)
        {
            answer.append(account.getName()).append('\n');
            account.getTransactions().stream().sorted(new Transaction.ByDate())
                            .forEach(t -> answer.append(t).append('\n'));
        }

        return answer.toString();
    }

}
