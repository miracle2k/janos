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
package net.sf.janos.model;

import java.io.IOException;
import java.net.URL;

import net.sf.janos.control.ZonePlayer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class representing an entry in a zone players music library. eg. a queue
 * entry, or a line in.
 * 
 * @author David Wheeler
 * 
 */
public class Entry {
  private static final Log LOG = LogFactory.getLog(Entry.class);

  
    private final String id;
    private final String title;
    private final String parentId;
    private final String upnpClass;
    private final String res;
    private String album;
    private String albumArtUri;
    private String creator;

    public Entry(String id, String title, String parentId, String album, String albumArtUri, 
        String creator, String upnpClass, String res) {
      this.id = id;
      this.title=title;
      this.parentId = parentId;
      this.album = album;
      this.albumArtUri = albumArtUri;
      this.creator = creator;
      this.upnpClass = upnpClass;
      this.res = res;
    }
    
    /**
     * @return the title of the entry.
     */
    @Override
    public String toString() {
      return title;
    }
    
    /**
     * @return the unique identifier of this entry.
     */
    public String getId() {
      return id;
    }
    
    /**
     * @return the title of the entry.
     */
    public String getTitle() {
      return title;
    }
    
    /**
     * @return the unique identifier of the parent of this entry.
     */
    public String getParentId() {
      return parentId;
    }

    /**
     * @return a URI of this entry.
     */
    public String getRes() {
      return res;
    }

    /**
     * @return the UPnP classname for this entry.
     */
    public String getUpnpClass() {
      return upnpClass;
    }

    /**
     * @return the name of the album.
     */
    public String getAlbum() {
      return album;
    }

    /**
     * @return the URI for the album art.
     */
    public String getAlbumArtUri() {
      return StringEscapeUtils.unescapeXml(albumArtUri);
    }
    
    /**
     * @param zp
     *          the zone player from which to retrieve the album art.
     * @return the URL containing the album art image.
     * @throws IOException
     */
    public URL getAlbumArtURL(ZonePlayer zp) throws IOException {
      String uri = getAlbumArtUri();
      if (uri.startsWith("/getAA")) {
        // need to use mpath. what does this mean??
        LOG.info("uri = " + uri);
      } 
      
      return new URL("http", zp.getIP().getHostAddress(), zp.getPort(), uri);
    }

    /**
     * @return the name of the artist who created the entry.
     */
    public String getCreator() {
      return creator;
    }
    
}
