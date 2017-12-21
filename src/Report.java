import java.io.*;

// This is to handle logging of normal and erroneous behaviours.

public class Report {
  //syncronised the methods after i added the flush
  //because i had a bug where messages printed by System.out were out 
  //of sync with the messages printed by System.err
  public static synchronized void behaviour(String message) {
    System.err.println(message);
    System.err.flush();
  }

  public static synchronized void error(String message) {
    System.err.println(message);
    System.err.flush();

  }

  public static synchronized void errorAndGiveUp(String message) {
    Report.error(message);
    System.exit(1);
  }
}
