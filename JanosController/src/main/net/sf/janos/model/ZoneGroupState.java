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
package net.sf.janos.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An immutable data transfer object containing each of the known zone groups.
 * 
 * @author David Wheeler
 * 
 */
public class ZoneGroupState {

  private final List<ZoneGroup> zoneGroups;
  public ZoneGroupState(Collection<ZoneGroup> groups) {
    this.zoneGroups = new ArrayList<ZoneGroup>(groups);
  }
  
  public List<ZoneGroup> getGroups() {
    return zoneGroups;
  }
  
}
