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
import java.util.Collections;

import net.sf.janos.control.BrowseHandle;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.MusicLibrary;
import net.sf.janos.model.MusicLibraryModel;

/**
 * A Music Library containing the line in entries for each available zone
 * 
 * @author David Wheeler
 * 
 */
public class LineInMusicLibrary extends MusicLibrary implements MusicLibraryModel {

  public LineInMusicLibrary() {
    super(null);
  }
  
  @Override
  protected BrowseHandle loadEntries(ZonePlayer zone, String type) {
    AggregatingBrowseHandle result = new AggregatingBrowseHandle();
    for (ZonePlayer zp : SonosController.getInstance().getZonePlayerModel().getAllZones()) {
      result.addBrowseHandle(super.loadEntries(zp, "AI:"));
    }
    return result;
  }
  
  private static class AggregatingBrowseHandle implements BrowseHandle {

    private Collection<BrowseHandle> browseHandles = Collections.synchronizedCollection(new ArrayList<BrowseHandle>());
    
    public void addBrowseHandle(BrowseHandle browseHandle) {
      browseHandles.add(browseHandle);
    }
    
    public void cancel() {
      synchronized (browseHandles) {
        for (BrowseHandle handle : browseHandles) {
          handle.cancel();
        }
        browseHandles.clear();
      }
    }
    
  }
}
