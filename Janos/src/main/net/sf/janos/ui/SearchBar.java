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
import java.util.List;

import net.sf.janos.control.BrowseHandle;
import net.sf.janos.control.EntryCallback;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class SearchBar extends Composite {

  private SonosControllerShell controller;
  private List<BrowseHandle> browseHandles;

  public SearchBar(Composite parent, int style, SonosControllerShell controller) {
    super(parent, style);
    setLayout(new FillLayout());
    this.controller = controller;
    this.browseHandles = Collections.synchronizedList(new ArrayList<BrowseHandle>());
    
    Text searchField = new Text(this, SWT.SEARCH | SWT.CANCEL );
    @SuppressWarnings("unused")
	SearchAction searchAction = new SearchAction(searchField);
  }

  public void performSearch(String text) {
    cancelPreviousSearch();
    ZonePlayer currentZone = controller.getZoneList().getSelectedZone();
    if (currentZone != null) {
      // TODO this should use cached results, it'll take some time this way...
      currentZone = SonosController.getCoordinatorForZonePlayer(currentZone);
      SearchResultCallback callback = new SearchResultCallback();
      browseHandles.add(currentZone.getMediaServerDevice().getContentDirectoryService().getAllEntriesAsync(callback, "A:TRACKS:" + text));
      browseHandles.add(currentZone.getMediaServerDevice().getContentDirectoryService().getAllEntriesAsync(callback, "A:ARTIST:" + text));
      browseHandles.add(currentZone.getMediaServerDevice().getContentDirectoryService().getAllEntriesAsync(callback, "A:ALBUMARTIST:" + text));
      browseHandles.add(currentZone.getMediaServerDevice().getContentDirectoryService().getAllEntriesAsync(callback, "A:ALBUM:" + text));
    }
  }


  private void cancelPreviousSearch() {
    synchronized (browseHandles) {
      for (BrowseHandle handle : browseHandles) {
        handle.cancel();
      }
      browseHandles.clear();
    }
  }


  public class SearchAction implements SelectionListener {

    private Text searchField;

    public SearchAction(Text searchField) {
      searchField.addSelectionListener(this);
      this.searchField = searchField;
    }

    public void widgetDefaultSelected(SelectionEvent e) {
      if (e.detail == SWT.CANCEL) {
        cancelPreviousSearch();
        controller.getMusicLibrary().clearSearch();
      } else {
        // do search
        controller.getMusicLibrary().clearSearch();
        performSearch(searchField.getText());
      }
    }

    public void widgetSelected(SelectionEvent e) {
      // don't care
    }

  }

  public class SearchResultCallback implements EntryCallback {

    public void addEntries(BrowseHandle handle, final Collection<Entry> entries) {
      final MusicLibraryTable musicLibrary = controller.getMusicLibrary();
      musicLibrary.getDisplay().asyncExec(new Runnable() {
        public void run() {
          musicLibrary.addSearchResults(entries);
        }
      });
    }

    public void retrievalComplete(BrowseHandle handle, boolean completedSuccessfully) {
      browseHandles.remove(handle);
    }

    public void updateCount(BrowseHandle handle, int count) {
      // ignored
    }

  }
}
