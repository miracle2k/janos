/*
   Copyright 2007 David Wheeler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.sf.janos.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;
import net.sf.janos.model.AlarmProperties;
import net.sf.janos.model.DeviceCapabilities;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MediaInfo;
import net.sf.janos.model.PlayMode;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.model.SeekTarget;
import net.sf.janos.model.SeekTargetFactory;
import net.sf.janos.model.TrackMetaData;
import net.sf.janos.model.TransportAction;
import net.sf.janos.model.TransportInfo;
import net.sf.janos.model.TransportSettings;
import net.sf.janos.model.xml.ResultParser;
import net.sf.janos.model.xml.AVTransportEventHandler.AVTransportEventType;
import net.sf.janos.util.TimeUtilities;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * For controlling the audio transport service of a zone player.
 * 
 * NOTE: some methods in this class are incomplete stubs.
 * 
 * @author David Wheeler
 *
 */
public class AVTransportService extends AbstractService implements ServiceEventHandler {
  
  private static final Log LOG = LogFactory.getLog(AVTransportService.class);
  
  private static final String SET_AV_TRANSPORT_URI_ACTION = "SetAVTransportURI";
  private static final String PLAY_ACTION = "Play";
    
  // TODO here's a lazy hack
  private static final String METADATA1 = 
      "<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
      "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
      "xmlns:r=\"urn:schemas-rinconnetworks-com:metadata-1-0/\" " +
      "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">" +
      "<item id=\"";
  private static final String METADATA2 = 
      "\" parentID=\"";
      
  private static final String METADATA3 = 
    "\" restricted=\"true\">" +
      "<dc:title>";
  private static final String METADATA4 = "</dc:title>" +
      "<upnp:class>object.item.audioItem.audioBroadcast</upnp:class>" +
      "<desc id=\"cdudn\" nameSpace=\"urn:schemas-rinconnetworks-com:metadata-1-0/\">" +
      "RINCON_AssociatedZPUDN</desc>" +
      "</item></DIDL-Lite>";

  private final Map<AVTransportEventType, String> state = new HashMap<AVTransportEventType, String>();
  private final List<AVTransportListener> listeners = new ArrayList<AVTransportListener>();
  
  protected AVTransportService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_AV_TRANSPORT);
    registerServiceEventing(this);
  }
  
  /**
   * Sets the item currently playing. Does not modify the queue.
   * 
   * @param entry
   *          an Entry whose res and metadata (id, parentId, title) are used to
   *          message the zoneplayer.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void setAvTransportUri(Entry entry) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage(SET_AV_TRANSPORT_URI_ACTION);
    message.setInputParameter("InstanceID", 0); // no instance id required
    message.setInputParameter("CurrentURI", entry.getRes());
    String metadata = compileMetadataString(entry);
    message.setInputParameter("CurrentURIMetaData", metadata); 
    LOG.debug("SetAvTransportURI(0,"+entry.getRes()+"," + metadata + ")");
    message.service();
    // ignore result.
  }
  
  private static String compileMetadataString(Entry entry) {
    StringBuilder str = new StringBuilder(METADATA1);
    str.append(entry.getId()).append(METADATA2);
    str.append(entry.getParentId()).append(METADATA3);
    str.append(entry.getTitle()).append(METADATA4);
    return StringEscapeUtils.escapeXml(str.toString());
  }

  /**
   * Adds the given entry to the end of the queue
   * @param entry
   * @return the position of the entry in the queue
   * @throws IOException
   * @throws UPNPResponseException
   */
  public int addToQueue(Entry entry) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("AddURIToQueue");
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("EnqueuedURI", entry.getRes());
    message.setInputParameter("EnqueuedURIMetaData", compileMetadataString(entry));
    message.setInputParameter("DesiredFirstTrackNumberEnqueued", -1);
    message.setInputParameter("EnqueueAsNext", true);
    ActionResponse resp = message.service();
    return Integer.parseInt(resp.getOutActionArgumentValue("FirstTrackNumberEnqueued"));
  }
  
  /**
   * Moves a selection of tracks in the queue.
   * 
   * @param startAt
   *          the index of the first track
   * @param num
   *          the number of tracks to move
   * @param insertBefore
   *          the position to place the tracks
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void reorderTracksInQueue(int startAt, int num, int insertBefore)
      throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("AddURIToQueue");
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("StartingIndex", startAt);
    message.setInputParameter("NumberOfTracks", num);
    message.setInputParameter("InsertBefore", insertBefore);
    message.service();
  }
  
  /**
   * Removes the given entry from the queue
   * @param entry
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void removeTrackFromQueue(Entry entry) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("AddURIToQueue");
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("ObjectID", entry.getId());
    message.service();
  }
  
  /**
   * Removes all entries from the queue
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void clearQueue() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("AddURIToQueue");
    message.setInputParameter("InstanceID", 0);
    message.service();
  }
  
  /**
   * Saves the given queue to the given title
   * @param title
   * @param queue
   * @return the new ObjectID to refer to the saved queue
   * @throws IOException
   * @throws UPNPResponseException
   */
  public String saveQueue(String title, Entry queue) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("AddURIToQueue");
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("Title", title);
    message.setInputParameter("ObjectID", queue.getId());
    ActionResponse resp = message.service();
    return resp.getOutActionArgumentValue("AssignedObjectID");
  }

  /**
   * @return Information about the currently playing media.
   * @throws IOException
   * @throws UPNPResponseException
   * @throws SAXException 
   */
  public MediaInfo getMediaInfo() throws IOException, UPNPResponseException, SAXException {
    if (!(state.containsKey(AVTransportEventType.NumberOfTracks) 
        && state.containsKey(AVTransportEventType.CurrentTrackDuration) 
        && state.containsKey(AVTransportEventType.AVTransportURI)
        && state.containsKey(AVTransportEventType.AVTransportURIMetaData)
        && state.containsKey(AVTransportEventType.NextAVTransportURI)
        && state.containsKey(AVTransportEventType.NextAVTransportURIMetaData)
        && state.containsKey(AVTransportEventType.PlaybackStorageMedium)
        && state.containsKey(AVTransportEventType.RecordStorageMedium)
        && state.containsKey(AVTransportEventType.RecordMediumWriteStatus))) {
      ActionMessage message = messageFactory.getMessage("GetMediaInfo");
      message.setInputParameter("InstanceID", 0);
      ActionResponse resp = message.service();
      state.put(AVTransportEventType.NumberOfTracks, resp.getOutActionArgumentValue("NrTracks"));
      state.put(AVTransportEventType.CurrentTrackDuration, resp.getOutActionArgumentValue("MediaDuration"));
      state.put(AVTransportEventType.AVTransportURI, resp.getOutActionArgumentValue("CurrentURI"));
      state.put(AVTransportEventType.AVTransportURIMetaData, resp.getOutActionArgumentValue("CurrentURIMetaData"));
      state.put(AVTransportEventType.NextAVTransportURI, resp.getOutActionArgumentValue("NextURI"));
      state.put(AVTransportEventType.NextAVTransportURIMetaData, resp.getOutActionArgumentValue("NextURIMetaData"));
      state.put(AVTransportEventType.PlaybackStorageMedium, resp.getOutActionArgumentValue("PlayMedium"));
      state.put(AVTransportEventType.RecordStorageMedium, resp.getOutActionArgumentValue("RecordMedium"));
      state.put(AVTransportEventType.RecordMediumWriteStatus, resp.getOutActionArgumentValue("WriteStatus"));
    }
    String metaDataString = state.get(AVTransportEventType.AVTransportURIMetaData);
    TrackMetaData trackMetaData = metaDataString.isEmpty() ? null : ResultParser.parseTrackMetaData(metaDataString);
    return new MediaInfo(state.get(AVTransportEventType.NumberOfTracks), 
        TimeUtilities.convertDurationToLong(state.get(AVTransportEventType.CurrentTrackDuration)),
        state.get(AVTransportEventType.AVTransportURI), 
        trackMetaData,
        state.get(AVTransportEventType.NextAVTransportURI),
        state.get(AVTransportEventType.NextAVTransportURIMetaData),
        state.get(AVTransportEventType.PlaybackStorageMedium),
        state.get(AVTransportEventType.RecordStorageMedium),
        state.get(AVTransportEventType.RecordMediumWriteStatus));
  }
  
  /**
   * @return Information about the audio transport.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public TransportInfo getTransportInfo() throws IOException, UPNPResponseException {
    if (!(state.containsKey(AVTransportEventType.TransportState) 
        && state.containsKey(AVTransportEventType.TransportStatus) 
        && state.containsKey(AVTransportEventType.TransportPlaySpeed))) {
      ActionMessage message = messageFactory.getMessage("GetTransportInfo");
      message.setInputParameter("InstanceID", 0);
      ActionResponse resp = message.service();
      state.put(AVTransportEventType.TransportState, resp.getOutActionArgumentValue("CurrentTransportState"));
      state.put(AVTransportEventType.TransportStatus, resp.getOutActionArgumentValue("CurrentTransportStatus"));
      state.put(AVTransportEventType.TransportPlaySpeed, resp.getOutActionArgumentValue("CurrentSpeed"));
    }
    return new TransportInfo(state.get(AVTransportEventType.TransportState), 
        state.get(AVTransportEventType.TransportStatus), 
        state.get(AVTransportEventType.TransportPlaySpeed));
  }
  
  /**
   * @return information about the progress of the current media.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public PositionInfo getPositionInfo() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetPositionInfo");
    message.setInputParameter("InstanceID", 0);
    ActionResponse resp = message.service();
    state.put(AVTransportEventType.CurrentTrack, resp.getOutActionArgumentValue("Track"));
    state.put(AVTransportEventType.CurrentTrackDuration, resp.getOutActionArgumentValue("TrackDuration"));
    state.put(AVTransportEventType.CurrentTrackMetaData, resp.getOutActionArgumentValue("TrackMetaData"));
    state.put(AVTransportEventType.CurrentTrackURI, resp.getOutActionArgumentValue("TrackURI"));
    return new PositionInfo(Integer.parseInt(state.get(AVTransportEventType.CurrentTrack)), 
        TimeUtilities.convertDurationToLong(state.get(AVTransportEventType.CurrentTrackDuration)), 
        state.get(AVTransportEventType.CurrentTrackMetaData), 
        state.get(AVTransportEventType.CurrentTrackURI),
        TimeUtilities.convertDurationToLong(resp.getOutActionArgumentValue("RelTime")),
        TimeUtilities.convertDurationToLong(resp.getOutActionArgumentValue("AbsTime")),
        Integer.parseInt(resp.getOutActionArgumentValue("RelCount")),
        Integer.parseInt(resp.getOutActionArgumentValue("AbsCount")));
  }
  
  /**
   * @return a DeviceCapabilities object expressing the capabilities of this AVTransportService.
   * @throws UPNPResponseException 
   * @throws IOException 
   *
   */
  public DeviceCapabilities getDeviceCapabilities() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetDeviceCapabilities");
    message.setInputParameter("InstanceID", 0);
    ActionResponse response = message.service();
    return new DeviceCapabilities(response.getOutActionArgumentValue("PlayMedia"), 
        response.getOutActionArgumentValue("RecMedia"), 
        response.getOutActionArgumentValue("RecQualityModes"));
  }
  
  /**
   * @return the current transport settings for the service.
   * @throws UPNPResponseException 
   * @throws IOException 
   *
   */
  public TransportSettings getTransportSettings() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetTransportSettings");
    message.setInputParameter("InstanceID", 0);
    ActionResponse response = message.service();
    return new TransportSettings(response.getOutActionArgumentValue("PlayMode"), 
        response.getOutActionArgumentValue("RecQualityMode"));
  }
  
  /**
   * Starts the playback
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void play() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage(PLAY_ACTION);
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("Speed", 1);
    message.service();
  }
  
  /**
   * Stops playback
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void stop() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("Stop");
    message.setInputParameter("InstanceID", 0);
    message.service();
  }
  
  /**
   * Pauses playback
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void pause() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("Pause");
    message.setInputParameter("InstanceID", 0);
    message.service();
  }
  
  /**
   * NOT IMPLEMENTED
   * 
   * @throws IOException
   * @throws UPNPResponseException
   *           will contain a detail error code as one of the following
   *           <ul>
   *           <li>402 Invalid Args: Could be any of the following: not enough
   *           in args, too many in args, no in arg by that name, one or more in
   *           args are of the wrong data type. </li>
   *           <li>701 Transition not available: The immediate transition from
   *           current transport state to desired transport state is not
   *           supported by this device. </li>
   *           <li>705 Transport is locked: The transport is “hold locked”.
   *           <li>710 Seek mode not supported: The specified seek mode is not
   *           supported by the device. </li>
   *           <li>711 Illegal seek target: The specified seek target is not
   *           specified in terms of the seek mode, or is not present on the
   *           media. </li>
   *           <li>718 Invalid InstanceID: The specified instanceID is invalid
   *           for this AVTransport.</li>
   *           </ul>
   * @see SeekTargetFactory
   */
  public void seek(SeekTarget target) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("Seek");
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("Unit", target.getSeekMode());
    message.setInputParameter("Target", target.getTarget());
    message.service();
  }
  
  /**
   * Move to the next track
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void next() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("Next");
    message.setInputParameter("InstanceID", 0);
    message.service();
  }

  /**
   * Move to the previous track
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void previous() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("Previous");
    message.setInputParameter("InstanceID", 0);
    message.service();
  }
  
  /**
   * Not too sure...
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void nextSection() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("NextSection");
    message.setInputParameter("InstanceID", 0);
    message.service();
  }

  /**
   * not too sure...
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void previousSection() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("PreviousSection");
    message.setInputParameter("InstanceID", 0);
    message.service();
  }
  
  /**
   * Applies the new play mode. 
   * @throws UPNPResponseException 
   * @throws IOException 
   *
   */
  public void setPlayMode(PlayMode mode) throws IOException, UPNPResponseException  {
    ActionMessage message = messageFactory.getMessage("SetPlayMode");
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("NewPlayMode", mode.toString());
    message.service();
  }
  
  /**
   * @return A Collection of TransportActions representing the available
   *         actions.
   * @throws UPNPResponseException
   * @throws IOException
   */
  public Collection<TransportAction> getCurrentTransportActions() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetCurrentTransportActions");
    message.setInputParameter("InstanceID", 0);
    ActionResponse response = message.service();
    String actionList = response.getOutActionArgumentValue("Actions");
    ArrayList<TransportAction> actions = new ArrayList<TransportAction>();
    for (TransportAction action : TransportAction.values()) {
      if (actionList.contains(action.toString())) {
        actions.add(action);
      }
    }
    return actions;
  }

  /**
   * Indicates that this node should become the coordinator of its own
   * standalone group
   * 
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void becomeCoordinatorOfStandaloneGroup() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("BecomeCoordinatorOfStandaloneGroup");
    message.setInputParameter("InstanceID", 0); 
    message.service();
  }

  /**
   * Tells this node to become group coordinator
   * @param currentCoordinator
   * @param currentGroupId
   * @param otherMemebers
   * @param transportSettings
   * @param currentURI
   * @param currentURIMetadata
   * @param sleepTimerState
   * @param alarmState
   * @param streamRestartState
   * @param currentQueueTrackList
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void becomeGroupCoordinator(String currentCoordinator, 
      String currentGroupId, 
      String otherMemebers,
      String transportSettings,
      String currentURI, 
      String currentURIMetadata,
      String sleepTimerState, 
      String alarmState, 
      String streamRestartState, 
      String currentQueueTrackList) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("BecomeGroupCoordinator");
    message.setInputParameter("InstanceID", 0); 
    message.setInputParameter("CurrentCoordinator", currentCoordinator); 
    message.setInputParameter("CurrentGroupID", currentGroupId); 
    message.setInputParameter("OtherMembers", otherMemebers);
    message.setInputParameter("TransportSettings", transportSettings);
    message.setInputParameter("CurrentURI", currentURI);
    message.setInputParameter("CurrentURIMetaData", currentURIMetadata);
    message.setInputParameter("SleepTimerState", sleepTimerState);
    message.setInputParameter("AlarmState", alarmState);
    message.setInputParameter("StreamRestartState", streamRestartState);
    message.setInputParameter("CurrentQueueTrackList", currentQueueTrackList);
    message.service();
  }

  /**
   * Indicates that a group should become the coordinator and source of audio.
   * @param currentCoordinator
   * @param currentGroupId
   * @param otherMemebers
   * @param currentURI
   * @param currentURIMetadata
   * @param sleepTimerState
   * @param alarmState
   * @param streamRestartState
   * @param currentAVTTrackList
   * @param currentQueueTrackList
   * @param currentSourceState
   * @param resumePlayback
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void becomeGroupCoordinatorAndSource(String currentCoordinator, 
      String currentGroupId, 
      String otherMemebers,
      String currentURI, 
      String currentURIMetadata,
      String sleepTimerState, 
      String alarmState, 
      String streamRestartState, 
      String currentAVTTrackList, 
      String currentQueueTrackList, 
      String currentSourceState, 
      String resumePlayback) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("BecomeGroupCoordinatorAndSource");
    message.setInputParameter("InstanceID", 0); 
    message.setInputParameter("CurrentCoordinator", currentCoordinator); 
    message.setInputParameter("CurrentGroupID", currentGroupId); 
    message.setInputParameter("OtherMembers", otherMemebers);
    message.setInputParameter("CurrentURI", currentURI);
    message.setInputParameter("CurrentURIMetaData", currentURIMetadata);
    message.setInputParameter("SleepTimerState", sleepTimerState);
    message.setInputParameter("AlarmState", alarmState);
    message.setInputParameter("StreamRestartState", streamRestartState);
    message.setInputParameter("CurrentAVTTrackList", currentAVTTrackList);
    message.setInputParameter("CurrentQueueTrackList", currentQueueTrackList);
    message.setInputParameter("CurrentSourceState", currentSourceState);
    message.setInputParameter("ResumePlayback", resumePlayback);
    message.service();
  }
  
  /**
   * Changes group coordinator from current coordinator ot newCoordinator
   * 
   * @param currentCoordinator
   * @param newCoordinator
   * @param newTransportSettings
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void changeCoordinator(String currentCoordinator, 
      String newCoordinator, 
      String newTransportSettings) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("ChangeCoordinator");
    message.setInputParameter("InstanceID", 0); 
    message.setInputParameter("CurrentCoordinator", currentCoordinator); 
    message.setInputParameter("NewCoordinator", newCoordinator); 
    message.setInputParameter("NewTransportSettings", newTransportSettings);
    message.service();
  }
  
  /**
   * Changes transport settings to the settings provided
   * @param newTransportSettings
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void changeTransportSettings(String newTransportSettings) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("ChangeTransportSettings");
    message.setInputParameter("InstanceID", 0); 
    message.setInputParameter("NewTransportSettings", newTransportSettings); 
    message.service();
  }
  
  /**
   * Sets the new sleep timer duration.
   * @param newSleepTimerDuration
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void configureSleepTimer(int newSleepTimerDuration) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("ConfigureSleepTimer");
    message.setInputParameter("InstanceID", 0); 
    // ISO8601 Time - this uses joda time, but could use java.util.time.
    message.setInputParameter("NewSleepTimerDuration", 
        TimeUtilities.convertLongToDuration(newSleepTimerDuration)); 
    message.service();
  }
  
  /**
   * Returns the number of milliseconds until the sleep timer goes off.
   * 
   * @throws UPNPResponseException
   * @throws IOException
   */
  public long getRemainingSleepTimerDuration() throws IOException, UPNPResponseException {
    // TODO verify these strings.
    ActionMessage message = messageFactory.getMessage("GetRemainingSleepTimerDuration");
    message.setInputParameter("InstanceID", 0); 
    ActionResponse response = message.service();
    // ISO8601 Time - difficult to implement with java.util.calendar
    return TimeUtilities.convertDurationToLong(
        response.getOutActionArgumentValue("RemainingSleepTimerDuration"));
  }
  
  /**
   * TODO what does this method do? 
   * @throws UPNPResponseException 
   * @throws IOException 
   *
   */
  public void runAlarm(int alarmId, long loggedStartTime, long duration, 
      String programUri, String programMetadata, PlayMode playMode, 
      int volume, boolean includeLinkedZones) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("RunAlarm");
    message.setInputParameter("InstanceID", 0); 
    message.setInputParameter("AlarmID", alarmId); 
    message.setInputParameter("LoggedStartTime", TimeUtilities.convertLongToISO8601Date(loggedStartTime)); // TODO is this ISO9601 time?
    message.setInputParameter("Duration", duration); 
    message.setInputParameter("ProgramURI", programUri); 
    message.setInputParameter("ProgramMetaData", programMetadata); 
    message.setInputParameter("PlayMode", playMode); 
    message.setInputParameter("Volume", volume); 
    message.setInputParameter("IncludeLinkedZones", includeLinkedZones); 
    message.service();
  }
  
  /**
   * @return the properties of the alarm service.
   * @throws UPNPResponseException
   * @throws IOException
   * 
   */
  public AlarmProperties getRunningAlarmProperties () throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("RunAlarm");
    message.setInputParameter("InstanceID", 0); 
    ActionResponse response = message.service();
    return new AlarmProperties(Integer.parseInt(response.getOutActionArgumentValue("AlarmID")),
        Integer.parseInt(response.getOutActionArgumentValue("GroupID")),
        TimeUtilities.convertISO8601DateToLong(response.getOutActionArgumentValue("LoggedStartTime")));
  }

  /**
   * Delays alarm detonation by the given duration.
   * @throws UPNPResponseException 
   * @throws IOException 
   *
   */
  public void snoozeAlarm(long duration) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("SnoozeAlarm");
    message.setInputParameter("InstanceID", 0); 
    message.setInputParameter("Duration", TimeUtilities.convertLongToISO8601Duration(duration)); 
    message.service();
  }
  
  public void handleStateVariableEvent(String varName, String newValue) {
    LOG.info("recieved AVTransport notification: " + varName + "=" + newValue);
    try {
      Map<AVTransportEventType, String> changes = ResultParser.parseAVTransportEvent(newValue);
      state.putAll(changes);
      // TODO only fire real changes - currently the changes variable contains everything. 
      fireStateChanged(changes.keySet());
    } catch (SAXException e) {
      LOG.error("Could not parse change event: ", e);
    }
  }

  private void fireStateChanged(Set<AVTransportEventType> name) {
    synchronized (listeners) {
      for (AVTransportListener l : listeners) {
        l.valuesChanged(name, this);
      }
    }
  }

  /**
   * registers the given listener to be notified of changes to the state of the AVTransportService. 
   * @param l
   */
  public void addAvTransportListener(AVTransportListener l) {
    synchronized (listeners) {
      listeners.add(l);
    }
  }
  
  /**
   * unregisters the given listener: no further notifications shall be recieved.
   * @param l
   */
  public void removeAvTransportListener(AVTransportListener l) {
    synchronized (listeners) {
      listeners.remove(l);
    }
  }

}
