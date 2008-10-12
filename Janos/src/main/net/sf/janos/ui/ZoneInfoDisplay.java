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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.janos.control.AVTransportListener;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.MediaInfo;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.model.QueueModel;
import net.sf.janos.model.QueueModelListener;
import net.sf.janos.model.TrackMetaData;
import net.sf.janos.model.xml.ResultParser;
import net.sf.janos.model.xml.AVTransportEventHandler.AVTransportEventType;
import net.sf.janos.util.ui.ImageUtilities;
import net.sf.janos.util.ui.SingleWorkQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A UI component for displaying the now-playing and queue.
 * @author David Wheeler
 *
 */
public class ZoneInfoDisplay extends Composite implements AVTransportListener {

	private static final Log LOG = LogFactory.getLog(ZoneInfoDisplay.class);

	/**
	 * The maximum number of items to display in the queue
	 */
	public static final int QUEUE_LENGTH = 100;

	/**
	 * The colour of the labels
	 */
	private final Color LABEL_COLOR;


	/**
	 * The name of the artist of the currently playing track
	 */
	private final Label trackArtist;

	/**
	 * The name of the album of the currently playing track
	 */
	private final Label trackAlbum;

	/**
	 * The name of the currently playing track
	 */
	private final Label trackName;

	/**
	 * The label with the album artwork
	 */
	private final Label artwork;

	/**
	 * The queue
	 */
	private final Table queue;

	/**
	 * The current zone
	 */
	private ZonePlayer currentZone;

	/**
	 * The mouse listener for the queue table
	 */
	private QueueMouseListener queueMouseListener;

	/**
	 * Fills the queue table with data from the queue model
	 */
	private QueueDataFiller queueDataFiller = new QueueDataFiller();

	/**
	 * the image for the song that is currently playing
	 */
	private final Image nowPlayingImage;

	/**
	 * The image for songs that aren't playing
	 */
	private final Image emptyImage;

	/**
	 * The queue model
	 */
	private QueueModel queueModel = new QueueModel();

	/**
	 * updates the queue table when the queue model changes
	 */
	private TableUpdater queueModelListener;

	/*
	 * allow access to the Now Playing border for dynamic display
	 */
	private Group nowPlaying;

	/*
	 * a Thread to load Album Artwork when necessary
	 */
	private final ArtworkWorker artworkWorker;

	/**
	 * Creates a new QueueDisplay
	 * @param parent
	 * @param style
	 * @param controller
	 */
	public ZoneInfoDisplay(Composite parent, int style, ZonePlayer zone) {
		super(parent, style);
		this.currentZone = zone;
		LABEL_COLOR = new Color(parent.getDisplay(), new RGB(0, 0, 128));

		setLayout(new GridLayout(1, false));
		
		GridData gridData  = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
		
		nowPlaying = new Group(this, SWT.NONE);
		nowPlaying.setText("Now Playing");
		nowPlaying.setForeground(LABEL_COLOR);
		nowPlaying.setLayout(new GridLayout(2, false));
		nowPlaying.setLayoutData(gridData);
		
		GridData labelGridData = new GridData (GridData.HORIZONTAL_ALIGN_END );
		
		Label trackArtistLabel = new Label(nowPlaying, SWT.RIGHT);
		trackArtistLabel.setText("Artist:");
		trackArtistLabel.setForeground(LABEL_COLOR);
		trackArtist = new Label(nowPlaying, SWT.LEFT);
		trackArtist.setLayoutData(gridData);
		
		Label trackAlbumLabel = new Label(nowPlaying, SWT.RIGHT);
		trackAlbumLabel.setText("Album:");
		trackAlbumLabel.setForeground(LABEL_COLOR);
		trackAlbum = new Label(nowPlaying, SWT.LEFT);
		trackAlbum.setLayoutData(gridData);
		
		Label trackNameLabel = new Label(nowPlaying, SWT.RIGHT);
		trackNameLabel.setText("Name:");
		trackNameLabel.setForeground(LABEL_COLOR);

		trackName = new Label(nowPlaying, SWT.LEFT);
		trackName.setLayoutData(gridData);
		
		int width = trackArtistLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		width = Math.max( width, trackAlbumLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		width = Math.max( width, trackNameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		labelGridData.widthHint = width;
		trackNameLabel.setLayoutData(labelGridData);
		trackArtistLabel.setLayoutData(labelGridData);
		trackAlbumLabel.setLayoutData(labelGridData);
		
		artwork = new Label(nowPlaying, 0);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.widthHint = 128;
		gd.heightHint = 128;
		artwork.setLayoutData(gd);

		InputStream is = getClass().getResourceAsStream("/nowPlaying.png");

		nowPlayingImage = new Image(getDisplay(), is);
		try {
			is.close();
		} catch (IOException e) {
		}
		is = getClass().getResourceAsStream("/empty.png");
		emptyImage = new Image(getDisplay(), is);
		try {
			is.close();
		} catch (IOException e) {
		}


		queueModelListener = new TableUpdater();
		queueModel.addQueueModelListener(queueModelListener);

		Group queueBox = new Group(this, SWT.NONE);
		queueBox.setText("Zone Queue");
		queueBox.setForeground(LABEL_COLOR);
		queueBox.setLayout(new FillLayout());
		queue = new Table(queueBox, SWT.SINGLE | SWT.VIRTUAL);
		queue.setLinesVisible(true);
		queueMouseListener = new QueueMouseListener();
		queue.addMouseListener(queueMouseListener);
		TableColumn queueColumn = new TableColumn(queue, SWT.NONE);
		queueColumn.setText("Queue Entries");
		queueColumn.setWidth(SonosControllerShell.NOW_PLAYING_WIDTH);
		queue.addListener(SWT.SetData, queueDataFiller);
		GridData queueData = new GridData(GridData.FILL, GridData.FILL, true, true);
		queueBox.setLayoutData(queueData);
		nowPlaying.setData(queueData);

		artworkWorker = new ArtworkWorker(new SingleWorkQueue());
		artworkWorker.start();
		zone.getMediaRendererDevice().getAvTransportService().addAvTransportListener(this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (nowPlayingImage != null) {
			nowPlayingImage.dispose();
		}
		if (emptyImage != null) {
			emptyImage.dispose();
		}
		if (queueModel != null) {
			queueModel.removeQueueModelListener(queueModelListener);
		}
		super.dispose();
	}

	/**
	 * Reloads the now-playing and queue to be that from the given zone.
	 * @param zone
	 */
	public void showNowPlaying() {
		new NowPlayingFetcher().start();
	}

	/**
	 * Asynchronously sets the now playing details to the given information. The
	 * image is loaded in a background thread before being displayed.
	 * 
	 * @param artist
	 * @param album
	 * @param name
	 * @param artworkUrl
	 */
	protected void setNowPlayingAsync(final String artist, final String album, final String name, final URL artworkUrl) {
		getDisplay().asyncExec(new NowPlayingSetter(album, artworkUrl, artist, name));
	}

	/**
	 * Signifies that a queue is being played at the provided positionInfo
	 * @param posInfo
	 * @param zone
	 */
	private void setQueueEntry(PositionInfo posInfo, final ZonePlayer zone) {
		// TODO develop a queue model to display
		final List<Entry> queueEntries = zone.getMediaServerDevice().getContentDirectoryService().getQueue(0,QUEUE_LENGTH);
		if (posInfo != null ) {
			if (queueEntries.size() > 0) {
				final Entry currentEntry = queueEntries.get(posInfo.getTrackNum()-1);
				URL albumArtUrl = null;
				try {
					albumArtUrl = currentEntry.getAlbumArtURL(zone);
				} catch (MalformedURLException e) {
					LOG.error("Could not get album art URL: ", e);
				}
				setNowPlayingAsync(currentEntry.getCreator(), currentEntry.getAlbum(), currentEntry.getTitle(), albumArtUrl);
			} else {
				setNowPlayingAsync("No Music", "", "", null);
			}
		}
		getDisplay().asyncExec(new QueueUpdater(queueEntries));
	}

	/**
	 * {@inheritDoc}
	 */
	public void valuesChanged(Set<AVTransportEventType> events, AVTransportService source) {
		new NowPlayingFetcher().start();
	}

	/**
	 * Updates the queue
	 */
	private final class QueueUpdater implements Runnable {
		private final List<Entry> entries;

		private QueueUpdater(List<Entry> entries) {
			this.entries = entries;
		}

		public void run() {
			queueModel.setEntries(entries);
		}
	}

	/**
	 * Sets the now playing labels
	 */
	private class ArtworkWorker extends Thread {
		// Special end-of-stream marker. If a worker retrieves
		// an Object that equals this marker, the worker will terminate.
		final Object NO_MORE_WORK = new Object();

		SingleWorkQueue q;

		ArtworkWorker(SingleWorkQueue q) {
			this.q = q;
			this.setName("ArtworkWorker: " + currentZone.getDevicePropertiesService().getZoneAttributes().getName());
			this.setDaemon(true);
		}

    @Override
		public void run() {
			try {
				while (true) {
					// Retrieve some work; block if the queue is empty
					Object x = q.getWork();

					// Terminate if the end-of-stream marker was retrieved
					if (x == NO_MORE_WORK) {
						break;
					}

					// load the artwork
					if (x == null) {
						loadImage((Image)null, null);
					} else {
						loadImage((URL)x);
					}
				}
			} catch (InterruptedException e) {
			}
		}

		private void loadImage(URL url) {
			ImageData imageData;
			Image artworkImage = null;
			InputStream artworkStream = null;
			try {
				artworkStream = new BufferedInputStream(url.openStream());
				imageData = new ImageLoader().load(artworkStream)[0];
				Image tmpImage = new Image(getDisplay(), imageData);
				artworkImage = ImageUtilities.scaleImageTo(tmpImage, 128, 128);
				tmpImage.dispose();
			} catch (Exception e) {
				// no artwork. TODO add default image
			} finally {
				if (artworkStream != null) {
					try {
						artworkStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			loadImage(artworkImage, url);
		}

		private void loadImage(Image image, URL url) {
			final Image i = image;
			final URL u = url;
			
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					
					URL lastURL = (URL)artwork.getData();
					if (u==null || lastURL == null || !lastURL.sameFile(u)) {
						Image oldImage = artwork.getImage();
						artwork.setImage(i);
						if ( oldImage != null) {
							oldImage.dispose();
						}
						artwork.setData(u);
						layout();
					}
				}
			});

		}
	}

	private final class NowPlayingSetter extends Thread {
		private final String album;
		private final URL url;
		private final String artist;
		private final String name;

		private NowPlayingSetter(String album, URL url, String artist, String name) {
			this.album = album;
			this.url = url;
			this.artist = artist;
			this.name = name;
		}

    @Override
		public void run() {
			trackArtist.setText(artist);
			trackAlbum.setText(album);
			trackName.setText(name);

			URL oldURL = (URL)artwork.getData();
			if (oldURL != null && url != null && url.sameFile(oldURL) == false) {
				if (artwork.getImage() != null) {
					artwork.getImage().dispose();
					artwork.setData(null);
				}
				artwork.setImage(null);
			}
		
			artworkWorker.q.addWork(url);
			
			layout();
		}
	}

	/**
	 * A Runnable that requests the currently playing track and applies the info to this QueueDisplay
	 * @author David Wheeler
	 *
	 */
	protected class NowPlayingFetcher extends Thread {


		public NowPlayingFetcher() {
		}
		
    @Override
		public void run() {
			if (currentZone == null) {
				return;
			}
			try {
				MediaInfo mediaInfo = currentZone.getMediaRendererDevice().getAvTransportService().getMediaInfo();
				PositionInfo posInfo = currentZone.getMediaRendererDevice().getAvTransportService().getPositionInfo();
				String uri = mediaInfo.getCurrentURI();

				if (uri == null || posInfo == null) {
					setNowPlayingAsync("No Music", "", "" , null);
					setQueueEntry(null, currentZone);
				} else if (uri.startsWith("x-rincon-queue:")) {
					setQueueEntry(posInfo, currentZone);
					queueModel.setNowPlaying(posInfo.getTrackNum() -1);
				} else if (uri.startsWith("x-rincon:")){
					// We're streaming from another sonos
					// XXXM BML: I broke this.  I must fix it.
//					String id = uri.substring(9);
//					ZonePlayer zp = controller.getZonePlayerModel().getById(id);
//					setQueueEntry(posInfo, zp);
				} else if (uri.startsWith("x-file-cifs:")) {
					// just playing one file
					setNowPlayingAsync(mediaInfo.getCurrentURIMetaData().getCreator(), 
							mediaInfo.getCurrentURIMetaData().getAlbum(), 
							mediaInfo.getCurrentURIMetaData().getTitle(), 
							mediaInfo.getCurrentURIMetaData().getAlbumArtUrl(currentZone));

				} else if (uri.startsWith("x-rincon-mp3radio:")) {
					// yep, it's the radio
					setNowPlayingAsync("Internet Radio", mediaInfo.getCurrentURI().substring(mediaInfo.getCurrentURI().lastIndexOf("://") + 3), mediaInfo.getCurrentURIMetaData().getTitle(), null);
					setQueueEntry(null, currentZone);
				} else if (uri.startsWith("x-rincon-stream:")) {
					// line in stream
					setNowPlayingAsync("Line In", "", mediaInfo.getCurrentURIMetaData().getTitle(), null);
					setQueueEntry(null, currentZone);
				} else if (uri.startsWith("pndrradio:")) {
					// Pandora
					try {
						TrackMetaData i = ResultParser.parseTrackMetaData(posInfo.getTrackMetaData());
						setNowPlayingAsync("Pandora: " + i.getCreator(), i.getAlbum(), i.getTitle() , new URL(i.getAlbumArtUri()));
					} catch (Exception e) {
						setNowPlayingAsync("No Music", "", "" , null);
					}
					setQueueEntry(null, currentZone);
				} else if (uri.startsWith("rdradio:station:")) {
						// Rhapsody Station
						try {
							TrackMetaData i = ResultParser.parseTrackMetaData(posInfo.getTrackMetaData());
							setNowPlayingAsync(i.getCreator(), i.getAlbum(), i.getTitle() , i.getAlbumArtUrl(currentZone));
						} catch (Exception e) {
							setNowPlayingAsync("No Music", "", "" , null);
						}
						setQueueEntry(null, currentZone);
				} else {
					if (LOG.isWarnEnabled() && mediaInfo != null ) {
						LOG.warn("Couldn't find type of " + mediaInfo.getCurrentURIMetaData().getId() + ": " + uri);
					}
				}

				// TODO else...
			} catch (Exception e) {
				LOG.error("Couldn't load queue", e);
			}
		}
	}

	public void displayEmptyQueue() {
		getDisplay().asyncExec(new QueueUpdater(new ArrayList<Entry>()));
	}

	/**
	 * Plays any queue entry on double click
	 * @author David Wheeler
	 *
	 */
	public class QueueMouseListener implements MouseListener {
		public void mouseDoubleClick(MouseEvent e) {
			int queueIndex = ((Table)e.getSource()).getSelectionIndex();
			try {
				currentZone.playQueueEntry(queueIndex + 1);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}

		public void mouseDown(MouseEvent e) {
		}

		public void mouseUp(MouseEvent e) {
		}
	}

	/**
	 * Populates the queue Table with data from the QueueModel
	 * @author David Wheeler
	 *
	 */
	public class QueueDataFiller implements Listener {

		public void handleEvent(Event event) {
			TableItem row = (TableItem) event.item;
			row.setText(queueModel.getTitle(event.index));
			if (queueModel.isNowPlaying(event.index)) {
				row.setImage(nowPlayingImage);
			}
			else {
				row.setImage(emptyImage);
			}
		}
	}

	/**
	 * Notifies the queue Table of any changes to the QueueModel
	 * @author David Wheeler
	 *
	 */
	public class TableUpdater implements QueueModelListener {

		private Runnable updater = new Runnable() {
			public void run() {
				queue.setItemCount(queueModel.getSize());
				queue.clearAll();
				if (queueModel.getNowPlaying() > -1) {
					queue.showItem(queue.getItem(queueModel.getNowPlaying()));
				}
			}
		};

		public void entriesChanged(QueueModel model) {
			updater.run();
		}

		public void nowPlayingChanged(QueueModel model) {
			// TODO a bit lazy - shouldn't have to repaint the whole table, just a few
			// rows
			queue.getDisplay().asyncExec(updater);
		}
	}
}
