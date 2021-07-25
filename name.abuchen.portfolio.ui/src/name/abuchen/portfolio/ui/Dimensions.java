package name.abuchen.portfolio.ui;

import org.eclipse.swt.SWT;
import org.eclipse.jface.preference.PreferenceStore;

public class Dimensions
{
    public static int AbsolutePerformanceColumnWidth               = Dimensions.AmountColumnWidth;
    public static int AbsolutePerformancePercentColumnWidth        = Dimensions.PercentageColumnWidth;
    public static int AccountColumnWidth                           = 200;
    public static int AccountDirection                             = SWT.UP;
    public static int AmountColumnWidth                            = 120;
    public static int AttributeLabelColumnWidth                    = 150;
    public static int AttributeNameColumnWidth                     = 250;
    public static int AttributeTypeColumnWidth                     = 150;
    public static int AutogenerateLogoColumnWidth                  = 125;
    public static int BalanceColumnWidth                           = AmountColumnWidth;
    public static int BookmarkColumnWidth                          = 250;
    public static int BookmarkURLColumnWidth                       = 500;
    public static int CapitalGainsColumnWidth                      = AmountColumnWidth;
    public static int CapitalGainsMovingAverageColumnWidth         = AmountColumnWidth;
    public static int CapitalGainsMovingAveragePercentColumnWidth  = Dimensions.PercentageColumnWidth;
    public static int CapitalGainsPercentColumnWidth               = Dimensions.PercentageColumnWidth;
    public static int CSVImportDataHeight                          = 800;
    public static int CSVImportDataWidth                           = 500;
    public static int CSVImportTypeNameWidth                       = 200;
    public static int CSVImportTypePatternWidth                    = 400;
    public static int CurrencyColumnWidth                          =  40;
    public static int CurrencyProviderColumnWidth                  = 160;
    public static int DateColumnAlignment                          = SWT.LEFT;
    public static int DateColumnWidth                              = 120;
    public static int DateDirection                                = SWT.UP;
    public static int DayCountColumnWidth                          = 120;
    public static int DeltaPercentColumnWidth                      =  60;
    public static int DeltaPercentIndicatorColumnWidth             =  90;
    public static int DeltaPercentRelativeColumnWidth              = 130;
    public static int DeltaSharesColumnWidth                       = 100;
    public static int DesiredAllocationColumnWidth                 =  70;
    public static int DetailsColumnWidth                           = 300;
    public static int DividendMovingAverageRateOfReturnColumnWidth = Dimensions.PercentageColumnWidth;
    public static int DividendPaymentColumnWidth                   =  80;
    public static int DividendPaymentCountColumnWidth              =  40;
    public static int DividendPeriodicityColumnWidth               = Dimensions.IntervalColumnWidth;
    public static int DividendRateOfReturnColumnWidth              = Dimensions.PercentageColumnWidth;
    public static int DividendSumColumnWidth                       = AmountColumnWidth;
    public static int DividendYieldColumnWidth                     = Dimensions.PercentageColumnWidth;
    public static int DividendYieldMovingAverageColumnWidth        = Dimensions.PercentageColumnWidth;
    public static int EntryValueColumnWidth                        = AmountColumnWidth;
    public static int ExchangeRateColumnWidth                      =  60;
    public static int ExitValueColumnWidth                         = AmountColumnWidth;
    public static int FeesColumnWidth                              =  80;
    public static int ForexCapitalGainsColumnWidth                 = AmountColumnWidth;
    public static int HoldingPeriodColumnWidth                     = DayCountColumnWidth;
    public static int IbanColumnWidth                              = 200;
    public static int InterestColumnWidth                          =  80;
    public static int IntervalColumnWidth                          = 120;
    public static int InvestmentPlanNameColumnWidth                = 300;
    public static int InvestmentPlanNameDirection                  = SWT.DOWN;
    public static int IRRColumnWidth                               = Dimensions.PercentageColumnWidth;
    public static int IsinColumnWidth                              = 120;
    public static int MarketValueColumnWidth                       = AmountColumnWidth;
    public static int MiniLogoWidth                                =  20;
    public static int NamedColumnWidth                             = 300;
    public static int NamedDirection                               = SWT.DOWN;
    public static int NetValueColumnWidth                          = AmountColumnWidth;
    public static int NumberColumnAlignment                        = SWT.RIGHT;
    public static int NumberColumnWidth                            =  80;
    public static int NoteColumnWidth                              = 400;
    public static int ParameterNameColumnWidth                     = 500;
    public static int ParameterTypeColumnWidth                     = 150;
    public static int ParameterValueColumnWidth                    = 400;
    public static int PeerColumnWidth                              = 250;
    public static int PeerDirection                                = SWT.DOWN;
    public static int PercentageColumnAlignment                    = SWT.RIGHT;
    public static int PercentageColumnWidth                        =  80;
    public static int PerformanceCalculationLabelColumnWidth       = 350;
    public static int PerShareColumnWidth                          =  75;
    public static int PortfolioColumnWidth                         = AccountColumnWidth;
    public static int PortfolioDirection                           = SWT.DOWN;
    public static int ProfitLossColumnWidth                        = AmountColumnWidth;
    public static int PurchasePriceColumnWidth                     = Dimensions.QuoteColumnWidth;
    public static int PurchasePriceMovingAverageColumnWidth        =  60;
    public static int PurchaseValueColumnWidth                     = AmountColumnWidth;
    public static int PurchaseValueMovingAverageColumnWidth        =  80;
    public static int QuoteColumnWidth                             = 100;
    public static int SashDefaultSize                              = 500;
    public static int SashSecuritiesPerformanceHeight              = 700;
    public static int SashSecurityListHeight                       = 800;
    public static int SecurityColumnWidth                          = 250;
    public static int SecurityDirection                            = SWT.DOWN;
    public static int ShareInPercentColumnWidth                    =  80;
    public static int SharesColumnWidth                            = 100;
    public static int SymbolColumnWidth                            =  80;
    public static int TaxesColumnWidth                             =  80;
    public static int TaxonomyLevelsColumnWidth                    = 400;
    public static int TransactionCountColumnWidth                  = 120;
    public static int TWRORColumnWidth                             = PercentageColumnWidth;
    public static int TypeColumnWidth                              = 190;
    public static int URLColumnAlignment                           = SWT.LEFT;
    public static int URLColumnWidth                               = 300;
    public static int VolumeOfSecurityDepositsColumnWidth          = AmountColumnWidth;
    public static int WeightColumnWidth                            =  70;
    public static int WknColumnWidth                               =  80;

    static public void initializePreferenceStore(PreferenceStore preferenceStore)
    {
        preferenceStore.setValue("filter-retired-portfolios", (boolean) true); //$NON-NLS-1$
        preferenceStore.setValue("filter-retired-accounts", (boolean) true); //$NON-NLS-1$
        preferenceStore.setValue("FilterDropDown-filterSettings", (int) 18); //$NON-NLS-1$
        preferenceStore.setValue("filter-accounts", (boolean) false); //$NON-NLS-1$
    }
}