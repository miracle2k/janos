/*
 * Created on 17/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model.xml;

import java.util.ArrayList;
import java.util.List;

import net.sf.janos.model.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class EntryHandler extends DefaultHandler {
  private enum Element {
    TITLE, 
    CLASS,
    ALBUM, 
    ALBUM_ART_URI,
    CREATOR,
    RES
  }
    
  private String id;
  private String parentId;
  private StringBuilder upnpClass = new StringBuilder();
  private StringBuilder res = new StringBuilder();
  private StringBuilder title = new StringBuilder();
  private StringBuilder album = new StringBuilder();
  private StringBuilder albumArtUri = new StringBuilder();
  private StringBuilder creator = new StringBuilder();
  private Element element = null;
  
  private List<Entry> artists = new ArrayList<Entry>();
  
  EntryHandler() {
    // shouldn't be used outside of this package.
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equals("container") || qName.equals("item")) {
      id = attributes.getValue("id");
      parentId = attributes.getValue("parentID");
    } else if (qName.equals("res")) {
      element = Element.RES;
    } else if (qName.equals("dc:title")) {
      element = Element.TITLE;
    } else if (qName.equals("upnp:class")) {
      element = Element.CLASS;
    } else if (qName.equals("dc:creator")) {
      element = Element.CREATOR;
    } else if (qName.equals("upnp:album")) {
      element = Element.ALBUM;
    } else if (qName.equals("upnp:albumArtURI")) {
      element = Element.ALBUM_ART_URI;
    }
  }
  
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (element == null) {
      return;
    }
    switch (element) {
      case TITLE: 
        title.append(ch, start, length);
        break;
      case CLASS:
        upnpClass.append(ch, start, length);
        break;
      case RES:
        res.append(ch, start, length);
        break;
      case ALBUM:
        album.append(ch, start, length);
        break;
      case ALBUM_ART_URI:
        albumArtUri.append(ch, start, length);
        break;
      case CREATOR:
        creator.append(ch, start, length);
        break;
      // no default
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals("container") || qName.equals("item")) {
      element = null;
      artists.add(new Entry(id, title.toString(), parentId, album.toString(), 
          albumArtUri.toString(), creator.toString(), upnpClass.toString(), res.toString()));
      title= new StringBuilder();
      upnpClass = new StringBuilder();
      res = new StringBuilder();
      album = new StringBuilder();
      albumArtUri = new StringBuilder();
      creator = new StringBuilder();
    }
  }
  
  public List<Entry> getArtists() {
    return artists;
  }
}