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

import java.io.IOException;
import java.net.InetAddress;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;

/**
 * This will use the net.sbbi.upnp.impls.InternetGatewayDevice class to discover
 * IGD devices on the nework via the getDevices() method and try to open and then close
 * port 9090 on the IGD device.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class IGDPortsTest {

  public final static void main( String args[] ) {
    
    int discoveryTiemout = 5000; // 5 secs
    try {
      InternetGatewayDevice[] IGDs = InternetGatewayDevice.getDevices( discoveryTiemout );
      if ( IGDs != null ) {
        for ( int i = 0; i < IGDs.length; i++ ) {
          InternetGatewayDevice testIGD = IGDs[i];
          System.out.println( "Found device " + testIGD.getIGDRootDevice().getModelName() );
          System.out.println( "NAT table size is " + testIGD.getNatTableSize() );
          // now let's open the port
          String localHostIP = InetAddress.getLocalHost().getHostAddress();
          boolean mapped = testIGD.addPortMapping( "Some mapping description", 
                                                   null, 9090, 9090,
                                                   localHostIP, 0, "TCP" );
          if ( mapped ) {

            System.out.println( "Port 9090 mapped to " + localHostIP );
            System.out.println( "Current mappings count is " + testIGD.getNatMappingsCount() );
            // checking on the device
            ActionResponse resp = testIGD.getSpecificPortMappingEntry( null, 9090, "TCP" );
            if ( resp != null ) {
              System.out.println( "Port 9090 mapping confirmation received from device" );
            }
            // and now close it
            boolean unmapped = testIGD.deletePortMapping( null, 9090, "TCP" );
            if ( unmapped ) {
              System.out.println( "Port 9090 unmapped" );
            }
          }
        }
      } else {
        System.out.println( "Unable to find IGD on your network" );
      }
    } catch ( IOException ex ) {
      System.err.println( "IOException occured during discovery or ports mapping " +
                          ex.getMessage() );
    } catch( UPNPResponseException respEx ) {
      System.err.println( "UPNP device unhappy " + respEx.getDetailErrorCode() + 
                          " " + respEx.getDetailErrorDescription() );
    }
  }
}
