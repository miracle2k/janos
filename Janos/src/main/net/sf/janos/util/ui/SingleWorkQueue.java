package net.sf.janos.util.ui;

import java.util.LinkedList;

// A specialization of the WorkQueue pattern which implements
// a single piece of work.  An arbitrary number of callers
// may place an arbitrarily large number of work items on the queue.
// This class guarantees that only the lat work item placed on the queue
// since the last call to getWork() will be returned.  All other WorkItems
// are ignored.
public class SingleWorkQueue {
    LinkedList<Object> queue = new LinkedList<Object>();

    // Add work to the work queue
    public synchronized void addWork(Object o) {
    	while (!queue.isEmpty()) {
    		queue.remove();
    	}
        queue.addLast(o);
        notify();

    }

    // Retrieve work from the work queue; block if the queue is empty
    public synchronized Object getWork() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        Object o = queue.removeFirst();
        return o;
    }
}
