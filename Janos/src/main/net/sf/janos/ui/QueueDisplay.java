/*
 * Created on 06/11/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import net.sbbi.upnp.devices.UPNPDevice;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.Debug;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MediaInfo;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.util.ui.ImageUtilities;

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
public class QueueDisplay extends Composite {
  private final Color LABEL_COLOR;
  
  private SonosController controller;
  private final Label trackArtist;
  private final Label trackAlbum;
  private final Label trackName;
  private final Label artwork;

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
    trackAlbumLabel.setText("Artist: ");
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
    
    getNowPlaying();
    
  }

  private void getNowPlaying() {
    controller.getExecutor().execute(new NowPlayingFetcher(controller.getCurrentZonePlayer()));
  }
  
  protected class NowPlayingFetcher implements Runnable {
    
    private ZonePlayer zone;
    public NowPlayingFetcher(ZonePlayer zone) {
      this.zone = zone;
    }
    public void run() {
      try {
        MediaInfo mediaInfo = zone.getMediaRendererDevice().getAvTransportService().getMediaInfo();
        PositionInfo posInfo = zone.getMediaRendererDevice().getAvTransportService().getPositionInfo();
        String uri = mediaInfo.getCurrentURI();
        if (uri == null) {
          
        } else if (uri.startsWith("x-rincon-queue:")) {
          ZonePlayer currentZone = null;
          // a queue is playing
          for (ZonePlayer zp : controller.getAllZonePlayers()) {
            if (uri.substring(15).startsWith(zp.getRootDevice().getUDN().substring(5))) {
              // this is the zoneplayer
              currentZone = zp;
              break;
            }
          }
          if (currentZone == null) {
            // unknown queue
            Debug.error("unknown queue: " + uri);
            return;
          }
          
          setQueueEntry(posInfo, currentZone);          
        } else if (uri.startsWith("x-rincon:")){
          // We're streaming from another sonos
          String id = uri.substring(9);
          ZonePlayer zp = controller.getZonePlayerById(id);
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
      List<Entry> queueEntry = zone.getMediaServerDevice().getContentDirectoryService().getQueue(posInfo.getTrackNum() -1,2);
      if (queueEntry.size() > 0) {
        final Entry entry = queueEntry.get(0);
        getDisplay().asyncExec(new Runnable() {
          public void run() {
            trackArtist.setText(entry.getCreator());
            trackAlbum.setText(entry.getAlbum());
            trackName.setText(entry.getTitle());

            ImageData imageData;
            Image artworkImage = null;
            try {
              imageData = entry.getAlbumArt(zone)[0];
              artworkImage = ImageUtilities.scaleImageTo(new Image(getDisplay(), imageData), 128, 128);
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            if (artwork.getImage() != null) {
              artwork.getImage().dispose();
            }
            artwork.setImage(artworkImage);

            layout();
          }
        });
      }
    }
  }
}
