package name.abuchen.portfolio.online.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.PortfolioLog;
import name.abuchen.portfolio.model.LatestSecurityPrice;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityPrice;
import name.abuchen.portfolio.online.QuoteFeed;
import name.abuchen.portfolio.online.FeedData;
import name.abuchen.portfolio.online.impl.variableurl.Factory;
import name.abuchen.portfolio.online.impl.variableurl.urls.VariableURL;
import name.abuchen.portfolio.snapshot.QuoteQualityMetrics;
import name.abuchen.portfolio.util.Interval;
import name.abuchen.portfolio.util.Pair;

public class HTMLTableQuoteFeed implements QuoteFeed
{
// ==> name.abuchen.portfolio.online.impl/Column.java
//    protected abstract static class Column
//    {
//
//        static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT_GERMAN = ThreadLocal
//                        .withInitial(() -> new DecimalFormat("#,##0.###", new DecimalFormatSymbols(Locale.GERMAN))); //$NON-NLS-1$
//
//        static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT_ENGLISH = ThreadLocal
//                        .withInitial(() -> new DecimalFormat("#,##0.###", new DecimalFormatSymbols(Locale.ENGLISH))); //$NON-NLS-1$
//
//        static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT_APOSTROPHE = ThreadLocal.withInitial(() -> {
//            DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols(Locale.US);
//            unusualSymbols.setGroupingSeparator('\'');
//            return new DecimalFormat("#,##0.##", unusualSymbols); //$NON-NLS-1$
//        });
//
//        private final Pattern[] patterns;
//
//        protected Column(String[] strings)
//        {
//            this.patterns = new Pattern[strings.length];
//            for (int ii = 0; ii < strings.length; ii++)
//                this.patterns[ii] = Pattern.compile(strings[ii]);
//        }
//
//        protected boolean matches(Element header)
//        {
//            String text = TextUtil.strip(header.text());
//            for (Pattern pattern : patterns)
//            {
//                if (pattern.matcher(text).matches())
//                    return true;
//            }
//            return false;
//        }
//
//        abstract void setValue(Element value, LatestSecurityPrice price, String languageHint) throws ParseException;
//
//        protected double asDouble(Element elem, String languageHint) throws ParseException
//        {
//            return asDouble(elem.text().trim(), languageHint);
//        }
//
//        static public double asDouble(String text, String languageHint) throws ParseException
//        {
//            DecimalFormat format = null;
//
//            if ("de".equals(languageHint)) //$NON-NLS-1$
//                format = DECIMAL_FORMAT_GERMAN.get();
//            else if ("en".equals(languageHint)) //$NON-NLS-1$
//                format = DECIMAL_FORMAT_ENGLISH.get();
//
//            if (format == null)
//            {
//                // check first for apostrophe
//
//                int apostrophe = text.indexOf('\'');
//                if (apostrophe >= 0)
//                    format = DECIMAL_FORMAT_APOSTROPHE.get();
//            }
//
//            if (format == null)
//            {
//                // determine format based on the relative location of the last
//                // comma and dot, e.g. the last comma indicates a German number
//                // format
//                int lastDot = text.lastIndexOf('.');
//                int lastComma = text.lastIndexOf(',');
//                format = Math.max(lastDot, lastComma) == lastComma ? DECIMAL_FORMAT_GERMAN.get()
//                                : DECIMAL_FORMAT_ENGLISH.get();
//            }
//            double value = format.parse(text).doubleValue(); 
//            return value;
//        }
//
//        protected long asQuote(Element value, String languageHint) throws ParseException
//        {
//            return Math.round(asDouble(value,languageHint) * Values.Quote.factor());
//        }
//
//        protected int asInt(Element elem, String languageHint) throws ParseException
//        {
//            return (int)asDouble(elem,languageHint);
//        }
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableParser
//    protected static class DateColumn extends Column
//    {
//        private DateTimeFormatter[] formatters;
//
//        @SuppressWarnings("nls")
//        public DateColumn()
//        {
//            this(new String[] { "Datum.*", "Date.*" });
//        }
//
//        @SuppressWarnings("nls")
//        public DateColumn(String[] patterns)
//        {
//            super(patterns);
//
//            formatters = new DateTimeFormatter[] { DateTimeFormatter.ofPattern("y-M-d"),
//                            // https://stackoverflow.com/a/29496149/1158146
//                            new DateTimeFormatterBuilder().appendPattern("d.M.")
//                                            .appendValueReduced(ChronoField.YEAR, 2, 2, Year.now().getValue() - 80)
//                                            .toFormatter(),
//                            new DateTimeFormatterBuilder().appendPattern("M/d/")
//                                            .appendValueReduced(ChronoField.YEAR, 2, 2, Year.now().getValue() - 80)
//                                            .toFormatter(),
//                            DateTimeFormatter.ofPattern("d.M.yy"), //$NON-NLS-1$
//                            DateTimeFormatter.ofPattern("d.M.y"), //$NON-NLS-1$
//                            DateTimeFormatter.ofPattern("d. MMM y"), //$NON-NLS-1$
//                            DateTimeFormatter.ofPattern("d. MMMM y"), //$NON-NLS-1$
//                            DateTimeFormatter.ofPattern("d. MMM. y"), //$NON-NLS-1$
//                            DateTimeFormatter.ofPattern("MMM d, y", Locale.ENGLISH), //$NON-NLS-1$
//                            DateTimeFormatter.ofPattern("MMM dd, y", Locale.ENGLISH), //$NON-NLS-1$
//                            DateTimeFormatter.ofPattern("EEEE, MMMM dd, yEEE, MMM dd, y", Locale.ENGLISH) //$NON-NLS-1$
//            };
//        }
//
//        @Override
//        void setValue(Element value, LatestSecurityPrice price, String languageHint) throws ParseException
//        {
//            String text = TextUtil.strip(value.text());
//            for (int ii = 0; ii < formatters.length; ii++)
//            {
//                try
//                {
//                    LocalDate date = LocalDate.parse(text, formatters[ii]);
//                    price.setDate(date);
//                    return;
//                }
//                catch (DateTimeParseException e) // NOSONAR
//                {
//                    // continue with next pattern
//                }
//            }
//
//            throw new ParseException(text, 0);
//        }
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableQuoteParser    
//    protected static class CloseColumn extends Column
//    {
//        @SuppressWarnings("nls")
//        public CloseColumn()
//        {
//            super(new String[] { "Schluss.*", "Schluß.*", "Rücknahmepreis.*", "Close.*", "Zuletzt", "Price",
//                            "akt. Kurs", "Dernier" });
//        }
//
//        public CloseColumn(String[] patterns)
//        {
//            super(patterns);
//        }
//
//        @Override
//        void setValue(Element value, LatestSecurityPrice price, String languageHint) throws ParseException
//        {
//            price.setValue(asQuote(value, languageHint));
//        }
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableQuoteParser    
//    protected static class HighColumn extends Column
//    {
//        @SuppressWarnings("nls")
//        public HighColumn()
//        {
//            super(new String[] { "Hoch.*", "Tageshoch.*", "Max.*", "High.*" });
//        }
//
//        public HighColumn(String[] patterns)
//        {
//            super(patterns);
//        }
//
//        @Override
//        void setValue(Element value, LatestSecurityPrice price, String languageHint) throws ParseException
//        {
//            if ("-".equals(value.text().trim())) //$NON-NLS-1$
//                price.setHigh(LatestSecurityPrice.NOT_AVAILABLE);
//            else
//                price.setHigh(asQuote(value, languageHint));
//        }
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableQuoteParser    
//    protected static class LowColumn extends Column
//    {
//        @SuppressWarnings("nls")
//        public LowColumn()
//        {
//            super(new String[] { "Tief.*", "Tagestief.*", "Low.*" });
//        }
//
//        public LowColumn(String[] patterns)
//        {
//            super(patterns);
//        }
//
//        @Override
//        void setValue(Element value, LatestSecurityPrice price, String languageHint) throws ParseException
//        {
//            if ("-".equals(value.text().trim())) //$NON-NLS-1$
//                price.setLow(LatestSecurityPrice.NOT_AVAILABLE);
//            else
//                price.setLow(asQuote(value, languageHint));
//        }
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableQuoteParser    
//    protected static class VolumeColumn extends Column
//    {
//        @SuppressWarnings("nls")
//        public VolumeColumn()
//        {
//            super(new String[] { "Volume.*", "Umsatz" , "Stücke" });
//        }
//
//        @Override
//        public void setValue(Element value, LatestSecurityPrice price, String languageHint) throws ParseException
//        {
//            if ("-".equals(value.text().trim())) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//                price.setVolume((int) LatestSecurityPrice.NOT_AVAILABLE);
//            else
//                price.setVolume(asInt(value, languageHint));
//        }
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableParser
//    private static class Spec
//    {
//        private final Column column;
//        private final int index;
//
//        public Spec(Column column, int index)
//        {
//            this.column = column;
//            this.index = index;
//        }
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableParser
//    private static class HeaderInfo
//    {
//        private final int rowIndex;
//        private final int numberOfHeaderColumns;
//
//        public HeaderInfo(int rowIndex, int numberOfHeaderColumns)
//        {
//            this.rowIndex = rowIndex;
//            this.numberOfHeaderColumns = numberOfHeaderColumns;
//        }
//    }

    public static final String ID = HTML;

//    => name.abuchen.portfolio.online.impl/HTMLTableQuoteParser
//    private static final Column[] COLUMNS = new Column[] { new DateColumn(), new CloseColumn(), new HighColumn(),
//                    new LowColumn(), new VolumeColumn() };

//    => // => name.abuchen.portfolio.online.impl/HTMLTableParser:GenericPageCache
//    private final PageCache<Pair<String, List<LatestSecurityPrice>>> cache = new PageCache<>();
    private final HTMLTableQuoteParser Parser = new HTMLTableQuoteParser();

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getName()
    {
        return Messages.LabelHTMLTable;
    }

    @Override
    public Optional<String> getHelpURL()
    {
        return Optional.of("https://help.portfolio-performance.info/kursdaten_laden/#tabelle-auf-einer-webseite"); //$NON-NLS-1$
    }

    @Override
    public Optional<LatestSecurityPrice> getLatestQuote(Security security)
    {
        FeedData data = getLatestData(security);
        return getLatestQuote(data);
    }
    
    public FeedData getLatestData(Security security)
    {
        // if latestFeed is null, then the policy is 'use same configuration
        // as historic quotes'
        String feedURL = security.getLatestFeed() == null ? security.getFeedURL() : security.getLatestFeedURL();

        FeedData data = internalGetQuotes(security, feedURL, false, false);

        return data;
    }

    public Optional<LatestSecurityPrice> getLatestQuote(FeedData data)
    {
        if (!data.getErrors().isEmpty())
            PortfolioLog.error(data.getErrors());

        List<LatestSecurityPrice> prices = data.getLatestPrices();
        if (prices.isEmpty())
            return Optional.empty();

        Collections.sort(prices, new SecurityPrice.ByDate());

        return Optional.of(prices.get(prices.size() - 1));
    }

    @Override
    public FeedData getHistoricalQuotes(Security security, boolean collectRawResponse)
    {
        return internalGetQuotes(security, security.getFeedURL(), collectRawResponse, false);
    }

    public FeedData getHistoricalQuotes(String html)
    {
        FeedData data = new FeedData();
        data.addAllPrices(Parser.parseFromHTML(html, data));
        return data;
    }

    @Override
    public FeedData previewHistoricalQuotes(Security security)
    {
        return internalGetQuotes(security, security.getFeedURL(), true, true);
    }

    private FeedData internalGetQuotes(Security security, String feedURL, boolean collectRawResponse,
                    boolean isPreview)
    {
        if (feedURL == null || feedURL.length() == 0)
        {
            return FeedData.withError(
                            new IOException(MessageFormat.format(Messages.MsgMissingFeedURL, security.getName())));
        }

        FeedData data = new FeedData();

        VariableURL variableURL = Factory.fromString(feedURL);
        variableURL.setSecurity(security);

        SortedSet<LatestSecurityPrice> newPricesByDate = new TreeSet<>(new SecurityPrice.ByDate());
        long failedAttempts = 0;
        long maxFailedAttempts = variableURL.getMaxFailedAttempts();

        List<String> urls = new ArrayList<>();
        Set<String> missingMonths = new HashSet<>();

        String ariva = "www.ariva.de"; //$NON-NLS-1$
        if (feedURL.startsWith("http://" + ariva)) //$NON-NLS-1$
            PortfolioLog.warning(MessageFormat.format(Messages.MsgArivaWarningHTTP, feedURL));
        if ((feedURL.contains("://" + ariva + "/") || feedURL.startsWith("http")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            && feedURL.endsWith("month=")) //$NON-NLS-1$
        {
            maxFailedAttempts = 5;
            QuoteQualityMetrics metrics = new QuoteQualityMetrics(security);
            for(Interval interval: metrics.getMissingIntervals())
            {
                LocalDate start = interval.getStart();
                missingMonths.add(String.format("%04d-%02d", start.getYear(), start.getMonthValue())); //$NON-NLS-1$
                LocalDate stop = interval.getEnd();
                missingMonths.add(String.format("%04d-%02d", stop.getYear(), stop.getMonthValue())); //$NON-NLS-1$
            }
            int urlCount = 0;
            for (String missingMonth : missingMonths)
            {
                urls.add(feedURL + missingMonth + "-31"); //$NON-NLS-1$
                urlCount += 1;
                if (urlCount > 10)
                    break;
            }
            if (missingMonths.size() == 0)
                urls.add(feedURL);
        }
        else
        {
            for (String url : variableURL) // NOSONAR
                urls.add(url);
        }
        // DEBUG: PortfolioLog.info(" URLS: " + urls); //$NON-NLS-1$

        for (String url : urls) // NOSONAR
        {
            // => name.abuchen.portfolio.online.impl/HTMLTableParser:_parseFromUrl
            // Pair<String, List<LatestSecurityPrice>> answer = cache.lookup(url);
            //
            //if (answer == null || (collectRawResponse && answer.getLeft().isEmpty()))
            //{
            //     answer = parseFromURL(url, collectRawResponse, data);
            //
            //     if (!answer.getRight().isEmpty())
            //          cache.put(url, answer);
            //}
            //
            //if (collectRawResponse)
            //    data.addResponse(url, answer.getLeft());
            Pair<String, List<LatestSecurityPrice>> answer = null;
            try
            {
                answer = Parser.parseFromURL(url, collectRawResponse, data);
            }
            catch (Exception e)
            {
                continue;
            }

            int sizeBefore = newPricesByDate.size();
            newPricesByDate.addAll(answer.getRight());

            if (newPricesByDate.size() > sizeBefore)
                failedAttempts = 0;
            else if (++failedAttempts > maxFailedAttempts)
                break;

            if (isPreview && newPricesByDate.size() >= 100)
                break;
            int delay = (newPricesByDate.size() - sizeBefore) * 100;
            if (feedURL.contains("://" + ariva + "/") || feedURL.startsWith("http")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            {
                try
                {
                    TimeUnit.MILLISECONDS.sleep(delay);
                }
                catch (InterruptedException ie)
                {
                    continue;
                }
            }
        }
        PortfolioLog.info(MessageFormat.format(Messages.MsgQuotesReceived, security.toString(), newPricesByDate.size(), urls.size()));
        data.addAllPrices(newPricesByDate);
        if (feedURL.contains("://" + ariva + "/") || feedURL.startsWith("http") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        && feedURL.endsWith("month=")) //$NON-NLS-1$
        {
            if (missingMonths.size() == 0 && newPricesByDate.size() == 0)
                PortfolioLog.info(MessageFormat.format(Messages.MsgArivaNoMissingMonths, security.toString()));
            else if (missingMonths.size() > 0 && newPricesByDate.size() > 0)
                PortfolioLog.info(MessageFormat.format(Messages.MsgArivaLoadMissingMonths, security.toString(), missingMonths));
            else if (missingMonths.size() > 0 && newPricesByDate.size() == 0)
                PortfolioLog.warning(MessageFormat.format(Messages.MsgArivaLoadMissingMonths, security.toString(), missingMonths));
        }

        return data;
    }

//  => name.abuchen.portfolio.online.impl/HTMLTableParser
//    protected String getUserAgent()
//    {
//        return OnlineHelper.getUserAgent();
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableParser:_parseFromUrl
//    protected Pair<String, List<LatestSecurityPrice>> parseFromURL(String url, QuoteFeedData data)
//    {
//        try
//        {
//            String html = new WebAccess(url) //
//                            .addUserAgent(Parser.getUserAgent()) //
//                            .get();
//
//            Document document = Jsoup.parse(html);
//            List<LatestSecurityPrice> prices = Parser.parse(url, document, data);
//
//            return new Pair<>(collectRawResponse ? html : "", prices); //$NON-NLS-1$
//        }
//        catch (URISyntaxException | IOException | UncheckedIOException e)
//        {
//            data.addError(new IOException(url + '\n' + e.getMessage(), e));
//            return new Pair<>(String.valueOf(e.getMessage()), Collections.emptyList());
//        }
//    }
//
//  => name.abuchen.portfolio.online.impl/HTMLTableParser.java
//    protected List<LatestSecurityPrice> parseFromHTML(String html, QuoteFeedData data)
//    {
//        return parse("n/a", Jsoup.parse(html), data); //$NON-NLS-1$
//    }
//
//    =>   name.abuchen.portfolio.online.impl/HTMLTableParser.java
//    private List<LatestSecurityPrice> parse(String url, Document document, QuoteFeedData data)
//    {
//        // check if language is provided
//        String language = document.select("html").attr("lang"); //$NON-NLS-1$ //$NON-NLS-2$
//
//        List<LatestSecurityPrice> prices = new ArrayList<>();
//
//        // first: find tables
//        Elements tables = document.getElementsByTag("table"); //$NON-NLS-1$
//        for (Element table : tables)
//        {
//            List<Spec> specs = new ArrayList<>();
//
//            HeaderInfo headerInfo = buildSpecFromTable(table, specs);
//            int rowIndex = headerInfo.rowIndex;
//
//            if (isSpecValid(specs))
//            {
//                Elements rows = table.select("> tbody > tr"); //$NON-NLS-1$
//
//                int size = rows.size();
//                if (size != 0)
//                {
//                    for (; rowIndex < size; rowIndex++)
//                    {
//                        Element row = rows.get(rowIndex);
//
//                        try
//                        {
//                            LatestSecurityPrice price = extractPrice(row, specs, language,
//                                            headerInfo.numberOfHeaderColumns);
//                            if (price != null)
//                                prices.add(price);
//                        }
//                        catch (Exception e)
//                        {
//                            data.addError(new IOException(url + '\n' + e.getMessage(), e));
//                        }
//                    }
//
//                    // skip all other tables
//                    break;
//                }
//            }
//        }
//
//        // if no quotes could be extract, log HTML for further analysis
//        if (prices.isEmpty())
//            data.addError(new IOException(MessageFormat.format(Messages.MsgNoQuotesFoundInHTML, url,
//                            Jsoup.clean(document.html(), Safelist.relaxed()))));
//
//        return prices;
//    }
//
//    => name.abuchen.portfolio.online.impl/HTMLTableParser.java
//    @SuppressWarnings("nls")
//    private HeaderInfo buildSpecFromTable(Element table, List<Spec> specs)
//    {
//        // check if thead exists
//        Elements header = table.select("> thead > tr > th");
//        if (!header.isEmpty())
//        {
//            buildSpecFromRow(header, specs);
//            if (!specs.isEmpty())
//                return new HeaderInfo(0, header.size());
//        }
//
//        header = table.select("> thead > tr > td");
//        if (!header.isEmpty())
//        {
//            buildSpecFromRow(header, specs);
//            if (!specs.isEmpty())
//                return new HeaderInfo(0, header.size());
//        }
//
//        // check if th exist in body
//        header = table.select("> tbody > tr > th");
//        if (!header.isEmpty())
//        {
//            buildSpecFromRow(header, specs);
//            if (!specs.isEmpty())
//                return new HeaderInfo(0, header.size());
//        }
//
//        // then check first two regular rows
//        int rowIndex = 0;
//
//        Elements rows = table.select("> tbody > tr");
//        Elements headerRow = null;
//
//        if (!rows.isEmpty())
//        {
//            Element firstRow = rows.get(0);
//            headerRow = firstRow.select("> td");
//            buildSpecFromRow(headerRow, specs);
//            rowIndex++;
//        }
//
//        if (specs.isEmpty() && rows.size() > 1)
//        {
//            Element secondRow = rows.get(1);
//            headerRow = secondRow.select("> td");
//            buildSpecFromRow(headerRow, specs);
//            rowIndex++;
//        }
//
//        return new HeaderInfo(rowIndex, headerRow != null ? headerRow.size() : 0);
//    }
//
//    => name.abuchen.portfolio.online.impl/HTMLTableParser.java
//    protected Column[] getColumns()
//    {
//        return COLUMNS;
//    }
//
//    => name.abuchen.portfolio.online.impl/HTMLTableParser.java
//    private void buildSpecFromRow(Elements row, List<Spec> specs)
//    {
//        Set<Column> available = new HashSet<>();
//        Collections.addAll(available, getColumns());
//
//        for (int ii = 0; ii < row.size(); ii++)
//        {
//            Element element = row.get(ii);
//
//            for (Column column : available)
//            {
//                if (column.matches(element))
//                {
//                    specs.add(new Spec(column, ii));
//                    available.remove(column);
//                    break;
//                }
//            }
//        }
//    }
//
//    => name.abuchen.portfolio.online.impl/HTMLTableQuoteParser.java
//    private boolean isSpecValid(List<Spec> specs)
//    {
//        if (specs == null || specs.isEmpty())
//            return false;
//
//        boolean hasDate = false;
//        boolean hasClose = false;
//
//        for (Spec spec : specs)
//        {
//            hasDate = hasDate || spec.column instanceof DateColumn;
//            hasClose = hasClose || spec.column instanceof CloseColumn;
//        }
//
//        return hasDate && hasClose;
//    }
//
//    => name.abuchen.portfolio.online.impl/HTMLTableParser.java:extractData
//    private LatestSecurityPrice extractPrice(Element row, List<Spec> specs, String languageHint,
//                    int numberOfHeaderColumns) throws ParseException
//    {
//        Elements cells = row.select("> td"); //$NON-NLS-1$
//
//        // we're only looking at rows having the same size as the header row
//        if (cells.size() != numberOfHeaderColumns)
//            return null;
//
//        LatestSecurityPrice price = new LatestSecurityPrice();
//
//        for (Spec spec : specs)
//            spec.column.setValue(cells.get(spec.index), price, languageHint);
//
//        return price;
//    }
}
