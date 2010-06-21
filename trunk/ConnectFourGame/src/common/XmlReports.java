package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import theProtocol.ClientServerProtocol;

public class XmlReports {
	
	public  static class WrongSchema extends Exception{}
	private Document doc;
	private String clientNameFile;
	
	
	public XmlReports(String clientName) throws SAXException, IOException, WrongSchema
	{
		this.clientNameFile=clientName;
		doc=null;
		getDomTree(clientName);
		
	}
	
	
	public ArrayList<GameReport> generateGameList()
	{
		ArrayList<GameReport> list = new ArrayList<GameReport>();
		NodeList nodes=doc.getElementsByTagName("game");
		for (int i=0; i<nodes.getLength(); i++)
		{
			String [] arr= new String[4];
			arr[0]=((Element)nodes.item(i)).getElementsByTagName("gameId").item(0).getTextContent();
			arr[1]=((Element)nodes.item(i)).getElementsByTagName("clientName").item(0).getTextContent();
			arr[2]=((Element)nodes.item(i)).getElementsByTagName("gameResult").item(0).getTextContent();
			arr[3]=((Element)nodes.item(i)).getElementsByTagName("winner").item(0).getTextContent();
			list.add(new GameReport(arr[0], arr[1], arr[2], arr[3]));
		}
		return list;
		
	}
	
	public String createGamesReportString()
	{
		ArrayList<GameReport> list = generateGameList();
		StringBuilder sb= new StringBuilder();
		sb.append(ClientServerProtocol.BATCHGAMESREPORT);
		for (GameReport gameReport : list) {
			sb.append(" "+gameReport.getGameId()+" "+gameReport.getClientName()+" "+gameReport.getStatus()+" "+gameReport.getWinner());
		}
		return sb.toString();
	}
	
	public static ArrayList<GameReport> gameReportsFromReportString(String [] params)
	{
		ArrayList<GameReport> list = new ArrayList<GameReport>();
		int numOfReports= (params.length-1)/4;
		for (int i=0; i<numOfReports; i++)
		{
			String [] arr= new String[4];
			for (int j=0; j<4; j++)
			{
				arr[j]= params[4*i+1+j];
			}
			list.add(new GameReport(arr[0], arr[1], arr[2], arr[3]));
			
		}
		return list;
	}
	
	
	public void removeReport(String gameId)
	{
		NodeList list=doc.getElementsByTagName("gameId");
		for (int i=0 ; i<list.getLength(); i++)
		{
			if (list.item(i).getTextContent().equals(gameId))
			{
				doc.getDocumentElement().removeChild(list.item(i).getParentNode());
			}
		}
		printDomTreeToFile();
	}
	
	private void printDomTreeToFile()
	{
		TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = null;
		try {
			trans = transfac.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
		  DOMSource source = new DOMSource(doc);
	      StreamResult result = new StreamResult(new java.io.File("").getAbsolutePath()+"\\src\\"+clientNameFile+".xml");
          try {
			trans.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void addGameReport(String gameId,String clientName,String gameResult,String winner)
	{
		Node game=doc.createElement("game");
		Node gameIdN= doc.createElement("gameId");
		gameIdN.setTextContent(gameId);
		Node clientNameN= doc.createElement("clientName");
		clientNameN.setTextContent(clientName);
		Node resultN= doc.createElement("gameResult");
		resultN.setTextContent(gameResult);
		Node winnerN= doc.createElement("winner");
		winnerN.setTextContent(winner);
		game.appendChild(gameIdN);
		game.appendChild(clientNameN);
		game.appendChild(resultN);
		game.appendChild(winnerN);
		doc.getDocumentElement().appendChild(game);
		printDomTreeToFile();
		
	
	}
	
	private void getDomTree(String clientName) throws SAXException, IOException, WrongSchema  {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuilder localpath = new StringBuilder();
		localpath.append(File.separator);
		localpath.append("src");
		localpath.append(File.separator);
		localpath.append(clientName+".xml");
		try{
		doc = builder.parse(new java.io.File("").getAbsolutePath()
					+ localpath.toString());
	
		}
		catch (FileNotFoundException ex)
		{
			doc= builder.newDocument();
			
			doc.appendChild(doc.createElement("reports"));
		}
		doc.normalize();
		
		
		//Validate the xml file via the DTD
		  DOMSource source = new DOMSource(doc);
	      StreamResult result = new StreamResult(System.out);
	      TransformerFactory tf = TransformerFactory.newInstance();
	      Transformer transformer = null;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "\\src\\common\\reports.dtd");
	      try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new WrongSchema();
		}

		
	}
	
	

}
