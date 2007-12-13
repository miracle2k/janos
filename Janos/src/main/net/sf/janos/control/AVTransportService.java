/*
 * Created on 21/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

import java.io.IOException;

import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;
import net.sf.janos.Debug;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MediaInfo;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.model.TransportInfo;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * For controlling the audio transport service of a zone player.
 * 
 * NOTE: some methods in this class are incomplete stubs.
 * 
 * @author David Wheeler
 *
 */
public class AVTransportService extends AbstractService {
  
  private static final String SET_AV_TRANSPORT_URI_ACTION = "SetAVTransportURI";
  private static final String PLAY_ACTION = "Play";
    
  // TODO I know there's a better way to do this...
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


  protected AVTransportService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_AV_TRANSPORT);
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
    Debug.debug("SetAvTransportURI(0,"+entry.getRes()+"," + metadata + ")");
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
   */
  public MediaInfo getMediaInfo() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetMediaInfo");
    message.setInputParameter("InstanceID", 0);
    ActionResponse resp = message.service();
    return new MediaInfo(resp.getOutActionArgumentValue("NrTracks"), 
        resp.getOutActionArgumentValue("MediaDuration"),
        resp.getOutActionArgumentValue("CurrentURI"), 
        resp.getOutActionArgumentValue("CurrentURIMetaData"),
        resp.getOutActionArgumentValue("NextURI"),
        resp.getOutActionArgumentValue("NextURIMetaData"),
        resp.getOutActionArgumentValue("PlayMedium"),
        resp.getOutActionArgumentValue("RecordMedium"),
        resp.getOutActionArgumentValue("WriteStatus"));
  }
  
  /**
   * @return Information about the audio transport.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public TransportInfo getTransportInfo() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("GetTransportInfo");
    message.setInputParameter("InstanceID", 0);
    ActionResponse resp = message.service();
    return new TransportInfo(resp.getOutActionArgumentValue("CurrentTransportState"), 
        resp.getOutActionArgumentValue("CurrentTransportStatus"), 
        resp.getOutActionArgumentValue("CurrentSpeed"));
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
    return new PositionInfo(resp.getOutActionArgumentValue("Track"), 
        resp.getOutActionArgumentValue("TrackDuration"), 
        resp.getOutActionArgumentValue("TrackMetaData"), 
        resp.getOutActionArgumentValue("TrackURI"),
        resp.getOutActionArgumentValue("RelTime"),
        resp.getOutActionArgumentValue("AbsTime"),
        resp.getOutActionArgumentValue("RelCount"),
        resp.getOutActionArgumentValue("AbsCount"));
  }
  
  /**
   * NOT IMPLEMENTED
   *
   */
  public void getDeviceCapabilities() {
    /* TODO 
     *         <action>
            <name>GetDeviceCapabilities</name>
            <argumentList>
                <argument>
                    <name>InstanceID</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>
                </argument>
                <argument>
                    <name>PlayMedia</name>
                    <direction>out</direction>                    <relatedStateVariable>PossiblePlaybackStorageMedia</relatedStateVariable>
                </argument>
                <argument>
                    <name>RecMedia</name>
                    <direction>out</direction>                    <relatedStateVariable>PossibleRecordStorageMedia</relatedStateVariable>
                </argument>
                <argument>
                    <name>RecQualityModes</name>
                    <direction>out</direction>                    <relatedStateVariable>PossibleRecordQualityModes</relatedStateVariable>
                </argument>
            </argumentList>
        </action>
     */
  }
  
  /**
   * NOT IMPLEMENTED
   *
   */
  public void getTransportSettings() {
    /* TODO
     *         <action>
            <name>GetTransportSettings</name>
            <argumentList>
                <argument>
                    <name>InstanceID</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>
                </argument>
                <argument>
                    <name>PlayMode</name>
                    <direction>out</direction>                    <relatedStateVariable>CurrentPlayMode</relatedStateVariable>
                </argument>
                <argument>
                    <name>RecQualityMode</name>
                    <direction>out</direction>                 <relatedStateVariable>CurrentRecordQualityMode</relatedStateVariable>
                </argument>
            </argumentList>
        </action>
     */
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
   * TODO this is returning error 701 (invalid name?)
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
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void seek() throws IOException, UPNPResponseException {
//    ActionMessage message = messageFactory.getMessage("Seek");
//    message.setInputParameter("InstanceID", 0);
//    message.service();
    /* TODO this one's a bit tricky?
     *         <action>
            <name>Seek</name>
            <argumentList>
                <argument>
                    <name>InstanceID</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>
                </argument>
                <argument>
                    <name>Unit</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_SeekMode</relatedStateVariable>
                </argument>
                <argument>
                    <name>Target</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_SeekTarget</relatedStateVariable>
                </argument>
            </argumentList>
        </action>

     */
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
   * NOT IMPLEMENTED
   *
   */
  public void setPlayMode(/*playmode*/)  {
    /* TODO
     *         <action>
            <name>SetPlayMode</name>
            <argumentList>
                <argument>
                    <name>InstanceID</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>
                </argument>
                <argument>
                    <name>NewPlayMode</name>
                    <direction>in</direction>                    <relatedStateVariable>CurrentPlayMode</relatedStateVariable>
                </argument>
            </argumentList>
        </action>
     */
  }
  
  /**
   * NOT IMPLEMENTED
   */
  public void getCurrentTransportActions() {
    /* TODO
     *             <name>GetCurrentTransportActions</name>
            <argumentList>
                <argument>
                    <name>InstanceID</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>
                </argument>
                <argument>
                    <name>Actions</name>
                    <direction>out</direction>                    <relatedStateVariable>CurrentTransportActions</relatedStateVariable>
                </argument>
            </argumentList>
        </action>
     */
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
   * Sets the new sleep timer duration
   * @param newSleepTimerDuration
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void configureSleepTimer(int newSleepTimerDuration) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage("ConfigureSleepTimer");
    message.setInputParameter("InstanceID", 0); 
    // TODO what is ISO8601Time?
    message.setInputParameter("NewSleepTimerDuration", newSleepTimerDuration); 
    message.service();
  }
  
  /**
   * NOT IMPLEMENTED
   */
  public int getRemainingSleepTimerDuration() {
    /*
    ActionMessage message = messageFactory.getMessage("ConfigureSleepTimer");
    message.setInputParameter("InstanceID", 0); 
    // TODO what is ISO8601Time?
    message.service();
    */
    return -1;
  }
  
  /**
   * NOT IMPLEMENTED
   *
   */
  public void runAlarm() {
    /* TODO
     *         <action>
            <name>RunAlarm</name>
            <argumentList>
                <argument>
                    <name>InstanceID</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>
                </argument>
                <argument>
                    <name>AlarmID</name>
                    <direction>in</direction>
                    <relatedStateVariable>AlarmIDRunning</relatedStateVariable>
                </argument>
                <argument>
                    <name>LoggedStartTime</name>
                    <direction>in</direction>
                    <relatedStateVariable>AlarmLoggedStartTime</relatedStateVariable>
                </argument>
                <argument>
                    <name>Duration</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_ISO8601Time</relatedStateVariable>
                </argument>
                <argument>
                    <name>ProgramURI</name>
                    <direction>in</direction>
                    <relatedStateVariable>AVTransportURI</relatedStateVariable>
                </argument>
                <argument>
                    <name>ProgramMetaData</name>
                    <direction>in</direction>
                    <relatedStateVariable>AVTransportURIMetaData</relatedStateVariable>
                </argument>
                <argument>
                    <name>PlayMode</name>
                    <direction>in</direction>
                    <relatedStateVariable>CurrentPlayMode</relatedStateVariable>
                </argument>
                <argument>
                    <name>Volume</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_AlarmVolume</relatedStateVariable>
                </argument>
                <argument>
                    <name>IncludeLinkedZones</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_AlarmIncludeLinkedZones</relatedStateVariable>
                </argument>
            </argumentList>
        </action>
     */
  }
  
  /**
   * NOT IMPLEMENTED
   *
   */
  public void getRunningAlarmProperties () {
    /* TODO
     *         <action>
            <name>GetRunningAlarmProperties</name>
            <argumentList>
                <argument>
                    <name>InstanceID</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>
                </argument>
                <argument>
                    <name>AlarmID</name>
                    <direction>out</direction>
                    <relatedStateVariable>AlarmIDRunning</relatedStateVariable>
                </argument>
                <argument>
                    <name>GroupID</name>
                    <direction>out</direction>
                    <relatedStateVariable>A_ARG_TYPE_GroupID</relatedStateVariable>
                </argument>
                <argument>
                    <name>LoggedStartTime</name>
                    <direction>out</direction>
                    <relatedStateVariable>AlarmLoggedStartTime</relatedStateVariable>
                </argument>
            </argumentList>
        </action>
     */
  }

  /**
   * NOT IMPLEMENTED
   *
   */
  public void snoozeAlarm() {
    /* TODO
     *         <action>
            <name>SnoozeAlarm</name>
            <argumentList>
                <argument>
                    <name>InstanceID</name>
                    <direction>in</direction>                    <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>
                </argument>
                <argument>
                    <name>Duration</name>
                    <direction>in</direction>
                    <relatedStateVariable>A_ARG_TYPE_ISO8601Time</relatedStateVariable>
                </argument>            
            </argumentList>
        </action>

     */
  }

}
