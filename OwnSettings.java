import java.awt.*;
import java.awt.event.*;


public class OwnSettings
    extends Dialog
    implements ActionListener, KeyListener, WindowListener
{
    private OwnWindow parentWindow;

    private Label nameLabel;
    private TextField name;
    private Label passwordLabel;
    private TextField password;
    private Label additionalLabel;
    private TextArea additional;
    private Label portLabel;
    private Label hostLabel;
    private TextField port;
    private TextField host;
    private Panel p1;

    private Button ok;
    private Button cancel;
    private Panel p2;

    private GridBagLayout myLayout;
    private GridBagConstraints myConstraints;


    OwnSettings(Frame parent)
    {
	super(parent, "Connection settings", true);

	parentWindow = (OwnWindow) parent;

	myLayout = new GridBagLayout();
	myConstraints = new GridBagConstraints();
	myConstraints.insets = new Insets(0, 5, 0, 5);
	setLayout(myLayout);

	p1 = new Panel();
	p1.setLayout(myLayout);

	// set up the 'name' field

	nameLabel = new Label("Your user name :");
	myConstraints.gridx = 0; myConstraints.gridy = 0;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.anchor = myConstraints.EAST;
	myLayout.setConstraints(nameLabel, myConstraints);
	p1.add(nameLabel);

	name = new TextField(20);
	name.addKeyListener(this);
	name.setText(parentWindow.name);
	myConstraints.gridx = 1; myConstraints.gridy = 0;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.anchor = myConstraints.WEST;
	myLayout.setConstraints(name, myConstraints);
	p1.add(name);

	// Password field
	passwordLabel = new Label("Your password :");
	passwordLabel.setVisible(parentWindow.requirePassword);
	myConstraints.gridx = 0; myConstraints.gridy = 1;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
	myConstraints.anchor = myConstraints.EAST;
	myLayout.setConstraints(passwordLabel, myConstraints);
	p1.add(passwordLabel);

	password = new TextField(20);
	password.setVisible(parentWindow.requirePassword);
	password.setEchoChar(new String("*").charAt(0));
	password.setText(parentWindow.plainPassword);
	password.addKeyListener(this);
	myConstraints.gridx = 1; myConstraints.gridy = 1;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
	myConstraints.anchor = myConstraints.WEST;
	myLayout.setConstraints(password, myConstraints);
	p1.add(password);

	

	hostLabel = new Label("Server name :");
	myConstraints.gridx = 0; myConstraints.gridy = 5;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.anchor = myConstraints.EAST;
	myLayout.setConstraints(hostLabel, myConstraints);
	p1.add(hostLabel);

	host = new TextField(20);
	host.addKeyListener(this);
	host.setText(parentWindow.host);
	myConstraints.gridx = 1; myConstraints.gridy = 5;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.anchor = myConstraints.WEST;
	myLayout.setConstraints(host, myConstraints);
	p1.add(host);

	portLabel = new Label("Network port :");
	myConstraints.gridx = 0; myConstraints.gridy = 6;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.anchor = myConstraints.EAST;
	myLayout.setConstraints(portLabel, myConstraints);
	p1.add(portLabel);

	port = new TextField(20);
	port.addKeyListener(this);
	port.setText(parentWindow.port);
	myConstraints.gridx = 1; myConstraints.gridy = 6;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.anchor = myConstraints.WEST;
	myLayout.setConstraints(port, myConstraints);
	p1.add(port);

	if (parentWindow.connected)
	    {
		name.setEnabled(false);
		password.setEnabled(false);
		host.setEnabled(false);
		port.setEnabled(false);
	    }
	else
	    {
		name.setEnabled(true);
		password.setEnabled(true);
		host.setEnabled(true);
		port.setEnabled(true);
	    }


	additionalLabel = new Label("Additional info (optional) :");
	myConstraints.gridx = 0; myConstraints.gridy = 8;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.anchor = myConstraints.EAST;
	myLayout.setConstraints(additionalLabel, myConstraints);
	p1.add(additionalLabel);

	additional = new TextArea(5,20);
	additional.addKeyListener(this);
	additional.setText(parentWindow.additional);
	myConstraints.gridx = 1; myConstraints.gridy = 8;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.anchor = myConstraints.WEST;
	myLayout.setConstraints(additional, myConstraints);
	p1.add(additional);

	// set up the panel

	myConstraints.gridx = 0; myConstraints.gridy = 0;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
	myLayout.setConstraints(p1, myConstraints);
	add(p1);

	// set up a panel for the buttons

	p2 = new Panel();
	p2.setLayout(myLayout);

	ok = new Button("Ok");
	ok.addActionListener(this);
	ok.addKeyListener(this);
	myConstraints.gridx = 0; myConstraints.gridy = 0;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.insets = new Insets(5, 5, 5, 0);
	myLayout.setConstraints(ok, myConstraints);
	p2.add(ok);

	cancel = new Button("Cancel");
	cancel.addActionListener(this);
	cancel.addKeyListener(this);
	myConstraints.gridx = 1; myConstraints.gridy = 0;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
        myConstraints.insets = new Insets(5, 0, 5, 5);
	myLayout.setConstraints(cancel, myConstraints);
	p2.add(cancel);

	// set up the panel

	myConstraints.gridx = 0; myConstraints.gridy = 1;
	myConstraints.gridheight = 1; myConstraints.gridwidth = 1;
	myLayout.setConstraints(p2, myConstraints);
	add(p2);

	setSize(200,200);
	pack();
	setLocation((((((parentWindow.getBounds()).width)
		       - ((getSize()).width)) / 2)
		     + ((parentWindow.getLocation()).x)),
		    (((((parentWindow.getBounds()).height)
		       - ((getSize()).height)) / 2)
		     + ((parentWindow.getLocation()).y)));

	addKeyListener(this);
	addWindowListener(this);
	setVisible(true);
	name.requestFocus();
    }

    private void setValues()
    {
	parentWindow.name = name.getText();
	parentWindow.userId.setText(parentWindow.name);
	parentWindow.port = port.getText();
	parentWindow.host = host.getText();
	parentWindow.additional = additional.getText();

	parentWindow.plainPassword = password.getText();
	
	    
	System.out.println("setting values");
    }

    public void actionPerformed(ActionEvent E)
    {
	if (E.getSource() == ok)
	    {
		setValues();
		dispose();
		return;
	    }

	else if (E.getSource() == cancel)
	    {
		dispose();
		return;
	    }
    }

    public void keyPressed(KeyEvent E)
    {
    }

    public void keyReleased(KeyEvent E)
    {
	if (E.getKeyCode() == E.VK_ENTER)
	    {
		if ((E.getSource() == ok) ||
		    (E.getSource() == name) ||
		    (E.getSource() == password) ||
		    (E.getSource() == port) ||
		    (E.getSource() == host))
		    {
			setValues();
			dispose();
			return;
		    }

		else if (E.getSource() == cancel)
		    {
			dispose();
			return;
		    }
	    }

	else if (E.getSource() == additional)
	    {
		if (E.getKeyCode() == E.VK_TAB)
		    additional.transferFocus();
	    }
    }

    public void keyTyped(KeyEvent E)
    {
    }   

    public void windowActivated(WindowEvent E)
    {
    }

    public void windowClosed(WindowEvent E)
    {
    }

    public void windowClosing(WindowEvent E)
    {
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
