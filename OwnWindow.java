import java.awt.*;
import java.awt.event.*;
import javax.swing.ImageIcon;
import java.io.*;
import java.net.*;


public class OwnWindow
    extends Frame
    implements ActionListener, ItemListener, KeyListener, MouseListener, 
	       WindowListener
{
    public int id = 0;
    public String name = "";
    public String plainPassword = "";
    public String encryptedPassword = "";
    public String host = "localhost";
    public String port = "12468";
    public int portNumber = 12468;
    public String additional = "";
    protected boolean requirePassword = true;
    public int drawFontNumber;
    public int drawStyle;
    public int drawSize;
    public String drawText;
    public URL OrbitURL;
    protected String buffer;
    
    public GridBagLayout myLayout = new GridBagLayout();
    public GridBagConstraints myConstraints = new GridBagConstraints();
    static final Font XsmallFont = new Font("Helvetica", Font.PLAIN, 10);
    static final Font smallFont = new Font("Helvetica", Font.PLAIN, 12);
    static final Font largeFont = new Font("Helvetica", Font.PLAIN, 14);
    static final Font XlargeFont = new Font("Helvetica", Font.PLAIN, 16);

    
    // socket stuff

    public OwnClient theClient;
    protected boolean connected;

  

    // If this is an administrator console (setting this to true won't
    // grant special privileges as far as the server is concerned -- it's
    // only for the benefit of this window so don't bother trying ;-)
    protected boolean adminConsole = false;

    // the menu items

    protected MenuItem menuConnect;
    protected MenuItem menuDisconnect;
    protected MenuItem menuSaveText;
    protected MenuItem menuexit;
    protected Menu fileMenu;
    protected MenuItem menuCopy;
    protected MenuItem menuPaste;
    protected Menu editMenu; 
    protected MenuItem menuAbout;
    protected MenuItem userInfo;
    protected Menu helpMenu;
 
    // Left side

    protected Label sendLineLabel;
    protected Label conferenceLabel;
    protected TextArea typed;
    protected TextArea messages;

    // Right side

    protected Label nameLabel;
    public TextField userId;
    protected Label activityLabel;
    protected TextField activity;
    protected Label sendToLabel;
    protected Checkbox sendToAll;
    public java.awt.List sendTo;


    // set up

    public OwnWindow(String userName, String userPassword,
			 String hostName, String portName,
			URL myURL)
    {
	super();

	// Set the username, host, and port values if they've been specified
	if (userName != null)
	    if (!userName.equals(""))
		name = userName;
	if (userPassword != null)
	    if (!userPassword.equals(""))
		plainPassword = userPassword;
	if (hostName != null)
	    if (!hostName.equals(""))
		host = hostName;
	if (portName != null)
	    if (!portName.equals(""))
		port = portName;

	// Get the URL of the current directory, so that we can find the
	// rest of our files.
	OrbitURL = myURL;

	// set background color
	Color mycolor = Color.lightGray;
	setBackground(mycolor);

	setSize(600,500);
	myConstraints.fill = myConstraints.BOTH;

	// set up all of the window crap

	setLayout(myLayout);
	myConstraints.insets = new Insets(0, 5, 0, 5);

	// the menu bar

	menuConnect = new MenuItem("Connect");
	menuConnect.addActionListener(this);
	menuConnect.setEnabled(true);

	menuDisconnect = new MenuItem("Disconnect");
	menuDisconnect.addActionListener(this);
	menuDisconnect.setEnabled(false);

	menuSaveText = new MenuItem("Save chat as...");
	menuSaveText.addActionListener(this);
	menuSaveText.setEnabled(true);

	menuexit = new MenuItem("Exit");
	menuexit.addActionListener(this);
	menuexit.setEnabled(true);

	fileMenu = new Menu("File");
	fileMenu.add(menuConnect);
	fileMenu.add(menuDisconnect);
	fileMenu.add(menuSaveText);
	fileMenu.add(menuexit);

	menuCopy = new MenuItem("Copy text");
	menuCopy.addActionListener(this);
	menuCopy.setEnabled(true);

	menuPaste = new MenuItem("Paste text");
	menuPaste.addActionListener(this);
	menuPaste.setEnabled(false);

	editMenu = new Menu("Edit");
	editMenu.add(menuCopy);
	editMenu.add(menuPaste);

	menuAbout = new MenuItem("About Intranet Chatting");
	menuAbout.addActionListener(this);
	menuAbout.setEnabled(true);
        
        userInfo = new MenuItem("About Intranet Chatting");
	userInfo.addActionListener(this);
	userInfo.setEnabled(true);
	
        helpMenu = new Menu("Help");
	helpMenu.add(menuAbout);
        helpMenu.add(userInfo);

	MenuBar menubar = new MenuBar();
	menubar.add(fileMenu);
	menubar.add(editMenu);
	menubar.add(helpMenu);
	menubar.setHelpMenu(helpMenu);
	setMenuBar(menubar);

	sendLineLabel = new Label("Text lines to send:");
	sendLineLabel.setFont(smallFont);
	myConstraints.gridx = 0; myConstraints.gridy = 0;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
	myConstraints.anchor = myConstraints.WEST;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 0.0; myConstraints.weighty = 0.0;
	myLayout.setConstraints(sendLineLabel, myConstraints);
	add(sendLineLabel);

	typed = new TextArea("", 2, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
	typed.setEditable(true);
	typed.setFont(largeFont);
	typed.addKeyListener(this);
	myConstraints.gridx = 0; myConstraints.gridy = 1;
	myConstraints.gridheight = 2; myConstraints.gridwidth = 1;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 1.0; myConstraints.weighty = 0.0;
	myLayout.setConstraints(typed, myConstraints);
	add(typed);

	conferenceLabel = new Label("Conference text:");
	conferenceLabel.setFont(smallFont);
	myConstraints.gridx = 0; myConstraints.gridy = 3;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
	myConstraints.anchor = myConstraints.WEST;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 0.0; myConstraints.weighty = 0.0;
	myLayout.setConstraints(conferenceLabel, myConstraints);
	add(conferenceLabel);

	messages = new TextArea("", 10, 50,
				TextArea.SCROLLBARS_VERTICAL_ONLY);
	messages.setEditable(false);
	messages.setFont(smallFont);
	myConstraints.gridx = 0; myConstraints.gridy = 4;
	myConstraints.gridheight = 7; myConstraints.gridwidth = 1;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.insets.top = 5; myConstraints.insets.bottom = 5;
	myConstraints.weightx = 1.0; myConstraints.weighty = 1.0;
	myLayout.setConstraints(messages, myConstraints);
	add(messages);


	myConstraints.insets.top = 0; myConstraints.insets.bottom = 0;

	nameLabel = new Label("User name:");
	nameLabel.setFont(smallFont);
	myConstraints.gridx = 1; myConstraints.gridy = 0;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 2;
	myConstraints.anchor = myConstraints.WEST;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 0.0; myConstraints.weighty = 0.0;
	myConstraints.insets.right = 5; myConstraints.insets.left = 0;
	myLayout.setConstraints(nameLabel, myConstraints);
	add(nameLabel);

	userId = new TextField(name);
	userId.setFont(smallFont);
	userId.setEditable(false);
	myConstraints.gridx = 1; myConstraints.gridy = 1;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 2;
	myConstraints.anchor = myConstraints.WEST;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 0.0; myConstraints.weighty = 0.0;
	myConstraints.insets.right = 5; myConstraints.insets.left = 0;
	myLayout.setConstraints(userId, myConstraints);
	add(userId);

	activityLabel = new Label("Current activity:");
	activityLabel.setFont(smallFont);
	myConstraints.gridx = 1; myConstraints.gridy = 2;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 2;
	myConstraints.anchor = myConstraints.WEST;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 0.0; myConstraints.weighty = 0.0;
	myConstraints.insets.right = 5; myConstraints.insets.left = 0;
	myLayout.setConstraints(activityLabel, myConstraints);
	add(activityLabel);

	activity = new TextField();
	activity.setEditable(false);
	activity.setFont(smallFont);
	myConstraints.gridx = 1; myConstraints.gridy = 3;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 2;
	myConstraints.anchor = myConstraints.CENTER;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 0.0; myConstraints.weighty = 0.0;
	myConstraints.insets.right = 5; myConstraints.insets.left = 0;
	myLayout.setConstraints(activity, myConstraints);
	add(activity);

	sendToLabel = new Label("Currently sending to:");
	sendToLabel.setFont(smallFont);
	myConstraints.gridx = 1; myConstraints.gridy = 4;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 2;
	myConstraints.anchor = myConstraints.WEST;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 0.0; myConstraints.weighty = 0.0;
	myConstraints.insets.right = 5; myConstraints.insets.left = 0;
	myLayout.setConstraints(sendToLabel, myConstraints);
	add(sendToLabel);

	sendTo = new java.awt.List(4);
	sendTo.setFont(XsmallFont);
	sendTo.addItemListener(this);
	sendTo.setMultipleMode(true);
	myConstraints.gridx = 1; myConstraints.gridy = 5;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 2;
	myConstraints.anchor = myConstraints.CENTER;
	myConstraints.fill = myConstraints.BOTH;
	myConstraints.weightx = 0.0; myConstraints.weighty = 1.0;
	myConstraints.insets.right = 5; myConstraints.insets.left = 0;
	myConstraints.insets.top = 0; myConstraints.insets.bottom = 0;
	myLayout.setConstraints(sendTo, myConstraints);
	add(sendTo);

	sendToAll = new Checkbox("send to everyone", true);
	sendToAll.setFont(XsmallFont);
	sendToAll.addItemListener(this);
	myConstraints.gridx = 1; myConstraints.gridy = 6;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 2;
	myConstraints.anchor = myConstraints.WEST;
	myConstraints.fill = myConstraints.NONE;
	myConstraints.weightx = 0.0; myConstraints.weighty = 0.0;
	myConstraints.insets.right = 5; myConstraints.insets.left = 0;
	myConstraints.insets.top = 0; myConstraints.insets.bottom = 0;
	myLayout.setConstraints(sendToAll, myConstraints);
	add(sendToAll);

	

	// register to receive the various events
	addKeyListener(this);
	addMouseListener(this);
	addWindowListener(this);
	
	
	// show the window and get going
	pack();
	offline();
	typed.requestFocus();
     
        encryptedPassword=plainPassword;
        System.out.println("waiting");
	
  }

    public void online()
    {
	connected = true;
	menuConnect.setEnabled(false);
	menuDisconnect.setEnabled(true);
	setTitle("Intranet chatting at " + host);
	return;
    }

    public void offline()
    {
	connected = false;
	menuConnect.setEnabled(true);
	menuDisconnect.setEnabled(false);
	sendToAll.setState(true);
	setTitle("offline");
	return;
    }

    protected boolean haveRequiredInformation()
    {
	// Do we have everything we need to connect to the server?
	if (name.equals("") ||
	    (requirePassword && plainPassword.equals("")) ||
	    host.equals("") ||
	    port.equals(""))
	    return (true);
	else
	    return (true);
    }

    protected void connect()
    {
	// Have all the settings been entered?
	if (haveRequiredInformation())
	    {
		// Give the user a chance to enter it
		new OwnSettings(this);
                System.out.println("returned");
		
		// Do we have it NOW?
		if (!haveRequiredInformation())
		    {
			new OwnInfoDialog(this, "Connection canceled",
					      true,
					      "You are missing some " +
					      "required information!");
			return;
		    }
	    }

	synchronized (messages)
	    {
		messages.setText("");
                System.out.println("setting");
	    }

	// open up my socket
	try {
	    portNumber = Integer.parseInt(port);
	}
	catch (NumberFormatException n) {;}

	try {
	    theClient = new OwnClient(host, name, portNumber, this);
	}
	catch (UnknownHostException a) {
	    new OwnInfoDialog(this, "Couldn't connect", true,
				  "Couldn't find the server " + host);
	    return;
	}
	catch (IOException b) {
	    new OwnInfoDialog(this, "Couldn't connect", true,
				  "Couldn't connect to port " + portNumber
				  + " on server " + host);
	    return;
	}
	catch (Exception c)
	    {
		
	    }
	
	online();
	return;
    }

    protected synchronized void disconnect()
    {
	if (theClient != null)
	    {
		// Tell the server
		try {
		    theClient.sendDisconnect();
		    theClient.shutdown(false);
		}
		catch (IOException e) {
		    theClient.lostConnection();
		}
	    }
	theClient = null;
	offline();
	return;
    }

 protected void showUserInfo()
    {
	// This will show info about another user in a text dialog
	
	String message = "";
	String[] selectedUsers;

	selectedUsers = sendTo.getSelectedItems();

	// Loop for each user that's selected
	for (int count1 = 0; count1 < selectedUsers.length; count1 ++)
	    {
		// Find this user in our user list
		for (int count2 = 0; count2 < theClient.userList.size();
		     count2++)
		    {
			OwnUser tmp =
			    (OwnUser) theClient.userList.elementAt(count2);

			if (selectedUsers[count1].equals(tmp.name))
			    {
				// Here's one.
				message = "Login name:\t" + tmp.name
				    + "\nAdditional info:\n\n";
				   
				new OwnTextDialog(this,
				      "User information for " + tmp.name, 
				      message, 40, 10,
				      TextArea.SCROLLBARS_VERTICAL_ONLY,
				      false);
			    }
		    }
	    }
    }

    protected void saveText()
    {
	// Save the chat text as a file.

	File textFile = null;
	FileOutputStream fileStream = null;

	// Fire up a file dialog to let the user choose the file location
	FileDialog saveTextDialog =
	    new FileDialog(this, "Save chat text as...", 
			   FileDialog.SAVE);
	saveTextDialog.show();

	// Try to create the file
	try {
	    textFile = new File(saveTextDialog.getDirectory() +
			       saveTextDialog.getFile());
	    fileStream = new FileOutputStream(textFile);
	    byte[] bytes = messages.getText().getBytes();
	    fileStream.write(bytes);
	} 
	catch (IOException F) { 
	    new OwnInfoDialog(this, "Failed", true,
				  "Can't write to that file");
	    return;
	}
    }

   

    public void actionPerformed(ActionEvent E)
    {
	// the menu items

	if (E.getSource() == menuConnect)
	    {
		connect();
		return;
	    }

	if (E.getSource() == menuDisconnect)
	    {
		disconnect();
		return;
	    }
    
	if (E.getSource() == menuSaveText)
	    {
		saveText();
		return;
	    }
    
	if (E.getSource() == menuexit)
	    {
		if (connected == true)
		    disconnect();
		dispose();

		return;
	    }

	if (E.getSource() == menuCopy)
	    {
		buffer = messages.getSelectedText();
		menuPaste.setEnabled(true);
		return;
	    }

	if (E.getSource() == menuPaste)
	    {
		typed.setText(buffer);
		return;
	    }

	

	if (E.getSource() == menuAbout)
	    {
                String abouttext = new String("Intranet Chatting by sudhir");
		
		new OwnTextDialog(this, "About Intranet Chatting", 
				      abouttext, 60, 22,
				      TextArea.SCROLLBARS_NONE, false);
		return;
	    }

	if(E.getSource()==userInfo)
            {
               showUserInfo();
               return;
            }
    }

    public void keyPressed(KeyEvent E)
    {
    }

    public void keyReleased(KeyEvent E)
    {
	System.out.println("in release");
         if (E.getSource() == typed)
	    {
		// the 'enter' key in the send text field
		if (E.getKeyCode() == E.VK_ENTER) 
		    {
			// Is this a private communication?
			if (!sendToAll.getState())
			    {
				String wholist[];

				messages.append("*private to ");
				wholist = sendTo.getSelectedItems();
				for (int count = 0; count < wholist.length;
				     count ++)
				    {
					messages.append(wholist[count]);
					if (count < (wholist.length - 1))
					    messages.append(", ");
				    }
				messages.append("*> ");
			    }
			else
			    messages.append(name + "> ");

			// Print the rest.
			messages.append(typed.getText());

			if (connected == true)
			    try {
				// Send it.
				theClient.sendChatText(typed.getText());
			    }
			    catch (IOException e) {
				theClient.lostConnection();
				return;
			    }

			// Empty the typing and activity fields
			typed.setText("");
			activity.setText("");
			return;
		    }
		else
		    {
			if (!activity.getText().equals("typing: " + name))
			    activity.setText("typing: " + name);
			if (connected == true)
			    try {
				// Send a message to indicate that our user is
				// busy typing something
				theClient.sendActivity(OwnCommand
						       .ACTIVITY_TYPING);
			    }
			    catch (IOException e) {
				theClient.lostConnection();
				return;
			    }
			return;
		    }
	    }
    }

    public void keyTyped(KeyEvent E)
    {
    }   

    public void mouseClicked(MouseEvent E)
    {
    }   

    public void mouseEntered(MouseEvent E)
    {
    }   

    public void mouseExited(MouseEvent E)
    {
    }   

    public void mousePressed(MouseEvent E)
    {
    }   

    public void mouseReleased(MouseEvent E)
    {
    }   

    public void itemStateChanged(ItemEvent E)
    {
	

	// The 'sendToAll' checkbox
	if (E.getSource() == sendToAll)
	    {
		// If 'send to all' is selected, we should deselect all
		// the items in the sendTo list.
		if (sendToAll.getState())
		    {
			int items = sendTo.getRows();
			for (int count = 0; count < items; count ++)
			    sendTo.deselect(count);
			    userInfo.setEnabled(false);
		    }

		// Also make sure that this checkbox is selected if
		// nothing is selected in the sendTo window
		else
		    {
			if (sendTo.getSelectedItems().length == 0)
			    {
				sendToAll.setState(true);
				userInfo.setEnabled(false);
			    }
		    }
	    }

	// the 'sendTo' window
	if (E.getSource() == sendTo)
	    {
		if (sendTo.getSelectedItems().length == 0)
		    {
			// Nothing is selected in this list.  Make the
			// 'sendToAll' checkbox be checked.
			sendToAll.setState(true);
			userInfo.setEnabled(false);
		    }
		
		else
		    {
			// Don't allow "everyone" to be selected if any
			// individual users are selected
			sendToAll.setState(false);
			userInfo.setEnabled(true);
		    }
		return;
	    }

	
    }

    public void windowActivated(WindowEvent E)
    {
    }

    public void windowClosed(WindowEvent E)
    {
    }

    public void windowClosing(WindowEvent E)
    {
	if (connected == true)
	    disconnect();
	dispose();

	return;
    }

    public void windowDeactivated(WindowEvent E)
    {
    }

    public void windowDeiconified(WindowEvent E)
    {
    }

    public void windowIconified(WindowEvent E)
    {
    }

    public void windowOpened(WindowEvent E)
    {
    }

   
}
