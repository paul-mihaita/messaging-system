import java.net.*;
import java.util.ArrayList;
import java.io.*;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
	private String myClientsName;
	private ObjectInputStream myClient;
	private ClientTable clientTable;
	private boolean m_run;
	private final String EXIT_MSG_SERVER = new String(" closed the chat.");
	private MessageQueue m_queue;
	private ClientTable registeredUsers;
	private GroupTable groupTable;


	public ServerReceiver(String n, ObjectInputStream c, ClientTable t, ClientTable registeredUsers, GroupTable groupTable) {
		myClientsName = n;
		myClient = c;
		clientTable = t;
		m_run = true;
		m_queue = new MessageQueue();
		this.registeredUsers = registeredUsers;
		this.groupTable = groupTable;
	}
	
	public void run() {
		try {
			while (m_run) {
				//reading the queue from the client sender
				m_queue = (MessageQueue) myClient.readObject();
				while (!m_queue.isEmpty()) {
					//decomposing the message
					MessageQueue recipientsQueue;
					Message msg = m_queue.take();
					Job job = msg.getJob();
					String recipient = msg.getSender();
					String text = msg.getText();
					msg = new Message(myClientsName,text,job);

					if(job.equals(Job.LOGOUT)||job.equals(Job.QUIT)){//doing what needs to be done for quitting
						m_run = false;
						Report.behaviour(recipient + EXIT_MSG_SERVER);//writing on the server 
						recipientsQueue = clientTable.getQueue(recipient);
						clientTable.remove(recipient);//removing the client from the active clients table
						if (recipientsQueue != null){
							recipientsQueue.offer(msg);//adding the message to the queue(my clients queue)to be read in server sender
						}
						else
							Report.error("Message for user " + recipient
									+ " wich is not logged in " + ": " + text);
							
					}
					else if(job.equals(Job.MESSAGE)){//doing what needs to be done for sending a message
						recipientsQueue = clientTable.getQueue(recipient); //getting the recipients queue
						MessageQueue userQueue = registeredUsers.getQueue(recipient);
						if (recipientsQueue != null && userQueue != null){
							recipientsQueue.offer(msg); //adding the message to the recipients queue to be read in server sender
						}
						else
							if(userQueue != null){ //if the user is registered but not logged in, we save the message
							Report.error("Message for user " + recipient
									+ " will be received when logged in" + ": " + text);
							recipientsQueue = registeredUsers.getQueue(recipient); //adding the message to the users queue,
							// in the registered users table
							recipientsQueue.offer(msg);
							}
							else{
								Report.error("Message for user " + recipient
										+ " wich is not registered  " + ": " + text );
							}
					}else if(job.equals(Job.CREATEGROUP)){ //creating the group
						if(text.equals("secret") || text.equals("public")){
							if(!groupTable.isMember(recipient)){ //if the group is not existent
								groupTable.add(recipient,text,clientTable.getUser(myClientsName));//creating the group
								recipientsQueue = clientTable.getQueue(myClientsName); 
								Message msgg = new Message(recipient,text,job); //sending the message to the client, "group registered"
								recipientsQueue.offer(msgg);
							}else{ //else send the message already registered group
								recipientsQueue = clientTable.getQueue(myClientsName);
								Message msgg = new Message(recipient," already registered " ,Job.CREATEGROUPFAILED);
								recipientsQueue.offer(msgg);
							}
						}else{//else message that group cand only be public or secret
							recipientsQueue = clientTable.getQueue(myClientsName);
							Message msgg = new Message(recipient,"failed to be created, type can only be secret/public",Job.CREATEGROUPFAILED);
							recipientsQueue.offer(msgg);
						}
					}else if(job.equals(Job.ADDMEMBER)||job.equals(Job.ADDADMIN)){ //adding a member or admin
						if(groupTable.isMember(text)){ //if the group exists
							if(groupTable.getGroup(text).isAdmin(registeredUsers.getUser(myClientsName))){ //if the client adding is admin in the group
								if(registeredUsers.isMember(recipient)){//if the user added is registered
									if(!groupTable.getGroup(text).isMember(registeredUsers.getUser(recipient)) && !groupTable.getGroup(text).isAdmin(registeredUsers.getUser(recipient))){
										if(clientTable.isMember(recipient)){ //if the user is logged in 
											recipientsQueue = clientTable.getQueue(recipient);
											Message msgg = new Message(recipient,"You were added to the group " + text,job);
											recipientsQueue.offer(msgg);//sending the message
											recipientsQueue = clientTable.getQueue(myClientsName);
											msgg = new Message(recipient,"User " + recipient+" Added successfully to the group" + " "+text,Job.MEMBERADDED);
											recipientsQueue.offer(msgg);//writin in the client that the user was added successfully
											if(job.equals(Job.ADDMEMBER))
												groupTable.getGroup(text).addMember(clientTable.getUser(recipient));
											else
												groupTable.getGroup(text).addAdmin(clientTable.getUser(recipient));

										}else{ //same as above, but for users wich are offline
											recipientsQueue = registeredUsers.getQueue(recipient);
											Message msgg = new Message(recipient,"You were added to the group " + text,job);
											recipientsQueue.offer(msgg);
											recipientsQueue = clientTable.getQueue(myClientsName);
											msgg = new Message(recipient,"User " + recipient+" Added successfully to the group" + " "+text,Job.MEMBERADDED);
											recipientsQueue.offer(msgg);
											if(job.equals(Job.ADDMEMBER))
												groupTable.getGroup(text).addMember(registeredUsers.getUser(recipient));
											else
												groupTable.getGroup(text).addAdmin(registeredUsers.getUser(recipient));
										}
									}else{
										recipientsQueue = clientTable.getQueue(myClientsName);
										Message msgg = new Message(recipient,"user already in the group ",Job.MEMBERNOTADDED);
										recipientsQueue.offer(msgg);
									}
								}else{
									recipientsQueue = clientTable.getQueue(myClientsName);
									Message msgg = new Message(recipient,"user not registered ",Job.MEMBERNOTADDED);
									recipientsQueue.offer(msgg);
								}
							}else{
								recipientsQueue = clientTable.getQueue(myClientsName);
								Message msgg = new Message(recipient,"only admins can add users ",Job.MEMBERNOTADDED);
								recipientsQueue.offer(msgg);
							}
						}
						else{
							recipientsQueue = clientTable.getQueue(myClientsName);
							Message msgg = new Message(recipient,"group not registered ",Job.MEMBERNOTADDED);
							recipientsQueue.offer(msgg);
						}
					}else if (job.equals(Job.REMOVEMEMBER) ){//removing a member
						if(groupTable.isMember(text)){
							Group aux = groupTable.getGroup(text);
							User me = registeredUsers.getUser(recipient);
							if(registeredUsers.isMember(recipient)){ //if the member removed exists
								if(!recipient.equals(myClientsName)){ //if the client is not removing himself
									if((aux.isAdmin(me) || aux.isMember(me))&&aux.isAdmin(registeredUsers.getUser(myClientsName))){
										//if the user is member in the group and the client is admin
										aux.removeUser(me.getName()); //removing
										if(aux.getAdmins().isEmpty()){ //if there are no more admins, removing the group
											groupTable.remove(text);
											ArrayList<User> members = aux.getMembers();
											for(User u:members){
												if(clientTable.isMember(u)){
													recipientsQueue = clientTable.getQueue(recipient);
													Message msgg = new Message("","you were removed from group.No admins left " + text,Job.REMOVEMEMBER);
													recipientsQueue.offer(msgg);
												}else{
													recipientsQueue = registeredUsers.getQueue(recipient);
													Message msgg = new Message("","you were removed from group.No admins left " + text,Job.REMOVEMEMBER);
													recipientsQueue.offer(msgg);
												}
											}
										}
										if(clientTable.isMember(me.getName())){ //sending message to the user that he is removed, if he is online
											recipientsQueue = clientTable.getQueue(me.getName());
											Message msgg = new Message("","you were removed from group " + text,Job.REMOVEMEMBER);
											recipientsQueue.offer(msgg);
											recipientsQueue = clientTable.getQueue(myClientsName);
											msgg = new Message(recipient,"removed from group " + text,Job.REMOVEMEMBER);
											recipientsQueue.offer(msgg);
										}else if(registeredUsers.isMember(me.getName())){//sending the message to the user, if he is offline
											recipientsQueue = registeredUsers.getQueue(me.getName());
											Message msgg = new Message("","you were removed from group " + text,Job.REMOVEMEMBER);
											recipientsQueue.offer(msgg);
											recipientsQueue = clientTable.getQueue(myClientsName);
											msgg = new Message(recipient,recipient + " removed from group " + text,Job.REMOVEMEMBER);
											recipientsQueue.offer(msgg);
										}else{//sending a message to the client if the user he tried to remove is not registered
											recipientsQueue = clientTable.getQueue(myClientsName);
											Message msgg = new Message("","User "+me.getName()+" not registered ",Job.REMOVEMEMBER);
											recipientsQueue.offer(msgg);
										}
									}
								}else{//if the clients wants to remove himself
									aux.removeUser(me.getName());
									if(aux.getAdmins().isEmpty()){
										groupTable.remove(text);
									}
									recipientsQueue = clientTable.getQueue(me.getName());
									Message msgg = new Message("","you were removed from group " + text,Job.REMOVEMEMBER);
									recipientsQueue.offer(msgg);
								}
							}
							else{//if the user is not registered
								recipientsQueue = clientTable.getQueue(myClientsName);
								Message msgg = new Message("","User " + recipient + " not registered",Job.REMOVEMEMBER);
								recipientsQueue.offer(msgg);
							}
						}else{//if the group is not registered
							recipientsQueue = clientTable.getQueue(myClientsName);
							Message msgg = new Message("","Group " + text + " not registered",Job.REMOVEMEMBER);
							recipientsQueue.offer(msgg);
						}
					}else if (job.equals(Job.TOADMIN)){ //changing users from group to admins
						if(groupTable.isMember(text)){//if the group exists
							Group aux = groupTable.getGroup(text);
							if(aux.isAdmin(clientTable.getUser(myClientsName))){//if the client is admin in the group
								User me = registeredUsers.getUser(recipient);
								if(aux.isMember(me)){ //removing and adding to the group
									synchronized(me){
										aux.removeUser(me.getName());
									
									aux.addAdmin(me);
									}
									if(clientTable.isMember(me.getName())){ //if the user changed is logged in
										recipientsQueue = clientTable.getQueue(me.getName());
										Message msgg = new Message("","you are now admin in group " + text,Job.TOADMIN);
										recipientsQueue.offer(msgg);
										recipientsQueue = clientTable.getQueue(myClientsName);
										msgg = new Message("",me.getName() + " is now admin in group " + text,Job.TOADMIN);
										recipientsQueue.offer(msgg);
									}else if(registeredUsers.isMember(me.getName())){//if the user changed is logged out
										recipientsQueue = registeredUsers.getQueue(me.getName());
										Message msgg = new Message("","you are now admin in group " + text,Job.TOADMIN);
										recipientsQueue.offer(msgg);
										recipientsQueue = clientTable.getQueue(myClientsName);
										msgg = new Message("",me.getName() + " is now admin in group " + text,Job.TOADMIN);
										recipientsQueue.offer(msgg);
									}
								}else{
									if(me != null){//if the member i am trying to change is not in the group
										recipientsQueue = clientTable.getQueue(myClientsName);
										Message msgg = new Message("",me.getName() + " is not member in group " + text,Job.TOADMIN);
										recipientsQueue.offer(msgg);
									}else{//if the group is not registered
										recipientsQueue = clientTable.getQueue(myClientsName);
										Message msgg = new Message("",recipient + " is not registered",Job.TOADMIN);
										recipientsQueue.offer(msgg);
									}
								}
							}else{//if the client is not admin
								recipientsQueue = clientTable.getQueue(myClientsName);
								Message msgg = new Message("","You are not admin.User "+ recipient +" not changed to admin",Job.TOADMIN);
								recipientsQueue.offer(msgg);
							}
						}else{//if the group is not registered
							recipientsQueue = clientTable.getQueue(myClientsName);
							Message msgg = new Message("","Group inexistent.User "+ recipient +" not changed to admin",Job.TOADMIN);
							recipientsQueue.offer(msgg);
						}
					}else if(job.equals(Job.JOIN)){//joining a group
						MessageQueue m_queue;
						if(groupTable.isMember(text)){
							Group group = groupTable.getGroup(text);
							User user = clientTable.getUser(recipient);
							if(!group.isMember(user) && !group.isAdmin(user)){//if the group exists and the user is not member
								if(!group.getSecret()){
									group.addRequest(user);
									m_queue = clientTable.getQueue(user);
									Message msgg = new Message("","Request sent.",Job.MEMBERADDED);
									m_queue.offer(msgg);
									ArrayList <User> admins = group.getAdmins();
									for(User u: admins){
										if(clientTable.isMember(u.getName())){
											m_queue = clientTable.getQueue(u.getName());
											msgg = new Message("","New join request for group " + text+".Command \"requests\" to see a list of requests",Job.MEMBERADDED);
											m_queue.offer(msgg);
										}else{
											m_queue = registeredUsers.getQueue(u.getName());
											msgg = new Message("","New join request for group " + text+".Command \"requests\" to see a list of requests",Job.MEMBERADDED);
											m_queue.offer(msgg);
										}
									}
								}else{//if the group is secret a user cannot join it, it has to be added by an admin
									m_queue = clientTable.getQueue(recipient);
									Message gg = new Message("","You cannot join group "+ text,Job.MEMBERADDED);
									m_queue.offer(gg);
								}
							}else{
								m_queue = clientTable.getQueue(recipient);
								Message gg = new Message("","You are already member in group "+ text,Job.MEMBERADDED);
								m_queue.offer(gg);
							}
						}else{
							m_queue = clientTable.getQueue(recipient);
							Message dd = new Message("","Group "+text+" not registered", Job.MEMBERADDED);
							m_queue.offer(dd);
						}
					}else if(job.equals(Job.REQUESTS)){ //showing request in the client
						MessageQueue m_queue = clientTable.getQueue(myClientsName);
						ArrayList<User> r = groupTable.getGroup(text).getRequests();
						if(r.isEmpty()){
							m_queue.offer(new Message("","no requests for group "+ text,Job.REQUESTS));
						}
						for(User u: r){
							m_queue.offer(new Message(u.getName(),"wants to join group "+ text,Job.REQUESTS));
						}
						
					}else if(job.equals(Job.SHOWGROUPS)){//showing public groups in the client
						MessageQueue m_queue = clientTable.getQueue(myClientsName);
						ArrayList<Group> g = groupTable.getGroups();
						if(g.isEmpty()){
							m_queue.offer(new Message("","There are no groups registered "+ text,Job.MESSAGEGROUPFAILED));
						}
						for(Group u: g){
							m_queue.offer(new Message("",u.getName(),Job.MESSAGEGROUPFAILED));
						}
						
					}else if(job.equals(Job.MESSAGEGROUP) ){//doing what needs to be done for sending a message
						if(groupTable.isMember(recipient)){
							Group aux = groupTable.getGroup(recipient);
							if(aux.isAdmin(registeredUsers.getUser(myClientsName))|| aux.isMember(registeredUsers.getUser(myClientsName))){
								ArrayList<User> members = aux.getMembers();
								ArrayList<User> admins = aux.getAdmins();
								for(User u: admins){
									if(!u.getName().equals(myClientsName))
										members.add(u);
								}
								msg = new Message(myClientsName,text,Job.MESSAGE);
								for(User u: members){
									recipient = u.getName();
									if(!recipient.equals(myClientsName)){
									recipientsQueue = clientTable.getQueue(recipient); //getting the recipients queue
									MessageQueue userQueue = registeredUsers.getQueue(recipient);
									if (recipientsQueue != null && userQueue != null){
										recipientsQueue.offer(msg); //adding the message to the recipients queue to be read in server sender
									}
									else
										if(userQueue != null){ //if the user is registered but not logged in, we save the message
										Report.error("Message for user " + recipient
												+ " will be received when logged in" + ": " + text);
										recipientsQueue = registeredUsers.getQueue(recipient); //adding the message to the users queue,
										// in the registered users table
										recipientsQueue.offer(msg);
										}
										else{
											Report.error("Message for user " + recipient
													+ " wich is not registered  " + ": " + text );
										}
									}
								}
							
							}else{
								recipientsQueue = clientTable.getQueue(myClientsName); 
								recipientsQueue.offer(new Message("","You are not member of group " + recipient,Job.MESSAGEGROUPFAILED));
							}
						}else{
							recipientsQueue = clientTable.getQueue(myClientsName);
							Message msgg = new Message("","Group " + recipient + " not existent",Job.MESSAGEGROUPFAILED);
							recipientsQueue.offer(msgg);
						}
					}
					
				}
				m_queue = new MessageQueue();
			}

		} catch (IOException e) {
			if(e.getMessage() == null){//if we get here and the message is null, we need to close the server
				Report.behaviour("Logged out");
				MessageQueue recipientsQueue = clientTable.getQueue(myClientsName);
				clientTable.remove(myClientsName);
				if (recipientsQueue != null){
					recipientsQueue.offer(new Message("","",Job.QUIT));
				}
			}
			else{
				Report.error("Something went wrong with the client "
					+ myClientsName + " " + e.getMessage());
			// No point in trying to close sockets. Just give up.
			// We end this thread (we don't do System.exit(1)).
			}
		} catch (ClassNotFoundException e) {

			Report.errorAndGiveUp("Something went wrong :"+e.getMessage());
		}
	}
}
