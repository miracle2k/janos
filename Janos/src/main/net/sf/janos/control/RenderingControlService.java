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
package net.sf.janos.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;
import net.sf.janos.model.xml.ResultParser;
import net.sf.janos.model.xml.RenderingControlEventHandler.EventType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * Provides control over the rendering (playing) of media (audio).
 * 
 * NOTE: this class is incomplete
 * 
 * @author David Wheeler
 * 
 */
public class RenderingControlService extends AbstractService implements ServiceEventHandler {
  
  private static final Log LOG = LogFactory.getLog(RenderingControlService.class);

  /**
   * The known state of the ZonePlayer rendering control service.
   */
  private final Map<EventType, String> state = new HashMap<EventType, String>();

  /**
   * The listeners to be notified when the state changes.
   */
  private final List<ZonePlayerServiceListener<RenderingControlService>> listeners = new ArrayList<ZonePlayerServiceListener<RenderingControlService>>();
  
  protected RenderingControlService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_RENDERING_CONTROL);
    try {
      refreshServiceEventing(DEFAULT_EVENT_PERIOD, this);
      // TODO refresh eventing each ...s
    } catch (IOException e) {
      LOG.error("Could not register service eventing: ", e);
    }
  }
  
//  public boolean getMute() {
//    ActionMessage msg = messageFactory.getMessage(GET_MUTE_ACTION);
//    msg.setInputParameter(parameterName, parameterValue)
//  }
  
  /**
   * Sets the volume to the given %. Must be between 0-100 inclusive.
   * 
   * @param vol
   *          the new volume as a percentage of maximum volume.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void setVolume(int vol) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("SetVolume");
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("Channel", "Master"); // can also be LF or RF
    message.setInputParameter("DesiredVolume", vol);
    message.service();
  }
  
  /**
   * @return the current volume, as a percentage of maximum volume.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public int getVolume() throws IOException, UPNPResponseException {
    String volume_master = state.get(EventType.VOLUME_MASTER);
    if (volume_master == null) {
      ActionMessage message = messageFactory.getMessage("GetVolume");
      message.setInputParameter("InstanceID", 0);
      message.setInputParameter("Channel", "Master"); // can also be LF or RF
      ActionResponse resp = message.service();
      state.put(EventType.VOLUME_MASTER, resp.getOutActionArgumentValue("CurrentVolume"));
    }
    return Integer.parseInt(state.get(EventType.VOLUME_MASTER));
  }
  
  public void handleStateVariableEvent(String varName, String newValue) {
    /*
     * EG: 
     * <Event xmlns="urn:schemas-upnp-org:metadata-1-0/RCS/">
     *   <InstanceID val="0">
     *     <Volume channel="Master" val="58"/>
     *     <Volume channel="LF" val="100"/>
     *     <Volume channel="RF" val="100"/>
     *     <Mute channel="Master" val="0"/>
     *     <Mute channel="LF" val="0"/>
     *     <Mute channel="RF" val="0"/>
     *     <Bass val="10"/>
     *     <Treble val="0"/>
     *     <Loudness channel="Master" val="1"/>
     *     <OutputFixed val="0"/>
     *     <PresetNameList>FactoryDefaults</PresetNameList>
     *   </InstanceID>
     * </Event>
     */
    LOG.debug("received event " + varName + ": " + newValue);
    try {
      Map<EventType, String> changes = ResultParser.parseRenderingControlEvent(newValue);
      state.putAll(changes);
      fireChangeEvent(changes.keySet());
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void fireChangeEvent(Set<EventType> events) {
    for (ZonePlayerServiceListener<RenderingControlService> l : listeners) {
      l.valuesChanged(events, this);
    }
  }
  
  /**
   * Adds a listener to be notified when notifications are received from the ZonePlayer.
   * @param l
   */
  public void addListener(ZonePlayerServiceListener<RenderingControlService> l) {
    listeners.add(l);
  }
  
  /**
   * Removes a listener.
   * @param l
   */
  public void removeListener(ZonePlayerServiceListener<RenderingControlService> l) {
    listeners.remove(l);
  }

}
