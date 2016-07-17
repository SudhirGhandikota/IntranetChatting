
public class OwnMessage
{
    public String messageFor;
    public String messageFrom;
    public String text;

    public OwnMessage(String whoFor, String whoFrom, String info)
    {
	messageFor = whoFor;
	messageFrom = whoFrom;
	text = info;
    }
}

