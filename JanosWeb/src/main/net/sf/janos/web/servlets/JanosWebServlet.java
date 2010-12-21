package net.sf.janos.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sbbi.upnp.devices.DeviceIcon;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.MediaRendererDevice;
import net.sf.janos.control.RenderingControlService;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.PlayMode;
import net.sf.janos.model.SeekTargetFactory;
import net.sf.janos.model.TransportAction;
import net.sf.janos.model.TransportInfo.TransportState;
import net.sf.janos.model.ZoneGroup;
import net.sf.janos.web.exception.JanosWebException;
import net.sf.janos.web.model.UpdateListener;
import net.sf.janos.web.model.GetRequestHandler;
import net.sf.janos.web.structure.Element;
import net.sf.janos.web.structure.ElementUtil;
import net.sf.janos.web.structure.Formatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

public class JanosWebServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3820306931297259395L;
	private static final Log LOG = LogFactory.getLog(JanosWebServlet.class);
	private final SonosController controller;
	private String thishost;
	private Formatter formatter;
	private GetRequestHandler zd;

	private enum Command {
		getZoneGroups 
		, listenForZoneGroupUpdates
		, getPlayCommands
		, getPlayModes
		, listenForZoneGroupVolumeUpdates
		, listenForZoneGroupMusicUpdates
		, listenForAllZoneGroupUpdates
		, getZoneGroupVolume 
		, setZoneGroupVolume 
		, setZoneGroupMuted 
		, getZoneGroupPlayInfo
		, setZoneGroupCommand
		, setZoneGroupPlayMode
		, getZoneGroupCurrentTrackPosition
		, setZoneGroupCurrentTrackPosition
		, getZoneGroupCurrentTrack
		, getZoneGroupAllInfo
		, getZoneGroupQueue
		, getZoneGroupArtists
		, getZoneGroupAlbums
		, getZoneGroupTracks
		, getZoneGroupSearch
		, setZoneGroupEnqueue
		, setZoneGroupEnqueueNext
		, setZoneGroupEnqueueNow
		, setZoneGroupClearQueue

		, listenForZoneVolumeUpdates
		, listenForZoneMusicUpdates
		, listenForAllZoneUpdates
		, getZoneVolume
		, setZoneVolume 
		, setZoneMuted
		, getZonePlayInfo
		, setZoneCommand
		, setZonePlayMode
		, getZoneCurrentTrackPosition
		, setZoneCurrentTrackPosition
		, getZoneCurrentTrack
		, getZoneAllInfo
		, getZoneQueue
		, getZoneArtists
		, getZoneAlbums
		, getZoneTracks
		, getZoneSearch
		, setZoneEnqueue
		, setZoneEnqueueNext
		, setZoneEnqueueNow
		, setZoneClearQueue

		, nonExistingCommand
		;

		public static Command toCmd(String str) {
			try {
				return valueOf(str);
			} catch (Exception e) {
				return nonExistingCommand;
			}
		}
	};

	public JanosWebServlet(SonosController contr, Formatter formatter) {
		this.controller = contr;
		this.formatter = formatter;
		try {
			getGroups();
		} catch (Exception e) {
			LOG.debug("Got an exception of type '"+e.getClass().getName()+"' at initialization of the servlet");
		}
		zd = new GetRequestHandler(controller);
	}

	public void destroy() {
		super.destroy();
		controller.dispose();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			thishost = request.getRequestURL().toString().replaceFirst(request.getServletPath(), "");
			String cmd = (String) request.getParameter("cmd");
			if (cmd == null) {
				printServletInterface(response);
				return;
			}
		
			// Dispatch control to the relevant methods, based on the "cmd"
			// parameter.
			Element resp = ElementUtil.createResponse();

			//---------------------------------------------------------------------------------
			//Dispatch to correct top level method, based on the cmd-parameter
			//---------------------------------------------------------------------------------

		try {
			switch (Command.toCmd(cmd)) {
			case getZoneGroups:
				resp.addChild(getZoneGroups());
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getPlayCommands:
				resp.addChild(getPlayCommands());
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getPlayModes:
				resp.addChild(getPlayModes());
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			
			case listenForZoneGroupUpdates:
				resp.addChild(listenForZoneGroupUpdates());
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;

			case listenForZoneVolumeUpdates:
				resp.addChild(listenForZoneVolumeUpdates(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case listenForZoneMusicUpdates:
				resp.addChild(listenForZoneMusicUpdates(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case listenForAllZoneUpdates:
				resp.addChild(listenForAllZoneUpdates(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;

			case listenForZoneGroupVolumeUpdates:
				resp.addChild(listenForZoneGroupVolumeUpdates(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case listenForZoneGroupMusicUpdates:
				resp.addChild(listenForZoneGroupMusicUpdates(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case listenForAllZoneGroupUpdates:
				resp.addChild(listenForAllZoneGroupUpdates(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;

				
				
			case getZoneVolume:
				resp.addChild(getZoneVolume(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneVolume:
				setZoneVolume(request.getParameter("zoneID"),
						request.getParameter("volume"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneMuted:
				setZoneMuted(request.getParameter("zoneID"),
						request.getParameter("mute"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneCommand:
				setZoneCommand(request.getParameter("zoneID"), request.getParameter("command"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZonePlayInfo:
				resp.addChild(getZonePlayInfo(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZonePlayMode:
				setZonePlayMode(request.getParameter("zoneID"), request.getParameter("playMode"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneCurrentTrackPosition:
				resp.addChild(getZoneCurrentTrackPosition(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneCurrentTrackPosition:
				setZoneCurrentTrackPosition(request.getParameter("zoneID"), request.getParameter("trackPosition"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneCurrentTrack:
				resp.addChild(getZoneCurrentTrack(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneAllInfo:
				resp.addChild(getZoneAllInfo(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
				
			case getZoneQueue:
				//resp.addChild(getZoneQueue(request.getParameter("zoneID"), request.getParameter("startIndex"), request.getParameter("numEntries")));
				resp.addChild(getZoneQueue2(request.getParameter("zoneID"), request.getParameter("startIndex"), request.getParameter("numEntries")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneArtists:
				resp.addChild(getZoneArtists(request.getParameter("zoneID"), request.getParameter("startIndex"), request.getParameter("numEntries")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneAlbums:
				resp.addChild(getZoneAlbums(request.getParameter("zoneID"), request.getParameter("startIndex"), request.getParameter("numEntries"), request.getParameter("artist")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneTracks:
				resp.addChild(getZoneTracks(request.getParameter("zoneID"), request.getParameter("startIndex"), request.getParameter("numEntries"), request.getParameter("artist"), request.getParameter("album")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneSearch:
				resp.addChild(getZoneSearch(request.getParameter("zoneID"), request.getParameter("startIndex"), request.getParameter("numEntries"), request.getParameter("searchData")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneEnqueue:
				setZoneEnqueue(request.getParameter("zoneID"), request.getParameter("itemID"), null);
				resp.addChild(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneEnqueueNext:
				setZoneEnqueue(request.getParameter("zoneID"), request.getParameter("itemID"), new Integer(1));
				resp.addChild(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneEnqueueNow:
				setZoneEnqueue(request.getParameter("zoneID"), request.getParameter("itemID"), new Integer(0));
				resp.addChild(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneClearQueue:
				setZoneClearQueue(request.getParameter("zoneID"));
				resp.addChild(ElementUtil.getStatusSuccesElement());
				break;
				
			case getZoneGroupVolume:
				resp.addChild(getZoneGroupVolume(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupVolume:
				setZoneGroupVolume(request.getParameter("groupID"),
						request.getParameter("volume"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupMuted:
				setZoneGroupMuted(request.getParameter("groupID"),
						request.getParameter("mute"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupPlayInfo:
				resp.addChild(getZoneGroupPlayInfo(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupCommand:
				setZoneGroupCommand(request.getParameter("groupID"), request.getParameter("command"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupPlayMode:
				setZoneGroupPlayMode(request.getParameter("groupID"), request.getParameter("playMode"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupCurrentTrackPosition:
				resp.addChild(getZoneGroupCurrentTrackPosition(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupCurrentTrackPosition:
				setZoneGroupTrackPosition(request.getParameter("groupID"), request.getParameter("trackPosition"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupCurrentTrack:
				resp.addChild(getZoneGroupCurrentTrack(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupAllInfo:
				resp.addChild(getZoneGroupAllInfo(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupQueue:
				resp.addChild(getZoneGroupQueue(request.getParameter("groupID"), request.getParameter("startIndex"), request.getParameter("numEntries")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupArtists:
				resp.addChild(getZoneGroupArtists(request.getParameter("groupID"), request.getParameter("startIndex"), request.getParameter("numEntries")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupAlbums:
				resp.addChild(getZoneGroupAlbums(request.getParameter("groupID"), request.getParameter("startIndex"), request.getParameter("numEntries"), request.getParameter("artist")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupTracks:
				resp.addChild(getZoneGroupTracks(request.getParameter("groupID"), request.getParameter("startIndex"), request.getParameter("numEntries"), request.getParameter("artist"), request.getParameter("album")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupSearch:
				resp.addChild(getZoneGroupSearch(request.getParameter("groupID"), request.getParameter("startIndex"), request.getParameter("numEntries"), request.getParameter("searchData")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupEnqueue:
				setZoneGroupEnqueue(request.getParameter("groupID"), request.getParameter("itemID"), null);
				resp.addChild(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupEnqueueNext:
				setZoneGroupEnqueue(request.getParameter("groupID"), request.getParameter("itemID"), new Integer(1));
				resp.addChild(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupEnqueueNow:
				setZoneGroupEnqueue(request.getParameter("groupID"), request.getParameter("itemID"), new Integer(0));
				resp.addChild(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupClearQueue:
				setZoneGroupClearQueue(request.getParameter("groupID"));
				resp.addChild(ElementUtil.getStatusSuccesElement());
				break;
				



			default:
				resp.addChildFirst(ElementUtil.createFailureElement("Non-existing command: " + request.getParameter("cmd"), "", this.getClass().getName()));
			}
		} catch (Exception e) {
			LOG.error("Unknown error", e);
			resp.addChildFirst(ElementUtil.createFailureElement("Unknown error", e.getMessage(), e.getClass().getName()));
		}

		PrintWriter out = response.getWriter();
		formatter.modifyResponseHeader(response);
		formatter.write(out, resp);
		out.close();
		} catch (ConcurrentModificationException cme) {
			try { Thread.sleep(1000); } catch (InterruptedException ie) {};
			doPost(request, response);
		}
	}

	

	
	
	
	
	
	
	//---------------------------------------------------------------------------------
	//Top level methods, that return Element representations of the objects involved
	//---------------------------------------------------------------------------------
	
	//*************************************************
	//     ___  ____  ____  ____  ____  ____  ____ 
	//    / __)(  __)(_  _)(_  _)(  __)(  _ \/ ___)
	//   ( (_ \ ) _)   )(    )(   ) _)  )   /\___ \
	//    \___/(____) (__)  (__) (____)(__\_)(____/
	//
	//*************************************************

	
	
	/**
	 * Get an Element-representaion of the ZoneGroups
	 * 
	 * @param parent
	 *            the parent Element, that the Element-representation must be
	 *            added as a child to
	 * @throws JanosWebException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * */
	private Element getZoneGroups() throws JanosWebException, IOException, UPNPResponseException {
		List<ZoneGroup> zonegroups = getGroups();

		Element groups = new Element("groups");

		for (ZoneGroup zgroup : zonegroups) {
			ZonePlayer coord = getZonePlayer(zgroup.getCoordinator());
			if (coord != null) {
				Element group = new Element("group", true);
				group.addChild(new Element("groupID", zgroup.getId()));
				group.addChild(new Element("icon", getZonePlayerIcon(coord)));
				group.addChild(new Element("playing", getZonePlaying(coord) ? "true" : "false"));
				Element gmembers = new Element("members");
				for (String zoneID : zgroup.getMembers()) {
					Element zone = new Element("zone", true);
					zone.addChild(new Element("zoneID", zoneID));
					ZonePlayer zp = getZonePlayer(zoneID);
					zone.addChild(new Element("icon", getZonePlayerIcon(zp)));
					zone.addChild(new Element("playing", getZonePlaying(zp) ? "true" : "false"));
					zone.addChild(new Element("name", getZonePlayerName(zp)));
					gmembers.addChild(zone);
				}
				group.addChild(gmembers);
				group.addChild(new Element("coordinatorName", getZonePlayerName(coord)));
				group.addChild(new Element("coordinatorID", zgroup.getCoordinator()));
				groups.addChild(group);
			}
		}
		return groups;
	}
	
	private Element getPlayCommands() {
		Element coms = new Element("playCommands");
		coms.addChild(new Element("command", "Play"));
		coms.addChild(new Element("command", "Pause"));
		coms.addChild(new Element("command", "Stop"));
		coms.addChild(new Element("command", "Next"));
		coms.addChild(new Element("command", "Previous"));
		return coms;
	}
	private Element getPlayModes() {
		Element modes = new Element("playModes");
		modes.addChild(new Element("mode", "NORMAL"));
		modes.addChild(new Element("mode", "SHUFFLE"));
		modes.addChild(new Element("mode", "SHUFFLE_NOREPEAT"));
		modes.addChild(new Element("mode", "REPEAT_ALL"));
		return modes;
	}

	

	
	
	/**
	 * Returns an Element-representation of the ZonePlayer's volume
	 * 
	 * @param zoneID
	 * @return an Element representation of a ZonePlayer and it's volume
	 * @throws JanosWebException 
	 */
	private Element getZoneVolume(String zoneID)
			throws IOException, UPNPResponseException, JanosWebException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZoneVolume(zoneID, zone);
		return zone;
	}
	
	/**
	 * Get an Element-representation of the ZoneGroup's volume settings
	 * 
	 * @param parent
	 * @param groupID
	 * @throws JanosWebException
	 * @throws UPNPResponseException
	 * @throws IOException
	 */
	
	private Element getZoneGroupVolume(String groupID) throws JanosWebException, IOException, UPNPResponseException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		zd.getZoneGroupVolume(groupID, zonegroup);
		return zonegroup;
	}

	/**Get an Element-representation of a ZonePlayer's playmode (shuffle/normal/etc.) and playstate (playing or not)
	 * @param the ZonePlayer's ID
	 * */
	private Element getZonePlayInfo(String zoneID) throws IOException, UPNPResponseException, JanosWebException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZonePlayInfo(zoneID, zone);
		return zone;
	}
	
	/**Get an Element-representation of a ZoneGroup's playmode (shuffle/normal/etc.) and playstate (playing or not)
	 * @param groupID the ZoneGroup's ID
	 * */
	private Element getZoneGroupPlayInfo(String groupID) throws IOException, UPNPResponseException, JanosWebException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		zd.getZonePlayInfo(getZoneGroup(groupID).getCoordinator(), zonegroup);
		return zonegroup;
	}


	/**Get an Element-representation of a ZonePlayer's current track position
	 * @param the ZonePlayer's ID
	 * @return song position*/
	private Element getZoneCurrentTrackPosition(String zoneID) throws IOException, UPNPResponseException, JanosWebException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZoneCurrentTrackPosition(zoneID, zone);
		return zone;
	}

	/**Get an Element-representation of a ZoneGroup's current song position
	 * @param groupID the ZoneGroup's ID
	 * @return track position*/
	private Element getZoneGroupCurrentTrackPosition(String groupID) throws IOException, UPNPResponseException, JanosWebException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		zd.getZoneCurrentTrackPosition(getZoneGroup(groupID).getCoordinator(), zonegroup);
		return zonegroup;
	}

	
	/**Get an Element-representation of a ZonePlayer's current track
	 * @param the ZonePlayer's ID
	 * @return song position
	 * @throws SAXException */
	private Element getZoneCurrentTrack(String zoneID) throws IOException, UPNPResponseException, JanosWebException, SAXException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZoneCurrentTrack(zoneID, zone, thishost);
		return zone;
	}
	private Element getZoneGroupCurrentTrack(String groupID) throws IOException, UPNPResponseException, JanosWebException, SAXException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		zd.getZoneCurrentTrack(getZoneGroup(groupID).getCoordinator(), zonegroup, thishost);
		return zonegroup;
	}
	
	private Element getZoneAllInfo(String zoneID) throws IOException, UPNPResponseException, JanosWebException, SAXException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZoneVolume(zoneID, zone);
		zd.getZonePlayInfo(zoneID, zone);
		zd.getZoneCurrentTrack(zoneID, zone, thishost);
		return zone;
	}
	
	private Element getZoneGroupAllInfo(String groupID) throws IOException, UPNPResponseException, JanosWebException, SAXException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		String coordinatorID = getZoneGroup(groupID).getCoordinator();
		zd.getZoneGroupVolume(groupID, zonegroup);
		zd.getZonePlayInfo(coordinatorID, zonegroup);
		zd.getZoneCurrentTrack(coordinatorID, zonegroup, thishost);
		return zonegroup;
	}
	
	/**Get an Element-representation of a ZonePlayer's queue
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the queue. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneQueue2(String zoneID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZoneQueue(zoneID, startIndex, numEntries, zone);
		return zone;
	}

	private Element getZoneGroupQueue(String groupID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		zd.getZoneQueue(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries, zonegroup);
		return zonegroup;
	}
	
	/**Get an Element-representation of a ZonePlayer's list of artists
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the list of artists. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return */
	private Element getZoneArtists(String zoneID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZoneArtists(zoneID, startIndex, numEntries, zone);
		return zone;
	}

	private Element getZoneGroupArtists(String groupID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		zd.getZoneArtists(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries, zonegroup);
		return zonegroup;
	}
	
	/**Get an Element-representation of a ZonePlayer's list of albums
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the list of albums. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return */
	private Element getZoneAlbums(String zoneID, String startIndex, String numEntries, String artist) throws IOException, UPNPResponseException, JanosWebException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZoneAlbums(zoneID, startIndex, numEntries, artist, zone);
		return zone;
	}

	private Element getZoneGroupAlbums(String groupID, String startIndex, String numEntries, String artist) throws IOException, UPNPResponseException, JanosWebException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		zd.getZoneAlbums(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries, artist, zonegroup);
		return zonegroup;
	}


	/**Get an Element-representation of a ZonePlayer's list of tracks
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the list of tracks. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneTracks(String zoneID, String startIndex, String numEntries, String artist, String album) throws IOException, UPNPResponseException, JanosWebException {
		Element zone = getZoneElement(zoneID, false);
		zd.getZoneTracks(zoneID, startIndex, numEntries, artist, album, zone);
		return zone;
	}
	
	private Element getZoneGroupTracks(String groupID, String startIndex, String numEntries, String artist, String album) throws IOException, UPNPResponseException, JanosWebException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		zd.getZoneTracks(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries, artist, album, zonegroup);
		return zonegroup;
	}

	/**Get an Element-representation of a ZonePlayer's 
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the list of tracks. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneSearch(String zoneID, String startIndex, String numEntries, String searchData) throws IOException, UPNPResponseException, JanosWebException {
		Element zone = getZoneElement(zoneID, false);
		if (searchData == null) {
			missingParameterException("searchData");
		}
		zd.getZoneSearch(zoneID, startIndex, numEntries, searchData, zone);
		return zone;
	}
	
	private Element getZoneGroupSearch(String groupID, String startIndex, String numEntries, String searchData) throws IOException, UPNPResponseException, JanosWebException {
		Element zonegroup = getZoneGroupElement(groupID, false);
		if (searchData == null) {
			missingParameterException("searchData");
		}
		zd.getZoneSearch(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries, searchData, zonegroup);
		return zonegroup;
	}


	
	
	
	//***************************************************************
	//    __    __  ____  ____  ____  __ _  ____  ____  ____ 
	//   (  )  (  )/ ___)(_  _)(  __)(  ( \(  __)(  _ \/ ___)
	//   / (_/\ )( \___ \  )(   ) _) /    / ) _)  )   /\___ \
	//   \____/(__)(____/ (__) (____)\_)__)(____)(__\_)(____/
	// (a kind of getter but asynchronous (or kinda asynchronous))
	//***************************************************************
	
	//TODO: do like the getter methods: move to other class: ListenRequestHandler?
	
	private Element listenForZoneGroupUpdates() throws JanosWebException, IOException, UPNPResponseException {
		UpdateListener l = new UpdateListener();
		controller.getZoneGroupStateModel().addListener(l);
		//this is a blocking call
		Element retval = l.getZoneChanges();
		controller.getZoneGroupStateModel().removeListener(l);
		return retval;
	}
	
	private Element listenForZoneVolumeUpdates(String zoneID)  throws JanosWebException, IOException, UPNPResponseException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		RenderingControlService rcs = getZonePlayer(zoneID).getMediaRendererDevice().getRenderingControlService();
		UpdateListener l = new UpdateListener();
		rcs.addListener(l);
		Element retval = l.getVolumeChanged();
		rcs.removeListener(l);
		return retval;
	}

	private Element listenForZoneGroupVolumeUpdates(String groupID)  throws JanosWebException, IOException, UPNPResponseException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return listenForZoneVolumeUpdates(getZoneGroup(groupID).getCoordinator());
	}

	private Element listenForZoneMusicUpdates(String zoneID)  throws JanosWebException, IOException, UPNPResponseException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		AVTransportService avts = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService();
		UpdateListener l = new UpdateListener();
		avts.addAvTransportListener(l);
		Element retval = l.getMusicChanged();
		avts.removeAvTransportListener(l);
		return retval;
	}
	
	private Element listenForZoneGroupMusicUpdates(String groupID)  throws JanosWebException, IOException, UPNPResponseException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return listenForZoneMusicUpdates(getZoneGroup(groupID).getCoordinator());
	}

	private Element listenForAllZoneUpdates(String zoneID) throws JanosWebException, IOException, UPNPResponseException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		MediaRendererDevice mrd = getZonePlayer(zoneID).getMediaRendererDevice();
		RenderingControlService rcs = mrd.getRenderingControlService();
		AVTransportService avts = mrd.getAvTransportService();
		UpdateListener l = new UpdateListener();
		avts.addAvTransportListener(l);
		rcs.addListener(l);
		controller.getZoneGroupStateModel().addListener(l);
		//this is a blocking call
		Element retval = l.getAllChanges();
		controller.getZoneGroupStateModel().removeListener(l);
		rcs.removeListener(l);
		avts.removeAvTransportListener(l);
		return retval;
	}

	private Element listenForAllZoneGroupUpdates(String groupID)  throws JanosWebException, IOException, UPNPResponseException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return listenForAllZoneUpdates(getZoneGroup(groupID).getCoordinator());
	}

	
	
	
	//*************************************************
	//    ____  ____  ____  ____  ____  ____  ____ 
	//   / ___)(  __)(_  _)(_  _)(  __)(  _ \/ ___)
	//   \___ \ ) _)   )(    )(   ) _)  )   /\___ \
	//   (____/(____) (__)  (__) (____)(__\_)(____/
	//
	//*************************************************

	//TODO: Move functionality implementation away from servlet class into a SetRequestHandler
	
	private void setZoneVolume(String zoneID, String volume) throws NumberFormatException, IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (volume == null) {
			missingParameterException("volume");

		}
		int vol = Integer.parseInt(volume);
		checkVolume(vol);
		ZonePlayer zp = getZonePlayer(zoneID);
		RenderingControlService rcs = zp.getMediaRendererDevice()
		.getRenderingControlService();
		rcs.setVolume(vol);
	}

	/**
	 * Set the volume for a ZoneGroup and get an Element-representation of the
	 * ZoneGroup's volume settings afterwards
	 * 
	 * @param parent
	 * @param groupID
	 * @throws JanosWebException
	 * @throws UPNPResponseException
	 * @throws IOException
	 */
	private void setZoneGroupVolume(String groupID, String volume)
			throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (volume == null) {
			missingParameterException("volume");
		}
		int vol = Integer.parseInt(volume);
		checkVolume(vol);
		ZoneGroup zgroup = getZoneGroup(groupID);
		
		int totalvolume = 0;
		List<String> members = zgroup.getMembers();
		Collections.sort(members);

		// First calculate the current group-volume and store the individual
		// member's volumes
		int[] zonevolumes = new int[members.size()];
		int idx = 0;
		for (String zoneID : members) {
			ZonePlayer zp = getZonePlayer(zoneID);
			int volum = zp.getMediaRendererDevice()
					.getRenderingControlService().getVolume();
			zonevolumes[idx++] = volum;
			totalvolume += volum;
		}
		int oldgroupvolume = totalvolume / members.size();

		// Then calculate the difference in volume between old volume and new
		// volume (increment)
		int increment = vol - oldgroupvolume;
		int spilloverincrement = 0;
		int adjustablezones = members.size();
		// now calculate the individual group volumes
		for (int i = 0; i < zonevolumes.length; i++) {
			zonevolumes[i] += increment;
			if (zonevolumes[i] < 0) {
				adjustablezones--;
				spilloverincrement += zonevolumes[i];
				zonevolumes[i] = 0;
			} else if (zonevolumes[i] > 100) {
				adjustablezones--;
				spilloverincrement += (zonevolumes[i] - 100);
				zonevolumes[i] = 100;
			}
		}

		// ok, zone volumes are calculated BUT if spilloverincrement is not 0 it
		// means that one or more zones volumes were maxed/min'ed. So this
		// spilloverincrement has to be distributed between the rest of the 
		// zones, in order to get the desired group volume
		while (spilloverincrement != 0 && adjustablezones > 0) {
			// If the spilloverincrement is greater than or equal to the number
			// of adjustable zones, it is possible to do integer division:
			// spilloverincrement/adjustablezones to obtain a  value that each
			// adjustable zone must be adjusted with.
			if (Math.abs(spilloverincrement) >= adjustablezones) {
				increment = (spilloverincrement / adjustablezones);
				for (int i = 0; i < zonevolumes.length; i++) {
					if (0 < zonevolumes[i] && zonevolumes[i] < 100) {
						zonevolumes[i] += increment;
						spilloverincrement -= increment; // subtract from the
													     // spilloverincrement
						if (zonevolumes[i] <= 0) {
							adjustablezones--;
							spilloverincrement += zonevolumes[i];
							zonevolumes[i] = 0;
						} else if (zonevolumes[i] >= 100) {
							adjustablezones--;
							spilloverincrement += (zonevolumes[i] - 100);
							zonevolumes[i] = 100;
						}
					}
				}
			} else { // When spilloverincrement is smaller than the number of
						// adjustable zones we adjust one zone at a time with
						// 1 volume until spilloverincrement is 0
				for (int i = 0; i < zonevolumes.length; i++) {
					if (spilloverincrement == 0) {
						break;
					}
					if (0 < zonevolumes[i] && zonevolumes[i] < 100) {
						if (spilloverincrement < 0) {
							zonevolumes[i]--;
							spilloverincrement++;
							if (zonevolumes[i] == 0) {
								adjustablezones--;
							}
						} else if (spilloverincrement > 0) {
							zonevolumes[i]++;
							spilloverincrement--;
							if (zonevolumes[i] == 100) {
								adjustablezones--;
							}
						}
					}
				}
			}
		}

		// now update the hardware using the "zonevolumes" int-array.
		idx = 0;
		for (String zoneID : members) {
			setZoneVolume(zoneID, zonevolumes[idx++]+"");
		}
	}

	private void setZoneMuted(String zoneID, String mute) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (mute == null) {
			missingParameterException("mute");
		}

		ZonePlayer zp = getZonePlayer(zoneID);
		RenderingControlService rcs = zp.getMediaRendererDevice()
		.getRenderingControlService();
		rcs.setMute(mute.equalsIgnoreCase("true") ? 1 : 0);
	}

	
	private void setZoneGroupMuted(String groupID, String mute) throws IOException, UPNPResponseException,	JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (mute == null) {
			missingParameterException("mute");
		}

		ZoneGroup zgroup = getZoneGroup(groupID);

		List<String> members = zgroup.getMembers();
		for (String zoneID : members) {
			setZoneMuted(zoneID, mute);
		}
	}

	/**Make a zone player perform a play/stop/pause/previous/next-command
	 * @throws JanosWebException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * */
	private void setZoneCommand(String zoneID, String command) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (command == null) {
			missingParameterException("command");
		}
		AVTransportService ats = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService();
		switch (TransportAction.valueOf(command)) {
		case Play: ats.play(); break; 
		case Pause: ats.pause(); break; 
		case Stop: ats.stop(); break; 
		case Next: ats.next(); break; 
		case Previous: ats.previous(); break; 
		}
	}

	/**Make a zone group perform a play/stop/pause/previous/next-command
	 * @throws JanosWebException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * */
	private void setZoneGroupCommand(String groupID, String command) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (command == null) {
			missingParameterException("command");
		}
		setZoneCommand(getZoneGroup(groupID).getCoordinator(), command);
	}
	

	/**Set a ZonePlayer's playmode (shuffle/normal/etc.)
	 * @param the ZonePlayer's ID
	 * @return playmode*/
	private void setZonePlayMode(String zoneID, String playMode) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (playMode == null) {
			missingParameterException("playMode");
		}
		PlayMode playmode = PlayMode.NORMAL;
		try {
			playmode = PlayMode.valueOf(playMode);
		} catch (Exception e) {
			throw new JanosWebException("Playmode '"+playMode+"' is not supported");
		}
		getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().setPlayMode(playmode);
	}


	/**Set a ZoneGroup's playmode (shuffle/normal/etc.)
	 * @param the ZoneGroup's ID
	 * */
	private void setZoneGroupPlayMode(String groupID, String playMode) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (playMode == null) {
			missingParameterException("playMode");
		}
		setZonePlayMode(getZoneGroup(groupID).getCoordinator(), playMode);
	}

	
	/**Make a zone player skip to a position in a track
	 * @throws JanosWebException 
	 * @throws NumberFormatException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * */
	private void setZoneCurrentTrackPosition(String zoneID, String trackPosition) throws JanosWebException, NumberFormatException, IOException, UPNPResponseException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (trackPosition == null) {
			missingParameterException("trackPosition");
		}
		AVTransportService ats = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService();
		ats.seek(SeekTargetFactory.createRelTimeSeekTarget(Long.parseLong(trackPosition)));
	}


	
	/**Make a zone group skip to a position in a song
	 * @throws JanosWebException 
	 * @throws NumberFormatException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * */
	private void setZoneGroupTrackPosition(String groupID, String trackPosition) throws JanosWebException, NumberFormatException, IOException, UPNPResponseException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (trackPosition == null) {
			missingParameterException("trackPosition");
		}
		setZoneCurrentTrackPosition(getZoneGroup(groupID).getCoordinator(), trackPosition);
	}
	
	
	/**Set a ZonePlayer's current track
	 * @param the ZonePlayer's ID
	 * */
	private void setZoneEnqueue(String zoneID, String itemID, Integer position) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (itemID == null) {
			missingParameterException("itemID");
		}
		String upnpclass, res; //parentid
		//parentid = null;
		upnpclass = null;
		res = null;
		if (itemID.startsWith("A:ARTIST")) {
			//parentid = "A:ARTIST";
			upnpclass = "object.container.person.musicArtist";
			res = "x-rincon-playlist:"+zoneID+"#"+itemID;
		} else if (itemID.startsWith("A:ALBUM")) {
			//parentid = "A:ALBUM";
			upnpclass = "object.container.person.musicAlbum";
			res = "x-rincon-playlist:"+zoneID+"#"+itemID;
		} else if (itemID.substring(1).startsWith("://")) {
			//parentid = "A:TRACKS";
			upnpclass = "object.container.person.musicTrack";
			res = "x-file-cifs"+itemID.substring(1);
		} else {
			upnpclass = "object.container";
			res = "x-rincon-playlist:";
		}
		//getZonePlayer(zoneID).enqueueEntry(new Entry(itemID, null, parentid, null, null, null, upnpclass, res));
		if (position == null) {
			getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().addToQueue(new Entry(itemID, null, null, null, null, null, upnpclass, res));
		} else {
			int currentTrackNum = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().getPositionInfo().getTrackNum();
			int pos = position.intValue();
			if (pos > 0) {
				getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().addToQueue(new Entry(itemID, null, null, null, null, null, upnpclass, res), currentTrackNum+pos-1);
			} else { //if pos is zero or below, enqueue track as next track and skip to track
				AVTransportService serv = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService();
				serv.addToQueue(new Entry(itemID, null, null, null, null, null, upnpclass, res), currentTrackNum);
				serv.next();
			}
		}
	}

	private void setZoneGroupEnqueue(String groupID, String itemID, Integer position) throws JanosWebException, NumberFormatException, IOException, UPNPResponseException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (itemID == null) {
			missingParameterException("itemID");
		}
		setZoneEnqueue(getZoneGroup(groupID).getCoordinator(), itemID, position);
	}


	/**Clear a zonePlayer's queue
	 * @param the ZonePlayer's ID*/
	private void setZoneClearQueue(String zoneID) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().clearQueue();
	}

	private void setZoneGroupClearQueue(String groupID) throws JanosWebException, NumberFormatException, IOException, UPNPResponseException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		setZoneClearQueue(getZoneGroup(groupID).getCoordinator());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//---------------------------------------------------------------------------------
	//Helper and convenience methods
	//---------------------------------------------------------------------------------
	

	/**
	 * Get the total list of ZoneGroups visible to this application via
	 * SonosController
	 * 
	 * @return the list of ZoneGroup s
	 * */
	private List<ZoneGroup> getGroups() {
		List<ZonePlayer> zps = controller.getZonePlayerModel().getAllZones();
		if (zps.size() > 0) {
			List<ZoneGroup> groups = zps.get(0).getZoneGroupTopologyService().getGroupState().getGroups();
			for (ZoneGroup group : groups) {
				if (group.getMembers().size() > 0) {
					try {
						getZonePlayer(group.getCoordinator());
					} catch (JanosWebException e) {
						//remove zones with no coordinator
						LOG.debug("Removing zonegroup with groupID '"+group.getId()+"', because its coordinator with ZoneID '"+group.getCoordinator()+"' could not be located as a ZonePlayer");
						groups.remove(group);
					}
				}
			}
			return groups;
		} else {
			return new LinkedList<ZoneGroup>();
		}
	}
	
	
	
	/**Convenience method for checking if the volume is out of bounds
	 * @param volume the input volume
	 * @throws JanosWebException if the input volume is out of bounds [0, 100]*/
	private void checkVolume(int volume) throws JanosWebException {
		if (volume > 100) {
			throw new JanosWebException("Volume must not be greater than 100");
		} else if (volume < 0) {
			throw new JanosWebException("Volume must not be less than 0");
		}

	}

	
	/**Convenience method for getting the ZonePlayer with the given zoneID
	 * @param zoneID the ID of the ZonePlayer
	 * @returns the ZonePlayer with ID equal to zoneID
	 * @throws JanosWebException if no ZonePlayer with the given ID can be found*/
	private ZonePlayer getZonePlayer(String zoneID) throws JanosWebException {
		ZonePlayer zp = controller.getZonePlayerModel().getById(zoneID);
		if (zp == null) {
			throw new JanosWebException("Zoneplayer with zoneID '"+zoneID+"' could not be found");
		}
		return zp;
	}
	
	/**Convenience method for getting a ZonePlayer's name
	 * @param zp the ZonePlayer
	 * @return the ZonePlayer's name*/
	private String getZonePlayerName(ZonePlayer zp) {
		return zp.getDevicePropertiesService().getZoneAttributes().getName();
	}

	/**Convenience method for getting a ZonePlayer's icon
	 * @param zp the ZonePlayer
	 * @return the ZonePlayer's icon
	 * @throws MalformedURLException */
	private String getZonePlayerIcon(ZonePlayer zp) throws MalformedURLException {
		List<?> icons = zp.getMediaRendererDevice().getUPNPDevice().getDeviceIcons();
		if (icons == null || icons.isEmpty()) {
			LOG.error("No icon for zone with ID '"+zp.getId()+"' found.");
			return thishost + "/images/default-sonos-icon.png";
		}
		DeviceIcon icon = (DeviceIcon)icons.get(0);
		return icon.getUrl().toExternalForm();
	}

	/**
	 * @throws UPNPResponseException 
	 * @throws IOException */
	private boolean getZonePlaying(ZonePlayer zp) throws IOException, UPNPResponseException {
		return zp.getMediaRendererDevice().getAvTransportService().getTransportInfo().getState().equals(TransportState.PLAYING);
	}
	
	/**
	 * Get the ZoneGroup with the given ID
	 * 
	 * @param groupID
	 *            the id of the ZoneGroup you want
	 * @return the ZoneGroup you want
	 * @throws JanosWebException if the ZoneGroup cannot be found
	 * */
	private ZoneGroup getZoneGroup(String groupID) throws JanosWebException {
		List<ZoneGroup> zgs = getGroups();
		for (ZoneGroup zg : zgs) {
			if (zg.getId().equals(groupID)) {
				return zg;
			}
		}
		throw new JanosWebException("Could not find ZoneGroup with groupID: "+groupID);
	}
	
	private void missingParameterException(String parametername) throws JanosWebException {
		throw new JanosWebException("Missing parameter '"+parametername+"'");
	}
	

	private Element getZoneElement(String zoneID, boolean isSibling) throws JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		Element zone = new Element("zone", isSibling);
		zone.addChild(new Element("zoneID", zoneID));
		return zone;
	}
	
	private Element getZoneGroupElement(String groupID, boolean isSibling) throws JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		Element group = new Element("zoneGroup", isSibling);
		group.addChild(new Element("groupID", groupID));
		return group;
	}
	
	
	
	//Method and helper methods for generating the a small help for the servlet interface
	//NOTE: Only method names included in the Command will be listed by this method.
	//When changing this class beware that you keep the convention of keeping the exact
	//method name as an entry in the Command enum.
	private void printServletInterface(HttpServletResponse response) throws IOException {
		//TODO: improve this "help", though it will ruin the "no maintenance"-part
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.println("<html>");
		out.println("<head><title>Servlet interface</title></head>");
		out.println("<body>");
		out.println("<h3>This servlet provides these functionalies</h3>");
		out.println("<p>To use the functionalities, use the parameter '<tt>cmd</tt>', either via POST or GET</p>");
		out.println("<p>Example: <tt>cmd=getZoneGroups</tt></p>");
		out.println("<ul>");
		Method[] methods = this.getClass().getDeclaredMethods();
		for (Method method : methods) {
			String methodName = method.getName();
			try {
				Command.valueOf(methodName);
				out.println("<li><tt>"+methodName+"</tt></li>");
				
			} catch (Exception e) {
				//Don't print anything about methods not enumerated in Command
			}
		}
		out.println("</ul>");
		out.println("</body>");
		out.close();
	}

}
