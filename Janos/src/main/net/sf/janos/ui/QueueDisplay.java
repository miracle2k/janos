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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.ApplicationContext;
import net.sf.janos.control.AVTransportListener;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.BrowseHandle;
import net.sf.janos.control.EntryCallback;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.Entry;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.model.QueueModel;
import net.sf.janos.model.QueueModelListener;
import net.sf.janos.model.xml.AVTransportEventHandler.AVTransportEventType;
import net.sf.janos.ui.dnd.EntryTransfer;
import net.sf.janos.ui.dnd.QueueItemTransfer;
import net.sf.janos.util.EntryHelper;
import net.sf.janos.util.ui.ImageUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
	 * The queue
	 */
	private final Table queue;

	/**
	 * The current zone
	 */
	private ZonePlayer zone;

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
		this.zone = zone;

		setLayout(new GridLayout(2, false));

		nowPlayingImage = new Image(getDisplay(), ImageUtilities.loadImageDataFromSystemClasspath("nowPlaying.png"));
		emptyImage = new Image(getDisplay(), ImageUtilities.loadImageDataFromSystemClasspath("empty.png"));


		queueModelListener = new TableUpdater();
		queueModel.addQueueModelListener(queueModelListener);

		queue = new Table(this, SWT.MULTI | SWT.VIRTUAL);
		queue.setLinesVisible(true);
		queueMouseListener = new QueueMouseListener();
		queue.addMouseListener(queueMouseListener);
		TableColumn queueColumn = new TableColumn(queue, SWT.NONE);
		queueColumn.setText("Queue Entries");
		queue.addControlListener(tableResizer);
		queue.addListener(SWT.SetData, queueDataFiller);
		GridData queueData = new GridData(SWT.FILL, SWT.FILL, true, true);
		queue.setLayoutData(queueData);
		queue.setHeaderVisible(true);
		
		Composite buttonPanel = new Composite(this, SWT.None);
		GridLayout buttonLayout = new GridLayout(1, false);
		buttonLayout.marginBottom = 0;
		buttonLayout.marginLeft = 0;
    buttonLayout.marginTop = 0;
    buttonLayout.marginRight = 0;
    buttonLayout.verticalSpacing = 0;
    buttonLayout.horizontalSpacing = 0;
    buttonLayout.marginWidth = 0;
    buttonPanel.setLayout(buttonLayout);
		buttonPanel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
		
		Button deleteButton = new Button(buttonPanel, SWT.FLAT);
		deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
		deleteButton.setImage(new Image(deleteButton.getDisplay(), ImageUtilities.loadImageDataFromSystemClasspath("remove.png")));
		deleteButton.setToolTipText("Remove selection from queue");
		deleteButton.addSelectionListener(new SelectionAdapter() {
		  @Override
		  public void widgetSelected(SelectionEvent e) {
		    deleteSelection();
		  }
		});
		Button clearButton = new Button(buttonPanel, SWT.FLAT);
		clearButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
		clearButton.setImage(new Image(clearButton.getDisplay(), ImageUtilities.loadImageDataFromSystemClasspath("remove-all.png")));
		clearButton.setToolTipText("Clear Queue");
		clearButton.addSelectionListener(new SelectionAdapter() {
		  @Override
		  public void widgetSelected(SelectionEvent e) {
		    deleteAll();
		  }
		});
		
		// Set up Drag & Drop
    DragSource dragSource = new DragSource(queue, DND.DROP_MOVE);
    dragSource.setTransfer(new Transfer[] {QueueItemTransfer.getInstance(), EntryTransfer.getInstance()});
    dragSource.addDragListener(new QueueDragListener());
    
    DropTarget dropTarget = new DropTarget(queue, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
    dropTarget.setTransfer(new Transfer[] {QueueItemTransfer.getInstance(), EntryTransfer.getInstance(), URLTransfer.getInstance()});
    dropTarget.addDropListener(new QueueDropListener());

    // Set up tooltips
    ApplicationContext.getInstance().getShell().getToolTipHandler().activateHoverHelp(queue);
    
		zone.getMediaRendererDevice().getAvTransportService().addAvTransportListener(this);
		
		queue.addKeyListener(new DeleteListener());
	}

	static TableResizer tableResizer = new TableResizer();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		
		zone.getMediaRendererDevice().getAvTransportService().removeAvTransportListener(this);
		
		if (nowPlayingImage != null) {
			nowPlayingImage.dispose();
		}
		if (emptyImage != null) {
			emptyImage.dispose();
		}
		if (queueModel != null) {
			queueModel.removeQueueModelListener(queueModelListener);
		}
		
		queue.removeMouseListener(queueMouseListener);
		queue.removeControlListener(tableResizer);
		queue.removeListener(SWT.SetData, queueDataFiller);

		super.dispose();
	}

	/**
	 * Reloads the now-playing and queue to be that from the given zone.
	 * @param zone
	 */
	public void showNowPlaying() {
		setQueueEntries(zone);
	}


	/**
	 * Signifies that a queue is being played at the provided positionInfo
	 * @param zone
	 */
	private void setQueueEntries(final ZonePlayer zone) {

		zone.getMediaServerDevice().getContentDirectoryService().getAllEntriesAsync(new EntryCallback() {

			final List<Entry> queueEntries = new ArrayList<Entry>();
			
			public void addEntries(BrowseHandle handle, Collection<Entry> entries) {
				queueEntries.addAll(entries);
			}

			public void retrievalComplete(BrowseHandle handle, boolean completedSuccessfully) {
				if (completedSuccessfully) {
					getDisplay().asyncExec(new QueueUpdater(queueEntries));
				} else {
					getDisplay().asyncExec(new QueueUpdater(new ArrayList<Entry>()));
				}
				

				try {
					PositionInfo posInfo = zone.getMediaRendererDevice().getAvTransportService().getPositionInfo();
					queueModel.setNowPlaying(posInfo.getTrackNum() -1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void updateCount(BrowseHandle handle, int count) {
			}
			
		}, "Q:0");
		
	}
	
	/**
	 * Deletes the currently selected entries
	 */
	private void deleteSelection() {
	  int[] selection = queue.getSelectionIndices();
	  Arrays.sort(selection);
	  try {
	    for (int selectionIndex = selection.length-1; selectionIndex >= 0; selectionIndex--) {
	      int index = selection[selectionIndex];
	      zone.getMediaRendererDevice().getAvTransportService().removeTrackFromQueue(queueModel.getEntryAt(index));
	    }
	  } catch (Exception e) {
	    LOG.debug("Couldn't delete queue entry : ", e);
	  }
	}
	
	private void deleteAll() {
    try {
      zone.getMediaRendererDevice().getAvTransportService().clearQueue();
    } catch (Exception e) {
      LOG.debug("Couldn't delete queue entry : ", e);
    }
	}

  /**
	 * {@inheritDoc}
	 */
	public void valuesChanged(Set<AVTransportEventType> events, AVTransportService source) {
		setQueueEntries(zone);
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
	 * Plays any queue entry on double click
	 * @author David Wheeler
	 *
	 */
	public class QueueMouseListener implements MouseListener {
		public void mouseDoubleClick(MouseEvent e) {
			int queueIndex = ((Table)e.getSource()).getSelectionIndex();
			try {
				zone.playQueueEntry(queueIndex);
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
			row.setData("ENTRY", queueModel.getEntryAt(event.index));
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
				if (queueModel.getNowPlaying() > -1 && queueModel.getSize() > 0) {
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
	
	 private class QueueDropListener extends DropTargetAdapter {
	    @Override
	    public void dragEnter(DropTargetEvent event) {
	      LOG.debug("Drag enter");
	      for (TransferData dataType : event.dataTypes) {
	        if (QueueItemTransfer.getInstance().isSupportedType(dataType) && 
	            (event.operations & DND.DROP_MOVE) != 0) {
	          LOG.debug("Drag enter QueueItem");
	          event.detail = DND.DROP_MOVE;
	          event.currentDataType = dataType;
	          // this is preferred choice, don't check the others.
	          break;
	        } else if (EntryTransfer.getInstance().isSupportedType(dataType) &&
	            (event.operations & DND.DROP_COPY) != 0) {
	          LOG.debug("Drag enter Entry");
	          event.detail = DND.DROP_COPY;
	          event.currentDataType = dataType;
	        } else if (URLTransfer.getInstance().isSupportedType(dataType)) {
	          LOG.debug("Drag enter Url");
            event.currentDataType = dataType;
	          if ((event.operations & DND.DROP_COPY) != 0) {
	            event.detail = DND.DROP_COPY;
	          } else if ((event.operations & DND.DROP_LINK) != 0) {
	            event.detail = DND.DROP_LINK;
	          }
	        }
	      }
	    }

	    @Override
	    public void dragOver(DropTargetEvent event) {
	      event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
	    }

	    @Override
	    public void drop(DropTargetEvent event) {
	      DropTarget target = (DropTarget) event.widget;
	      Table table = (Table) target.getControl();
	      TableItem targetItem = (TableItem) event.item;
	      int targetIndex = targetItem == null ? -1 : table.indexOf(targetItem);
	      if (targetIndex < 0) {
	        targetIndex = table.getItemCount() + 1;
	      }
	      try {
	        if (QueueItemTransfer.getInstance().isSupportedType(event.currentDataType)){
	          LOG.debug("Processing Queue item move. ");
	          int[] data = (int[]) event.data;
	          for (int entry : data) {
	            QueueDisplay.this.zone.getMediaRendererDevice().getAvTransportService().reorderTracksInQueue(entry, 1, targetIndex);
	          }
	        } else if (EntryTransfer.getInstance().isSupportedType(event.currentDataType)) {
	          LOG.debug("Processing Queue Entry add. ");
	          // Get the dropped data
	          Entry[] data = (Entry[]) event.data;

	          for (Entry entry : data) {
	            QueueDisplay.this.zone.getMediaRendererDevice().getAvTransportService().addToQueue(entry, targetIndex);
	          }
	        } else if (URLTransfer.getInstance().isSupportedType(event.currentDataType)) {
	          LOG.debug("Processing Url add. ");
	          String data = (String) event.data;
	          QueueDisplay.this.zone.getMediaRendererDevice().getAvTransportService().addToQueue(EntryHelper.createEntryForUrl(data), targetIndex);
	        }
	      } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      } catch (UPNPResponseException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	      }
	    }
	  }

	  private class QueueDragListener extends DragSourceAdapter {
	    
      private int[] tableSelection;

      @Override
      public void dragStart(DragSourceEvent event) {
        DragSource ds = (DragSource) event.widget;
        Table table = (Table) ds.getControl();
        int[] selection = table.getSelectionIndices();
        if (selection.length < 1) {
          event.doit = false;
        } else {
          tableSelection = selection;
          /*
           * TODO: there's a bizarre bug where dragging over an SWT table
           * deselects any selected entry in that table. we may need to
           * preserve the selection of ALL tables here (yuk!)
           */
        }
      }

	    @Override
	    public void dragFinished(DragSourceEvent event) {
	      if (event.detail == DND.DROP_NONE || event.doit == false) {
	        LogFactory.getLog(getClass()).debug("No DnD performed");
	      }
	      tableSelection = null;
	    }

	    @Override
	    public void dragSetData(DragSourceEvent event) {
	      // Get the selected items in the drag source
	      if (EntryTransfer.getInstance().isSupportedType(event.dataType)) {
	        Entry[] entries = new Entry[tableSelection.length];
	        for (int i=0;i<tableSelection.length;i++) {
	          entries[i] = queueModel.getEntryAt(tableSelection[i]);
	        }

	        event.data = entries;
	      } else if (QueueItemTransfer.getInstance().isSupportedType(event.dataType)) {
	        event.data = tableSelection;
	      }
	    }
	  }

	  private static class TableResizer implements ControlListener {
	    public void controlMoved(ControlEvent arg0) {
	    }

	    public void controlResized(ControlEvent arg0) {
	      Table t = (Table)arg0.widget;
	      TableColumn c = t.getColumn(0);
	      c.setWidth(t.getClientArea().width);
	    }
	  };

	  /**
	   * @author David Wheeler
	   *
	   */
	  public class DeleteListener extends KeyAdapter {
	    
	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void keyPressed(KeyEvent e) {
	      if (e.character == SWT.BS || e.character == SWT.DEL) {
	        // delete or backspace pressed
	        deleteSelection();
	      }
	    }
	  }
	  
}
