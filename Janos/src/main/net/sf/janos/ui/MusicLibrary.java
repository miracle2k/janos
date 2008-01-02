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
package net.sf.janos.ui;

import java.util.ArrayList;
import java.util.List;

import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;

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
      entries.addAll(zone.getMediaServerDevice().getContentDirectoryService().getEntries(0, 50, entry.getId()));
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
