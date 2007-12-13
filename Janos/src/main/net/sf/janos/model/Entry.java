/*
 * Created on 17/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sf.janos.Debug;
import net.sf.janos.control.ZonePlayer;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

/**
 * A class representing an entry in a zone players music library. eg. a queue
 * entry, or a line in.
 * 
 * @author David Wheeler
 * 
 */
public class Entry {
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
     * @return ImageData array of length 1 containing the album art image.
     * @throws IOException
     */
    public ImageData[] getAlbumArt(ZonePlayer zp) throws IOException {
      InputStream artworkStream = null;
      String uri = getAlbumArtUri();
      if (uri.startsWith("/getAA")) {
        // need to use mpath. what does this mean??
        Debug.info("uri = " + uri);
      } 
      
      try {
        URL artworkUrl = new URL("http", zp.getIP().getHostAddress(), zp.getPort(), uri);
        artworkStream = artworkUrl.openStream();
        return new ImageLoader().load(artworkStream);
      } finally {
        if (artworkStream != null) {
          try {
            artworkStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    /**
     * @return the name of the artist who created the entry.
     */
    public String getCreator() {
      return creator;
    }
    
}
