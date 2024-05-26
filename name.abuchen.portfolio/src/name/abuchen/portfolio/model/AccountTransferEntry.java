package name.abuchen.portfolio.model;

import java.time.LocalDateTime;

public class AccountTransferEntry implements CrossEntry, Annotated
{
    private Account accountFrom;
    private AccountTransaction transactionFrom;
    private Account accountTo;
    private AccountTransaction transactionTo;

    public AccountTransferEntry()
    {
        this.transactionFrom = new AccountTransaction();
        this.transactionFrom.setType(AccountTransaction.Type.TRANSFER_OUT);
        this.transactionFrom.setCrossEntry(this);

        this.transactionTo = new AccountTransaction();
        this.transactionTo.setType(AccountTransaction.Type.TRANSFER_IN);
        this.transactionTo.setCrossEntry(this);
    }

    public AccountTransferEntry(Account accountFrom, Account accountTo)
    {
        this();
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
    }

    public void setSourceTransaction(AccountTransaction transaction)
    {
        this.transactionFrom = transaction;
    }

    public void setTargetTransaction(AccountTransaction transaction)
    {
        this.transactionTo = transaction;
    }

    public AccountTransaction getSourceTransaction()
    {
        return this.transactionFrom;
    }

    public AccountTransaction getTargetTransaction()
    {
        return this.transactionTo;
    }

    public void setSourceAccount(Account account)
    {
        this.accountFrom = account;
    }

    public Account getSourceAccount()
    {
        return accountFrom;
    }

    public void setTargetAccount(Account account)
    {
        this.accountTo = account;
    }

    public Account getTargetAccount()
    {
        return accountTo;
    }

    public void setPrimaryTransactionOwner(TransactionOwner<Transaction> owner)
    {
        Object subject = (Object) owner;
        if (subject instanceof Account)
        {
            if (!this.accountFrom.equals((Account) subject))
                this.accountTo = (Account) subject;
        }
        else
            throw new IllegalArgumentException();
    }

    public void setSecondaryTransactionOwner(TransactionOwner<Transaction> owner)
    {
        Object subject = (Object) owner;
        if (subject instanceof Account)
        {
            if (!this.accountTo.equals((Account) subject))
                this.accountFrom = (Account) subject;
        }
        else
            throw new IllegalArgumentException();
    }

    public TransactionOwner<Transaction> getPrimaryTransactionOwner()
    {
        @SuppressWarnings("unchecked")
        TransactionOwner<Transaction> owner = (TransactionOwner<Transaction>) this.getOwner(transactionTo);
        return owner;
    }

    public TransactionOwner<Transaction> getSecondaryTransactionOwner()
    {
        @SuppressWarnings("unchecked")
        TransactionOwner<Transaction> owner = (TransactionOwner<Transaction>) this.getOwner(transactionFrom);
        return owner;
    }

    public void setDate(LocalDateTime date)
    {
        this.transactionFrom.setDateTime(date);
        this.transactionTo.setDateTime(date);
    }

    public void setAmount(long amount)
    {
        this.transactionFrom.setAmount(amount);
        this.transactionTo.setAmount(amount);
    }

    public void setCurrencyCode(String currencyCode)
    {
        this.transactionFrom.setCurrencyCode(currencyCode);
        this.transactionTo.setCurrencyCode(currencyCode);
    }

    @Override
    public String getNote()
    {
        return this.transactionFrom.getNote();
    }

    @Override
    public void setNote(String note)
    {
        this.transactionFrom.setNote(note);
        this.transactionTo.setNote(note);
    }

    @Override
    public String getSource()
    {
        return this.transactionFrom.getSource();
    }

    @Override
    public void setSource(String source)
    {
        this.transactionFrom.setSource(source);
        this.transactionTo.setSource(source);
    }

    @Override
    public void insert()
    {
        accountFrom.addTransaction(transactionFrom);
        accountTo.addTransaction(transactionTo);
    }

    @Override
    public void updateFrom(Transaction t)
    {
        if (t == transactionFrom)
            copyAttributesOver(transactionFrom, transactionTo);
        else if (t == transactionTo)
            copyAttributesOver(transactionTo, transactionFrom);
        else
            throw new UnsupportedOperationException();
    }

    private void copyAttributesOver(AccountTransaction source, AccountTransaction target)
    {
        target.setDateTime(source.getDateTime());
        target.setNote(source.getNote());
    }

    @Override
    public TransactionOwner<? extends Transaction> getOwner(Transaction t)
    {
        if (t.equals(transactionFrom))
            return accountFrom;
        else if (t.equals(transactionTo))
            return accountTo;
        else
            throw new UnsupportedOperationException();
    }

    @Override
    public void setOwner(Transaction t, TransactionOwner<? extends Transaction> owner)
    {
        if (!(owner instanceof Account))
            throw new IllegalArgumentException();

        if (t.equals(transactionFrom) && !accountTo.equals(owner))
            accountFrom = (Account) owner;
        else if (t.equals(transactionTo) && !accountFrom.equals(owner))
            accountTo = (Account) owner;
        else
            throw new IllegalArgumentException();
    }

    @Override
    public Transaction getCrossTransaction(Transaction t)
    {
        if (t.equals(transactionFrom))
            return transactionTo;
        else if (t.equals(transactionTo))
            return transactionFrom;
        else
            throw new UnsupportedOperationException();
    }

    @Override
    public TransactionOwner<? extends Transaction> getCrossOwner(Transaction t)
    {
        if (t.equals(transactionFrom))
            return accountTo;
        else if (t.equals(transactionTo))
            return accountFrom;
        else
            throw new UnsupportedOperationException();
    }
}
