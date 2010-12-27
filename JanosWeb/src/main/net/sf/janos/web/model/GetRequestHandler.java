package net.sf.janos.web.model;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.RenderingControlService;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MediaInfo;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.model.TrackMetaData;
import net.sf.janos.model.ZoneGroup;
import net.sf.janos.model.TransportInfo.TransportState;
import net.sf.janos.web.exception.JanosWebException;
import net.sf.janos.web.servlets.JanosWebServlet;
import net.sf.janos.web.structure.Element;

public class GetRequestHandler {
	private SonosController controller;
	private static final Log LOG = LogFactory.getLog(JanosWebServlet.class);

	public GetRequestHandler(SonosController controller) {
		this.controller = controller;
	}



	public void getZoneVolume(String zoneID, Element parent)
	throws IOException, UPNPResponseException, JanosWebException {
		ZonePlayer zp = getZonePlayer(zoneID);
		RenderingControlService rcs = zp.getMediaRendererDevice().getRenderingControlService();
		parent.addChild(new Element("volume", rcs.getVolume() + ""));
		parent.addChild(new Element("muted", rcs.getMute() ? "true" : "false"));
	}

	public void getZoneGroupVolume(String groupID, Element parent) throws JanosWebException, IOException, UPNPResponseException {
		ZoneGroup zgroup = getZoneGroup(groupID);
		int totalvolume = 0;
		boolean groupmuted = true;
		List<String> members = zgroup.getMembers();
		for (String zoneID : members) {
			ZonePlayer zp = getZonePlayer(zoneID);
			RenderingControlService rcs = zp.getMediaRendererDevice()
			.getRenderingControlService();
			int volume = rcs.getVolume();
			totalvolume += volume;
			boolean zonemuted = rcs.getMute();

			groupmuted = groupmuted && zonemuted;
			//here I should really call zone.addChild(getZoneVolume(zoneID)), but it's not efficient to do so
			Element zone = new Element("zone", true);
			zone.addChild(new Element("zoneID", zoneID));
			//zone.addChild(new Element("zoneName", getZonePlayerName(zp)));
			zone.addChild(new Element("volume", volume + ""));
			zone.addChild(new Element("muted", zonemuted ? "true" : "false"));
			//Element zone = getZoneVolume(zoneID);
			parent.addChild(zone);
		}
		parent.addChildFirst(new Element("muted", groupmuted ? "true" : "false"));
		parent.addChildFirst(new Element("volume", totalvolume / members.size() + ""));
	}


	public void getZonePlayInfo(String zoneID, Element parent) throws IOException, UPNPResponseException, JanosWebException {
		ZonePlayer zp = getZonePlayer(zoneID);
		AVTransportService avts = zp.getMediaRendererDevice().getAvTransportService();
		boolean playing = avts.getTransportInfo().getState().equals(TransportState.PLAYING);
		parent.addChild(new Element("playing", playing ? "true" : "false"));
		parent.addChild(new Element("playMode", avts.getTransportSettings().getPlayMode()));
	}

	public void getZoneCurrentTrackPosition(String zoneID, Element parent) throws IOException, UPNPResponseException, JanosWebException {
		PositionInfo posinfo = getZonePlayer(zoneID).getMediaRendererDevice().getAvTransportService().getPositionInfo();
		Element track = new Element("currentTrack");
		track.addChild(new Element("duration", posinfo.getTrackDuration()+""));
		track.addChild(new Element("position", posinfo.getRelTime()+""));
		parent.addChild(track);
	}

	public void getZoneCurrentTrack(String zoneID, Element parent, String thishost) throws IOException ,UPNPResponseException, JanosWebException, SAXException {
		ZonePlayer zp = getZonePlayer(zoneID);
		PositionInfo posinfo = zp.getMediaRendererDevice().getAvTransportService().getPositionInfo();
		MediaInfo mediainfo = zp.getMediaRendererDevice().getAvTransportService().getMediaInfo();
		String uri = mediainfo.getCurrentURI();

		Element track = new Element("currentTrack");

		if (uri == null || posinfo == null) {
			track.addChild(new Element("noMusic", "true"));
		} else if (uri.startsWith("x-rincon-queue:")) {
			TrackMetaData trackmeta = posinfo.getTrackMetaData();
			if ( trackmeta != null ) {
				// Playing from Queue
				track.addChild(new Element("artist", trackmeta.getCreator()));
				track.addChild(new Element("album", trackmeta.getAlbum()));
				track.addChild(new Element("title", trackmeta.getTitle()));
				URL aart = trackmeta.getAlbumArtUrl(zp);
				if (aart == null) {
					aart = new URL(thishost+"/images/cd.gif");
				}
				track.addChild(new Element("albumArt", aart.toExternalForm()));
				track.addChild(new Element("albumArtist", trackmeta.getAlbumArtist()));
				track.addChild(new Element("queueIndex", posinfo.getTrackNum()+""));
				track.addChild(new Element("duration", posinfo.getTrackDuration()+""));
				track.addChild(new Element("position", posinfo.getRelTime()+""));


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
				getZoneCurrentTrack(coordID, parent, thishost);
			}
		} else if (uri.startsWith("x-file-cifs:")) {
			// just playing one file
			track.addChild(new Element("artist", mediainfo.getCurrentURIMetaData().getCreator()));
			track.addChild(new Element("album", mediainfo.getCurrentURIMetaData().getAlbum()));
			track.addChild(new Element("title", mediainfo.getCurrentURIMetaData().getTitle()));
			track.addChild(new Element("albumArt", mediainfo.getCurrentURIMetaData().getAlbumArtUrl(zp).toExternalForm()));
			track.addChild(new Element("albumArtist", mediainfo.getCurrentURIMetaData().getAlbumArtist()));
			track.addChild(new Element("duration", posinfo.getTrackDuration()+""));
			track.addChild(new Element("position", posinfo.getRelTime()+""));


		} else if (uri.startsWith("x-rincon-mp3radio:")) {
			// yep, it's the radio
			track.addChild(new Element("artist", mediainfo.getCurrentURIMetaData().getCreator()));
			track.addChild(new Element("album", mediainfo.getCurrentURIMetaData().getAlbum()));
			track.addChild(new Element("title", mediainfo.getCurrentURIMetaData().getTitle()));
			track.addChild(new Element("albumArt", thishost+"/images/internetradio.png"));
			track.addChild(new Element("albumArtist", mediainfo.getCurrentURI()));
			track.addChild(new Element("duration", posinfo.getTrackDuration()+""));
			track.addChild(new Element("position", posinfo.getRelTime()+""));
		} else if (uri.startsWith("x-rincon-stream:")) {
			// line in stream
			track.addChild(new Element("artist", "N/A"));
			track.addChild(new Element("album", "N/A"));
			track.addChild(new Element("title", "Local Line In"));
			track.addChild(new Element("albumArt", thishost+"/images/linein.png"));
			track.addChild(new Element("albumArtist", "N/A"));
		} else if (uri.startsWith("pndrradio:")) {
			// Pandora
			try {
				TrackMetaData trackmeta = posinfo.getTrackMetaData();
				track.addChild(new Element("artist", "Pandora: " + trackmeta.getCreator()));
				track.addChild(new Element("album", "Pandora: " + trackmeta.getAlbum()));
				track.addChild(new Element("title", "Pandora: " + trackmeta.getTitle()));
				track.addChild(new Element("albumArt", new URL(trackmeta.getAlbumArtUri()).toExternalForm()));
				track.addChild(new Element("albumArtist", "Pandora: " + trackmeta.getAlbumArtist()));
				track.addChild(new Element("duration", posinfo.getTrackDuration()+""));
				track.addChild(new Element("position", posinfo.getRelTime()+""));

			} catch (Exception e) {
				track.addChild(new Element("noMusic", "true"));
			}

		} else if (uri.startsWith("rdradio:station:")) {
			// Rhapsody Station
			try {
				TrackMetaData trackmeta = posinfo.getTrackMetaData();
				track.addChild(new Element("artist", "Rhapsody: " + trackmeta.getCreator()));
				track.addChild(new Element("album", "Rhapsody: " + trackmeta.getAlbum()));
				track.addChild(new Element("title", "Rhapsody: " + trackmeta.getTitle()));
				track.addChild(new Element("albumArt", new URL(trackmeta.getAlbumArtUri()).toExternalForm()));
				track.addChild(new Element("albumArtist", "Rhapsody: " + trackmeta.getAlbumArtist()));
				track.addChild(new Element("duration", posinfo.getTrackDuration()+""));
				track.addChild(new Element("position", posinfo.getRelTime()+""));

			} catch (Exception e) {
				track.addChild(new Element("noMusic", "true"));
			}
		} else if (uri.startsWith("lastfm:")) {
			// last.fm Station
			try {
				TrackMetaData trackmeta = posinfo.getTrackMetaData();
				track.addChild(new Element("artist", "last.fm: " + trackmeta.getCreator()));
				track.addChild(new Element("album", "last.fm: " + trackmeta.getAlbum()));
				track.addChild(new Element("title", "last.fm: " + trackmeta.getTitle()));
				track.addChild(new Element("albumArt", new URL(trackmeta.getAlbumArtUri()).toExternalForm()));
				track.addChild(new Element("albumArtist", "last.fm: " + trackmeta.getAlbumArtist()));
				track.addChild(new Element("duration", posinfo.getTrackDuration()+""));
				track.addChild(new Element("position", posinfo.getRelTime()+""));
			} catch (Exception e) {
				track.addChild(new Element("noMusic", "true"));
			}

		} else if (uri.startsWith("x-sonosapi-stream:")) {
			// Local Radio
			try {
				TrackMetaData trackmeta = posinfo.getTrackMetaData();
				track.addChild(new Element("artist", "Radio: " + trackmeta.getCreator()));
				track.addChild(new Element("album", "Radio: " + trackmeta.getAlbum()));
				track.addChild(new Element("title", "Radio: " + trackmeta.getTitle()));
				track.addChild(new Element("albumArt", trackmeta.getAlbumArtUrl(zp).toExternalForm()));
				track.addChild(new Element("albumArtist", "Radio: " + trackmeta.getAlbumArtist()));
				track.addChild(new Element("duration", posinfo.getTrackDuration()+""));
				track.addChild(new Element("position", posinfo.getRelTime()+""));

			} catch (Exception e) {
				track.addChild(new Element("noMusic", "true"));
			}

		} else {
			if (LOG.isWarnEnabled() && mediainfo != null ) {
				LOG.warn("Couldn't find type of " + uri);
				track.addChild(new Element("music", "unknown"));
			}
		}
		parent.addChild(track);
	}


	public void getZoneQueue(String zoneID, String startIndex, String numEntries, Element parent) throws IOException, UPNPResponseException, JanosWebException {
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
		parent.addChild(queue);
	}


	public void getZoneArtists(String zoneID, String startIndex, String numEntries, Element parent) throws IOException, UPNPResponseException, JanosWebException {
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
		parent.addChild(artists);
	}

	public void getZoneAlbums(String zoneID, String startIndex, String numEntries, String artist, Element parent) throws IOException, UPNPResponseException, JanosWebException {
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
			album.addChild(new Element("albumArt", e.getAlbumArtURL(zp).toExternalForm()));
			album.addChild(new Element("id", e.getId()));
			albums.addChild(album);

		}
		parent.addChild(albums);
	}

	public void getZoneTracks(String zoneID, String startIndex, String numEntries, String artist, String album, Element parent) throws IOException, UPNPResponseException, JanosWebException  {
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
		parent.addChild(tracks);
	}

	public void getZoneSearch(String zoneID, String startIndex, String numEntries, String searchData, Element parent) throws IOException, UPNPResponseException, JanosWebException {
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
			parent.addChild(new Element("results"));

		Element results = new Element("results");
		for (Entry e : entries.subList(readidx, Math.min(readidx+length, entries.size()-1))) {
			Element result = new Element("result", true);
			String type = e.getId();
			if (type.startsWith("A:ARTIST") || type.startsWith("A:ALBUMARTIST")) {
				result.addChild(new Element("type", "artist"));
				Element artist = new Element("artist");
				artist.addChild(new Element("name", e.getTitle()));
				artist.addChild(new Element("id", e.getId()));
				result.addChild(artist);
			} else if (type.startsWith("A:ALBUM") && !type.startsWith("A:ALBUMARTIST")) {
				result.addChild(new Element("type", "album"));
				Element album = new Element("album");
				album.addChild(new Element("title", e.getTitle()));
				album.addChild(new Element("artist", e.getCreator()));
				album.addChild(new Element("albumArt", e.getAlbumArtURL(zp).toExternalForm()));
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
		parent.addChild(results);
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

	/**Convenience method for finding the ZoneGroup that a given ZonePlayer belongs to
	 * @param zoneID, the ID of the ZonePlayer
	 * @return the ZoneGroup that the ZonePlayer is member of.
	 * */
	private ZoneGroup getZoneGroupFromMember(String zoneID) throws JanosWebException {
		ZonePlayer zp = getZonePlayer(zoneID);
		List<ZoneGroup> groups = zp.getZoneGroupTopologyService().getGroupState().getGroups();
		for (ZoneGroup zg : groups) {
			for (String member : zg.getMembers()) {
				if (member.equals(zoneID)) {
					return zg;
				}
			}
		}
		return null;
	}

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



}
