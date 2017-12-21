import java.io.Serializable;

public class Message implements Serializable{//the message class
	//implements serializable so that i can send it trough the socket

	private final String sender;
	private final String text;
	private Job job;

	Message(String sender, String text,Job job) { //initialising the message with the sender, text, and it's job
		this.sender = sender;
		this.text = text;
		this.job = job;
	}
	public String getSender() {//get the sender
		return sender;
	}

	public String getText() {//get the text
		return text;
	}
	public Job getJob(){//get the job
		return job;
	}
	public String toString() {//converting the message to a string "sender:text"
		return sender + ": " + text;
	}
}
