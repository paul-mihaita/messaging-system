// We use https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/BlockingQueue.html

import java.io.Serializable;
import java.util.concurrent.*;

public class MessageQueue implements Serializable {
	// We choose the LinkedBlockingQueue implementation of BlockingQueue:
	private BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();

	// Inserts the specified message into this queue.
	public void offer(Message m) {
		queue.offer(m);
	}

	// Retrieves and removes the head of this queue, waiting if
	// necessary until an element becomes available.
	public Message take() {

		while (true) {
			try {
				return (queue.take());
			} catch (InterruptedException e) {
				// This can in principle be triggered by queue.take().
				// But this would only happen if we had interrupt() in our code,
				// which we don't.
				//
				// In any case, if it could happen, we should do nothing here
				// and try again until we succeed.
			}
		}
	}


	@Override
	public String toString() { // converting the queue to a string
		//messages are separated by '|'
		String s = "";
		while(!queue.isEmpty()){
			try {
				s+="|"+queue.take().toString();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return s;

	}

	public boolean isEmpty() {//if the queue is empty
		return queue.isEmpty();
	}
}
