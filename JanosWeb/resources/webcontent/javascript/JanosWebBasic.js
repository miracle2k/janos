var xmlhttps = new Array;

function getXmlHttp(command) {
	//reuse xmlhttp-object if it exists - to save the browser's memory
	if (xmlhttps[command]) {
		return xmlhttps[command];
	}
	
	var xmlhttp;
	if (window.XMLHttpRequest)
	  {// code for IE7+, Firefox, Chrome, Opera, Safari
	  xmlhttp = new XMLHttpRequest();
	  }
	else
	  {// code for IE6, IE5
	  xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	  }
	  //store the xmlhttp-object for later reuse;
	xmlhttps[command] = xmlhttp;
	return xmlhttp;
}

//Generic method calling the janosWeb-servlet with a command, querystring and display function
function displayReply(command, querystring, displayfunction) {
    xmlhttp = getXmlHttp(command);
	    xmlhttp.onreadystatechange=function() {
  		if (xmlhttp.readyState==4 && xmlhttp.status==200) {
			jsondoc = xmlhttp.responseText;
			//eval is unsafe, but I trust myself ;)
			//feel free to rewrite this to adress any safety concerns you might have
			displayfunction(eval('(' + jsondoc + ')'));
    	}
  	}
  	if (querystring>"") {
  		querystring = "&" + querystring;
  	}
	xmlhttp.open("GET","/janosWeb?cmd="+command+querystring+"&nocache="+Math.random(),true);
	xmlhttp.send();
}

//Shortcut function for displaying the zone groups
//http://host:port/janosWeb?cmd=getZoneGroups
function getGroups(displayfunction) {
	displayReply("getZoneGroups", "", displayfunction);
}

function getPlayModes(displayfunction) {
	displayReply("getPlayModes", "", displayfunction);
}

function getPlayCommands(displayfunction) {
	displayReply("getPlayCommands", "", displayfunction);
}


/////////////////////////////////////////////////////////
//  _______  _  _ ___    ___ ___  ___  _   _ ___  ___  //
// |_  / _ \| \| | __|  / __| _ \/ _ \| | | | _ \/ __| //
//  / / (_) | .` | _|  | (_ |   / (_) | |_| |  _/\__ \ //
// /___\___/|_|\_|___|  \___|_|_\\___/ \___/|_|  |___/ //
//                                                     //
/////////////////////////////////////////////////////////
                                                   
//http://host:port/janosWeb?cmd=getZoneGroupVolume&groupID=RINCON_00000000000000000:00
function getGroupVolume(groupID, displayfunction) {
    displayReply("getZoneGroupVolume", "groupID="+groupID, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupVolume&groupID=RINCON_00000000000000000:00&groupVolume=30
function setGroupVolume(groupID, volume, displayfunction) {
    displayReply("setZoneGroupVolume", "groupID="+groupID+"&groupVolume="+volume, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupMuted&groupID=RINCON_00000000000000000:00&groupMuted=true
function setGroupMute(groupID, mute, displayfunction) {
    displayReply("setZoneGroupMuted", "groupID="+groupID+"&groupMuted="+mute, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupPlayInfo&groupID=RINCON_00000000000000000:00
function getGroupPlayInfo(groupID, displayfunction) {
	displayReply("getZoneGroupPlayInfo", "groupID="+groupID, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupCommand&groupID=RINCON_00000000000000000:00&groupCommand=Previous
//Valid commands (case sensitive): Play, Pause, Stop, Next, Previous 
function setGroupCommand(groupID, command, displayfunction) {
    displayReply("setZoneGroupCommand", "groupID="+groupID+"&groupCommand="+command, displayfunction);
}
function setGroupPlay(groupID, displayfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&groupCommand=Play", displayfunction);
}
function setGroupPause(groupID, displayfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&groupCommand=Pause", displayfunction);
}
function setGroupStop(groupID, displayfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&groupCommand=Stop", displayfunction);
}
function setGroupNext(groupID, displayfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&groupCommand=Next", displayfunction);
}
function setGroupPrevious(groupID, displayfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&groupCommand=Previous", displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupPlayMode&groupID=RINCON_00000000000000000:00&groupCommand=SHUFFLE
//Valid play modes (case sensitive): NORMAL, SHUFFLE, SHUFFLE_NOREPEAT, REPEAT_ONE, REPEAT_ALL, RANDOM, DIRECT_1, INTRO; 
function setGroupPlayMode(groupID, playmode, displayfunction) {
    displayReply("setZoneGroupPlayMode", "groupID="+groupID+"&groupPlayMode="+playmode, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupPlayInfo&groupID=RINCON_00000000000000000:00
function getGroupPlayInfo(groupID, displayfunction) {
	displayReply("getZoneGroupPlayInfo", "groupID="+groupID, displayfunction);
}
//http://host:port/janosWeb?cmd=getZoneGroupTrackPosition&groupID=RINCON_00000000000000000:00
function getGroupTrackPos(groupID, displayfunction) {
	displayReply("getZoneGroupTrackPosition", "groupID="+groupID, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupTrackPosition&groupID=RINCON_00000000000000000:00&groupTrackPosition=5000
function setGroupTrackPos(groupID, trackpos, displayfunction) {
	displayReply("setZoneGroupTrackPosition", "groupID="+groupID+"&groupTrackPosition="+trackpos, displayfunction);
}


//http://host:port/janosWeb?cmd=getZoneGroupTrack&groupID=RINCON_00000000000000000:00
function getGroupTrack(groupID, displayfunction) {
	displayReply("getZoneGroupTrack", "groupID="+groupID, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupQueue&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10
function getGroupQueue(groupID, startidx, numentries, displayfunction) {
	displayReply("getZoneGroupQueue", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupArtists&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10
function getGroupArtists(groupID, startidx, numentries, displayfunction) {
	displayReply("getZoneGroupArtists", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupAlbums&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10
function getGroupAlbums(groupID, startidx, numentries, displayfunction) {
	displayReply("getZoneGroupAlbums", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupTracks&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10
function getGroupTracks(groupID, startidx, numentries, displayfunction) {
	displayReply("getZoneGroupTracks", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupSearch&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10&searchData=music
function getGroupSearch(groupID, startidx, numentries, searchdata, displayfunction) {
	displayReply("getZoneGroupSearch", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries+"&searchData="+searchdata, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupEnqueue&groupID=RINCON_00000000000000000:00&itemID=A:ARTIST/Test
function setGroupEnqueue(groupID, itemid, displayfunction) {
	displayReply("setZoneGroupEnqueue", "groupID="+groupID+"&itemID="+itemid, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupEnqueueNow&groupID=RINCON_00000000000000000:00&itemID=A:ARTIST/Test
function setGroupEnqueueNow(groupID, itemid, displayfunction) {
	displayReply("setZoneGroupEnqueueNow", "groupID="+groupID+"&itemID="+itemid, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupEnqueueNext&groupID=RINCON_00000000000000000:00&itemID=A:ARTIST/Test
function setGroupEnqueueNext(groupID, itemid, displayfunction) {
	displayReply("setZoneGroupEnqueueNext", "groupID="+groupID+"&itemID="+itemid, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupClearQueue&groupID=RINCON_00000000000000000:00
function setGroupClearQueue(groupID, displayfunction) {
	displayReply("setZoneGroupClearQueue", "groupID="+groupID, displayfunction);
}
////////////////////////////
// _______  _  _ ___ ___  //
//|_  / _ \| \| | __/ __| //
// / / (_) | .` | _|\__ \ //
///___\___/|_|\_|___|___/ //
//                        //
////////////////////////////

//http://host:port/janosWeb?cmd=getZoneVolume&zoneID=RINCON_00000000000000000
function getZoneVolume(zoneID, displayfunction) {
    displayReply("getZoneVolume", "zoneID="+zoneID, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneVolume&zoneID=RINCON_00000000000000000&volume=30
function setZoneVolume(zoneID, volume, displayfunction) {
    displayReply("setZoneVolume", "zoneID="+zoneID+"&volume="+volume, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneMuted&zoneID=RINCON_00000000000000000&zoneMuted=true
function setZoneMute(zoneID, mute, displayfunction) {
    displayReply("setZoneMuted", "zoneID="+zoneID+"&zoneMuted="+mute, displayfunction);
}

//http://host:port/janosWeb?cmd=getZonePlayInfo&zoneID=RINCON_00000000000000000
function getZonePlayInfo(zoneID, displayfunction) {
	displayReply("getZonePlayInfo", "zoneID="+zoneID, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneCommand&zoneID=RINCON_00000000000000000&zoneCommand=Previous
//Valid commands (case sensitive): Play, Pause, Stop, Next, Previous 
function setZoneCommand(zoneID, command, displayfunction) {
    displayReply("setZoneCommand", "zoneID="+zoneID+"&zoneCommand="+command, displayfunction);
}
function setZonePlay(zoneID, displayfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&zoneCommand=Play", displayfunction);
}
function setZonePause(zoneID, displayfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&zoneCommand=Pause", displayfunction);
}
function setZoneStop(zoneID, displayfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&zoneCommand=Stop", displayfunction);
}
function setZoneNext(zoneID, displayfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&zoneCommand=Next", displayfunction);
}
function setZonePrevious(zoneID, displayfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&zoneCommand=Previous", displayfunction);
}

//http://host:port/janosWeb?cmd=setZonePlayMode&zoneID=RINCON_00000000000000000&zonePlayMode=SHUFFLE
//Valid play modes (case sensitive): NORMAL, SHUFFLE, SHUFFLE_NOREPEAT, REPEAT_ONE, REPEAT_ALL, RANDOM, DIRECT_1, INTRO; 
function setZonePlayMode(zoneID, playmode, displayfunction) {
    displayReply("setZonePlayMode", "zoneID="+zoneID+"&zonePlayMode="+playmode, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneTrackPosition&zoneID=RINCON_00000000000000000
function getZoneTrackPos(zoneID, displayfunction) {
	displayReply("getZoneTrackPosition", "zoneID="+zoneID, displayfunction);
}
//http://host:port/janosWeb?cmd=setZoneTrackPosition&zoneID=RINCON_00000000000000000&zoneTrackPosition=7000
function setZoneTrackPos(zoneID, trackpos, displayfunction) {
    displayReply("setZonePlayMode", "zoneID="+zoneID+"&zoneTrackPosition="+trackpos, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneTrack&zoneID=RINCON_00000000000000000
function getZoneTrack(zoneID, displayfunction) {
	displayReply("getZoneTrack", "zoneID="+zoneID, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneQueue&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10
function getZoneTrack(zoneID, startidx, numentries, displayfunction) {
	displayReply("getZoneQueue", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneArtists&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10
function getZoneArtists(zoneID, startidx, numentries, displayfunction) {
	displayReply("getZoneArtists", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneAlbums&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10
function getZoneAlbums(zoneID, startidx, numentries, displayfunction) {
	displayReply("getZoneAlbums", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneTracks&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10
function getZoneTracks(zoneID, startidx, numentries, displayfunction) {
	displayReply("getZoneTracks", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneSearch&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10&searchData=music
function getZoneSearch(zoneID, startidx, numentries, searchdata, displayfunction) {
	displayReply("getZoneSearch", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries+"&searchData="+searchdata, displayfunction);
}

//http://host:port/janosWeb?cmd=getZoneSearch&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10&searchData=music
function getZoneSearch(zoneID, startidx, numentries, searchdata, displayfunction) {
	displayReply("getZoneSearch", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries+"&searchData="+searchdata, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneEnqueue&zoneID=RINCON_00000000000000000&itemID=A:ARTIST/Test
function setZoneEnqueue(zoneID, itemid) {
	displayReply("setZoneEnqueue", "zoneID="+zoneID+"&itemID="+itemid, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneEnqueueNow&zoneID=RINCON_00000000000000000&itemID=A:ARTIST/Test
function setZoneEnqueueNow(zoneID, itemid) {
	displayReply("setZoneEnqueueNow", "zoneID="+zoneID+"&itemID="+itemid, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneEnqueueNext&zoneID=RINCON_00000000000000000&itemID=A:ARTIST/Test
function setZoneEnqueueNext(zoneID, itemid) {
	displayReply("setZoneEnqueueNext", "zoneID="+zoneID+"&itemID="+itemid, displayfunction);
}

//http://host:port/janosWeb?cmd=setZoneClearQueue&zoneID=RINCON_00000000000000000
function setZoneClearQueue(zoneID) {
	displayReply("setZoneClearQueue", "zoneID="+zoneID, displayfunction);
}