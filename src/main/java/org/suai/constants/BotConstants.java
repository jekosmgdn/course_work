package org.suai.constants;


import com.vdurmont.emoji.EmojiParser;


public interface BotConstants {

	// BOT SETTING
	public static final String BOT_USERNAME = "Lenta_SUAI_Bot";
	public static final String BOT_TOKEN = "here insert bot token";
	public static final String TELEGRAPH_TOKEN = "here insert telegraph account token";
	public static final long PERIOD_UPDATE_EXCHANGE = 60000;
	public static final int MAX_COUNT_LIST = 7;
	public static final int MAX_COUNT_GALLERY = 3;

	// PATH, FILES AND LINKS
	public static final String RESOURCE_PATH = "resource/";
	public static final String LOG_PATH = "log/";
	public static final String RATES_FILE = "daily_cbr_rates.json";
	public static final String POPULAR_LINK = "http://api.lenta.ru/lists/popular";
	public static final String GALLERY_LINK = "http://api.lenta.ru/parts/photo";
	public static final String EXCHANGE_RATES_LINK = "https://www.cbr-xml-daily.ru/daily_json.js";


	// COMMANDS
	public static final String START_COMMAND = "/start";
	public static final String HELP_COMMAND = "/help";

	// BUTTONS
	public static final String RUBRICS_BUTTON = 
			EmojiParser.parseToUnicode(":ledger: Рубрики :ledger:");
	public static final String[][] RUBRICS = {
		{"Россия", "http://api.lenta.ru/rubrics/russia/"},
		{"Мир", "http://api.lenta.ru/rubrics/world/"},
		{"Экономика", "http://api.lenta.ru/rubrics/economics/"},
		{"Наука и техника", "http://api.lenta.ru/rubrics/science/"},
		{"Культура", "http://api.lenta.ru/rubrics/culture/"},
		{"Спорт", "http://api.lenta.ru/rubrics/sport/"}
	};
	public static final String BACK_BUTTON = 
			EmojiParser.parseToUnicode(":door: Назад :door:");
	public static final String POPULAR_BUTTON = 
			EmojiParser.parseToUnicode(":boom: Популярное :boom:");
	public static final String RANDOM_NEWS_BUTTON = 
			EmojiParser.parseToUnicode(":alien: Случайная новость :alien:");
	public static final String GALLERY_BUTTON = 
			EmojiParser.parseToUnicode(":camera: Галерея :camera:");
	public static final String EXCHANGE_RATES_BUTTON = 
			EmojiParser.parseToUnicode(":heavy_dollar_sign: Курс валют :heavy_dollar_sign:");
	public static final String HELP_BUTTON =
			EmojiParser.parseToUnicode(":question: Помощь :question:");


	// ANSWERS
	public static final String WELCOME_MESSAGE =
			EmojiParser.parseToUnicode("Привет, я работаю с сервисом Lenta.ru." +
									   "Чтобы начать просто нажми на одну из кнопок :wink:");

	public static final String HELP_MESSAGE =
			"Привет, я умею работать с сервисом Lenta.ru и делать кое-что по мелочи.\n" +
			"\"Рубрики\" - показывает новости по определенным рубрикам.\n" +
			"\"Популярное\" - покажет популярные новости дня.\n" +
			"\"Случайная новость\" - отправляет одну из случайных новостей из базы данных.\n" +
			"\"Галерея\" - показывает последние изображения галереи.\n" + 
			"\"Курс валют\" - покажет текущий курс.\n" +
			"\"Помощь\" - то, что ты сейчас читаешь.";

	public static final String[] WARNING_ANSWERS = 
			{
				"Хмммм, что ты имеешь в виду?",
				EmojiParser.parseToUnicode("Я тебя не понимаю. Видимо я не очень :cry:"),
				"Ой, беда. Кажется я понятия не имею о чем ты.",
				"Дружище, вот только давай без этого!",
				"Кажется мои силы на исходе, все очень плохо.",
				EmojiParser.parseToUnicode("Попробуй что-нибудь из того, что мне известно :blush:")
			};

}