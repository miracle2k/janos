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

import javax.management.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import mx4j.log.CommonsLogger;

/**
 * JMX manager for UPNP devices, entry point for the MX4j HTTP admin console
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class JMXManager {
  
  private final static JMXManager instance = new JMXManager();
  
  private final static Log log = LogFactory.getLog( JMXManager.class );
  
  private ObjectName discBeanName = null;

  private MBeanServer server;
  
  public final static JMXManager getInstance() {
    return instance;
  }
  
  public final static JMXManager getNewInstance( MBeanServer server ) {
    JMXManager manager = new JMXManager();
    manager.setMBeanserver( server );
    return manager;
  }
  
  private void setMBeanserver( MBeanServer server ) {
    this.server = server;
  }
  
  public void startup( int discoveryTimeout ) throws Exception {
    discBeanName = new ObjectName( "UPNPLib discovery:name=Discovery MBean_" + this.hashCode() );
    UPNPDiscoveryMBean bean = new UPNPDiscovery( discoveryTimeout, true, true );
    server.registerMBean( bean, discBeanName );
  }
  
  public void shutdown() {
    try {
      server.unregisterMBean( discBeanName );
    } catch ( Exception ex ) {
      log.error( "Error occured during UPNPDiscoveryMBean unregistration", ex );
    }
  }
  
  private MBeanServer initMBeanServer( MBeanServerConfig conf ) throws Exception {
    mx4j.log.Log.redirectTo( new CommonsLogger() );
    //  make sure that MX4j Server builder is used
    String oldSysProp = System.getProperty( "javax.management.builder.initial" );
    System.setProperty( "javax.management.builder.initial", "mx4j.server.MX4JMBeanServerBuilder" );
    MBeanServer server = MBeanServerFactory.createMBeanServer( "UPNPLib" );
    if ( oldSysProp != null ) {
      System.setProperty( "javax.management.builder.initial", oldSysProp );
    }
    ObjectName serverName = new ObjectName( "Http:name=HttpAdaptor" );
    server.createMBean( "mx4j.tools.adaptor.http.HttpAdaptor", serverName, null );
    // set attributes
    server.setAttribute( serverName, new Attribute( "Port", new Integer( conf.adapterAdapterPort ) ) );
    
    Boolean allowWanBool = new Boolean( conf.allowWan );
    if ( allowWanBool.booleanValue() ) {
      server.setAttribute( serverName, new Attribute( "Host", "0.0.0.0" ) );
    } else {
      server.setAttribute( serverName, new Attribute( "Host", "localhost" ) );
    }
    
    ObjectName processorName = new ObjectName( "Http:name=XSLTProcessor" );
    server.createMBean( "mx4j.tools.adaptor.http.XSLTProcessor", processorName, null );
    server.setAttribute( processorName, new Attribute( "LocaleString", conf.locale ) );
    
    server.setAttribute( processorName, new Attribute( "UseCache", Boolean.FALSE ) );
    
    server.setAttribute( processorName, new Attribute( "PathInJar", "net/sbbi/jmx/xsl" ) );
    
    server.setAttribute( serverName, new Attribute( "ProcessorName", processorName ) );
    // add user names
    server.invoke( serverName, "addAuthorization", new Object[] { conf.adapterUserName, conf.adapterPassword }, new String[] { "java.lang.String", "java.lang.String" } );
    // use basic authentication
    server.setAttribute( serverName, new Attribute( "AuthenticationMethod", "basic" ) );
    // starts the server
    server.invoke( serverName, "start", null, null );
    
    return server;
  }
  
  public final static void main( String args[] ) {
    
    if ( args.length != 6 ) {
      log.info( "Usage : JMXManager <AdapterPort> <UserName> <Password> <AllowWan> <Locale> <discoveryTimeout>" );
      System.exit( 0 );
    }
    
    try {
      JMXManager manager = JMXManager.getInstance();
      MBeanServerConfig conf = new MBeanServerConfig( args );
      manager.setMBeanserver( manager.initMBeanServer( conf ) );
      manager.startup( conf.discoveryTimeout );
    } catch ( Exception ex ) {
      log.error( "Error during startup", ex );
    }
    
  }
  
  private final static class MBeanServerConfig {
    
    private String adapterAdapterPort;
    private String adapterUserName;
    private String adapterPassword;
    private String allowWan;
    private String locale;
    private int discoveryTimeout;
    
    private MBeanServerConfig( String args[] ) {
      adapterAdapterPort = args[0];
      adapterUserName = args[1];
      adapterPassword = args[2];
      allowWan = args[3];
      locale = args[4];
      discoveryTimeout = Integer.parseInt( args[5] );
    }
    
  }
  
}
