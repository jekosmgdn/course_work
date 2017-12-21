import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegraph.exceptions.TelegraphException;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.suai.bot.LentaBot;

import org.suai.abilities.DataBase;
import org.suai.abilities.ExchangeRates;
import org.suai.abilities.TelegraphNews;
import java.io.IOException;


public class Main {

	public static void main(String[] args) {
		ApiContextInitializer.init();

		TelegramBotsApi botsApi = new TelegramBotsApi();

		try {
			botsApi.registerBot(new LentaBot());
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
	}
	
}