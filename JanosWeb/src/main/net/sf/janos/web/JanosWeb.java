package net.sf.janos.web;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.janos.control.SonosController;
import net.sf.janos.web.model.UpdateListener;
import net.sf.janos.web.servlets.JanosWebServlet;
import net.sf.janos.web.structure.Formatter;
import net.sf.janos.web.structure.JSONFormatter;
import net.sf.janos.web.structure.XMLFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JanosWeb {
	private static final Log LOG = LogFactory.getLog(JanosWeb.class);
	private static final int default_web_port = 8001;
	private static final String nohup = "nohup";
	
	
	public static void main(String[] args) {
		Properties upnpprop = new Properties();
		try {
			upnpprop.load(new FileReader("resources/upnp.properties"));
			System.setProperty("net.sbbi.upnp.Discovery.bindPort", upnpprop.getProperty("discoverybindport"));
		} catch (FileNotFoundException e) {
			// Do nothing, the system works without setting this property.
		} catch (IOException e) {
			// Do nothing, the system works without setting this property.
		}
		
		
		final SonosController controller = SonosController.getInstance();
		try {
			Timer zonePollerTimer = new Timer("ZonePoller", true);
			TimerTask zonePollerTask = new TimerTask() {

				@Override
				public void run() {
					controller.searchForDevices();
					long pollPeriod = Long.parseLong(System.getProperty(
							"net.sf.janos.pollPeriod", "5000"));
					controller.purgeStaleDevices(pollPeriod * 2);
				}
			};
			long pollPeriod = Long.parseLong(System.getProperty(
					"net.sf.janos.pollPeriod", "5000"));
			zonePollerTimer.scheduleAtFixedRate(zonePollerTask, 0, pollPeriod);
			Thread.sleep(Integer.parseInt(System.getProperty(
					"net.sf.janos.searchTime", "1000")));
		} catch (NumberFormatException e) {
			LOG.warn("Sleep interrupted:", e);
		} catch (InterruptedException e) {
			LOG.warn("Sleep interrupted:", e);
		}

		class MyServ extends Acme.Serve.Serve {
			private static final long serialVersionUID = 1L;

			// Overriding method for public access
			public void setMappingTable(PathTreeDictionary mappingtable) {
				super.setMappingTable(mappingtable);
			}

			// add the method below when .war deployment is needed
			public void addWarDeployer(String deployerFactory, String throttles) {
				super.addWarDeployer(deployerFactory, throttles);
			}
		}
		;

		final MyServ srv = new MyServ();
		// setting aliases, for an optional file servlet
		
		Acme.Serve.Serve.PathTreeDictionary aliases = new Acme.Serve.Serve.PathTreeDictionary();
		aliases.put("/", new java.io.File("resources/webcontent"));
		// note cast name will depend on the class name, since it is anonymous
		// class
		srv.setMappingTable(aliases);
		// setting properties for the server, and exchangable Acceptors
		Properties tjwsprop = new Properties();
		try {
			tjwsprop.load(new FileReader("resources/tjws.properties"));
		} catch (FileNotFoundException e) {
			//Set default values
			tjwsprop.put("port", default_web_port);
			tjwsprop.put(Acme.Serve.Serve.ARG_NOHUP, nohup);
		} catch (IOException e) {
			//Set default values
			tjwsprop.put("port", default_web_port);
			tjwsprop.put(Acme.Serve.Serve.ARG_NOHUP, nohup);
		}
		try {
			tjwsprop.put("port", Integer.parseInt(tjwsprop.getProperty("port")));
		}
		catch (Exception e) {
			//Set standard port to 8000, if there are any problems with reading from the properties file.
			tjwsprop.put("port", default_web_port);
		}
		
		Properties janoswebprop = new Properties();
		Formatter formatter = new JSONFormatter(false);
		try {
			janoswebprop.load(new FileReader("resources/janosweb.properties"));
			if (janoswebprop.getProperty("format").equalsIgnoreCase("xml")) {
				formatter = new XMLFormatter(false);
			}
		} catch (FileNotFoundException e) {
			// Do nothing, the system works without setting this property.
		} catch (IOException e) {
			// Do nothing, the system works without setting this property.
		}
		
		srv.arguments = tjwsprop;
		srv.addDefaultServlets(null); // optional file servlet
		srv.addServlet("/janosWeb", new JanosWebServlet(controller, formatter));
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					srv.notifyStop();
				} catch (java.io.IOException ioe) {

				}
				controller.dispose();
				srv.destroyAllServlets();
			}
		}));
		srv.serve();
	}
}
