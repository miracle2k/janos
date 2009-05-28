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
package net.sf.janos.model.xml;

import java.util.ArrayList;
import java.util.List;

import net.sf.janos.model.ZoneGroup;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ZoneGroupStateHandler extends DefaultHandler {

  private final List<ZoneGroup> groups = new ArrayList<ZoneGroup>();
  private final List<String> currentGroupPlayers = new ArrayList<String>();
  private String coordinator;
  private String groupId;
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equals("ZoneGroup")) {
      groupId = attributes.getValue("ID");
      coordinator = attributes.getValue("Coordinator");
    } else if (qName.equals("ZoneGroupMember")) {
      currentGroupPlayers.add(attributes.getValue("UUID"));
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals("ZoneGroup")) {
      groups.add(new ZoneGroup(groupId, coordinator, currentGroupPlayers));
      currentGroupPlayers.clear();
    }
  }
  
  public List<ZoneGroup> getGroups() {
    return groups;
  }
}
