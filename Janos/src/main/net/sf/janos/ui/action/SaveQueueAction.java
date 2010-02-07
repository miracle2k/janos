/*
   Copyright 2010 david

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
package net.sf.janos.ui.action;

import java.io.IOException;

import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.ApplicationContext;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.ui.SonosControllerShell;

import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SaveQueueAction implements SelectionListener {

  protected String queueName;
  protected boolean okPressed;

  public void widgetDefaultSelected(SelectionEvent e) {
    // no action
  }

  public void widgetSelected(SelectionEvent e) {
    
    SonosControllerShell controllerShell = ApplicationContext.getInstance().getShell();
    final Shell saveQueueDialog = new Shell(controllerShell.getShell(), SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
    Display display = controllerShell.getShell().getDisplay();
    saveQueueDialog.setText("Choose Saved Queue Name");
    saveQueueDialog.setLayout(new GridLayout(1, true));
    
    Label explanation = new Label(saveQueueDialog, SWT.NONE);
    explanation.setText("Choose a name for the saved Queue.");
    
    final Text queueNameText = new Text(saveQueueDialog, SWT.BORDER);
    queueNameText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    
    okPressed = false;
    Composite buttons = new Composite(saveQueueDialog, SWT.NONE);
    buttons.setLayout(new GridLayout(2, false));
    final Button ok = new Button(buttons, SWT.NONE);
    ok.setText("OK");
    ok.setEnabled(false);
    queueNameText.addKeyListener(new KeyAdapter() {
      
      @Override
      public void keyReleased(KeyEvent e) {
        boolean nameSet = !"".equals(queueNameText.getText());
        if (nameSet && e.character == '\r') {
          okPressed = true;
          saveQueueDialog.close();
        } else {
          ok.setEnabled(nameSet);
        }
      }
    });
    ok.addSelectionListener(new SelectionListener() {
      
      public void widgetSelected(SelectionEvent e) {
        okPressed = true;
        saveQueueDialog.close();
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
        System.out.print("DEFAULT selected");
        okPressed = true;
        saveQueueDialog.close();
      }
    });
    Button cancel = new Button(buttons, SWT.NONE);
    cancel.setText("Cancel");
    cancel.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        saveQueueDialog.close();
      }
    });
    
    saveQueueDialog.addShellListener(new ShellAdapter() {
      @Override
      public void shellClosed(ShellEvent e) {
        queueName = queueNameText.getText();
      }
    });
    
    saveQueueDialog.pack();
    saveQueueDialog.open();
    while (!saveQueueDialog.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    saveQueueDialog.dispose();
    
    if (okPressed && queueName != null) {
      SonosController controller = ApplicationContext.getInstance().getController();
      ZonePlayer zone = controller.getCoordinatorForZonePlayer(controllerShell.getZoneList().getSelectedZone());
      try {
        AVTransportService avTransportService = zone.getMediaRendererDevice().getAvTransportService();
        avTransportService.saveQueue(queueName, "");
      } catch (IOException ex) {
        LogFactory.getLog(getClass()).error("Could not pause/resume playback", ex);
      } catch (UPNPResponseException ex) {
        LogFactory.getLog(getClass()).error("Could not pause/resume playback", ex);
      }
    }
  }

}
