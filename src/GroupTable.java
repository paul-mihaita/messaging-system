import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;

public class GroupTable {
//a class using a concurrent map having group names as keys and groups as values
	
	private ConcurrentMap<String, Group> queueTable = new ConcurrentHashMap<String, Group>();

	public void add(String name,String type,User u) {
		if(type.equals("secret"))
			queueTable.put(name, new Group(true,u,name));
		if(type.equals("public"))
			queueTable.put(name, new Group(false,u,name));
	}
	
	public void remove(String name) { 
		queueTable.remove(name, queueTable.get(name));
	}
	
	public boolean isEmpty(){
		return queueTable.isEmpty();
	}
	
	public Group getGroup(String name) { // getting the Group using it's name
		return queueTable.get(name);
	}
	public boolean isMember(String name) { //checking if a group is member in the queuetable
		return queueTable.containsKey(name);
	}
	public ArrayList<Group> getGroups(){
		ArrayList<Group> g= new ArrayList<>();
		Iterator<String> it = queueTable.keySet().iterator();
		while (it.hasNext()) {
			String aux = it.next();
			if(!queueTable.get(aux).getSecret())
				g.add(queueTable.get(aux));
		}
		return g;
	}
	public ArrayList<Group> getAllGroups(){
		ArrayList<Group> g= new ArrayList<>();
		Iterator<String> it = queueTable.keySet().iterator();
		while (it.hasNext()) {
			String aux = it.next();
			g.add(queueTable.get(aux));
		}
		return g;
	}
}