/*
 * Created on 04/11/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.ui;

import java.util.List;

import net.sf.janos.control.SonosController;
import net.sf.janos.control.SonosControllerListener;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;

public class RootEntryLibrary extends MusicLibrary implements SonosControllerListener {

  public RootEntryLibrary(SonosController controller) {
    super(controller);
    controller.addControllerListener(this);
  }
  
  public synchronized void deviceAdded(ZonePlayer dev) {
    if (!entries.isEmpty()) {
      return;
    }
    int start = 0;
    int length = 50;
    List<Entry> newArtists = dev.getMediaServerDevice()
        .getContentDirectoryService().getFolderEntries(start, length);
    while (newArtists != null && newArtists.size() > 0) {
      entries.addAll(newArtists);
      if (length > newArtists.size()) {
        break;
      }
      start += length;
      newArtists = dev.getMediaServerDevice().getContentDirectoryService()
          .getArtists(start, length);
    }

  }

  public void deviceRemoved(ZonePlayer dev) {
    // TODO Auto-generated method stub
    
  }
}
