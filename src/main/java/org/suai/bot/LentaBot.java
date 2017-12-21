package org.suai.bot;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.logging.BotsFileHandler;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import org.telegram.telegraph.exceptions.TelegraphException;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.suai.abilities.DataBase;
import org.suai.abilities.NewsList;
import org.suai.abilities.TelegraphNews;
import org.suai.abilities.Gallery;
import org.suai.abilities.ExchangeRates;

import org.suai.constants.BotConstants;
import org.suai.constants.DBConstants;


public class LentaBot extends TelegramLongPollingBot implements BotConstants, DBConstants {

	private DataBase db;

	private NewsList newsList;
	private TelegraphNews telegraph;
	private ExchangeRates exchangeRates;
	private Gallery gallery;
	private ReplyKeyboardMarkup defaultKeyboard;
	private ReplyKeyboardMarkup rubricsKeyboard;


	public LentaBot() throws MalformedURLException, TelegraphException, UnknownHostException, IOException {
		BotLogger.setLevel(Level.ALL);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("(dd-MMM-uuuu)");
		BotLogger.registerLogger(new BotsFileHandler(RESOURCE_PATH +
				LOG_PATH + "log" + LocalDateTime.now().format(formatter) + ".txt", true));

		this.db = new DataBase();

		this.newsList = new NewsList();
		this.telegraph = new TelegraphNews(TELEGRAPH_TOKEN);
		this.gallery = new Gallery();
		this.exchangeRates = new ExchangeRates(EXCHANGE_RATES_LINK,
				RESOURCE_PATH + RATES_FILE, PERIOD_UPDATE_EXCHANGE);
		this.defaultKeyboard = createDefaultKeyboard(
				RUBRICS_BUTTON, POPULAR_BUTTON, RANDOM_NEWS_BUTTON,
				GALLERY_BUTTON, EXCHANGE_RATES_BUTTON, HELP_BUTTON);

		this.rubricsKeyboard = createDefaultKeyboard(
				RUBRICS[0][0], RUBRICS[1][0], RUBRICS[2][0],
				RUBRICS[3][0], RUBRICS[4][0], RUBRICS[5][0],
				BACK_BUTTON);
	}


	private ReplyKeyboardMarkup createDefaultKeyboard(String ... textButtons) {
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		setKeyboardAppearance(keyboardMarkup, false, true, true);

		List<KeyboardRow> keyboard = new ArrayList<>();
		int length = textButtons.length;

		for(int i = 0; i < length - 1; i+=2) {
			KeyboardRow row = new KeyboardRow();
			row.add(textButtons[i]);
			row.add(textButtons[i + 1]);

			keyboard.add(row);
		}

		if((length & 1) == 1) {
			KeyboardRow row = new KeyboardRow();
			row.add(textButtons[length - 1]);

			keyboard.add(row);
		}

		return keyboardMarkup.setKeyboard(keyboard);
	}


	private void setKeyboardAppearance(ReplyKeyboardMarkup keyboardMarkup, boolean isOneTime,
	boolean isResize, boolean isSelective) {
		keyboardMarkup.setOneTimeKeyboard(isOneTime);
		keyboardMarkup.setResizeKeyboard(isResize);
		keyboardMarkup.setSelective(isSelective);
	}


	@Override
	public void onUpdateReceived(Update update) {
		if(update.hasMessage()) {
			BotLogger.info(getClass().getName(), getMessageInfo(update.getMessage()));

			long chatId = update.getMessage().getChatId();

			if(update.getMessage().hasText()) {
				String text = update.getMessage().getText();

				handleMessageText(chatId, text);
			}
			else {
				sendExcuseResponse(chatId);
			}
		}
		else if(update.hasCallbackQuery()) {
			handleCallbackQuery(update.getCallbackQuery());
		}
	}


	private void handleMessageText(long chatId, String text) {
		int indexRubric = 0;

		if(text.equals(START_COMMAND)) {
			sendResponse(chatId, WELCOME_MESSAGE, this.defaultKeyboard);
		}
		else if(text.equals(HELP_COMMAND) ||
				text.equals(HELP_BUTTON)) {
			sendResponse(chatId, HELP_MESSAGE, null);
		}
		else if(text.equals(RUBRICS_BUTTON)) {
			sendResponse(chatId, "Выбери рубрику", this.rubricsKeyboard);
		}
		else if((indexRubric = getIndexRubric(text)) != -1) {
			sendListRespons(chatId,
					this.newsList.getListNews(RUBRICS[indexRubric][1], MAX_COUNT_LIST));
		}
		else if(text.equals(BACK_BUTTON)) {
			sendResponse(chatId, "Нажми на одну из кнопок", this.defaultKeyboard);
		}
		else if(text.equals(POPULAR_BUTTON)) {
			sendListRespons(chatId, this.newsList.getListNews(POPULAR_LINK, MAX_COUNT_LIST));
		}
		else if(text.equals(RANDOM_NEWS_BUTTON)) {
			sendResponse(chatId, (String) this.db.getRandomRecordParameter(NEWS_COLECTION,
					NEWS_KEY_LINK_TELEGRAPH), null);
		}
		else if(text.equals(GALLERY_BUTTON)) {
			sendListMedia(chatId,
					this.gallery.getListGallery(GALLERY_LINK,
					MAX_COUNT_GALLERY));
		}
		else if(text.equals(EXCHANGE_RATES_BUTTON)) {
			sendResponse(chatId, this.exchangeRates.getRate(), null);
		}
		else {
			sendExcuseResponse(chatId);
		}
	}


	private void handleCallbackQuery(CallbackQuery callbackQuery) {
		Message message = callbackQuery.getMessage();

		long chatId = message.getChatId();
		String link = callbackQuery.getData();

		String telegraphLink = (String) this.db.findRecordParameter(NEWS_COLECTION,
				NEWS_KEY_LINK_API, link,
				NEWS_KEY_LINK_TELEGRAPH);

		if(telegraphLink == null) {
			telegraphLink = this.telegraph.createTelegraphPage(link);

			if(telegraphLink != TelegraphNews.ERROR_PARSING) {
				this.db.addRecord(NEWS_COLECTION,
						NEWS_KEY_LINK_API, link,
						NEWS_KEY_LINK_TELEGRAPH, telegraphLink);
			}
		}

		BotLogger.info(getClass().getName(), getMessageInfo(message)
				+ "(query: " + link + ") (response: " + telegraphLink + ") ");

		sendResponse(chatId, telegraphLink, null);
	}


	private String getMessageInfo(Message message) {
		StringBuilder info = new StringBuilder();

		Chat chat = message.getChat();

		info.append(chat.getFirstName()).append(' ')
			.append(chat.getLastName()).append(' ')
			.append(chat.getUserName())
			.append(" (id: ").append(chat.getId()).append("), ")
			.append(" (message: ").append(message.getText()).append(") ");

        return info.toString();
	}


	private int getIndexRubric(String rubric) {
		int index = -1;

		for(int i = 0; i < RUBRICS.length; i++) {
			if(RUBRICS[i][0].equals(rubric)) {
				return i;
			}
		}

		return -1;
	}


	private void sendExcuseResponse(long chatId) {
		Random random = new Random();
		int index = random.nextInt(WARNING_ANSWERS.length);

		sendResponse(chatId, WARNING_ANSWERS[index], null);
	}


	private void sendResponse(long chatId, String answer, ReplyKeyboardMarkup keyboardMarkup) {
		SendMessage sendMessage = new SendMessage(chatId, answer);
		sendMessage.setReplyMarkup(keyboardMarkup);

		try {
			execute(sendMessage);
		}
		catch(TelegramApiException exception) {
			BotLogger.warning(getClass().getName(), "Cant send message", exception);
		}
	}


	private void sendListRespons(long chatId, ArrayList<SendMessage> list) {
		for(int i = 0; i < list.size(); i++) {
			SendMessage message = list.get(i);

			message.setChatId(chatId);

			try {
				execute(message);
			}
			catch(TelegramApiException exception) {
				BotLogger.warning(getClass().getName(), "Cant send list of messages", exception);
			}
		}
	}


	private void sendListMedia(long chatId, Object object) {
		if(object instanceof SendMessage) {
			SendMessage message = (SendMessage) object;
			sendResponse(chatId, message.getText(), null);

			return;
		}

		ArrayList<Object[]> galleries = (ArrayList<Object[]>) object;
		for(int i = 0; i < galleries.size(); i++) {
			SendMediaGroup sendMediaGroup = (SendMediaGroup) galleries.get(i)[1];
			SendMessage sendMessage = (SendMessage) galleries.get(i)[0];

			sendMediaGroup.setChatId(chatId);
			sendMessage.setChatId(chatId);

			try {
				sendMediaGroup(sendMediaGroup);
				execute(sendMessage);
			}
			catch(TelegramApiException exception) {
				BotLogger.warning(getClass().getName(), "Cant send list of media", exception);	
			}
		}


	}


	@Override
	public String getBotUsername() {
		return BOT_USERNAME;
	}


	@Override
	public String getBotToken() {
		return BOT_TOKEN;
	}

}