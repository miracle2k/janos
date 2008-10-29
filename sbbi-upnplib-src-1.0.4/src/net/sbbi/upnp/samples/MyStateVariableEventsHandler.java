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

import net.sbbi.upnp.Discovery;
import net.sbbi.upnp.ServicesEventing;
import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.devices.UPNPDevice;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.services.UPNPService;

public class MyStateVariableEventsHandler 
  implements ServiceEventHandler {

  public void handleStateVariableEvent( String varName, String newValue ) {
    System.out.println( "State variable " + varName +
                        " changed to " + newValue );
  }
    
  public static void main( String[] args ) {
    
    ServicesEventing instance = ServicesEventing.getInstance();
    MyStateVariableEventsHandler handler = new MyStateVariableEventsHandler();
    instance.setDaemon( false );
    // let's find a device
    UPNPRootDevice[] devices = null;
    try {
      devices = Discovery.discover();
    } catch ( IOException ex ) {
      ex.printStackTrace( System.err );
    }
    if ( devices != null ) {
      UPNPDevice firstDevice = (UPNPDevice)devices[0].getChildDevices()
                                                     .iterator().next();
      UPNPService firstService = (UPNPService)firstDevice.getServices()
                                                         .iterator().next();
      try {
        int duration = instance.register( firstService, handler, -1 );
        if ( duration != -1 && duration != 0 ) {
          System.out.println( "State variable events registered for " + duration + " ms" );
        } else if ( duration == 0 ) {
          System.out.println( "State variable events registered for infinite ms" );
        }
        try {
          Thread.sleep( 5000 );
        } catch ( InterruptedException ex ) {
          
        }
        instance.unRegister( firstService, handler );
      } catch ( IOException ex ) {
        ex.printStackTrace( System.err );
        // comm error during registration with device such as timeoutException
      }
    } else {
      System.out.println( "Unable to find devices" );
    }
  }
}