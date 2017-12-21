import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class User {//user class,, used to store a user

	private String name;
	private String password;

	public User(String name, String password) { //initilising a user with a name and password
		this.name = name;
		this.password = password;
	}
	//i found the crypting algorithm on stack overflow
	public static String crypt(String s) { //method shared between users to crypt a string (used for password)
		try {
			MessageDigest md = MessageDigest.getInstance("MD5"); //choosing the crypting algorithm
			byte[] passBytes = s.getBytes(); //converting the string into bytes
			md.reset();
			//digesting the bytes (crypting the string)
			byte[] digested = md.digest(passBytes);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < digested.length; i++) {
				sb.append(Integer.toHexString(0xff & digested[i])); 
			}
			return sb.toString();//convergint the bytes to the string
		} catch (NoSuchAlgorithmException ex) {
			
		}
		return null;
	}
	public String getName(){//getting the name
		return name;
	}
	public String getPassword(){//getting the password
		return password;
	}
	public String toString(){//converting the user to a string "name|pass"
		return name + "|" + password;
	}
	 @Override //equals method so that we can make an arraylist of type user, and can compare
	    public boolean equals(Object object) 
	    {
	        boolean sameSame = false;

	        if (object != null && object instanceof User)
	        {
	            sameSame = this.name.equals(((User) object).getName()) ;
	           
	        }

	        return sameSame;
	    }
	 
}
