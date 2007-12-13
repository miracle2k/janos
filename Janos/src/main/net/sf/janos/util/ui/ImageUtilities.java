/*
 * Created on 24/11/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.util.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

public class ImageUtilities {
  
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

}
