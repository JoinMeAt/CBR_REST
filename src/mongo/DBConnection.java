package mongo;

import java.net.UnknownHostException;

import util.CBR;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class DBConnection {

	public static DB getDB(String db) throws UnknownHostException {
		Mongo m = new Mongo(CBR.DATABASE, CBR.PORT);
		return m.getDB(db);
	}
	
	public static DBCollection getCollection(String coll) throws UnknownHostException {
		Mongo m = new Mongo(CBR.DATABASE, CBR.PORT);
		return m.getDB(CBR.CBR_MONGO_DB).getCollection(coll);
	}
}
