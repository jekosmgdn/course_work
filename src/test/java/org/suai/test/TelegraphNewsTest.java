package org.suai.test;


import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.BeforeClass;

import org.telegram.telegraph.api.methods.GetPage;
import org.telegram.telegraph.api.objects.Page;
import org.telegram.telegraph.api.objects.Node;
import org.telegram.telegraph.api.objects.NodeElement;
import org.telegram.telegraph.api.objects.NodeText;
import org.telegram.telegraph.exceptions.TelegraphException;

import java.util.List;
import java.util.Map;

import org.suai.abilities.TelegraphNews;


public class TelegraphNewsTest {

	private static TelegraphNews telegraphNews;

	private static final String TELEGRAPH_TOKEN = "here insert telegraph account token";
	private static final String LENTA_LINK = "https://api.lenta.ru/news/2017/12/21/greed/";
	private static final String IMAGE_LINK = 
			"https://icdn.lenta.ru/images/2017/12/21/16/20171221163053591/" +
			"detail_590caba90329d2b347119ea54951dc92.jpg";

	private static final String TITLE_PAGE = "Атаковавших российский банк хакеров подвела жадность";
	private static final String PARAGRAPHS[] = {
			"Хакеры, пытавшиеся вывести из банка «Глобэкс» 55 миллионов рублей, смогли украсть менее" +
			" 10 процентов этой суммы из-за собственной жадности. Об этом в четверг, 21 декабря, пишет" +
			" «Коммерсантъ» со ссылкой на источники.",

			"«Злоумышленники выводили средства крупными суммами, всего было совершено около полутора" +
			" десятка трансакций, большая часть которых была заблокирована», — сказал собеседник издания." +
			" Привлекло внимание и то, что проводились валютные операции, добавил он.",

			"Атака на банк продолжалась несколько месяцев с помощью внедрения нового вредоносного программного" +
			" обеспечения, не выявляемого антивирусами. Завершающая фаза атаки — отправка хакерами платежных" +
			" сообщений через международную систему передачи финансовой информации SWIFT — была своевременно" +
			" выявлена и остановлена.",

			"Эта атака уникальна тем, что впервые в России для вывода средств использовалась международная" +
			" межбанковская система. Последние два года злоумышленники целенаправленно атаковали SWIFT, в" +
			" результате пострадали банки более чем в десяти странах мира, отмечают в «Лаборатории Касперского»."
	};


	@BeforeClass
	public static void init() {
		try {
			telegraphNews = new TelegraphNews(TELEGRAPH_TOKEN);
		}
		catch(TelegraphException exception) {
			fail(exception.getMessage());
		}
	}


	@Test
	public void createTelegraphPageTest() {
		String telegraphLink = telegraphNews.createTelegraphPage(LENTA_LINK);

		int indexCut = telegraphLink.lastIndexOf("/");
		telegraphLink = telegraphLink.substring(indexCut);

		Page page = null;

		try {
			page = new GetPage(telegraphLink)
								.setReturnContent(true)
								.execute();
		}
		catch(TelegraphException exception) {
			fail(exception.getMessage());
		}

		assertEquals(page.getTitle(), TITLE_PAGE);

		List<Node> root = page.getContent();

		NodeElement figure = (NodeElement) root.get(0);
		checkImage(figure);

		for(int i = 1; i < root.size(); i++) {
			checkParagraph((NodeElement) root.get(i), i - 1);
		}
	}


	private void checkImage(NodeElement figure) {
		assertEquals(figure.getTag(), "figure");

		NodeElement image = (NodeElement) figure.getChildren().get(0);
		Map<String, String> attrs = image.getAttrs();

		assertEquals(image.getTag(), "img");
		assertEquals(attrs.get("src"), IMAGE_LINK);
	}


	private void checkParagraph(NodeElement paragraph, int index) {
		assertEquals(paragraph.getTag(), "p");

		NodeText text = (NodeText) paragraph.getChildren().get(0);

		assertEquals(text.getContent(), PARAGRAPHS[index]);
	}

}