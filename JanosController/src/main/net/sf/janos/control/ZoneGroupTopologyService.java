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
import java.util.List;

import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;
import net.sf.janos.model.UnresponsiveDeviceActionType;
import net.sf.janos.model.UpdateType;
import net.sf.janos.model.ZoneGroupState;
import net.sf.janos.model.xml.ResultParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * Provides information and control over the group topology.
 * 
 * NOTE: this class is incomplete.
 * 
 * @author David Wheeler
 *
 */
public class ZoneGroupTopologyService extends AbstractService {
  
  private static final Log LOG = LogFactory.getLog(ZoneGroupTopologyService.class);

  private final List<ZoneGroupTopologyListener> listeners = new ArrayList<ZoneGroupTopologyListener>();
  
  private final ServiceEventHandler serviceEventHandler = new ServiceEventHandler() {
    public void handleStateVariableEvent(String varName, String newValue) {
      LOG.debug(varName + "=" + newValue);
      try {
        if (varName.equals("AvailableSoftwareUpdate")) {
        } else if (varName == "ZoneGroupState") {
          zoneGroup = ResultParser.getGroupStateFromResult(newValue);
          fireStateChanged();
        } else if (varName == "ThirdPartyMediaServers") {

        } else if (varName == "AlarmRunSequence") {

        }
      } catch (SAXException e) {
        LOG.error("Could not parse state var: " + e);
      }

    }
  };
  

  private ZoneGroupState zoneGroup = null;
  public ZoneGroupTopologyService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_ZONE_GROUP_TOPOLOGY);
    registerServiceEventing(serviceEventHandler);
  }

  /**
   * @return an object representing the (perceived) group state.
   */
  public ZoneGroupState getGroupState() {
	  
	  if (zoneGroup != null) {
		  return zoneGroup;
	  }
	  
	  // if we don't already have the zoneState, ask for it.
	  // TODO: figure out why this returns 401
//	  StateVariableMessage groupMessage = messageFactory.getStateVariableMessage( ZonePlayerConstants.SONOS_VARIABLE_ZONE_GROUP_STATE );
//	  try {
//		  StateVariableResponse resp = groupMessage.service();
//		  zoneGroup = ResultParser.getGroupStateFromResult(SonosController.getInstance(), resp.getStateVariableValue());
//	  } catch ( Exception ex ) {
//	    LOG.error("Could not retrieve zone group topology", ex);
//	  }
	  
	  return zoneGroup;
  }
  
  /*
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_UpdateType</name>
        <dataType>string</dataType>
        <allowedValueList>
           <allowedValue>All</allowedValue>
           <allowedValue>Software</allowedValue>
        </allowedValueList>
      </stateVariable>
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_CachedOnly</name>
        <dataType>boolean</dataType>
      </stateVariable>
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_UpdateItem</name>
        <dataType>string</dataType>
      </stateVariable>
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_UpdateURL</name>
        <dataType>string</dataType>
      </stateVariable>
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_UpdateFlags</name>
        <dataType>ui4</dataType>
      </stateVariable>
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_Version</name>
        <dataType>string</dataType>
      </stateVariable>
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_MemberID</name>
        <dataType>string</dataType>
      </stateVariable>
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_UnresponsiveDeviceActionType</name>
        <dataType>string</dataType>
        <allowedValueList>
           <allowedValue>Remove</allowedValue>
           <allowedValue>VerifyThenRemoveSystemwide</allowedValue>
        </allowedValueList>
      </stateVariable>
    </serviceStateTable> */
  
  /**
   * Checks for the availability of an update.
   * @param type
   * @param cachedOnly
   * @param version
   * @throws IOException
   * @throws UPNPResponseException
   */
  public String checkForUpdate(UpdateType type, boolean cachedOnly, String version) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("CheckForUpdate");
    message.setInputParameter("UpdateType", type);
    message.setInputParameter("CachedOnly", cachedOnly);
    message.setInputParameter("Version", version);
    ActionResponse response = message.service();
    return response.getOutActionArgumentValue("UpdateItem");
  }

  /**
   * 
   * @param updateUrl
   * @param updateFlags
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void beginSoftwareUpdate(String updateUrl, int updateFlags) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("BeginSoftwareUpdate");
    message.setInputParameter("UpdateURL", updateUrl);
    message.setInputParameter("Flags", updateFlags);
    message.service();
  }
  
  /**
   * 
   * @param deviceUuid
   * @param action
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void reportUnresponsiveDevice(String deviceUuid, UnresponsiveDeviceActionType action) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("ReportUnresponsiveDevice");
    message.setInputParameter("DeviceUUID", deviceUuid);
    message.setInputParameter("DesiredAction", action);
    message.service();
  }
  
  @Override
  public void dispose() {
    super.dispose();
    unregisterServiceEventing(serviceEventHandler);
  }
  /*
        <action>
            <name>ReportAlarmStartedRunning</name>
        </action>
   */
  
  private void fireStateChanged() {
	  synchronized (listeners) {
		  for (ZoneGroupTopologyListener l : listeners) {
			  l.zoneGroupTopologyChanged(getGroupState());
		  }
	  }
  }

  /**
   * registers the given listener to be notified of changes to the state of the AVTransportService. 
   * @param l
   */
  public void addZoneGroupTopologyListener(ZoneGroupTopologyListener l) {
	  synchronized (listeners) {
		  listeners.add(l);
	  }
  }

  /**
   * unregisters the given listener: no further notifications shall be recieved.
   * @param l
   */
  public void removeZoneGroupTopologyListener(ZoneGroupTopologyListener l) {
	  synchronized (listeners) {
		  listeners.remove(l);
	  }
  }
}
