package org.suai.abilities;


import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import org.telegram.telegrambots.logging.BotLogger;

import java.net.UnknownHostException;

import java.io.Closeable;

import java.util.Random;



public class DataBase implements Closeable {

	private MongoClient mongo;
	private DB db;

	private Random randomGenerator;

	public static final String DB_NAME = "BotDataBase";


	public DataBase() throws UnknownHostException {
		this.mongo = new MongoClient();
		this.db = this.mongo.getDB(DB_NAME);

		this.randomGenerator = new Random();
	}


	public DB getDB() {
		return this.db;
	}


	public void addRecord(String collectionName, String ... content) {
		if(content.length == 0 || (content.length & 1) != 0) {
			throw new IllegalArgumentException("Length of content must be positive even number.");
		}

		DBCollection collection = this.db.getCollection(collectionName);

		BasicDBObject oldDocument = new BasicDBObject(content[0], content[1]);

		BasicDBObject document = new BasicDBObject(content[0], content[1]);
		for(int i = 2; i < content.length; i+=2) {
			document.append(content[i], content[i + 1]);
		}

		collection.update(oldDocument, document, true, false);

		BotLogger.info(getClass().getName(),
				"Refresh record in collection \"" + collectionName + "\"");
	}


	public Object findRecordParameter(String collectionName, String keyExample, String valueExample, String keyQuery) {
		DBCollection collection = this.db.getCollection(collectionName);

		BasicDBObject searchQuery = new BasicDBObject(keyExample, valueExample);
		DBObject document = collection.findOne(searchQuery);

		if(document == null) {
			return null;
		}

		Object parameterQuery = document.get(keyQuery);

		BotLogger.info(getClass().getName(),
				"Get query parameter from \"" + collectionName +
				"\" (parameter: " + parameterQuery + ")");

		return parameterQuery;
	}


	public void deleteRecord(String collectionName, String keyExample, String valueExample) {
		DBCollection collection = this.db.getCollection(collectionName);

		BasicDBObject searchQuery = new BasicDBObject(keyExample, valueExample);
		collection.remove(searchQuery);

		BotLogger.info(getClass().getName(),
				"Delete record in collection \"" + collectionName + "\"");
	}


	public Object getRandomRecordParameter(String collectionName, String keyQuery) {
		DBCollection collection = this.db.getCollection(collectionName);
		int count = (int) collection.getCount();
		int randomIndex = this.randomGenerator.nextInt(count);

		DBObject randomDocument = collection.find().limit(1).skip(randomIndex).next();
		Object parameterQuery = randomDocument.get(keyQuery);

		BotLogger.info(getClass().getName(),
				"Get random parameter from \"" + collectionName +
				"\" (parameter: " + parameterQuery + ")");

		return parameterQuery;
	}


	public void close() {
		this.mongo.close();
	}

}