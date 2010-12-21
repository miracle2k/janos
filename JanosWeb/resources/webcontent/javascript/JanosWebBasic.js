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

function AjaxClosure(command, querystring, callbackfunction, sync) {
	var xmlhttp = init();
	if (!sync) {
		xmlhttp.onreadystatechange = processRequest;
	}
	
	function processRequest() {
  		if (xmlhttp.readyState==4 && xmlhttp.status==200) {
			jsondoc = xmlhttp.responseText;
			//eval is unsafe, but I trust myself ;)
			//feel free to rewrite this to adress any safety concerns you might have
			if (callbackfunction)
				callbackfunction(eval('(' + jsondoc + ')'));
    	}
  	}

  	function init() {
  	 	if (querystring>"") {
  			querystring = "&" + querystring;
	  	}
  		return getXmlHttp(command);
  	}
  	
  	this.doGet = function() {
  		if (!sync) {
  			xmlhttp.open("GET","/janosWeb?cmd="+command+querystring+"&nocache="+Math.random(),true);
			xmlhttp.send();
		}
		else {
			//synchronous call
			xmlhttp.open("GET","/janosWeb?cmd="+command+querystring+"&nocache="+Math.random(),false);
			xmlhttp.send();
			jsondoc = xmlhttp.responseText;
			callbackfunction(eval('(' + jsondoc + ')'));
		}
  	}
}

//Generic method calling the janosWeb-servlet with a command, querystring and display function
//NOTICE how it is exploited in the SET-functions below that if someone calls displayReply with 
//only 3 parameters, the value of the fourth parameter "sync" defaults to undefined which 
//yields the same results as "false" in an if-statement!
function displayReply(command, querystring, callbackfunction, sync) {
	var ac = new AjaxClosure(command, querystring, callbackfunction, sync);
	ac.doGet();
}


//Shortcut function for displaying the zone groups
//http://host:port/janosWeb?cmd=getZoneGroups
function getGroups(callbackfunction, sync) {
	displayReply("getZoneGroups", "", callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=listenForZoneGroupUpdates
function listenForGroupUpdates(callbackfunction, sync) {
	displayReply("listenForZoneGroupUpdates", "", callbackfunction, sync);
}

function getPlayModes(callbackfunction, sync) {
	displayReply("getPlayModes", "", callbackfunction, sync);
}

function getPlayCommands(callbackfunction, sync) {
	displayReply("getPlayCommands", "", callbackfunction, sync);
}


/////////////////////////////////////////////////////////
//  _______  _  _ ___    ___ ___  ___  _   _ ___  ___  //
// |_  / _ \| \| | __|  / __| _ \/ _ \| | | | _ \/ __| //
//  / / (_) | .` | _|  | (_ |   / (_) | |_| |  _/\__ \ //
// /___\___/|_|\_|___|  \___|_|_\\___/ \___/|_|  |___/ //
//                                                     //
/////////////////////////////////////////////////////////
//http://host:port/janosWeb?cmd=listenForZoneGroupVolumeUpdates&groupID=RINCON_00000000000000000:00
function listenForGroupVolumeUpdates(groupID, callbackfunction, sync) {
	displayReply("listenForZoneGroupVolumeUpdates", "groupID="+groupID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=listenForZoneGroupMusicUpdates&groupID=RINCON_00000000000000000:00
function listenForGroupMusicUpdates(groupID, callbackfunction, sync) {
	displayReply("listenForZoneGroupMusicUpdates", "groupID="+groupID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=listenForAllZoneGroupUpdates&groupID=RINCON_00000000000000000:00
function listenForAllZoneGroupUpdates(groupID, callbackfunction, sync) {
	displayReply("listenForAllZoneGroupUpdates", "groupID="+groupID, callbackfunction, sync);
}


//http://host:port/janosWeb?cmd=getZoneGroupVolume&groupID=RINCON_00000000000000000:00
function getGroupVolume(groupID, callbackfunction, sync) {
    displayReply("getZoneGroupVolume", "groupID="+groupID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=setZoneGroupVolume&groupID=RINCON_00000000000000000:00&groupVolume=30
function setGroupVolume(groupID, volume, callbackfunction) {
    displayReply("setZoneGroupVolume", "groupID="+groupID+"&volume="+volume, callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupMuted&groupID=RINCON_00000000000000000:00&mute=true
function setGroupMute(groupID, mute, callbackfunction) {
    displayReply("setZoneGroupMuted", "groupID="+groupID+"&mute="+mute, callbackfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupPlayInfo&groupID=RINCON_00000000000000000:00
function getGroupPlayInfo(groupID, callbackfunction, sync) {
	displayReply("getZoneGroupPlayInfo", "groupID="+groupID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=setZoneGroupCommand&groupID=RINCON_00000000000000000:00&command=Previous
//Valid commands (case sensitive): Play, Pause, Stop, Next, Previous 
function setGroupCommand(groupID, command, callbackfunction) {
    displayReply("setZoneGroupCommand", "groupID="+groupID+"&command="+command, callbackfunction);
}
function setGroupPlay(groupID, callbackfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&command=Play", callbackfunction);
}
function setGroupPause(groupID, callbackfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&command=Pause", callbackfunction);
}
function setGroupStop(groupID, callbackfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&command=Stop", callbackfunction);
}
function setGroupNext(groupID, callbackfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&command=Next", callbackfunction);
}
function setGroupPrevious(groupID, callbackfunction) {
	displayReply("setZoneGroupCommand", "groupID="+groupID+"&command=Previous", callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupPlayMode&groupID=RINCON_00000000000000000:00&playMode=SHUFFLE
//Valid play modes (case sensitive): NORMAL, SHUFFLE, SHUFFLE_NOREPEAT, REPEAT_ONE, REPEAT_ALL, RANDOM, DIRECT_1, INTRO; 
function setGroupPlayMode(groupID, playmode, callbackfunction) {
    displayReply("setZoneGroupPlayMode", "groupID="+groupID+"&playMode="+playmode, callbackfunction);
}

//http://host:port/janosWeb?cmd=getZoneGroupPlayInfo&groupID=RINCON_00000000000000000:00
function getGroupPlayInfo(groupID, callbackfunction, sync) {
	displayReply("getZoneGroupPlayInfo", "groupID="+groupID, callbackfunction, sync);
}
//http://host:port/janosWeb?cmd=getZoneGroupCurrentTrackPosition&groupID=RINCON_00000000000000000:00
function getGroupCurrentTrackPos(groupID, callbackfunction, sync) {
	displayReply("getZoneGroupCurrentTrackPosition", "groupID="+groupID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=setZoneGroupCurrentTrackPosition&groupID=RINCON_00000000000000000:00&trackPosition=5000
function setGroupCurrentTrackPos(groupID, trackpos, callbackfunction) {
	displayReply("setZoneGroupCurrentTrackPosition", "groupID="+groupID+"&trackPosition="+trackpos, callbackfunction);
}


//http://host:port/janosWeb?cmd=getZoneGroupCurrentTrack&groupID=RINCON_00000000000000000:00
function getGroupCurrentTrack(groupID, callbackfunction, sync) {
	displayReply("getZoneGroupCurrentTrack", "groupID="+groupID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneGroupAllInfo&groupID=RINCON_00000000000000000:00
function getGroupAllInfo(groupID, callbackfunction, sync) {
	displayReply("getZoneGroupAllInfo", "groupID="+groupID, callbackfunction, sync);
}


//http://host:port/janosWeb?cmd=getZoneGroupQueue&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10
function getGroupQueue(groupID, startidx, numentries, callbackfunction, sync) {
	displayReply("getZoneGroupQueue", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneGroupArtists&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10
function getGroupArtists(groupID, startidx, numentries, callbackfunction, sync) {
	displayReply("getZoneGroupArtists", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneGroupAlbums&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10
function getGroupAlbums(groupID, startidx, numentries, callbackfunction, sync) {
	displayReply("getZoneGroupAlbums", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneGroupTracks&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10
function getGroupTracks(groupID, startidx, numentries, callbackfunction, sync) {
	displayReply("getZoneGroupTracks", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneGroupSearch&groupID=RINCON_00000000000000000:00&startIndex=3&numEntries=10&searchData=music
function getGroupSearch(groupID, startidx, numentries, searchdata, callbackfunction, sync) {
	displayReply("getZoneGroupSearch", "groupID="+groupID+"&startIndex="+startidx+"&numEntries="+numentries+"&searchData="+searchdata, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=setZoneGroupEnqueue&groupID=RINCON_00000000000000000:00&itemID=A:ARTIST/Test
function setGroupEnqueue(groupID, itemid, callbackfunction) {
	itemid = itemid.replace(/%/gi, "%25");
	displayReply("setZoneGroupEnqueue", "groupID="+groupID+"&itemID="+itemid, callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupEnqueueNow&groupID=RINCON_00000000000000000:00&itemID=A:ARTIST/Test
function setGroupEnqueueNow(groupID, itemid, callbackfunction) {
	itemid = itemid.replace(/%/gi, "%25");
	displayReply("setZoneGroupEnqueueNow", "groupID="+groupID+"&itemID="+itemid, callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupEnqueueNext&groupID=RINCON_00000000000000000:00&itemID=A:ARTIST/Test
function setGroupEnqueueNext(groupID, itemid, callbackfunction) {
	itemid = itemid.replace(/%/gi, "%25");
	displayReply("setZoneGroupEnqueueNext", "groupID="+groupID+"&itemID="+itemid, callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneGroupClearQueue&groupID=RINCON_00000000000000000:00
function setGroupClearQueue(groupID, callbackfunction) {
	displayReply("setZoneGroupClearQueue", "groupID="+groupID, callbackfunction);
}
////////////////////////////
// _______  _  _ ___ ___  //
//|_  / _ \| \| | __/ __| //
// / / (_) | .` | _|\__ \ //
///___\___/|_|\_|___|___/ //
//                        //
////////////////////////////

//http://host:port/janosWeb?cmd=listenForZoneVolumeUpdates&groupID=RINCON_00000000000000000
function listenForZoneVolumeUpdates(zoneID, callbackfunction, sync) {
	displayReply("listenForZoneVolumeUpdates", "zoneID="+zoneID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=listenForZoneMusicUpdates&groupID=RINCON_00000000000000000
function listenForZoneMusicUpdates(zoneID, callbackfunction, sync) {
	displayReply("listenForZoneMusicUpdates", "zoneID="+zoneID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=listenForAllZoneUpdates&groupID=RINCON_00000000000000000
function listenForAllZoneUpdates(zoneID, callbackfunction, sync) {
	displayReply("listenForAllZoneUpdates", "zoneID="+zoneID, callbackfunction, sync);
}


//http://host:port/janosWeb?cmd=getZoneVolume&zoneID=RINCON_00000000000000000
function getZoneVolume(zoneID, callbackfunction, sync) {
    displayReply("getZoneVolume", "zoneID="+zoneID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=setZoneVolume&zoneID=RINCON_00000000000000000&volume=30
function setZoneVolume(zoneID, volume, callbackfunction) {
    displayReply("setZoneVolume", "zoneID="+zoneID+"&volume="+volume, callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneMuted&zoneID=RINCON_00000000000000000&mute=true
function setZoneMute(zoneID, mute, callbackfunction) {
    displayReply("setZoneMuted", "zoneID="+zoneID+"&mute="+mute, callbackfunction);
}

//http://host:port/janosWeb?cmd=getZonePlayInfo&zoneID=RINCON_00000000000000000
function getZonePlayInfo(zoneID, callbackfunction, sync) {
	displayReply("getZonePlayInfo", "zoneID="+zoneID, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=setZoneCommand&zoneID=RINCON_00000000000000000&command=Previous
//Valid commands (case sensitive): Play, Pause, Stop, Next, Previous 
function setZoneCommand(zoneID, command, callbackfunction) {
    displayReply("setZoneCommand", "zoneID="+zoneID+"&command="+command, callbackfunction);
}
function setZonePlay(zoneID, callbackfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&command=Play", callbackfunction);
}
function setZonePause(zoneID, callbackfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&command=Pause", callbackfunction);
}
function setZoneStop(zoneID, callbackfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&command=Stop", callbackfunction);
}
function setZoneNext(zoneID, callbackfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&command=Next", callbackfunction);
}
function setZonePrevious(zoneID, callbackfunction) {
	displayReply("setZoneCommand", "zoneID="+zoneID+"&command=Previous", callbackfunction);
}

//http://host:port/janosWeb?cmd=setZonePlayMode&zoneID=RINCON_00000000000000000&playMode=SHUFFLE
//Valid play modes (case sensitive): NORMAL, SHUFFLE, SHUFFLE_NOREPEAT, REPEAT_ALL; 
function setZonePlayMode(zoneID, playmode, callbackfunction) {
    displayReply("setZonePlayMode", "zoneID="+zoneID+"&playMode="+playmode, callbackfunction);
}

//http://host:port/janosWeb?cmd=getZoneCurrentTrackPosition&zoneID=RINCON_00000000000000000
function getZoneCurrentTrackPos(zoneID, callbackfunction, sync) {
	displayReply("getZoneCurrentTrackPosition", "zoneID="+zoneID, callbackfunction);
}
//http://host:port/janosWeb?cmd=setZoneCurrentTrackPosition&zoneID=RINCON_00000000000000000&trackPosition=7000
function setZoneCurrentTrackPos(zoneID, trackpos, callbackfunction) {
    displayReply("setZoneCurrentTrackPosition", "zoneID="+zoneID+"&trackPosition="+trackpos, callbackfunction);
}

//http://host:port/janosWeb?cmd=getZoneCurrentTrack&zoneID=RINCON_00000000000000000
function getZoneCurrentTrack(zoneID, callbackfunction, sync) {
	displayReply("getZoneCurrentTrack", "zoneID="+zoneID, callbackfunction);
}

//http://host:port/janosWeb?cmd=getZoneAllInfo&zoneID=RINCON_00000000000000000
function getZoneAllInfo(zoneID, callbackfunction, sync) {
	displayReply("getZoneAllInfo", "zoneID="+zoneID, callbackfunction);
}


//http://host:port/janosWeb?cmd=getZoneQueue&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10
function getZoneQueue(zoneID, startidx, numentries, callbackfunction, sync) {
	displayReply("getZoneQueue", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneArtists&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10
function getZoneArtists(zoneID, startidx, numentries, callbackfunction, sync) {
	displayReply("getZoneArtists", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneAlbums&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10
function getZoneAlbums(zoneID, startidx, numentries, callbackfunction, sync) {
	displayReply("getZoneAlbums", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneTracks&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10
function getZoneTracks(zoneID, startidx, numentries, callbackfunction, sync) {
	displayReply("getZoneTracks", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneSearch&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10&searchData=music
function getZoneSearch(zoneID, startidx, numentries, searchdata, callbackfunction, sync) {
	displayReply("getZoneSearch", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries+"&searchData="+searchdata, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=getZoneSearch&zoneID=RINCON_00000000000000000&startIndex=3&numEntries=10&searchData=music
function getZoneSearch(zoneID, startidx, numentries, searchdata, callbackfunction, sync) {
	displayReply("getZoneSearch", "zoneID="+zoneID+"&startIndex="+startidx+"&numEntries="+numentries+"&searchData="+searchdata, callbackfunction, sync);
}

//http://host:port/janosWeb?cmd=setZoneEnqueue&zoneID=RINCON_00000000000000000&itemID=A:ARTIST/Test
function setZoneEnqueue(zoneID, itemid) {
	displayReply("setZoneEnqueue", "zoneID="+zoneID+"&itemID="+itemid, callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneEnqueueNow&zoneID=RINCON_00000000000000000&itemID=A:ARTIST/Test
function setZoneEnqueueNow(zoneID, itemid) {
	displayReply("setZoneEnqueueNow", "zoneID="+zoneID+"&itemID="+itemid, callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneEnqueueNext&zoneID=RINCON_00000000000000000&itemID=A:ARTIST/Test
function setZoneEnqueueNext(zoneID, itemid) {
	displayReply("setZoneEnqueueNext", "zoneID="+zoneID+"&itemID="+itemid, callbackfunction);
}

//http://host:port/janosWeb?cmd=setZoneClearQueue&zoneID=RINCON_00000000000000000
function setZoneClearQueue(zoneID) {
	displayReply("setZoneClearQueue", "zoneID="+zoneID, callbackfunction);
}