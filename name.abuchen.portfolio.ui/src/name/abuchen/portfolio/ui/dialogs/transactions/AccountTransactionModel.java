package name.abuchen.portfolio.ui.dialogs.transactions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

import com.ibm.icu.text.MessageFormat;

import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Peer;
import name.abuchen.portfolio.model.PeerList;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityEvent;
import name.abuchen.portfolio.model.Transaction;
import name.abuchen.portfolio.money.CurrencyConverter;
import name.abuchen.portfolio.money.CurrencyConverterImpl;
import name.abuchen.portfolio.money.ExchangeRate;
import name.abuchen.portfolio.money.ExchangeRateTimeSeries;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.snapshot.ClientSnapshot;
import name.abuchen.portfolio.snapshot.SecurityPosition;
import name.abuchen.portfolio.ui.Messages;

public class AccountTransactionModel extends AbstractModel
{
    public enum Properties
    {
        security, account, date, peer, partner, iban, shares, fxGrossAmount, dividendAmount, exchangeRate, inverseExchangeRate, grossAmount, // NOSONAR
        fxTaxes, taxes, total, note, exchangeRateCurrencies, inverseExchangeRateCurrencies, // NOSONAR
        accountCurrencyCode, securityCurrencyCode, fxCurrencyCode, calculationStatus; // NOSONAR
    }

    public static final Security EMPTY_SECURITY = new Security("-----", ""); //$NON-NLS-1$ //$NON-NLS-2$
    public static final Peer     EMPTY_PEER     = new Peer().voidAccount();

    private final Client client;
    private AccountTransaction.Type type;

    private Account sourceAccount;
    private AccountTransaction sourceTransaction;

    private Security security;
    private Account account;
    private Peer peer;
    private String partner;
    private String iban;

    private LocalDate date = LocalDate.now();
    private LocalDate cutoffDate = LocalDate.now();
    private long shares;

    private long fxGrossAmount;
    private BigDecimal dividendAmount = BigDecimal.ZERO;
    private BigDecimal exchangeRate = BigDecimal.ONE;
    private long grossAmount;

    private long fxTaxes;
    private long taxes;
    private long total;

    private String note;

    private IStatus calculationStatus = ValidationStatus.ok();

    public AccountTransactionModel(Client client, AccountTransaction.Type type)
    {
        this.client = client;
        this.type = type;

        checkType();
    }

    @Override
    public String getHeading()
    {
        return type.toString();
    }

    private void checkType()
    {
        switch (type)
        {
            case DEPOSIT:
            case REMOVAL:
            case FEES:
            case FEES_REFUND:
            case TAXES:
            case TAX_REFUND:
            case INTEREST:
            case INTEREST_CHARGE:
            case DIVIDENDS:
            case DIVIDEND_CHARGE:
                return;
            case BUY:
            case SELL:
            case TRANSFER_IN:
            case TRANSFER_OUT:
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void applyChanges()
    {
        if (security == null && supportsSecurity() && !supportsOptionalSecurity())
            throw new UnsupportedOperationException(Messages.MsgMissingSecurity);
        if (account == null)
            throw new UnsupportedOperationException(Messages.MsgMissingAccount);
        if (peer == null && supportsPeer())
            throw new UnsupportedOperationException(Messages.MsgMissingPeer);

        AccountTransaction t;

        if (sourceTransaction != null && sourceAccount.equals(account))
        {
            // transactions stays in same account
            t = sourceTransaction;
        }
        else
        {
            if (sourceTransaction != null)
            {
                sourceAccount.deleteTransaction(sourceTransaction, client);
                sourceTransaction = null;
                sourceAccount = null;
            }

            t = new AccountTransaction();
            t.setCurrencyCode(getAccountCurrencyCode());
            account.addTransaction(t);
        }

        t.setDateTime(date.atStartOfDay());
        t.setSecurity(!EMPTY_SECURITY.equals(security) ? security : null);
        t.setShares(supportsShares() ? shares : 0);
        t.setAmount(total);
        t.setType(type);
        t.setNote(note);

        if (supportsPeer())
        {
            // DEBUG: System.err.println(">> AccountTransactionModel::applyChanges: peer " + peer.toString() + " " + (EMPTY_PEER.equals(peer) ? null : peer) + "[" + iban != null + "] (" + Iban.isValid(iban) + ")");           // TODO: may need to map Peer to existing one
            // TODO:  Change Type DEPOSIT/REMOVAL <> TRANSFER
            t.setPeer(EMPTY_PEER.equals(peer) ? null : peer);
        }

        t.clearUnits();

        if (taxes != 0)
            t.addUnit(new Transaction.Unit(Transaction.Unit.Type.TAX, Money.of(getAccountCurrencyCode(), taxes)));

        String fxCurrencyCode = getFxCurrencyCode();
        if (!fxCurrencyCode.equals(account.getCurrencyCode()))
        {
            Transaction.Unit forex = new Transaction.Unit(Transaction.Unit.Type.GROSS_VALUE, //
                            Money.of(getAccountCurrencyCode(), grossAmount), //
                            Money.of(getSecurityCurrencyCode(), fxGrossAmount), //
                            getExchangeRate());
            t.addUnit(forex);

            if (fxTaxes != 0)
                t.addUnit(new Transaction.Unit(Transaction.Unit.Type.TAX, //
                                Money.of(getAccountCurrencyCode(), Math.round(fxTaxes * exchangeRate.doubleValue())), //
                                Money.of(getSecurityCurrencyCode(), fxTaxes), //
                                exchangeRate));
        }
        if (type == AccountTransaction.Type.DIVIDENDS)
        {
                security.addEvent((new SecurityEvent(cutoffDate, SecurityEvent.Type.STOCK_DIVIDEND)).setAmount(getFxCurrencyCode(), dividendAmount));
        }

    }

    @Override
    public void resetToNewTransaction()
    {
        this.sourceAccount = null;
        this.sourceTransaction = null;

        setFxGrossAmount(0);
        setDividendAmount(BigDecimal.ZERO);
        setGrossAmount(0);
        setTaxes(0);
        setFxTaxes(0);
        setNote(null);
    }


    public boolean supportsPeer()
    {
        switch (type)
        {
            case REMOVAL:
            case DEPOSIT:
            case INTEREST:
            case INTEREST_CHARGE:
                return true;
            default:
                return false;
        }
    }

    public boolean supportsShares()
    {
        switch (type)
        {
            case DIVIDEND_CHARGE:
            case DIVIDENDS:
                return true;
            default:
                return false;
        }
    }

    public boolean supportsSecurity()
    {
        switch (type)
        {
            case DIVIDEND_CHARGE:
            case DIVIDENDS:
            case TAXES:
            case TAX_REFUND:
            case FEES:
            case FEES_REFUND:
                return true;
            default:
                return false;
        }
    }

    public boolean supportsOptionalSecurity()
    {
        switch (type)
        {
            case TAXES:
            case TAX_REFUND:
            case FEES:
            case FEES_REFUND:
                return true;
            default:
                return false;
        }
    }

    public boolean supportsTaxUnits()
    {
        switch (type)
        {
            case DIVIDEND_CHARGE:
            case DIVIDENDS:
            case INTEREST:
            case INTEREST_CHARGE:
                return true;
            default:
                return false;
        }
    }

    public void setSource(Account account, AccountTransaction transaction)
    {
        this.sourceAccount = account;
        this.sourceTransaction = transaction;

        this.security = transaction.getSecurity();
        if (this.security == null && supportsOptionalSecurity())
            this.security = EMPTY_SECURITY;

        this.account = account;
        LocalDateTime transactionDate = transaction.getDateTime();
        this.date    = transactionDate.toLocalDate();
        this.cutoffDate = this.date.minusDays((long) (EMPTY_SECURITY.equals(this.security) || !supportsShares()? 0 : this.security.getDelayedDividend()));
        this.shares  = transaction.getShares();
        this.total   = transaction.getAmount();
        this.peer    = transaction.getPeer();
        if (supportsPeer())
        {
            if (this.peer == null)
                this.peer = EMPTY_PEER;
            if (peer != EMPTY_PEER)
            {
                this.partner = this.peer.getName();
                this.iban    = this.peer.getIban();
            }
            else
            {
                this.partner = "";
                this.iban    = "";
            }
        }

        // both will be overwritten if forex data exists
        this.exchangeRate = BigDecimal.ONE;
        this.taxes = 0;
        this.fxTaxes = 0;

        transaction.getUnits().forEach(unit -> {
            switch (unit.getType())
            {
                case GROSS_VALUE:
                    this.exchangeRate = unit.getExchangeRate();
                    this.grossAmount = unit.getAmount().getAmount();
                    this.fxGrossAmount = unit.getForex().getAmount();
                    break;
                case TAX:
                    if (unit.getForex() != null)
                        this.fxTaxes += unit.getForex().getAmount();
                    else
                        this.taxes += unit.getAmount().getAmount();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        });

        this.grossAmount = calculateGrossAmount4Total();

        // in case units have to forex gross value
        if (exchangeRate.equals(BigDecimal.ONE))
            this.fxGrossAmount = grossAmount;

        this.dividendAmount = calculateDividendAmount();

        this.note = transaction.getNote();

    }

    public void setEvent(SecurityEvent event)
    {
        this.dividendAmount = event.getAmount().getValue();
        setDate(event.getDate().plusDays((long) this.security.getDelayedDividend()));
        triggerTotal(calculateTotal());
    }

    @Override
    public IStatus getCalculationStatus()
    {
        return calculationStatus;
    }

    /**
     * Due to the limited precision of the exchange rate (4 digits), the amount
     * is checked against a range.
     */
    private IStatus calculateStatus()
    {
        // check whether converted amount is in range
        long upper = Math.round(fxGrossAmount * exchangeRate.add(BigDecimal.valueOf(0.0001)).doubleValue());
        long lower = Math.round(fxGrossAmount * exchangeRate.add(BigDecimal.valueOf(-0.0001)).doubleValue());

        if (grossAmount < lower || grossAmount > upper)
            return ValidationStatus.error(Messages.MsgErrorConvertedAmount);

        if (grossAmount == 0L)
            return ValidationStatus.error(MessageFormat.format(Messages.MsgDialogInputRequired, Messages.ColumnTotal));

        return ValidationStatus.ok();
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        System.err.println(">>>> AccountTransactionModel::setAccount account: " + account.toString()); // TODO: still needed for debug?
        String oldCurrencyCode = getAccountCurrencyCode();
        String oldFxCurrencyCode = getFxCurrencyCode();
        String oldExchangeRateCurrencies = getExchangeRateCurrencies();
        String oldInverseExchangeRateCurrencies = getInverseExchangeRateCurrencies();

        firePropertyChange(Properties.account.name(), this.account, this.account = account);

        firePropertyChange(Properties.accountCurrencyCode.name(), oldCurrencyCode, getAccountCurrencyCode());
        firePropertyChange(Properties.fxCurrencyCode.name(), oldFxCurrencyCode, getFxCurrencyCode());
        firePropertyChange(Properties.exchangeRateCurrencies.name(), oldExchangeRateCurrencies,
                        getExchangeRateCurrencies());
        firePropertyChange(Properties.inverseExchangeRateCurrencies.name(), oldInverseExchangeRateCurrencies,
                        getInverseExchangeRateCurrencies());

        updateExchangeRate();
    }

    public Security getSecurity()
    {
        return security;
    }

    public void setSecurity(Security security)
    {
        if (!supportsSecurity())
            return;

        String oldCurrencyCode = getSecurityCurrencyCode();
        String oldFxCurrencyCode = getFxCurrencyCode();
        String oldExchangeRateCurrencies = getExchangeRateCurrencies();
        String oldInverseExchangeRateCurrencies = getInverseExchangeRateCurrencies();

        firePropertyChange(Properties.security.name(), this.security, this.security = security);

        firePropertyChange(Properties.securityCurrencyCode.name(), oldCurrencyCode, getSecurityCurrencyCode());
        firePropertyChange(Properties.fxCurrencyCode.name(), oldFxCurrencyCode, getFxCurrencyCode());
        firePropertyChange(Properties.exchangeRateCurrencies.name(), oldExchangeRateCurrencies,
                        getExchangeRateCurrencies());
        firePropertyChange(Properties.inverseExchangeRateCurrencies.name(), oldInverseExchangeRateCurrencies,
                        getInverseExchangeRateCurrencies());

        updateExchangeRate();
        updateShares();
    }

    private void updateExchangeRate()
    {
        // do not auto-suggest exchange rates when editing an existing
        // transaction
        if (sourceTransaction != null)
            return;

        if (getAccountCurrencyCode().equals(getSecurityCurrencyCode()))
        {
            setExchangeRate(BigDecimal.ONE);
        }
        else if (!getSecurityCurrencyCode().isEmpty())
        {
            ExchangeRateTimeSeries series = getExchangeRateProviderFactory() //
                            .getTimeSeries(getSecurityCurrencyCode(), getAccountCurrencyCode());

            if (series != null)
                setExchangeRate(series.lookupRate(cutoffDate).orElse(new ExchangeRate(cutoffDate, BigDecimal.ONE)).getValue());
            else
                setExchangeRate(BigDecimal.ONE);
        }
    }

    @SuppressWarnings("nls")
    private void updateShares()
    {
        // do not auto-suggest shares and quote when editing an existing
        // transaction
        if (sourceTransaction != null)
            return;

        if (!supportsShares() || security == null)
            return;

        CurrencyConverter converter = new CurrencyConverterImpl(getExchangeRateProviderFactory(),
                        client.getBaseCurrency());
        ClientSnapshot snapshot = ClientSnapshot.create(client, converter, cutoffDate);
        SecurityPosition p = snapshot.getJointPortfolio().getPositionsBySecurity().get(security);
        setShares(p != null ? p.getShares() : 0);
    }

    public LocalDate getDate()
    {
        return date;
    }

    @SuppressWarnings("nls")
    public void setDate(LocalDate date)
    {
        firePropertyChange(Properties.date.name(), this.date, this.date = date);
        cutoffDate = date.minusDays((long) (EMPTY_SECURITY.equals(this.security) || !supportsShares()? 0 : this.security.getDelayedDividend()));
        System.err.println(">>>> AccountTransactionModel::setDate() date : " + date + " cutoff: " + cutoffDate);
        updateShares();
        updateExchangeRate();
    }

    /// ==== CONSTRUCTION AREA ===== START ====

    public String getPartner()
    {
     // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::getPartner() partner   : " + (peer != null ? peer.toString() : "<null>") + "> Name: " + (peer != null ? peer.getName(): "=/="));
     // TODO: still needed for debug? new Exception().printStackTrace(System.err);
        if (peer == null || EMPTY_PEER.equals(peer))
            return "";
        else
            return peer.getName();
    }

    public void setPartner(String partner)
    {
        // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::setPartner() PRE  partner   : " + partner + "  this.partner: " + (this.partner != null ? this.partner.toString() : "<null>"));
        // TODO: still needed for debug? new Exception().printStackTrace(System.err);
        firePropertyChange(Properties.partner.name(), this.partner, this.partner = partner); //this.peer.setName(peerStr)
    }

    public String getIban()
    {
        // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::getIban() peer : " + (peer != null ? peer.toString() : "<null>") + "> IBAN: " + (peer != null ? peer.getIban() : "=/="));
        // TODO: still needed for debug? new Exception().printStackTrace(System.err);
        if (peer == null || EMPTY_PEER.equals(peer))
            return "";
        else
            return peer.getIban();
    }

    public void setIban(String iban)
    {
        // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::setIban() iban PRE   : " + (iban != null ? iban : "<null>")); // TODO: still needed for debug?
        // TODO: still needed for debug? new Exception().printStackTrace(System.err);
        firePropertyChange(Properties.iban.name(), this.iban, this.iban = iban); // this.peer.setIban(iban)
        // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::setIban() iban POST  : " + (this.iban != null ? this.iban : "<null>")); // TODO: still needed for debug?
    }

    public Peer getPeer()
    {
        // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::getPeer() peer : " + (peer != null ? peer.toString() : "<null>")); // TODO: still needed for debug?
        // TODO: still needed for debug? new Exception().printStackTrace(System.err);
        return peer;
    }

    public void setPeer(Peer peer)
    {
        if (peer == null)
        {
            this.peer = peer;
            return;
        }
        // TODO: still needed for debug? 
        //        new Exception().printStackTrace(System.err);
        //        if (this.peer != null)
        //            System.err.println(">PRE AccountTransactionModel::setPeer() OLD peer : " + this.peer.toString());
        //        if (peer != null)
        //            System.err.println(">PRE AccountTransactionModel::setPeer() NEW peer : " + peer.toString());
        //        if (partner != null)
        //            System.err.println(">PRE AccountTransactionModel::setPeer() partner : " + partner.toString());
        //        if (iban != null)
        //            System.err.println(">PRE AccountTransactionModel::setPeer() iban : " + iban.toString());
        firePropertyChange(Properties.peer.name(), this.peer, this.peer = peer); //this.peer.setName(peerStr)
        //        System.err.println(">A AccountTransactionModel::setPeer()");
        //        new Exception().printStackTrace(System.err);
        //        if (peer != null)
        //            System.err.println("POST AccountTransactionModel::setPeer() peer : " + peer.toString());
        //        if (partner != null)
        //            System.err.println("POST AccountTransactionModel::setPeer() partner : " + partner.toString());
        //        if (iban != null)
        //            System.err.println("POST AccountTransactionModel::setPeer() iban : " + iban.toString());
    }

    public boolean matchPeer(String matchStr)
    {
        PeerList peerList = client.getPeers().findPeer(matchStr, true);
        // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::matchPeer() POST matchStr : " + matchStr + "  this.partner: " + (this.partner != null ? this.partner.toString() : "<null>") + "  this.iban: " + (this.iban != null ? this.iban.toString() : "<null>")); // TODO: still needed for debug?
        // TODO: still needed for debug? new Exception().printStackTrace(System.err);
        // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::matchPeer() peerList   : " + (peerList != null ? peerList.toString()  : "<null>"));
        if (peerList == null)
            return false;
        if (peerList.size() == 1 && matchStr.length() >= 3)
        {
           this.peer = peerList.get(0);
           firePropertyChange(Properties.peer.name(), "", this.peer);
           firePropertyChange(Properties.iban.name(), "", this.peer.getIban());
           firePropertyChange(Properties.partner.name(), "", this.peer.getName());
           return true;
        }
        else if (peerList.size() > 1)
            System.err.println(">>>> AccountTransactionModel::matchPeer() peerList   : " + peerList.toString() + " peer : " + (peer != null ? peer.toString() : "<null>") ); // TODO: still needed for debug?
        // TODO: still needed for debug? System.err.println(">>>> AccountTransactionModel::matchPeer() peer : " + (peer != null ? peer.toString() : "<null>") ); // TODO: still needed for debug?
        return false;
    }

    /// ==== CONSTRUCTION AREA ====== END =====

    public long getShares()
    {
        return shares;
    }

    public void setShares(long shares)
    {
        firePropertyChange(Properties.shares.name(), this.shares, this.shares = shares);

        if (this.dividendAmount.equals(BigDecimal.ZERO))
            firePropertyChange(Properties.dividendAmount.name(), this.dividendAmount,this.dividendAmount = calculateDividendAmount());
        else
            firePropertyChange(Properties.fxGrossAmount.name(), this.fxGrossAmount,this.fxGrossAmount = calculateGrossAmount4Dividend());
    }

    public long getFxGrossAmount()
    {
        return fxGrossAmount;
    }

    public void setFxGrossAmount(long foreignCurrencyAmount)
    {
        firePropertyChange(Properties.fxGrossAmount.name(), this.fxGrossAmount,
                        this.fxGrossAmount = foreignCurrencyAmount);

        triggerGrossAmount(Math.round(exchangeRate.doubleValue() * foreignCurrencyAmount));

        firePropertyChange(Properties.dividendAmount.name(), this.dividendAmount,
                        this.dividendAmount = calculateDividendAmount());

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public BigDecimal getDividendAmount()
    {
        return dividendAmount;
    }

    public void setDividendAmount(BigDecimal amount)
    {
        triggerDividendAmount(amount);
        if (getShares() > 0 || getFxGrossAmount() > 0)
        {
            long myGrossAmount = calculateGrossAmount4Dividend();
            setFxGrossAmount(myGrossAmount);
        }
    }

    public void triggerDividendAmount(BigDecimal amount)
    {
        firePropertyChange(Properties.dividendAmount.name(), this.dividendAmount, this.dividendAmount = amount);
    }

    public BigDecimal getExchangeRate()
    {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate)
    {
        BigDecimal newRate = exchangeRate == null ? BigDecimal.ZERO : exchangeRate;
        BigDecimal oldInverseRate = getInverseExchangeRate();

        firePropertyChange(Properties.exchangeRate.name(), this.exchangeRate, this.exchangeRate = newRate);
        firePropertyChange(Properties.inverseExchangeRate.name(), oldInverseRate, getInverseExchangeRate());

        triggerGrossAmount(Math.round(newRate.doubleValue() * fxGrossAmount));

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public BigDecimal getInverseExchangeRate()
    {
        return BigDecimal.ONE.divide(exchangeRate, 10, BigDecimal.ROUND_HALF_DOWN);
    }

    public void setInverseExchangeRate(BigDecimal rate)
    {
        setExchangeRate(BigDecimal.ONE.divide(rate, 10, BigDecimal.ROUND_HALF_DOWN));
    }

    public long getGrossAmount()
    {
        return grossAmount;
    }

    public void setGrossAmount(long amount)
    {
        triggerGrossAmount(amount);

        if (fxGrossAmount != 0)
        {
            BigDecimal newExchangeRate = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(fxGrossAmount), 10,
                            RoundingMode.HALF_UP);
            BigDecimal oldInverseRate = getInverseExchangeRate();
            firePropertyChange(Properties.exchangeRate.name(), this.exchangeRate, this.exchangeRate = newExchangeRate);
            firePropertyChange(Properties.inverseExchangeRate.name(), oldInverseRate, getInverseExchangeRate());
        }

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public void triggerGrossAmount(long amount)
    {
        firePropertyChange(Properties.grossAmount.name(), this.grossAmount, this.grossAmount = amount);
        triggerTotal(calculateTotal());
    }

    public long getFxTaxes()
    {
        return fxTaxes;
    }

    public void setFxTaxes(long fxTaxes)
    {
        firePropertyChange(Properties.fxTaxes.name(), this.fxTaxes, this.fxTaxes = fxTaxes);
        triggerTotal(calculateTotal());

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public long getTaxes()
    {
        return taxes;
    }

    public void setTaxes(long taxes)
    {
        firePropertyChange(Properties.taxes.name(), this.taxes, this.taxes = taxes);
        triggerTotal(calculateTotal());

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        triggerTotal(total);

        firePropertyChange(Properties.grossAmount.name(), this.grossAmount,
                        this.grossAmount = calculateGrossAmount4Total());

        firePropertyChange(Properties.fxGrossAmount.name(), this.fxGrossAmount,
                        this.fxGrossAmount = Math.round(grossAmount / exchangeRate.doubleValue()));

        firePropertyChange(Properties.dividendAmount.name(), this.dividendAmount,
                        this.dividendAmount = calculateDividendAmount());

        firePropertyChange(Properties.calculationStatus.name(), this.calculationStatus,
                        this.calculationStatus = calculateStatus());
    }

    public void triggerTotal(long total)
    {
        firePropertyChange(Properties.total.name(), this.total, this.total = total);
    }

    protected BigDecimal calculateDividendAmount()
    {
        if (shares > 0)
            return BigDecimal.valueOf(
                            (fxGrossAmount * Values.Share.factor()) / (double) shares / Values.Amount.divider());
        else
            return BigDecimal.ZERO;
    }

    protected long calculateGrossAmount4Total()
    {
        long totalTaxes = taxes + Math.round(exchangeRate.doubleValue() * fxTaxes);
        return total + (type == AccountTransaction.Type.INTEREST_CHARGE ||  type == AccountTransaction.Type.DIVIDEND_CHARGE ? -1 : 1) * totalTaxes;
    }

    protected long calculateGrossAmount4Dividend()
    {
        return Math.round((shares * dividendAmount.doubleValue() * Values.Amount.factor())
                        / (double) Values.Share.factor());
    }

    @SuppressWarnings("nls")
    private long calculateTotal()
    {
        long totalTaxes = taxes + Math.round(exchangeRate.doubleValue() * fxTaxes);
        return Math.max(0, grossAmount + (type == AccountTransaction.Type.INTEREST_CHARGE || type == AccountTransaction.Type.DIVIDEND_CHARGE ? 1 : -1) * totalTaxes);
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        firePropertyChange(Properties.note.name(), this.note, this.note = note);
    }

    public String getAccountCurrencyCode()
    {
        return account != null ? account.getCurrencyCode() : ""; //$NON-NLS-1$
    }

    public String getSecurityCurrencyCode()
    {
        return security != null ? security.getCurrencyCode() : ""; //$NON-NLS-1$
    }

    public String getFxCurrencyCode()
    {
        return security != null && !security.getCurrencyCode().isEmpty() ? security.getCurrencyCode()
                        : getAccountCurrencyCode();
    }

    /**
     * Returns exchange rate label in direct (price) notation.
     */
    public String getExchangeRateCurrencies()
    {
        return String.format("%s/%s", getSecurityCurrencyCode(), getAccountCurrencyCode()); //$NON-NLS-1$
    }

    /**
     * Returns exchange rate label in indirect (quantity) notation.
     */
    public String getInverseExchangeRateCurrencies()
    {
        return String.format("%s/%s", getAccountCurrencyCode(), getSecurityCurrencyCode()); //$NON-NLS-1$
    }

    public AccountTransaction.Type getType()
    {
        return type;
    }
}
