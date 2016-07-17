import java.net.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import java.io.*;

class OrbitClientTickler
    extends Thread
{
    // This class just runs as a separate thread, pinging the client
    // every second to make sure we haven't lost the connection
     
    OwnClientSocket client;
 
    public OrbitClientTickler(OwnClientSocket c)
    {
	super("Orbit Chat client connection tickler");
 
	client = c;
 
	start();
    }
 
    public void run()
    {
	while(!client.stop)
	    {
		try {
		    client.sendPing();
		}
		catch (IOException e) {
		    if (!client.stop)
			client.server.disconnect(client, false);
		    break;
		}
 
		try {
		    sleep (1000);
		}
		catch (InterruptedException e) {}
	    }
    }
}






public class OwnClientSocket
    extends Thread
{
    protected OwnServer server;
    protected Socket mySocket;
    protected DataInputStream istream;
    protected DataOutputStream ostream;
    protected OwnUser user;
    protected Date connectDate;
    protected boolean stop = false;
    protected int pings = 0;

    protected SimpleDateFormat dateFormatter =
	new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    public OwnClientSocket(OwnServer parent, Socket s, 
			       ThreadGroup threadgroup)
    {
	
        super("Orbit Chat client connection");
        System.out.println("just reached client socket"); 
	server = parent;
	mySocket = s;

	// Get a 'user' object
	user = new OwnUser(server);

	// set up the streams
	try {
	    istream = new DataInputStream(mySocket.getInputStream());
	    ostream = new DataOutputStream(mySocket.getOutputStream());
	} 
	catch (IOException a) {
	    server.serverOutput("Error setting up client streams\n");
	    try {
		mySocket.close();
	    } 
	    catch (IOException b) {
		server.serverOutput("Couldn't close socket\n");
	    }
	}

	// Record the date/time of the connection
	connectDate = new Date();

	start();
    }

    public void run()
 
    {

         new OrbitClientTickler(this);
	try{
             while (!stop)
		{
		  
                    parseCommand();
		    ostream.flush();
		}
	}
	catch (IOException a) {
	    if (!stop)
		{
		    
		    System.out.println("disconnecting");
                    server.disconnect(this, false);
		}
	    return;
	}
	return;
    }

    public void parseCommand()
	throws IOException
    {
	short commandType = 0;
	
	synchronized (istream) {

	  
	   
            commandType = istream.readShort();
		 System.out.println("client socket is"+commandType);
         
	
	    // Make sure we weren't stopped while blocking for input
	    if (stop)
		return;

	    
		    switch (commandType) {
			
		    case OwnCommand.NOOP:
			{
			    // Do nothing
			    break;
			}

		    case OwnCommand.PING:
			{
			    // We're receiving a ping reply from the client.
			    receivePing();
			    break;
			}

		    case OwnCommand.USERINFO:
			{
			    // The new user is supplying information about
			    // themselves.
			    receiveUserInfo();
			    break;
			}

		    case OwnCommand.DISCONNECT:
			{
			    // The user is telling us that he's disconnecting.
			    receiveDisconnect();
			    break;
			}

		   
		    case OwnCommand.ACTIVITY:
			{
			    // The client is sending a message about the
			    // user's activity
			    receiveActivity();
			    break;
			}

		    case OwnCommand.CHATTEXT:
			{
			    // The client is sending a line of chat text
			    System.out.println("welcome to chat text"+commandType);
                            receiveChatText();
			    break;
			}

		    

		    default:
			{
				
			    System.out.println("server:jaffa command");
			    
                            break;
			}
		    }
		
	}
    }

   

    protected OwnClientSocket findClientSocket(int userid)
    {
	// This will return the client socket object for the user with the
	// requested ID
	
	OwnClientSocket tempClient = null;
	OwnClientSocket returnClient = null;

	for (int count = 0; count < server.currentConnections; count ++)
	    {
		tempClient = (OwnClientSocket)
		    server.connections.elementAt(count);

		if (tempClient.user.id == userid)
		    {
			returnClient = tempClient;
			break;
		    }
	    }

	return (returnClient);
    }

   

    protected void sendPing()
	throws IOException
    {
	

	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.PING);
	    }
    
	// Add one to the number of pings sent
	pings++;
        System.out.println("pings are"+pings);

	// Have we got too many unanswered pings?
    }

    protected void receivePing()
    {
	// The client has sent back a ping to us.  Subtract 1 from the number
	// of pings outstanding
	pings--;
        System.out.println("pings are"+pings);
    }

    protected void sendConnect(String userName)
	throws IOException
    {
	// Send a notification that a new user has connected
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.CONNECT);
		ostream.writeUTF(userName);
	    }
    }
    
    protected void sendUserInfo(OwnUser newuser)
	throws IOException
    {
	// Send information about a new user to this participant
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.USERINFO);
                System.out.println("in userinfo");
		ostream.writeInt(newuser.id);
		ostream.writeUTF(newuser.name);
		ostream.writeUTF(""); // Don't send password info
		//ostream.writeUTF(newuser.additional);
	    }
    }

    protected void receiveUserInfo()
	throws IOException
    {
	OwnClientSocket tmpClient;

	
	istream.readInt(); // WE assign the user IDs
	user.name = istream.readUTF();
	user.password = istream.readUTF();
         System.out.println("username is"+user.name);
         System.out.println("password is"+user.password);
	
	//user.additional = istream.readUTF();

	if  (this.user.name.equals("Administrator"))
	    {

		
		server.administratorClient = this;
	    }

	for (int count = 0; count < server.currentConnections; count ++)
	    {
		OwnClientSocket tmpSocket =
		    ((OwnClientSocket)
		     server.connections.elementAt(count));
				
		if (tmpSocket.user.name.equals(user.name))
		    {
			System.out.println("Sending 'already' message");
			sendDisconnect(0, "There is already a user " +
				       "called \"" + user.name +
				       "\" logged in!");

			// Give the client a moment to receive the message
			// before we disconnect him
			try { sleep(1000); } catch (InterruptedException e) {}

			this.shutdown();
			return;
		    }
	    }

	// If we are requiring passwords, make sure the password matches
	// the one in the password file
	if (server.requirePasswords)
	    {
		boolean authenticated = false;
                System.out.println(authenticated);
  

		try {
		    authenticated = server
			.checkPassword(OwnServer.userPasswordFileName,
				       user.name, user.password);
                    System.out.println("password checked"+authenticated);
		}
		catch (Exception e) {
		    // This user does not
		    if (server.allowNewUsers)
			{
			    // Create the new user account
			    try {
				server.createNewUser(user.name,
						     user.password);
			    }
			    catch (Exception f) {
				sendDisconnect(0, "Unable to create " +
					       "a new user account");

				// Give the client a moment to receive the
				// message before we disconnect him
				try { sleep(1000); }
				catch (InterruptedException g) {}

				this.shutdown();
				return;
			    }
			    authenticated = true;
			}
		}

		if (!authenticated)
		    {
			// The user/password combo don't match.
			sendDisconnect(0, "Incorrect user name or " +
				       "password");
			InetAddress tmpAddr =
			    mySocket.getInetAddress();
			server.serverOutput("Failed login for user name \"" +
					    user.name + "\" from host " +
					    tmpAddr.getHostName() + "\n");

			// Give the client a moment to receive the message
			// before we disconnect him
			try { sleep(1000); } catch (InterruptedException e) {}

			this.shutdown();
			return;
		    }
	    }

	// Send the user info about themselves
	sendUserInfo(user);
        System.out.println("back after sending user info");

	
	for (int count = 0; count < server.currentConnections; count ++)
	    {
		tmpClient = (OwnClientSocket)
		    server.connections.elementAt(count);
		
		if (tmpClient.user != user)
		    {
			sendUserInfo(tmpClient.user);
			
			tmpClient.sendConnect(user.name);
			tmpClient.sendUserInfo(user);
		    }
	    }

	// OK, we can add this user to the list of connections
	synchronized (server.connections) 
	    {
		server.connections.addElement(this);

		// Update our statistics
		server.currentConnections = server.connections.size();
		server.totalConnections++;

		// Has the peak number of connections been exceeded?
		if (server.currentConnections > server.peakConnections)
		    server.peakConnections = server.currentConnections;
	    }

	server.serverOutput("New user " + user.name + " logging on at "
			      + dateFormatter.format(connectDate) + " from "
			      + mySocket.getInetAddress().getHostName() +
			      "\n");
	server.serverOutput("There are " + server.currentConnections
			      + " users connected\n");

	if (server.graphics)
	    {
		// Update the server window
		server.myWindow.userList.add(user.name);
		server.myWindow.disconnect.setEnabled(true);
		
		if (server.currentConnections > 1)
		    server.myWindow.disconnectAll.setEnabled(true);

		server.myWindow.updateStats();
	    }

	// Send the banner message to the user
	sendServerText("Welcome to Intranet Chatting version\n\n");

	
	String mess = "";
	if (server.currentConnections == 1) 
	    mess = "You are the first user online.";
	else if (server.currentConnections > 2)
	    {
		mess = "There are " +
		    (server.currentConnections - 1)
		    + " other users online";
		
	    }
	else
	    {
		mess = "There is 1 other user online";
		
	    }
	sendServerText(mess + "\n\n");

	
	// Are there any saved messages waiting for this user name?
	int numberMessages = 0;
	for (int count = 0; count < server.messages.size(); count ++)
	    if (((OwnMessage) server.messages.elementAt(count))
		.messageFor.equals(user.name))
		numberMessages++;
	if (numberMessages > 0)
	    sendServerMessage("You have " + numberMessages
			      + " message(s) waiting.");

	// All set
    }

    protected void sendServerMessage(String data)
	throws IOException
    {
	// This will cause a message dialog to appear on the screen of
	// the user
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.SERVERMESS);
		ostream.writeUTF(data);
	    }
    }

    protected void sendDisconnect(int userId, String mess)
	throws IOException
    {
	// Tell this user that a user (possibly the user himself) is
	// disconnecting
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.DISCONNECT);
                System.out.println("sending disconnect");
		ostream.writeInt(userId);
		ostream.writeUTF(mess);
	    }
    }

    protected void receiveDisconnect()
	throws IOException
    {
	// The user is disconnecting from the server.  Just clear out the
	// stream
	istream.readInt();
	istream.readUTF();
	istream.readFully(new byte[istream.available()]);


	server.disconnect(this, false);
    }

   

    protected void sendActivity(int fromid, short activity)
	throws IOException
    {
	// This will inform the other users that we're in the process of
	// typing or drawing something
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.ACTIVITY);
		ostream.writeInt(fromid);
		ostream.writeShort(activity);
		ostream.writeInt(0); // Empty recipient list
	    }
    }

    protected void receiveActivity()
	throws IOException
    {
	// This will receive a signal that our client is busy doing some
	// activity such as typing or drawing and output it to the
	// appropriate recipients

	short activity = 0;
	int numRecipients = 0;
	OwnClientSocket toUser;

	// Read the sender user id (ours) and discard it.  Then read the
	// activity and the number of recipients
	istream.readInt();
	activity = istream.readShort();
	numRecipients = istream.readInt();

	
	   
		// Loop for each of the intended recipients
		for (int count = 0; count < numRecipients; count ++)
		    {
			toUser = findClientSocket(istream.readInt());
		
			if (toUser != null)
			    if (toUser != this)
				// Send it out to this recipient
				toUser.sendActivity(user.id, activity);
		    }
	    
    }

    protected void sendChatText(int fromid, boolean priv, String data)
	throws IOException
    {
	// This will send a line of chat text to our client
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.CHATTEXT);
		ostream.writeInt(fromid);
		ostream.writeBoolean(priv);
		ostream.writeUTF(data);
		ostream.writeInt(0); // Empty recipient list
	    }
    }

    protected void receiveChatText()
	throws IOException
    {
	// This will receive a line of chat text and output it to the
	// appropriate recipients

	boolean priv = false;
	String data = "";
	int numRecipients = 0;
	OwnClientSocket toUser;

	// Read the sender user id (ours) and discard it.  Then read the
	// privacy value, the colour, the data, and the number of recipients
	istream.readInt();
	priv = istream.readBoolean();
	data = istream.readUTF();
	numRecipients = istream.readInt();
        System.out.println("number of recipients"+numRecipients);
        System.out.println("data is "+data);

	if (!priv)
	    {
		for (int count = 0; count < server.currentConnections; count ++)
		    {
			toUser = (OwnClientSocket)
			    server.connections.elementAt(count);
			if (toUser != this)
			    toUser.sendChatText(user.id, priv, data);
		    }

			
	    }
	else
	    {
		// Loop for each of the intended recipients
		for (int count = 0; count <  numRecipients; count ++)
		    {
			toUser = findClientSocket(istream.readInt());
			
			if (toUser != null)
			    if (toUser != this)
				// Send it out to this recipient
				toUser.sendChatText(user.id, priv, data);
		    }
	    }
    }
    

    protected void sendServerText(String data)
	throws IOException
    {
	// This will just output a text message from the server to be
	// displayed in the user's chat window
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.CHATTEXT);
		ostream.writeInt(0); // From user "nobody"
		ostream.writeBoolean(false); // Not private
		ostream.writeUTF(data);
		ostream.writeInt(0); // Empty recipient list
	    }
    }

    protected void shutdown()
    {
	// Stop the thread for this client socket
	stop = true;

	// Close the data streams
	try {
	    // Don't do this synchronized, since the client reader will be
	    // sitting there blocking, waiting for data.
	    istream.close();
	}
	catch (IOException a) {}

	try {
	    synchronized (ostream)
		{
		    ostream.flush();
		    ostream.close();
		}
	}
	catch (IOException a) {}

	// Close the socket
	try {
	    mySocket.close();
	} 
	catch (IOException F) {}

	// Force garbage collection, since some clients seem to hold on
	// to the connection somehow
	System.gc();
	
	return;
    }
}
