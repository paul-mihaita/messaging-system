// Each nickname has a different incomming-message queue.

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;

public class ClientTable {

	private ConcurrentMap<User, MessageQueue> queueTable = new ConcurrentHashMap<User, MessageQueue>();

	// The following overrides any previously existing nickname, and
	// hence the last client to use this nickname will get the messages
	// for that nickname, and the previously exisiting clients with that
	// nickname won't be able to get messages. Obviously, this is not a
	// good design of a messaging system. So I don't get full marks:

	public void add(User user) {
		queueTable.put(user, new MessageQueue());
	}
	public void add(User user,MessageQueue queue) {
		queueTable.put(user, queue);
	}
	public void remove(User user) { // added remove method , to remove the
									// client from the client table
		queueTable.remove(user, queueTable.get(user));
	}

	public void remove(String name) { 
		Iterator<User> it = queueTable.keySet().iterator();
		while (it.hasNext()) {
			User user = it.next();
			if (user.getName().equals(name)) {
				queueTable.remove(user);
			}
		}
	}
	public boolean isEmpty(){ //if the queue is empty
		return queueTable.isEmpty();
	}
	
	public MessageQueue getQueue(User user) { // getting the queueue  using a user
		return queueTable.get(user);
	}
	public User getUser(String name){ //getting a user using it's name
		Iterator<User> it = queueTable.keySet().iterator();
		while (it.hasNext()) {
			User user = it.next();
			if (user.getName().equals(name)) {
				return user;
			}
		}
		return null;
	}
	public boolean isMember(String name){
		if(getUser(name) != null)
			return true;
		return false;
	}
	public MessageQueue getQueue(String name) { //getting a queue using a user name
		Iterator<User> it = queueTable.keySet().iterator();
		while (it.hasNext()) {
			User user = it.next();
			if (user.getName().equals(name)) {
				return queueTable.get(user);
			}
		}
		return null;
	}

	public boolean isMember(User user) { //checking if a user is member in the queuetable
		Iterator<User> it = queueTable.keySet().iterator();
		while (it.hasNext()) {
			User aux = it.next();
			if (aux.getName().equals(user.getName())) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<User> getAllUsers() { //getting all users from the table, returning an array list
		Iterator<User> it = queueTable.keySet().iterator();
		ArrayList<User> auxL = new ArrayList<>();
		while (it.hasNext()) {
			User aux = it.next();
			auxL.add(aux);
		}
		return auxL;
	}
}
