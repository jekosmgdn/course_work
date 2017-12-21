package org.suai.abilities;


import org.telegram.telegrambots.logging.BotLogger;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.vdurmont.emoji.EmojiParser;


public class ExchangeRates {

	private URL url;
	private File fileRates;

	private long periodMLS;
	private String rates = null;

	public static final String ERROR_PARSING = 
		EmojiParser.parseToUnicode("Оййй, что-то у меня с курсом совсем неполадки :cry:");


	public ExchangeRates(String spec, String filename, long periodMLS) throws MalformedURLException {
		this.url = new URL(spec);
		this.fileRates = new File(filename);

		this.periodMLS = periodMLS;
	}


	public synchronized String getRate() {
		boolean isUpdate = updateRates();

		if(isUpdate || this.rates == null) {
			this.rates = parseDocument();
		}

		return this.rates;
	}


	private String parseDocument() {
		double rateUSD = 0.;
		double previousUSD = 0.;

		double rateEUR = 0.; 
		double previousEUR = 0.;

		try(
			FileReader reader = new FileReader(this.fileRates);
		) {
			JSONParser parser = new JSONParser();
		
			JSONObject object = (JSONObject) parser.parse(reader);
			JSONObject valute = (JSONObject) object.get("Valute");
			JSONObject dollar = (JSONObject) valute.get("USD");
			JSONObject euro = (JSONObject) valute.get("EUR");

			rateUSD = (double) dollar.get("Value");
			previousUSD = (double) dollar.get("Previous");

			rateEUR = (double) euro.get("Value");
			previousEUR = (double) euro.get("Previous");
		}
		catch(IOException | ParseException exception) {
			BotLogger.warning(getClass().getName(), exception);

			return ERROR_PARSING;
		}

		return toStringRate("USD", rateUSD, previousUSD) +
			   toStringRate("EUR", rateEUR, previousEUR); 
	}


	private String toStringRate(String name, double rate, double previous) {
		StringBuilder rateBuilder = new StringBuilder();

		double difference = rate - previous;
		int codePoint = ((difference < 0) ? 0x2193 : 0x2191);

		rateBuilder.append(name).append(" ")
				   .append(rate).append(" (");

		if(difference != 0) {
			rateBuilder.append(Character.toString((char) codePoint)).append(" ");
		}

		rateBuilder.append(previous).append(")\n");

		return rateBuilder.toString();
	}


	private boolean updateRates() {
		long differenceTime = System.currentTimeMillis() - this.fileRates.lastModified();

		if(differenceTime < this.periodMLS) {
			return false;
		}

		try {	
			FileUtils.copyURLToFile(this.url, this.fileRates);

			BotLogger.info(getClass().getName(), "File exchange rates update success");
		}
		catch(IOException exception) {
			BotLogger.warning(getClass().getName(), exception);

			return false;
		}

		return true;
	}

}