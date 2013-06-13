package service;

import java.net.UnknownHostException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.bson.types.ObjectId;

import util.ActiveUser;
import util.CBR;
import util.CBRException;
import util.User;

import mongo.DBConnection;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

@Path("/user")
public class UserSvc extends Application {
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/create")
	public String create(
			@FormParam("username") String username,
			@FormParam("email") String email,
			@FormParam("password") String password) {
		String response = null;
		
		try {
			DBCollection users = DBConnection.getCollection(CBR.COLLECTION_USERS);
			
			// verify user doesn't already exist with this username or email address
			DBObject u1 = users.findOne(new BasicDBObject("username", username));
			DBObject u2 = users.findOne(new BasicDBObject("email", email));
			
			if( u1 != null || u2 != null ) {
				response = new CBRException(1).transformToXML();
			} else {	// create the new user
				User user = new User(username,email,password);
				user.put("transmitDistance",250);
				user.put("receiveDistance", 1000);
				users.insert(user);	
				String userID = user.get("_id").toString();
				CBR.assertNotNull(userID);
				response = userID;
			}
		} catch (UnknownHostException e) {
			response = new CBRException(0, e.getMessage()).transformToXML();
		} catch (NullPointerException e) {
			response = new CBRException(0,"Mongo: Unable to create User.").transformToXML();
		}
		
		return response;
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/login")
	public String login(
			@FormParam("email") String email,
			@FormParam("password") String password,
			@DefaultValue("8008.5") @FormParam("latitude") String latitude,
			@DefaultValue("8008.5") @FormParam("longitude") String longitude) {
		String response = null;

		double lat = Double.parseDouble(latitude);
		double lon = Double.parseDouble(longitude);
		
		try {
			DBCollection users = DBConnection.getCollection(CBR.COLLECTION_USERS);
			users.setObjectClass(User.class);
			
			BasicDBObject query = new BasicDBObject("email",email)
					.append("password", password);
			DBObject tmp = users.findOne(query);
			
			if( tmp != null  ) {
				// pull userID
				User user = new User(tmp);
				
				String userID = user.get("_id").toString();
				CBR.assertNotNull(userID);
				
				ActiveUser active = null;
				if( lat == 8008.5 && lon == 8008.5)
					active = new ActiveUser(user.getObjectId(), 8008.5, 8008.5);
				else {					
					if( lat > 90 || lat < -90 || lon > 180 || lon < -180 )
						return new CBRException(6).transformToXML();
					
					active = new ActiveUser(user.getObjectId(), lat, lon );
				}
									
				DBCollection actives = DBConnection.getCollection(CBR.COLLECTION_ACTIVE_USERS);
				query = new BasicDBObject(CBR.USERID,user.getObjectId());
				tmp = actives.findOne(query);
				
				if( tmp == null )
					actives.insert(active);
				else 
					actives.update(query, active);
								
				user.remove("password"); // don't send password back
				return user.toString();
			} else {
				response = new CBRException(2).transformToXML();
			}
		} catch (UnknownHostException e) {
			return new CBRException(0, e.getMessage()).transformToXML();
		} catch( NullPointerException e) {
			return new CBRException(0, "Unable to retrieve User").transformToXML();
		}
		
		return response;
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/logout")
	public String logout(
			@FormParam("userID") String userID
			) {
		
		try {
			DBCollection actives = DBConnection.getCollection(CBR.COLLECTION_ACTIVE_USERS);
			
			BasicDBObject query = new BasicDBObject(CBR.USERID,new ObjectId(userID));
			DBObject active = actives.findOne(query);
			
			if( active == null )
				return "true";
			
			WriteResult result = actives.remove(query);
			
			if( result.getN() > 0 ) // just == 1 ?
				return String.valueOf(result.getN());
			
			
			
		} catch (UnknownHostException e) {
			return new CBRException(0, e.getMessage()).transformToXML();
		} catch (IllegalArgumentException e) {
			return new CBRException(7).transformToXML();
		}
		
		return "false";
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/update")
	public String update(
			@FormParam("userID") String userID,
			@DefaultValue("0") @FormParam("updateParams") String updateParams
			) {
		String response = null;
		
		if( "0".equals(updateParams) ) { // no parameters specified
			return new CBRException(3).transformToXML();
		}
		
		try {
			DBCollection users = DBConnection.getCollection(CBR.COLLECTION_USERS);
			users.setObjectClass(User.class);
			// TODO verify user exists first?
			BasicDBObject query = new BasicDBObject("_id",new ObjectId(userID));
			BasicDBObject update = new BasicDBObject("$set", (BasicDBObject) JSON.parse(updateParams));
			users.update(query, update);
			
			
		} catch (UnknownHostException e) {
			response = new CBRException(-1, e.getMessage()).transformToXML();
		} catch( NullPointerException e) {
			response = new CBRException(0, "Unable to retrieve User").transformToXML();
		} catch (IllegalArgumentException e) {
			return new CBRException(7).transformToXML();
		}
		
		return response;
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/update/position")
	public String updatePosition(
			@FormParam("userID") String userID,
			@FormParam("latitude") String latitude,
			@FormParam("longitude") String longitude
			) {
		
		double lat = Double.valueOf(latitude);
		double lon = Double.valueOf(longitude);
		
		if( lat > 90 || lat < -90 || lon > 180 || lon < -180 )
			return new CBRException(6).transformToXML();
		
		try {
			DBCollection actives = DBConnection.getCollection(CBR.COLLECTION_ACTIVE_USERS);
			actives.setObjectClass(ActiveUser.class);
			BasicDBObject query = new BasicDBObject(CBR.USERID, new ObjectId(userID));
			DBObject tmp = actives.findOne(query);
			
			if( tmp == null )
				return new CBRException(5).transformToXML();
			
			ActiveUser user = new ActiveUser(tmp);
			user.put("latitude", lat);
			user.put("longitude", lon);
			
			actives.update(query, user);
			
			return "true";			
		} catch (UnknownHostException e) {
			return new CBRException(-1, e.getMessage()).transformToXML();
		} catch (IllegalArgumentException e) {
			return new CBRException(7).transformToXML();
		}
	}
}
