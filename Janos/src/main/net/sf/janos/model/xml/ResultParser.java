/*
 * Created on 17/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.sf.janos.control.SonosController;
import net.sf.janos.model.Entry;
import net.sf.janos.model.ZoneGroupState;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parses the String result from a zone player into a more useable type.
 * @author David Wheeler
 *
 */
public class ResultParser {

  /**
   * @param xml
   * @return a list of Entrys from the given xml string.
   * @throws IOException
   * @throws SAXException
   */
  public static List<Entry> getEntriesFromStringResult(String xml) throws IOException, SAXException {
    // TODO this is very slow - is there a faster way?
    XMLReader reader = XMLReaderFactory.createXMLReader();
    EntryHandler handler = new EntryHandler();
    reader.setContentHandler(handler);
    reader.parse(new InputSource(new StringReader(xml)));
    return handler.getArtists();
  }

  /**
   * @param controller
   * @param xml
   * @return zone group state from the given xml
   * @throws IOException
   * @throws SAXException
   */
  public static ZoneGroupState getGroupStateFromResult(SonosController controller, String xml) throws IOException, SAXException {
    XMLReader reader = XMLReaderFactory.createXMLReader();
    ZoneGroupStateHandler handler = new ZoneGroupStateHandler(controller);
    reader.setContentHandler(handler);
    reader.parse(new InputSource(new StringReader(xml)));

    return new ZoneGroupState(handler.getGroups());
    
  }
}
