/*
 * Copyright 2007 David Wheeler
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
 * UPNP specified modes of play. TODO check this against Sonos modes
 * possibilities.
 * 
 * @author David Wheeler
 * 
 */
public enum PlayMode {
  NORMAL, 
  SHUFFLE, 
  REPEAT_ONE, 
  REPEAT_ALL, 
  RANDOM, 
  DIRECT_1, 
  INTRO;
}
