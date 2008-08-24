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
import net.sf.janos.model.ZoneInfo;

/**
 * For querying the device properties of a zone player. This is a Sonos specific
 * class.
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
  
  /**
   * Sets the state of the Sonos Device's LED to the given state.
   * 
   * @param ledEnabled
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void setLEDState(boolean ledEnabled) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("SetLEDState");
    message.setInputParameter("DesiredLEDState", ledEnabled?"On":"Off");
    message.service();
  }
  
  /**
   * Gets the current state of the Sonos Device's LED.
   * @return
   * @throws IOException
   * @throws UPNPResponseException
   */
  public boolean getLEDState() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetLEDState");
    ActionResponse response = message.service();
    String state = response.getOutActionArgumentValue("CurrentLEDState");
    return "On".equals(state);
  }
  
  /**
   * Sets or unsets the Sonos Device to run in invisible mode.
   * @param isInvisible
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void setInvisible(boolean isInvisible) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("SetInvisible");
    message.setInputParameter("DesiredInvisible", isInvisible);
    message.service();
  }
  
  /**
   * @return <code>true</code> if the Sonos Device is currently operating in
   *         Invisible mode, or <code>false</code> otherwise.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public boolean getInvisible() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetInvisible");
    ActionResponse response = message.service();
    return Boolean.parseBoolean(response.getOutActionArgumentValue("CurrentInvisible"));
  }
  
  /**
   * Applies the given {@link ZoneAttributes} to this Sonos Device.
   * @param atts
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void setZoneAttributes(ZoneAttributes atts) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("SetZoneAttributes");
    message.setInputParameter("DesiredZoneName", atts.getName());
    message.setInputParameter("DesiredIcon", atts.getIcon());
    message.service();
  }
  
//  public ZoneAttributes getZoneAttributes() {
//    ActionMessage message = messageFactory.getMessage("GetZoneAttributes");
//    ActionResponse response = message.service();
//    String name = response.getOutActionArgumentValue("DesiredZoneName");
//    String icon = response.getOutActionArgumentValue("DesiredIcon");
//    return new ZoneAttributes(name, icon);
//  }
  
  /**
   * @return A string representation of the HouseholdID for this Sonos Device.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public String getHouseholdID() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetHouseholdID");
    ActionResponse response = message.service();
    return response.getOutActionArgumentValue("HouseholdID");
  }
  
  /**
   * @return a ZoneInfo object containing the information for this Sonos Device.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public ZoneInfo getZoneInfo() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetZoneInfo");
    ActionResponse response = message.service();
    
    return new ZoneInfo(response.getOutActionArgumentValue("SerialNumber"),
        response.getOutActionArgumentValue("SoftwareVersion"),
        response.getOutActionArgumentValue("DisplaySoftwareVersion"),
        response.getOutActionArgumentValue("HardwareVersion"),
        response.getOutActionArgumentValue("IPAddress"),
        response.getOutActionArgumentValue("MACAddress"),
        response.getOutActionArgumentValue("CopyrightInfo"),
        response.getOutActionArgumentValue("ExtraInfo"));
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
