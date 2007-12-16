/*
 * Created on 11/11/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model.xml;

import java.util.ArrayList;
import java.util.List;

import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZoneGroup;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ZoneGroupStateHandler extends DefaultHandler {

  private final List<ZoneGroup> groups = new ArrayList<ZoneGroup>();
  private final List<ZonePlayer> currentGroupPlayers = new ArrayList<ZonePlayer>();
  private String coordinator;
  private String groupId;
  private SonosController controller;
  
  ZoneGroupStateHandler(SonosController controller) {
    this.controller = controller;
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equals("ZoneGroup")) {
      groupId = attributes.getValue("ID");
      coordinator = attributes.getValue("Coordinator");
    } else if (qName.equals("ZoneGroupMember")) {
      currentGroupPlayers.add(controller.getZonePlayerModel().getById(attributes.getValue("UUID")));
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals("ZoneGroup")) {
      groups.add(new ZoneGroup(groupId, controller.getZonePlayerModel().getById(coordinator), currentGroupPlayers));
      currentGroupPlayers.clear();
    }
  }
  
  public List<ZoneGroup> getGroups() {
    return groups;
  }
}
