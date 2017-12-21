import java.io.*;
import java.net.*;

// Gets messages from other clients via the server (by the
// ServerSender thread).myClientmyClient

public class ClientReceiver extends Thread {

	private ObjectInputStream server;
	private boolean m_run;
	private final String EXIT_MSG_CLIENT = new String("Chat closed.");
	private final String EXIT_MSG_SERVER = new String(" closed the chat.");
	private MessageQueue m_queue;

	ClientReceiver(ObjectInputStream server) {
		this.server = server;
		m_run = true;
		m_queue = new MessageQueue();
	}

	public void run() {
		// Print to the user whatever we get from the server:

		try {
			while (m_run) {// added the boolean for the while loop
				//reading the messages sent by the server sender
				m_queue = (MessageQueue) server.readObject();
				while (!m_queue.isEmpty()) {
					//decomposing the message
					MessageQueue recipientsQueue;
					Message msg = m_queue.take();
					Job job = msg.getJob();
					String recipient = msg.getSender();
					String text = msg.getText();
					if (job.equals(Job.LOGOUT) || job.equals(Job.QUIT)) {//quitting if needed
						m_run = false;
						System.out.println(EXIT_MSG_CLIENT);
					} else if (job.equals(Job.MESSAGE)) {//else
						System.out.println("From " + msg.toString());//displaying the message
					}else if(job.equals(Job.CREATEGROUP)){
						System.out.println("Group"+" " + recipient + " type"+ " " + text + " is created" );
					}else if(job.equals(Job.CREATEGROUPFAILED)){
						System.out.println("Group "+" " + recipient + text );
					}else if(job.equals(Job.ADDMEMBER)){
						System.out.println(text);
					}else if(job.equals(Job.MEMBERADDED)){
						System.out.println( text );
					}else if(job.equals(Job.MEMBERNOTADDED)){
						System.out.println("Member "+ recipient+" not added because"+ " "+ text );
					}else if(job.equals(Job.MESSAGEGROUPFAILED)){
						System.out.println(text);
					}else if(job.equals(Job.TOADMIN)){
						System.out.println(text);
					}else if(job.equals(Job.REMOVEMEMBER)){
						System.out.println(text);
					}else if(job.equals(Job.REQUESTS)){
						System.out.println(recipient + " "+ text);
					}
				}
			}
		} catch (IOException e) {
			if(e.getMessage() == null){//if we get here and the message is null, the server closed
				m_run = false;
				Report.behaviour("Client Closed (because server closed)");
			}
			else
				Report.errorAndGiveUp("Server seems to have died " + e.getMessage());

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
