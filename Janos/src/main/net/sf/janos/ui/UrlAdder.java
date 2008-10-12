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

import java.io.IOException;

import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.ZoneListSelectionListener;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.util.ui.ImageUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class UrlAdder extends Composite implements ZoneListSelectionListener {

  private Text urlField;
  private ZonePlayer currentZone;

  public UrlAdder(Composite parent, int style) {
    super(parent, style);
    setLayout(new GridLayout(3, false));
    
    urlField = new Text(this, SWT.BORDER);
    GridData urlFieldData = new GridData();
    urlFieldData.grabExcessHorizontalSpace=true;
    urlFieldData.horizontalAlignment=SWT.FILL;
    urlField.setLayoutData(urlFieldData);
    
    SelectionListener playNowAction = new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
      public void widgetSelected(SelectionEvent e) {
        Entry entry = createEntry(urlField.getText());
        AVTransportService service = currentZone.getMediaRendererDevice().getAvTransportService();
        int index;
        try {
          index = service.addToQueue(entry);
          currentZone.playQueueEntry(index);
          service.play();
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } catch (UPNPResponseException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }
    };
    urlField.addSelectionListener(playNowAction);
    
    Button playNowButton = new Button(this, SWT.None);
    playNowButton.setImage(new Image(getDisplay(), ImageUtilities.loadImageDataFromSystemClasspath("nowPlaying.png")));
    playNowButton.addSelectionListener(playNowAction);

    SelectionListener addToQueueAction = new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
      }
      public void widgetSelected(SelectionEvent e) {
        Entry entry = createEntry(urlField.getText());
        AVTransportService service = currentZone.getMediaRendererDevice().getAvTransportService();
        try {
          service.addToQueue(entry);
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        } catch (UPNPResponseException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }
      
    };
    
    Button addToQueueButton = new Button(this, SWT.None);
    addToQueueButton.setImage(new Image(getDisplay(), ImageUtilities.loadImageDataFromSystemClasspath("plus.png")));
    addToQueueButton.addSelectionListener(addToQueueAction);
  }

  protected Entry createEntry(String text) {
    String res;
    if (text.startsWith("http:")) {
      // replace protocol part
      res = "x-rincon-mp3radio:" + text.substring(5);
    } else if (text.startsWith("//")) {
      res = "x-rincon-mp3radio:" + text;
    } else {
      res = "x-rincon-mp3radio://" + text;
    }
    return new Entry("URL:" + text, text, "URL:", "URL", "", "", "object.item.audioItem.audioBroadcast", res);
  }

  public void zoneSelectionChangedTo(ZonePlayer newSelection) {
    this.currentZone = newSelection;
  }

  
}
