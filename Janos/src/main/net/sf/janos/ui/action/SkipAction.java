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
package net.sf.janos.ui.action;

import java.io.IOException;

import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.ApplicationContext;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.ui.SonosControllerShell;

import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
/**
 * A Selection Listener that when triggered skips to the next track
 * @author David Wheeler
 *
 */
public class SkipAction implements SelectionListener {

  public void widgetDefaultSelected(SelectionEvent e) {
    // do nothing
  }

  public void widgetSelected(SelectionEvent e) {
    SonosControllerShell controllerShell = ApplicationContext.getInstance().getShell();
    SonosController controller = ApplicationContext.getInstance().getController();
    ZonePlayer zone = controller.getCoordinatorForZonePlayer(controllerShell.getZoneList().getSelectedZone());
    try {
      AVTransportService avTransportService = zone.getMediaRendererDevice().getAvTransportService();
      avTransportService.next();
    } catch (IOException ex) {
      LogFactory.getLog(getClass()).error("Could not skip forward", ex);
    } catch (UPNPResponseException ex) {
      LogFactory.getLog(getClass()).error("Could not skip forward", ex);
    }
  }

}
