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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;

public class SonosControllerShell {

	private final Display display;
	private final Shell shell;
	private final SonosController controller;
	private MusicLibraryTable music;
	private SearchBar searchBar;
	private UrlAdder urlAdder;
	private ZoneControlList zoneControls;
	private SashForm sashForm;
	
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
		shell.setLayout(new FillLayout());

		sashForm= new SashForm(shell,SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout());
		

		
		ExpandBar bar = new ExpandBar(sashForm, SWT.NONE);
//		GridData zoneControlData = new GridData(GridData.FILL_VERTICAL);
//		zoneControlData.widthHint = 175;
//		zoneControlData.verticalSpan = 2;
//		bar.setLayoutData(zoneControlData);
		zoneControls = new ZoneControlList(bar, controller);
		
		Composite rightSide = new Composite(sashForm, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 3;
		layout.marginHeight = 3;
		rightSide.setLayout(layout);
		
		Composite topPanel = new Composite(rightSide, SWT.NONE);
		topPanel.setLayout(new GridLayout(4, false));
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0, 0);
		data1.top = new FormAttachment(0, 0);
		topPanel.setLayoutData(data1);


		Label searchLabel = new Label(topPanel, SWT.NONE);
		searchLabel.setText("Search: ");
		
		searchBar = new SearchBar(topPanel, SWT.NONE, this);


		Label urlAdderLabel = new Label(topPanel, SWT.NONE);
		urlAdderLabel.setText("URL Adder: ");
		
		urlAdder = new UrlAdder(topPanel, SWT.NONE);


		music = new MusicLibraryTable(rightSide, SWT.NONE, this);
		FormData data2 = new FormData();
		data2.left = new FormAttachment(0, 0);
		data2.right = new FormAttachment(100, 0);
		data2.top = new FormAttachment(topPanel);
		data2.bottom = new FormAttachment(100,0);
		music.setLayoutData(data2);
		
		sashForm.setWeights(new int[] {30, 70} );
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
