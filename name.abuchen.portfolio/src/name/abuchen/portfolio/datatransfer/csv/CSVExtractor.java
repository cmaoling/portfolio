package name.abuchen.portfolio.datatransfer.csv;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Column;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Field;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.FieldFormat;
import name.abuchen.portfolio.datatransfer.csv.CSVImporter.Header;
import name.abuchen.portfolio.money.Values;

public abstract class CSVExtractor implements Extractor
{
    public abstract List<Field> getFields();

    public abstract List<Item> extract(int skipLines, List<String[]> rawValues, Map<String, Column> field2column,
                    List<Exception> errors);

    @Override
    public String getFilterExtension()
    {
        return "*.csv"; //$NON-NLS-1$
    }

    @Override
    public List<Item> extract(List<File> files, List<Exception> errors)
    {
        throw new UnsupportedOperationException();
    }

    public int getDefaultSkipLines()
    {
        return 0;
    }

    public Header.Type getDefaultHeadering()
    {
        return Header.Type.FIRST;
    }

    public String getDefaultEncoding()
    {
        return "UTF-8";
    }

    public <E extends Enum<E>> EnumMap<E, String> getDefaultEnum(Class<E> enumType)
    {
        return null;
    }

    public String[] getDefaultHeader()
    {
        return null;
    }
    
    protected String getText(String name, String[] rawValues, Map<String, Column> field2column)
    {
        Column column = field2column.get(name);
        if (column == null)
            return null;

        int columnIndex = column.getColumnIndex();

        if (columnIndex < 0 || columnIndex >= rawValues.length)
            return null;

        String value = rawValues[columnIndex];
        return value != null && value.trim().length() == 0 ? null : value;
    }

    protected String getISIN(String name, String[] rawValues, Map<String, Column> field2column)
    {
        Column column = field2column.get(name);
        if (column == null)
            return null;

        int columnIndex = column.getColumnIndex();

        if (columnIndex < 0 || columnIndex >= rawValues.length)
            return null;

        String value = rawValues[columnIndex];

        Pattern pattern = Pattern.compile(" ([A-Z]{2}[A-Z0-9]{9}\\d) ");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find())
        {
            value= matcher.group(1);
        }

        return value != null && value.trim().length() == 0 ? null : value;
    }

    protected Long getAmount(String name, String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        return getValue(name, rawValues, field2column, Values.Amount);
    }

    protected Long getQuote(String name, String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        return getValue(name, rawValues, field2column, Values.Quote);
    }

    protected Long getValue(String name, String[] rawValues, Map<String, Column> field2column, Values<Long> values)
                    throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;

        Number num = (Number) field2column.get(name).getFormat().getFormat().parseObject(value);
        return Long.valueOf((long) Math.round(num.doubleValue() * values.factor()));
    }

    protected LocalDate getDate(String name, String[] rawValues, Map<String, Column> field2column) throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;
        Date date = (Date) field2column.get(name).getFormat().getFormat().parseObject(value);
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }

    protected final BigDecimal getBigDecimal(String name, String[] rawValues, Map<String, Column> field2column)
                    throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;

        Number num = (Number) field2column.get(name).getFormat().getFormat().parseObject(value);
        return BigDecimal.valueOf(num.doubleValue());
    }

    protected final Long getShares(String name, String[] rawValues, Map<String, Column> field2column)
                    throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;

        Number num = (Number) field2column.get(name).getFormat().getFormat().parseObject(value);
        return Math.round(Math.abs(num.doubleValue()) * Values.Share.factor());
    }

    @SuppressWarnings("unchecked")
    protected final <E extends Enum<E>> E getEnum(String name, Class<E> type, String[] rawValues,
                    Map<String, Column> field2column) throws ParseException
    {
        String value = getText(name, rawValues, field2column);
        if (value == null)
            return null;
        FieldFormat ff = field2column.get(name).getFormat();

        if (ff != null && ff.getFormat() != null)
            return (E) ff.getFormat().parseObject(value);
        else
            return Enum.valueOf(type, value);
    }
}
