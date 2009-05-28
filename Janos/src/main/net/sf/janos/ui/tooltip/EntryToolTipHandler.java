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
package net.sf.janos.ui.tooltip;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.janos.ApplicationContext;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.util.ui.ImageUtilities;
import net.sf.janos.util.ui.LabelHelper;
import net.sf.janos.util.ui.ImageUtilities.Callback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * A custom tooltip handler - modified from
 * http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/HowtoimplementhoverhelpfeedbackusingtheMouseTrackListener.htm
 * 
 * @author David Wheeler
 * 
 */
public class EntryToolTipHandler implements ToolTipHandler {

  protected static final ImageData EMPTY_IMAGE = ImageUtilities.loadImageDataFromSystemClasspath("empty.png").scaledTo(64, 64);

  private Shell tipShell;

  private Label tipLabelImage, tipLabelText;

  private Widget tipWidget; // widget this tooltip is hovering over

  private Point tipPosition; // the position being hovered over

  /**
   * Creates a new tooltip handler
   * 
   * @param parent
   *            the parent Shell
   */
  public EntryToolTipHandler(Shell parent) {
    final Display display = parent.getDisplay();

    tipShell = new Shell(parent, SWT.ON_TOP | SWT.TOOL);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.marginWidth = 2;
    gridLayout.marginHeight = 2;
    tipShell.setLayout(gridLayout);

    tipShell.setBackground(display
        .getSystemColor(SWT.COLOR_INFO_BACKGROUND));

    tipLabelImage = new Label(tipShell, SWT.NONE);
    tipLabelImage.setForeground(display
        .getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    tipLabelImage.setBackground(display
        .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    tipLabelImage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
        | GridData.VERTICAL_ALIGN_CENTER));

    tipLabelText = new Label(tipShell, SWT.NONE);
    tipLabelText.setForeground(display
        .getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    tipLabelText.setBackground(display
        .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    tipLabelText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
        | GridData.VERTICAL_ALIGN_CENTER));
  }

  /**
   * @see net.sf.janos.ui.tooltip.ToolTipHandler#activateHoverHelp(org.eclipse.swt.widgets.Control)
   */
  public void activateHoverHelp(final Control control) {
    /*
     * Get out of the way if we attempt to activate the control
     * underneath the tooltip
     */
    control.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDown(MouseEvent e) {
        if (tipShell.isVisible())
          tipShell.setVisible(false);
      }
    });

    /*
     * Trap move events to pop-up tooltip
     */
    MouseListener listener = new MouseListener(control);
    control.addMouseTrackListener(listener);
    control.addMouseMoveListener(listener);
  }

  /**
   * Sets the location for a hovering shell
   * 
   * @param shell
   *            the object that is to hover
   * @param position
   *            the position of a widget to hover over
   * @return the top-left location for a hovering box
   */
  private void setHoverLocation(Shell shell, Point position) {
    Rectangle displayBounds = shell.getDisplay().getBounds();
    Rectangle shellBounds = shell.getBounds();
    shellBounds.x = Math.max(Math.min(position.x, displayBounds.width
        - shellBounds.width), 0);
    shellBounds.y = Math.max(Math.min(position.y + 16,
        displayBounds.height - shellBounds.height), 0);
    shell.setBounds(shellBounds);
  }
  
  /**
   * Adds and removes the shell depending on mouse movements
   * @author David Wheeler
   *
   */
  private final class MouseListener extends MouseTrackAdapter implements MouseMoveListener {
    private final Control control;
    private Entry previousEntry;
    private ImageLoadCallback imageLoadCallback;
    
    private MouseListener(Control control) {
      this.control = control;
    }
    
    @Override
    public void mouseExit(MouseEvent e) {
      if (tipShell.isVisible()) {
        tipShell.setVisible(false);
        Image image = tipLabelImage.getImage();
        tipLabelImage.setImage(null);
        if (image != null && !image.isDisposed()) {
          image.dispose();
        }
      }
      tipWidget = null;
      previousEntry = null;
    }
    
    public void mouseMove(MouseEvent event) {
      Point pt = new Point(event.x, event.y);
      Widget widget = event.widget;
      if (widget instanceof ToolBar) {
        ToolBar w = (ToolBar) widget;
        widget = w.getItem(pt);
      }
      if (widget instanceof Table) {
        Table w = (Table) widget;
        widget = w.getItem(pt);
      }
      if (widget instanceof Tree) {
        Tree w = (Tree) widget;
        widget = w.getItem(pt);
      }
      if (widget == null) {
        tipShell.setVisible(false);
        tipWidget = null;
        return;
      }
      if (widget == tipWidget)
        return;
      tipWidget = widget;
      tipPosition = control.toDisplay(pt);
      Entry entry = (Entry) widget.getData("ENTRY");
      if (entry == previousEntry) {
        // nothing to do
      } else if (entry != null && displayToolTipFor(entry)) {
        previousEntry = entry;
        String text = getToolTipTextFrom(entry);
        Image image = tipLabelImage.getImage();
        tipLabelImage.setImage(new Image(tipLabelImage.getDisplay(), EMPTY_IMAGE));
        if (image != null && !image.isDisposed()) {
          image.dispose();
        }
        
        if (imageLoadCallback != null) {
          imageLoadCallback.cancel();
        }
        imageLoadCallback = new ImageLoadCallback(tipLabelImage, tipShell);
        ImageUtilities.loadImageAsync(getToolTipImageUrlFrom(entry), imageLoadCallback);
        tipLabelText.setText(text != null ? LabelHelper.escapeText(text) : "");
        tipShell.pack();
        setHoverLocation(tipShell, tipPosition);
        tipShell.setVisible(true);
      } else {
        mouseExit(event);
      }
    }
    
    /**
     * @param entry
     * @return true if a tooltip should be displayed for this entry
     */
    private boolean displayToolTipFor(Entry entry) {
      return getToolTipTextFrom(entry) != null;
    }
    
    /**
     * 
     * @param entry
     * @return text appropriate for a label in a tooltip for the given entry (or null)
     */
    private String getToolTipTextFrom(Entry entry) {
      if (entry.getUpnpClass().startsWith("object.item")) {
        return "Title: " + entry.getTitle() + "\nArtist: " + entry.getCreator() + "\nAlbum: " + entry.getAlbum();
      } else if (entry.getUpnpClass().equals("object.container.album.musicAlbum")){
        return "Artist: " + entry.getCreator() + "\nAlbum: " + entry.getTitle();
      } else {
        return null;
      }
    }
    
    /**
     * 
     * @param entry
     * @return a URL appropriate for the given entry (or null)
     */
    private URL getToolTipImageUrlFrom(Entry entry) {
      if (entry.getAlbumArtUri() == null || entry.getAlbumArtUri().length() == 0) {
        return null;
      }
      try {
        ApplicationContext appContext = ApplicationContext.getInstance();
        ZonePlayer zone = appContext.getController().getCoordinatorForZonePlayer(appContext.getShell().getZoneList().getSelectedZone());
        return zone.appendUrl(entry.getAlbumArtUri());
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return null;
    }
  }
  
  private static class ImageLoadCallback implements Callback {
    
    private Label label;
    private boolean isCancelled;
    private Control parent;

    public ImageLoadCallback(Label label, Control parent) {
      this.label = label;
      this.parent = parent;
    }
    
    public synchronized void imageLoaded(final ImageData data) {
      if (!isCancelled) {
        label.getDisplay().asyncExec(new Runnable() {
          public void run() {
            Image image = label.getImage();
            if (data == null) {
              label.setImage(new Image(label.getDisplay(), EMPTY_IMAGE));
            } else {
              label.setImage(new Image(label.getDisplay(), data.scaledTo(64, 64)));
            }
            if (image != null && !image.isDisposed()) {
              image.dispose();
            }
            parent.pack();
          }
        });
      }
    }
    
    public synchronized void cancel() {
      this.isCancelled = true;
    }
    
  }
}
