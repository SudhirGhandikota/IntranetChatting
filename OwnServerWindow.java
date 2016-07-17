import java.awt.*;
import java.awt.event.*;
import javax.swing.ImageIcon;
import java.io.*;
import java.net.*;


public class OwnServerWindow
    extends Frame
    implements ActionListener, ItemListener, WindowListener
{
    protected GridBagLayout myLayout;
    protected GridBagConstraints myConstraints;
    protected Label listening;
    protected List userList;
    protected Button disconnect;
    protected Button disconnectAll;
    protected Button console;
    protected Button userAdmin;
    protected Checkbox logChat;
    protected Button shutdown;
    protected TextField stats;
    protected TextArea logWindow;
    protected OwnServer myParent;
    protected OwnWindow consoleWindow;


    public OwnServerWindow(OwnServer parent, String Name)
    {
	super(Name);
	myParent = parent;

	myLayout = new GridBagLayout();
	myConstraints = new GridBagConstraints();
	setLayout(myLayout);

	myConstraints.insets.top = 0; myConstraints.insets.bottom = 0;
	myConstraints.insets.right = 5; myConstraints.insets.left = 5;
	myConstraints.anchor = myConstraints.WEST;
	myConstraints.fill = myConstraints.BOTH;

	listening = new Label("Listening on port " + myParent.port);
	myConstraints.gridwidth = 1; myConstraints.gridheight = 1;
	myConstraints.gridx = 0; myConstraints.gridy = 0;
	myConstraints.weightx = 0; myConstraints.weighty = 0;
	myLayout.setConstraints(listening, myConstraints);
	add(listening);

	userList = new List(4, false);
	myConstraints.gridwidth = 1; myConstraints.gridheight = 5;
	myConstraints.gridx = 0; myConstraints.gridy = 1;
	myConstraints.weightx = 1; myConstraints.weighty = 0;
	myLayout.setConstraints(userList, myConstraints);
	add(userList);

	logChat = new Checkbox("Log chat(s)", myParent.logChats);
	logChat.addItemListener(this);
	myConstraints.gridwidth = 1; myConstraints.gridheight = 1;
	myConstraints.gridx = 1; myConstraints.gridy = 0;
	myConstraints.weightx = 0; myConstraints.weighty = 0;
	myLayout.setConstraints(logChat, myConstraints);
	logChat.setEnabled(true);
	add(logChat);

	userAdmin = new Button("User management");
	userAdmin.addActionListener(this);
	myConstraints.gridwidth = 1; myConstraints.gridheight = 1;
	myConstraints.gridx = 1; myConstraints.gridy = 1;
	myConstraints.weightx = 0; myConstraints.weighty = 0;
	myLayout.setConstraints(userAdmin, myConstraints);
	userAdmin.setEnabled(true);
	add(userAdmin);

	console = new Button("Administrator client");
	console.addActionListener(this);
	myConstraints.gridwidth = 1; myConstraints.gridheight = 1;
	myConstraints.gridx = 1; myConstraints.gridy = 2;
	myConstraints.weightx = 0; myConstraints.weighty = 0;
	myLayout.setConstraints(console, myConstraints);
	console.setEnabled(true);
	add(console);

	disconnect = new Button("Disconnect user");
	disconnect.addActionListener(this);
	myConstraints.gridwidth = 1; myConstraints.gridheight = 1;
	myConstraints.gridx = 1; myConstraints.gridy = 3;
	myConstraints.weightx = 0; myConstraints.weighty = 0;
	myLayout.setConstraints(disconnect, myConstraints);
	disconnect.setEnabled(false);
	add(disconnect);

	disconnectAll = new Button("Disconnect all");
	disconnectAll.addActionListener(this);
	myConstraints.gridwidth = 1; myConstraints.gridheight = 1;
	myConstraints.gridx = 1; myConstraints.gridy = 4;
	myConstraints.weightx = 0; myConstraints.weighty = 0;
	myLayout.setConstraints(disconnectAll, myConstraints);
	disconnectAll.setEnabled(false);
	add(disconnectAll);

	shutdown = new Button("Shut down");
	shutdown.addActionListener(this);
	myConstraints.gridwidth = 1; myConstraints.gridheight = 1;
	myConstraints.gridx = 1; myConstraints.gridy = 5;
	myConstraints.weightx = 0; myConstraints.weighty = 0;
	myLayout.setConstraints(shutdown, myConstraints);
	shutdown.setEnabled(true);
	add(shutdown);

	myConstraints.insets.top = 5; myConstraints.insets.bottom = 5;

	stats =
	    new TextField("Connections - current: 0  peak: 0  total: 0", 40);
	stats.setEditable(false);
	myConstraints.gridwidth = 2; myConstraints.gridheight = 1;
	myConstraints.gridx = 0; myConstraints.gridy = 7;
	myConstraints.weightx = 0; myConstraints.weighty = 0;
	myLayout.setConstraints(stats, myConstraints);
	add(stats);

	logWindow = new TextArea("Server activity log:\n", 20, 40,
				 TextArea.SCROLLBARS_VERTICAL_ONLY);
	logWindow.setEditable(false);
	myConstraints.gridwidth = 2; myConstraints.gridheight = 1;
	myConstraints.gridx = 0; myConstraints.gridy = 8;
	myConstraints.weightx = 1; myConstraints.weighty = 1;
	myLayout.setConstraints(logWindow, myConstraints);
	add(logWindow);

	addWindowListener(this);
        System.out.println("last");
    }

    
    public void updateStats()
    {
	// This just updates any statistics that are shown on the face of
	// the server window
	stats.setText("Connections - current: " +
		      myParent.currentConnections + "  peak: " +
		      myParent.peakConnections + "  total: " +
		      myParent.totalConnections);
	return;
    }


    public void actionPerformed(ActionEvent E)
    {
	if (E.getSource() == userAdmin)
	    {
		OwnUserToolDialog userTool =
		    new OwnUserToolDialog(this);
		return;
	    }

	if (E.getSource() == console)
	    {
		OwnInfoDialog tmp =
		    new OwnInfoDialog(this, "Loading", false,
			  "Starting the client, one moment please...");
		consoleWindow =
		    new OwnWindow("Administrator", "", "localhost",
				      Integer.toString(myParent.port),
				      myParent.myURL);
		tmp.dispose();

		//consoleWindow.adminConsole = true;
		//consoleWindow.lockSettings = true;
		consoleWindow.requirePassword = false;

		// Show the window
		consoleWindow.show();

		// Connect
		consoleWindow.connect();

		return;
	    }

	if (E.getSource() == disconnect)
	    {
		String disconnectUser;

		synchronized (userList) {
		    disconnectUser = userList.getSelectedItem();
		}

		if (disconnectUser != null)
		    {
			// Loop through all of the current connections to find
			// the object that corresponds to this name
			
			for (int count = 0;
			     count < myParent.currentConnections;
			     count ++)
			    {
				OwnClientSocket tempuser =
				    (OwnClientSocket)
				    myParent.connections.elementAt(count);
				
				if (tempuser.user.name.equals(disconnectUser))
				    {
					myParent.disconnect(tempuser, true);
					break;
				    }
			    }
		    }
		return;
	    }

	if (E.getSource() == disconnectAll)
	    {
		myParent.disconnectAll(true);
		return;
	    }

	if (E.getSource() == shutdown)
	    {
		 myParent.shutdown();
		return;
	    }
    }

    public void itemStateChanged(ItemEvent E)
    {
	// The 'log chat' checkbox
	if (E.getSource() == logChat)
	    {
		myParent.logChats = logChat.getState();
		
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
	if (myParent.currentConnections > 0)
	    dispose();
	else
	    myParent.shutdown();
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


