/*
 * Created on 21/10/2007
 * By David Wheeler
 * Student ID: 3691615
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
      </stateVariable>
      <stateVariable sendEvents="yes">
        <name>GroupCoordinatorIsLocal</name>
        <dataType>boolean</dataType>
      </stateVariable>
      <stateVariable sendEvents="yes">
        <name>LocalGroupUUID</name>
        <dataType>string</dataType>
      </stateVariable>
    </serviceStateTable>
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
