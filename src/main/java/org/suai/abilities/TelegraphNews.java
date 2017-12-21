package org.suai.abilities;


import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegraph.ExecutorOptions;
import org.telegram.telegraph.TelegraphContext;
import org.telegram.telegraph.TelegraphContextInitializer;
import org.telegram.telegraph.api.methods.GetAccountInfo;
import org.telegram.telegraph.api.methods.CreatePage;
import org.telegram.telegraph.api.objects.Account;
import org.telegram.telegraph.api.objects.Page;
import org.telegram.telegraph.api.objects.Node;
import org.telegram.telegraph.api.objects.NodeElement;
import org.telegram.telegraph.api.objects.NodeText;
import org.telegram.telegraph.exceptions.TelegraphException;

import com.vdurmont.emoji.EmojiParser;

import java.net.HttpURLConnection;

import java.io.InputStreamReader;
import java.io.IOException;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import org.suai.constants.ConnectionConstants;


public class TelegraphNews implements ConnectionConstants {

	private Account account;
	private String accessToken;

	public static final String ERROR_PARSING = 
			EmojiParser.parseToUnicode("Хмммм, что-то пошло не так. Страничку не дам :confused:");

	public static final String FORBIDDEN_PHRASES[] = {
			"Больше важных новостей в Telegram-канале «Лента дня». Подписывайся!",
			"Больше ада и странных новостей в Telegram-канале «Лента дна». Подпишись!",
			"<br />"
	};


	public TelegraphNews(String accessToken) throws TelegraphException {
		TelegraphContextInitializer.init();
		TelegraphContext.registerInstance(ExecutorOptions.class, new ExecutorOptions());

		this.accessToken = accessToken;
		this.account = new GetAccountInfo(accessToken).execute();
	}


	public String createTelegraphPage(String link) {
		try {
			HttpURLConnection connection = getURLConnection(link);
			JSONObject root = (JSONObject) getRoot(connection);

			CreatePage createPage = transferToTelegraph(root);
			Page page = createPage.execute();

			return page.getUrl();
		}
		catch(IOException | ParseException | TelegraphException exception) {
			BotLogger.warning(getClass().getName(),
					"Cant create telegraph page by link (" + link + ")", exception);

			return ERROR_PARSING;
		}
	}


	private Object getRoot(HttpURLConnection connection) throws IOException, ParseException {
		Object root = null;

		try(
			InputStreamReader reader = new InputStreamReader(connection.getInputStream());
		) {
			JSONParser jsonParser = new JSONParser();
			root = jsonParser.parse(reader);
		}

		return root;
	}


	private CreatePage transferToTelegraph(JSONObject root) {
		Object[] content = parseJSON(root);

		CreatePage createPage = new CreatePage(this.accessToken,
				(String) content[0], (List<Node>) content[1]);
		createPage.setAuthorName(this.account.getAuthorName());
		createPage.setAuthorUrl(this.account.getAuthorUrl());
		createPage.setReturnContent(true);

		return createPage;
	}


	// object[0] - title; object[1] - contentDOM
	private Object[] parseJSON(JSONObject root) {
		Object[] content = new Object[2];

		JSONObject topic = (JSONObject) root.get("topic");

		JSONObject headline = (JSONObject) topic.get("headline");
		String title = (String) parseBlock(headline, "info", "title");
		String imageURL = (String) parseBlock(headline, "title_image", "url");
		String imageCaption = (String) parseBlock(headline, "title_image", "caption");

		JSONArray body = (JSONArray) topic.get("body");
		ArrayList<String> text = new ArrayList<>();

		for(int i = 0; i < body.size(); i++) {
			JSONObject paragraph = (JSONObject) body.get(i);

			if(paragraph.get("type").equals("p")) {
				String handleParagraph = handleText((String) paragraph.get("content"));

				if(handleParagraph != null) {
					text.add(handleParagraph);	
				}
			}
		}

		List<Node> contentDOM = createDOM(imageURL, imageCaption, text);

		content[0] = title;
		content[1] = contentDOM;

		return content;
	}


	private Object parseBlock(JSONObject root, String blockName, String subBlockName) {
		JSONObject block = (JSONObject) root.get(blockName);

		if(block == null) return null;

		return block.get(subBlockName);
	}


	private String handleText(String paragraph) {
		paragraph = paragraph.replaceAll("<a[^>]*>|<\\/a>", "");

		for(int i = 0; i < FORBIDDEN_PHRASES.length; i++) {
			if(paragraph.contains(FORBIDDEN_PHRASES[i])) {
				return null;
			}
		}

		return paragraph;
	}


	private List<Node> createDOM(String imageURL, String imageCaption, ArrayList<String> content) {
		List<Node> DOM = new ArrayList<>();

		if(imageURL != null) {
			DOM.add(createImageNode(imageURL, imageCaption));
		}

		for(int i = 0; i < content.size(); i++) {
			DOM.add(new NodeElement("p", null, createNodeText(content.get(i))));
		}

		return DOM;
	}


	// refactoring
	private NodeElement createImageNode(String imageURL, String imageCaption) {
		List<Node> children = new ArrayList<>();
		List<Node> imageNode = new ArrayList<>();

		imageNode.add(new NodeElement("img",
				createAttributes("src", imageURL), null));

		// div.figure_wrapper (image)
		children.add(new NodeElement("div",
				createAttributes("class", "figure_wrapper"), imageNode));

		// span.cursor_wrapper
		children.add(new NodeElement("span",
				createAttributes("class", "cursor_wrapper", "contenteditable", "true"), null));

		// figcaption.editable_text
		children.add(new NodeElement("figcaption",
				createAttributes("class", "editable_text", "data-placeholder", "Caption (optional)"),
				createNodeText(imageCaption)));

		return new NodeElement("figure",
				createAttributes("contenteditable", "false"), children);
	}


	private Map<String, String> createAttributes(String ... attributesName) {
		Map<String, String> attributes = new HashMap<>();

		if(attributesName.length < 2) {
			return attributes;
		}

		for(int i = 0; i < attributesName.length; i+=2) {
			attributes.put(attributesName[i], attributesName[i + 1]);
		}

		return attributes;
	}


	private List<Node> createNodeText(String text) {
		List<Node> textNode = new ArrayList<>();
		textNode.add(new NodeText(text));

		return textNode;
	}

}