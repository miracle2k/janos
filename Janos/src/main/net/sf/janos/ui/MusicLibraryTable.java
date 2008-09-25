/*
 * Copyright 2007 David Wheeler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.janos.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MusicLibrary;
import net.sf.janos.model.MusicLibraryListener;
import net.sf.janos.model.ZonePlayerModel;
import net.sf.janos.model.ZonePlayerModelListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A UI component for displaying a table of entries.
 * @author David Wheeler
 *
 */
public class MusicLibraryTable extends Composite implements ZonePlayerModelListener {
  
  /**
   * The string displayed when no zone players are known of
   */
  private static final String NO_ZONES_TABLE_STRING = "No ZonePlayers found";
  
  private static final ImageData ARTIST_IMAGE = new ImageData("artist.png");
  private static final ImageData TRACK_IMAGE = new ImageData("track.png");
  private static final ImageData ALBUM_IMAGE = new ImageData("album.png");
  private static final ImageData PLAYLIST_IMAGE = new ImageData("playlist.png");
  private static final ImageData GENRE_IMAGE = new ImageData("genre.png");
  
  /**
   * The list of music tables and related objects
   */
  private final List<MusicTable> musicTables = new LinkedList<MusicTable>();
  
  /**
   * The mouse listener that acts on double click events
   */
  private final MouseListener tableMouseListener;
  
  /**
   * The listener that acts on table selection events
   */
  private final SelectionListener tableSelectionListener;
  
  /**
   * Creates a new MusicLibraryTable
   * @param parent the parent Composite in which this Composite resides
   * @param style The style
   * @param controllerShell the SonosControllerShell
   */
  public MusicLibraryTable(final Composite parent, int style, final SonosControllerShell controllerShell) {
    super(parent, style);
    tableMouseListener = new TableMouseListener(controllerShell);
    tableSelectionListener = new TableSelectionListener(controllerShell);
    setLayout(new FillLayout(SWT.HORIZONTAL));

    ZonePlayerModel zoneModel = controllerShell.getController().getZonePlayerModel();
    zoneModel.addZonePlayerModelListener(this);
    
    if (zoneModel.getAllZones().isEmpty()) {
      clearMusicLibrary();
    } else {
      populateMusicLibraryFrom(zoneModel.get(0));
    }
    
  }
  
  /**
   * Populates the music library from the given zonePlayer
   * @param player
   */
  private void populateMusicLibraryFrom(ZonePlayer player) {
    for (MusicTable table : musicTables) {
      removeTable(table);
    }
    musicTables.clear();
    
    final MusicLibrary rootLib = new RootEntryLibrary(player);
    final Table typeTable = new Table(this, SWT.MULTI |SWT.FULL_SELECTION| SWT.VIRTUAL | SWT.BORDER);
    TableLibraryAdapter listener = new TableLibraryAdapter(rootLib);
    TableUpdater musicLibraryListener = new TableUpdater(typeTable, rootLib);
    rootLib.addListener(musicLibraryListener);
    musicTables.add(new MusicTable(typeTable, rootLib, listener));
    typeTable.setLinesVisible(true);
    typeTable.setHeaderVisible(true);
    TableColumn name = new TableColumn(typeTable, SWT.LEFT);
    name.setText("Type");
    name.setWidth(180);
    
    typeTable.setItemCount(rootLib.getSize());
    typeTable.addListener (SWT.SetData, listener);
    typeTable.addMouseListener(tableMouseListener);
    typeTable.addSelectionListener(tableSelectionListener);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    if (musicTables != null) {
      for (MusicTable table : musicTables) {
        if (table.table != null) {
          if (table.listener != null) {
            table.table.removeListener(SWT.SetData, table.listener);
          }
          if (table.model != null) {
            table.model.removeListeners();
          }
          for (TableItem item : table.table.getItems()) {
            if (item.getImage() != null) {
              item.getImage().dispose();
            }
          }
          table.table.dispose();
        }
      }
    }
    super.dispose();
  }
  /**
   * Removes the given table from the display. NOTE: the disposed table is still
   * in <code>musicTables</code>.
   * 
   * @param table the table to remove
   */
  private void removeTable(MusicTable table) {
    table.getTable().removeMouseListener(tableMouseListener);
    table.getTable().removeSelectionListener(tableSelectionListener);
    if (table.model != null) {
      table.model.dispose();
    }
    table.table.dispose();
  }
  
  /**
   * Clears the music library and replaces it with an empty table
   *
   */
  private void clearMusicLibrary() {
    for (MusicTable table : musicTables) {
      removeTable(table);
    }
    musicTables.clear();

    Table emptyTable = new Table(this, SWT.MULTI |SWT.FULL_SELECTION| SWT.VIRTUAL | SWT.BORDER);
    musicTables.add(new MusicTable(emptyTable, null, null));
    emptyTable.setLinesVisible(true);
    emptyTable.setHeaderVisible(true);
    TableColumn name = new TableColumn(emptyTable, SWT.LEFT);
    name.setText(NO_ZONES_TABLE_STRING);
    name.setWidth(150);
  }

  /**
   * {@inheritDoc}
   */
  public void zonePlayerAdded(final ZonePlayer zp, final ZonePlayerModel model) {
    // need to do this on display thread
    getDisplay().asyncExec(new Runnable() {
      public void run() {
        if (!musicTables.isEmpty() && musicTables.get(0).getModel() == null) {
          populateMusicLibraryFrom(zp);
          layout();
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public void zonePlayerRemoved(final ZonePlayer zp, final ZonePlayerModel model) {
    // need to do this on display thread
    getDisplay().asyncExec(new Runnable() {
      public void run() {
        if (model.getAllZones().isEmpty()) {
          clearMusicLibrary();
          layout();
        }
      }
    });
  }

  /**
   * Adds selected entry to queue on double click events
   */
  private final class TableMouseListener implements MouseListener {
    private final SonosControllerShell controller;

    private TableMouseListener(SonosControllerShell controller) {
      this.controller = controller;
    }

    public void mouseDoubleClick(MouseEvent e) {
      // TODO multi selection
      for (MusicTable table : musicTables ) {
        if (table.getTable() == e.getSource()) {
          int sel = table.getTable().getSelectionIndex();
          if (sel >= 0) {
            Entry entry = table.getModel().getEntryAt(sel);
            ZonePlayer zone = SonosController.getCoordinatorForZonePlayer(controller.getZoneList().getSelectedZone());
            zone.enqueueEntry(entry);
            
          }
          return;
        }
      }
    }

    public void mouseDown(MouseEvent e) {
      // Don't care
    }

    public void mouseUp(MouseEvent e) {
      // Don't care
    }
  }

  /**
   * Opens child table on selection events
   */
  private final class TableSelectionListener implements SelectionListener {
    private final SonosControllerShell controller;

    private TableSelectionListener(SonosControllerShell controller) {
      this.controller = controller;
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
      // populate next table
      ListIterator<MusicTable> i = musicTables.listIterator();
      MusicTable selectedTable = null;
      boolean tableFound = false;
      while (i.hasNext()) {
        MusicTable table = i.next();
        if (table.getTable() == e.getSource()) {
          tableFound = true;
          selectedTable = table;
        } else if (tableFound) {
          i.remove();
          removeTable(table);
        }
      }
      if (selectedTable != null && selectedTable.getTable().getSelectionIndex() >= 0) {
        // TODO multi selection
        Entry entry = selectedTable.getModel().getEntryAt(selectedTable.getTable().getSelectionIndex());
        if (entry.getUpnpClass().startsWith("object.container")) {
          final MusicLibrary lib = new MusicLibrary(controller.getZoneList().getSelectedZone(), entry);
          final Table table = new Table(MusicLibraryTable.this, SWT.MULTI |SWT.FULL_SELECTION| SWT.VIRTUAL | SWT.BORDER);
          TableUpdater musicLibraryListener = new TableUpdater(table, lib);
          lib.addListener(musicLibraryListener);
          TableLibraryAdapter listener = new TableLibraryAdapter(lib);
          musicTables.add(new MusicTable(table, lib, listener));
          table.setLinesVisible(true);
          table.setHeaderVisible(true);
          TableColumn name = new TableColumn(table, SWT.LEFT);
          name.setText(entry.getTitle());
          name.setWidth(150);
          name.setResizable(true);

          table.setItemCount(lib.getSize());
          table.addListener (SWT.SetData, listener);

          table.addMouseListener(tableMouseListener);
          table.addSelectionListener(this);
          layout();
        }
      }
    }
  }

  /**
   * Populates the tables with the data from the MusicLibrary
   */
  private final class TableLibraryAdapter implements Listener {

    private final MusicLibrary lib;

    private TableLibraryAdapter(MusicLibrary lib) {
      this.lib = lib;
    }

    public void handleEvent (final Event event) {
      TableItem item = (TableItem) event.item;
      if (lib.hasEntryFor(event.index)) {
        Entry entry = lib.getEntryAt(event.index);
        item.setText(entry.getTitle());
        item.setImage(createImage(item.getDisplay(), entry));
      } else {
        item.setText("<loading..>");
      }
    }
  }

  /**
   * Updates the table when the library changes
   *
   */
  private final class TableUpdater implements MusicLibraryListener {
    private final Table table;

    private final MusicLibrary lib;

    private TableUpdater(Table table, MusicLibrary lib) {
      this.table = table;
      this.lib = lib;
    }

    public void entriesAdded(final int start, final int end) {
      table.getDisplay().asyncExec(new Runnable() {
        public void run() {
          if (!table.isDisposed()) {
            table.clear(start, end);
          }
        }
      });
    }

    public void sizeChanged() {
      table.getDisplay().asyncExec(new Runnable() {
        public void run() {
          if (!table.isDisposed()) {
            table.setItemCount(lib.getSize());
          }
        }
      });
    }
  }

  /**
   * A container for a table and a model.
   * @author David Wheeler
   *
   */
  protected class MusicTable {
    private final Table table;
    private final MusicLibrary model;
    private final TableLibraryAdapter listener;
    
    public MusicTable(Table table, MusicLibrary model, TableLibraryAdapter listener) {
      this.table = table;
      this.model = model;
      this.listener = listener;
    }

    public MusicLibrary getModel() {
      return model;
    }

    public Table getTable() {
      return table;
    }
  }

  /**
   * Creates an image appropriate for the provided entry
   * @param display the Device to create the image for
   * @param entry the entry the image is to reflect
   * @return an Image appropriate for the given entry, or null
   */
  public Image createImage(Device display, Entry entry) {
    // try to do it in order of most-popular to least-popular, to reduce cpu load
    if (entry.getUpnpClass().equals("object.container.album.musicAlbum")) {
      return new Image(display, ALBUM_IMAGE);
    }
    if (entry.getUpnpClass().equals("object.item.audioItem.musicTrack")) {
      return new Image(display, TRACK_IMAGE);
    }
    if (entry.getId().startsWith("A:ALBUMARTIST")) {
      return new Image(display, ARTIST_IMAGE);
    }
    if (entry.getId().startsWith("A:ALBUM")) {
      return new Image(display, ALBUM_IMAGE);
    }
    if (entry.getId().startsWith("A:TRACK")) {
      return new Image(display, TRACK_IMAGE);
    }
    if (entry.getId().startsWith("A:COMPOSER")) {
      return new Image(display, ARTIST_IMAGE);
    }
    if (entry.getId().startsWith("A:PLAYLIST")) {
      return new Image(display, PLAYLIST_IMAGE);
    }
    if (entry.getId().startsWith("A:ARTIST")) { // Contributing Artist
      return new Image(display, ARTIST_IMAGE);
    }
    if (entry.getId().startsWith("A:GENRE")) {
      return new Image(display, GENRE_IMAGE);
    }
    if (entry.getUpnpClass().equals("object.container.radioContainer")) { // "Radio Stations"
      return new Image(display, PLAYLIST_IMAGE);
    }
    if (entry.getUpnpClass().equals("object.item.audioItem.audioBroadcast")) { // a playable radio station
      return new Image(display, TRACK_IMAGE);
    }
    if (entry.getId().startsWith("R:")) { // a radio station grouping
      return new Image(display, PLAYLIST_IMAGE);
    }
    if (entry.getId().startsWith("AI:")) {
      return null; // line in
    }
    return null;
  }

}
