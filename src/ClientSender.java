import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

// Repeatedly reads recipient's nickname and text from the user in two
// separate lines, sending them to the server (read by ServerReceiver
// thread).

public class ClientSender extends Thread {

	private static String nickname;
	private static ObjectOutputStream server;
	private static boolean m_run;
	private static MessageQueue m_queue;
	private static ArrayList<String> commands = new ArrayList<>();
	ClientSender(String nickname, ObjectOutputStream server) {
		this.nickname = nickname;
		this.server = server;
		m_run = true;
		m_queue = new MessageQueue();
		commands.add("help - shows commands you can use");
		commands.add("logout - loggs you out");
		commands.add("quit - loggs out and closes the client");
		commands.add("message - sends a message to a user");
		commands.add("registerGroup - registers a group");
		commands.add("addMembers - add one or more members to a group");
		commands.add("messageGroup - message a group");
		commands.add("removeMembers - remove one or more mebers from a group");
		commands.add("removeMe - remove yourself from a group");
		commands.add("changeToAdmins - change normal members of a group to admins");
		commands.add("join - send a request to join a public group");
		commands.add("requests - shows requests for a group");
		commands.add("showGroups - shows public groups ");
	}
	
	
	
	public void run() {
		
		
		// So that we can use the method readLine:
		BufferedReader user = new BufferedReader(new InputStreamReader(
				System.in));

		try {
			// Then loop forever sending messages to recipients via the server:
			System.out.println("Commands you can use: ");
			for(String s: commands){
				System.out.println(s);
			}
			while (m_run) {
				String recipient = user.readLine();//reading the action from the user
				//for now quit,logout,registergroup,or message
				if (recipient.equals("logout") || recipient.equals("quit")) {
					m_run = false;
					//adding the specific message to the queue
					if (recipient.equals("logout"))
						addMsg(nickname, recipient, Job.LOGOUT, m_queue); 
					if (recipient.equals("quit"))
						addMsg(nickname, recipient, Job.QUIT, m_queue);
					sendMsgQ(server, m_queue); //sending the queue to the server receiver
				} else if (recipient.equals("message")) {
					//reading the message from the user
					System.out.print("To: ");
					recipient = user.readLine();
					System.out.print("Text: ");
					String text = user.readLine();
					addMsg(recipient, text, Job.MESSAGE, m_queue);//adding the message to the queue
					sendMsgQ(server, m_queue);//sending the queue to the server receiver
				}else if(recipient.equals("registerGroup")){
					System.out.print("Name: ");
					String name = user.readLine();
					System.out.print("Type: ");
					String type = user.readLine();
					addMsg(name,type,Job.CREATEGROUP,m_queue);
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("addMembers")){
					System.out.print("Group Name: ");
					String gname = user.readLine();
					System.out.print("\"admin\" or \"user\": ");
					String type = user.readLine();
					System.out.print("Names separated by \'|\': ");
					String names = user.readLine();
					names.replaceAll("\\s+","");
					String[] toAdd = names.split("\\|");
					for(String s : toAdd){
						if(type.equals("admin"))
							addMsg(s,gname,Job.ADDADMIN,m_queue);
						if(type.equals("user"))
							addMsg(s,gname,Job.ADDMEMBER,m_queue);

					}
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("messageGroup")){
					System.out.print("Group Name: ");
					String gname = user.readLine();
					System.out.print("Text: ");
					String text = user.readLine();
					addMsg(gname,text,Job.MESSAGEGROUP,m_queue);
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("removeMembers")){
					System.out.print("Group Name: ");
					String gname = user.readLine();
					System.out.print("Names separated by \'|\': ");
					String names = user.readLine();
					names.replaceAll("\\s+","");
					String[] toAdd = names.split("\\|");
					for(String s : toAdd){
						addMsg(s,gname,Job.REMOVEMEMBER,m_queue);
					}
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("removeMe")){
					System.out.print("Group Name: ");
					String gname = user.readLine();
					String name = nickname;
					addMsg(name,gname,Job.REMOVEMEMBER,m_queue);
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("changeToAdmins")){
					System.out.print("Group Name: ");
					String gname = user.readLine();
					System.out.print("Names separated by \'|\': ");
					String names = user.readLine();
					names.replaceAll("\\s+","");
					String[] toAdd = names.split("\\|");
					for(String s : toAdd){
						addMsg(s,gname,Job.TOADMIN,m_queue);
					}
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("join")){
					System.out.print("Group Name: ");
					String gname = user.readLine();
					addMsg(nickname,gname,Job.JOIN,m_queue);
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("requests")){
					System.out.print("Group Name: ");
					String gname = user.readLine();
					addMsg(nickname,gname,Job.REQUESTS,m_queue);
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("showGroups")){
					
					addMsg(nickname,"",Job.SHOWGROUPS,m_queue);
					sendMsgQ(server,m_queue);
				}else if(recipient.equals("help")){
					for(String s:commands){
						System.out.println(s);
					}
				}
				
				m_queue = new MessageQueue(); //reinitialising the queue , if i don't do this 
				//sending trough the socket doesn't work as expected
			}
		} catch (IOException e) {
			Report.errorAndGiveUp("Communication broke in ClientSender"
					+ e.getMessage());
		}
	}



	public synchronized static void addMsg(String nickname, String text, Job job,
			MessageQueue m_queue) { //adding a message to a queue
		Message msg = new Message(nickname, text, job);
		m_queue.offer(msg);

	}

	public synchronized static void sendMsgQ(ObjectOutputStream stream, MessageQueue m) {
		try {//sending a queue trough a stream(socket)
			stream.writeObject(m);
			stream.flush();
		} catch (IOException e) {
				Report.errorAndGiveUp("Something went wrong :" + e.getMessage());
		}
	}
}
