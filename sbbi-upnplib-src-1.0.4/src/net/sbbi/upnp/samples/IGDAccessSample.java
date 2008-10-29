/*
 * ============================================================================
 *                 The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following  acknowledgment: "This product includes software
 *    developed by SuperBonBon Industries (http://www.sbbi.net/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "UPNPLib" and "SuperBonBon Industries" must not be
 *    used to endorse or promote products derived from this software without
 *    prior written permission. For written permission, please contact
 *    info@sbbi.net.
 *
 * 5. Products  derived from this software may not be called 
 *    "SuperBonBon Industries", nor may "SBBI" appear in their name, 
 *    without prior written permission of SuperBonBon Industries.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT,INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software  consists of voluntary contributions made by many individuals
 * on behalf of SuperBonBon Industries. For more information on 
 * SuperBonBon Industries, please see <http://www.sbbi.net/>.
 */
package net.sbbi.upnp.samples;

import net.sbbi.upnp.devices.*;
import net.sbbi.upnp.*;
import net.sbbi.upnp.messages.*;
import net.sbbi.upnp.services.*;

import java.util.Iterator;
import java.util.List;

/**
 * Sample class to access an UPNP device that implement the Internet Gateway Device specs
 * This sample will simply print the device external ip.
 * We assume tht an UPNP device that is implementing IGD is available on the network
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class IGDAccessSample {

  public static void main( String args[] ) {
    //DiscoveryAdvertisement.getInstance().notifyEvent(1, null);
    try {
      // search for an INTERNET_GATEWAY_DEVICE, we give 1500 ms for the device to respond
      UPNPRootDevice[] rootDevices = Discovery.discover( 1500, "urn:schemas-upnp-org:device:InternetGatewayDevice:1" );
      // looks like we have received a response now we need to look at a wan connection device
      // for our little search
      if ( rootDevices != null ) {
        // we take the first device found
        UPNPRootDevice rootDevice = rootDevices[0];
        System.out.println( "Plugged to device " + rootDevice.getDeviceType() + ", manufactured by " + rootDevice.getManufacturer() + " model " + rootDevice.getModelName() );
        // let s list all the available devices first
        List devices = rootDevice.getChildDevices();
        if ( devices != null ) {
          devices.add( rootDevice );
          for ( Iterator i = devices.iterator(); i.hasNext(); ) {
            UPNPDevice device = (UPNPDevice)i.next();
            System.out.println();
            System.out.println( "type " + device.getDeviceType() );
            if ( device.getDirectParent() != null ) {
              System.out.println( "parent type " + device.getDirectParent().getDeviceType() );
            }
            List deviceServices = device.getServices();
            if ( deviceServices != null ) {
              for ( Iterator iSrv = deviceServices.iterator(); iSrv.hasNext(); ) {
                UPNPService srv = (UPNPService)iSrv.next();
                System.out.println( "  service " + srv.getServiceType() + " at " + srv.getSCPDURL() );
                for ( Iterator itrActions = srv.getAvailableActionsName(); itrActions.hasNext(); ) {
                  System.out.println( "\t" + itrActions.next() );
                }
              }
            }
          }
        }

        List rootChildDevices = rootDevice.getChildDevices();
        System.out.println( "Child devices available : " + rootChildDevices );
        // we lookup for the wan connection device object now.
        UPNPDevice wanConnDevice = rootDevice.getChildDevice( "urn:schemas-upnp-org:device:WANConnectionDevice:1" );

        if ( wanConnDevice != null ) {
          // great this device is implemented
          System.out.println( "Found required device " + wanConnDevice.getDeviceType() );
          // now we need to lookup the service WANIPConnection for our litte action
          UPNPService wanIPSrv = wanConnDevice.getService( "urn:schemas-upnp-org:service:WANIPConnection:1" );

          if ( wanIPSrv != null ) {
            System.out.println( "Service " + wanIPSrv.getServiceType() + " found\n" );
            if ( wanIPSrv.getUPNPServiceAction( "GetExternalIPAddress" ) != null ) {
              // great our action is available ( normal this is required by the specs :o) )
              UPNPMessageFactory wanIPMsgFactory = UPNPMessageFactory.getNewInstance( wanIPSrv );
              ActionMessage externalIPAdrMsg = wanIPMsgFactory.getMessage( "GetExternalIPAddress" );
              List params = externalIPAdrMsg.getInputParameterNames();
              // now we list the needed inpu paramater for this message, should be empty
              System.out.println( "Action required input params:" );
              System.out.println( params );
              params = externalIPAdrMsg.getOutputParameterNames();
              // and now the output ( returned by the device after the message is sent ) params
              // normally only 1 value, the external IP address
              System.out.println( "Action returned values:" );
              System.out.println( params );
              // now we send the message to the UPNPDevice and we wait for a response.
              try {
                ActionResponse response = externalIPAdrMsg.service();
                System.out.println( "Message response values:" );
                for ( int i = 0; i < params.size(); i++ ) {
                  String param = (String)params.get( i );
                  System.out.println( param + "=" + response.getOutActionArgumentValue( param ) );
                }
              } catch ( UPNPResponseException ex ) {
                // can happen if device do not implement state variables queries
              }
              System.out.println( "Validity time remaining=" + rootDevice.getValidityTime() );
              // now let s try to query a state variable
              System.out.println( "Query PortMappingDescription state variable" );
              try {
                System.out.println( "Response=" + wanIPSrv.getUPNPServiceStateVariable( "PortMappingDescription" ).getValue() );
              } catch ( UPNPResponseException ex ) {
                // can happen if device do not implement state variables queries
              }
            }
          }
        }
      }
    } catch ( Exception ex ) {
      ex.printStackTrace( System.err );
    }
  }

}
