package name.abuchen.portfolio.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.ClientFactory;
import name.abuchen.portfolio.model.PeerList;

public class UmarshallingPeerListTest
{
    @Test
    public void testMigrationOfClassificationKeys() throws IOException
    {
        Client client = ClientFactory.load(UmarshallingPeerListTest.class
                        .getResourceAsStream("UmarshallingPeerList.xml")); //$NON-NLS-1$

        PeerList peers = client.getPeers();

        assertThat(peers, instanceOf(PeerList.class));
        assertThat(peers.size(), is(3));

        assertThat(peers.findPeer("John Doe", true).toString(), is(Arrays.asList("Markt", "Region", "Land"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertThat(peers.findPeer("Heidrun", false).toString(), is(Arrays.asList("Markt", "Region", "Land"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
