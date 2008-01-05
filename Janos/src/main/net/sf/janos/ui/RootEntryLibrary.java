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

import java.util.List;

import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MusicLibrary;

// TODO need to implement selection listener?
public class RootEntryLibrary extends MusicLibrary {

  public RootEntryLibrary(ZonePlayer zone) {
    super(zone);
    int start = 0;
    int length = 50;
    List<Entry> newArtists = zone.getMediaServerDevice()
        .getContentDirectoryService().getFolderEntries(start, length);
    while (newArtists != null && newArtists.size() > 0) {
      entries.addAll(newArtists);
      if (length > newArtists.size()) {
        break;
      }
      start += length;
      newArtists = zone.getMediaServerDevice().getContentDirectoryService()
          .getArtists(start, length);
    }

  }
}
