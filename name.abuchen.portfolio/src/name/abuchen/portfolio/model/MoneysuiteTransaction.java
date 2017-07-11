package name.abuchen.portfolio.model;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import name.abuchen.portfolio.model.PortfolioTransaction.Type;

public class MoneysuiteTransaction extends Transaction
{
    public enum Type
    {
        /** Records nothing. */
        NONE(false),
        /** Records the purchase of a security. */
        BUY(true),
        /** Records the purchase of a security. */
        INVEST(true),
        /** Records the purchase of a bond. */
        BUY_BOND(true),
        /** Records the sale of a security. */
        SELL(false),
        /** Records the sale of a security. */
        DEVEST(false),
        /** Records the repayment of a bond. */
        REPAYMENT(false),
        /** Records the transfer of assets from another portfolio. */
        TRANSFER_IN(true),
        /** Records the transfer of assets to another portfolio. */
        TRANSFER_OUT(false),
        /** Records the transfer of assets into the portfolio. */
        DELIVERY_INBOUND(true),
        /** Records the transfer of assets out of a portfolio. */
        DELIVERY_OUTBOUND(false),
        /** Records the split of a security. */
        SPLIT(false),
        /** Records the reinvestment of a security dividend. */
        REINVEST(false),
        /** Records the transfer of money in and out of a account. */        
        DEPOSIT(false), REMOVAL(true), //
        /** Records an interest payment to or from an account. */        
        INTEREST(false), INTEREST_CHARGE(true), //
        /** Records a dividend payment of an assets to an account. */        
        DIVIDENDS(false), //
        /** Records an fee payment to or from an account. */        
        FEES(true), FEES_REFUND(false), //
        /** Records an tax payment to or from an account. */        
        TAXES(true), TAX_REFUND(false);

        private static final ResourceBundle RESOURCES = ResourceBundle.getBundle("name.abuchen.portfolio.model.labels"); //$NON-NLS-1$

        private final boolean isPurchase;

        private Type(boolean isPurchase)
        {
            this.isPurchase = isPurchase;
        }

        /**
         * True if the transaction is one of the purchase types such as buy,
         * transfer in, or an inbound delivery.
         */
        public boolean isPurchase()
        {
            return isPurchase;
        }

        /**
         * True if the transaction is one of the liquidation types such as sell,
         * transfer out, or an outbound delivery.
         */
        public boolean isLiquidation()
        {
            return !isPurchase;
        }

        public String MapTo()
        {
            System.err.println("MoneysuiteTransaction.MapTo: " + this.toString());
            switch (this)
            {
                case NONE:
                    return "";
                case BUY:
                    return "KaufX";
                case INVEST:
                    return "Kauf";
                case BUY_BOND:
                    return "AnlageX";
                case SELL:
                    return "VerkaufX";
                case DEVEST:
                    return "Verkauf";
                case REPAYMENT:
                    return "TilgungX";
                case TRANSFER_IN:
                    return "TransferZu";
                case TRANSFER_OUT:
                    return "TransferAb";
                case DELIVERY_INBOUND:
                    return "AktZu";
                case DELIVERY_OUTBOUND:
                    return "AktAb";
                case SPLIT:
                    return "AktSplit";
                case REINVEST:
                    return "ReinvDiv";
                case DEPOSIT:
                    return "XEin";
                case REMOVAL:
                    return "XAus";
                case INTEREST:
                    return "Zinsen/Dividenden";
                case INTEREST_CHARGE:
                    return "Stückzinsen";
                case DIVIDENDS:
                    return "DivX";
                case FEES:
                case FEES_REFUND:
                case TAXES:
                case TAX_REFUND:
                    return "DEADBEEF";
                default:
                    throw new IllegalArgumentException(this.toString());
            }
        }
        
        @Override
        public String toString()
        {
            List<String> TransactionClasses = Arrays.asList("moneysuite", "portfolio", "account");
            for (String TransactionClass : TransactionClasses)
            {
                if (RESOURCES.containsKey(TransactionClass + "." + name()))
                {
                    return RESOURCES.getString(TransactionClass + "." + name()); //$NON-NLS-1$
                }
            }
            throw new NoSuchElementException(name());
        }
    }

    private Type type;


    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

}
