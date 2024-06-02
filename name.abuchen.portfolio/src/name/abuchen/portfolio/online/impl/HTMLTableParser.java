package name.abuchen.portfolio.online.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.safety.Safelist;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.model.LatestSecurityPrice;
import name.abuchen.portfolio.model.SecurityElement;
import name.abuchen.portfolio.online.FeedData;
import name.abuchen.portfolio.util.OnlineHelper;
import name.abuchen.portfolio.util.Pair;
import name.abuchen.portfolio.util.TextUtil;
import name.abuchen.portfolio.util.WebAccess;

abstract class HTMLTableParser
{
    protected GenericPageCache<Pair<String, List<Object>>> cache = new GenericPageCache<>();
 
    // from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    protected static class Spec
    {
        private final Column column;
        private final int index;

        public Spec(Column column, int index)
        {
            this.column = column;
            this.index = index;
        }
        
        public Column getColumn()
        {
            return column;
        }
    }

    // from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    protected static class HeaderInfo
    {
        private final int rowIndex;
        private final int numberOfHeaderColumns;

        public HeaderInfo(int rowIndex, int numberOfHeaderColumns)
        {
            this.rowIndex = rowIndex;
            this.numberOfHeaderColumns = numberOfHeaderColumns;
        }
    }


    protected static class DateColumn extends Column
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        private DateTimeFormatter[] formatters;

        @SuppressWarnings("nls")
        public DateColumn()
        {
            this(new String[] { "Datum.*", "Date.*" });
        }

        @SuppressWarnings("nls")
        public DateColumn(String[] patterns)
        {
            super(patterns);

            formatters = new DateTimeFormatter[] { DateTimeFormatter.ofPattern("y-M-d"),
                            // https://stackoverflow.com/a/29496149/1158146
                            new DateTimeFormatterBuilder().appendPattern("d.M.")
                                            .appendValueReduced(ChronoField.YEAR, 2, 2, Year.now().getValue() - 80)
                                            .toFormatter(),
                            new DateTimeFormatterBuilder().appendPattern("M/d/")
                                            .appendValueReduced(ChronoField.YEAR, 2, 2, Year.now().getValue() - 80)
                                            .toFormatter(),
                            DateTimeFormatter.ofPattern("d.M.yy"), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("d.M.y"), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("d. MMM y"), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("d. MMMM y"), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("d. MMM. y"), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("MMM d, y", Locale.ENGLISH), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("MMM dd, y", Locale.ENGLISH), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("MMM dd y", Locale.ENGLISH), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("d MMM y", Locale.ENGLISH), //$NON-NLS-1$
                            DateTimeFormatter.ofPattern("EEEE, MMMM dd, yEEE, MMM dd, y", Locale.ENGLISH) //$NON-NLS-1$
            };
        }

        @Override
        public void setValue(Element value, Object obj, String languageHint) throws ParseException
        {
            String text = TextUtil.strip(value.text());
            for (int ii = 0; ii < formatters.length; ii++)
            {
                try
                {
                    LocalDate date = LocalDate.parse(text, formatters[ii]);
                    if (date == null)
                        throw new ParseException(text, 0);
                    ((SecurityElement) obj).setDate(date);
                    return;
                }
                catch (DateTimeParseException e) // NOSONAR
                {
                    // continue with next pattern
                }
            }

            throw new ParseException(text, 0);
        }
    }

    public HTMLTableParser()
    {        
    }
    
    public abstract <T extends Object> T newRowObject();
    
    protected Column[] COLUMNS = new Column[] {}; 

    protected String getUserAgent()
    {
        return OnlineHelper.getUserAgent();
    }

    protected Pair<String, List<Object>> _parseFromURL(String url, FeedData data)
    {
        return _parseFromURL(url, false, data);
    }

    protected String _getHtml(String url) throws IOException, URISyntaxException
    {
        String html = ""; //$NON-NLS-1$
        if (url.startsWith("file://")) //$NON-NLS-1$
        {
            String resourceName = url.replaceFirst("^file://", "");  //$NON-NLS-1$ $//$NON-NLS-2$
            InputStream stream =  getClass().getResourceAsStream(resourceName);
            try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name()))
            {
                html = scanner.useDelimiter("\\A").next(); //$NON-NLS-1$
            }
        }
        else
        {
                html = new WebAccess(url) //
                        .addUserAgent(getUserAgent()) //
                        .get();
        }
        return html;
    }

    protected Pair<String, List<Object>> _parseFromURL(String url, boolean collectRawResponse, FeedData data)
    {
        //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed: internalGetQuotes
        Pair<String, List<Object>> answer = cache.lookup(url);

        if (answer == null || (collectRawResponse && answer.getLeft().isEmpty()))
        {
            try
            {
                String html = _getHtml(url);

                Document document = Jsoup.parse(html);
                List<Object>  oList = parse(url, document, data);
                answer = new Pair<>(collectRawResponse ? html : "", oList); //$NON-NLS-1$
                if (!answer.getRight().isEmpty())
                    cache.put(url, answer);
            }
            catch (URISyntaxException | IOException | UncheckedIOException e)
            {
                data.addError(new IOException("<HTMLTableParser::_parseFromURL>: " +  url + '\n' + e.getMessage(), e)); //$NON-NLS-1$
                return new Pair<>(String.valueOf(e.getMessage()), Collections.emptyList());
            }
        }

        if (collectRawResponse)
            data.addResponse(url, answer.getLeft());
        return answer;
    }

    protected List<Object> _parseFromHTML(String html, FeedData data)
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        return parse("n/a", Jsoup.parse(html), data); //$NON-NLS-1$
    }

    @SuppressWarnings("nls")
    protected HeaderInfo buildSpecFromTable(Element table, List<Spec> specs)
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        // check if thead exists
        Elements header = table.select("> thead > tr > th");
        if (!header.isEmpty())
        {
            buildSpecFromRow(header, specs);
            if (!specs.isEmpty())
                return new HeaderInfo(0, header.size());
        }

        header = table.select("> thead > tr > td");
        if (!header.isEmpty())
        {
            buildSpecFromRow(header, specs);
            if (!specs.isEmpty())
                return new HeaderInfo(0, header.size());
        }

        // check if th exist in body
        header = table.select("> tbody > tr > th");
        if (!header.isEmpty())
        {
            buildSpecFromRow(header, specs);
            if (!specs.isEmpty())
                return new HeaderInfo(0, header.size());
        }

        // then check first two regular rows
        int rowIndex = 0;

        Elements rows = table.select("> tbody > tr");
        Elements headerRow = null;

        if (!rows.isEmpty())
        {
            Element firstRow = rows.get(0);
            headerRow = firstRow.select("> td");
            buildSpecFromRow(headerRow, specs);
            rowIndex++;
        }

        if (specs.isEmpty() && rows.size() > 1)
        {
            Element secondRow = rows.get(1);
            headerRow = secondRow.select("> td");
            buildSpecFromRow(headerRow, specs);
            rowIndex++;
        }

        return new HeaderInfo(rowIndex, headerRow != null ? headerRow.size() : 0);
    }

    protected <T extends Object> List<T> parse(String url, Document document, FeedData data)
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        // check if language is provided
        String language = document.select("html").attr("lang"); //$NON-NLS-1$ //$NON-NLS-2$

        List<T> elementList = new ArrayList<>();

        // first: find tables
        Elements tables = document.getElementsByTag("table"); //$NON-NLS-1$
        for (Element table : tables)
        {
            List<Spec> specs = new ArrayList<>();

            HeaderInfo headerInfo = buildSpecFromTable(table, specs);
            int rowIndex = headerInfo.rowIndex;

            if (isSpecValid(specs))
            {
                Elements rows = table.select("> tbody > tr"); //$NON-NLS-1$

                int size = rows.size();
                if (size != 0)
                {
                    for (; rowIndex < size; rowIndex++)
                    {
                        Element row = rows.get(rowIndex);
                        try
                        {
                            T element = extractData(row, specs, language, headerInfo.numberOfHeaderColumns, data);
                            if (element != null)
                            {
                                if (element instanceof LatestSecurityPrice)
                                {
                                    LatestSecurityPrice price = (LatestSecurityPrice) element;
                                    if (price.getDate() == null && price.getTime() != null)
                                        ((LatestSecurityPrice) element).setDate(LocalDate.now());
                                }
                                elementList.add(element);
                            }
                        }
                        catch (Exception e)
                        {
                            data.addError(new IOException("<HTMLTableParser::parse>: " + url + '\n' + e.getMessage(), e)); //$NON-NLS-1$
                        }
                    }

                    // skip all other tables
                    break;
                }
            }
        }

        // if no quotes could be extract, log HTML for further analysis
        if (elementList.isEmpty())
            data.addError(new IOException("<HTMLTableParser::parse>: " + MessageFormat.format(Messages.MsgNoQuotesFoundInHTML, url, //$NON-NLS-1$
                            Jsoup.clean(document.html(), Safelist.relaxed()))));
        else if (elementList.get(0) instanceof LatestSecurityPrice)
        {
            // sort by date and, if available, by time

            Collections.sort(elementList, (r, l) -> {
                int compare = ((SecurityElement) l).getDate().compareTo(((SecurityElement) r).getDate());
                if (compare == 0  && l instanceof LatestSecurityPrice && r instanceof LatestSecurityPrice)
                {
                    if (((LatestSecurityPrice) r).getTime() == null || ((LatestSecurityPrice) l).getTime() == null)
                        return 0;

                    return ((LatestSecurityPrice) l).getTime().compareTo(((LatestSecurityPrice) r).getTime());
                }
                else
                    return compare;
            });

            // remove the duplicates with the same date (keep the one with the
            // oldest time)

            List<T> answer = new ArrayList<>(elementList.size());

            LocalDate last = null;
            for (T t : elementList)
            {
                SecurityElement e = (SecurityElement) t;
                if (e.getDate().equals(last))
                    continue;

                answer.add(t);
                last = e.getDate();
            }
            return answer;
        }
        return elementList;
    }

    protected Column[] getColumns()
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        return COLUMNS;
    }

    protected void buildSpecFromRow(Elements row, List<Spec> specs)
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        Set<Column> available = new HashSet<>();
        Collections.addAll(available, getColumns());

        for (int ii = 0; ii < row.size(); ii++)
        {
            Element element = row.get(ii);

            if (element.hasAttr("colspan")) //$NON-NLS-1$
            {
                int colspan = Integer.valueOf(element.attr("colspan")); //$NON-NLS-1$
                // remove attribute
                element.removeAttr("colspan"); //$NON-NLS-1$

                // add copies of this column to the header to align header with
                // columns in table
                for (int c = 1; c < colspan; c++)
                {
                    row.add(ii, element);
                }
            }

            for (Column column : available)
            {
                if (column.matches(element))
                {
                    specs.add(new Spec(column, ii));
                    available.remove(column);
                    break;
                }
            }
        }
    }

    protected boolean isSpecValid(List<Spec> specs)
    {
        return false;
    }
    
    private <T extends Object> T extractData(Element row, List<Spec> specs, String languageHint, int numberOfHeaderColumns, FeedData data)
    //from: name.abuchen.portfolio.online.impl/HTMLTableQuoteFeed.java
    {
        Elements cells = row.select("> td"); //$NON-NLS-1$

        // we're only looking at rows having the same size as the header row
        if (cells.size() != numberOfHeaderColumns)
            return null;

        T obj = newRowObject();

        for (Spec spec : specs)
        {
            if (obj == null)
                break;
            try
            {
                spec.column.setValue(cells.get(spec.index), obj, languageHint);
            }
            catch (Exception e)
            {
                data.addError(new IOException(MessageFormat.format(Messages.MsgParsingFailedWithHTML, spec.column.toString(), spec.index,  cells.toString())));
            }
        }

        return obj;
    }

    
}