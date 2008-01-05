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
 * Provides information about the zone player system.
 * 
 * NOTE: this class is incomplete.
 * 
 * @author David Wheeler
 * 
 */
public class SystemPropertiesService extends AbstractService {

  public SystemPropertiesService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_SYSTEM_PROPERTIES);
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
            <name>A_ARG_TYPE_VariableName</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
            <name>A_ARG_TYPE_VariableStringValue</name>
            <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
           <name>A_ARG_TYPE_AccountType</name>
           <dataType>ui4</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
           <name>A_ARG_TYPE_AccountID</name>
           <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
           <name>A_ARG_TYPE_AccountPassword</name>
           <dataType>string</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
           <name>A_ARG_TYPE_IsExpired</name>
           <dataType>boolean</dataType>
        </stateVariable>
        <stateVariable sendEvents="no">
           <name>A_ARG_TYPE_StubsCreated</name>
           <dataType>string</dataType>
        </stateVariable>
    </serviceStateTable>
    <actionList>
        <action>
        <name>SetString</name>
        <argumentList>
            <argument>
                <name>VariableName</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_VariableName</relatedStateVariable>
            </argument>
            <argument>
                <name>StringValue</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_VariableStringValue</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>GetString</name>
        <argumentList>
            <argument>
                <name>VariableName</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_VariableName</relatedStateVariable>
            </argument>
            <argument>
                <name>StringValue</name>
                <direction>out</direction>
                <relatedStateVariable>A_ARG_TYPE_VariableStringValue</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>Remove</name>
        <argumentList>
            <argument>
                <name>VariableName</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_VariableName</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>ProvisionTrialAccount</name>
        <argumentList>
            <argument>
                <name>AccountType</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountType</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>ProvisionCredentialedTrialAccount</name>
        <argumentList>
            <argument>
                <name>AccountType</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountType</relatedStateVariable>
            </argument>
            <argument>
                <name>AccountID</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountID</relatedStateVariable>
            </argument>
            <argument>
                <name>AccountPassword</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountPassword</relatedStateVariable>
            </argument>
            <argument>
                <name>IsExpired</name>
                <direction>out</direction>
                <relatedStateVariable>A_ARG_TYPE_IsExpired</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>MigrateTrialAccount</name>
        <argumentList>
            <argument>
                <name>TargetAccountType</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountType</relatedStateVariable>
            </argument>
            <argument>
                <name>TargetAccountID</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountID</relatedStateVariable>
            </argument>
            <argument>
                <name>TargetAccountPassword</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountPassword</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>AddAccount</name>
        <argumentList>
            <argument>
                <name>AccountType</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountType</relatedStateVariable>
            </argument>
            <argument>
                <name>AccountID</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountID</relatedStateVariable>
            </argument>
            <argument>
                <name>AccountPassword</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountPassword</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>RemoveAccount</name>
        <argumentList>
            <argument>
                <name>AccountType</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountType</relatedStateVariable>
            </argument>
            <argument>
                <name>AccountID</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountID</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>EditAccountPassword</name>
        <argumentList>
            <argument>
                <name>AccountType</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountType</relatedStateVariable>
            </argument>
            <argument>
                <name>AccountID</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountID</relatedStateVariable>
            </argument>
            <argument>
                <name>NewAccountPassword</name>
                <direction>in</direction>
                <relatedStateVariable>A_ARG_TYPE_AccountPassword</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
        <action>
        <name>CreateStubAccounts</name>
        <argumentList>
            <argument>
                <name>StubsCreated</name>
                <direction>out</direction>
                <relatedStateVariable>A_ARG_TYPE_StubsCreated</relatedStateVariable>
            </argument>
        </argumentList>
        </action>
    </actionList>
</scpd>
   */
  
  public void handleStateVariableEvent(String varName, String newValue) {
    // TODO Auto-generated method stub
    
  }
}
