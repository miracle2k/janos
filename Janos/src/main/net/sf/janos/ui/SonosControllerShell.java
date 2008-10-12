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

import net.sf.janos.control.SonosController;
import net.sf.janos.ui.zonelist.ZoneList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class SonosControllerShell {

	public final static int ZONE_LIST_WIDTH = 200;
	public final static int NOW_PLAYING_WIDTH = 250;
	
	private final Display display;
  
  private final Shell shell;
	
  private final SonosController controller;
  
  private ZoneList zoneList;
  private MusicControlPanel controls;
	private ZoneInfo zoneInfo;

  private MusicLibraryTable music;

  private SearchBar searchBar;
  private UrlAdder urlAdder;

	public SonosControllerShell(Display display, SonosController controller) {
		this.display = display;
    this.shell = new Shell(display);
    this.controller = controller;
		buildComponents();
	}
  
  public void start() {
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    controller.dispose();
    display.dispose();
    dispose();
  }
  
	private void buildComponents() {
    shell.setText("SonosJ");
    shell.setLayout(new GridLayout(3, false));
		
    Composite topPanel = new Composite(shell, SWT.NONE);
    topPanel.setLayout(new GridLayout(3, false));
    GridData topPanelData = new GridData();
    topPanelData.horizontalSpan=3;
    topPanelData.grabExcessHorizontalSpace=true;
    topPanelData.horizontalAlignment=SWT.FILL;
    topPanel.setLayoutData(topPanelData);
    
    controls = new MusicControlPanel(topPanel, SWT.NONE, null);
    GridData controlData = new GridData();
		controls.setLayoutData(controlData);
    
    Group urlGroup = new Group(topPanel, SWT.None);
    urlGroup.setText("Play / Add URL");
    urlGroup.setLayout(new FillLayout());
		urlAdder = new UrlAdder(urlGroup, SWT.NONE);
		GridData urlAdderData = new GridData();
		urlAdderData.horizontalAlignment = SWT.FILL;
    urlAdderData.grabExcessHorizontalSpace=true;
    urlAdderData.verticalAlignment = SWT.FILL;
    urlGroup.setLayoutData(urlAdderData);
		
    Group searchGroup = new Group(topPanel, SWT.None);
    searchGroup.setText("Search");
    searchGroup.setLayout(new FillLayout());
    searchBar = new SearchBar(searchGroup, SWT.NONE, this);
    GridData searchBarData = new GridData();
    searchBarData.horizontalAlignment = SWT.RIGHT;
    searchBarData.verticalAlignment = SWT.FILL;
    searchGroup.setLayoutData(searchBarData);

		zoneList = new ZoneList(shell, SWT.BORDER, controller);
		GridData zoneData = new GridData();
    zoneData.verticalAlignment=SWT.FILL;
		zoneData.widthHint = ZONE_LIST_WIDTH;
		zoneData.heightHint= 400;
		zoneList.setLayoutData(zoneData);
		zoneList.addSelectionListener(this.controller);
		zoneList.addSelectionListener(controls);
    zoneList.addSelectionListener(urlAdder);
		
		music = new MusicLibraryTable(shell, SWT.NONE, this);
		GridData musicData = new GridData(SWT.FILL, SWT.FILL, true, true);
		musicData.widthHint=400;
		musicData.heightHint=400;
		music.setLayoutData(musicData);
		
		zoneInfo = new ZoneInfo(shell, SWT.SINGLE | SWT.BORDER);
		GridData ziData = new GridData();
		ziData.widthHint=NOW_PLAYING_WIDTH;
		ziData.verticalAlignment=SWT.FILL;
		ziData.horizontalAlignment=SWT.FILL;
		ziData.grabExcessVerticalSpace=true;
		zoneInfo.setLayoutData(ziData);
		zoneList.addSelectionListener(zoneInfo);
	}
	
  private void dispose() {
    zoneList.removeSelectionListener(this.controller);
    zoneList.removeSelectionListener(controls);
    zoneList.dispose();
    searchBar.dispose();
    urlAdder.dispose();
    music.dispose();
		
    controls.dispose();
		if (zoneInfo != null) {
			zoneInfo.dispose();
		}
    shell.dispose();
  }
  
  public SonosController getController() {
    return controller;
  }
  
  public ZoneList getZoneList() {
    return zoneList;
  }

  public MusicLibraryTable getMusicLibrary() {
    return music;
  }
}
