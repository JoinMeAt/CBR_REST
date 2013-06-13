package service;

import java.net.UnknownHostException;

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
import util.Message;
import util.User;

import mongo.DBConnection;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Path("/message")
public class MessageSvc extends Application {

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/create")
	public String create(
			@FormParam("userID") String userID,
			@FormParam("text") String text,
			@FormParam("latitude") String lat,
			@FormParam("longitude") String lon
			) {
		
		double latitude = Double.parseDouble(lat);
		double longitude = Double.parseDouble(lon);
		
		if( latitude > 90 || latitude < -90 || longitude > 180 || longitude < -180 )
			return new CBRException(6).transformToXML();
		
		BasicDBObject query = null;
		DBObject tmp = null;
		
		try {
			DBCollection msgs = DBConnection.getCollection(CBR.COLLECTION_MESSAGES);
			msgs.setObjectClass(Message.class);
			
			DBCollection users = DBConnection.getCollection(CBR.COLLECTION_USERS);
			users.setObjectClass(User.class);
			User user = null;
			
			DBCollection actives = DBConnection.getCollection(CBR.COLLECTION_ACTIVE_USERS);
			actives.setObjectClass(ActiveUser.class);
			ActiveUser active = null;
			
			query = new BasicDBObject("_id",new ObjectId(userID));
			tmp = users.findOne(query);
			
			if( tmp == null )
				return new CBRException(2).transformToXML();
			user = new User(tmp);
			
			String username = user.getUsername();
			double txDistance = user.getTransmitDistance();
			
			query = new BasicDBObject(CBR.USERID, new ObjectId(userID));
			tmp = actives.findOne(query);
			
			if( tmp == null ) 
				return new CBRException(5).transformToXML();
			
			// update the user's current location
			active = new ActiveUser(tmp);
			active.put("latitude", latitude);
			active.put("longitude", longitude);
			// save updates to db
			actives.update(query,active);
			
			// create message
			Message msg = new Message(text, latitude, longitude, username, txDistance );
			// save message into db
			msgs.insert(msg);
			
			notifyOtherUsers(actives, msg, txDistance);
			
			return "true";		
			
		} catch (UnknownHostException e) {
			return new CBRException(0,e.getMessage()).transformToXML();
		} catch (IllegalArgumentException e) {
			return new CBRException(7).transformToXML();
		}
	}
	
	private void notifyOtherUsers(DBCollection actives, Message msg, double txDistance) {
		double radius = 6371000.0;
		double lonTx = msg.getDouble("longitude");
		double latTx = msg.getDouble("latitude");
		
		double dTheta = txDistance * 180 / Math.PI / radius; // average radius of the Earth in meters
		// query based on a rectangle then use the great circle distance to localize
		double latNorth = latTx + dTheta;
		double rNorth = latNorth > 90 ? 90 - latNorth : 0;
		double latSouth = latTx - dTheta;
		double rSouth = latSouth > 90 ? 90 + latSouth : 0;
		double lonEast = lonTx + dTheta;
		double rEast = lonEast > 180 ? lonEast - 180 : 0;
		double lonWest = lonTx - dTheta;
		double rWest = lonWest > 180 ? lonWest - 180 : 0;
		
		// North / South boundary
		if( rNorth != 0 ) { // wrapped north pole 

		} 
		
		if( rSouth != 0 ) { // wrapped south pole
			
		}
		
		// East / West boundary
		if( rEast != 0 ) { // wrap east
			
		} 
		
		if( rWest != 0 ) { /// wrap west
			
		}
		
		// Nominal case
		BasicDBObject query = new BasicDBObject("latitude", 
				new BasicDBObject("$lte", latNorth)
				.append("$gte", latSouth)
				.append("longitude", new BasicDBObject("$lte",lonEast)
				.append("$gte",lonWest)));
		
		DBCursor cursor = actives.find(query);
		
		while( cursor.hasNext() ) {
			User to = (User) cursor.next();
			// TODO Thread this
			double lonRx = to.getDouble("longitude");
			double latRx = to.getDouble("latitude");
			
			// haversine formula  where sin2(t/2) = (1-cos(t))/2
			double d =  2 * radius * Math.sqrt((1 - Math.cos(latRx-latTx)) / 2
					+ Math.cos(latTx) * Math.cos(latRx) 
					* (1 - Math.cos(lonRx-lonTx)) / 2);
			
			// sender transmitted far enough to hear the msg and  
			if( d <= txDistance && d <= to.getReceiveDistance() ) {
				sendMessageAlert(to, msg.getObjectId("_id").toString());
			}
		}
	}
	
	/**
	 * send GCM push notification to User to retrieve this message
	 * @param to - User to receive a message
	 * @param msgID - message ID for message to be retrieved
	 */
	private void sendMessageAlert(User to, String msgID) {
		// TODO thread this
		// store GCM/push Registration with User or ActiveUser?
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/retrieve")
	public String retrieve(
			@FormParam("userID") String userID,
			@FormParam("messageID") String messageID
			) {
			// TODO add input for current position and check against message 
		try {
			DBCollection actives = DBConnection.getCollection(CBR.COLLECTION_ACTIVE_USERS);
			BasicDBObject query = new BasicDBObject(CBR.USERID,new ObjectId(userID));
			DBObject tmp = actives.findOne(query);
			
			// receiving user does exist
			if( tmp == null ) {
				return new CBRException(2).transformToXML();
			}
			
			
			DBCollection msgs = DBConnection.getCollection(CBR.COLLECTION_MESSAGES);
			query = new BasicDBObject("_id", new ObjectId(messageID));
			DBObject msg = msgs.findOne(query);
			
			// message to receive doesn't exist
			if( msg == null ) {
				return new CBRException(4).transformToXML();
			}
			
			return msg.toString();
		} catch (UnknownHostException e) {
			return new CBRException(-1, e.getMessage()).transformToXML();
		} catch (IllegalArgumentException e) {
			return new CBRException(7).transformToXML();
		}
	}
}
