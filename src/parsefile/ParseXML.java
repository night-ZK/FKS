package parsefile;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ParseXML {
	
	private static ParseXML parseXML = new ParseXML();
	
	private ParseXML() {
		
	}
	
	public synchronized static ParseXML createParseXML() {
		return parseXML;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * ���DBcontion information
	 * @param dbID
	 * @return
	 */
	public Element getDBXMLElement(String dbID) {
		SAXReader reader = new SAXReader();
		try {
			File dbXML = new File("./src/resources/db.xml");
			Document document = reader.read(dbXML);
			Element root = document.getRootElement();
			List<Element> dbElements = root.elements("db-connection");
			for (Element dbelement : dbElements) {
				
				if (dbelement.attributeValue("id").equals(dbID)) {
					return dbelement;
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * ���socketserver������Ϣ
	 * @param serverID
	 * @return
	 */
	public Element getServerXMLElement(String serverID) {
		SAXReader reader = new SAXReader();
		try {
			File dbXML = new File("./resources/server-information.xml");
			Document document = reader.read(dbXML);
			Element root = document.getRootElement();
			@SuppressWarnings("unchecked")
			List<Element> dbElements = root.elements("server");
			for (Element dbelement : dbElements) {
				
				if (dbelement.attributeValue("id").equals(serverID)) {
					return dbelement;
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * �����Ҫ������XML�ļ�root�ڵ��µ�����ڵ�list
	 * @param parseFile ������XML�ļ� 
	 * @param childNodeName ����ڵ���
	 * @return �������ļ���root�ڵ��µ���������ڵ��list
	 */
	public List<Element> getXMLElementList(File parseFile, String childNodeName) {
		SAXReader reader = new SAXReader();
		try {
			File file_XML = parseFile;
			Document document = reader.read(file_XML);
			Element root = document.getRootElement();
			return root.elements(childNodeName);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		String url = new ParseXML().getDBXMLElement("001").elementText("url");
		System.out.println(url);
		
		String address = new ParseXML().getServerXMLElement("001").elementText("port");
		System.out.println(address);
		
	}
}