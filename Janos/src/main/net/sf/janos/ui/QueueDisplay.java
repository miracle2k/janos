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
package net.sf.janos.ui;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.control.AvTransportListener;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MediaInfo;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.ui.zonelist.ZoneListSelectionListener;
import net.sf.janos.util.ui.ImageUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * A UI component for displaying the now-playing and queue.
 * @author David Wheeler
 *
 */
public class QueueDisplay extends Composite implements ZoneListSelectionListener, AvTransportListener {
  private static final Log LOG = LogFactory.getLog(QueueDisplay.class);
  public static final int QUEUE_LENGTH = 20;
  private final Color LABEL_COLOR;
  
  private SonosController controller;
  private final Label trackArtist;
  private final Label trackAlbum;
  private final Label trackName;
  private final Label artwork;
  
  private final org.eclipse.swt.widgets.List queue;

  public QueueDisplay(Composite parent, int style, SonosController controller) {
    super(parent, style);
    this.controller = controller;
    LABEL_COLOR = new Color(parent.getDisplay(), new RGB(0, 0, 128));
    
    setLayout(new GridLayout(1, false));
    Group nowPlaying = new Group(this, SWT.NONE);
    nowPlaying.setText("Now Playing");
    nowPlaying.setForeground(LABEL_COLOR);
    nowPlaying.setLayout(new GridLayout(2, false));
    Label trackArtistLabel = new Label(nowPlaying, SWT.RIGHT);
    trackArtistLabel.setText("Artist: ");
    trackArtistLabel.setForeground(LABEL_COLOR);
    trackArtist = new Label(nowPlaying, SWT.LEFT);
    Label trackAlbumLabel = new Label(nowPlaying, SWT.RIGHT);
    trackAlbumLabel.setText("Album: ");
    trackAlbumLabel.setForeground(LABEL_COLOR);
    trackAlbum = new Label(nowPlaying, SWT.LEFT);
    Label trackNameLabel = new Label(nowPlaying, SWT.RIGHT);
    trackNameLabel.setText("Name: ");
    trackNameLabel.setForeground(LABEL_COLOR);
    trackName = new Label(nowPlaying, SWT.LEFT);
    
    artwork = new Label(nowPlaying, 0);
    GridData gd = new GridData();
    gd.horizontalSpan = 2;
    gd.widthHint = 128;
    gd.heightHint = 128;
    artwork.setLayoutData(gd);
    
    queue = new org.eclipse.swt.widgets.List(this, SWT.SINGLE);
    GridData queueData = new GridData(GridData.FILL, GridData.FILL, true, true);
    queue.setLayoutData(queueData);
  }

//  public void refreshNowPlaying() {
//    controller.getExecutor().execute(new NowPlayingFetcher(controller.getCurrentZonePlayerController()));
//  }
  
  protected class NowPlayingFetcher implements Runnable {
    
    private ZonePlayer zone;
    public NowPlayingFetcher(ZonePlayer zone) {
      this.zone = zone;
    }
    public void run() {
      if (zone == null) {
        return;
      }
      try {
        MediaInfo mediaInfo = zone.getMediaRendererDevice().getAvTransportService().getMediaInfo();
        PositionInfo posInfo = zone.getMediaRendererDevice().getAvTransportService().getPositionInfo();
        String uri = mediaInfo.getCurrentURI();
        if (uri == null) {
          
        } else if (uri.startsWith("x-rincon-queue:")) {
          ZonePlayer currentZone = null;
          // a queue is playing
          for (ZonePlayer zp : controller.getZonePlayerModel().getAllZones()) {
            if (uri.substring(15).startsWith(zp.getRootDevice().getUDN().substring(5))) {
              // this is the zoneplayer
              currentZone = zp;
              break;
            }
          }
          if (currentZone == null) {
            // unknown queue
            LOG.error("unknown queue: " + uri);
            return;
          }
          
          setQueueEntry(posInfo, currentZone);          
        } else if (uri.startsWith("x-rincon:")){
          // We're streaming from another sonos
          String id = uri.substring(9);
          ZonePlayer zp = controller.getZonePlayerModel().getById(id);
          setQueueEntry(posInfo, zp);
        } // TODO else...
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (UPNPResponseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    private void setQueueEntry(PositionInfo posInfo, final ZonePlayer zone) {
      final List<Entry> queueEntries = zone.getMediaServerDevice().getContentDirectoryService().getQueue(posInfo.getTrackNum() -1,QUEUE_LENGTH + 1);
      if (queueEntries.size() > 0) {
        final Entry currentEntry = queueEntries.remove(0);
        getDisplay().asyncExec(new Runnable() {
          public void run() {
            trackArtist.setText(currentEntry.getCreator());
            trackAlbum.setText(currentEntry.getAlbum());
            trackName.setText(currentEntry.getTitle());
            
            queue.removeAll();
            for (Entry entry : queueEntries) {
              queue.add(entry.getTitle());
            }

            if (artwork.getImage() != null) {
              artwork.getImage().dispose();
            }
            artwork.setImage(null);

            // asynchronously load image
            controller.getExecutor().execute(new Runnable() {
              public void run() {
                ImageData imageData;
                Image artworkImage = null;
                InputStream artworkStream = null;
                try {
                  URL artworkUrl = currentEntry.getAlbumArtURL(zone);
                  artworkStream = new BufferedInputStream(artworkUrl.openStream());
                  imageData = new ImageLoader().load(artworkStream)[0];
                  Image tmpImage = new Image(getDisplay(), imageData);
                  artworkImage = ImageUtilities.scaleImageTo(tmpImage, 128, 128);
                  tmpImage.dispose();
                } catch (FileNotFoundException e) {
                  // no artwork. TODO add default image
                } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                } finally {
                  if (artworkStream != null) {
                    try {
                      artworkStream.close();
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }
                }

                final Image image = artworkImage;
                getDisplay().asyncExec(new Runnable() {
                  public void run() {
                    Image oldImage = artwork.getImage();
                    artwork.setImage(image);
                    if ( oldImage != null) {
                      oldImage.dispose();
                    }
                  }
                });
              }
            });

            layout();
          }
        });
      }
    }
  }

  public void zoneSelectionChangedTo(ZonePlayer newSelection) {
    showNowPlayingForZone(newSelection);
  }
  
  /**
   * Reloads the now-playing and queue to be that from the given zone.
   * @param zone
   */
  public void showNowPlayingForZone(ZonePlayer zone) {
    zone.getMediaRendererDevice().getAvTransportService().addAvTransportListener(this);
    controller.getExecutor().execute(new NowPlayingFetcher(zone));
  }
  
}
