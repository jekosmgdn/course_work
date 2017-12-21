package org.suai.test;


import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;

import java.net.UnknownHostException;

import org.suai.abilities.DataBase;


public class DataBaseTest {

	private static DataBase dataBase;
	private static DB db;

	private static final String TEST_COLLECTION = "test";

	private static final String FIRST_KEY = "first-key";
	private static final String FIRST_VALUE = "www.test.org";

	private static final String SECOND_KEY = "second-key";
	private static final String SECOND_VALUE = "org.test.www";


	@BeforeClass
	public static void init() {
		try {
			dataBase = new DataBase();
			db = dataBase.getDB();
		}
		catch(UnknownHostException exception) {
			fail(exception.getMessage());
		}
	}


	@AfterClass
	public static void destroy() {
		DBCollection collection = db.getCollection(TEST_COLLECTION);
		collection.drop();

		dataBase.close();
	}


	@Test
	public void addRecordTest() {
		dataBase.addRecord(TEST_COLLECTION,
				FIRST_KEY, FIRST_VALUE,
				SECOND_KEY, SECOND_VALUE);

		DBCollection collection = db.getCollection(TEST_COLLECTION);
		BasicDBObject document = new BasicDBObject(FIRST_KEY, FIRST_VALUE)
				.append(SECOND_KEY, SECOND_VALUE);

		long count = collection.count(document);

		assertEquals(count, 1);
	}


	@Test
	public void findRecordParameterTest() {
		createTempRecord(TEST_COLLECTION, FIRST_KEY,
				FIRST_VALUE, SECOND_KEY, SECOND_VALUE);

		String secondValue = (String) dataBase.findRecordParameter(TEST_COLLECTION,
				FIRST_KEY, FIRST_VALUE,
				SECOND_KEY);
		assertEquals(secondValue, SECOND_VALUE);

		String firstValue = (String) dataBase.findRecordParameter(TEST_COLLECTION,
				SECOND_KEY, SECOND_VALUE,
				FIRST_KEY);
		assertEquals(firstValue, FIRST_VALUE);

		String notExistingValue = (String) dataBase.findRecordParameter(TEST_COLLECTION,
				"random", "value",
				"notExist");
		assertNull(notExistingValue);
	}


	@Test
	public void deleteRecordTest() {
		DBCollection collection = db.getCollection(TEST_COLLECTION);
		BasicDBObject document = new BasicDBObject(FIRST_KEY, FIRST_VALUE)
				.append(SECOND_KEY, SECOND_VALUE);

		createTempRecord(TEST_COLLECTION, FIRST_KEY,
				FIRST_VALUE, SECOND_KEY, SECOND_VALUE);
		dataBase.deleteRecord(TEST_COLLECTION, FIRST_KEY, FIRST_VALUE);

		long count = collection.count(document);
		assertEquals(count, 0);

		createTempRecord(TEST_COLLECTION, FIRST_KEY,
				FIRST_VALUE, SECOND_KEY, SECOND_VALUE);
		dataBase.deleteRecord(TEST_COLLECTION, SECOND_KEY, SECOND_VALUE);

		count = collection.count(document);
		assertEquals(count, 0);
	}


	private void createTempRecord(String nameCollection, String firstKey,
	String firstValue, String secondKey, String secondValue) {

		DBCollection collection = db.getCollection(nameCollection);

		BasicDBObject oldDocument = new BasicDBObject(firstKey, firstValue);
		BasicDBObject document = new BasicDBObject(firstKey, firstValue)
				.append(secondKey, secondValue);

		collection.update(oldDocument, document, true, false);
	}

}