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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sbbi.upnp.DiscoveryAdvertisement;
import net.sbbi.upnp.DiscoveryEventHandler;
import net.sbbi.upnp.devices.UPNPRootDevice;

public class MyDiscoveryEventsHandler 
  implements DiscoveryEventHandler {

  private Map devices = new HashMap();

  public void eventSSDPAlive( String usn, String udn, 
                              String nt, String maxAge, 
                              URL location ) {
    System.out.println( "Device " + usn + " at " +
                        location + " of type " +
                        nt + " alive" );
    if ( devices.get( usn ) == null ) {
      // let's create the device
      UPNPRootDevice device = null;
      try {
        device = new UPNPRootDevice( location, maxAge );
        devices.put( usn, device );
        System.out.println( "Device " + usn + " added" );
        // and now let's play with the device..
      } catch ( MalformedURLException ex ) {
        // should never happen unless the UPNP devices
        // sends crappy URLs
      }
    }
  }

  public void eventSSDPByeBye( String usn, String udn, 
                               String nt ) {
    if ( devices.get( usn ) != null ) {
      devices.remove( usn );
      System.out.println( "Device " + usn + " leaves" );
    }
  }

  public static void main( String[] args ) throws IOException {
    // let's look for all root devices joining the network
    // ( "upnp:rootdevice" ) and set the events handler thread
    // as a non deamon thread so that the JVM does not stop
    // when the main static methods ends
    DiscoveryAdvertisement instance = DiscoveryAdvertisement.getInstance();
    MyDiscoveryEventsHandler handler = new MyDiscoveryEventsHandler();
    instance.setDaemon( false );
    instance.registerEvent( DiscoveryAdvertisement.EVENT_SSDP_ALIVE, 
                            "upnp:rootdevice", handler );
  }
}
