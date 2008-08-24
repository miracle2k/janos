/*
 * Copyright 2008 David Wheeler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.janos.model;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.janos.control.ZonePlayer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TrackMetaData {
  private static final Log LOG = LogFactory.getLog(TrackMetaData.class);
  private final String id;
  private final String parentId;
  private final String resource;
  private final String streamContent;
  private final String albumArtUri;
  private final String title;
  private final String upnpClass;
  private final String creator;
  private final String album;
  private final String albumArtist;
  
  public TrackMetaData(String id, String parentId, String res, String streamContent, 
      String albumArtUri, String title, String upnpClass, String creator, String album, 
      String albumArtist) {
    this.id = id;
    this.parentId = parentId;
    this.resource = res;
    this.streamContent = streamContent;
    this.albumArtUri = albumArtUri;
    this.title = title;
    this.upnpClass = upnpClass;
    this.creator = creator;
    this.album = album;
    this.albumArtist = albumArtist;
  }

  public String getAlbum() {
    return album;
  }

  public String getAlbumArtist() {
    return albumArtist;
  }

  public String getAlbumArtUri() {
    return albumArtUri;
  }

  public String getCreator() {
    return creator;
  }

  public String getResource() {
    return resource;
  }

  public String getStreamContent() {
    return streamContent;
  }

  public String getTitle() {
    return title;
  }

  public String getUpnpClass() {
    return upnpClass;
  }

  public String getId() {
    return id;
  }

  public String getParentId() {
    return parentId;
  }

  public URL getAlbumArtUrl(ZonePlayer zp) throws MalformedURLException {
    String uri = getAlbumArtUri();
    if (uri.startsWith("/getAA")) {
      // need to use mpath. what does this mean??
      LOG.info("uri = " + uri);
    } 
    
    return new URL("http", zp.getIP().getHostAddress(), zp.getPort(), uri);
  }

}
