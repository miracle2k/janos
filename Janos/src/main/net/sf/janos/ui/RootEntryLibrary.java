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

import net.sf.janos.control.BrowseHandle;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MusicLibrary;

public class RootEntryLibrary extends MusicLibrary{

  private Entry searchResults;
  private ArrayList<Entry> additionalEntries;

  public RootEntryLibrary(ZonePlayer zone) {
    super(zone); // already adds A: entries
  }
  
  @Override
  protected void setReportedSize(int count) {
    super.setReportedSize(count + additionalEntries.size());
  }
  
  public Entry getSearchLibraryEntry() {
    return searchResults;
  }
  
  @Override
  protected BrowseHandle loadEntries(ZonePlayer zone, String type) {
    Entry radio = new Entry("R:0", "Radio", null, null, null, null, "object.container.radioContainer", null);
    Entry lineIn = new Entry("AI:", "Line In", null, null, null, null, "object.container.lineInContainer", null);
    Entry savedQueues = new Entry("SQ:", "Saved Queues", null, null, null, null, "object.container.savedQueueContainer", null);
    searchResults = new Entry("SEARCH_RESULT", "Search Results", null, null, null, null, "object.container.searchResultContainer", null);
    additionalEntries = new ArrayList<Entry>();
    additionalEntries.add(radio);
    additionalEntries.add(lineIn);
    additionalEntries.add(savedQueues);
    additionalEntries.add(searchResults);
    // TODO these should be added AFTER the other entries...
    addEntries(additionalEntries);
    return super.loadEntries(zone, type);
  }
}
