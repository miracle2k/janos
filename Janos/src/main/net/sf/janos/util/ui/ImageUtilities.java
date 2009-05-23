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
package net.sf.janos.util.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sf.janos.ApplicationContext;
import net.sf.janos.util.ui.softcache.NoSuchKeyException;
import net.sf.janos.util.ui.softcache.SoftCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

public class ImageUtilities {

  /**
   * A cache for image data retrieved from remote resources.
   * <p>
   * NOTE: Having URL as the key can require a DNS lookup each time a URL is
   * hashed. This may not be a problem for URLs with ips as hostname
   */
  private static final SoftCache<URL, ImageData> IMAGE_DATA_CACHE = new SoftCache<URL, ImageData>();
  
  /**
   * Scales the given image to the given size.
   * 
   * NOTE that the original image is <strong>NOT</strong> disposed by this
   * method.
   * 
   * @param image
   * @param width
   * @param height
   * @return a new Image of the given dimensions.
   */
  public static Image scaleImageTo(Image image, int width, int height) {
    Image outImage = new Image(image.getDevice(), width, height);
    GC gc = new GC(outImage);
    gc.setAntialias(SWT.ON);
    gc.drawImage(image, image.getBounds().x, image.getBounds().y, 
        image.getBounds().width, image.getBounds().height, 0, 0, width, height);
    gc.dispose();
    return outImage;
  }

  /**
   * Loads an image from the provided resource (treated as an absolute, not relative resource)
   * @param resource the image to load
   * @return the loaded image
   */
  public static ImageData loadImageDataFromSystemClasspath(String resource) {
    InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
    ImageData data;
    if (is == null) {
      throw new RuntimeException("Resource " + resource + " does not exist: could not load image");
    }
    data = new ImageData(is);
    try {
      is.close();
    } catch (IOException e) {}
    return data;
  }
  
  /**
   * Loads an image from the provided resource
   * @param resource the image to load
   * @return the loaded image, or null if an error occurred
   */
  public static ImageData loadImageData(URL resource) {
    if (resource == null) {
      return null;
    }
    ImageData data = null;
    synchronized (IMAGE_DATA_CACHE) {
      try {
        data = IMAGE_DATA_CACHE.get(resource);
        return data;
      } catch (NoSuchKeyException e) {
        // fall through
      }
    }
    InputStream is = null;
    try {
      is = resource.openStream();
      data = new ImageData(is);
      synchronized (IMAGE_DATA_CACHE) {
        IMAGE_DATA_CACHE.put(resource, data);
      }
    } catch (FileNotFoundException e) {
      Log log = LogFactory.getLog(ImageUtilities.class);
      log.debug("Image file " + resource + " does not exist");
      synchronized (IMAGE_DATA_CACHE) {
        IMAGE_DATA_CACHE.put(resource, null);
      }
    } catch (IOException e) {
      Log log = LogFactory.getLog(ImageUtilities.class);
      log.error("Couldn't load image from " + resource, e);
    } finally {
      try {
        is.close();
      } catch (Exception e) {}
    }
    return data;
  }
  
  /**
   * Loads the given image in a background thread, providing
   * <code>callback</code> with the results when completed. NOTE: callback may
   * be notified in a background thread.
   */
  public static void loadImageAsync(final URL resource, final Callback callback) {
    if (resource == null) {
      callback.imageLoaded(null);
    }
    
    // look up cache to see if we actually need to load the image
    synchronized (IMAGE_DATA_CACHE) {
      ImageData data;
      try {
        data = IMAGE_DATA_CACHE.get(resource);
        callback.imageLoaded(data);
        return;
      } catch (NoSuchKeyException e) {
        // fall through
      }
    }
    
    // No shortcuts. just load the image
    ApplicationContext.getInstance().getController().getWorkerExecutor().execute(new Runnable() {
      public void run() {
        final ImageData imageData = loadImageData(resource);
        callback.imageLoaded(imageData);
      }
    });
  }
  
  /**
   * A callback to be notified when the image is loaded
   * 
   * @author David Wheeler
   * 
   */
  public interface Callback {
    /**
     * Called when the image has been loaded. Note that this method may be called from any thread
     * @param data the ImageData requested, or null if it could not be loaded
     */
    void imageLoaded(ImageData data);
  }
}
