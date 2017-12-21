import java.net.*;
import java.io.*;

// Continuously reads from message queue for a particular client,
// forwarding to the client.

public class ServerSender extends Thread {
	private MessageQueue clientQueue;
	private ObjectOutputStream client;
	private boolean m_run;
	private MessageQueue m_queue;

	public ServerSender(MessageQueue q, ObjectOutputStream c) {
		clientQueue = q;
		client = c;
		m_run = true;
		m_queue = new MessageQueue();
	}

	public void run() {
		Message lastMsg= new Message("","",Job.EMPTY);
		while (m_run) {//reading messages to be sent from the queue
			m_queue = new MessageQueue();
			Message msg = clientQueue.take();
			Job job = msg.getJob();
			m_queue.offer(msg);
			sendMsgQ(client,m_queue);//sending
			if(job.equals(Job.LOGOUT)||job.equals(Job.QUIT)){//quitting if needed
				m_run = false;
				lastMsg = msg;
			}
		}
		try {
			client.writeObject(lastMsg); //sending the last message from the chat to the client
		} catch (IOException e) {
		}
	}
	//adding a message to a queue
	public synchronized void addMsg(String nickname,String text,Job job,MessageQueue m_queue){
		Message msg = new Message(nickname,text,job);
		m_queue.offer(msg);
		
	}
	//sending a message trough a stream(socket)
	public synchronized void sendMsgQ(ObjectOutputStream stream,MessageQueue m){
		try {
			stream.writeObject(m);
			stream.flush();
		} catch (IOException e) {
			if(e.getMessage().equals("Broken pipe (Write failed)")){
				m_run = false;
			}
			else{
			
			Report.errorAndGiveUp("Something went wrong :"+e.getMessage());
			}
		}
	}
}
