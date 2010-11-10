package net.sf.janos.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sbbi.upnp.devices.DeviceIcon;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.RenderingControlService;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MediaInfo;
import net.sf.janos.model.PlayMode;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.model.SeekTargetFactory;
import net.sf.janos.model.TrackMetaData;
import net.sf.janos.model.TransportAction;
import net.sf.janos.model.TransportInfo.TransportState;
import net.sf.janos.model.ZoneGroup;
import net.sf.janos.web.exception.JanosWebException;
import net.sf.janos.web.model.MusicLibrary;
import net.sf.janos.web.structure.Element;
import net.sf.janos.web.structure.ElementUtil;
import net.sf.janos.web.structure.Formatter;
import net.sf.janos.web.structure.JSONFormatter;
import net.sf.janos.web.structure.XMLFormatter;

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

	private enum Command {
		getZoneGroups 
		, getPlayCommands
		, getPlayModes
		, getZoneGroupVolume 
		, setZoneGroupVolume 
		, setZoneGroupMuted 
		, getZoneGroupPlayInfo
		, setZoneGroupCommand
		, setZoneGroupPlayMode
		, getZoneGroupTrackPosition
		, setZoneGroupTrackPosition
		, getZoneGroupTrack
		, getZoneGroupQueue
		, getZoneGroupArtists
		, getZoneGroupAlbums
		, getZoneGroupTracks
		, getZoneGroupSearch
		, setZoneGroupEnqueue
		, setZoneGroupEnqueueNext
		, setZoneGroupEnqueueNow
		, setZoneGroupClearQueue

		, getZoneVolume
		, setZoneVolume 
		, setZoneMuted
		, getZonePlayInfo
		, setZoneCommand
		, setZonePlayMode
		, getZoneTrackPosition
		, setZoneTrackPosition
		, getZoneTrack
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
		// this.controller.getZoneGroupStateModel().addListener(this);
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
						request.getParameter("zoneMuted"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneCommand:
				setZoneCommand(request.getParameter("zoneID"), request.getParameter("zoneCommand"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZonePlayInfo:
				resp.addChild(getZonePlayInfo(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZonePlayMode:
				setZonePlayMode(request.getParameter("zoneID"), request.getParameter("zonePlayMode"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneTrackPosition:
				resp.addChild(getZoneTrackPosition(request.getParameter("zoneID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneTrackPosition:
				setZoneTrackPosition(request.getParameter("zoneID"), request.getParameter("zoneTrackPosition"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneTrack:
				resp.addChild(getZoneTrack(request.getParameter("zoneID")));
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
						request.getParameter("groupVolume"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupMuted:
				setZoneGroupMuted(request.getParameter("groupID"),
						request.getParameter("groupMuted"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupPlayInfo:
				resp.addChild(getZoneGroupPlayInfo(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupCommand:
				setZoneGroupCommand(request.getParameter("groupID"), request.getParameter("groupCommand"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupPlayMode:
				setZoneGroupPlayMode(request.getParameter("groupID"), request.getParameter("groupPlayMode"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupTrackPosition:
				resp.addChild(getZoneGroupTrackPosition(request.getParameter("groupID")));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case setZoneGroupTrackPosition:
				setZoneGroupTrackPosition(request.getParameter("groupID"), request.getParameter("groupTrackPosition"));
				resp.addChildFirst(ElementUtil.getStatusSuccesElement());
				break;
			case getZoneGroupTrack:
				resp.addChild(getZoneGroupTrack(request.getParameter("groupID")));
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
	}

	

	
	
	
	
	
	
	//---------------------------------------------------------------------------------
	//Top level methods, that return Element representations of the objects involved
	//---------------------------------------------------------------------------------
	
	/**
	 * Returns an Element-representation of the ZonePlayer's volume
	 * 
	 * @param zoneID
	 * @return an Element representation of a ZonePlayer and it's volume
	 * @throws JanosWebException 
	 */
	private Element getZoneVolume(String zoneID)
			throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		ZonePlayer zp = getZonePlayer(zoneID);
		RenderingControlService rcs = zp.getMediaRendererDevice()
				.getRenderingControlService();
		Element volinfo = new Element("volumeInfo");
		volinfo.addChild(new Element("volume", rcs.getVolume() + ""));
		volinfo.addChild(new Element("muted", rcs.getMute() ? "true" : "false"));
		return volinfo;
	}

	private void setZoneVolume(String zoneID, String volume)
			throws NumberFormatException, IOException, UPNPResponseException, JanosWebException {
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

	private void setZoneMuted(String zoneID, String zoneMuted)
			throws NumberFormatException, IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (zoneMuted == null) {
			missingParameterException("zoneMuted");
		}

		ZonePlayer zp = getZonePlayer(zoneID);
		RenderingControlService rcs = zp.getMediaRendererDevice()
				.getRenderingControlService();
		rcs.setMute(zoneMuted.equalsIgnoreCase("true") ? 1 : 0);
	}

	/**Get an Element-represenation of a ZonePlayer's playmode (shuffle/normal/etc.)
	 * @param the ZonePlayer's ID
	 * @return playmode*/
	private Element getZonePlayInfo(String zoneID) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		ZonePlayer zp = getZonePlayer(zoneID);
		AVTransportService avts = zp.getMediaRendererDevice().getAvTransportService();
		boolean playing = avts.getTransportInfo().getState().equals(TransportState.PLAYING);

		Element playinfo = new Element("playInfo");
		playinfo.addChild(new Element("isPlaying", playing ? "true" : "false"));
		playinfo.addChild(new Element("playMode", avts.getTransportSettings().getPlayMode()));
		return playinfo;
	}

	/**Make a zoneplayer perform a play/stop/pause/previous/next-command
	 * @throws JanosWebException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * */
	private void setZoneCommand(String zoneID, String zoneCommand) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (zoneCommand == null) {
			missingParameterException("zoneCommand");
		}
		AVTransportService ats = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService();
		switch (TransportAction.valueOf(zoneCommand)) {
		case Play: ats.play(); break; 
		case Pause: ats.pause(); break; 
		case Stop: ats.stop(); break; 
		case Next: ats.next(); break; 
		case Previous: ats.previous(); break; 
		}
	}


	/**Set a ZonePlayer's playmode (shuffle/normal/etc.)
	 * @param the ZonePlayer's ID
	 * @return playmode*/
	private void setZonePlayMode(String zoneID, String zonePlayMode) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (zonePlayMode == null) {
			missingParameterException("zonePlayMode");
		}
		PlayMode playmode = PlayMode.NORMAL;
		try {
			playmode = PlayMode.valueOf(zonePlayMode);
		} catch (Exception e) {
			throw new JanosWebException("Playmode '"+zonePlayMode+"' is not supported");
		}
		getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().setPlayMode(playmode);
	}
	
	/**Get an Element-represenation of a ZonePlayer's current track position
	 * @param the ZonePlayer's ID
	 * @return song position*/
	private Element getZoneTrackPosition(String zoneID) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		PositionInfo posinfo = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().getPositionInfo();
		Element track = new Element("track");
		track.addChild(new Element("trackDuration", posinfo.getTrackDuration()+""));
		track.addChild(new Element("trackPosition", posinfo.getRelTime()+""));
		return track;
	}
	
	/**Make a zoneplayer skip to a position in a track
	 * @throws JanosWebException 
	 * @throws NumberFormatException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * */
	private void setZoneTrackPosition(String zoneID, String zoneTrackPosition) throws JanosWebException, NumberFormatException, IOException, UPNPResponseException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (zoneTrackPosition == null) {
			missingParameterException("zoneTrackPosition");
		}
		AVTransportService ats = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService();
		ats.seek(SeekTargetFactory.createRelTimeSeekTarget(Long.parseLong(zoneTrackPosition)));
	}

	/**Get an Element-represenation of a ZonePlayer's current track
	 * @param the ZonePlayer's ID
	 * @return song position
	 * @throws SAXException */
	private Element getZoneTrack(String zoneID) throws IOException, UPNPResponseException, JanosWebException, SAXException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		ZonePlayer zp = getZonePlayer(zoneID);
		PositionInfo posinfo = zp.getMediaRendererDevice().getAvTransportService().getPositionInfo();
		MediaInfo mediainfo = zp.getMediaRendererDevice().getAvTransportService().getMediaInfo();
		String uri = mediainfo.getCurrentURI();
		
		Element track = new Element("track");

		if (uri == null || posinfo == null) {
			track.addChild(new Element("noMusic", "true"));
		} else if (uri.startsWith("x-rincon-queue:")) {
			TrackMetaData trackmeta = posinfo.getTrackMetaData();
			if ( trackmeta != null ) {
				// Playing from Queue
				track.addChild(new Element("trackArtist", trackmeta.getCreator()));
				track.addChild(new Element("trackAlbum", trackmeta.getAlbum()));
				track.addChild(new Element("trackTitle", trackmeta.getTitle()));
				track.addChild(new Element("trackAlbumArt", trackmeta.getAlbumArtUrl(zp).toExternalForm()));
				track.addChild(new Element("trackAlbumArtist", trackmeta.getAlbumArtist()));
				track.addChild(new Element("queueIndex", posinfo.getTrackNum()-1+""));

			} else {
				track.addChild(new Element("noMusic", "true"));
			}
		} else if (uri.startsWith("x-rincon:")){
			// This means that we are handling a ZonePlayer that is not the coordinator of its group
			// Fix it, by finding the coordinator and showing its details ;)
			String coordID = getZoneGroupFromMember(zoneID).getCoordinator();
			//Avoid endless recursion
			if (zoneID.equals(coordID)) {
				track.addChild(new Element("music", "unknown2"));
			} else {
				return getZoneTrack(coordID);
			}
		} else if (uri.startsWith("x-file-cifs:")) {
			// just playing one file
			track.addChild(new Element("trackArtist", mediainfo.getCurrentURIMetaData().getCreator()));
			track.addChild(new Element("trackAlbum", mediainfo.getCurrentURIMetaData().getAlbum()));
			track.addChild(new Element("trackTitle", mediainfo.getCurrentURIMetaData().getTitle()));
			track.addChild(new Element("trackAlbumArt", mediainfo.getCurrentURIMetaData().getAlbumArtUrl(zp).toExternalForm()));
			track.addChild(new Element("trackAlbumArtist", mediainfo.getCurrentURIMetaData().getAlbumArtist()));

		} else if (uri.startsWith("x-rincon-mp3radio:")) {
			// yep, it's the radio
			track.addChild(new Element("trackArtist", mediainfo.getCurrentURIMetaData().getCreator()));
			track.addChild(new Element("trackAlbum", mediainfo.getCurrentURIMetaData().getAlbum()));
			track.addChild(new Element("trackTitle", mediainfo.getCurrentURIMetaData().getTitle()));
			track.addChild(new Element("trackAlbumArt", thishost+"/images/internetradio.png"));
			track.addChild(new Element("trackAlbumArtist", mediainfo.getCurrentURI()));
		} else if (uri.startsWith("x-rincon-stream:")) {
			// line in stream
			track.addChild(new Element("trackArtist", "N/A"));
			track.addChild(new Element("trackAlbum", "N/A"));
			track.addChild(new Element("trackTitle", "Local Line In"));
			track.addChild(new Element("trackAlbumArt", thishost+"/images/linein.png"));
			track.addChild(new Element("trackAlbumArtist", "N/A"));
		} else if (uri.startsWith("pndrradio:")) {
			// Pandora
			try {
				TrackMetaData trackmeta = posinfo.getTrackMetaData();
				track.addChild(new Element("trackArtist", "Pandora: " + trackmeta.getCreator()));
				track.addChild(new Element("trackAlbum", "Pandora: " + trackmeta.getAlbum()));
				track.addChild(new Element("trackTitle", "Pandora: " + trackmeta.getTitle()));
				track.addChild(new Element("trackAlbumArt", new URL(trackmeta.getAlbumArtUri()).toExternalForm()));
				track.addChild(new Element("trackAlbumArtist", "Pandora: " + trackmeta.getAlbumArtist()));
			} catch (Exception e) {
				track.addChild(new Element("noMusic", "true"));
			}

		} else if (uri.startsWith("rdradio:station:")) {
			// Rhapsody Station
				try {
					TrackMetaData trackmeta = posinfo.getTrackMetaData();
					track.addChild(new Element("trackArtist", "Rhapsody: " + trackmeta.getCreator()));
					track.addChild(new Element("trackAlbum", "Rhapsody: " + trackmeta.getAlbum()));
					track.addChild(new Element("trackTitle", "Rhapsody: " + trackmeta.getTitle()));
					track.addChild(new Element("trackAlbumArt", new URL(trackmeta.getAlbumArtUri()).toExternalForm()));
					track.addChild(new Element("trackAlbumArtist", "Rhapsody: " + trackmeta.getAlbumArtist()));
				} catch (Exception e) {
					track.addChild(new Element("noMusic", "true"));
				}
		} else if (uri.startsWith("lastfm:")) {
			// last.fm Station
			try {
				TrackMetaData trackmeta = posinfo.getTrackMetaData();
				track.addChild(new Element("trackArtist", "last.fm: " + trackmeta.getCreator()));
				track.addChild(new Element("trackAlbum", "last.fm: " + trackmeta.getAlbum()));
				track.addChild(new Element("trackTitle", "last.fm: " + trackmeta.getTitle()));
				track.addChild(new Element("trackAlbumArt", new URL(trackmeta.getAlbumArtUri()).toExternalForm()));
				track.addChild(new Element("trackAlbumArtist", "last.fm: " + trackmeta.getAlbumArtist()));
			} catch (Exception e) {
				track.addChild(new Element("noMusic", "true"));
			}

		} else if (uri.startsWith("x-sonosapi-stream:")) {
			// Local Radio
			try {
				TrackMetaData trackmeta = posinfo.getTrackMetaData();
				track.addChild(new Element("trackArtist", "Radio: " + trackmeta.getCreator()));
				track.addChild(new Element("trackAlbum", "Radio: " + trackmeta.getAlbum()));
				track.addChild(new Element("trackTitle", "Radio: " + trackmeta.getTitle()));
				track.addChild(new Element("trackAlbumArt", trackmeta.getAlbumArtUrl(zp).toExternalForm()));
				track.addChild(new Element("trackAlbumArtist", "Radio: " + trackmeta.getAlbumArtist()));
				
			} catch (Exception e) {
				track.addChild(new Element("noMusic", "true"));
			}

		} else {
			if (LOG.isWarnEnabled() && mediainfo != null ) {
				LOG.warn("Couldn't find type of " + uri);
				track.addChild(new Element("music", "unknown"));
			}
		}
		return track;
	}
	
	/**Get an Element-represenation of a ZonePlayer's queue
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the queue. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneQueue(String zoneID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		int startidx = 0;
		int length = Integer.MAX_VALUE;
		if (startIndex != null) {
			startidx = Integer.parseInt(startIndex);
		}
		if (numEntries != null) {
			length = Integer.parseInt(numEntries);
		}
		ZonePlayer zp = getZonePlayer(zoneID);
		Element queue = new Element("queue");
		for (Entry e : zp.getMediaServerDevice().getContentDirectoryService().getQueue(startidx, length)) {
			Element track = new Element("track", true);
			track.addChild(new Element("artist", e.getCreator()));
			track.addChild(new Element("album", e.getAlbum()));
			track.addChild(new Element("title", e.getTitle()));
			track.addChild(new Element("albumArt", e.getAlbumArtURL(zp).toExternalForm()));
			track.addChild(new Element("no", e.getOriginalTrackNumber()+""));
			track.addChild(new Element("id", e.getId()));
			queue.addChild(track);
		}
		return queue;
	}
	
	/**Get an Element-represenation of a ZonePlayer's queue
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the queue. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneQueue2(String zoneID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		int readidx = 0;
		int length = Integer.MAX_VALUE;
		if (startIndex != null) {
			readidx = Integer.parseInt(startIndex);
		}
		if (numEntries != null) {
			length = Integer.parseInt(numEntries);
		}
		
		ZonePlayer zp = getZonePlayer(zoneID);
		MusicLibrary musiclib = new MusicLibrary(zp, new Entry("Q:0", null, null, null, null, null, null, null));

		Element queue = new Element("queue");
			for (Entry e : musiclib.getEntries(readidx, readidx+length)) {
				Element track = new Element("track", true);
				track.addChild(new Element("artist", e.getCreator()));
				track.addChild(new Element("album", e.getAlbum()));
				track.addChild(new Element("title", e.getTitle()));
				track.addChild(new Element("albumArt", e.getAlbumArtURL(zp).toExternalForm()));
				track.addChild(new Element("no", e.getOriginalTrackNumber()+""));
				track.addChild(new Element("id", e.getId()));
				queue.addChild(track);
			}
		return queue;
	}

	

	
	/**Get an Element-represenation of a ZonePlayer's list of artists
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the list of artists. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneArtists(String zoneID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		int readidx = 0;
		int length = Integer.MAX_VALUE;
		if (startIndex != null) {
			readidx = Integer.parseInt(startIndex);
		}
		if (numEntries != null) {
			length = Integer.parseInt(numEntries);
		}
		
		ZonePlayer zp = getZonePlayer(zoneID);
		MusicLibrary musiclib = new MusicLibrary(zp, new Entry("A:ARTIST", null, null, null, null, null, null, null));
		Element artists = new Element("artists");
		for (Entry e : musiclib.getEntries(readidx, readidx+length)) {
			Element artist = new Element("artist", true);
			artist.addChild(new Element("name", e.getTitle()));
			artist.addChild(new Element("id", e.getId()));
			artists.addChild(artist);
		}
		return artists;

/*
        //Commenting out old version, that used the synchronous call to get albums.
		int buffer = 1000;
		while (length > 0) {
			List<Entry> entrylist = zp.getMediaServerDevice().getContentDirectoryService().getArtists(readidx, Math.min(Math.min(buffer, length), Integer.MAX_VALUE - readidx));
			for (Entry e : entrylist) {
				artists.addChild(new Element("artist", ElementUtil.createSafeContentString(e.getTitle())));
			}
			//Don't continue calling if there are no more entries after this call
			//this is useful if the input for number of elements is way higher than the total number of entries.
			if (entrylist.size() < buffer) {
				break;
			}
			length -= buffer;
			readidx += buffer;
		}
		return artists;
*/		
	}
	
	/**Get an Element-represenation of a ZonePlayer's list of albums
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the list of albums. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneAlbums(String zoneID, String startIndex, String numEntries, String artist) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		int readidx = 0;
		int length = Integer.MAX_VALUE;
		if (startIndex != null) {
			readidx = Integer.parseInt(startIndex);
		}
		if (numEntries != null) {
			length = Integer.parseInt(numEntries);
		}
		
		ZonePlayer zp = getZonePlayer(zoneID);
		MusicLibrary musiclib;
		if (artist == null) {
			musiclib = new MusicLibrary(zp, new Entry("A:ALBUM", null, null, null, null, null, null, null));
		} else {
			musiclib = new MusicLibrary(zp, new Entry("A:ARTIST/"+artist, null, null, null, null, null, null, null));
		}
		Element albums = new Element("albums");
		for (Entry e: musiclib.getEntries(readidx, readidx+length)) {
			String title =  e.getTitle();
			String creator = e.getCreator();
			//Don't add the "all" meta-album
			if (creator == null || title==null || title.equals("All") && creator.equals("")) {
				continue;
			}
			Element album = new Element("album", true);
			album.addChild(new Element("title", title));
			album.addChild(new Element("artist", creator));
			album.addChild(new Element("albumart", e.getAlbumArtURL(zp).toExternalForm()));
			album.addChild(new Element("id", e.getId()));
			albums.addChild(album);

		}
		return albums;
	}

	/**Get an Element-represenation of a ZonePlayer's list of tracks
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the list of tracks. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneTracks(String zoneID, String startIndex, String numEntries, String artist, String album) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		int readidx = 0;
		int length = Integer.MAX_VALUE;
		if (startIndex != null) {
			readidx = Integer.parseInt(startIndex);
		}
		if (numEntries != null) {
			length = Integer.parseInt(numEntries);
		}
		
		ZonePlayer zp = getZonePlayer(zoneID);
		MusicLibrary musiclib;
		if (artist != null && album == null) {
			musiclib = new MusicLibrary(zp, new Entry("A:ARTIST/"+artist+"/", null, null, null, null, null, null, null));
		} else if (artist != null && album != null) {
			musiclib = new MusicLibrary(zp, new Entry("A:ARTIST/"+artist+"/"+album, null, null, null, null, null, null, null));
		} else if (artist == null && album != null) {
			musiclib = new MusicLibrary(zp, new Entry("A:ALBUM/"+album, null, null, null, null, null, null, null));
		} else { //if (artist == null && album == null)
			musiclib = new MusicLibrary(zp, new Entry("A:TRACKS", null, null, null, null, null, null, null));
		}

		Element tracks = new Element("tracks");
			for (Entry e : musiclib.getEntries(readidx, readidx+length)) {
				Element track = new Element("track", true);
				track.addChild(new Element("artist", e.getCreator()));
				track.addChild(new Element("album", e.getAlbum()));
				track.addChild(new Element("title", e.getTitle()));
				track.addChild(new Element("albumArt", e.getAlbumArtURL(zp).toExternalForm()));
				track.addChild(new Element("no", e.getOriginalTrackNumber()+""));
				track.addChild(new Element("id", e.getId()));
				tracks.addChild(track);
			}
		return tracks;
	}

	/**Get an Element-represenation of a ZonePlayer's list of tracks
	 * @param the ZonePlayer's ID
	 * @param startIndex starting index of the list of tracks. null is treated as "0"
	 * @param numEntries number of entries from the starting index. null is treated as Integer.MAX_VALUE 
	 * @return queue*/
	private Element getZoneSearch(String zoneID, String startIndex, String numEntries, String searchData) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		if (searchData == null) {
			missingParameterException("searchData");
		}
		int readidx = 0;
		int length = Integer.MAX_VALUE;
		if (startIndex != null) {
			readidx = Integer.parseInt(startIndex);
		}
		if (numEntries != null) {
			length = Integer.parseInt(numEntries);
		}
		
		ZonePlayer zp = getZonePlayer(zoneID);
		MusicLibrary artistmusiclib = new MusicLibrary(zp, new Entry("A:ARTIST:"+searchData, null, null, null, null, null, null, null));
		MusicLibrary albumartistmusiclib = new MusicLibrary(zp, new Entry("A:ALBUMARTIST:"+searchData, null, null, null, null, null, null, null));
		MusicLibrary albummusiclib = new MusicLibrary(zp, new Entry("A:ALBUM:"+searchData, null, null, null, null, null, null, null));
		MusicLibrary trackmusiclib = new MusicLibrary(zp, new Entry("A:TRACKS:"+searchData, null, null, null, null, null, null, null));

		List<Entry> entries = artistmusiclib.getEntries();
		entries.addAll(albumartistmusiclib.getEntries());
		entries.addAll(albummusiclib.getEntries());
		entries.addAll(trackmusiclib.getEntries());
		//No results after the last entry
		if (readidx > entries.size() - 1)
			return new Element("results");
		
		Element results = new Element("results");
			for (Entry e : entries.subList(readidx, Math.min(readidx+length, entries.size()-1))) {
				Element result = new Element("result", true);
				String type = e.getId();
				if (type.startsWith("A:ARTIST") || type.startsWith("A:ALBUMARTIST")) {
					result.addChild(new Element("type", "artist"));
					result.addChild(new Element("artist", e.getTitle()));
					result.addChild(new Element("id", e.getId()));
				} else if (type.startsWith("A:ALBUM") && !type.startsWith("A:ALBUMARTIST")) {
					result.addChild(new Element("type", "album"));
					Element album = new Element("album");
					album.addChild(new Element("title", e.getTitle()));
					album.addChild(new Element("artist", e.getCreator()));
					album.addChild(new Element("albumart", e.getAlbumArtURL(zp).toExternalForm()));
					album.addChild(new Element("id", e.getId()));
					result.addChild(album);
				} else if (type.substring(1).startsWith("://")) {
					result.addChild(new Element("type", "track"));
					Element track = new Element("track");
					track.addChild(new Element("artist", e.getCreator()));
					track.addChild(new Element("album", e.getAlbum()));
					track.addChild(new Element("title", e.getTitle()));
					track.addChild(new Element("albumArt", e.getAlbumArtURL(zp).toExternalForm()));
					track.addChild(new Element("no", e.getOriginalTrackNumber()+""));
					track.addChild(new Element("id", e.getId()));
					result.addChild(track);
				}
				results.addChild(result);
			}
		return results;
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

	/**Clear a zonePlayer's queue
	 * @param the ZonePlayer's ID*/
	private void setZoneClearQueue(String zoneID) throws IOException, UPNPResponseException, JanosWebException {
		if (zoneID == null) {
			missingParameterException("zoneID");
		}
		getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().clearQueue();
	}

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
				group.addChild(new Element("groupIcon", getZonePlayerIcon(coord)));
				group.addChild(new Element("groupIsPlaying", getZoneIsPlaying(coord) ? "true" : "false"));
				Element gmembers = new Element("groupMembers");
				for (String zoneID : zgroup.getMembers()) {
					Element zone = new Element("zone", true);
					zone.addChild(new Element("zoneID", zoneID));
					ZonePlayer zp = getZonePlayer(zoneID);
					zone.addChild(new Element("zoneName", getZonePlayerName(zp)));
					zone.addChild(new Element("zoneIcon", getZonePlayerIcon(zp)));
					zone.addChild(new Element("zoneIsPlaying", getZoneIsPlaying(zp) ? "true" : "false"));
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
		modes.addChild(new Element("mode", "REPEAT_ONE"));
		modes.addChild(new Element("mode", "REPEAT_ALL"));
		modes.addChild(new Element("mode", "RANDOM"));
		modes.addChild(new Element("mode", "DIRECT_1"));
		modes.addChild(new Element("mode", "INTRO"));
		return modes;
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
	private Element getZoneGroupVolume(String groupID)
			throws JanosWebException, IOException, UPNPResponseException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		ZoneGroup zgroup = getZoneGroup(groupID);
		int totalvolume = 0;
		boolean groupmuted = true;
		List<String> members = zgroup.getMembers();
		Element zonegroup = new Element("group");
		for (String zoneID : members) {
			ZonePlayer zp = getZonePlayer(zoneID);
			RenderingControlService rcs = zp.getMediaRendererDevice()
			.getRenderingControlService();
			int volume = rcs.getVolume();
			totalvolume += volume;
			boolean zonemuted = rcs.getMute();
			groupmuted = groupmuted && zonemuted;
			Element zone = new Element("zone", true);
			zone.addChild(new Element("zoneID", zoneID));
			//zone.addChild(new Element("zoneName", getZonePlayerName(zp)));
			//here I should really call zone.addChild(getZoneVolume(zoneID)), but it's not efficient to do so
			Element volinfo = new Element("volumeInfo");
			volinfo.addChild(new Element("volume", volume + ""));
			volinfo.addChild(new Element("muted", zonemuted ? "true" : "false"));
			zone.addChild(volinfo);
			zonegroup.addChild(zone);
		}
		zonegroup.addChildFirst(new Element("muted", groupmuted ? "true" : "false"));
		zonegroup.addChildFirst(new Element("volume", totalvolume / members.size() + ""));
		zonegroup.addChildFirst(new Element("groupID", groupID));
		return zonegroup;
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
	private void setZoneGroupVolume(String groupID, String groupVolume)
			throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (groupVolume == null) {
			missingParameterException("groupVolume");
		}
		int vol = Integer.parseInt(groupVolume);
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
			int volume = zp.getMediaRendererDevice()
					.getRenderingControlService().getVolume();
			zonevolumes[idx++] = volume;
			totalvolume += volume;
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

	private void setZoneGroupMuted(String groupID,
			String groupMuted) throws IOException, UPNPResponseException,
			JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (groupMuted == null) {
			missingParameterException("groupMuted");
		}

		ZoneGroup zgroup = getZoneGroup(groupID);

		List<String> members = zgroup.getMembers();
		for (String zoneID : members) {
			setZoneMuted(zoneID, groupMuted);
		}
	}

	/**Get an Element-represenation of a ZoneGroup's playstate (playing or not)
	 * @param groupID the ZoneGroup's ID
	 * @return playstate*/
	private Element getZoneGroupPlayInfo(String groupID) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		String coordinator = getZoneGroup(groupID).getCoordinator();
		return getZonePlayInfo(coordinator);
	}

	/**Make a zone group perform a play/stop/pause/previous/next-command
	 * @throws JanosWebException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * */
	private void setZoneGroupCommand(String groupID, String groupCommand) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (groupCommand == null) {
			missingParameterException("groupCommand");
		}
		setZoneCommand(getZoneGroup(groupID).getCoordinator(), groupCommand);
	}
	
	/**Set a ZoneGroup's playmode (shuffle/normal/etc.)
	 * @param the ZoneGroup's ID
	 * */
	private void setZoneGroupPlayMode(String groupID, String groupPlayMode) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (groupPlayMode == null) {
			missingParameterException("groupPlayMode");
		}
		setZonePlayMode(getZoneGroup(groupID).getCoordinator(), groupPlayMode);
	}

	/**Get an Element-represenation of a ZoneGroup's current song position
	 * @param groupID the ZoneGroup's ID
	 * @return track position*/
	private Element getZoneGroupTrackPosition(String groupID) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return getZoneTrackPosition(getZoneGroup(groupID).getCoordinator());
	}
	
	/**Make a zone group skip to a position in a song
	 * @throws JanosWebException 
	 * @throws NumberFormatException 
	 * @throws UPNPResponseException 
	 * @throws IOException 
	 * */
	private void setZoneGroupTrackPosition(String groupID, String groupTrackPosition) throws JanosWebException, NumberFormatException, IOException, UPNPResponseException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (groupTrackPosition == null) {
			missingParameterException("groupTrackPosition");
		}
		setZoneTrackPosition(getZoneGroup(groupID).getCoordinator(), groupTrackPosition);
	}
	
	private Element getZoneGroupTrack(String groupID) throws IOException, UPNPResponseException, JanosWebException, SAXException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return getZoneTrack(getZoneGroup(groupID).getCoordinator());
	}

	private Element getZoneGroupQueue(String groupID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return getZoneQueue2(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries);
	}

	private Element getZoneGroupArtists(String groupID, String startIndex, String numEntries) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return getZoneArtists(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries);
	}

	private Element getZoneGroupAlbums(String groupID, String startIndex, String numEntries, String artist) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return getZoneAlbums(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries, artist);
	}
	
	private Element getZoneGroupTracks(String groupID, String startIndex, String numEntries, String artist, String album) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		return getZoneTracks(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries, artist, album);
	}
	
	
	private Element getZoneGroupSearch(String groupID, String startIndex, String numEntries, String searchData) throws IOException, UPNPResponseException, JanosWebException {
		if (groupID == null) {
			missingParameterException("groupID");
		}
		if (searchData == null) {
			missingParameterException("searchData");
		}
		return getZoneSearch(getZoneGroup(groupID).getCoordinator(), startIndex, numEntries, searchData);
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
	
	
	/**Convenience method for finding the ZoneGroup that a given ZonePlayer belongs to
	 * @param zoneID, the ID of the ZonePlayer
	 * @return the ZoneGroup that the ZonePlayer is member of.
	 * */
	private ZoneGroup getZoneGroupFromMember(String zoneID) throws JanosWebException {
		ZonePlayer zp = getZonePlayer(zoneID);
		for (ZoneGroup zg : zp.getZoneGroupTopologyService().getGroupState().getGroups()) {
			for (String member : zg.getMembers()) {
				if (member.equals(zoneID)) {
					return zg;
				}
			}
		}
		return null;
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
	private boolean getZoneIsPlaying(ZonePlayer zp) throws IOException, UPNPResponseException {
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
