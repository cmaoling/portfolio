package name.abuchen.portfolio.online.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Strings;

import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.SecurityEvent;
import name.abuchen.portfolio.money.Monetary;
import name.abuchen.portfolio.online.DividendFeed;
import name.abuchen.portfolio.util.WebAccess;

public class DivvyDiaryDividendFeed implements DividendFeed
{
    private String apiKey;

    private PageCache<String> cache = new PageCache<>();

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SecurityEvent> getDividendPayments(Security security) throws IOException
    {
        if (apiKey == null)
            return Collections.emptyList();

        if (Strings.isNullOrEmpty(security.getIsin()))
            return Collections.emptyList();

        String json = cache.lookup(security.getIsin());
        if (json == null)
        {
            json = new WebAccess("api.divvydiary.com", "/symbols/" + security.getIsin()) //$NON-NLS-1$ //$NON-NLS-2$
                            .addHeader("X-API-Key", apiKey) //$NON-NLS-1$
                            .addUserAgent("PortfolioPerformance/" //$NON-NLS-1$
                                            + FrameworkUtil.getBundle(PortfolioReportNet.class).getVersion().toString())
                            .get();
            cache.put(security.getIsin(), json);
        }

        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);

        JSONArray dividends = (JSONArray) jsonObject.get("dividends"); //$NON-NLS-1$

        List<SecurityEvent> answer = new ArrayList<>();

        dividends.forEach(entry -> {
            JSONObject row = (JSONObject) entry;

            SecurityEvent payment = new SecurityEvent(YahooHelper.fromISODate((String) row.get("exDate")), //$NON-NLS-1$
                            YahooHelper.fromISODate((String) row.get("payDate")), //$NON-NLS-1$
                            new Monetary().valueOf((String) row.get("currency"),  //$NON-NLS-1$
                                            new BigDecimal(((Number) row.get("amount")).toString())), //$NON-NLS-1$
                            "divvydiary.com"); //$NON-NLS-1$
            answer.add(payment);
        });

        return answer;
    }

}
