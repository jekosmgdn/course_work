package org.suai.abilities;


import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.api.objects.media.InputMedia;
import org.telegram.telegrambots.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.logging.BotLogger;

import com.vdurmont.emoji.EmojiParser;

import java.net.HttpURLConnection;

import java.io.InputStreamReader;
import java.io.IOException;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.ArrayList;

import org.suai.constants.ConnectionConstants;


public class Gallery implements ConnectionConstants {

	private final int MAX_PHOTO = 10;

	public static final String ERROR_PARSING = 
			EmojiParser.parseToUnicode(":alien: Хммм, а куда делась галерея?");


	public Object getListGallery(String link, int count) {
		try {
			HttpURLConnection connection = getURLConnection(link);

			JSONObject root = (JSONObject) getRoot(connection);
			ArrayList<Object[]> list = parseList(root, count);

			return list;
		}
		catch(IOException | ParseException exception) {
			return handleWarning(exception);
		}
	}


	private SendMessage handleWarning(Exception exception) {
		BotLogger.warning(getClass().getName(), exception);

		SendMessage message = new SendMessage();
		message.setText(ERROR_PARSING);

		return message;
	}


	private Object getRoot(HttpURLConnection connection) throws IOException, ParseException {
		Object root = null;

		try(
			InputStreamReader reader = new InputStreamReader(connection.getInputStream());
		) {
			JSONParser parser = new JSONParser();
			root = parser.parse(reader);
		}

		return root;
	}


	private ArrayList<Object[]> parseList(JSONObject root, int count) {
		ArrayList<Object[]> list = new ArrayList<>();

		JSONArray galleries = (JSONArray) root.get("headlines");

		for(int i = 0; i < galleries.size() && count != 0; i++) {
			JSONObject element = (JSONObject)  galleries.get(i);
			JSONObject links = (JSONObject) element.get("links");
			String linkSelf = (String) links.get("self");

			Object[] gallery = getGallery(linkSelf);

			if(gallery[1] == null) {
				continue;
			}

			list.add(gallery);

			count--;
		}

		return list;
	}


	private Object[] getGallery(String link) {
		Object[] gallery = null;

		try {
			HttpURLConnection connection = getURLConnection(link);
 
			JSONObject root = (JSONObject) getRoot(connection);
			SendMediaGroup media = new SendMediaGroup();
			gallery = parseGallery(root, MAX_PHOTO);

			media.setMedia((List<InputMedia>) gallery[1]);

			gallery[1] = media;
		}
		catch(IOException | ParseException exception) {}

		return gallery;
	}


	private Object[] parseGallery(JSONObject root, int count) {
		Object result[] = new Object[2];
		List<InputMedia> list = new ArrayList<>();

		JSONObject topic = (JSONObject) root.get("topic");

		JSONObject headline = (JSONObject) topic.get("headline");
		JSONObject info = (JSONObject) headline.get("info");

		JSONObject gallery = (JSONObject) topic.get("gallery");
		JSONArray content = (JSONArray) gallery.get("content");

		StringBuilder title = new StringBuilder();
		title.append(info.get("title")).append(". ")
			 .append(info.get("rightcol")).append(". ")
			 .append(info.get("announce")).append(".");

		for(int i = 0; i < content.size() && count != 0; i++) {
			JSONObject element = (JSONObject) content.get(i);
			String imageCaption = (String) element.get("caption");
			String imageURL = (String) element.get("image_url");

			list.add((InputMedia) new InputMediaPhoto(imageURL, imageCaption));

			count--;
		}

		SendMessage sendMessage = new SendMessage();
		sendMessage.setText(title.toString());

		result[0] = sendMessage;
		result[1] = list;

		return result;
	}

}