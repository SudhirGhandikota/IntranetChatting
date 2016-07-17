import java.awt.*;
import java.net.*;
import java.io.*;


public class Own
    extends Object
    implements Runnable
{
    public static final String VERSION = "4.01";

    public static String usernameParam     = "-username";
    public static String passwordParam     = "-password";
    public static String servernameParam   = "-servername";
    public static String portnumberParam   = "-portnumber";
    public static String nopasswordsParam  = "-nopasswords";
    public static String autoconnectParam  = "-autoconnect";
 

    private OwnWindow window;
    private URL myURL = null;
    private String name = "";
    private String password = "";
    private String host = "";
    private String port = "";
    private boolean requirePasswords = true;
    private boolean autoConnect = false;


    private void usage()
    {
	System.out.println("\nIntranet Chatting usage:");
	System.out.println("follow commands\n");
	return;
    }

    private boolean parseArgs(String[] args)
    {
	// Loop through any command line arguments
	for (int count = 0; count < args.length; count ++)
	    {
		if (args[count].equals(usernameParam))
		    {
			if (++count < args.length)
			    name = args[count];
		    }

		else if (args[count].equals(passwordParam))
		    {
			if (++count < args.length)
			    password = args[count];
		    }

		else if (args[count].equals(servernameParam))
		    {
			if (++count < args.length)
			    host = args[count];
		    }

		else if (args[count].equals(portnumberParam))
		    {
			if (++count < args.length)
			    port = args[count];
		    }

		else if (args[count].equals(nopasswordsParam))
		    requirePasswords = false;

		else if (args[count].equals(autoconnectParam))
		    autoConnect = true;

		else if (args[count].equals("-help"))
		    {
			usage();
			return (false);
		    }

		else
		    {
			System.out.println("\nOrbit: unknown argument "
					   + args[count]);
			System.out.println("Type 'java Orbit -help' for "
					   + "usage information");
			return (false);
		    }
	    }

	return (true);
    }

    public static void main(String[] args)
    {
	Own firstinstance = new Own(args);
	firstinstance.run();
	return;
    }

    public Own(String[] args)
    {
	// Get a URL to describe the invocation directory
	try {
	    myURL = new URL("file", "localhost", "./");
	}
	catch (Exception E) {
	    System.out.println(E);
	    System.exit(1);
	}
	
	// Parse our args.  Only continue if successful
	if (!parseArgs(args))
	    System.exit(1);

	// If "username" is blank, that's OK.  However, if the server and/or
	// port are blank, we'll supply some default ones here
	if ((host == null) || host.equals(""))
	    host = "localhost";
	if ((port == null) || port.equals(""))
	    port = "12468";

	// Open the window
	window = new OwnWindow(name, password, host, port,
				   myURL);

	// Set the window width and height, if applicable
	

	// Should the window prompt users for passwords automatically?
	window.requirePassword = requirePasswords;

	

	// Show the window
	window.show();

	// Are we supposed to attempt an automatic connection?
	if (autoConnect)
	    window.connect();

	
	return;
    }

    public void run()
    {
	System.out.println("last");
	return;
    }
}
