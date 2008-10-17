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

import java.io.IOException;
import java.io.InputStream;
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
import net.sf.janos.model.xml.AVTransportEventHandler.AVTransportEventType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A UI component for displaying the now-playing and queue.
 * @author David Wheeler
 *
 */
public class QueueDisplay extends Composite implements AVTransportListener {

	private static final Log LOG = LogFactory.getLog(QueueDisplay.class);

	/**
	 * The maximum number of items to display in the queue
	 */
	public static final int QUEUE_LENGTH = 100;

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

	/**
	 * Creates a new QueueDisplay
	 * @param parent
	 * @param style
	 * @param controller
	 */
	public QueueDisplay(Composite parent, int style, ZonePlayer zone) {
		super(parent, style);
		this.currentZone = zone;

		setLayout(new GridLayout(1, true));

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

		queue = new Table(this, SWT.SINGLE | SWT.VIRTUAL);
		queue.setLinesVisible(true);
		queueMouseListener = new QueueMouseListener();
		queue.addMouseListener(queueMouseListener);
		TableColumn queueColumn = new TableColumn(queue, SWT.NONE);
		queueColumn.setText("Queue Entries");
		queue.addControlListener(tableResizer);
		queue.addListener(SWT.SetData, queueDataFiller);
		GridData queueData = new GridData(GridData.CENTER, GridData.BEGINNING, false, false);
		queueData.horizontalAlignment = GridData.FILL;
		queueData.grabExcessHorizontalSpace = true;
		queue.setLayoutData(queueData);

		zone.getMediaRendererDevice().getAvTransportService().addAvTransportListener(this);
	}

	static class TableResizer implements ControlListener {
		@Override
		public void controlMoved(ControlEvent arg0) {
		}

		@Override
		public void controlResized(ControlEvent arg0) {
			Table t = (Table)arg0.widget;
			TableColumn c = t.getColumn(0);
			c.setWidth(t.getClientArea().width);
		}
	};
	static TableResizer tableResizer = new TableResizer();


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
	 * Signifies that a queue is being played at the provided positionInfo
	 * @param posInfo
	 * @param zone
	 */
	private void setQueueEntry(PositionInfo posInfo, final ZonePlayer zone) {
		final List<Entry> queueEntries = zone.getMediaServerDevice().getContentDirectoryService().getQueue(0,QUEUE_LENGTH);
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
					setQueueEntry(null, currentZone);
				} else {
					setQueueEntry(posInfo, currentZone);
					queueModel.setNowPlaying(posInfo.getTrackNum() -1);
				} 
			} catch (Exception e) {
				LOG.error("Couldn't load queue", e);
			}
		}
	}

	//	public void displayEmptyQueue() {
	//		getDisplay().asyncExec(new QueueUpdater(new ArrayList<Entry>()));
	//	}

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
