package name.abuchen.portfolio.ui.handlers;

import java.io.File;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Portfolio;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.editor.PortfolioPart;
import name.abuchen.portfolio.ui.wizards.datatransfer.CSVImportWizard;

public class ImportCSVHandler
{
    @CanExecute
    boolean isVisible(@Named(IServiceConstants.ACTIVE_PART) MPart part)
    {
        return MenuHelper.isClientPartActive(part);
    }

    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part,
                    @Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
    {
        MenuHelper.getActiveClient(part).ifPresent(client -> runImport((PortfolioPart) part.getObject(), shell, client, "", null, null)); //$NON-NLS-1$
    }
    
    public static void runImport(PortfolioPart part, Shell shell, Client client, String filterPath, Account account, Portfolio portfolio)
    {
        FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
        fileDialog.setFilterPath(filterPath);
        fileDialog.setFilterNames(new String[] { Messages.CSVImportLabelFileCSV, Messages.CSVImportLabelFileAll });
        fileDialog.setFilterExtensions(new String[] { "*.csv", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        String fileName = fileDialog.open();

        if (fileName == null)
            return;

        IPreferenceStore preferences = part.getPreferenceStore();
        CSVImportWizard wizard = new CSVImportWizard(client, preferences, new File(fileName));
        if (account != null)
            wizard.setTarget(account);
        if (portfolio != null)
            wizard.setTarget(portfolio);

        Dialog wizwardDialog = new WizardDialog(shell, wizard);
        wizwardDialog.open();
    }
}
