import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class OwnClient
    extends Thread
{
    protected Socket socket;
    protected DataInputStream istream;
    protected DataOutputStream ostream;
    protected boolean stop = false;

    protected OwnWindow parentWindow;
    protected Vector userList = new Vector();
   
    public OwnClient(String host, String name, int portnumber,
			 OwnWindow mainWindow)
	throws UnknownHostException, IOException, Exception 
    {
	super("jntu Chat client thread");
        System.out.println("entered client socket");

	parentWindow = mainWindow;

	// set up the client socket
	socket = new Socket(host, portnumber);

	// Get the output stream
	ostream = new DataOutputStream(socket.getOutputStream());

	// Get an input stream to correspond to the client socket
	istream = new DataInputStream(socket.getInputStream());

	

	// Start listening for stuff from the server
	start();
                                 

	// Now send the server some information about this user
	sendUserInfo();
        System.out.println("back after sending user info");
    }

    public void run()
    {
	  System.out.println("starting client thread");
          while (!stop)
	    try {
		parseCommand();
	
	    }
	    catch (IOException e) {
		lostConnection();
		return;
	    }

	return;
    }

    void parseCommand()
	throws IOException
    {
	
	short commandType = 0;

	synchronized (istream) {

              
	    commandType = istream.readShort();
            System.out.println("client command"+commandType);

	 
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
			    // The server is sending us a ping request.  Just
			    // send one back
			    sendPing();
			    break;
			}

		    case OwnCommand.CONNECT:
			{
			    // The server is telling us that a new user
			    // connected
			    receiveConnect();
			    break;
			}

		    case OwnCommand.USERINFO:
			{
			    // The server is sending info about a user
			    // (maybe us)
			    receiveUserInfo();
			    break;
			}

		    case OwnCommand.SERVERMESS:
			{
			    // The server is sending us a 'dialog box' message
			    receiveServerMess();
			    break;
			}

		    case OwnCommand.DISCONNECT:
			{
			    // Somebody is disconnecting.  Maybe us :)
			    receiveDisconnect();
			    break;
			}


		    case OwnCommand.ACTIVITY:
			{
			    // Someone is typing, drawing, etc.
			    receiveActivity();
			    break;
			}

		    case OwnCommand.CHATTEXT:
			{
			    // There's incoming chat text from another user
			    System.out.println("caught chat text");
                            receiveChatText();
			    break;
			}


		    default:
			{
			   
			    System.out.println("client: unknown command "
					       + commandType);
			    break;
			}
		    }
		
	}
    }

   

    protected OwnUser findUser(int userId)
    {
	// Find a user in the list

	OwnUser tmpUser = null;
	OwnUser returnUser = null;

	if (userId == 0)
	    return (null);

	for (int count = 0; count < userList.size(); count ++)
	    {
		tmpUser = (OwnUser) userList.elementAt(count);
                System.out.println("find user is "+tmpUser.name);

		if (tmpUser.id == userId)
		    {
			returnUser = tmpUser;
			break;
		    }
	    }

	return (returnUser);
    }

    protected OwnUser readUser()
	throws IOException
    {
	
	int userId = 0;
	userId = istream.readInt();
        System.out.println("user id of sender is"+userId);

	return (findUser(userId));
    }

    protected void sendRecipients()
	throws IOException
    {
	// This function will construct a recipient list and send it
	// down the pipe based on which users are selected in the 'send to'
	// list of the parent window

	String[] selectedUsers;
	int numberUsers = 0;

	if (parentWindow.sendToAll.getState())
	    {
	
		ostream.writeInt(0);
	    }
	else
	    {
		// Get the list of selected user names
		selectedUsers = parentWindow.sendTo.getSelectedItems();

		numberUsers = selectedUsers.length;

		// Write out how many
		ostream.writeInt(numberUsers);
			
		// Loop for each 
		for (int count1 = 0; count1 < numberUsers; count1 ++)
		    {
			
			for (int count2 = 0; count2 < userList.size();
			     count2++)
			    {
				OwnUser tmp = (OwnUser)
				    userList.elementAt(count2);
					
				if (selectedUsers[count1].equals(tmp.name))
				    ostream.writeInt(tmp.id);
			    }
		    }
	    }
    }


    
    protected void sendPing()
	throws IOException
    {
	// Send a ping reply back to the server
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.PING);
                System.out.println("i ");
                return;
	    }  
    }

    protected void receiveConnect()
	throws IOException
    {
	// A new user has connected.  We only use this to output a message
	// saying that the user has connected; we get a USERINFO command
	// that will actually tell us about the user later
	
	String userName = istream.readUTF();

	parentWindow.messages
	    .append("<<New user \"" + userName + "\" connected>>\n");
        parentWindow.sendTo.add(userName);
    }

    protected void sendUserInfo()
	throws IOException
    {
	// Send the server some information about this user
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.USERINFO);
		ostream.writeInt(parentWindow.id); // We don't know our id
		ostream.writeUTF(parentWindow.name);
		ostream.writeUTF(parentWindow.plainPassword);
                System.out.println("two"+OwnCommand.USERINFO);
                System.out.println("client id"+parentWindow.id);
                System.out.println("client name"+parentWindow.name);
                System.out.println("client pasword"+parentWindow.plainPassword);
		
	    }
    }

    protected void receiveUserInfo()
	throws IOException
    {
	int tmpId = 0;
	String tmpName = "";
	OwnUser newUser;

	// The server is sending new information about some user.
	tmpId = istream.readInt();
	tmpName = istream.readUTF();
        System.out.println("temp name is"+tmpName);
        System.out.println("temp id is" +tmpId);
	// Password field will be empty
	istream.readUTF();


	// Is the user name ours?  If so, the server is telling us our
	// own user id number.
	if (tmpName.equals(parentWindow.name))
	    {
		parentWindow.id = tmpId;
	    }
	else
	    {
		// Some new user has connected.  Create a new user object
		newUser = new OwnUser(tmpId, tmpName, "");
                

		// Add the user to our collection of users
		userList.addElement(newUser);
                System.out.println("new user is connected"+newUser.name);
               parentWindow.sendTo.add(newUser.name);
	    }
    }

    protected void receiveServerMess()
	throws IOException
    {
	String message = "";

	// The server is sending us a message.  Get the message.
	message = istream.readUTF();

	// Make a dialog box with the message
	
	new OwnInfoDialog(parentWindow, "Server message", true,
			      message);
    }

    protected void sendDisconnect()
	throws IOException
    {
	// Tell the server that we're disconnecting
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.DISCONNECT);
		ostream.writeInt(parentWindow.id);
		ostream.writeUTF("");
	    }
    }

    protected void receiveDisconnect()
	throws IOException
    {
	int tmpId = 0;
	OwnUser tmpUser;
	String disconnectMess = "";
	java.awt.List list = parentWindow.sendTo;

	tmpId = istream.readInt();
	disconnectMess = istream.readUTF();
	istream.readFully(new byte[istream.available()]);

	// Who is it?  Is it us?
	if ((tmpId == parentWindow.id) || (tmpId == 0))
	    {
		// If it's us, make a dialog box with the disconnection
		// message
	
		if (disconnectMess.equals(""))
		    disconnectMess = "(no reason given)";

		new OwnInfoDialog(parentWindow, "Disconnected", true,
				      disconnectMess);

		shutdown(false);
		parentWindow.offline();
	    }
	else
	    {
		// Some other user disconnected.  Output a message that this
		// user has left the chat.

		tmpUser = findUser(tmpId);

		if (tmpUser == null)
		    // Don't know who this is.  Ignore.
		    return;

		parentWindow.messages.append("<<" + tmpUser.name
					     + " is disconnecting>>\n");

		// Remove this name from our 'currently sending to' list
		synchronized (list)
		    {
			for (int count2 = 0; count2 < list.getItemCount();
			     count2 ++)
			    {
				if (list.getItem(count2).equals(tmpUser.name))
				    {
					if (list.isIndexSelected(count2))
					    list.select(0);
					list.remove(count2);
					break;
				    }
			    }
			
			// If there's nothing left in the list, make sure
			// the 'send to all' checkbox is checked
			if (list.getSelectedItems().length == 0)
			    parentWindow.sendToAll.setState(true);
		    }

		
		userList.removeElement((Object) tmpUser);
		userList.trimToSize();

		
	    }
    }

   

    protected void sendActivity(short activity)
	throws IOException
    {
	// This tells our selected recipients that we're in the middle
	// of typing something.
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.ACTIVITY);
		ostream.writeInt(parentWindow.id);
		ostream.writeShort(activity);
		sendRecipients();
	    }
    }

    protected void receiveActivity()
	throws IOException
    {
	// Some user is doing some activity, such as typing or drawing.

	OwnUser fromUser = null;
	short activity = 0;
	int numForUsers = 0;

	fromUser = readUser();

	// Read the activity and discard the recipient list, if there is one
	activity = istream.readShort();
	numForUsers = istream.readInt();
	for (int count = 0; count < numForUsers; count ++)
	    istream.readInt();
	
	if (fromUser == null)
	    // Ack.  No such user.  It can happen if someone logs out at
	    // just the right moment
	    return;

	String tmpString = "";
	
	if (activity == OwnCommand.ACTIVITY_TYPING)
	    tmpString = "typing: " + fromUser.name;

	if (!parentWindow.activity.getText().equals(tmpString))
	    parentWindow.activity.setText(tmpString);
    }

    protected void sendChatText(String data)
	throws IOException
    {
	// This sends a line of chat text to the selected recipients
	synchronized (ostream)
	    {
		ostream.writeShort(OwnCommand.CHATTEXT);
		ostream.writeInt(parentWindow.id);
		if (parentWindow.sendToAll.getState())
		    // Public
		    ostream.writeBoolean(false);
		else
		    // Private
		    ostream.writeBoolean(true);
		ostream.writeUTF(data);
		sendRecipients();
	    }
    }

    protected void receiveChatText()
	throws IOException
    {
	OwnUser fromUser = null;
	boolean priv = false;
	short colour = 0;
	String data = "";
	String output = "";
	int numForUsers = 0;

	// There is incoming chat text.

	// From whom is this message?
	fromUser = readUser();
        System.out.println("from user is"+fromUser);

	// Is this message private
	priv = istream.readBoolean();
	data = istream.readUTF();

	// Discard the recipient list, if there is one
	numForUsers = istream.readInt();
	for (int count = 0; count < numForUsers; count ++)
	    istream.readInt();
	
	if (fromUser != null)
	    {
		
			if (priv)
			    output += "*private from " + fromUser.name +
				"*> ";
			else
			    output += fromUser.name + "> ";
		    
	    }

	// Append the actual message
	
	output += data;
	parentWindow.activity.setText("");
	parentWindow.messages.append(output);
    }

   

 

    
    public void lostConnection()
    {
	 System.out.println("in lost connection");
           if (!stop)
	    {
		shutdown(true);
		parentWindow.offline();
	    }
	return;
    }

    public synchronized void shutdown(boolean notifyUser)
    {
	// Shut down the reader thread
	stop = true;

	// Close my input and output data streams.  Don't do this
	// synchronized, since the client reader will be sitting there
	// blocking, waiting for data.
	try {
	    istream.close();
	}
	catch (IOException e) {}
	
	try {
	    synchronized (ostream)
		{
		    ostream.flush();
		    ostream.close();
		}

	    // close up my socket
	    socket.close();
	}
	catch (IOException e) {}

	// Empty out our user list
	userList.removeAllElements();

	 new OwnInfoDialog(parentWindow, "Disconnected", true,
				  ("Disconnected from "
				   + parentWindow.host));

	parentWindow.theClient = null;

	// Force garbage collection, since some clients seem to hold on
	// to the connection somehow
	System.gc();

	return;
    }
}
