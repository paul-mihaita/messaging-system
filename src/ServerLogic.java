import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ServerLogic extends Thread {
	private ObjectOutputStream toClient;
	private ObjectInputStream fromClient;
	private boolean m_run;
	private ClientTable clientTable;
	private MessageQueue m_queue;
	private ClientTable registeredUsers;
	private GroupTable groupTable;
	
	public ServerLogic(ObjectOutputStream toClient,
			ObjectInputStream fromClient, ClientTable t, ClientTable r, GroupTable groupTable) {
		this.toClient = toClient;
		this.fromClient = fromClient;
		this.m_run = true;
		this.clientTable = t;
		this.m_queue = new MessageQueue();
		this.registeredUsers = r;
		this.groupTable = groupTable;
	}

	@Override
	public void run() {
		
		try {
			boolean messageSent = false;
			while (m_run) {
				//initialising variables
				Message lastMsg = new Message("", "", Job.EMPTY);
				MessageQueue temp = new MessageQueue();
				//reading the requests from the client
				m_queue = (MessageQueue) fromClient.readObject();

				while (!m_queue.isEmpty()) {
					//decomposing the message (request)
					Message request = m_queue.take();
					Job job = request.getJob();
					String nickname = request.getSender();
					String text = request.getText();
					User user = new User(nickname, text);
					//dealing with the request
					switch (job) {
					case REGISTER: //if register
						if (!registeredUsers.isMember(user)) {//checking to see if the user is already registered
							addMsg(nickname, "registered successfully",
									Job.REGISTER, temp);//adding the success message to the answer queue
							registeredUsers.add(user);//adding the user to the registered users
						} else {
							addMsg(nickname, "already registered",
									Job.REGISTER, temp);//adding the already registered message to the answer queue
						}
						break;
					case LOGIN:
						if (registeredUsers.isMember(user)//if login, check to see if the user is member and the user is not logged in
							//and the password intered matches the password the user registered with
								&& !clientTable.isMember(user)
								&& text.equals(registeredUsers
										.getUser(nickname).getPassword())) {
							addMsg(nickname, "login successfully", Job.LOGIN,
									temp);//adding the success message to the answer queue
							clientTable.add(user); //adding the user to the "active users" table
							messageSent = true;
							sendMsgQ(toClient, temp); //sending the message to the client, so he knows to log in
							temp = new MessageQueue(); //reseting the answer queue
							startSession(nickname); //starting the session
							lastMsg = (Message) fromClient.readObject();//reading the last message from the chat
						} else {
							//checking which condition was not true
							if (!registeredUsers.isMember(user)) 
								addMsg(nickname, "not registered", Job.LOGIN,
										temp);
							else if (clientTable.isMember(user))
								addMsg(nickname, "already connected",
										Job.LOGIN, temp);
							else
								addMsg("", "wrong password", Job.LOGIN, temp);
						}
						break;
					case QUIT: //if the "request" was quit
						m_run = false;
						messageSent = true;
						Report.behaviour(text);
						break;
					case FORCEDQUIT: 
						m_run = false;
						messageSent = true;
						Report.behaviour(text);
						break;
					}
				}
				if (messageSent == false) //if the queue was not sent in the login phase
					sendMsgQ(toClient, temp);
				else
					messageSent = false;
				if (lastMsg.getJob().equals(Job.QUIT)) {//if the last message from chat was quit
					Report.behaviour("Client Closed");
					m_run = false;
				}
			}
		} catch (IOException e) {
			if(e.getMessage() == null){
				Report.behaviour("Client closed");
				//terminate, means that server is closed
			}
			else
			Report.error("Something wrong " + e.getMessage());

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	

	public void startSession(String clientName) { //starting the chat session

		Report.behaviour(clientName + " connected");
		//copying the unsent messages to the queue of the logged in user
		MessageQueue from = registeredUsers.getQueue(clientName);
		MessageQueue to = clientTable.getQueue(clientName);
		while (!from.isEmpty()) {
			Message msg = from.take();
			to.offer(msg);
		}

		// We create and start a new thread to read from the client:
		ServerReceiver receiver = new ServerReceiver(clientName, fromClient,
				clientTable, registeredUsers,groupTable);

		// We create and start a new thread to write to the client:
		ServerSender sender = new ServerSender(
				clientTable.getQueue(clientName), toClient);
		receiver.start();
		sender.start();
		try {
			//waiting for the session to be ended
			sender.join();
			receiver.join();
		} catch (InterruptedException e) {
			Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
		}
	}
	public void stopp(){//changing the boolean for the main run loop to false
		m_run = false;
	}
	public void addMsg(String nickname, String text, Job job,
			MessageQueue m_queue) { ///adding a message to a queue
		Message msg = new Message(nickname, text, job);
		m_queue.offer(msg);

	}

	public void sendMsgQ(ObjectOutputStream stream, MessageQueue m) { //sending a queue trough a stream(socket)
		try {
			stream.writeObject(m);
			stream.flush();
		} catch (IOException e) {
			
			Report.errorAndGiveUp("Something went wrong :" + e.getMessage());
		}
	}
}
