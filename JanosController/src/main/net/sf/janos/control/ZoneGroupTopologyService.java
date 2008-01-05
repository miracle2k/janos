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

import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.services.UPNPService;
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
public class ZoneGroupTopologyService extends AbstractService implements ServiceEventHandler {
  
  private static final Log LOG = LogFactory.getLog(ZoneGroupTopologyService.class);


  private ZoneGroupState zoneGroup;
  public ZoneGroupTopologyService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_ZONE_GROUP_TOPOLOGY);
    registerServiceEventing(this);
  }

  // TODO move into sub class to hide from API
  public void handleStateVariableEvent(String varName, String newValue) {
    LOG.debug(varName + "=" + newValue);
    try {
      if (varName.equals("AvailableSoftwareUpdate")) {
      } else if (varName == "ZoneGroupState") {
        zoneGroup = ResultParser.getGroupStateFromResult(SonosController.getInstance(), newValue);
      } else if (varName == "ThirdPartyMediaServers") {

      } else if (varName == "AlarmRunSequence") {

      }
    } catch (SAXException e) {
      LOG.error("Could not parse state var: " + e);
    }
  }
  
  /**
   * @return an object representing the (perceived) group state.
   */
  public ZoneGroupState getGroupState() {
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
   * NOT IMPLEMENTED
   */
  public void checkForUpdate(/* TODO */) {
    /* TODO
     *     <actionList>
        <action>
            <name>CheckForUpdate</name>
            <argumentList>
                <argument>
                    <name>UpdateType</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_UpdateType</relatedStateVariable>
                </argument>
                <argument>
                    <name>CachedOnly</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_CachedOnly</relatedStateVariable>
                </argument>
                <argument>
                    <name>Version</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_Version</relatedStateVariable>
                </argument>
                <argument>
                    <name>UpdateItem</name>
                    <direction>out</direction>
                    <relatedStateVariable>A_ARG_TYPE_UpdateItem</relatedStateVariable>
                </argument>
            </argumentList>
        </action>

     */
  }

  /*
        <action>
            <name>BeginSoftwareUpdate</name>
            <argumentList>
                <argument>
                    <name>UpdateURL</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_UpdateURL</relatedStateVariable>
                </argument>
                <argument>
                    <name>Flags</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_UpdateFlags</relatedStateVariable>
                </argument>
            </argumentList>
        </action>
        <action>
            <name>ReportUnresponsiveDevice</name>
            <argumentList>
                <argument>
                    <name>DeviceUUID</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_MemberID</relatedStateVariable>
                </argument>
                <argument>
                    <name>DesiredAction</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_UnresponsiveDeviceActionType</relatedStateVariable>
                </argument>
            </argumentList>
        </action>
        <action>
            <name>ReportAlarmStartedRunning</name>
        </action>
    </actionList>
</scpd>
   */
}
