package name.abuchen.portfolio.ui.util;

import org.eclipse.jface.fieldassist.IContentProposal;

import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.model.Peer;


// this class was inspired by http://javawiki.sowas.com/doku.php?id=swt-jface:autocompletefield

public class PeerListContentProposal implements IContentProposal
{
    public Peer peer;
    
    public PeerListContentProposal(final Peer peer)
    {
        this.peer = peer;
    }

    public Peer getPeer()
    {
        return peer;
    }

    @Override
    public String getContent()
    {
        return peer.getName() + " (" + peer.getIban() + ") - " + peer.getNote(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Override
    public String getDescription()
    {
       // Wenn hier was zurückgegeben wird, dann erscheint dieser Text in einem seperatem Fenster
       return null;  
    }

    @Override
    public String getLabel()
    {
        // Wenn die IBAN nicht existiert darf nur der Name zurückgegeben werden, sonst gibt es eine NULL exception
        return peer.getName() + (peer.getIban() != null && peer.getIban().length() > 0 ? " (" + peer.getIban() + ")" : Messages.LabelNothing); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int getCursorPosition()
    {
       return peer.getName().length();
    }          
}