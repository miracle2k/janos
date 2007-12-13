/*
 * Created on 21/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

import net.sbbi.upnp.services.UPNPService;

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
    <actionList>
        <action>
        <name>StartTransmissionToGroup</name>
        <argumentList>
            <argument>
                <name>CoordinatorID</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_MemberID</relatedStateVariable>
            </argument>
            <argument>
    <name>CurrentTransportSettings</name>
    <direction>out</direction>
    <relatedStateVariable>A_ARG_TYPE_TransportSettings</relatedStateVariable>
      </argument>
        </argumentList>
        </action>
        <action>
        <name>StopTransmissionToGroup</name>
        <argumentList>
            <argument>
                <name>CoordinatorID</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_MemberID</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>SetAudioInputAttributes</name>
        <argumentList>
            <argument>
                <name>DesiredName</name>
                <direction>in</direction>
                <relatedStateVariable>AudioInputName</relatedStateVariable>
            </argument>
            <argument>
                <name>DesiredIcon</name>
                <direction>in</direction>
                <relatedStateVariable>Icon</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>GetAudioInputAttributes</name>
        <argumentList>
            <argument>
                <name>CurrentName</name>
                <direction>out</direction>
                <relatedStateVariable>AudioInputName</relatedStateVariable>
            </argument>
            <argument>
                <name>CurrentIcon</name>
                <direction>out</direction>
                <relatedStateVariable>Icon</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>SetLineInLevel</name>
        <argumentList>
            <argument>
    <name>DesiredLeftLineInLevel</name>
    <direction>in</direction>
    <relatedStateVariable>LeftLineInLevel</relatedStateVariable>
      </argument>
            <argument>
    <name>DesiredRightLineInLevel</name>
    <direction>in</direction>
    <relatedStateVariable>RightLineInLevel</relatedStateVariable>
      </argument>
        </argumentList>
        </action>
        <action>
        <name>GetLineInLevel</name>
        <argumentList>
            <argument>
                <name>CurrentLeftLineInLevel</name>
                <direction>out</direction>
                <relatedStateVariable>LeftLineInLevel</relatedStateVariable>
            </argument>
            <argument>
                <name>CurrentRightLineInLevel</name>
                <direction>out</direction>
                <relatedStateVariable>RightLineInLevel</relatedStateVariable>
            </argument>
        </argumentList>
        </action>


    </actionList>
</scpd>
   */
}
