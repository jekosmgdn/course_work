package org.suai.abilities;


import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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


public class NewsList implements ConnectionConstants {

	public static final String ERROR_PARSING = 
			EmojiParser.parseToUnicode(":anguished: Не могу найти популярные новости.");


	public ArrayList<SendMessage> getListNews(String link, int count) {
		try {
			HttpURLConnection connection = getURLConnection(link);

			JSONObject root = (JSONObject) getRoot(connection);
			ArrayList<SendMessage> list = parseList(root, count);

			return list;
		}
		catch(IOException | ParseException exception) {
			return handleWarning(exception);
		}
	}


	private ArrayList<SendMessage> handleWarning(Exception exception) {
			BotLogger.warning(getClass().getName(), exception);

			ArrayList<SendMessage> listError = new ArrayList<>();

			SendMessage message = new SendMessage();
			message.setText(ERROR_PARSING);

			listError.add(message);

			return listError;
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


	private ArrayList<SendMessage> parseList(JSONObject root, int count) {
		ArrayList<SendMessage> list = new ArrayList<>();

		JSONArray jsonArray = (JSONArray) root.get("headlines");

		for(int i = 0; i < jsonArray.size() && count != 0; i++) {
			SendMessage message = new SendMessage();

			JSONObject element = (JSONObject) jsonArray.get(i);
			JSONObject links = (JSONObject) element.get("links");

			String title = parseTitle(element);
			message.setText(title);

			InlineKeyboardMarkup inlineKeyboard = createInlineKeyboard("Получить новость", (String) links.get("self"));
			message.setReplyMarkup(inlineKeyboard);

			list.add(message);

			count--;
		}

		return list;
	}


	private String parseTitle(JSONObject element) {
		StringBuilder title = new StringBuilder();

		JSONObject info = (JSONObject) element.get("info");
		JSONObject imageURL = (JSONObject) element.get("title_image");

		title.append((String) info.get("title")).append(". ")
			 .append((String) info.get("rightcol")).append(". ");

		if(imageURL != null) {
			title.append((String) imageURL.get("url"));
		}

		return title.toString();
	}


	private InlineKeyboardMarkup createInlineKeyboard(String textButton, String callbackData) {
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();

		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> rowInline = new ArrayList<>();

		rowInline.add(new InlineKeyboardButton().setText(textButton).setCallbackData(callbackData));
		rowsInline.add(rowInline);

		markupInline.setKeyboard(rowsInline);

		return markupInline;
	}

}