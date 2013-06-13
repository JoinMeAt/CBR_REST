package util;

import java.util.Map;

import org.bson.BSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class User extends BasicDBObject {
	private static final long serialVersionUID = -7065682189901550614L;
	
	public User() { }
	
	public User(String bson) {
		super();
		super.putAll((BSONObject) JSON.parse(bson));
	}
	
	public User(BSONObject b) {
		super();
		super.putAll(b);
	}
	
	public User(DBObject b) {
		super();
		super.putAll(b.toMap());
	}
	
	public User(Map m) {
		super();
		super.putAll(m);
	}

	public User (String username, String email, String password) {
		put("username", username);
		put("email",email);
		put("password",password);
	}
	
	public void setParams(String transmitDistance, String receiveDistance) {
		put("transmitDistance", transmitDistance);
		put("receiveDistance", receiveDistance);
	}
	
	public ObjectId getObjectId() {
		return ((ObjectId) get("_id"));
	}
	
	public String getId() {
		return ((ObjectId) get("_id")).toString();
	}

	public void setId(String id) {
		put("_id", new ObjectId(id));
	}

	public String getUsername() {
		return (String) get("username");
	}

	public void setUsername(String username) {
		put("username",username);
	}

	public String getEmail() {
		return (String) get("email");
	}

	public void setEmail(String email) {
		put("email", email);
	}

	public double getTransmitDistance() {
		return getDouble("transmitDistance");
	}

	public void setTransmitDistance(double transmitDistance) {
		put("transmitDistance",transmitDistance);
	}

	public Double getReceiveDistance() {
		return getDouble("receiveDistance");
	}

	public void setReceiveDistance(Double receiveDistance) {
		put("receiveDistance", receiveDistance);
	}
}
