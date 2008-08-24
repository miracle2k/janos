/*
 * Copyright 2008 David Wheeler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.janos.model;

/**
 * The possible seek modes. NOTE that only TRACK_NR is required to be supported
 * on all devices. 
 * 
 * @author David Wheeler
 * 
 */
public enum SeekMode {
  TRACK_NR("TRACK_NR"), 
  ABS_TIME("ABS_TIME"), 
  REL_TIME("REL_TIME"), 
  ABS_COUNT("ABS_COUNT"), 
  REL_COUNT("REL_COUNT"), 
  CHANNEL_FREQ("CHANNEL_FREQ"), 
  TAPE_INDEX("TAPE-INDEX"), 
  FRAME("FRAME");
  
  private final String modeString;

  private SeekMode(String modeString) {
    this.modeString = modeString;
  }
  
  public String getModeString() {
    return modeString;
  }
}
