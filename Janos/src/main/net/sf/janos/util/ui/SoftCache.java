/*
   Copyright 2009 david

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
package net.sf.janos.util.ui;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

/**
 * A "Soft" cache - it maintains soft references to the values so that the JVM
 * can clean up the values when it requires memory.
 * 
 * @author David Wheeler
 * 
 */
public final class SoftCache<K,V> {

  private Map<K,SoftReference<V>> map = new HashMap<K, SoftReference<V>>();
  
  private ReferenceQueue<Entry<K, V>> queue = new ReferenceQueue<Entry<K, V>>();
  
  /**
   * @param key The key
   * @return the value corresponding to <code>key</code> or null
   */
  public final V get(K key) {
    expungeStaleEntries();
    SoftReference<V> ref = map.get(key);
    if (ref != null) {
      // we have to maintain a reference to val so it doesn't get removed after we test for null
      V val = ref.get();
      if (val != null) {
        return val;
      }
    }
    return null;
  }
  
  /**
   * Puts the given value in the cache, under the provided key. 
   * @param key
   * @param value
   */
  public final void put(K key, V value) {
    expungeStaleEntries();
    Entry<K, V> ref = new Entry<K, V>(key, value, queue);
    map.put(key, ref);
  }
  
  /**
   * Removes entries from the map that have had their value garbage-collected
   */
  private void expungeStaleEntries() {
    Entry<K, V> e;
    while ((e = (Entry<K, V>)queue.poll()) != null) {
      LogFactory.getLog(getClass()).debug("Cleaning up Softcache entry for " + e.getKey());
      map.remove(e.getKey());
    }
  }
  
  /**
   * A soft reference that also contains the key to identify it in the map
   * @author David Wheeler
   *
   */
  private static class Entry<K, V> extends SoftReference<V>{
    private K key;

    public Entry(K key, V value, ReferenceQueue<Entry<K, V>> queue) {
      super(value);
      this.key = key;
    }

    public K getKey() {
      return key;
    }
  }
}
