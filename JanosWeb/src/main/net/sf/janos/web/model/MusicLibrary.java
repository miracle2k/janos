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
package net.sf.janos.web.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.janos.control.BrowseHandle;
import net.sf.janos.control.EntryCallback;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MusicLibraryListener;
import net.sf.janos.model.MusicLibraryModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A table model for a list of entries. eg queue.
 * @author David Wheeler - Adapted for janos.web by Chris Christiansen
 *
 */
public class MusicLibrary implements MusicLibraryModel {

  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(MusicLibrary.class);

  protected final List<Entry> entries = new ArrayList<Entry>();
  
  private int reportedSize = -1;

  private List<MusicLibraryListener> listeners = new ArrayList<MusicLibraryListener>();

  private BrowseHandle browser;
  
  public MusicLibrary(ZonePlayer zone) {
    this(zone, null);
  }
  
  public MusicLibrary(ZonePlayer zone, Entry entry) {
    String id;
    if (entry != null) {
      id = entry.getId();
    } else {
      id = "A:";
    }
    browser = loadEntries(zone, id);
  }
  
  /**
   * Begins the process of loading the entries for this library. This method is
   * called from the constructor, and thus overriding implementers cannot assume
   * existance of even final class fields
   * 
   * @param zone
   * @param type
   * @return the handle for the search
   */
  protected synchronized BrowseHandle loadEntries(ZonePlayer zone, String type) {
	  BrowseHandle b = zone.getMediaServerDevice().getContentDirectoryService().getAllEntriesAsync(new MusicLibraryEntryCallback(), type);
	  notifyAll();
	  return b; 
  }
  
  /**
   * {@inheritDoc}
   */
  public void dispose() {
    if (browser != null) {
      browser.cancel();
    }
    removeListeners();
  }
  
  protected synchronized void addEntries(Collection<Entry> newEntries) {
    int oldSize = entries.size();
    entries.addAll(newEntries);
    fireEntriesAdded(oldSize, entries.size() -1);
    notifyAll();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized int getSize() {
    return Math.max(entries.size(), reportedSize);
  }
  
  protected synchronized void setReportedSize(int count) {
    reportedSize = count;
    fireSizeChanged();
    notifyAll();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized Entry getEntryAt(int index) {
    return entries.get(index);
  }

  public synchronized boolean hasEntryFor(int index) {
    return index < entries.size();
  }
  
  public synchronized int indexOf(Entry entry) {
    return entries.indexOf(entry);
  }
  
  
  public synchronized List<Entry> getEntries() {
	  //make this a blocking call (converting the asynchronous call to a synchronous call for use with the servlet)
	  while (browser != null) {
		  try {
			  wait(60000);
		  } catch (InterruptedException e) {
		  }
	  }
	  return getEntries(0, reportedSize);
  }
  
  
  public synchronized List<Entry> getEntries(int startindex, int endindex) {
	  //make this a blocking call (converting the asynchronous call to a synchronous call for use with the servlet)
	  while (!hasEntryFor(endindex-1)) {
		  //Busy-wait with a bit of sleeping
		  try {
			  wait(60000);
			  if (reportedSize >= 0 && endindex > reportedSize) {
				  endindex = reportedSize;
			  }	  
		  } catch (InterruptedException e) {
		  }
	  }
	  return entries.subList(startindex, endindex);
  }
  
  /**
   * {@inheritDoc}
   */
  public void addListener(MusicLibraryListener listener) {
    this.listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void removeListener(MusicLibraryListener listener) {
    this.listeners.remove(listener);
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeListeners() {
    this.listeners.clear();
  }

  protected void fireEntriesAdded(int start, int end) {
    for (MusicLibraryListener listener: this.listeners ) {
      listener.entriesAdded(start, end);
    }
  }
  
  protected void fireSizeChanged() {
    for (MusicLibraryListener listener: this.listeners) {
      listener.sizeChanged();
    }
  }

  public class MusicLibraryEntryCallback implements EntryCallback {

    public synchronized void addEntries(BrowseHandle handle, Collection<Entry> entries) {
      MusicLibrary.this.addEntries(entries);
      notifyAll();
    }

    public synchronized void retrievalComplete(BrowseHandle handle, boolean completedSuccessfully) {
      if (handle == browser) {
        browser = null;
      }
      notifyAll();
    }

    public synchronized void updateCount(BrowseHandle handle, int count) {
      setReportedSize(count);
      notifyAll();
    }

  }

}
