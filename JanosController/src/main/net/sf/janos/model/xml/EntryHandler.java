/*
   Copyright 2007 David Wheeler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.sf.janos.model.xml;

import java.util.ArrayList;
import java.util.List;

import net.sf.janos.model.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class EntryHandler extends DefaultHandler {
  private static final Log LOG = LogFactory.getLog(EntryHandler.class);
  private enum Element {
    TITLE, 
    CLASS,
    ALBUM, 
    ALBUM_ART_URI,
    CREATOR,
    RES,
    TRACK_NUMBER
  }
  
  // Maintain a set of elements about which it is unuseful to complain about.
  // This list will be initialized on the first failure case
  private static List<String> ignore = null;
    
  private String id;
  private String parentId;
  private StringBuilder upnpClass = new StringBuilder();
  private StringBuilder res = new StringBuilder();
  private StringBuilder title = new StringBuilder();
  private StringBuilder album = new StringBuilder();
  private StringBuilder albumArtUri = new StringBuilder();
  private StringBuilder creator = new StringBuilder();
  private StringBuilder trackNumber = new StringBuilder();
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
    } else if (qName.equals("upnp:originalTrackNumber")) {
      element = Element.TRACK_NUMBER;
    } else {
      if (ignore == null) {
    	  ignore = new ArrayList<String>();
    	  ignore.add("DIDL-Lite");
      }
      
      if (!ignore.contains(localName)) {
    	  LOG.warn("did not recognise element named " + localName);
      }
      element = null;
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
      case TRACK_NUMBER:
        trackNumber.append(ch, start, length);
        break;
      // no default
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals("container") || qName.equals("item")) {
      element = null;
      
      int trackNumberVal = 0;
      try {
    	  trackNumberVal = Integer.parseInt(trackNumber.toString());
      } catch (Exception e) {
      }
      
      artists.add(new Entry(id, title.toString(), parentId, album.toString(), 
          albumArtUri.toString(), creator.toString(), upnpClass.toString(), res.toString(), trackNumberVal));
      title= new StringBuilder();
      upnpClass = new StringBuilder();
      res = new StringBuilder();
      album = new StringBuilder();
      albumArtUri = new StringBuilder();
      creator = new StringBuilder();
      trackNumber = new StringBuilder();
    }
  }
  
  public List<Entry> getArtists() {
    return artists;
  }
}