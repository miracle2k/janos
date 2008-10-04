/*
   Copyright 2008 davidwheeler

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
package net.sf.janos.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.janos.model.Entry;
import net.sf.janos.model.MusicLibraryListener;
import net.sf.janos.model.MusicLibraryModel;

public class SearchResultLibrary implements MusicLibraryModel {
  protected final List<Entry> entries = new ArrayList<Entry>();

  private List<MusicLibraryListener> listeners = new ArrayList<MusicLibraryListener>();

  /**
  * {@inheritDoc}
  */
  public void dispose() {
   removeListeners();
  }

  public void addEntries(Collection<Entry> newEntries) {
   int oldSize = entries.size();
   entries.addAll(newEntries);
   fireSizeChanged();
   fireEntriesAdded(oldSize, entries.size() -1);
  }
  
  public void clear() {
    entries.clear();
    fireSizeChanged();
  }

  /**
  * {@inheritDoc}
  */
  public int getSize() {
   return entries.size();
  }

  /**
  * {@inheritDoc}
  */
  public Entry getEntryAt(int index) {
   return entries.get(index);
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasEntryFor(int index) {
   return index < entries.size();
  }
  
  /**
   * {@inheritDoc}
   */
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

}
