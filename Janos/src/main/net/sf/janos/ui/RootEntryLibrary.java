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
import java.util.Collection;

import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MusicLibrary;

public class RootEntryLibrary extends MusicLibrary{

  public RootEntryLibrary(ZonePlayer zone) {
    super(zone); // already adds A: entries
    Entry radio = new Entry("R:", "Radio Stations", null, null, null, null, "object.container.radioContainer", null);
    Entry lineIn = new Entry("AI:", "Line In", null, null, null, null, "object.container.lineInContainer", null);
    Collection<Entry> entries = new ArrayList<Entry>();
    entries.add(radio);
    entries.add(lineIn);
    // TODO these should be added AFTER the other entries...
    addEntries(entries);

  }
  
  @Override
  protected void setReportedSize(int count) {
    super.setReportedSize(count + 2);
  }
}
