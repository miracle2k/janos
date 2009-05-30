/*
   Copyright 2009 david

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

import net.sf.janos.ui.action.PauseResumeAction;
import net.sf.janos.ui.action.SkipAction;
import net.sf.janos.ui.action.SkipBackAction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class SonosMenuBar {
  
  private final Shell shell;
  private final Menu menuBar;

  public SonosMenuBar(Shell shell) {
    this.shell = shell;
    this.menuBar = new Menu(shell, SWT.BAR);

    MenuItem playbackItem = new MenuItem(menuBar, SWT.CASCADE);
    playbackItem.setText("Playback");
    Menu playbackMenu = new Menu(shell, SWT.DROP_DOWN);
    playbackItem.setMenu(playbackMenu);

    MenuItem pauseItem = new MenuItem(playbackMenu, SWT.NONE);
    pauseItem.setText("&Play/Pause");
    pauseItem.setAccelerator(SWT.MOD1 + 'p');
    pauseItem.addSelectionListener(new PauseResumeAction());

    MenuItem skipItem = new MenuItem(playbackMenu, SWT.NONE);
    skipItem.setText("&Next");
    skipItem.setAccelerator(SWT.MOD1 + SWT.ARROW_RIGHT);
    skipItem.addSelectionListener(new SkipAction());

    MenuItem skipBackItem = new MenuItem(playbackMenu, SWT.NONE);
    skipBackItem.setText("&Back");
    skipBackItem.setAccelerator(SWT.MOD1 + SWT.ARROW_LEFT);
    skipBackItem.addSelectionListener(new SkipBackAction());
}

  public void activate() {
    this.shell.setMenuBar(this.menuBar);
  }

}
