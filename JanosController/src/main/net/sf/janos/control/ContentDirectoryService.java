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
public class ContentDirectoryService extends AbstractService {
  
  public static final int DEFAULT_REQUEST_COUNT = 200;
  public static final String DEFAULT_SORT_CRITERIA = "";
  public static final String DEFAULT_FILTER_STRING = "dc:title,res,dc:creator,upnp:artist,upnp:album";
  public static final BrowseType DEFAULT_BROWSE_TYPE = BrowseType.BrowseDirectChildren;
  
  public static enum BrowseType {
    BrowseDirectChildren,
    BrowseMetadata;
  }

  private static final Log LOG = LogFactory.getLog(ContentDirectoryService.class);

  private final ServiceEventHandler serviceEventHandler = new ServiceEventHandler() {
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
  };
  
  protected ContentDirectoryService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_CONTENT_DIRECTORY);
    registerServiceEventing(serviceEventHandler);
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
    return getEntries(startAt, length, type, DEFAULT_BROWSE_TYPE);
  }
  
  /**
   * Retrieves a list of entries from the device.
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @param type the type of entries to be retrieved eg "A:ARTIST" or "Q:".
   * @param browseType the desired browse type 
   * @return a List of Entries of maximum size <code>length</code>, or null if the request fails.
   */
  public List<Entry> getEntries(int startAt, int length, String type, BrowseType browseType) {
    return getEntries(startAt, length, type, browseType, DEFAULT_FILTER_STRING, DEFAULT_SORT_CRITERIA);
  }
  
  /**
   * Retrieves a list of entries from the device.
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @param type the type of entries to be retrieved eg "A:ARTIST" or "Q:".
   * @param browseType the desired browse type 
   * @param filterString the comma-seperated list of fields to have returned
   * @param sortCriteria the sort criteria, or empty string to use default sort criteria
   * @return a List of Entries of maximum size <code>length</code>, or null if the request fails.
   */
  public List<Entry> getEntries(int startAt, int length, String type, BrowseType browseType, String filterString, String sortCriteria) {
    try {
      ActionResponse response = getEntriesImpl(startAt, length, type, browseType, filterString, sortCriteria);

      LOG.debug("response value types: " + response.getOutActionArgumentNames());
      LOG.info("Returned " + response.getOutActionArgumentValue("NumberReturned") + " of " + response.getOutActionArgumentValue("TotalMatches") + " results.");
      String result = response.getOutActionArgumentValue("Result");
      LOG.debug(result);
      return ResultParser.getEntriesFromStringResult(result);
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * @param type
   * @return the entry given by type, or null if it could not be retrieved
   */
  public Entry getSingleEntry(String type) {
    try {
      ActionResponse response = getEntriesImpl(0, 1, type, BrowseType.BrowseMetadata, DEFAULT_FILTER_STRING, DEFAULT_SORT_CRITERIA);
      LOG.debug("response value types: " + response.getOutActionArgumentNames());
      LOG.info("Returned " + response.getOutActionArgumentValue("NumberReturned") + " of " + response.getOutActionArgumentValue("TotalMatches") + " results.");
      String result = response.getOutActionArgumentValue("Result");
      LOG.debug(result);
      List<Entry> entries = ResultParser.getEntriesFromStringResult(result);
      if (entries.size() > 0)
        return entries.get(0);
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;

  }

  /**
   * Performs a getEntries request, returning the response
   * @param startAt the index of the first entry to be returned.
   * @param length the maximum number of entries to returned.
   * @param type the type of entries to be retrieved eg "A:ARTIST" or "Q:".
   * @param browseType either "BrowseMetadata" or "BrowseDirectChildren"
   * @param filter a filter on the returned results
   * @param sortCriteria how to sort the returned results
   * @return the ActionResponse retured from the Sonos unit
   * @throws IOException if a network error prevents communications
   * @throws UPNPResponseException if a UPnP error response is returned
   */
  protected ActionResponse getEntriesImpl(int startAt, int length, String type, BrowseType browseType, String filter, String sortCriteria) throws IOException, UPNPResponseException {
    ActionMessage browseAction = messageFactory.getMessage("Browse");
    browseAction.setInputParameter("ObjectID", type);
    browseAction.setInputParameter("BrowseFlag", String.valueOf(browseType));
    browseAction.setInputParameter("Filter", filter);
    browseAction.setInputParameter("StartingIndex", String.valueOf(startAt));
    browseAction.setInputParameter("RequestedCount", String.valueOf(length));
    browseAction.setInputParameter("SortCriteria", sortCriteria);
    ActionResponse response;
    response = browseAction.service();
    return response;
  }
  
  public String getSearchCapabilites() {
    ActionMessage browseAction = messageFactory.getMessage("GetSortCapabilities");
    ActionResponse response = null;
    try {
      response = browseAction.service();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return response.getOutActionArgumentValue("SortCaps");
    
  }
  
  /**
   * Retrieves all entries of the given type asyncronously, via the given callback
   * @param callback
   * @param type
   * @return A unique handle to this search, allowing cancellation
   */
  public BrowseHandle getAllEntriesAsync(final EntryCallback callback, final String type) {
    AsyncBrowser handle = new AsyncBrowser(type, callback);
    SonosController.getInstance().getWorkerExecutor().execute(handle);
    return handle;
  }
  
  @Override
  public void dispose() {
    super.dispose();
    unregisterServiceEventing(serviceEventHandler);
  }
  
  private final class AsyncBrowser implements Runnable, BrowseHandle {
    private final String type;

    private final EntryCallback callback;
    
    private boolean isCancelled = false;

    protected AsyncBrowser(String type, EntryCallback callback) {
      this.type = type;
      this.callback = callback;
    }

    public void run() {
      int startAt = 0;
      boolean completedSuccessfully = false;
      try {
        ActionResponse response = getEntriesImpl(startAt, DEFAULT_REQUEST_COUNT, type, DEFAULT_BROWSE_TYPE, DEFAULT_FILTER_STRING, DEFAULT_SORT_CRITERIA);
        int totalCount = Integer.parseInt(response.getOutActionArgumentValue("TotalMatches"));
        
        startAt = Integer.parseInt(response.getOutActionArgumentValue("NumberReturned"));
        callback.updateCount(this, totalCount);
        if (!isCancelled) {
          callback.addEntries(this, ResultParser.getEntriesFromStringResult(response.getOutActionArgumentValue("Result")));
        }
        while (!isCancelled && startAt < totalCount) {
          response = getEntriesImpl(startAt, DEFAULT_REQUEST_COUNT, type, DEFAULT_BROWSE_TYPE, DEFAULT_FILTER_STRING, DEFAULT_SORT_CRITERIA);
          startAt += Integer.parseInt(response.getOutActionArgumentValue("NumberReturned"));
          if (!isCancelled) {
            callback.addEntries(this, ResultParser.getEntriesFromStringResult(response.getOutActionArgumentValue("Result")));
          }
        }
        completedSuccessfully = !isCancelled;
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (UPNPResponseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        callback.retrievalComplete(this, completedSuccessfully);
      }
      
    }
    
    public void cancel() {
      isCancelled = true;
    }
  }

}
