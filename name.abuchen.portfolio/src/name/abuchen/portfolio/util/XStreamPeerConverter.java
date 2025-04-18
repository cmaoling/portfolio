package name.abuchen.portfolio.util;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import name.abuchen.portfolio.model.Peer;

public class XStreamPeerConverter implements Converter
{

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class object)
    {
        System.err.println(">>>> PeerConverter::Xstream() canConvert name   : " + object.getName() + " vs. " + Peer.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        if (Peer.class.equals(object))
            System.err.println(">>>> PeerConverter::Xstream() canConvert name TRUE  : " + object.getName() + " vs. " + Peer.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        else
            System.err.println(">>>> PeerConverter::Xstream() canConvert name FALSE : " + object.getName() + " vs. " + Peer.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        return object.equals(Peer.class);
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context)
    {
//        PeerList peerList = (PeerList) value;
//        if (peerList != null && !peerList.isEmpty())
//        {
//            for (Peer peer : peerList)
//            {
//                writer.startNode("peer");  //$NON-NLS-1$
//                writer.addAttribute("name", peer.getName()); //$NON-NLS-1$
//                writer.addAttribute("IBAN", peer.getIban()); //$NON-NLS-1$
//                writer.endNode();
//            }
//        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        System.err.println(">>>> PeerConverter::Xstream() reader   : " + reader.toString()); //$NON-NLS-1$
        Peer peer = new Peer();
        String attribute = reader.getAttribute("name"); //$NON-NLS-1$
        if (attribute != null)
        {
            peer.setName(attribute);
            System.err.println(">>>> PeerConverter:::Xstream() unmarshal name   : " + attribute); //$NON-NLS-1$
        }
        attribute = reader.getAttribute("IBAN"); //$NON-NLS-1$
        if (attribute != null)
        {
            peer.setIban(attribute);
            System.err.println(">>>> PeerConverter:::Xstream() unmarshal IBAN   : " + attribute); //$NON-NLS-1$
        }
        while (reader.hasMoreChildren())
        {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            switch (nodeName)
            {
                case "name": //$NON-NLS-1$
                    peer.setName(reader.getValue());
                    break;
                case "note"://$NON-NLS-1$
                    peer.setNote(reader.getValue());
                    break;
                case "IBAN": //$NON-NLS-1$
                    peer.setIban(reader.getValue());
                    break;
                case "account": //$NON-NLS-1$
                    System.err.println(">>>> PeerConverter:::Xstream() unmarshal account   : <" + reader.getValue() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
                    break;
                default:
                    System.err.println(">>>> PeerConverter:::Xstream() unmarshal default (" + nodeName + ") " + reader.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            

            reader.moveUp();
        }
        System.err.println(">>>> PeerConverter:::Xstream() peers   : " + peer.toString()); //$NON-NLS-1$
        return peer;
    }
}
