/*
 * Created on 01/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.ui;

import java.util.ArrayList;
import java.util.List;

import net.sf.janos.control.SonosController;
import net.sf.janos.model.Entry;

/**
 * A table model for a list of entries. eg queue.
 * @author David Wheeler
 *
 */
public class MusicLibrary {
  
  protected final List<Entry> entries = new ArrayList<Entry>();
  
  public MusicLibrary(SonosController controller) {
    this(controller, null);
  }
  
  public MusicLibrary(SonosController controller, Entry entry) {
    if (entry != null) {
      entries.addAll(controller.getCurrentZonePlayer().getMediaServerDevice().getContentDirectoryService().getEntries(0, 50, entry.getId()));
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
