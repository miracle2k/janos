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
import net.sf.janos.model.AudioInputAttributes;
import net.sf.janos.model.LineLevel;

/**
 * For controlling the audio in service of a zone player. 
 * 
 * NOTE: this class is incomplete.
 * 
 * @author David Wheeler
 *
 */
public class AudioInService extends AbstractService {

  public AudioInService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_AUDIO_IN);
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
        <name>A_ARG_TYPE_MemberID</name>
        <dataType>string</dataType>
      </stateVariable>
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_TransportSettings</name>
        <dataType>string</dataType>
      </stateVariable>
      <stateVariable sendEvents="yes">
        <name>AudioInputName</name>
        <dataType>string</dataType>
      </stateVariable>
      <stateVariable sendEvents="yes">
        <name>Icon</name>
        <dataType>string</dataType>
      </stateVariable>
      <stateVariable sendEvents="yes">
        <name>LineInConnected</name>
        <dataType>boolean</dataType>
      </stateVariable>
      <stateVariable sendEvents="yes">
        <name>LeftLineInLevel</name>
        <dataType>i4</dataType>
      </stateVariable>
      <stateVariable sendEvents="yes">
        <name>RightLineInLevel</name>
        <dataType>i4</dataType>
      </stateVariable>
    </serviceStateTable>
    */
  
  /**
   * TODO return a TransportSettings object.
   * @param groupId The group to which the transmission should be.
   * @throws UPNPResponseException 
   * @throws IOException 
   */
  public String startTransmissionToGroup(String groupId) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("StartTransmissionToGroup");
    message.setInputParameter("CoordinatorID", groupId);
    ActionResponse response = message.service();
    return response.getOutActionArgumentValue("CurrentTransportSettings");
  }
  
  /**
   * 
   * @param groupId
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void stopTransmissionToGroup(String groupId) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("StopTransmissionToGroup");
    message.setInputParameter("CoordinatorID", groupId);
    message.service();
  }
  
  /**
   * Sets the attributes for the audio input.
   * @param name the name of the audio input
   * @param iconUri a URI defining an icon for the audio input
   * @throws UPNPResponseException 
   * @throws IOException 
   */
  public void setAudioInputAttributes(String name, String iconUri) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("SetAudioInputAttributes");
    message.setInputParameter("DesiredName", name);
    message.setInputParameter("DesiredIcon", iconUri);
    message.service();
  }
  
  /**
   * 
   * @return an object containing the attributes of the audio input service.
   * @throws UPNPResponseException
   * @throws IOException
   */
  public AudioInputAttributes getAudioInputAttributes() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetAudioInputAttributes");
    ActionResponse response = message.service();
    return new AudioInputAttributes(response.getOutActionArgumentValue("AudioInputName"),
        response.getOutActionArgumentValue("Icon"));
  }
  
  /**
   * Sets the level for each component of the line in. 
   * @param leftLevel
   * @param rightLevel
   * @throws UPNPResponseException 
   * @throws IOException 
   */
  public void setLineInLevel(int leftLevel, int rightLevel) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("SetLineInLevel");
    message.setInputParameter("DesiredLeftLineInLevel", leftLevel);
    message.setInputParameter("DesiredRightLineInLevel", rightLevel);
    message.service();
  }
  
  /**
   * @return the current line levels for the audio input
   * @throws UPNPResponseException 
   * @throws IOException 
   */
  public LineLevel getLineInLevel() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetLineInLevel");
    ActionResponse response = message.service();
    return new LineLevel(Integer.parseInt(response.getOutActionArgumentValue("CurrentLeftLineInLevel")), 
        Integer.parseInt(response.getOutActionArgumentValue("CurrentRightLineInLevel")));
  }
  
  public void handleStateVariableEvent(String varName, String newValue) {
    // TODO Auto-generated method stub
    
  }
}
