// Usage:
//        java Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Server {

	public static FileHandler initAndReadFiles(ClientTable registeredUsers) {
		//Initialising file handling and reading from file, the users and possible messages not sent for them
		String path = System.getProperty("user.dir");
		path += "/files/users.txt";
		FileHandler userFile = new FileHandler(path);

		userFile.initFileRead();
		registeredUsers = userFile.readMessages(registeredUsers);
		return userFile;

	}
	public static FileHandler initAndReadFiles(GroupTable  groups,ClientTable registeredUsers) {
		//Initialising file handling and reading from file, the users and possible messages not sent for them
		String path = System.getProperty("user.dir");
		path += "/files/groups.txt";
		FileHandler userFile = new FileHandler(path);

		userFile.initFileRead();
		groups = userFile.readMessages(groups,registeredUsers);
		return userFile;

	}
	public static void main(String[] args) {
		//Creating tables , queue and file handling objects
		FileHandler userFile ;
		FileHandler groupFile ;
		ClientTable registeredUsers = new ClientTable();
		ClientTable clientTable = new ClientTable();
		GroupTable groupTable = new GroupTable();
		MessageQueue m_queue = new MessageQueue();
		//reading from the user file
		userFile = initAndReadFiles( registeredUsers);
		groupFile = initAndReadFiles( groupTable,registeredUsers);
		ServerSocket serverSocket = null;
		boolean m_run = true;
		//adding the shutdown hook for the server
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				//cleaning up , writing to files and writing on the screen that the server has been closed
				Server.cleanup(m_run,groupTable ,registeredUsers, clientTable, userFile,groupFile);
				Report.behaviour("Server closed");
			}
		});
		
		//trying to open a new socket 
		try {
			serverSocket = new ServerSocket(Port.number);
		} catch (IOException e) {
			Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
		}

		try {
			// We loop for ever, as servers usually do.

			while (m_run) {
				// Listen to the socket, accepting connections from new clients:
				Socket socket = serverSocket.accept(); 

				//this is so we can send objects trough the socket
				ObjectOutputStream toClient = new ObjectOutputStream(
						socket.getOutputStream());
				
				ObjectInputStream fromClient = new ObjectInputStream(
						socket.getInputStream());
				//Creating and starting the thread which handles requests from the client
				ServerLogic requestHandler = new ServerLogic(toClient,
						fromClient, clientTable, registeredUsers,groupTable);
				requestHandler.start();

			}
		} catch (IOException e) {
			// Lazy approach:
			Report.error("IO error " + e.getMessage());
			// A more sophisticated approach could try to establish a new
			// connection. But this is beyond this simple exercise.
		}
	}


	private static void cleanup(boolean m_run, GroupTable groupTable, ClientTable registeredUsers,ClientTable loggedUsers, FileHandler userFile, FileHandler groupFile) {
		userFile.initFileWrite();
		groupFile.initFileWrite();
		ArrayList<User> U = new ArrayList<>();
		ArrayList<Group> G = new ArrayList<>();
		ArrayList<User> stillActive = new ArrayList<>();
		//getting the users list from the client table, users that might still be active
		stillActive = loggedUsers.getAllUsers();
		for (User i : stillActive) {
			//transfering unsent messages from the active users table to the registered users table
			//so that we write in the file 
			MessageQueue from = loggedUsers.getQueue(i);
			MessageQueue to = registeredUsers.getQueue(i);
			while(!from.isEmpty()){
				to.offer(from.take());
			}
		}
		U = registeredUsers.getAllUsers();
		//getting all registered users
		for (User i : U) {
			//writing the user in the file (name + pass)
			//if the user has messages left, we write them in the file too
			if (!registeredUsers.getQueue(i).isEmpty())
				userFile.write(i.toString()
						+ registeredUsers.getQueue(i).toString());
			else userFile.write(i.toString());
		}
		G = groupTable.getAllGroups();
		for(Group g : G){
			String s = "";
			s+=g.getAdmins().size()+"|";
			s+=g.getName()+"|";
			if(g.getSecret()){
				s+="secret|";
			}else
				s+="public|";
			ArrayList<User> admins = g.getAdmins();
			ArrayList<User> members = g.getMembers();
			for(User u: admins){
				s+=u.getName()+"|";
			}
			for(User u: members){
				s+=u.getName()+"|";
			}
			s = s.substring(0, s.length() - 1);
			groupFile.write(s);
		}
		try {
			//closing the file stream
			userFile.bufferedWriter.close();
			groupFile.bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
