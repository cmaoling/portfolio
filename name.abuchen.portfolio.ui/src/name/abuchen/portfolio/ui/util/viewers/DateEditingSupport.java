package name.abuchen.portfolio.ui.util.viewers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import java.text.MessageFormat;

import name.abuchen.portfolio.money.Values;
import name.abuchen.portfolio.ui.Messages;

public class DateEditingSupport extends PropertyEditingSupport
{
    private static final DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT), //
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG), //
                    DateTimeFormatter.ofPattern("d.M.yyyy"), //$NON-NLS-1$
                    DateTimeFormatter.ofPattern("d.M.yy"), //$NON-NLS-1$
                    DateTimeFormatter.ISO_DATE };

    public DateEditingSupport(Class<?> subjectType, String attributeName)
    {
        super(subjectType, attributeName);

        if (!LocalDate.class.isAssignableFrom(descriptor().getPropertyType()))
            throw new RuntimeException(String.format("Property %s needs to be of type date", attributeName)); //$NON-NLS-1$
    }

    @Override
    public CellEditor createEditor(Composite composite)
    {
        TextCellEditor textEditor = new TextCellEditor(composite);
        ((Text) textEditor.getControl()).setTextLimit(20);
        return textEditor;
    }

    @Override
    public final Object getValue(Object element) throws Exception
    {
        LocalDate date = (LocalDate) descriptor().getReadMethod().invoke(adapt(element));
        return Values.Date.format(date);
    }

    @Override
    public final void setValue(Object element, Object value) throws Exception
    {
        Object subject = adapt(element);
        LocalDate newValue = null;

        for (DateTimeFormatter formatter : formatters)
        {
            try
            {
                newValue = LocalDate.parse(String.valueOf(value), formatter);
                break;
            }
            catch (DateTimeParseException ignore)
            {
                // continue with next formatter
            }
        }

        if (newValue == null)
            throw new IllegalArgumentException(MessageFormat.format(Messages.MsgErrorNotAValidDate, value));

        LocalDate oldValue = (LocalDate) descriptor().getReadMethod().invoke(subject);

        if (!newValue.equals(oldValue))
        {
            descriptor().getWriteMethod().invoke(subject, newValue);
            notify(element, newValue, oldValue);
        }
    }
}
