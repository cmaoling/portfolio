package name.abuchen.portfolio.datatransfer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import name.abuchen.portfolio.Messages;
import name.abuchen.portfolio.datatransfer.Extractor.Item;
import name.abuchen.portfolio.datatransfer.Extractor.PeerItem;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.Peer;
import name.abuchen.portfolio.util.Iban;

public class PeerCache
{
    private static final Peer DUPLICATE_PEER_MARKER = new Peer();

    private static final List<String> MESSAGES = Arrays.asList(Messages.MsgErrorDuplicateIBAN, Messages.MsgErrorDuplicateName);

    private final Client client;

    private final List<Map<String, Peer>> localMaps = new ArrayList<>();

    public PeerCache(Client client)
    {
        this.client = client;

        List<Peer> lp = ((List<Peer>) client.getPeers().addAccounts(client.getAccounts()));

        this.localMaps.add(lp.stream().filter(p -> p.getIban() != null && !p.getIban().isEmpty())
                        .collect(Collectors.toMap(Peer::getIban, p -> p, (l, r) -> DUPLICATE_PEER_MARKER)));

        this.localMaps.add(client.getPeers().addAccounts(client.getAccounts()).stream().filter(p -> p.getName() != null && !p.getName().isEmpty())
                        .collect(Collectors.toMap(Peer::getName, p -> p, (l, r) -> DUPLICATE_PEER_MARKER)));
    }

    public Peer lookup(String iban, String name,
                    Supplier<Peer> creationFunction)
    {
        if (Iban.isValid(name) && iban == null)
        {
            // parterName is a valid iban, so switch variables (enables a ParterName column to held IBANs as well)
            iban = name;
            name = null;
        }

        if (!Iban.isValid(iban))
            iban = Iban.IBANNUMBER_DUMMY;

        List<String> attributes = Arrays.asList(iban, name);
        // DEBUG System.err.println("PeerCache lookup: IBAN: " + iban + " valid? " + Iban.isValid(iban));  //$NON-NLS-1$  //$NON-NLS-2$

        if (Iban.isValid(iban))
            // first: check the identifying attributes (IBAN)
            for (int ii = 0; ii < 1; ii++)
            {
                String attribute = attributes.get(ii);

                Peer peer = localMaps.get(ii).get(attribute);
                if (peer == DUPLICATE_PEER_MARKER)
                    throw new IllegalArgumentException(MessageFormat.format(MESSAGES.get(ii), attribute));
                if (peer != null)
                    return peer;
            }

        // second: check the name. But: even if the name matches, we also must
        // check that the identifying attributes do not differ. Why? Investment
        // instruments could have the same name but different ISINs.
        Peer peer = lookupPeerByName(iban, name);
        // DEBUG System.err.println("PeerCache lookup: " + (peer != null?peer.toString():"<no peer>") + " - IBAN: " + (iban != null?iban:"<NULL>") + " Name: " + (name != null?name:"<NULL>")); //$NON-NLS-1$  //$NON-NLS-2$
        if (peer != null)
            return peer;

        peer = creationFunction.get();
        if (Iban.isValid(iban))
            peer.setIban(iban);
        else
            peer.setIban(Iban.IBANNUMBER_DUMMY);
        peer.setName(name);

        for (int ii = 0; ii < localMaps.size(); ii++)
        {
            String attribute = attributes.get(ii);
            if (attribute != null)
                localMaps.get(ii).put(attribute, peer);
        }

        // DEBUG System.err.println("PeerCache lookup: " + (peer != null?peer.toString()+"/"+peer:"<no peer>")); //$NON-NLS-1$  //$NON-NLS-2$
        return peer;
    }

    private Peer lookupPeerByName(String iban, String name)
    {
        Peer peer = localMaps.get(1).get(name);

        // Ignore upper/lower case in Peer Name
        if (peer == null || peer == DUPLICATE_PEER_MARKER)
        {
            for (String key : localMaps.get(1).keySet())
                if (key.toLowerCase().equals(name.toLowerCase()))
                    peer = localMaps.get(1).get(key);
        }

        // allow imports by duplicate name
        if (peer == null || peer == DUPLICATE_PEER_MARKER)
            return null;

        if (Iban.isValid(iban) && doNotMatchIfGiven(iban, peer.getIban()))
            return null;
        return peer;
    }

    private boolean doNotMatchIfGiven(String attribute1, String attribute2)
    {
        return attribute1 != null && attribute2 != null && !attribute1.equalsIgnoreCase(attribute2);
    }

    /**
    * Inserts {@link SecurityItem} which have been implicitly created by other
    * transactions.
    */
    public void addMissingPeerItems(Map<Extractor, List<Item>> extractor2items)
    {
        Set<Peer> available = new HashSet<>();
        available.addAll(client.getPeers().addAccounts(client.getAccounts()));

        extractor2items.values().stream().flatMap(List<Item>::stream).filter(i -> i instanceof PeerItem)
        .map(Item::getPeer).forEach(available::add);
        for (Entry<Extractor, List<Item>> entry : extractor2items.entrySet())
        {
            // copy list as we are potentially modifying it
            for (Item item : new ArrayList<>(entry.getValue()))
            {
                if (item instanceof PeerItem || item.getPeer() == null)
                    continue;

                if (!available.contains(item.getPeer()))
                {
                    entry.getValue().add(new PeerItem(item.getPeer()));
                    available.add(item.getPeer());
                }
            }
        }
    }
}
