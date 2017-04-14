/*
 * Created by Babita Singh
 * 
 */
package imageCrawler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.Namespace;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import for extraction of images : External jar has been used
import org.jsoup.Jsoup;

//imports for creating Excel sheet : External jar has been used
import jxl.Sheet;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/****************************************************
 * Image Class Structure to hold image attributes (beginning)
 ********************************/
class ImageDetails {
	private String imageName;
	private String imageSource;
	private String imageDomain;

	public ImageDetails(String imageName, String imageSource, String imageDomain) {
		this.setImageName(imageName);
		this.setImageSource(imageSource);
		this.setImageDomain(imageDomain);
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getImageSource() {
		return imageSource;
	}

	public void setImageSource(String imageSource) {
		this.imageSource = imageSource;
	}

	public String getImageDomain() {
		return imageDomain;
	}

	public void setImageDomain(String imageDomain) {
		this.imageDomain = imageDomain;
	}
}

/*****************************************************
 * Image Class Structure to hold image attributes (end)
 **********************************/

public class ImageCrawlerClass {
	/****************************************************
	 * SiteMap xml renderer and entry point (beginning)
	 ********************************/
	public static void EntryPoint(String sitemap) {
		try {
			ArrayList<String> entry = new ArrayList<String>();
			ArrayList<ImageDetails> imagesList = new ArrayList<ImageDetails>();
			ImageDetails imageDetails;
			entry = parser(sitemap);
			System.out.println(entry.get(0));
			for (String urladd : entry) {
				URL url = new URL(urladd);
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				//int code = con.getResponseCode();
				try {	
					
					InputStream in = con.getInputStream();	
					org.jsoup.nodes.Document doc = Jsoup.parse(in,"UTF-8", urladd);
					org.jsoup.select.Elements images = doc.select("img[src~=(?i)\\.(png|jpg|jpe?g|gif)]");
			        for (org.jsoup.nodes.Element image : images) {
			        	String source = image.attr("src");
			        	String[] parts = source.split("/");
			        	imageDetails = new ImageDetails(parts[parts.length-1],source,urladd);			        	
			            imagesList.add(imageDetails);
			        }
					excelCreation(imagesList); //
					createImageSiteMap(imagesList, urladd); //
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	/****************************************************
	 * SiteMap xml renderer and entry point (end)
	 **************************************/
	
	/****************************************************
	 * SiteMap rendering/ Dom Parsing Beginning
	 **************************************/
	public static ArrayList<String> parser(String path) {
		File file = new File(path);
		ArrayList<String> urlList = new ArrayList<String>();
		// Get Document Builder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			// Build Document
			Document document = builder.parse(file);
			// Normalize the XML Structure;
			document.getDocumentElement().normalize();
			NodeList nList = document.getElementsByTagName("loc");
			String name = document.getNodeName();
			for (int i = 0; i < nList.getLength(); i++) {
				urlList.add((nList.item(i)).getTextContent());
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return urlList;
	}	
	
	/****************************************************
	 * SiteMap rendering/ Dom Parsing end
	 **************************************/
	
	
	/***********************************************
	 * SiteMap creation for Images begins -- Dom Parser
	 ****************************/
	public static void createImageSiteMap(ArrayList<ImageDetails> imageDoc, String urlDomain) {
		try {
			File file = new File("Output\\ImageSiteMap.xml");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			docFactory.setNamespaceAware(true);
			if (!file.exists()) {			

				// root elements
				Document doc = docBuilder.newDocument();
				
				
				Element rootElement = doc.createElement("urlset");
				doc.appendChild(rootElement);
				
				Element url = doc.createElement("url");
				rootElement.appendChild(url);

				Element loc = doc.createElement("loc");
				loc.appendChild(doc.createTextNode(urlDomain));
				url.appendChild(loc);
				
				
				for (ImageDetails imgDoc : imageDoc) {
					
					Element NSimage = doc.createElementNS("", "image:image");
					url.appendChild(NSimage);
					
					Element NSimageNode = doc.createElementNS("", "image:loc");
					NSimageNode.appendChild(doc.createTextNode(imgDoc.getImageSource()));
					NSimage.appendChild(NSimageNode);
				}
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File("Output\\ImageSiteMap.xml"));

				transformer.transform(source, result);
				
			}else {
				Document doc = docBuilder.parse("Output\\ImageSiteMap.xml");
				Element rootElement = doc.getDocumentElement();
				
				Element url = doc.createElement("url");
				rootElement.appendChild(url);

				Element loc = doc.createElement("loc");
				loc.appendChild(doc.createTextNode(urlDomain));
				url.appendChild(loc);
			
				
				for (ImageDetails imgDoc : imageDoc) {
					Element NSimage = doc.createElementNS("", "image:image");
					url.appendChild(NSimage);
					
					Element NSimageNode = doc.createElementNS("", "image:loc");
					NSimageNode.appendChild(doc.createTextNode(imgDoc.getImageSource()));
					NSimage.appendChild(NSimageNode);
				}
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
		        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File("Output\\ImageSiteMap.xml"));
				transformer.transform(source, result);				
			}
			

			System.out.println("File saved!");
		} catch (Exception e) {

		}
	}
	/***********************************************
	 * SiteMap creation for Images ends
	 ****************************/
	
	/***********************************************
	 * Obtain images from parsed xml of WebPage (beginning)
	 ****************************/
	public static void excelCreation(ArrayList<ImageDetails> imageDetailsList) {			
			//System.out.println("****** Writing Excel for " + url.toString() + " ********");
			File file = new File("Output\\ExportToExcel.xls");
			WritableWorkbook myFirstWbook = null;
			try {
			if (!file.exists()) {
				myFirstWbook = Workbook.createWorkbook(new File("Output\\ExportToExcel.xls"));

				// create an Excel sheet
				WritableSheet excelSheet = myFirstWbook.createSheet("Sheet 1", 0);
				// add rows into the Excel sheet
				Label label = new Label(0, 0, "Domain URL");
				excelSheet.addCell(label);

				label = new Label(1, 0, "Image Name");
				excelSheet.addCell(label);

				label = new Label(2, 0, "Image Source");
				excelSheet.addCell(label);

				Number number = new Number(0, 1, 1);
				excelSheet.addCell(number);
				int i = 1;
				int j = 1;

				int row = 1;
				int col = 0;
				for (ImageDetails image : imageDetailsList) {
					label = new Label(col, row, image.getImageDomain());
					excelSheet.addCell(label);

					label = new Label(col + 1, row, image.getImageName());
					excelSheet.addCell(label);

					label = new Label(col + 2, row, image.getImageSource());
					excelSheet.addCell(label);
					row++;
				}
				myFirstWbook.write();
			} else {
				Workbook myWbookf = Workbook.getWorkbook(file);
				Sheet sheet = myWbookf.getSheet(0);
				WritableWorkbook myWbook = Workbook.createWorkbook(file, myWbookf);
				WritableSheet wsheet = myWbook.getSheet(0);
				int row = sheet.getRows();
				int col = 0;

				Label label;
				for (ImageDetails image : imageDetailsList) {
					label = new Label(col, row, image.getImageDomain());
					wsheet.addCell(label);

					label = new Label(col + 1, row, image.getImageName());
					wsheet.addCell(label);

					label = new Label(col + 2, row, image.getImageSource());
					wsheet.addCell(label);
					row++;
				}
				myWbook.write();
				myWbook.close();
				myWbookf.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (myFirstWbook != null) {
				try {
					myFirstWbook.close();

				} catch (IOException e) {
					e.printStackTrace();
				} catch (WriteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/***********************************************
	 * Obtain images from parsed xml of WebPage (end)
	 ****************************/

}
