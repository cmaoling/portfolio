package name.abuchen.portfolio.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ResourceBundle;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.money.Monetary;
import name.abuchen.portfolio.money.Values;

public class SecurityEvent extends SecurityElement
{
    public enum Type
    {
        STOCK_SPLIT(false, false), STOCK_DIVIDEND(true, false), STOCK_RIGHT(true, false), STOCK_OTHER(true, false), NONE(false, false), NOTE(true, true);
        // upstream STOCK_DIVIDEND is called DIVIDEND_PAYMENT

        private static final ResourceBundle RESOURCES = ResourceBundle.getBundle("name.abuchen.portfolio.model.labels"); //$NON-NLS-1$

        private boolean isUserEditable;
        private boolean isFlexible;

        private Type(boolean isFlexible, boolean isUserEditable)
        {
            this.isUserEditable = isUserEditable;
            this.isFlexible = isFlexible;
        }

        public boolean isUserEditable()
        {
            return isUserEditable;
        }

        public boolean isFlexible()
        {
            return isFlexible;
        }

        @Override
        public String toString()
        {
            return RESOURCES.getString("event." + name()); //$NON-NLS-1$
        }
    }

    private Type type = Type.NONE;

    private Monetary amount = null;

    private LocalDate exDate = null;

    private LocalDate paymentDate = null;

    private double[] ratio = null;

    private String typeStr = null;

    private boolean isVisible = true;

    protected String details = null;

    private String source = null;

    @Deprecated
    protected long value;

//+    public static class DividendEvent extends SecurityEvent
//+    {
//+        private LocalDate paymentDate;
//+        private Money amount;
//+        private String source;
//+
//+        public DividendEvent()
//+        {
//+            super(null, Type.DIVIDEND_PAYMENT, null);
//+        }
//+
//+        public DividendEvent(LocalDate exDate, LocalDate payDate, Money amount, String source)
//+        {
//+            super(exDate, Type.DIVIDEND_PAYMENT, null);
//+            this.paymentDate = payDate;
//+            this.amount = amount;
//+            this.source = source;
//+        }
//+
//+        @Override
//+        public void setType(Type type)
//+        {
//+            if (type != Type.DIVIDEND_PAYMENT)
//+                throw new IllegalArgumentException();
//+        }
//+
//+        public LocalDate getPaymentDate()
//+        {
//+            return paymentDate;
//+        }
//+
//+        public void setPaymentDate(LocalDate payDate)
//+        {
//+            this.paymentDate = payDate;
//+        }
//+
//+        public Money getAmount()
//+        {
//+            return amount;
//+        }
//+
//+        public void setAmount(Money amount)
//+        {
//+            this.amount = amount;
//+        }
//+
//+        public String getSource()
//+        {
//+            return source;
//+        }
//+
//+        public void setSource(String source)
//+        {
//+            this.source = source;
//+        }
//+
//+        @Override
//+        public int hashCode()
//+        {
//+            final int prime = 31;
//+            int result = super.hashCode();
//+            result = prime * result + Objects.hash(amount, paymentDate, source);
//+            return result;
//+        }
//+
//+        @Override
//+        public boolean equals(Object obj)
//+        {
//+            if (this == obj)
//+                return true;
//+            if (!super.equals(obj))
//+                return false;
//+            if (getClass() != obj.getClass())
//+                return false;
//+            DividendEvent other = (DividendEvent) obj;
//+            return Objects.equals(amount, other.amount) && Objects.equals(paymentDate, other.paymentDate)
//+                            && Objects.equals(source, other.source);
//+        }
//+    }

    public SecurityEvent()
    {
    }

    public SecurityEvent(LocalDate exDate, LocalDate payDate, Monetary monetary, String source)
    {
        this(exDate, Type.STOCK_DIVIDEND, null);
        this.paymentDate = payDate;
        this.amount = monetary;
        setSource(source);
    }

    public SecurityEvent(LocalDate date, Type type, String details)
    {
        this(date, type);
        this.details = details;
    }

    public SecurityEvent(LocalDate date, Type type)
    {
        super.setDate(date);
        setType(type);
    }
    
    public SecurityEvent setAmount(Monetary money)
    {
        this.amount = money;
        return this;
    }

    public SecurityEvent setAmount(String currencyCode, double value)
    {
        this.amount = new Monetary().valueOf(currencyCode, BigDecimal.valueOf(value));
        return this;
    }

    public SecurityEvent setAmount(String currencyCode, BigDecimal value)
    {
        this.amount = new Monetary().valueOf(currencyCode, value);
        return this;
    }

    public Monetary getAmount()
    {
        if (amount == null)
            throw new NoSuchElementException();
        return amount;
    }

    public SecurityEvent clearAmount()
    {
        if (amount != null && amount.getCurrency().equals(Messages.LabelNoCurrencyCode))
            amount = null;
        return this;
    }

    public SecurityEvent setExDate(LocalDate date)
    {
        this.exDate = date;
        return this;
    }

    public LocalDate getExDate()
    {
        if (exDate == null)
            return this.date;
        return this.exDate;
    }

    public LocalDate getPaymentDate()
    {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate payDate)
    {
        this.paymentDate = payDate;
    }

    public SecurityEvent setRatio(double enumerator, double denumerator)
    {
        ratio = new double[]{enumerator, denumerator};
        return this;
    }

    public SecurityEvent setRatio(double enumerator)
    {
        ratio = new double[]{enumerator, (double) 1.0};
        return this;
    }

    public String getRatioString()
    {
        if (ratio == null || ratio.length != 2)
        {
            System.err.println("SecurityEvent.getRatioString() - event: " + String.format("[%s] EVENT %tF (ex: %tF): [%s-%s] amount: <%s> ratio: <-/->", //$NON-NLS-1$ //$NON-NLS-2$ 
                                            (isVisible ? "+"                                           : "o" ), //$NON-NLS-1$ //$NON-NLS-2$
                                            date,
                                            (exDate == null  ? LocalDate.of(1900, Month.JANUARY, 1)  : exDate),
                                             type.toString(),
                                            (typeStr == null ? ""                                    : typeStr), //$NON-NLS-1$
                                            (amount == null  ? new Monetary()                        :  amount).toString()
                                            ));
            throw new NoSuchElementException();
        }
        // taken from http://stackoverflow.com/questions/8741107/format-a-double-to-omit-unnecessary-0-and-never-round-off
        double e = ratio[0];
        String eStr = (long) e == e ? "" + (long) e : "" + e; //$NON-NLS-1$ //$NON-NLS-2$ 
        double d = ratio[1];
        String dStr = (long) d == d ? "" + (long) d : "" + d; //$NON-NLS-1$ //$NON-NLS-2$
        return  eStr  + ":" + dStr; //$NON-NLS-1$
    }

    public double[] getRatio()
    {
        if (ratio == null)
            throw new NoSuchElementException();
        return ratio;
    }
    
    public SecurityEvent clearRatio()
    {
        ratio = null;
        return this;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getSource()
    {
        if (source != null)
            return source;
        else
            return Messages.LabelNothing;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Type getType()
    {
        return type;
    }

    public SecurityEvent setTypeStr(String str)
    {
        this.typeStr = str;
        return this;
    }

    public String getTypeStr()
    {
        return (typeStr == null ? type.toString() : typeStr);
    }

    public SecurityEvent clearTypeStr()
    {
        if (typeStr != null && typeStr.equals(type.toString()))
        {
            typeStr = null;
        }
        return this;
    }

    @Deprecated
    public String getDetails()
    {
          if (details != null)
              return details;
          else
              return Messages.LabelNothing;
    }
    
    public String getExplaination()
    {
        if (type.equals(Type.STOCK_DIVIDEND))
        {
            return getAmount().toString();
        }
        else if (type.equals(Type.STOCK_SPLIT))
        {
            return getRatioString();
        }
        else if (type.equals(Type.STOCK_RIGHT))
        {
            return (ratio == null ? "" : getRatioString()) + (amount == null? "" : " " + Messages.LabelStockRightReference + " - " + getAmount().toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        else if (type.equals(Type.STOCK_OTHER))
        {
            return getTypeStr() + ": " + (ratio == null ? "" : getRatioString()) + (amount == null? "" : " @ " + getAmount().toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        else if (type.equals(Type.NOTE))
        {
            return (details == null? "" : details); //$NON-NLS-1$ 
        }
        else
        {
            return Type.NONE.toString();
        }
    }

    public SecurityEvent clearDetails()
    {
        this.details = null;
        return this;
    }

    public SecurityEvent hide()
    {
        isVisible = false;
        return this;
    }

    public SecurityEvent unhide()
    {
        isVisible = true;
        return this;
    }

    public boolean isVisible()
    {
        return isVisible;
    }

    @Override
    public String toString()
    {
        return String.format("[%s] EVENT %tF (ex: %tF / pay: %tF): [%s-%s] amount: <%s> ratio: <%s> source; <%s> => [%08x] deprecated: value %,10.2f details: <%s>", //$NON-NLS-1$
                        (isVisible ? "+"                                              : "o" ), //$NON-NLS-1$ //$NON-NLS-2$
                        date,
                        (exDate == null  ? LocalDate.of(1900, Month.JANUARY, 1)       : exDate),
                        (paymentDate == null  ? LocalDate.of(1970, Month.JANUARY, 1)  : paymentDate),
                         type.toString(),
                        (typeStr == null ? ""                                         : typeStr), //$NON-NLS-1$
                        (amount == null  ? new Monetary()                             :  amount).toString(),
                        (ratio  == null ? Messages.LabelNoRatio                       :  getRatioString()),
                        (source == null  ? ""                                         :  source).toString(), //$NON-NLS-1$
                        this.hashCode(),
                        value / Values.Quote.divider(),
                        (details == null ? "?"                                        : details.toString()) //$NON-NLS-1$
                        );
    }

    public static List<SecurityEvent> castElement2EventList(List<SecurityElement> iList)
    {
        if (iList != null)
        {
            List<SecurityEvent> oList = new ArrayList<>();
            for (SecurityElement e : iList)
            {
                    if (e instanceof SecurityEvent)
                        oList.add((SecurityEvent) e); // need to cast each object specifically
            }
            return oList;
        }
        else
        {
            return null;
        }
    }

      @Override
      public int hashCode()
      {
          final int prime = 31;
          int result = 1;
          result = prime * result + ((date    == null) ? 0                     : date.hashCode());
          result = prime * result + ((exDate  == null) ? 0                     : exDate.hashCode());
          result = prime * result + ((paymentDate  == null) ? 0                : paymentDate.hashCode());
          result = prime * result + ((details == null) ? "?"                   : details).hashCode(); //$NON-NLS-1$
          result = prime * result + ((source == null) ? "=/="                  : source).hashCode(); //$NON-NLS-1$
          result = prime * result + ((ratio == null)   ? Messages.LabelNoRatio : getRatioString()).hashCode();
          result = prime * result + type.hashCode();
          result = prime * result + getTypeStr().hashCode();
          result = prime * result + ((amount == null)  ? new Monetary()        : amount).toString().hashCode();
          return result;
      }

//    @Override
//    public int compareTo(SecurityEvent o)
//    {
//        return super.date.compareTo(o.date);
//    }


    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SecurityEvent other = (SecurityEvent) obj;
        if (date == null)
        {
            if (other.date != null)
                return false;
        }
        else if (other.date == null)
            return false;
        else if (!date.equals(other.date))
            return false;
        if (amount == null)
        {
            if (other.amount != null)
                return false;
        }
        else if (other.amount == null)
            return false;
        else if (!amount.toString().equals(other.amount.toString()))
            return false;
        if (hashCode() != other.hashCode())
            return false;
        return Objects.equals(date, other.date) && Objects.equals(details, other.details) && type == other.type 
                        && Objects.equals(exDate, other.exDate)&& Objects.equals(paymentDate, other.paymentDate)
                        && Objects.equals(source, other.source);
    }
}
