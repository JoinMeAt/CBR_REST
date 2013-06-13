package util;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

public class Message extends BasicDBObject {
	private static final long serialVersionUID = -2967103316306771873L;

	public Message(String text, double latitude, double longitude, 
			String username, double txDistance) {
		put("text", text);
		put("latitude", latitude);
		put("longitude", longitude);
		put("username",username);
		put("transmitDistance", txDistance);
	}

}
