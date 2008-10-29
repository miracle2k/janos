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

import java.util.Iterator;
import java.util.Set;

/**
 * Class to handle HTTP UPNP GET requests on UPNPMBeanDevices
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class HttpGetRequest implements HttpRequestHandler {
  
  private final static HttpGetRequest instance = new HttpGetRequest();
  
  public static HttpRequestHandler getInstance() {
    return instance;
  }
  
  private HttpGetRequest() {
  }
  
  public String service( Set devices, HttpRequest request ) {
    StringBuffer rtr = null;
    String filePath = request.getHttpCommandArg();
    // Request are : /uuid/desc.xml or /uuid/serviceId/scpd.xml
    boolean validGet = ( filePath.startsWith( "/" ) && filePath.endsWith( "/desc.xml" ) ) ||
                       ( filePath.startsWith( "/" ) && filePath.endsWith( "/scpd.xml" ) );

    if ( validGet ) {
      String uuid = null;
      String serviceUuid = null;
      int lastSlash = filePath.lastIndexOf( '/' );
      if ( lastSlash != -1 ) {
        uuid = filePath.substring( 1, lastSlash );
        serviceUuid = uuid;
        // check if this is an uuid/desc.xml or uuid/serviceId/scpd.xml request type
        int slashIndex = uuid.indexOf( "/" );
        if ( slashIndex != -1 ) {
          uuid = uuid.substring( 0, slashIndex );
        }
      }
      if ( uuid != null ) {
        // search now the bean within the set
        UPNPMBeanDevice found = null;
        synchronized( devices ) {
          for ( Iterator i = devices.iterator(); i.hasNext(); ) {
            UPNPMBeanDevice dv = (UPNPMBeanDevice)i.next();
            if ( dv.getUuid().equals( uuid ) ) {
              found = dv;
              break;
            }
          }
        }
        if ( found != null ) {
          String contentToReturn = null;
          if ( filePath.endsWith( "/desc.xml" ) ) {
            contentToReturn = found.getDeviceInfo();
          } else if ( filePath.endsWith( "/scpd.xml" ) ) {
            UPNPMBeanService srv = found.getUPNPMBeanService( serviceUuid );
            if ( srv != null ) {
              contentToReturn = srv.getDeviceSCDP();
            }
          }
          
          rtr = new StringBuffer();
          rtr.append( "HTTP/1.1 200 OK\r\n" );
          String accept = request.getHTTPHeaderField( "CONTENT-LANGUAGE" );
          if ( accept != null ) {
            rtr.append( "CONTENT-LANGUAGE: " ).append( accept ).append( "\r\n" );
          }
          rtr.append( "CONTENT-LENGTH: " ).append( contentToReturn.length() ).append( "\r\n" );
          rtr.append( "CONTENT-TYPE: text/xml\r\n\r\n" );
          rtr.append( contentToReturn );
          return rtr.toString();
        }
      }
    }
    return null;
  }
  
}
