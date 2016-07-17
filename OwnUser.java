

public class OwnUser
{
    // This object keeps all the relevant information about a user for either
    // the server or the client

    protected int id;
    protected String name;
    protected String password;

    OwnUser(OwnServer server)
    {
	// This constructor will be used by the server, since it automatically
	// assigns a new user Id

	id = server.getUserId();
	name = "newuser" + id;
	password = "";
	
    }

    OwnUser(int i, String nm, String pw)
    {
	// This constructor will be used by the client, with information
	// supplied by the server

	id = i;
	name = nm;
	password = pw;
	
    }

   
}
