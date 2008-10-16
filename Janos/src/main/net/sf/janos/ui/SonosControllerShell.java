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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SonosControllerShell {

	public final static int ZONE_LIST_WIDTH = 200;
	public final static int NOW_PLAYING_WIDTH = 250;

	private final Display display;

	private final Shell shell;

	private final SonosController controller;
	// private ZoneInfo zoneInfo;
	private MusicLibraryTable music;
	private SearchBar searchBar;
	private UrlAdder urlAdder;
	private ZoneControlList zoneControls;
	
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
		shell.setLayout(new GridLayout(2, false));

		
		
		ExpandBar bar = new ExpandBar(shell, SWT.V_SCROLL);
		GridData zoneControlData = new GridData(GridData.FILL_VERTICAL);
		zoneControlData.widthHint = 400;
		zoneControlData.verticalSpan = 2;
		bar.setLayoutData(zoneControlData);
		zoneControls = new ZoneControlList(bar, controller);
		
		Composite topPanel = new Composite(shell, SWT.NONE);
		topPanel.setLayout(new GridLayout(4, false));
		GridData topPanelData = new GridData();
		topPanelData.grabExcessHorizontalSpace=true;
		topPanelData.horizontalAlignment=GridData.HORIZONTAL_ALIGN_FILL;
		topPanel.setLayoutData(topPanelData);

		Label searchLabel = new Label(topPanel, SWT.NONE);
		searchLabel.setText("Search: ");
		
		searchBar = new SearchBar(topPanel, SWT.NONE, this);
		GridData searchBarData = new GridData();
		searchBarData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		searchBar.setLayoutData(searchBarData);

		Label urlAdderLabel = new Label(topPanel, SWT.NONE);
		urlAdderLabel.setText("URL Adder: ");
		
		urlAdder = new UrlAdder(topPanel, SWT.NONE);
		GridData urlAdderData = new GridData();
		urlAdderData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		searchBar.setLayoutData(urlAdderData);

		

		music = new MusicLibraryTable(shell, SWT.NONE, this);
		GridData musicData = new GridData(SWT.FILL, SWT.FILL, true, true);
		musicData.widthHint=400;
		musicData.heightHint=400;
		music.setLayoutData(musicData);

//		zoneInfo = new ZoneInfo(shell, SWT.SINGLE | SWT.BORDER);
//		GridData ziData = new GridData();
//		ziData.widthHint=NOW_PLAYING_WIDTH;
//		ziData.verticalAlignment=GridData.FILL;
//		ziData.horizontalAlignment=GridData.FILL;
//		ziData.grabExcessVerticalSpace=true;
//		zoneInfo.setLayoutData(ziData);
//		zoneControls.addSelectionListener(zoneInfo);
	}

	private void dispose() {
		searchBar.dispose();
		music.dispose();
		shell.dispose();
	}

	public SonosController getController() {
		return controller;
	}

	public MusicLibraryTable getMusicLibrary() {
		return music;
	}
	
	public ZoneControlList getZoneList() {
		return zoneControls;
	}
}
