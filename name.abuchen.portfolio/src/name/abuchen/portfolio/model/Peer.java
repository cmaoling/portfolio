package name.abuchen.portfolio.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import name.abuchen.portfolio.util.Iban;
import name.abuchen.portfolio.Messages;

public class Peer implements Named
{
    public static final class ByIban implements Comparator<Peer>, Serializable
    {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Peer s1, Peer s2)
        {
            if (s1 == null)
                return s2 == null ? 0 : -1;
            return s1.IBAN.compareToIgnoreCase(s2.IBAN);
        }
    }

    private String name;
    private String note;
    private String IBAN;

    private Account account;

    public Peer()
    {
        //voidAccount();
    }

    public Peer voidAccount()
    {
        account = null;
        setName(Messages.LabelAnyPeer);
        setIban(Iban.IBANNUMBER_ANY);
        return this;
    }

    public String setIban(String iban)
    {
        if (links2Account())
            account.setIban(iban);
        else
        {
            account = null;
            if (Iban.isValid(iban))
                IBAN = iban;
        }
        return this.getIban();
    }

    public String setInvalidIban(String iban)
    {
        if (links2Account())
            account.setIban(iban);
        else
        {
            account = null;
            IBAN = iban;
        }
        return this.getIban();
    }

    public String setIban(String blz, String konto)
    {
        return this.setIban(Iban.DEconvert(blz,konto));
    }

    public boolean links2Account()
    {
        if (getAccount() == null)
            return false;
        else
            return true;
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
        if (IBAN != null && account != null && (account.getIban() == null || account.getIban().length() >= IBAN.length()))
            account.setIban(IBAN);
    }

    @Override
    public void setName(String name)
    {
        if (links2Account())
            getAccount().setName(name);
        else
            this.name = name;
    }

    @Override
    public void setNote(String note)
    {
        if (links2Account())
            getAccount().setNote(note);
        else
            this.note = note;
    }

    public String getIban()
    {
        if (links2Account())
            return getAccount().getIban();
        else if (IBAN != null && IBAN.equals(Iban.IBANNUMBER_ANY))
            return "";
        else
            return IBAN;
    }

    @Override
    public String getName()
    {
        if (links2Account())
            return "[" + this.getAccount().getName() + "]";
        else if (IBAN != null && IBAN.equals(Iban.IBANNUMBER_ANY))
            return Messages.LabelAnyPeer;
        else
            return name;
    }

    @Override
    public String getNote()
    {
        if (links2Account())
            return "<" + this.getAccount().getNote() + ">";
        else
        return this.note;
    }

    public Peer getPeer(String Iban)
    {
        if (links2Account())
        {
            if (this.getAccount().getIban().equals(Iban))
                return this;
        }       
        else if (IBAN.equals(Iban))
            return this;
        return null;
    }

    public Peer getPeer()
    {
        return this;
    }

    public List<DedicatedTransaction> getTransactions(Client client)
    {
        List<DedicatedTransaction> answer = new ArrayList<>();

        for (Account account : client.getAccounts())
        {
            account.getTransactions().stream()
                            .filter(t -> this.equals(t.getPeer()))
                            .forEach(element -> answer.add(new DedicatedTransaction(account, element)));
            account.getTransactions().stream()
                            .filter((t) -> AccountTransaction.Type.TRANSFER_IN.equals(t.getType()) || AccountTransaction.Type.TRANSFER_OUT.equals(t.getType()))
                            .filter((t) -> this.getAccount() != null)
                            .filter((t) -> this.getAccount().equals((Account) t.getCrossEntry().getOwner(t)))
                            .forEach(element -> answer.add(new DedicatedTransaction(account, element)));
                            //.forEach(element -> System.err.println("[ " + element.getCrossEntry().getCrossTransaction(element).toString() + "]   <" + (element != null? element.toString() : "") + "> <" + (element.getPeer() != null ? element.getPeer().toString() : "getPeer<null>") + "> <" + (this != null ? this.toString() : "<null>") + ">"));
        }
        return answer;
    }

    @Override
    public String toString()
    {

        return (getName() != null?getName():"NULL") + " (" + (getIban() != null?getIban():"=/=") + ")" + (links2Account() ? " <Account: " + getAccount().getUUID() + ">" : "");
    }
}
