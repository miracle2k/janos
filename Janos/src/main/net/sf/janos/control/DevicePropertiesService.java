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

import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;
import net.sf.janos.model.ZoneAttributes;

/**
 * For querying the device properties of a zone player.
 * 
 * NOTE: this class is incomplete.
 * 
 * @author David Wheeler
 *
 */
public class DevicePropertiesService extends AbstractService {
  protected DevicePropertiesService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_DEVICE_PROPERTIES);
  }
  
  /**
   * @return a ZoneAttributes object with the name and icon for this zone
   *         player, or null if the request fails
   */
  public ZoneAttributes getZoneAttributes() {
    try {
      ActionMessage message = messageFactory.getMessage("GetZoneAttributes");
      ActionResponse response = message.service();
      return new ZoneAttributes(response.getOutActionArgumentValue("CurrentZoneName"), response.getOutActionArgumentValue("CurrentIcon"));
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  /* TODO
   * <?xml version="1.0" encoding="utf-8" ?>
<scpd xmlns="urn:schemas-upnp-org:service-1-0">
    <specVersion>
        <major>1</major>
        <minor>0</minor>
    </specVersion>
    <serviceStateTable>
        <stateVariable sendEvents="no">
            <name>HouseholdID</name>
            <dataType>string</dataType>
        </stateVariable>

        <stateVariable sendEvents="no">
            <name>LEDState</name>
            <dataType>string</dataType>
            <allowedValueList>
                <allowedValue>On</allowedValue>
                <allowedValue>Off</allowedValue>
            </allowedValueList>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>SerialNumber</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>SoftwareVersion</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>DisplaySoftwareVersion</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>HardwareVersion</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>IPAddress</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>MACAddress</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>CopyrightInfo</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>ExtraInfo</name>
            <dataType>string</dataType>
        </stateVariable>
    </serviceStateTable>
    <actionList>
        <action>
        <name>SetLEDState</name>
        <argumentList>
            <argument>
                <name>DesiredLEDState</name>
                <direction>in</direction>
                <relatedStateVariable>LEDState</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>GetLEDState</name>
        <argumentList>
            <argument>
                <name>CurrentLEDState</name>
                <direction>out</direction>
                <relatedStateVariable>LEDState</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>SetInvisible</name>
        <argumentList>
            <argument>
                <name>DesiredInvisible</name>
                <direction>in</direction>
                <relatedStateVariable>Invisible</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>GetInvisible</name>
        <argumentList>
            <argument>
                <name>CurrentInvisible</name>
                <direction>out</direction>
                <relatedStateVariable>Invisible</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>SetZoneAttributes</name>
        <argumentList>
            <argument>
                <name>DesiredZoneName</name>
                <direction>in</direction>
                <relatedStateVariable>ZoneName</relatedStateVariable>
            </argument>
            <argument>
                <name>DesiredIcon</name>
                <direction>in</direction>
                <relatedStateVariable>Icon</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>GetZoneAttributes</name>
        <argumentList>
            <argument>
                <name>CurrentZoneName</name>
                <direction>out</direction>
                <relatedStateVariable>ZoneName</relatedStateVariable>
            </argument>
            <argument>
                <name>CurrentIcon</name>
                <direction>out</direction>
                <relatedStateVariable>Icon</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>GetHouseholdID</name>
        <argumentList>
            <argument>
                <name>CurrentHouseholdID</name>
                <direction>out</direction>
                <relatedStateVariable>HouseholdID</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>GetZoneInfo</name>
        <argumentList>
            <argument>
                <name>SerialNumber</name>
                <direction>out</direction>
                <relatedStateVariable>SerialNumber</relatedStateVariable>
            </argument>
            <argument>
                <name>SoftwareVersion</name>
                <direction>out</direction>
                <relatedStateVariable>SoftwareVersion</relatedStateVariable>
            </argument>
            <argument>
                <name>DisplaySoftwareVersion</name>
                <direction>out</direction>
                <relatedStateVariable>DisplaySoftwareVersion</relatedStateVariable>
            </argument>
            <argument>
                <name>HardwareVersion</name>
                <direction>out</direction>
                <relatedStateVariable>HardwareVersion</relatedStateVariable>
            </argument>
            <argument>
                <name>IPAddress</name>
                <direction>out</direction>
                <relatedStateVariable>IPAddress</relatedStateVariable>
            </argument>
            <argument>
                <name>MACAddress</name>
                <direction>out</direction>
                <relatedStateVariable>MACAddress</relatedStateVariable>
            </argument>
            <argument>
                <name>CopyrightInfo</name>
                <direction>out</direction>
                <relatedStateVariable>CopyrightInfo</relatedStateVariable>
            </argument>
            <argument>
                <name>ExtraInfo</name>
                <direction>out</direction>
                <relatedStateVariable>ExtraInfo</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
    </actionList>
</scpd>
   */

  public void handleStateVariableEvent(String varName, String newValue) {
    /*
     *  <stateVariable sendEvents="yes">
            <name>SettingsReplicationState</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="yes">
            <name>ZoneName</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="yes">
            <name>Icon</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="yes">
            <name>Invisible</name>
            <dataType>boolean</dataType>
        </stateVariable>
        <stateVariable sendEvents="yes">
            <name>IsZoneBridge</name>
            <dataType>boolean</dataType>
        </stateVariable>
     */
  }
}
