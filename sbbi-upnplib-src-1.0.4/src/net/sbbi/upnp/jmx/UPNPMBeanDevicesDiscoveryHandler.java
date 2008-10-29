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
package net.sbbi.upnp.jmx;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sbbi.upnp.Discovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle UPNP discovery mechanism on UPNPMBeanDevice
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPMBeanDevicesDiscoveryHandler implements Runnable {
  
  private final static Log log = LogFactory.getLog( UPNPMBeanDevicesDiscoveryHandler.class );
  
  private final static Map instances = new HashMap();

  private Set handledDevices = new HashSet();
  private java.net.MulticastSocket skt;
  private boolean isRunning = false;
  private boolean isRunningSSDPDaemon = false;
  
  private InetSocketAddress bindAddress;
  
  public final static UPNPMBeanDevicesDiscoveryHandler getInstance( InetSocketAddress bindAddress ) {
    String key = bindAddress.toString();
    synchronized( instances ) {
      UPNPMBeanDevicesDiscoveryHandler handler = (UPNPMBeanDevicesDiscoveryHandler)instances.get( key );
      if ( handler == null ) {
        handler = new UPNPMBeanDevicesDiscoveryHandler( bindAddress );
        instances.put( key, handler );
      }
      return handler;
    }
  }
  
  private UPNPMBeanDevicesDiscoveryHandler( InetSocketAddress bindAddress ) {
    this.bindAddress = bindAddress;
  }
  
  protected void addUPNPMBeanDevice( UPNPMBeanDevice rootDevice ) throws IOException {
    if ( !rootDevice.isRootDevice() ) return;
    synchronized( handledDevices ) {
      for ( Iterator i = handledDevices.iterator(); i.hasNext(); ) {
        UPNPMBeanDevice registred = (UPNPMBeanDevice)i.next();
        if ( registred.getDeviceType().equals( rootDevice.getDeviceType() ) && 
             registred.getUuid().equals( rootDevice.getUuid() ) ) {
          // API Use error
          throw new RuntimeException( "An UPNPMBeanDevice object of type " + rootDevice.getDeviceType() + 
                                      " with uuid " + rootDevice.getUuid() +
                                      " is already registred within this class, use a different UPNPMBeanDevice internalId" );
        }
      }
      if ( handledDevices.size() == 0 ) {
        Thread runner = new Thread( this, "UPNPMBeanDeviceDiscoveryHandler " + bindAddress.toString() );
        runner.setDaemon( true );
        runner.start();
        
        SSDPAliveBroadcastMessageSender sender = new SSDPAliveBroadcastMessageSender( handledDevices );
        Thread runner2 = new Thread( sender, "SSDPAliveBroadcastMessageSender " + bindAddress.toString() );
        runner2.setDaemon( true );
        runner2.start();

      }
      sendHello( rootDevice  );
      handledDevices.add( rootDevice );
    }
  }
  
  protected void removeUPNPMBeanDevice( UPNPMBeanDevice rootDevice ) throws IOException {
    if ( !rootDevice.isRootDevice() ) return;
    synchronized( handledDevices ) {
      if ( handledDevices.contains( rootDevice ) ) {
        handledDevices.remove( rootDevice );
        sendByeBye( rootDevice );
        if ( handledDevices.size() == 0 && isRunning ) {
          isRunning = false;
          isRunningSSDPDaemon = false;
          skt.close();
        }
      }
    }
  }
  
  private void sendHello( UPNPMBeanDevice dv ) throws IOException {
    InetAddress group = InetAddress.getByName( "239.255.255.250" );
    java.net.MulticastSocket multi = new java.net.MulticastSocket( bindAddress.getPort() );
    multi.setInterface( bindAddress.getAddress() );
    multi.setTimeToLive( dv.getSSDPTTL() );

    List packets = getReplyMessages( dv, true, dv.getSSDPAliveDelay() );
    for ( int i = 0; i < packets.size(); i++ ) {
      String packet = (String)packets.get( i );
      if ( log.isDebugEnabled() ) log.debug( "Sending ssdp alive message on 239.255.255.250:1900 multicast address:\n" + packet.toString() );
      byte[] pk = packet.getBytes();
      multi.send( new DatagramPacket( pk, pk.length, group, 1900 ) );
    }
    multi.close();
  }
  
  private void sendByeBye( UPNPMBeanDevice dv ) throws IOException {
    InetAddress group = InetAddress.getByName( "239.255.255.250" );
    java.net.MulticastSocket multi = new java.net.MulticastSocket( bindAddress.getPort() );
    multi.setInterface( bindAddress.getAddress() );
    multi.setTimeToLive( dv.getSSDPTTL() );
    
    List packets = getByeByeReplyMessages( dv );
    for ( int i = 0; i < packets.size(); i++ ) {
      String packet = (String)packets.get( i );
      if ( log.isDebugEnabled() ) log.debug( "Sending ssdp:byebye message on 239.255.255.250:1900 multicast address:\n" + packet.toString() );
      byte[] pk = packet.getBytes();
      multi.send( new DatagramPacket( pk, pk.length, group, 1900 ) );
    }
    multi.close();
  }
  
  private List getReplyMessages( UPNPMBeanDevice rootDevice, boolean ssdpAliveMsg, int maxAge ) {
    // TODO handle custom NT and ST
    // TODO create a thread to dispatch ssdp:alive messages
    List rtrVal = new ArrayList();
    
    StringBuffer basePacket = new StringBuffer();
    StringBuffer packet = null;
    if ( ssdpAliveMsg ) {
      basePacket.append( "NOTIFY * HTTP/1.1\r\n" );
      basePacket.append( "HOST: 239.255.255.250:1900\r\n" );
    } else {
      basePacket.append( "HTTP/1.1 200 OK\r\n" );
    }
    basePacket.append( "CACHE-CONTROL: max-age = " ).append( maxAge ).append( "\r\n" );
    basePacket.append( "LOCATION: " ).append( rootDevice.getLocation() ).append( "\r\n" );
    basePacket.append( "SERVER: " ).append( UPNPMBeanDevice.IMPL_NAME ).append( "\r\n" );
    
    // 3 messages for the root device
    packet = new StringBuffer( basePacket.toString() );
    if ( ssdpAliveMsg ) {
      packet.append( "NT: uuid:" ).append( rootDevice.getUuid() ).append( "\r\n" );
      packet.append( "NTS: ssdp:alive\r\n" );
    } else {
      packet.append( "ST: uuid:" ).append( rootDevice.getUuid() ).append( "\r\n" );
      packet.append( "EXT:\r\n" );
    }
    packet.append( "USN: uuid:" ).append( rootDevice.getUuid() ).append( "\r\n\r\n" );
    rtrVal.add( packet.toString() );
    
    packet = new StringBuffer( basePacket.toString() );
    if ( ssdpAliveMsg ) {
      packet.append( "NT: " ).append( rootDevice.getDeviceType() ).append( "\r\n" );
      packet.append( "NTS: ssdp:alive\r\n" );
    } else {
      packet.append( "ST: " ).append( rootDevice.getDeviceType() ).append( "\r\n" );
      packet.append( "EXT:\r\n" );
    }
    packet.append( "USN: uuid:" ).append( rootDevice.getUuid() ).append( "::" ).append( rootDevice.getDeviceType() ).append( "\r\n\r\n" );
    rtrVal.add( packet.toString() );
    
    packet = new StringBuffer( basePacket.toString() );
    if ( ssdpAliveMsg ) {
      packet.append( "NT: upnp:rootdevice\r\n" );
      packet.append( "NTS: ssdp:alive\r\n" );
    } else {
      packet.append( "ST: upnp:rootdevice\r\n" );
      packet.append( "EXT:\r\n" );
    }
    packet.append( "USN: uuid:" ).append( rootDevice.getUuid() ).append( "::upnp:rootdevice\r\n\r\n" );
    rtrVal.add( packet.toString() );
    
    packet = new StringBuffer( basePacket.toString() );

    List services = new ArrayList();
    services.addAll( rootDevice.getUPNPMBeanServices() );
    // 2 messages for each embedded devices
    for ( Iterator i = rootDevice.getUPNPMBeanChildrens().iterator(); i.hasNext();  ) {
      UPNPMBeanDevice child = (UPNPMBeanDevice)i.next();
      services.addAll( child.getUPNPMBeanServices() );
      
      packet = new StringBuffer( basePacket.toString() );
      if ( ssdpAliveMsg ) {
        packet.append( "NT: uuid:" ).append( child.getUuid() ).append( "\r\n" );
        packet.append( "NTS: ssdp:alive\r\n" );
      } else {
        packet.append( "ST: uuid:" ).append( child.getUuid() ).append( "\r\n" );
        packet.append( "EXT:\r\n" );
      }
      packet.append( "USN: uuid:" ).append( child.getUuid() ).append( "\r\n\r\n" );
      rtrVal.add( packet.toString() );
      
      packet = new StringBuffer( basePacket.toString() );
      if ( ssdpAliveMsg ) {
        packet.append( "NT: " ).append( child.getDeviceType() ).append( "\r\n" );
        packet.append( "NTS: ssdp:alive\r\n" );
      } else {
        packet.append( "ST: " ).append( child.getDeviceType() ).append( "\r\n" );
        packet.append( "EXT:\r\n" );
      }
      packet.append( "USN: uuid:" ).append( child.getUuid() ).append( "::" ).append( child.getDeviceType() ).append( "\r\n\r\n" );
      rtrVal.add( packet.toString() );

    }
    
    for ( Iterator i = services.iterator(); i.hasNext(); ) {
      UPNPMBeanService srv = (UPNPMBeanService)i.next();
      // 1 message for each service embedded service
      if ( ssdpAliveMsg ) {
        packet.append( "NT: " ).append( srv.getServiceType() ).append( "\r\n" );
        packet.append( "NTS: ssdp:alive\r\n" );
      } else {
        packet.append( "ST: " ).append( srv.getServiceType() ).append( "\r\n" );
        packet.append( "EXT:\r\n" );
      }
      packet.append( "USN: uuid:" ).append( srv.getDeviceUUID() ).append( "::" ).append( srv.getServiceType() ).append( "\r\n\r\n" );
      rtrVal.add( packet.toString() );
    }

    return rtrVal;
  }
  
  private List getByeByeReplyMessages( UPNPMBeanDevice rootDevice ) {
    List rtrVal = new ArrayList();
    
    StringBuffer basePacket = new StringBuffer();
    StringBuffer packet = null;
    basePacket.append( "NOTIFY * HTTP/1.1\r\n" );
    basePacket.append( "HOST: 239.255.255.250:1900\r\n" );
    // 3 messages for the root device
    packet = new StringBuffer( basePacket.toString() );
    packet.append( "NT: uuid:" ).append( rootDevice.getUuid() ).append( "\r\n" );
    packet.append( "NTS: ssdp:byebye\r\n" );
    packet.append( "USN: uuid:" ).append( rootDevice.getUuid() ).append( "\r\n\r\n" );
    rtrVal.add( packet.toString() );
    
    packet = new StringBuffer( basePacket.toString() );
    packet.append( "NT: " ).append( rootDevice.getDeviceType() ).append( "\r\n" );
    packet.append( "NTS: ssdp:byebye\r\n" );
    packet.append( "USN: uuid:" ).append( rootDevice.getUuid() ).append( "::" ).append( rootDevice.getDeviceType() ).append( "\r\n\r\n" );
    rtrVal.add( packet.toString() );
    
    packet = new StringBuffer( basePacket.toString() );
    packet.append( "NT: upnp:rootdevice\r\n" );
    packet.append( "NTS: ssdp:byebye\r\n" );
    packet.append( "USN: uuid:" ).append( rootDevice.getUuid() ).append( "::upnp:rootdevice\r\n\r\n" );
    rtrVal.add( packet.toString() );

    List services = new ArrayList();
    services.addAll( rootDevice.getUPNPMBeanServices() );
    // 2 messages for each embedded devices
    for ( Iterator i = rootDevice.getUPNPMBeanChildrens().iterator(); i.hasNext();  ) {
      UPNPMBeanDevice child = (UPNPMBeanDevice)i.next();
      services.addAll( child.getUPNPMBeanServices() );
      
      packet = new StringBuffer( basePacket.toString() );
      packet.append( "NT: uuid:" ).append( child.getUuid() ).append( "\r\n" );
      packet.append( "NTS: ssdp:byebye\r\n" );
      packet.append( "USN: uuid:" ).append( child.getUuid() ).append( "\r\n\r\n" );
      rtrVal.add( packet.toString() );
      
      packet = new StringBuffer( basePacket.toString() );
      packet.append( "NT: " ).append( child.getDeviceType() ).append( "\r\n" );
      packet.append( "NTS: ssdp:byebye\r\n" );
      packet.append( "USN: uuid:" ).append( child.getUuid() ).append( "::" ).append( child.getDeviceType() ).append( "\r\n\r\n" );
      rtrVal.add( packet.toString() );
      
    }
    // 1 messages for each service
    for ( Iterator i = services.iterator(); i.hasNext(); ) {
      UPNPMBeanService srv = (UPNPMBeanService)i.next();

      packet = new StringBuffer( basePacket.toString() );
      packet.append( "NT: urn:" ).append( srv.getServiceType() ).append( "\r\n" );
      packet.append( "NTS: ssdp:byebye\r\n" );
      packet.append( "USN: uuid:" ).append( srv.getDeviceUUID() ).append( "::" ).append( srv.getServiceType() ).append( "\r\n\r\n" );
      rtrVal.add( packet.toString() );
    }
    return rtrVal;
  }
  
  public void run() {
    InetAddress group = null;
    try {
      group = InetAddress.getByName( "239.255.255.250" );
      skt = new java.net.MulticastSocket( 1900 );
      skt.setInterface( bindAddress.getAddress() );
      skt.joinGroup( group );
    } catch ( IOException ex ) {
      log.error( "Error during multicast socket creation, thread cannot start", ex );
      return;
    }
    isRunning = true;
    while ( isRunning ) {
      try {
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket( buffer, buffer.length, group, bindAddress.getPort() );
        skt.receive( packet );
        String received = new String( packet.getData(), 0, packet.getLength() );
        if ( log.isDebugEnabled() ) log.debug( "Received message:\n" + received );
        HttpRequest req = new HttpRequest( received );
        if ( req.getHttpCommand().equals( "M-SEARCH" ) ) {
          String man = req.getHTTPHeaderField( "MAN" );
          if ( man.equals( "\"ssdp:discover\"" ) ) {
            String searchTarget = req.getHTTPHeaderField( "ST" );
            // TODO check ALL devices search target
            //if ( searchTarget.equals( Discovery.ALL_DEVICES ) ) {
            if ( searchTarget.equals( Discovery.ROOT_DEVICES ) ) {
              java.net.MulticastSocket multi = new java.net.MulticastSocket(  );
              multi.setInterface( bindAddress.getAddress() );
              for ( Iterator i = handledDevices.iterator(); i.hasNext(); ) {
                UPNPMBeanDevice dv = (UPNPMBeanDevice)i.next();
                List packets = getReplyMessages( dv, false, dv.getSSDPAliveDelay() );
                for ( int z = 0; z < packets.size(); z++ ) {
                  String pack = (String)packets.get( z );
                  if ( log.isDebugEnabled() ) log.debug( "Sending http reply message on " + packet.getAddress() + ":" + packet.getPort()+ " multicast address:\n" + pack.toString() );
                  byte[] pk = pack.getBytes();
                  multi.setTimeToLive( dv.getSSDPTTL() );
                  multi.send( new DatagramPacket( pk, pk.length, packet.getAddress(), packet.getPort() ) );
                }
              }
              multi.close();
            } else {
              // TODO check a specific search target
            }
          }
        }
      } catch ( IOException ex ) {
        if ( isRunning ) {
          log.error( "Error during multicast socket IO operations", ex );
        }
      }
    }
  }
  
  private class SSDPAliveBroadcastMessageSender implements Runnable {

    private Set devices = new HashSet();
    
    private Map devicesLastBroadCast = new HashMap();
    
    private SSDPAliveBroadcastMessageSender( Set upnpRootDevices ) {
      this.devices = upnpRootDevices;
    }
    
    public void run() {
      isRunningSSDPDaemon = true;
      while ( isRunningSSDPDaemon ) {
        synchronized( devices ) {
        
          for ( Iterator i = devices.iterator(); i.hasNext(); ) {
            UPNPMBeanDevice dv = (UPNPMBeanDevice)i.next();
            String key = dv.getUuid();
            long deviceDelay = dv.getSSDPAliveDelay();
            Long lastCall = (Long)devicesLastBroadCast.get( key );
            if ( lastCall == null ) {
              lastCall = new Long ( System.currentTimeMillis() + ( deviceDelay * 60 ) + 1000 );
              devicesLastBroadCast.put( key, lastCall );
            }
            if ( lastCall.longValue() + ( deviceDelay * 60 ) < System.currentTimeMillis() ) {
              try {
                InetAddress group = InetAddress.getByName( "239.255.255.250" );
                java.net.MulticastSocket multi = new java.net.MulticastSocket( bindAddress.getPort() );
                multi.setInterface( bindAddress.getAddress() );
                multi.setTimeToLive( dv.getSSDPTTL() );
                multi.joinGroup( group );
                List packets = getReplyMessages( dv, true, dv.getSSDPAliveDelay() );
                for ( int z = 0; z < packets.size(); z++ ) {
                  String pack = (String)packets.get( z );
                  if ( log.isDebugEnabled() ) log.debug( "Sending http message on " + group.getAddress() + ":1900 multicast address:\n" + pack.toString() );
                  byte[] pk = pack.getBytes();
                  multi.send( new DatagramPacket( pk, pk.length, group, 1900 ) );
                }
                multi.leaveGroup( group );
                multi.close();
                devicesLastBroadCast.put( key, new Long ( System.currentTimeMillis() ) );
              } catch ( IOException ex ) {
                log.error( "Error occured during SSDP alive broadcast message sending", ex );
              }
            }
          }
        }
        try {
          Thread.sleep( 1000 );
        } catch ( InterruptedException ex ) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }
}
