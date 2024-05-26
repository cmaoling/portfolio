package name.abuchen.portfolio.datatransfer.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.Extractor.Item;
import name.abuchen.portfolio.datatransfer.SecurityCache;
import name.abuchen.portfolio.model.Client;

public class PDFImportAssistant
{
    private final Client client;
    private final List<File> files;
    private final List<Extractor> extractors = new ArrayList<>();

    public PDFImportAssistant(Client client, List<File> files)
    {
        this.client = client;
        this.files = files;

     // CMAOLING: extractors.add(new AJBellSecuritiesLimitedPDFExtractor(client));
     // CMAOLING: extractors.add(new AvivaPLCPDFExtractor(client));
     // CMAOLING: extractors.add(new BaaderBankPDFExtractor(client));
     // CMAOLING: extractors.add(new BankSLMPDFExtractor(client));
     // CMAOLING: extractors.add(new BisonPDFExtractor(client));
     // CMAOLING: extractors.add(new BondoraCapitalPDFExtractor(client));
     // CMAOLING: extractors.add(new ComdirectPDFExtractor(client));
     // CMAOLING: extractors.add(new CommerzbankPDFExtractor(client));
     // CMAOLING: extractors.add(new CommSecPDFExtractor(client));
        extractors.add(new ConsorsbankPDFExtractor(client));
     // CMAOLING: extractors.add(new CreditSuisseAGPDFExtractor(client));
     // CMAOLING: extractors.add(new CrowdestorPDFExtractor(client));
     // CMAOLING: extractors.add(new DABPDFExtractor(client));
     // CMAOLING: extractors.add(new DADATBankenhausPDFExtractor(client));
     // CMAOLING: extractors.add(new DegiroPDFExtractor(client));
     // CMAOLING: extractors.add(new DekaBankPDFExtractor(client));
     // CMAOLING: extractors.add(new DeutscheBankPDFExtractor(client));
     // CMAOLING: extractors.add(new Direkt1822BankPDFExtractor(client));
     // CMAOLING: extractors.add(new DkbPDFExtractor(client));
     // CMAOLING: extractors.add(new DreiBankenEDVPDFExtractor(client));
     // CMAOLING: extractors.add(new DZBankGruppePDFExtractor(client));
     // CMAOLING: extractors.add(new EasyBankAGPDFExtractor(client));
     // CMAOLING: extractors.add(new EbasePDFExtractor(client));
     // CMAOLING: extractors.add(new ErsteBankPDFExtractor(client));
     // CMAOLING: extractors.add(new FidelityInternationalPDFExtractor(client));
     // CMAOLING: extractors.add(new FILFondbankPDFExtractor(client));
     // CMAOLING: extractors.add(new FindependentAGPDFExtractor(client));
     // CMAOLING: extractors.add(new FinTechGroupBankPDFExtractor(client));
     // CMAOLING: extractors.add(new GenoBrokerPDFExtractor(client));
     // CMAOLING: extractors.add(new HargreavesLansdownPlcExtractor(client));
     // CMAOLING: extractors.add(new HelloBankPDFExtractor(client));
        extractors.add(new INGDiBaPDFExtractor(client));
     // CMAOLING: extractors.add(new JustTradePDFExtractor(client));
     // CMAOLING: extractors.add(new KBCGroupNVPDFExtractor(client));
     // CMAOLING: extractors.add(new KeytradeBankPDFExtractor(client));
     // CMAOLING: extractors.add(new MLPBankingAGPDFExtractor(client));
     // CMAOLING: extractors.add(new NIBCBankPDFExtractor(client));
     // CMAOLING: extractors.add(new OldenburgischeLandesbankAGPDFExtractor(client));
     // CMAOLING: extractors.add(new LGTBankPDFExtractor(client));
     // CMAOLING: extractors.add(new LiechtensteinischeLandesbankAGPDFExtractor(client));
     // CMAOLING: extractors.add(new LimeTradingCorpPDFExtractor(client));
     // CMAOLING: extractors.add(new MerkurPrivatBankPDFExtractor(client));
     // CMAOLING: extractors.add(new OnvistaPDFExtractor(client));
     // CMAOLING: extractors.add(new OpenBankSAPDFExtractor(client));
     // CMAOLING: extractors.add(new PictetCieGruppeSAPDFExtractor(client));
     // CMAOLING: extractors.add(new PostbankPDFExtractor(client));
     // CMAOLING: extractors.add(new PostfinancePDFExtractor(client));
     // CMAOLING: extractors.add(new QuirinBankAGPDFExtractor(client));
     // CMAOLING: extractors.add(new RaiffeisenBankgruppePDFExtractor(client));
     // CMAOLING: extractors.add(new RenaultBankDirektPDFExtractor(client));
     // CMAOLING: extractors.add(new RevolutLtdPDFExtractor(client));
     // CMAOLING: extractors.add(new SantanderConsumerBankPDFExtractor(client));
     // CMAOLING: extractors.add(new SberbankEuropeAGPDFExtractor(client));
     // CMAOLING: extractors.add(new SBrokerPDFExtractor(client));
     // CMAOLING: extractors.add(new ScorePriorityIncPDFExtractor(client));
     // CMAOLING: extractors.add(new SelfWealthPDFExtractor(client));
     // CMAOLING: extractors.add(new SimpelPDFExtractor(client));
     // CMAOLING: extractors.add(new SolarisbankAGPDFExtractor(client));
     // CMAOLING: extractors.add(new StakePDFExtractor(client));
     // CMAOLING: extractors.add(new SuresseDirektBankPDFExtractor(client));
     // CMAOLING: extractors.add(new SwissquotePDFExtractor(client));
     // CMAOLING: extractors.add(new TargobankPDFExtractor(client));
     // CMAOLING: extractors.add(new TigerBrokersPteLtdPDFExtractor(client));
     // CMAOLING: extractors.add(new TradeRepublicPDFExtractor(client));
     // CMAOLING: extractors.add(new UBSAGBankingAGPDFExtractor(client));
     // CMAOLING: extractors.add(new UnicreditPDFExtractor(client));
     // CMAOLING: extractors.add(new VanguardGroupEuropePDFExtractor(client));
     // CMAOLING: extractors.add(new VBankAGPDFExtractor(client));
     // CMAOLING: extractors.add(new WealthsimpleInvestmentsIncPDFExtractor(client));
     // CMAOLING: extractors.add(new WirBankPDFExtractor(client));
     // CMAOLING: extractors.add(new WeberbankPDFExtractor(client));
    }

    public Map<Extractor, List<Item>> run(IProgressMonitor monitor, Map<File, List<Exception>> errors)
    {
        monitor.beginTask(Messages.PDFMsgExtracingFiles, files.size());

        List<PDFInputFile> inputFiles = files.stream().map(PDFInputFile::new).collect(Collectors.toList());

        Map<Extractor, List<Item>> itemsByExtractor = new HashMap<>();

        SecurityCache securityCache = new SecurityCache(client);

        for (PDFInputFile inputFile : inputFiles)
        {
            monitor.setTaskName(inputFile.getName());

            try
            {
                inputFile.convertPDFtoText();

                boolean extracted = false;

                List<Exception> warnings = new ArrayList<>();
                for (Extractor extractor : extractors)
                {
                    List<Item> items = extractor.extract(securityCache, inputFile, warnings);

                    if (!items.isEmpty())
                    {
                        extracted = true;
                        itemsByExtractor.computeIfAbsent(extractor, e -> new ArrayList<Item>()).addAll(items);
                        break;
                    }
                }

                if (!extracted)
                {
                    Predicate<? super Exception> isNotUnsupportedOperation = e -> !(e instanceof UnsupportedOperationException);
                    List<Exception> meaningfulExceptions = warnings.stream().filter(isNotUnsupportedOperation)
                                    .collect(Collectors.toList());

                    errors.put(inputFile.getFile(), meaningfulExceptions.isEmpty() ? warnings : meaningfulExceptions);
                }
            }
            catch (IOException e)
            {
                errors.computeIfAbsent(inputFile.getFile(), f -> new ArrayList<>()).add(e);
            }

            monitor.worked(1);
        }

        // post processing
        itemsByExtractor.entrySet().stream() //
                        .collect(Collectors.toMap(Entry<Extractor, List<Item>>::getKey,
                                        e -> e.getKey().postProcessing(e.getValue())));

        securityCache.addMissingSecurityItems(itemsByExtractor);

        return itemsByExtractor;
    }

    public List<Item> runWithPlainText(File file, List<Exception> errors) throws FileNotFoundException
    {
        String extractedText = null;
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name()))
        {
            extractedText = scanner.useDelimiter("\\A").next(); //$NON-NLS-1$
        }
        PDFInputFile inputFile = new PDFInputFile(file, extractedText);

        return runWithInputFile(inputFile, errors);
    }

    public List<Item> runWithInputFile(PDFInputFile file, List<Exception> errors) throws FileNotFoundException
    {
        SecurityCache securityCache = new SecurityCache(client);

        List<Item> items = null;
        for (Extractor extractor : extractors)
        {
            List<Exception> warnings = new ArrayList<>();
            items = extractor.extract(securityCache, file, warnings);

            if (!items.isEmpty())
            {
                // we extracted items; remove all errors from all other
                // extractors that
                // did not find any transactions in this text
                errors.clear();
                errors.addAll(warnings);

                items = extractor.postProcessing(items);

                break;
            }

            errors.addAll(warnings);
        }

        if (items == null || items.isEmpty())
            return Collections.emptyList();

        Map<Extractor, List<Item>> itemsByExtractor = new HashMap<>();
        itemsByExtractor.put(extractors.get(0), items);
        securityCache.addMissingSecurityItems(itemsByExtractor);

        return items;
    }

}
