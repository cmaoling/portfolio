package name.abuchen.portfolio.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.model.AttributeType.AmountPlainConverter;
import name.abuchen.portfolio.model.AttributeType.ImageConverter;
import name.abuchen.portfolio.model.AttributeType.PercentConverter;
import name.abuchen.portfolio.model.AttributeType.StringConverter;

public class ClientSettings
{
    private List<Bookmark> bookmarks;
    private List<AttributeType> attributeTypes;
    private Map<String, ConfigurationSet> configurationSets;
    private List<ClientAttribute> clientAttributes;

    public ClientSettings()
    {
        doPostLoadInitialization();
    }

    public void doPostLoadInitialization()
    {
        if (bookmarks == null)
        {
            this.bookmarks = new ArrayList<>();
            this.bookmarks.addAll(getDefaultBookmarks());
        }

        if (attributeTypes == null)
        {
            this.attributeTypes = new ArrayList<>();
            addDefaultAttributeTypes();
        }

        if (configurationSets == null)
            configurationSets = new HashMap<>();

        if (clientAttributes == null)
        {
            this.clientAttributes = new ArrayList<>();
        }
    }

    public static List<Bookmark> getDefaultBookmarks()
    {
        List<Bookmark> answer = new ArrayList<>();

        answer.add(new Bookmark("finance.yahoo.com", //$NON-NLS-1$
                        "https://finance.yahoo.com/quote/{tickerSymbol}")); //$NON-NLS-1$
        answer.add(new Bookmark("onvista.de", //$NON-NLS-1$
                        "https://www.onvista.de/suche.html?SEARCH_VALUE={isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("finanzen.net", //$NON-NLS-1$
                        "https://www.finanzen.net/suchergebnis.asp?frmAktiensucheTextfeld={isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("ariva.de", //$NON-NLS-1$
                        "https://www.ariva.de/{isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("justetf.com  (ETF)", //$NON-NLS-1$
                        "https://www.justetf.com/etf-profile.html?isin={isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("fondsweb.com", //$NON-NLS-1$
                        "https://www.fondsweb.com/{isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("morningstar.de", //$NON-NLS-1$
                        "https://www.morningstar.de/de/funds/SecuritySearchResults.aspx?type=ALL&search={isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("extraETF.com (ETF)", //$NON-NLS-1$
                        "https://extraetf.com/etf-profile/{isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("alleaktien.de (" + Messages.LabelSearchShare + ")", //$NON-NLS-1$ //$NON-NLS-2$
                        "https://www.alleaktien.de/quantitativ/{isin}/")); //$NON-NLS-1$
        answer.add(new Bookmark("comdirect.de (" + Messages.LabelSearchShare + ")", //$NON-NLS-1$ //$NON-NLS-2$
                        "https://www.comdirect.de/inf/aktien/{isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("comdirect.de (ETF)", //$NON-NLS-1$
                        "https://www.comdirect.de/inf/etfs/{isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("divvydiary.com", //$NON-NLS-1$
                        "https://divvydiary.com/symbols/{isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("trackingdifferences.com (ETF)", //$NON-NLS-1$
                        "https://www.trackingdifferences.com/ETF/ISIN/{isin}")); //$NON-NLS-1$
        answer.add(new Bookmark("tradingview.com", //$NON-NLS-1$
                        "https://www.tradingview.com/chart/?symbol=XETR:{tickerSymbolPrefix}")); //$NON-NLS-1$
        answer.add(new Bookmark("cnbc.com (" + Messages.LabelSearchShare + ")", //$NON-NLS-1$ //$NON-NLS-2$
                        "https://www.cnbc.com/quotes/{tickerSymbolPrefix}")); //$NON-NLS-1$
        answer.add(new Bookmark("nasdaq.com (" + Messages.LabelSearchShare + ")", //$NON-NLS-1$ //$NON-NLS-2$
                        "https://www.nasdaq.com/market-activity/stocks/{tickerSymbolPrefix}")); //$NON-NLS-1$

        return answer;
    }

    private void addDefaultAttributeTypes()
    {
        Function<Class<? extends Attributable>, AttributeType> factory = target -> {
            AttributeType logoType = new AttributeType("logo"); //$NON-NLS-1$
            logoType.setName(Messages.AttributesLogoName);
            logoType.setColumnLabel(Messages.AttributesLogoColumn);
            logoType.setTarget(target);
            logoType.setType(String.class);
            logoType.setConverter(ImageConverter.class);
            return logoType;
        };

        addAttributeType(factory.apply(Security.class));
        addAttributeType(factory.apply(Account.class));
        addAttributeType(factory.apply(Portfolio.class));
        addAttributeType(factory.apply(InvestmentPlan.class));

        AttributeType ter = new AttributeType("ter"); //$NON-NLS-1$
        ter.setName(Messages.AttributesTERName);
        ter.setColumnLabel(Messages.AttributesTERColumn);
        ter.setTarget(Security.class);
        ter.setSource("ter"); //$NON-NLS-1$
        ter.setType(Double.class);
        ter.setConverter(PercentConverter.class);
        addAttributeType(ter);

        AttributeType aum = new AttributeType("aum"); //$NON-NLS-1$
        aum.setName(Messages.AttributesAUMName);
        aum.setColumnLabel(Messages.AttributesAUMColumn);
        aum.setTarget(Security.class);
        aum.setType(Long.class);
        aum.setConverter(AmountPlainConverter.class);
        addAttributeType(aum);

        AttributeType vendor = new AttributeType("vendor"); //$NON-NLS-1$
        vendor.setName(Messages.AttributesVendorName);
        vendor.setColumnLabel(Messages.AttributesVendorColumn);
        vendor.setTarget(Security.class);
        vendor.setSource("vendor"); //$NON-NLS-1$
        vendor.setType(String.class);
        vendor.setConverter(StringConverter.class);
        addAttributeType(vendor);

        AttributeType fee = new AttributeType("acquisitionFee"); //$NON-NLS-1$
        fee.setName(Messages.AttributesAcquisitionFeeName);
        fee.setColumnLabel(Messages.AttributesAcquisitionFeeColumn);
        fee.setTarget(Security.class);
        fee.setType(Double.class);
        fee.setConverter(PercentConverter.class);
        addAttributeType(fee);

        AttributeType managementFee = new AttributeType("managementFee"); //$NON-NLS-1$
        managementFee.setName(Messages.AttributesManagementFeeName);
        managementFee.setColumnLabel(Messages.AttributesManagementFeeColumn);
        managementFee.setTarget(Security.class);
        managementFee.setType(Double.class);
        managementFee.setConverter(PercentConverter.class);
        addAttributeType(managementFee);
    }

    public List<Bookmark> getBookmarks()
    {
        return bookmarks;
    }

    public boolean removeBookmark(Bookmark bookmark)
    {
        return bookmarks.remove(bookmark);
    }

    public void insertBookmark(Bookmark before, Bookmark bookmark)
    {
        if (before == null)
            bookmarks.add(bookmark);
        else
            bookmarks.add(bookmarks.indexOf(before), bookmark);
    }

    public void insertBookmark(int index, Bookmark bookmark)
    {
        bookmarks.add(index, bookmark);
    }

    public void insertBookmarkAfter(Bookmark after, Bookmark bookmark)
    {
        if (after == null)
            bookmarks.add(bookmark);
        else
            bookmarks.add(bookmarks.indexOf(after) + 1, bookmark);
    }

    public void clearBookmarks()
    {
        bookmarks.clear();
    }

    public Stream<AttributeType> getAttributeTypes()
    {
        return attributeTypes.stream();
    }

    public void removeAttributeType(AttributeType type)
    {
        attributeTypes.remove(type);
    }

    public void addAttributeType(AttributeType type)
    {
        attributeTypes.add(type);
    }

    public void addAttributeType(int index, AttributeType type)
    {
        attributeTypes.add(index, type);
    }

    public int getAttributeTypeIndexOf(AttributeType type)
    {
        return attributeTypes.indexOf(type);
    }

    public boolean hasConfigurationSet(String key)
    {
        return configurationSets.containsKey(key);
    }

    public ConfigurationSet getConfigurationSet(String key)
    {
        return configurationSets.computeIfAbsent(key, k -> new ConfigurationSet());
    }

    public void addClientAttributes(ClientAttribute attribute)
    {
        boolean defined = false;
        for (ClientAttribute attr : clientAttributes)
        {
            if (attr.getId().equals(attribute.getId()))
                defined = true;
        }
        if (!defined)
            clientAttributes.add(attribute);
    }

    public List<ClientAttribute> getClientAttributes()
    {
        return clientAttributes;
    }

    public ClientAttribute getClientAttribute(String id)
    {
        return clientAttributes.stream()
                        .filter(attribute -> id.equals(attribute.getId()))
                        .findAny()
                        .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public Optional<AttributeType> getOptionalLogoAttributeType(Class<? extends Object> type)
    {
        return getAttributeTypes().filter(t -> t.getConverter() instanceof AttributeType.ImageConverter
                        && t.getName().equalsIgnoreCase("logo") //$NON-NLS-1$
                        && t.supports((Class<? extends Attributable>) type)).findFirst();
    }
}
