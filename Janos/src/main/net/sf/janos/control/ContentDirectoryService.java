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
package net.sf.janos.control;

import java.io.IOException;
import java.util.List;

import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;
import net.sf.janos.model.Entry;
import net.sf.janos.model.xml.ResultParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * Allows the listing and searching of the audio content of a zone player.
 * 
 * @author David Wheeler
 *
 */
public class ContentDirectoryService extends AbstractService implements ServiceEventHandler {
  
  private static final Log LOG = LogFactory.getLog(ContentDirectoryService.class);

  
  protected ContentDirectoryService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_CONTENT_DIRECTORY);
    try {
      refreshServiceEventing(DEFAULT_EVENT_PERIOD, this);
      // TODO refresh periodically
    } catch (IOException e) {
      LOG.error("Could not register for events: ", e);
    }
  }

  /**
   * Retrieves a list of root level entries from the device.
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @return a List of Entries of maximum size <code>length</code>, or null if the request fails.
   */
  public List<Entry> getFolderEntries(int startAt, int length) {
    return getEntries(startAt, length, "A:");
  }
  
  /**
   * Retrieves a list of Artist entries from the device.
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @return a List of Entries of maximum size <code>length</code>, or null if the request fails.
   */
  public List<Entry> getArtists(int startAt, int length) {
    return getEntries(startAt, length, "A:ARTIST");
  }
  
  /**
   * Retrieves a list of Album entries from the device.
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @return a List of Entries of maximum size <code>length</code>, or null if the request fails.
   */
  public List<Entry> getAlbums(int startAt, int length) {
    return getEntries(startAt, length, "A:ALBUM");
  }
  
  /**
   * Retrieves a list of Track entries from the device.
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @return a List of Entries of maximum size <code>length</code>, or null if the request fails.
   */
  public List<Entry> getTracks(int startAt, int length) {
    return getEntries(startAt, length, "A:TRACKS");
  }
  
  /**
   * Retrieves a list of Track entries from the device, representing the current queue.
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @return a List of Entries of maximum size <code>length</code>, or null if the request fails.
   */
  public List<Entry> getQueue(int startAt, int length) {
    return getEntries(startAt, length, "Q:0");
  }
  
  /**
   * Retrieves a list of entries from the device.
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @param type the type of entries to be retrieved eg "A:ARTIST" or "Q:".
   * @return a List of Entries of maximum size <code>length</code>, or null if the request fails.
   */
  public List<Entry> getEntries(int startAt, int length, String type) {
    ActionMessage browseAction = messageFactory.getMessage("Browse");
    browseAction.setInputParameter("ObjectID", type);
    browseAction.setInputParameter("BrowseFlag", "BrowseDirectChildren");
    browseAction.setInputParameter("Filter", "dc:title,res,dc:creator,upnp:artist,upnp:album");
    browseAction.setInputParameter("StartingIndex", String.valueOf(startAt));
    browseAction.setInputParameter("RequestedCount", String.valueOf(length));
    browseAction.setInputParameter("SortCriteria", "");
    ActionResponse response;
    try {
      response = browseAction.service();
      LOG.debug("response value types: " + response.getOutActionArgumentNames());
      LOG.info("Returned " + response.getOutActionArgumentValue("NumberReturned") + " of " + response.getOutActionArgumentValue("TotalMatches") + " results.");
      String result = response.getOutActionArgumentValue("Result");
      LOG.debug(result);
      return ResultParser.getEntriesFromStringResult(result);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public void handleStateVariableEvent(String variable, String value) {
    LOG.info("ContentDirectory Event: " + variable + "=" + value);
    /*
     * Expected Event Variables:
     * SystemUpdateID
     * ContainerUpdateID
     * ShareListRefreshState [NOTRUN|RUNNING|DONE]
     * ShareIndexInProgress
     * ShareIndexLastError
     * UserRadioUpdateID
     * MasterRadioUpdateID
     * SavedQueuesUpdateID
     * ShareListUpdateID
     */
    // TODO implement
    
  }
}
