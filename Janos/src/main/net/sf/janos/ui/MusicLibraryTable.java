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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import net.sf.janos.control.SonosController;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MusicLibrary;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
public class MusicLibraryTable extends Composite {
  
  private final List<MusicTable> music = new LinkedList<MusicTable>();
  
  public MusicLibraryTable(final Composite parent, int style, final SonosControllerShell controller) {
    super(parent, style);
    // TODO IndexOutOfBounds
    final MusicLibrary rootLib = new RootEntryLibrary(controller.getController().getZonePlayerModel().get(0));
    setLayout(new FillLayout(SWT.HORIZONTAL));
    final Table typeTable = new Table(this, SWT.MULTI |SWT.FULL_SELECTION| SWT.VIRTUAL | SWT.BORDER);
    music.add(new MusicTable(typeTable, rootLib));
    typeTable.setLinesVisible(true);
    typeTable.setHeaderVisible(true);
    TableColumn name = new TableColumn(typeTable, SWT.LEFT);
    name.setText("Type");
    name.setWidth(150);

    typeTable.setItemCount(rootLib.getSize());
    typeTable.addListener (SWT.SetData, new Listener () {
      public void handleEvent (final Event event) {
//        final Display currentDisplay = Display.getCurrent();
//        controller.getExecutor().execute(new Runnable() {
//          public void run() {
//            // do stuff to get info
//            currentDisplay.asyncExec(new Runnable() {
//              public void run() {
                TableItem item = (TableItem) event.item;
                int index = typeTable.indexOf(item);
                item.setText(rootLib.getEntryAt(index).getTitle());
//              }
//            });
//          }
//        });
      }
    });
    final MouseListener tableMouseListener = new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
        // TODO multi selection
        for (MusicTable table : music ) {
          if (table.getTable() == e.getSource()) {
            int sel = table.getTable().getSelectionIndex();
            Entry entry = table.getModel().getEntryAt(sel);
            SonosController.getCoordinatorForZonePlayer(controller.getZoneList().getSelectedZone()).enqueueEntry(entry);
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
      
    };
    final SelectionListener selectionListener = new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
      }
      public void widgetSelected(SelectionEvent e) {
        // populate next table
        ListIterator<MusicTable> i = music.listIterator();
        MusicTable selectedTable = null;
        boolean tableFound = false;
        while (i.hasNext()) {
          MusicTable table = i.next();
          if (table.getTable() == e.getSource()) {
            tableFound = true;
            selectedTable = table;
          } else if (tableFound) {
            i.remove();
            table.getTable().removeMouseListener(tableMouseListener);
            table.getTable().removeSelectionListener(this);
            table.table.dispose();
          }
        }
        if (selectedTable != null && selectedTable.getTable().getSelectionIndex() >= 0) {
          // TODO multi selection
          Entry entry = selectedTable.getModel().getEntryAt(selectedTable.getTable().getSelectionIndex());
          final MusicLibrary lib = new MusicLibrary(controller.getZoneList().getSelectedZone(), entry);
          final Table table = new Table(MusicLibraryTable.this, SWT.MULTI |SWT.FULL_SELECTION| SWT.VIRTUAL | SWT.BORDER);
          music.add(new MusicTable(table, lib));
          table.setLinesVisible(true);
          table.setHeaderVisible(true);
          TableColumn name = new TableColumn(table, SWT.LEFT);
          name.setText(entry.getTitle());
          name.setWidth(150);
          name.setResizable(true);
          
          table.setItemCount(lib.getSize());
          table.addListener (SWT.SetData, new Listener () {
            public void handleEvent (final Event event) {
              TableItem item = (TableItem) event.item;
              item.setText(lib.getEntryAt(event.index).getTitle());
            }
          });

          table.addMouseListener(tableMouseListener);
          table.addSelectionListener(this);
          layout();
        }
      }
    };
    typeTable.addMouseListener(tableMouseListener);
    typeTable.addSelectionListener(selectionListener);
  }
  
  protected class MusicTable {
    private final Table table;
    private final MusicLibrary model;
    
    public MusicTable(Table table, MusicLibrary model) {
      this.table = table;
      this.model = model;
    }

    public MusicLibrary getModel() {
      return model;
    }

    public Table getTable() {
      return table;
    }
  }

}
