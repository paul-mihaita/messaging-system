import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class FileHandler {

	public BufferedReader bufferedReader;
	public BufferedWriter bufferedWriter;
	private String path;
	private File file;

	public FileHandler(String path) { //initialising the files
		this.path = path;
		this.file = new File(path);
	}

	public BufferedReader initFileRead() {//initilising the reading buffer
		bufferedReader = null;
		try {
			FileReader fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
		} catch (IOException e) {
			Report.errorAndGiveUp("IO error " + e.getMessage());

		}
		return bufferedReader;

	}

	public BufferedWriter initFileWrite() {//initilising the writing buffer
		bufferedWriter = null;
		try {
			FileWriter fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
		} catch (IOException e) {
			Report.errorAndGiveUp("IO error " + e.getMessage());

		}
		return bufferedWriter;
	}

	public synchronized void write(String s) { //writing a string to the file 
		try {
			bufferedWriter.write(s + '\n');
			bufferedWriter.flush();
		} catch (IOException e) {
			Report.errorAndGiveUp("IO error " + e.getMessage());
		}
	}

	public synchronized void remove(String tokenToDelete) { //removing a token from a file
		//by overwriting the whole document to an aux doc, deliting the original and renaming the aux to the original
		//( i don;t use this methon anymore but i used in another version of this implementation)
		try {
			bufferedReader = initFileRead();
			int index = path.indexOf('.');
			String p = path.substring(0, index);
			File tempFile = new File(p + "copy.txt"); //creating a new file named by the old one + "copy"
			String name = "";
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			while((name = bufferedReader.readLine())!= null){ //writing to the aux file all the lines which are not the token 
				if(!name.equals(tokenToDelete))
					writer.write(name+'\n');
			}
			//closing streams
			writer.close();
			bufferedReader.close();
			tempFile.renameTo(file);
			file = tempFile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Report.errorAndGiveUp("IO error " + e.getMessage());
		}
	}

	public synchronized ArrayList<User> read() {//reading the users from a file containing just users : name|pass
		bufferedReader = initFileRead();
		ArrayList<User> list = new ArrayList<>();
		
		try {
			String s = "";
			s = bufferedReader.readLine();
			while (s != null) {
				int index = s.indexOf('|');
				String name = s.substring(0,index);
				String pass = s.substring(index+1,s.length());//decomposing the line into name and pass
				//from "name|pass"
				list.add(new User(name,pass));
				s = bufferedReader.readLine();
			}
			//closing the stream
			bufferedReader.close();
			
		} catch (IOException e) {
			Report.errorAndGiveUp("IO" + e.getMessage());
		}
		return list;
	}
	public synchronized ClientTable  readMessages(ClientTable table) { //readint the users and the messages from the file
		//containing the user and it's messages to be revceived
		bufferedReader = initFileRead();
		try {
			String s = "";
			s = bufferedReader.readLine(); //reading a line containing the user and it's messages to be received
			while (s != null) {
				String[] strings = s.split("\\|"); // separating the tokens, separated by '|', first 2 tokens
				//are username and pass, rest are messages
				User user = new User(strings[0],strings[1]);
				table.add(user);
				int i = 2;
				MessageQueue q = table.getQueue(user);//getting the queue of the user
				while( i <strings.length){
					//decomposing the messages of format "sender: message" into separate sender and message strings
					int j = strings[i].indexOf(':');
					String name = strings[i].substring(0,j);
					String text = strings[i].substring(j+1);
					q.offer(new Message(name,text,Job.MESSAGE)); //adding the message to the queue of the user
					i++;
				}
				s = bufferedReader.readLine();//reading a new line from the file

			}
			//closing the stream;
			bufferedReader.close();
			//returning the table
			return table;
			
		} catch (IOException e) {
			Report.errorAndGiveUp("IO" + e.getMessage());
		}
		//if the program gets here, no text was in the file
		table = null;
		return table;
	}

	public synchronized GroupTable readMessages(GroupTable table,ClientTable t) {
		
		bufferedReader = initFileRead();
		try {
			String s = "";
			s = bufferedReader.readLine(); //reading a line containing the user and it's messages to be received
			while (s != null) {
				String[] strings = s.split("\\|");
				table.add(strings[1], strings[2], t.getUser(strings[3]));
				Group g = table.getGroup(strings[1]);
				int i;
				int nrAdmins = Integer.parseInt(strings[0]);
				nrAdmins+=3;
				for(i =4;i<nrAdmins;i++){
					g.addAdmin(t.getUser(strings[i]));

				}
				for(; i < strings.length ; i++){
					g.addMember(t.getUser(strings[i]));
				}
				s = bufferedReader.readLine();//reading a new line from the file

			}
			//closing the stream;
			bufferedReader.close();
			//returning the table
			return table;
			
		} catch (IOException e) {
			Report.errorAndGiveUp("IO" + e.getMessage());
		}
		//if the program gets here, no text was in the file
		table = null;
		return table;
	}
}
