package classes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import interfaces.InterfaceWebCrowlerDataBase;

/**
 * Klasa indeksujaca streone i podstrony wielowatkowo.
 * Przed uzyciem nalezy wywolac statyczna metode 'setData'.
 * Aby rozpoczac indeksowanie wywolaj statyczna metode startIndexing * 
 */
public class ThreadIndexPage extends Thread
{
	static String startUrl;
	static InterfaceWebCrowlerDataBase interfaceDataBase;
	static Set<String> visitedUrls = new HashSet<String>();
	static int quantityOfThreads;
	static int threadNumber =0;

	private String url;
	private int nr;

	/**
	 * Statyczna metoda inicjalizujaca niezbedne dane
	 * @param startUrl url rozpoczecia indeksowania
	 * @param interfaceDataBase interface do bazydanyc gdzie beda zapisywane indeksy
	 * @param quantityOfThreads maksymalna liczba watkow
	 * @param visitedUrls referencja na kolekcje odwiedzonych stron
	 */
	static Collection<String> setData(String startUrl, InterfaceWebCrowlerDataBase interfaceDataBase,
			int quantityOfThreads)
	{
		ThreadIndexPage.startUrl = startUrl;
		ThreadIndexPage.interfaceDataBase = interfaceDataBase;	
		ThreadIndexPage.quantityOfThreads = quantityOfThreads;
		return ThreadIndexPage.visitedUrls;
	}

	public static void startIndexing()
	{
		if(startUrl==null || interfaceDataBase ==null)
		{
			System.out.println("nie wywolano funkcji set Data");		
		}
		else
		{
			new ThreadIndexPage(startUrl);
		}		
	}
	
	private ThreadIndexPage(String url)
	{
		quantityOfThreads--;
		nr = threadNumber++;
		this.url = url;
		start();
		quantityOfThreads++;
		threadNumber--;
	}

	

	void indexPage(String url)
	{
		System.out.println("\nindexing url:" + url+ "przez watek nr: "+ nr);

		// uzyskanie z url drzewa skladniowego (syntactic tree)
		Document doc;
		try
		{
			doc = Jsoup.connect(url).get();
		} catch (Exception e)
		{
			System.out.println("nie można otwozyc strony: " + url);
			return;
		}
		// dodanie url do odwiedzonych
		visitedUrls.add(url);

		// z dokumentu uzyskanie listy slow
		String text = doc.body().text();

		Pattern patern = Pattern.compile("\\W+");
		String tablicaSlow[] = patern.split(text);
		// wepchniecie listy slow kluczowych do bazy danych
		for (String keyWord : tablicaSlow)
		{
			interfaceDataBase.push(keyWord, url);
		}

		// z dokumentu uzyskanie listy linkow domeny
		Elements links = doc.select("a");

		for (Element element : links)
		{
			String urlToCheck = element.attr("abs:href");
			Pattern paternUrl = Pattern.compile(startUrl);
			Matcher matcher = paternUrl.matcher(urlToCheck);

			if (matcher.find() & !visitedUrls.contains(urlToCheck))
			{
				if (quantityOfThreads > 0)
				{
					new ThreadIndexPage(urlToCheck);
				} else
				{
					indexPage(urlToCheck);
				}

			}
		}
	}

	public void run()
	{		
		indexPage(url);
	}
}