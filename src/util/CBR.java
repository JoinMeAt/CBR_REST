package util;

public class CBR {

	public static String DATABASE = "127.0.0.1";
	public static int PORT = 27017;
	
	public static String CBR_MONGO_DB = "cbr";
	public static String COLLECTION_USERS = "users";
	public static String COLLECTION_ACTIVE_USERS = "actives";
	public static String COLLECTION_MESSAGES = "messages";
	
	public static String LAT = "latitude";
	public static String LONG = "longitude";
	public static String USERID = "userID";
	
	public static void assertNotNull(Object o) throws NullPointerException { 
		if( o == null ) {
			throw new NullPointerException();
		}
	}
}
