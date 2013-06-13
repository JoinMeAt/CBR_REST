package util;

import com.thoughtworks.xstream.XStream;

public class CBRException extends Throwable {
	private static final long serialVersionUID = -7230814401293547513L;
	
	private int id;
	private String description;
	
	public CBRException() {}
	public CBRException(int _id) {
		id = _id;
		description = getDescription(_id);
	}
	
	public CBRException(int _id, String _description) {
		id = _id;
		description = _description;
	}
	
	public int getID() { return id; }
	public String getDescription() { return description; }

	
	public String getDescription(int id) {
		String desc = null;
		
		switch(id) {
			case 0:
				desc = "General MongoDB error";
				break;
			case 1:
				desc = "User already exists";
				break;
			case 2:
				desc = "No such user";
				break;
			case 3:
				desc = "No parameters to update";
				break;
			case 4:
				desc = "Message ID doesn't exist";
				break;
			case 5:
				desc = "User not active";
				break;
			case 6:
				desc = "Malformed coordinates";
				break;
			case 7:
				desc = "Malformed ObjectId";
				break;
				
			
			
			
			default:
				desc = "General CBR error";
				
		}
		
		return desc;
	}
	
	public static CBRException transformFromXML(String xml) {
		if( !xml.startsWith("<") )
			return null;
		
		XStream x = new XStream();
		x.alias("CBRException", CBRException.class);
		return (CBRException) x.fromXML(xml);
	}
	
	public String transformToXML() {
		XStream x = new XStream();
		x.alias("CBRException",CBRException.class);
		return x.toXML(this);
	}
}
