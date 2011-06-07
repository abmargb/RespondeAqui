package com.merespondeaqui.placar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.DOMTextImpl;
import org.w3c.tidy.Tidy;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.merespondeaqui.Processor;
import com.merespondeaqui.Utils;

public class PlacarProcessor implements Processor {

	private static final String PREFIX = "quantoestaojogo";
	private static final String FULL_PREFIX = Utils.createFullPrefix(PREFIX);
	
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	private static final long A_DAY = 1000 * 60 * 60 * 24;
	
	@Override
	public void process(Tweet tweet, Twitter twitter) throws TwitterException {
		
		try {
			String text = tweet.getText().toLowerCase();
			text = preProcess(text);
			
			String[] splitText = text.split("\\s+");
			
			String team = text.substring(FULL_PREFIX.length() + splitText[2].length() + 2);
			
			Date yesterday = new Date(new Date().getTime() - A_DAY);
			
			findGame(team, "http://www.futebolaovivo.tv/jogos-do-" + FORMAT.format(yesterday) + ".html", 
					tweet, twitter);
			findGame(team, "http://www.futebolaovivo.tv/refresh/index", 
					tweet, twitter);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String preProcess(String text) {
		return text.replaceAll("?", "");
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	private static void findGame(String team, String url, Tweet tweet, Twitter twitter) throws IOException,
			MalformedURLException, XPathExpressionException, TwitterException {
		InputStream stream = new URL(url).openStream();
		
		String xmlString = IOUtils.toString(stream);
		
		Tidy tidy = new Tidy();
		tidy.setErrout(new PrintWriter(new StringWriter()));
		tidy.setXHTML(true);
		
		Document xmlDocument = tidy.parseDOM(
				new InputStreamReader(new ByteArrayInputStream(xmlString.getBytes())), 
				null);
		
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xPath = xPathFactory.newXPath();
		XPathExpression xPathTeamExp = xPath.compile(
				"//td[contains(@class, 'team-')]/a[contains(translate(text()," +
				"'çÇáéíóúýÁÉÍÓÚÝàèìòùÀÈÌÒÙãõñäëïöüÿÄËÏÖÜÃÕÑâêîôûÂÊÎÔÛABCDEFGHIJKLMNOPQRSTUVWXYZ'," +
				"'ccaeiouyaeiouyaeiouaeiouaonaeiouyaeiouaonaeiouaeiouabcedfghijklmnopqrstuvwxyz'), " +
				"'" + normalize(team) + "')]");
		
		NodeList teamList = (NodeList) xPathTeamExp.evaluate(
				xmlDocument, 
				XPathConstants.NODESET);
		
		for (int i = 0; i < teamList.getLength(); i++) {
			Node node = teamList.item(i);
			Node parentNode = node.getParentNode().getParentNode();
			
			Node nodeTeamHome = findNodeByClass(parentNode, "team-home").getFirstChild().getFirstChild();
			Node nodeTeamAway = findNodeByClass(parentNode, "team-away").getFirstChild().getFirstChild();
			Node nodeScore = findNodeByClass(parentNode, "score-live").getFirstChild().getFirstChild();
			Node nodeTime = findNodeByClass(parentNode, "td-left").getFirstChild();
			Node nodeEtat = findNodeByClass(parentNode, "etat").getLastChild();
			
			boolean started = hasStarted(nodeScore.getParentNode());
			
			String teamHome = ((DOMTextImpl)nodeTeamHome).getData();
			String teamAway = ((DOMTextImpl)nodeTeamAway).getData();
			
			String time = ((DOMTextImpl)nodeTime).getData();
			
			String response = null;
			
			if (!started) {
				response = teamHome + " x " + teamAway + " começa às " + time;
			} else {
				String score = ((DOMTextImpl)nodeScore).getData();
				String clock = time;
				
				boolean ended = time.equals("Finalizado");
				if (!ended) {
					clock = ((DOMTextImpl)nodeEtat).getData();
				}
				
				response = teamHome + " " + score + " " + teamAway + ", " + clock.trim();
			}
			
			twitter.updateStatus("@" + tweet.getFromUser() + " " + response);
		}
	}
	
	private static boolean hasStarted(Node nodeScore) {
		NodeList childNodes = nodeScore.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equals("span")) {
				return false;
			}
		}
		return true;
	}

	private static String normalize(String original) {
		String input = original.toLowerCase();
		input = Normalizer.normalize(input, Normalizer.Form.NFD);  
		input = input.replaceAll("[^\\p{ASCII}]", "");  
		return input; 
	}
	
	private static Node findNodeByClass(Node currentCondition, String key) {
		NodeList childNodes = currentCondition.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getAttributes().getNamedItem("class").getNodeValue().equals(key)) {
				return childNode;
			}
		}
		return null;
	}

}
