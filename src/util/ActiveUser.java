package util;

import java.util.Map;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ActiveUser extends BasicDBObject {
	private static final long serialVersionUID = 4966314991762682309L;

	public ActiveUser(ObjectId userID, double latitude, double longitude) {
		put("userID", userID);
		put(CBR.LAT, latitude);
		put(CBR.LONG, longitude);
	}
	
	public ActiveUser() { }
	
	public ActiveUser(BSONObject b) {
		super();
		super.putAll(b);
	}
	
	public ActiveUser(DBObject b) {
		super();
		super.putAll(b.toMap());
	}
	
	public ActiveUser(Map m) {
		super();
		super.putAll(m);
	}
}
