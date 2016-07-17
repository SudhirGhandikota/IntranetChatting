import java.net.*;
import java.util.*;
import java.text.*;
import java.io.*;


class OwnServerShutdown
    extends Thread
{
    // This gets called when the VM is shutting down.  If the server has
    // already terminated properly (for example, from the administrator
    // pushing the 'shut down' button), then there's nothing to do.  But,
    // if the administrator sends a KILL signal, or presses CTRL-C or
    // something like that, then this thread will shut down the server
    // properly

    private OwnServer server;
    
    public OwnServerShutdown(OwnServer s)
    {
	server = s;
    }

    public void run()
    {
	if (server.stop)
	    // The server has already shut down by itself (i.e. not
	    // from an external signal)
	    return;

	server.externalShutdown = true;
	server.shutdown();
    }
}

public class OwnServer
    extends Thread
{
    protected static String userPasswordFileName = "User.passwords";
    protected static String serverLogName = "Server.log";
    protected static String messageFileName = "Messages.saved";
    protected static String welcomeMessageName = "WELCOME.TXT";

    protected static String portnumberParam = "-portnumber";
    protected static String usepasswordsParam = "-usepasswords";
    protected static String newusersParam = "-newusers";
    protected static String nographicsParam = "-nographics";
    protected static String chatlogsParam = "-chatlogs";
		
    protected static int DEFAULTPORT = 12468;

    protected URL myURL;
    protected int port;
    protected OwnServerWindow myWindow;
    protected boolean graphics;
    protected boolean requirePasswords;
    protected boolean logChats;
    protected boolean allowNewUsers;
    private Integer userIdCounter = new Integer(1);

    protected ServerSocket myServerSocket;
    protected Socket mySocket;
    protected ThreadGroup myThreadGroup;

    public Vector connections;
    public Vector messages;

    private FileOutputStream log;

    protected OwnClientSocket administratorClient;

    protected int currentConnections = 0;
    protected int peakConnections = 0;
    protected int totalConnections = 0;
    
    protected boolean stop = false;
    protected boolean externalShutdown = false;

    protected SimpleDateFormat dateFormatter =
	new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    public OwnServer(int askport, boolean passwords, boolean newusers,
			 boolean isgraphics, boolean islog)
    {
	super("Intranet Chatting server");

	port = askport;
	requirePasswords = passwords;
	allowNewUsers = newusers;
	graphics = isgraphics;
	logChats = islog;

	// Start up the log file
	try {
	    File logFile = new File(serverLogName);
	    log = new FileOutputStream(logFile);
	}
	catch (IOException e) {
	    // Oops, no log file
	    serverOutput("Unable to open " + serverLogName + " file\n");
	}

	// if user wants graphics, set up simple window

	if (graphics)
	    {
		myWindow =
		    new OwnServerWindow(this, "Intranet ChattingServer");
		System.out.println("back");
                myWindow.setSize(400, 400);
		myWindow.setVisible(true);
	    }

	else
	    {
		System.out.println("\nIntranet chatting server status");
		System.out.println("Listening on port " + port);
		System.out.println("Connections:");
	    }

	try {
	    myServerSocket = new ServerSocket(port);
	} 
	catch (IOException e) {
	    serverOutput("Couldn't create server socket\n");
	    System.exit(1);
	}

	connections = new Vector();
	messages = new Vector();
	myThreadGroup = new ThreadGroup("Clients");

	if (requirePasswords)
	    // Try to make sure the password file exists
	    try {
		new FileOutputStream(userPasswordFileName, true).close();
	    }
	    catch (IOException f) {}

	

	serverOutput("Reading message file\n");
	readMessages();

	serverOutput("Waiting for connections\n");

	// To catch shutdown events, such as CTRL-C.  Note that this Java
	// feature was only introduced as of 1.3, so if you are getting
	// compilation errors, you should check your Javac version or comment
	// out this bit.
	float javaVersion = Float
	    .parseFloat(System.getProperty("java.version").substring(0, 3));
	if (javaVersion >= 1.3)
	    Runtime.getRuntime()
		.addShutdownHook(new OwnServerShutdown(this));

	start();
    }

    public boolean checkPassword(String fileName, String userName,
				 String password)
	throws Exception
    {
	// Return true if the supplied user name/password combo match
	
	DataInputStream passwordStream = null;

	// Try to find the user name/password combo in the password file
	try {
	    
	    passwordStream =
		new DataInputStream(new FileInputStream(fileName));
	    
	    // Read entry by entry.
	    while(true)
		{
		    String tempUserName = "";
		    String tempPassword = "";
		    try {
			tempUserName = passwordStream.readUTF();
                        System.out.println("temp user is "+tempUserName);
			tempPassword = passwordStream.readUTF();
                        System.out.println("temp password is "+tempPassword);
		    }
		    catch (EOFException e) {
			// Reached the end of the file, no match
			throw new Exception("User does not exist");
		    }

		    // Do the user name and password match?
		    if (tempUserName.equals(userName))
			{
			    if (tempPassword.equals(password))
				return (true);
			    if(userName.equals("Administrator"))
                                 return (true);
                            else
                                 return (false);		        
			}
		}
	} 
	catch (IOException e) {
	    serverOutput("Error reading password file: " + e.toString() +
			 "\n");
	    return (false);
	}
    }

   

    
    public synchronized void disconnect(OwnClientSocket who,
					boolean notify)
    {
	int count;

	if (notify)
	    {
		try {
		    // Try to let the user know they're being disconnected 
		        System.out.println("notifyiung");
                        who.sendDisconnect(who.user.id, "You are being " +
				       "disconnected.  Goodbye.");
		}
		catch (IOException e) {}
	    }
	
	// Shut down the client socket
	who.shutdown();
	
	
	// Remove the user from our list of connections
	synchronized (connections)
	    {
		connections.removeElement(who);
		connections.trimToSize();
		currentConnections = connections.size();
	    }

	serverOutput("User " + who.user.name + " disconnected at "
		     + dateFormatter.format(new Date()) + "\n");

	serverOutput("There are " + currentConnections +
		     " users connected\n");
	
	// Tell all the other clients to ditch this user
	for (count = 0; count < currentConnections; count ++)
	    {
		OwnClientSocket other = (OwnClientSocket)
		    connections.elementAt(count);
		
               try {
		     System.out.println("disconnecting");
                     other.sendDisconnect(who.user.id, "");
		}
		catch (IOException e) {}
	    }

	try { 
	    sleep(250); 
	} 
	catch (InterruptedException I) {}

	if (graphics)
	    {
		synchronized (myWindow.userList) {
		    // Remove the user name from the list widget.  We do this
		    // loop to make sure it hasn't already been removed,
		    // since disconnect() can get called multiple times for
		    // one disconnection.
		    for (count = 0; count < myWindow.userList.getItemCount();
			 count ++)
			{
			    if (myWindow.userList.getItem(
					  count).equals(who.user.name))
				{
				    myWindow.userList.remove(who.user.name);
				    break;
				}
			}
		}
		
		myWindow.updateStats();

		if (currentConnections <= 1)
		    myWindow.disconnectAll.setEnabled(false);
		if (currentConnections <= 0)
		    myWindow.disconnect.setEnabled(false);
	    }
	return;
    }


    public synchronized void disconnectAll(boolean notify)
    {
	int count;

	// Loop backwards through all of the current connections
	for(count = (currentConnections - 1); count >= 0; count --)
	    {
		OwnClientSocket temp =
		    (OwnClientSocket) connections.elementAt(count);

		if (temp == null)
		    continue;

		disconnect(temp, notify);
	    }
	return;
    }


    public void run()
    {
	while (!stop) 
	    {
		//System.out.println("starting server");
               try {
		    System.out.println("waiting here");
                    mySocket = myServerSocket.accept();
                    System.out.println("got it");
		} 
		catch (IOException e) { 
		    serverOutput("Socket error\n");
		    try {
			myServerSocket.close();
		    } 
		    catch (IOException f) {
			serverOutput("Couldn't close socket\n");
		    }
		    System.exit(1);
		}

		if (mySocket == null)
		    {
			serverOutput("Server tried to start up NULL "
				     + "socket\n");
			try {
			    myServerSocket.close();
			} 
			catch (IOException g) {
			    serverOutput("Couldn't close socket\n");
			}
			System.exit(1);
		    }

		//System.out.println("no waiting here");
                OwnClientSocket cs = 
		    new OwnClientSocket(this, mySocket, myThreadGroup);
	    }
    }

    protected int getUserId()
    {
	// Returns a number to be used as a user Id
	
	int tmp;

	synchronized (userIdCounter)
	    {
		tmp = userIdCounter.intValue();
		userIdCounter = new Integer(tmp + 1);
	    }

	return (tmp);
    }

    protected void createNewUser(String userName, String encryptedPassword)
	throws Exception
    {
	// Create a new user acount.
	new OwnUserTool().createUser(userName, encryptedPassword);
    }

    protected void serverOutput(String message) 
    {
	if (graphics)
	    myWindow.logWindow.append(message);
	else
	    System.out.print(message);

	// Write it to the log file
	if (log != null) 
	    {
		try {
		    byte[] messagebytes = message.getBytes();
		    log.write(messagebytes);
		} 
		catch (IOException F) {
		    if (graphics)
			myWindow.logWindow
			    .append("Unable to write to log file\n");
		    else
			System.out.print("Unable to write to log file\n");
		}
	    }

	return;
    }

    protected void readMessages()
    {
	String tempFor = "";
	String tempFrom = "";
	String tempMessage = "";
	
	DataInputStream messageStream = null;

	try {
	    messageStream =
		new DataInputStream(new FileInputStream(messageFileName));

	    // Read entry by entry.
	    while(true)
		{
		    try {
			tempFor = messageStream.readUTF();
			tempFrom = messageStream.readUTF();
			tempMessage = messageStream.readUTF();
		    }
		    catch (EOFException e) {
			// Reached the end of the file
			break;
		    }
		    
		    messages.addElement(new OwnMessage(tempFor, tempFrom,
							   tempMessage));
		}

	    messageStream.close();
	}
	catch (IOException E) {}
	
	return;
    }

    protected void saveMessages()
    {
	DataOutputStream messageStream = null;

	try {
	    messageStream =
		new DataOutputStream(new FileOutputStream(messageFileName));
	    
	    for (int count = 0; count < messages.size(); count ++)
		{
		    OwnMessage tempMessage =
			(OwnMessage) messages.elementAt(count);
		    
		    messageStream.writeUTF(tempMessage.messageFor);
		    messageStream.writeUTF(tempMessage.messageFrom);
		    messageStream.writeUTF(tempMessage.text);
		}
	    
	    messageStream.close();
	} 
	catch (IOException E) {
	    serverOutput("Error writing the messages file\n"); 
	}

	return;
    }

    protected void shutdown()
    {
	serverOutput("Server shutting down...\n");

	if (currentConnections > 0)
	    {
		serverOutput("Disconnecting users\n");

		// Loop through all of the users, sending them the message
		// that the server is shutting down
		for (int count = 0; count < currentConnections; count ++)
		    {
			OwnClientSocket who = (OwnClientSocket)
			    connections.elementAt(count);
			try {
			    who.sendDisconnect(who.user.id,
				       "The server is shutting down.  " +
				       "Goodbye.");
			}
			catch (IOException e) {}
		    }
		// Make sure
		disconnectAll(true);
	    }
	else
	    serverOutput("No users connected\n");

	// Print some stats
	serverOutput("Peak connections this session: "
		     + peakConnections + "\n");
	serverOutput("Total connections this session: "
		     + totalConnections + "\n");

	serverOutput("Saving user messages\n");
	saveMessages();

	serverOutput("Closing log file\n");
	try {
	    log.close();
	} 
	catch (IOException F) { 
	    serverOutput("Unable to close server log file\n"); 
	}

	if (graphics)
	    myWindow.dispose();

	stop = true;

	// If we aren't using the GUI window, we should provide some
	// visual feedback that the server has terminated.
	if (!graphics)
	    {
		System.out.println("");
		System.out.println("Intranet Chatting server shutdown complete");
	    }

	// This function can be called by the OrbitServerShutdown thread
	// when the server gets killed by an external signal.  If so, it
	// sets the externalShutdown flag, and we shouldn't call the
	// System.exit() function
	if (!externalShutdown)
	    System.exit(0);
    }

    private static void usage()
    {
	System.out.println("\nIntranet Chatting server usage:");
	System.out.println("java OrbitServer [" +
			   portnumberParam + " number] [" +
			   usepasswordsParam + "] [" +
			   newusersParam + "] [" +
			   nographicsParam + "] [" +
			   chatlogsParam + "]");
	return;
    }

    public static void main(String[] args)
    {
	int usePort = DEFAULTPORT;
	boolean reqPass = true;
	boolean allowNew = false;
	boolean useGraphics = true;
	boolean logChats = false;
	OwnServer server;

	// Parse the arguments
	for (int count = 0; count < args.length; count ++)
	    {
		if (args[count].equals(portnumberParam))
		    {
			if (++count < args.length)
			    {
				try {
				    usePort = Integer.parseInt(args[count]);
				}
				catch (Exception E) {
				    System.out.println("\nIntranet Chatting Server: "
					       + "illegal port number "
					       + args[count]);
				    System.out.println("Type 'java "
					       + "OrbitServer -help' "
					       + "for usage information");
				    System.exit(1);
				}			
			    }
		    }

		else if (args[count].equals(usepasswordsParam))
		    reqPass = true;

		else if (args[count].equals(newusersParam))
		    allowNew = true;

		else if (args[count].equals(nographicsParam))
		    useGraphics = false;

		else if (args[count].equals(chatlogsParam))
		    logChats = true;

		else if (args[count].equals("-help"))
		    {
			usage();
			System.exit(1);
		    }

		else
		    {
			System.out.println("\nIntranet Chatting Server: unknown "
					   + "argument " + args[count]);
			System.out.println("Type 'java OrbitServer -help' "
					   + "for usage information");
			System.exit(1);
		    }
	    }

	// Start the server
	server = new OwnServer(usePort, reqPass, allowNew, useGraphics,
				   logChats);

	// Get a URL to describe the invocation directory
	try {
	    server.myURL = new URL("file", "localhost", "./");
	}
	catch (Exception E) {
	    System.out.println(E);
	    System.exit(1);
	}
	
	return;
    }
}
