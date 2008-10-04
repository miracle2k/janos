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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.janos.control.BrowseHandle;
import net.sf.janos.control.EntryCallback;
import net.sf.janos.control.ZonePlayer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A table model for a list of entries. eg queue.
 * @author David Wheeler
 *
 */
public class MusicLibrary implements MusicLibraryModel {

  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(MusicLibrary.class);

  protected final List<Entry> entries = new ArrayList<Entry>();
  
  private int reportedSize;

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
  protected BrowseHandle loadEntries(ZonePlayer zone, String type) {
    return zone.getMediaServerDevice().getContentDirectoryService().getAllEntriesAsync(new MusicLibraryEntryCallback(), type);
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
  
  protected void addEntries(Collection<Entry> newEntries) {
    int oldSize = entries.size();
    entries.addAll(newEntries);
    fireEntriesAdded(oldSize, entries.size() -1);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() {
    return Math.max(entries.size(), reportedSize);
  }
  
  protected void setReportedSize(int count) {
    reportedSize = count;
    fireSizeChanged();
  }

  /**
   * {@inheritDoc}
   */
  public Entry getEntryAt(int index) {
    return entries.get(index);
  }

  public boolean hasEntryFor(int index) {
    return index < entries.size();
  }
  
  public int indexOf(Entry entry) {
    return entries.indexOf(entry);
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

    public void addEntries(BrowseHandle handle, Collection<Entry> entries) {
      MusicLibrary.this.addEntries(entries);
    }

    public void retrievalComplete(BrowseHandle handle, boolean completedSuccessfully) {
      if (handle == browser) {
        browser = null;
      }
    }

    public void updateCount(BrowseHandle handle, int count) {
      setReportedSize(count);
    }

  }

}
