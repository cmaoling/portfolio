package name.abuchen.portfolio.ui.util;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Display;

import name.abuchen.portfolio.util.ColorConversion;

public final class Colors
{

    /**
     * Theme holds the colors that a themed via CSS. Because Eclipse 4.16
     * disposes (still) disposes colors upon theme change, we just inject the
     * RGB values and cache the colors here.
     */
    public static class Theme
    {
        private Color defaultForeground = Colors.BLACK;
        private Color defaultBackground = Colors.WHITE;
        private Color warningBackground = getColor(254, 223, 107); // FEDF6B
        private Color redBackground = Colors.GREEN;
        private Color greenBackground = Colors.RED;
        private Color redForeground = Colors.DARK_RED;
        private Color greenForeground = Colors.DARK_GREEN;
        private Color grayForeground = getColor(112, 112, 112); // 707070
        private Color hyperlink = Display.getDefault().getSystemColor(SWT.COLOR_LINK_FOREGROUND);

        public Color defaultForeground()
        {
            return defaultForeground;
        }

        public void setDefaultForeground(RGBA color)
        {
            this.defaultForeground = getColor(color.rgb);
        }

        public Color defaultBackground()
        {
            return defaultBackground;
        }

        public void setDefaultBackground(RGBA color)
        {
            this.defaultBackground = getColor(color.rgb);
        }

        public Color warningBackground()
        {
            return warningBackground;
        }

        public void setWarningBackground(RGBA color)
        {
            this.warningBackground = getColor(color.rgb);
        }

        public Color redBackground()
        {
            return redBackground;
        }

        public void setRedBackground(RGBA color)
        {
            this.redBackground = getColor(color.rgb);
        }

        public Color greenBackground()
        {
            return greenBackground;
        }

        public void setGreenBackground(RGBA color)
        {
            this.greenBackground = getColor(color.rgb);
        }

        public Color redForeground()
        {
            return redForeground;
        }

        public void setRedForeground(RGBA color)
        {
            this.redForeground = getColor(color.rgb);
        }

        public Color greenForeground()
        {
            return greenForeground;
        }

        public void setGreenForeground(RGBA color)
        {
            this.greenForeground = getColor(color.rgb);
        }

        public Color grayForeground()
        {
            return grayForeground;
        }

        public void setGrayForeground(RGBA color)
        {
            this.grayForeground = getColor(color.rgb);
        }

        public Color hyperlink()
        {
            return hyperlink;
        }

        public void setHyperlink(RGBA color)
        {
            this.hyperlink = getColor(color.rgb);
        }
    }

    private static final ColorRegistry REGISTRY = new ColorRegistry();

    public static final Color GRAY = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
    public static final Color WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    public static final Color DARK_GRAY = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
    public static final Color DARK_RED = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED); // CMAOLING: getColor(209, 84, 84); // D15454
    public static final Color DARK_GREEN = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
    public static final Color BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    public static final Color RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);
    public static final Color GREEN = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);

    public static final Color ICON_ORANGE = getColor(241, 143, 1); // F18F01
    public static final Color ICON_BLUE = getColor(14, 110, 142); // 0E6E8E
    public static final Color ICON_GREEN = getColor(154, 193, 85); // 9AC155

    public static final Color TOTALS = getColor(0, 0, 0);

    public static final Color CASH = getColor(196, 55, 194);
    public static final Color EQUITY = getColor(87, 87, 255);

    public static final Color CPI = getColor(120, 120, 120);
    public static final Color IRR = getColor(0, 0, 0);

    public static final Color DARK_BLUE = getColor(149, 165, 180); // 95A5B4
    public static final Color LIGHT_GRAY = getColor(240, 240, 240);

    public static final Color LIGHT_GREEN = getColor(188, 226, 158); // BCE29E
    public static final Color LIGHT_ORANGE = getColor(255, 152, 89); // FF9859
    public static final Color OLIVE_GREEN = getColor(163, 215, 113); // A3D771

    public static final Color HEADINGS = getColor(57, 62, 66); // 393E42
    public static final Color OTHER_CATEGORY = getColor(180, 180, 180);
    public static final Color INFO_TOOLTIP_BACKGROUND = getColor(236, 235, 236);

    public static final Color WARNING = getColor(252, 209, 29); // FCCD1D
    public static final Color WARNING_RED = getColor(209, 29, 29); // D11D1D

    public static final Color SIDEBAR_TEXT = getColor(57, 62, 66); // 393E42
    public static final Color SIDEBAR_BACKGROUND = getColor(249, 250, 250); // F9FAFA
    public static final Color SIDEBAR_BACKGROUND_SELECTED = getColor(228, 230, 233); // E4E6E9
    public static final Color SIDEBAR_BORDER = getColor(244, 245, 245); // F4F5F5

    public static final Color colorQuote = getColor(52, 70, 235);
    public static final Color colorEventPurchase = getColor(26, 173, 33);
    public static final Color colorEventSale = getColor(232, 51, 69);
    public static final Color colorEventDividendPaid = getColor(128, 0, 128);
    public static final Color colorEventDividendPromised  = getColor(180, 128, 180);
    public static final Color colorEventSplit  = getColor(26, 52, 150);
    public static final Color colorEventRight  = getColor(150, 220, 220);
    public static final Color colorEventOther  = getColor(140, 90, 200);
    public static final Color colorEventNote  = getColor(66, 66, 66);
    public static final Color colorHigh = getColor(0, 102, 0);
    public static final Color colorLow = getColor(128, 0, 0);
    public static final Color colorFifoPurchasePrice = getColor(226, 122, 121);
    public static final Color colorInvestmentPerShare = getColor(122, 226, 121);
    public static final Color colorMovingAveragePurchasePrice = getColor(150, 82, 81);
    public static final Color colorBollingerBands = getColor(201, 141, 68);
    public static final Color colorSMA1 = getColor(179, 107, 107); // #B36B6B
    public static final Color colorSMA2 = getColor(179, 167, 107); // #B3A76B
    public static final Color colorSMA3 = getColor(131, 179, 107); // #83B36B
    public static final Color colorSMA4 = getColor(107, 179, 143); // #6BB38F
    public static final Color colorSMA5 = getColor(107, 155, 179); // #6B9BB3
    public static final Color colorSMA6 = getColor(119, 107, 179); // #776BB3
    public static final Color colorSMA7 = getColor(179, 107, 179); // #B36BB3
    public static final Color colorEMA1 = getColor(200, 107, 107); // #C86B6B
    public static final Color colorEMA2 = getColor(200, 167, 107); // #C8A76B
    public static final Color colorEMA3 = getColor(131, 200, 107); // #83C86B
    public static final Color colorEMA4 = getColor(107, 200, 143); // #6BC88F
    public static final Color colorEMA5 = getColor(107, 155, 200); // #6B9BC8
    public static final Color colorEMA6 = getColor(119, 107, 200); // #776BC8
    public static final Color colorEMA7 = getColor(200, 107, 200); // #C86BB3
    public static final Color colorAreaPositive = getColor(90, 114, 226);
    public static final Color colorAreaNegative = getColor(226, 91, 90);
    public static final Color colorNonTradingDay = getColor(255, 137, 89);

    private static final Theme theme = new Theme();

    private Colors()
    {
    }

    public static Theme theme()
    {
        return theme;
    }

    public static Color getColor(RGB rgb)
    {
        return getColor(rgb.red, rgb.green, rgb.blue);
    }

    /**
     * Constructs a color instance with the given red, green and blue values.
     *
     * @param red
     *            the red component of the new instance
     * @param green
     *            the green component of the new instance
     * @param blue
     *            the blue component of the new instance
     * @exception IllegalArgumentException
     *                if the red, green or blue argument is not between 0 and
     *                255
     */
    public static Color getColor(int red, int green, int blue)
    {
        String key = getColorKey(red, green, blue);
        if (REGISTRY.hasValueFor(key))
        {
            return REGISTRY.get(key);
        }
        else
        {
            REGISTRY.put(key, new RGB(red, green, blue));
            return getColor(key);
        }
    }

    private static Color getColor(String key)
    {
        return REGISTRY.get(key);
    }

    private static String getColorKey(int red, int green, int blue)
    {
        return red + "_" + green + "_" + blue; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String toHex(Color color)
    {
        return ColorConversion.toHex(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String toHex(RGB rgb)
    {
        return ColorConversion.toHex(rgb.red, rgb.green, rgb.blue);
    }

    public static RGB toRGB(String hex)
    {
        return ColorConversion.hex2RGB(hex);
    }

    /**
     * Returns an appropriate text color (black or white) for the given
     * background color.
     */
    public static Color getTextColor(Color color)
    {
        // http://stackoverflow.com/questions/596216/formula-to-determine-brightness-of-rgb-color

        double luminance = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.4 ? BLACK : WHITE;
    }

    public static Color brighter(Color base)
    {
        return getColor(ColorConversion.brighter(base.getRGB()));
    }

    public static Color darker(Color base)
    {
        return getColor(ColorConversion.darker(base.getRGB()));
    }

    public static RGB interpolate(RGB first, RGB second, float factor)
    {
        int red = Math.round(first.red + factor * (second.red - first.red));
        int green = Math.round(first.green + factor * (second.green - first.green));
        int blue = Math.round(first.blue + factor * (second.blue - first.blue));

        return new RGB(red, green, blue);
    }
}
