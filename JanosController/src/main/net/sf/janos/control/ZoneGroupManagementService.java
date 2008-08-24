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
import net.sf.janos.model.GroupJoinResponse;

/**
 * Provides management of the zone player's group.
 * 
 * NOTE: this class is incomplete
 * 
 * @author David Wheeler
 *
 */
public class ZoneGroupManagementService extends AbstractService {

  public ZoneGroupManagementService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_ZONE_GROUP_MANAGEMENT);
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
      <stateVariable sendEvents="no">
        <name>A_ARG_TYPE_BufferingResultCode</name>
        <dataType>i4</dataType>
      </stateVariable> */
  public void handleStateVariableEvent(String varName, String newValue) {
    /*
     *<stateVariable sendEvents="yes">
        <name>GroupCoordinatorIsLocal</name>
        <dataType>boolean</dataType>
      </stateVariable>
      <stateVariable sendEvents="yes">
        <name>LocalGroupUUID</name>
        <dataType>string</dataType>
      </stateVariable>
      */
    
  }


  /**
   * Adds the Zone Player with the given member ID TODO to what?
   * @param memberId
   * @return
   * @throws IOException
   * @throws UPNPResponseException
   */
  public GroupJoinResponse addMember(String memberId) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("AddMember");
    message.setInputParameter("MemberID", memberId);
    ActionResponse response = message.service();
    return new GroupJoinResponse(response.getOutActionArgumentValue("CurrentTransportSettings"), 
        response.getOutActionArgumentValue("GroupUUIDJoined"));
  }
  
  /**
   * Removes the given member
   * @param memberId
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void removeMember(String memberId) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("RemoveMember");
    message.setInputParameter("MemberID", memberId);
    message.service();
  }
  
  /**
   * 
   * @param memberId
   * @param resultCode
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void reportTrackBufferingResult(String memberId, int resultCode) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("ReportTrackBufferingResult");
    message.setInputParameter("MemberID", memberId);
    message.setInputParameter("ResultCode", resultCode);
    message.service();
  }
}
