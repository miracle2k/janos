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

import net.sf.janos.control.ZonePlayer;

/**
 * A table model for a list of entries. eg queue.
 * @author David Wheeler
 *
 */
public class MusicLibrary {
  
  protected final List<Entry> entries = new ArrayList<Entry>();
  
  public MusicLibrary(ZonePlayer zone) {
    this(zone, null);
  }
  
  public MusicLibrary(ZonePlayer zone, Entry entry) {
    if (entry != null) {
      // get them in small groups to speed it up
      int startAt = 0;
      int length = 100;
      Collection<Entry> newEntries = zone.getMediaServerDevice().getContentDirectoryService().getEntries(startAt, length, entry.getId());
      while (newEntries != null && newEntries.size() >= length) {
        entries.addAll(newEntries);
        startAt += length;
        newEntries = zone.getMediaServerDevice().getContentDirectoryService().getEntries(startAt, length, entry.getId());
      }
      if (newEntries != null) {
        entries.addAll(newEntries);
      }
    }
    // TODO add notification listener
  }

  public int getSize() {
    return entries.size();
  }
  
  public Entry getEntryAt(int index) {
    return entries.get(index);
  }
}
