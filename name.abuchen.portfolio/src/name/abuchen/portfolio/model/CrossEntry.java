package name.abuchen.portfolio.model;

public interface CrossEntry
{
    public abstract void updateFrom(Transaction t);

    public abstract TransactionOwner<? extends Transaction> getOwner(Transaction t);

    void setOwner(Transaction t, TransactionOwner<? extends Transaction> owner);

    Transaction getCrossTransaction(Transaction t);

    TransactionOwner<? extends Transaction> getCrossOwner(Transaction t);

    void insert();

    String getSource();

    void setSource(String source);
}
