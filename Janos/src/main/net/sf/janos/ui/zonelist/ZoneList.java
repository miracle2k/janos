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
package net.sf.janos.ui.zonelist;

import java.util.ArrayList;
import java.util.List;

import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZoneListSelectionListener;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZonePlayerModel;
import net.sf.janos.model.ZonePlayerModelListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class ZoneList extends Composite implements ZonePlayerModelListener {
  
//  private static final Log LOG = LogFactory.getLog(ZoneList.class);
  
  private final List<ZoneListSelectionListener> selectionListeners = new ArrayList<ZoneListSelectionListener>();
  private final org.eclipse.swt.widgets.List zoneTable;
  private final ZonePlayerModel model;
  private int currentSelection = -1;

  public ZoneList(Composite parent, int style, SonosController controller) {
    super(parent, style);
    this.model = controller.getZonePlayerModel();

    setLayout(new FillLayout(SWT.VERTICAL));
    zoneTable = new org.eclipse.swt.widgets.List(this, SWT.SINGLE);
    zoneTable.setToolTipText("Drag a zone onto another to link them");
    
    zoneTable.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
        // Don't care
      }
      public void widgetSelected(SelectionEvent arg0) {
        int newSel = zoneTable.getSelectionIndex();
        if (newSel != currentSelection) {
          currentSelection = newSel;
          fireZoneSelectionChanged(model.get(newSel));
        }
      }
    });
    currentSelection = zoneTable.getSelectionIndex();
    
    controller.getZonePlayerModel().addZonePlayerModelListener(this);
    for (ZonePlayer zp : controller.getZonePlayerModel().getAllZones()) {
      addZonePlayerToDisplay(zp);
    }
  }
  
  public void zonePlayerAdded(final ZonePlayer dev, ZonePlayerModel model) {
    addZonePlayerToDisplay(dev);
  }
  
  private void addZonePlayerToDisplay(final ZonePlayer dev) {
    getDisplay().asyncExec(new Runnable() {
      public void run() {
//        Label deviceLabel = new Label(ZoneList.this, SWT.NONE);
//        deviceLabel.setText(dev.getRootDevice().getFriendlyName());
//        List<DeviceIcon> icons = dev.getRootDevice().getDeviceIcons(); //getChildDevice(SonosController.MEDIA_SERVER_DEVICE_TYPE).getDeviceIcons();
//        LOG.info ("device icons: " + icons);
//        if (icons != null && icons.size() > 0) {
//          URL location = icons.get(0).getUrl();
//          LOG.debug("Setting icon to "+ location);
//          InputStream stream = null;
//          try {
//            stream = location.openStream();
//            deviceLabel.setImage(new Image(getDisplay(), stream));
//          } catch (IOException e) {
//            // who cares
//          } finally {
//            if (stream != null) {
//              try {
//                stream.close();
//              } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//              }
//            }
//          }
//        }
        zoneTable.add(dev.getDevicePropertiesService().getZoneAttributes().getName());
        if (zoneTable.getItemCount() == 1) {
          // BUG for some reason, this call doesn't fire selection events. so we have to do it ourselfs!
          zoneTable.select(0);
          currentSelection=0;
          fireZoneSelectionChanged(model.get(currentSelection));
        }
        redraw();
      }
    });
  }
  
  public void zonePlayerRemoved(final ZonePlayer dev, ZonePlayerModel model) {
    getDisplay().asyncExec(new Runnable() {
      public void run() {
       zoneTable.remove(dev.getDevicePropertiesService().getZoneAttributes().getName());
       redraw();
      }
    });
  }
  
  public ZonePlayer getSelectedZone() {
    return model.get(currentSelection);
  }

  public void addSelectionListener(ZoneListSelectionListener l) {
    this.selectionListeners.add(l);
    if (currentSelection >= 0) {
      l.zoneSelectionChangedTo(model.get(currentSelection));
    }
  }
  
  public void removeSelectionListener(ZoneListSelectionListener l) {
    this.selectionListeners.remove(l);
  }
  
  protected void fireZoneSelectionChanged(ZonePlayer newSelection) {
    for (ZoneListSelectionListener l : this.selectionListeners) {
      l.zoneSelectionChangedTo(newSelection);
    }
  }
  
}
