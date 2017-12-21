// Usage:
//        java Client user-nickname server-hostname
//
// After initializing and opening appropriate sockets, we start two
// client threads, one to send messages, and another one to get
// messages.
//
// A limitation of our implementation is that there is no provision
// for a client to end after we start it. However, we implemented
// things so that pressing ctrl-c will cause the client to end
// gracefully without causing the server to fail.
//
// Another limitation is that there is no provision to terminate when
// the server dies.

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class Client {
	private static final String LOGIN = "login";
	private static final String LOGOUT = "logout";
	private static final String REGISTER = "register";
	private static final String USERS = "files/usernames.txt";
	private static final String AUSERS = "files/activeUsers.txt";
	private static boolean  m_run;
	private static ObjectOutputStream toServer;
	private static Socket server;
	private static ObjectInputStream fromServer;
	private static MessageQueue m_queue;
	public static String clientSession(ObjectOutputStream toServer, ObjectInputStream fromServer, String nickname, String hostname) {
		

		//creating client threads
		ClientSender sender = new ClientSender(nickname, toServer);
		ClientReceiver receiver = new ClientReceiver(fromServer);

		// Run them in parallel:
		receiver.start();
		sender.start();

		// Wait for them to end and close sockets.
		try {
			// I modified the order of closing the streams and socket.
			// toServer was closed between sender.join and receiver.join
			// and because of this i was getting "socket died" error. I
			// tried to fix it with if(server.ready()) in client receiver
			// butb
			// that created another error( messages were no longer sent).
			
			sender.join();
			receiver.join();

		} catch (InterruptedException e) {
			Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
		}
		return "";
	}

	//method for adding message to a queue
	public static void addMsg(String nickname,String text,Job job,MessageQueue m_queue){
		Message msg = new Message(nickname,text,job);
		m_queue.offer(msg);
		
	}
	//method for sending a message , writitng the message to a stream
	public static void sendMsgQ(ObjectOutputStream stream,MessageQueue m){
		try {
			stream.writeObject(m);//writing
			stream.flush();//forcing the message to be sent
		} catch (IOException e) {
			if(e.getMessage().equals("Broken pipe (Write failed)")){
				//if the execution gets here it means that the server was closed in the meantime, so we finish the client
				m_run = false;
			}
			else
			Report.errorAndGiveUp("Something went wrong :"+e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		ArrayList<String> commands = new ArrayList<>();
		commands.add("login - loggs in a user");
		commands.add("register - registers a user");
		commands.add("help - shows commands you can use");
        Console console = System.console();
		 m_queue = new MessageQueue();
		// Check correct usage:
		if (args.length != 1) {
			Report.errorAndGiveUp("Usage: java Client server-hostname");
		}

		// Initialize information:
		String hostname = args[0];
		BufferedReader user = new BufferedReader(new InputStreamReader(
				System.in));
		
		toServer = null;
		fromServer = null;
		server = null;

		try {
			//opening socket 
			server = new Socket(hostname, Port.number); 
			toServer = new ObjectOutputStream(server.getOutputStream());
	        fromServer = new ObjectInputStream(server.getInputStream());
		} catch (UnknownHostException e) {
			Report.errorAndGiveUp("Unknown host: " + hostname);
		} catch (IOException e) {
			Report.errorAndGiveUp("The server doesn't seem to be running "
					+ e.getMessage());
		}
		try {
			
			Message lastMsg = new Message("","",Job.EMPTY);
			m_run = true;
			System.out.println("Commands you can use: ");
			for(String s: commands){
				System.out.println(s);
			}
			while (m_run) { //looping for client requests , login register and quit , for now
				m_queue = new MessageQueue(); // assigning a new queue , if i don't do this sending trough the socket 
				//doesn't work as expected
				String action = user.readLine(); //reading the request
				if (action.equals(REGISTER)) { //if register, reading the username and pass
					System.out.print("Enter your username:");
					String name = user.readLine();
					//reading the password, hiding the text from the user
					//String pass = new String (console.readPassword("Enter your secret password: "));
					System.out.print("Enter yout passsword");
					String pass = user.readLine();
					//adding a message for the request handler to solve , with the password already crypted
					addMsg(name,User.crypt(pass),Job.REGISTER,m_queue);
				} else if (action.equals(LOGIN)) {//if login, reading the username and pass
					System.out.print("Enter your username:");
					String name = user.readLine();
					//reading the password, hiding the text from the user
					//String pass = new String (console.readPassword("Enter your secret password: "));
					System.out.print("Enter yout passsword");
					String pass = user.readLine();					//adding a message for the request handler to solve , with the password already crypted
					addMsg(name,User.crypt(pass),Job.LOGIN,m_queue);

				}else if (action.equals("help")){
					for(String s: commands){
						System.out.println(s);
					}
				}
				else if(action.equals("quit")){//if action is quit
					m_run = false;
					addMsg("","Client Closed",Job.QUIT,m_queue);
					//adding and sending the message triggering quit in the request handler
					sendMsgQ(toServer,m_queue);
					Report.behaviour("Client Closed");
					break;
				}
				sendMsgQ(toServer,m_queue); // sending the queue with requests to the request handler
				m_queue = (MessageQueue) fromServer.readObject(); //reading the answers from the request handler
				while(!m_queue.isEmpty()){
					//decomposint the message
					Message msg = m_queue.take();
					Job job = msg.getJob();
					String nickname = msg.getSender();
					String text = msg.getText();
					Report.behaviour(msg.toString());
					// if the user logged in successfully
					if(text.equals("login successfully")){
						//starting the chatting session, execution blocks in the cllient session method
						//untill it finishes(when the session closes)
						clientSession(toServer,fromServer,nickname,hostname);
						lastMsg = (Message) fromServer.readObject();//reading the last message from the conversation to see
						//if it was quit
						toServer.writeObject(lastMsg);//also sending the message to the request handler, so it cand also quits
						//if necessary
					}
					if(job.equals(job.FORCEDQUIT)){
						lastMsg = new Message("","",Job.QUIT);
						break;
					}
				}
				if(lastMsg.getJob().equals(Job.QUIT)){//if the last message was quit, also close the client
					m_run = false;
					addMsg("","Client Closed",Job.QUIT,m_queue);
					sendMsgQ(toServer,m_queue);//also sending the message to the request handler, so it cand also quits
						//if necessary
					break;
				}
			}
		} catch (IOException e) {
			if(e.getMessage() == null){//if the program reaches here, and the message is null, the server closed
				m_run = false;
				Report.behaviour("Client Closed (because server closed)");
				System.exit(1);
			}
			else{
				Report.errorAndGiveUp("IO error " + e.getMessage());
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	
}
