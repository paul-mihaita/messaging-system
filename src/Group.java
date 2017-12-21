import java.util.ArrayList;

public class Group {// class containing data for a group with methods needed
	private ArrayList<User> admins;
	private ArrayList<User> members;
	private ArrayList<User> requests;
	private boolean secret;
	private String name="";
	public Group() {
		secret = true;
		admins = new ArrayList<>();
		members = new ArrayList<>();
		requests = new ArrayList<>();
	
	}

	public Group(String n ,boolean s, ArrayList<User> a, ArrayList<User> m) {
		secret = s;
		admins = a;
		members = m;
		requests = new ArrayList<>();
		name = n;
	}

	public Group(String n,boolean s) {
		secret = s;
		admins = new ArrayList<>();
		members = new ArrayList<>();
		requests = new ArrayList<>();
		name = n;
	}

	public Group(boolean s, User u,String name) {
		secret = s;
		admins = new ArrayList<>();
		admins.add(u);
		members = new ArrayList<>();
		requests = new ArrayList<>();
		this.name = name;

	}
	public String getName(){
		return name;
	}
	public void setName(String s){
		name = s;
	}
	public ArrayList<User> getRequests(){
		return requests;
	}
	public void addRequest(User u){
		requests.add(u);
	}
	public void addMember(User u) {
		members.add(u);
		if(requests.contains(u))
			requests.remove(u);
	}

	public void addAdmin(User u) {
		admins.add(u);
		if(requests.contains(u))
			requests.remove(u);
	}

	public void removeUser(String name) {
		ArrayList<User> toRemove = new ArrayList<>();
		if (!members.isEmpty()) {
			for (User u : members) {
				if (u.getName().equals(name))
					toRemove.add(u);
			}
		}
		members.removeAll(toRemove);
		toRemove = new ArrayList<>();
		if (!admins.isEmpty()) {
			for (User u : admins) {
				if (u.getName().equals(name))
					toRemove.add(u);
			}
		}
		admins.removeAll(toRemove);
	}

	public boolean isAdmin(User u) {
		return admins.contains(u);
	}

	public boolean isMember(User u) {
		return members.contains(u);
	}

	public boolean getSecret() {
		return secret;

	}

	public void setSecret(boolean s) {
		secret = s;
	}

	public ArrayList<User> getAdmins() {
		return admins;

	}

	public void setAdmins(ArrayList<User> s) {
		admins = s;
	}

	public ArrayList<User> getMembers() {
		return members;

	}

	public void setMembers(ArrayList<User> s) {
		members = s;
	}
}
