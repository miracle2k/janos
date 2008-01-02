/*
   Copyright 2008 davidwheeler

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
package net.sf.janos.model.xml;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RenderingControlEventHandler extends DefaultHandler {
  
  public static enum EventType {
    VOLUME_MASTER, 
    VOLUME_RF, 
    VOLUME_LF,
    MUTE_MASTER,
    MUTE_LF,
    MUTE_RF, 
    BASS,
    TREBLE,
    LOUDNESS,
    OUTPUT_FIXED,
    PRESET_NAME;
  }

  private final Map<RenderingControlEventHandler.EventType, String> changes = new HashMap<RenderingControlEventHandler.EventType, String>();

  private boolean getPresetName=false;
  private String presetName;
  
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if ("Volume".equals(qName)) {
      if ("Master".equals(atts.getValue("channel"))) {
        changes.put(EventType.VOLUME_MASTER, atts.getValue("val"));
      } else if ("LF".equals(atts.getValue("channel"))) {
        changes.put(EventType.VOLUME_LF, atts.getValue("val"));
      } else if ("RF".equals(atts.getValue("channel"))) {
        changes.put(EventType.VOLUME_RF, atts.getValue("val"));
      } // ignore other channels
    } else if ("Mute".equals(qName)) {
      if ("Master".equals(atts.getValue("channel"))) {
        changes.put(EventType.MUTE_MASTER, atts.getValue("val"));
      } else if ("LF".equals(atts.getValue("channel"))) {
        changes.put(EventType.MUTE_LF, atts.getValue("val"));
      } else if ("RF".equals(atts.getValue("channel"))) {
        changes.put(EventType.MUTE_RF, atts.getValue("val"));
      } // ignore other channels
    } else if ("Bass".equals(qName)) {
      changes.put(EventType.BASS, atts.getValue("val"));
    } else if ("Treble".equals(qName)) {
      changes.put(EventType.TREBLE, atts.getValue("val"));
    } else if ("Loudness".equals(qName)) {
      if ("Master".equals(atts.getValue("channel"))) {
        changes.put(EventType.LOUDNESS, atts.getValue("val"));
      } // ignore other channels
    } else if ("OutputFixed".equals(qName)) {
      changes.put(EventType.OUTPUT_FIXED, atts.getValue("val"));
    } else if ("PresetNameList".equals(qName)) {
      getPresetName=true;
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    if (getPresetName) {
      presetName = new String(ch, start, length);
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (getPresetName) {
      getPresetName = false;
    }
  }

  public Map<EventType, String> getChanges() {
    return changes;
  }

  
}
