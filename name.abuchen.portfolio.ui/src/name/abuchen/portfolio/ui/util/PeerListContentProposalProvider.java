package name.abuchen.portfolio.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import name.abuchen.portfolio.model.Peer;
import name.abuchen.portfolio.model.PeerList;

// this class was inspired by http://javawiki.sowas.com/doku.php?id=swt-jface:autocompletefield

public abstract class PeerListContentProposalProvider implements IContentProposalProvider
{
    private PeerList peerList;
    //private PeerListContentProposal contentProposal;

    public class IbanProposal extends PeerListContentProposal
    {
        public IbanProposal(Peer peer)
        {
            super(peer);
        }
        
        @Override
        public String getContent()
        {
            // TODO: still needed for debug? System.err.println(">>>> PeerListContentProvider::IbanProposal::getContent "  + peer.toString());
            return (peer.getIban()!=null?peer.getIban():""); //$NON-NLS-1$
        }

        @Override
        public int getCursorPosition()
        {
            // TODO: still needed for debug? System.err.println(">>>> PeerListContentProposal::IbanProposal::getCursorPosition "  + peer.toString());
            if (peer.getIban() != null)
                return peer.getIban().length();
            else
                return 0;
        }          
    }

    public class PartnerProposal extends PeerListContentProposal
    {
        public PartnerProposal(Peer peer)
        {
            super(peer);
        }

        @Override
        public String getContent()
        {
            // TODO: still needed for debug? System.err.println(">>>> PeerListContentProvider::PartnerProposal::getContent "  + peer.toString());
            return peer.getName();
        }

        @Override
        public int getCursorPosition()
        {
            // TODO: still needed for debug? System.err.println(">>>> PeerListContentProposal::PartnerProposal::getCursorPosition "  + peer.toString());
            if (peer.getName() != null)
                return peer.getName().length();
           else
               return 0;
        }          
    }

    public PeerListContentProposalProvider(PeerList peerList)
    {
        super();
        // TODO: still needed for debug? System.err.println(">>>> PeerListContentProvider::PeerContentProvider "  + Arrays.toString(peerList.toArray()));
        this.peerList = peerList;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position)
    {
        // TODO: still needed for debug? System.err.println(">>>> PeerListContentProvider::getProposals "  + contents);
        List<IContentProposal> proposals = new ArrayList<IContentProposal>();
        PeerList l = peerList.findPeer(contents, false);
        if (l != null)
            for (Peer p : l)
                proposals.add(makeContentProposal(p));
        return (IContentProposal[]) proposals.toArray(new IContentProposal[proposals.size()]);
    }

    protected IContentProposal makeContentProposal(Peer peer)
    {
        // TODO: still needed for debug? System.err.println(">>>> PeerListContentProvider::makeContentProposal(peer) peer: " + peer.toString());
        return (IContentProposal) new PeerListContentProposal(peer);
    }

}