package name.abuchen.portfolio.util;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import name.abuchen.portfolio.model.Peer;
import name.abuchen.portfolio.model.PeerList;

/**
 * Read (but not write) the internal Arrays$ArrayList collection. Starting with
 * Java 17, XStream does not have access to this class anymore (unless we
 * explicitly open the module). However, we anyway want to work with ArrayList
 * only, therefore we convert while reading.
 * 
 * <pre>
 * &lt;dimensions class="java.util.Arrays$ArrayList"&gt;
 * &lt;a class="string-array"&gt;
 *  &lt;string>category name&lt;/string&gt;
 * &lt;/a&gt;
 * &lt;/dimensions&gt;
 * </pre>
 */
public class XStreamPeerListConverter extends CollectionConverter
{
    public XStreamPeerListConverter(Mapper mapper)
    {
        super(mapper);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type)
    {
        System.err.println(">>>> PeerListConverter::Xstream() canConvert name   : " + type.getName() + " vs. " + PeerList.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        if (PeerList.class.equals(type))
            System.err.println(">>>> PeerListConverter::Xstream() canConvert name TRUE  : " + type.getName() + " vs. " + PeerList.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        else
            System.err.println(">>>> PeerListConverter::Xstream() canConvert name FALSE : " + type.getName() + " vs. " + PeerList.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        return PeerList.class.equals(type); // NOSONAR
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        PeerList peerList = new PeerList();
        System.err.println(">>>> PeerListConverter::Xstream() unmarshal" + reader.toString()); //$NON-NLS-1$
        System.err.println(">>>> PeerConverter:::Xstream() peers   : " + context.toString()); //$NON-NLS-1$
        while (reader.hasMoreChildren())
        {
            reader.moveDown();
            Peer peer = new Peer();
            String nodeName = reader.getNodeName();
            switch (nodeName)
            {
                case "peer": //$NON-NLS-1$
                    peer = (Peer) context.convertAnother(null, Peer.class);
                    if (peer != null)
                    {
                        System.err.println(">>>> PeerConverter:::Xstream() unmarshal peer (" + nodeName + ") PASS " + peer.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                        peerList.add(peer);
                    }
                    else
                    {
                        System.err.println(">>>> PeerConverter:::Xstream() unmarshal peer (" + nodeName + ") FAIL " + reader.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    break;
                default:
                    System.err.println(">>>> PeerConverter:::Xstream() unmarshal default (" + nodeName + ") " + reader.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            System.err.println(">>>> PeerListConverter::Xstream() unmarshal" + peer.toString()); //$NON-NLS-1$
            reader.moveUp();
        }
        return peerList;
    }

    @Override
    protected Object createCollection(@SuppressWarnings("rawtypes") Class type)
    {
        return new PeerList();
    }
}
