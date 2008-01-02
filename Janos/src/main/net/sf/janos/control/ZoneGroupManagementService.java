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

import net.sbbi.upnp.services.UPNPService;

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

  /*  </serviceStateTable>
    <actionList>
  <action>
    <name>AddMember</name>
    <argumentList>
        <argument>
      <name>MemberID</name>
      <direction>in</direction>
      <relatedStateVariable>A_ARG_TYPE_MemberID</relatedStateVariable>
        </argument>
        <argument>
      <name>CurrentTransportSettings</name>
      <direction>out</direction>
      <relatedStateVariable>A_ARG_TYPE_TransportSettings</relatedStateVariable>
        </argument>
        <argument>
      <name>GroupUUIDJoined</name>
      <direction>out</direction>
      <relatedStateVariable>LocalGroupUUID</relatedStateVariable>
        </argument>
    </argumentList>
        </action>
  <action>
    <name>RemoveMember</name>
    <argumentList>
        <argument>
      <name>MemberID</name>
      <direction>in</direction>
      <relatedStateVariable>A_ARG_TYPE_MemberID</relatedStateVariable>
        </argument>
    </argumentList>
        </action>
  <action>
    <name>ReportTrackBufferingResult</name>
    <argumentList>
        <argument>
      <name>MemberID</name>
      <direction>in</direction>
      <relatedStateVariable>A_ARG_TYPE_MemberID</relatedStateVariable>
        </argument>
        <argument>
      <name>ResultCode</name>
      <direction>in</direction>
      <relatedStateVariable>A_ARG_TYPE_BufferingResultCode</relatedStateVariable>
        </argument>
    </argumentList>
        </action>
    </actionList>
</scpd>
   */
}
