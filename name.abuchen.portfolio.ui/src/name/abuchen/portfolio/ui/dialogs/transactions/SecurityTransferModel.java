package name.abuchen.portfolio.ui.dialogs.transactions;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Portfolio;
import name.abuchen.portfolio.model.PortfolioTransferEntry;
import name.abuchen.portfolio.model.PortfolioTransferEntry.Suggestion;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.Transaction;
import name.abuchen.portfolio.model.TransactionOwner;
import name.abuchen.portfolio.money.CurrencyConverter;
import name.abuchen.portfolio.money.CurrencyConverterImpl;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.snapshot.ClientSnapshot;
import name.abuchen.portfolio.snapshot.PortfolioSnapshot;
import name.abuchen.portfolio.snapshot.security.SecurityPerformanceIndicator;
import name.abuchen.portfolio.snapshot.security.SecurityPerformanceRecord;
import name.abuchen.portfolio.snapshot.security.SecurityPerformanceSnapshot;
import name.abuchen.portfolio.snapshot.SecurityPosition;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.util.Interval;

public class SecurityTransferModel extends AbstractModel
{
    public enum Properties
    {
        security, securityCurrencyCode, sourcePortfolio, sourcePortfolioLabel, quoteSuggestion, targetPortfolio, targetPortfolioLabel, date, time, shares, quote, amount, note, calculationStatus;
    }

    public static final class QuoteSuggestion
    {
        private final Suggestion suggestion;
        private final String label;
        private final boolean editable;

        public QuoteSuggestion(Suggestion suggestion, String label, boolean editable)
        {
            this.suggestion = suggestion;
            this.label = label;
            this.editable = editable;
        }

        public Suggestion getSuggestion()
        {
            return suggestion;
        }

        public String getLabel()
        {
            return label;
        }

        public boolean getEditable()
        {
            return editable;
        }

        @Override
        public String toString()
        {
            return "-> " + getLabel(); // TODO was "x " before //$NON-NLS-1$
        }
    }

    private final Client client;

    private PortfolioTransferEntry source;

    private Security security;
    private Portfolio sourcePortfolio;
    private Portfolio targetPortfolio;
    private LocalDate date = LocalDate.now();
    private LocalTime time = PresetValues.getTime();

    private long shares;
    private BigDecimal quote = BigDecimal.ONE;
    private long amount;
    private String note;

    private List<QuoteSuggestion> quoteSuggestionList = new ArrayList<QuoteSuggestion>();
    private QuoteSuggestion quoteSuggestion;

    private IStatus calculationStatus = ValidationStatus.ok();

    public SecurityTransferModel(Client client)
    {
        this.client = client;
        QuoteSuggestion goodwill = new QuoteSuggestion(Suggestion.goodwill, Messages.ColumnQuoteSuggestion_goodwill, true);
        quoteSuggestionList.add(0, new QuoteSuggestion(Suggestion.market, Messages.ColumnQuoteSuggestion_market, false));
        quoteSuggestionList.add(0, new QuoteSuggestion(Suggestion.purchase, Messages.ColumnQuoteSuggestion_purchase, false));
        quoteSuggestionList.add(0, goodwill);
        quoteSuggestion = goodwill;
    }

    @Override
    public String getHeading()
    {
        return Messages.LabelSecurityTransfer;
    }

    @Override
    public void applyChanges()
    {
        if (security == null)
            throw new UnsupportedOperationException(Messages.MsgMissingSecurity);
        if (sourcePortfolio == null)
            throw new UnsupportedOperationException(Messages.MsgPortfolioFromMissing);
        if (targetPortfolio == null)
            throw new UnsupportedOperationException(Messages.MsgPortfolioToMissing);
        if (quoteSuggestion.getSuggestion().equals(Suggestion.none))
            throw new UnsupportedOperationException(Messages.MsgMissingSuggestion);

        PortfolioTransferEntry t;

        if (source != null && sourcePortfolio.equals(source.getOwner(source.getSourceTransaction()))
                        && targetPortfolio.equals(source.getOwner(source.getTargetTransaction())))
        {
            // transaction stays in same accounts
            t = source;
        }
        else
        {
            if (source != null)
            {
                @SuppressWarnings("unchecked")
                TransactionOwner<Transaction> owner = (TransactionOwner<Transaction>) source
                                .getOwner(source.getSourceTransaction());
                owner.deleteTransaction(source.getSourceTransaction(), client);
                source = null;
            }

            t = new PortfolioTransferEntry(sourcePortfolio, targetPortfolio);
            t.insert();
        }

        t.setSecurity(security);
        t.setDate(LocalDateTime.of(date, time));
        t.setShares(shares);
        t.setAmount(amount);
        t.setCurrencyCode(security.getCurrencyCode());
        t.setNote(note);
        t.setQuoteSuggestion(quoteSuggestion.getSuggestion());
    }

    @Override
    public void resetToNewTransaction()
    {
        this.source = null;

        setShares(0);
        setAmount(0);
        setNote(null);
        setTime(PresetValues.getTime());
        triggerQuoteSuggestion(quoteSuggestionList.stream()
                        .filter(suggestion -> Suggestion.goodwill.equals(suggestion.getSuggestion()))
                        .findAny()
                        .orElse(null));
    }

    private IStatus calculateStatus()
    {
        if (shares == 0L)
            return ValidationStatus.error(MessageFormat.format(Messages.MsgDialogInputRequired, Messages.ColumnShares));

        // check whether gross value is in range
        long lower = Math.round(shares * quote.add(BigDecimal.valueOf(-0.01)).doubleValue() * Values.Amount.factor()
                        / Values.Share.divider());
        long upper = Math.round(shares * quote.add(BigDecimal.valueOf(0.01)).doubleValue() * Values.Amount.factor()
                        / Values.Share.divider());
        if (amount < lower || amount > upper)
            return ValidationStatus.error(Messages.MsgIncorrectSubTotal);

        if (amount == 0L)
            return ValidationStatus.error(MessageFormat.format(Messages.MsgDialogInputRequired, Messages.ColumnAmount));

        return ValidationStatus.ok();
    }

    private void updateSharesAndQuote()
    {
        updateShares();
        updateQuote();
    }

    private SecurityPosition getPosition4Shares()
    {
        if (security != null)
        {
            CurrencyConverter converter = new CurrencyConverterImpl(getExchangeRateProviderFactory(),
                            client.getBaseCurrency());
            PortfolioSnapshot snapshot = sourcePortfolio != null
                            ? PortfolioSnapshot.create(sourcePortfolio, converter, date.minusDays(1))
                            : ClientSnapshot.create(client, converter, date).getJointPortfolio();
            return snapshot.getPositionsBySecurity().get(security);
        }
        else
            return null;
    }

    private void updateShares()
    {
        // do not auto-suggest shares and quote when editing an existing
        // transaction
        if (source != null)
            return;

        SecurityPosition position = getPosition4Shares();

        if (position != null)
            setShares(position.getShares());
        else if (security != null)
            setShares(0);
    }

    private SecurityPosition getPosition4Quote()
    {
        if (security != null)
        {
            CurrencyConverter converter = new CurrencyConverterImpl(getExchangeRateProviderFactory(),
                            client.getBaseCurrency());
            PortfolioSnapshot snapshot = ClientSnapshot.create(client, converter, date).getJointPortfolio();
            return snapshot.getPositionsBySecurity().get(security);
        }
        else
            return null;
    }

    private SecurityPerformanceRecord getRecord4Quote()
    {
        if (security != null)
        {
            CurrencyConverter converter = new CurrencyConverterImpl(getExchangeRateProviderFactory(),
                            client.getBaseCurrency());
            SecurityPerformanceSnapshot securityPerformanceSnapshot = SecurityPerformanceSnapshot.create(client, converter, Interval.of(LocalDate.MIN, date), SecurityPerformanceIndicator.Costs.class);
            SecurityPerformanceRecord securityPerformanceRecord = securityPerformanceSnapshot.getRecord(security).orElseThrow(IllegalArgumentException::new);
            return securityPerformanceRecord;
        }
        else
            return null;
    }

    public void updateQuote()
    {
        BigDecimal newQuote = (source == null ? BigDecimal.ZERO: getQuote());
        if (!quoteSuggestion.getSuggestion().equals(Suggestion.goodwill))
        {
            SecurityPosition position = getPosition4Quote();
            if (position != null)
            {
                if (quoteSuggestion.getSuggestion().equals(Suggestion.purchase))
                {
                    // purchase
                    SecurityPerformanceRecord record = getRecord4Quote();
                    newQuote = new BigDecimal(record.getInvestmentPerShare().getAmount() / Values.Amount.divider());
                }
                else
                    // market
                    newQuote = new BigDecimal(position.getPrice().getValue() / Values.Quote.divider());
            }
            else if (security != null)
                newQuote = new BigDecimal(security.getSecurityPrice(date).getValue() / Values.Quote.divider());
        }
        else if (security != null && source == null)
            // goodwill
            newQuote = new BigDecimal(security.getSecurityPrice(date).getValue() / Values.Quote.divider());

        setQuote(newQuote);
    }

    public void setSource(PortfolioTransferEntry entry)
    {
        this.source = entry;
        presetFromSource(entry);
    }

        public void presetFromSource(PortfolioTransferEntry entry)
    {
        this.sourcePortfolio = (Portfolio) entry.getOwner(entry.getSourceTransaction());
        this.targetPortfolio = (Portfolio) entry.getOwner(entry.getTargetTransaction());

        this.security = entry.getSourceTransaction().getSecurity();
        LocalDateTime transactionDate = entry.getSourceTransaction().getDateTime();
        this.date = transactionDate.toLocalDate();
        this.time = transactionDate.toLocalTime();
        this.shares = entry.getSourceTransaction().getShares();
        this.quote = entry.getSourceTransaction().getGrossPricePerShare().toBigDecimal();
        this.amount = entry.getTargetTransaction().getAmount();
        this.note = entry.getSourceTransaction().getNote();
        this.quoteSuggestion = getQuoteSuggestion(entry.getQuoteSuggestion());
    }

    @Override
    public IStatus getCalculationStatus()
    {
        return calculationStatus;
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        String oldCurrencyCode = getSecurityCurrencyCode();
        firePropertyChange(Properties.security.name(), this.security, this.security = security);
        firePropertyChange(Properties.securityCurrencyCode.name(), oldCurrencyCode, getSecurityCurrencyCode());

        updateSharesAndQuote();
    }

    public Portfolio getSourcePortfolio()
    {
        return sourcePortfolio;
    }

    public void setSourcePortfolio(Portfolio portfolio)
    {
        String oldLabel = getSourcePortfolioLabel();
        firePropertyChange(Properties.sourcePortfolio.name(), this.sourcePortfolio, this.sourcePortfolio = portfolio);
        firePropertyChange(Properties.sourcePortfolioLabel.name(), oldLabel, getSourcePortfolioLabel());

        updateSharesAndQuote();
    }

    public String getSourcePortfolioLabel()
    {
        return sourcePortfolio != null ? sourcePortfolio.getReferenceAccount().getName() : ""; //$NON-NLS-1$
    }

    public Portfolio getTargetPortfolio()
    {
        return targetPortfolio;
    }

    public void setTargetPortfolio(Portfolio portfolio)
    {
        String oldLabel = getTargetPortfolioLabel();
        firePropertyChange(Properties.targetPortfolio.name(), this.targetPortfolio, this.targetPortfolio = portfolio);
        firePropertyChange(Properties.targetPortfolioLabel.name(), oldLabel, getTargetPortfolioLabel());
    }

    public String getTargetPortfolioLabel()
    {
        return targetPortfolio != null ? targetPortfolio.getReferenceAccount().getName() : ""; //$NON-NLS-1$
    }

    public LocalDate getDate()
    {
        return date;
    }

    public LocalTime getTime()
    {
        return time;
    }

    public void setDate(LocalDate date)
    {
        firePropertyChange(Properties.date.name(), this.date, this.date = date);
        updateSharesAndQuote();
    }

    public void setTime(LocalTime time)
    {
        firePropertyChange(Properties.time.name(), this.time, this.time = time);
        updateSharesAndQuote();
    }

    public long getShares()
    {
        return shares;
    }

    public void setShares(long shares)
    {
        firePropertyChange(Properties.shares.name(), this.shares, this.shares = shares);

        if (quote.doubleValue() != 0)
        {
            setAmount(Math.round(shares * quote.doubleValue() * Values.Amount.factor() / Values.Share.divider()));
        }
        else if (amount != 0 && shares != 0)
        {
            setQuote(BigDecimal.valueOf(amount * Values.Share.factor() / (shares * Values.Amount.divider())));
        }

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public BigDecimal getQuote()
    {
        return quote;
    }

    public void setQuote(BigDecimal quote)
    {
        firePropertyChange(Properties.quote.name(), this.quote, this.quote = quote);

        triggerAmount(Math.round(shares * quote.doubleValue() * Values.Amount.factor() / Values.Share.divider()));

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public long getAmount()
    {
        return amount;
    }

    public void setAmount(long amount)
    {
        triggerAmount(amount);

        if (shares != 0)
        {
            BigDecimal newQuote = BigDecimal
                            .valueOf(amount * Values.Share.factor() / (shares * Values.Amount.divider()));
            firePropertyChange(Properties.quote.name(), this.quote, this.quote = newQuote);
        }

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public void triggerAmount(long amount)
    {
        firePropertyChange(Properties.amount.name(), this.amount, this.amount = amount);
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        firePropertyChange(Properties.note.name(), this.note, this.note = note);
    }

    public String getSecurityCurrencyCode()
    {
        return security != null ? security.getCurrencyCode() : ""; //$NON-NLS-1$
    }

    public List<QuoteSuggestion> getQuoteSuggestionList()
    {
        return this.quoteSuggestionList;
    }

    public QuoteSuggestion getQuoteSuggestion(Suggestion element)
    {
        for(QuoteSuggestion quoteSuggestion : this.quoteSuggestionList)
            if (quoteSuggestion.getSuggestion().toString().equals(element.toString()))
                return quoteSuggestion;
        return null;
    }

    public void triggerQuoteSuggestion(QuoteSuggestion suggestion)
    {
        firePropertyChange(Properties.quoteSuggestion.name(), this.quoteSuggestion, this.quoteSuggestion = suggestion);
    }

    public void setQuoteSuggestion(QuoteSuggestion quoteSuggestion)
    {
        if (!(this.quoteSuggestion.equals(quoteSuggestion)))
        {
            triggerQuoteSuggestion(quoteSuggestion);
            updateQuote();
        }
    }

    public QuoteSuggestion getQuoteSuggestion()
    {
        return this.quoteSuggestion;
    }
}
