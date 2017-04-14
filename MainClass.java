package imageCrawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
public class MainClass {	
	
	public static void main(String[] args) {
		//String path = "Input/SiteMap.xml";
		
	
	if(args.length>=1)
	{
			String path = args[0];
			ImageCrawlerClass imgObj = new ImageCrawlerClass();
			imgObj.EntryPoint(path);
	}else
		{
			System.out.println("Enter a valid file name as an argument\n");
			System.out.println("Follow the input pattern is jar -- Java -jar <Jar name.jar> <Input File Path for sitemap>");
		}
		
	return;
	}
}
