package apex.gui;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import apex.gui.Android.Screen;

public class Traversal {

	private Screen screen;
	public static String uiautomator_dump_location = "out/uiautomator_dump.xml";
	
	public static void main(String[] args)
	{
		Traversal t = new Traversal();
		t.traverse();
	}
	
	public void traverse()
	{
		//Adb.click(200, 670);
		this.updateScreen(Adb.dumpsys_displays());
		Adb.pull_uiautomator_dump(uiautomator_dump_location);
		this.parseUIAutomatorDump(new File(uiautomator_dump_location));
	}
	
	/**
	 * UIAutomator format:
	 * The root node name is "hierarchy". Its attributes contains Device rotation value.
	 * 
	 * Its child node is the root layout node.
	 * Its "class" attributes represents layout type,
	 * "package" attribute tells the package name,
	 * "bounds" attribute tells its bound
	 * 
	 * TODO: Real question: how to tell if two View objects are different?
	 * */
	private void parseUIAutomatorDump(File file)
	{
		try{
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		doc.getDocumentElement().normalize();
		// 1. get rotation from root node
		Node hierarchy = doc.getFirstChild();
		String rotation = hierarchy.getAttributes().getNamedItem("rotation").getNodeValue();
		// 2. parse layout tree
		NodeList nodeList = hierarchy.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			System.out.println("[" + node.getNodeName() + "] id="+i);
			NamedNodeMap attrs = node.getAttributes();
			for (int j = 0; j < attrs.getLength(); j++)
			{
				Node attr = attrs.item(j);
				if (attr.getNodeValue().equals(""))
					continue;
				System.out.println(" " + attr.getNodeName() + "=" + attr.getNodeValue());
			}
		}
		}catch (Exception e) {e.printStackTrace();}
		
	}
	
	
	/**
	 * 
	 * init=1080x1920 480dpi cur=1080x1920 app=1080x1776 rng=1080x1005-1794x1701
	 * */
	private void updateScreen(ArrayList<String> displayInfo)
	{
		System.out.println("----------- display information ----------");
		for (String s : displayInfo)
		{
			if (s.contains(" ") && s.trim().startsWith("init="))
			{
				String[] displayAttributes = s.split(" ");
				for (String attr : displayAttributes)
				{
					if (attr.startsWith("init="))
					{
						String[] xy = attr.replace("init=", "").split("x");
						int x = Integer.parseInt(xy[0]);
						int y = Integer.parseInt(xy[1]);
						if (this.screen == null)
							this.screen = new Screen(x, y);
					}
					else if (attr.startsWith("cur="))
					{
						String[] xy = attr.replace("cur=", "").split("x");
						int x = Integer.parseInt(xy[0]);
						int y = Integer.parseInt(xy[1]);
						if (this.screen != null)
							this.screen.updateRotation(x, y);
					}
					else if (attr.startsWith("app="))
					{
						String[] xy = attr.replace("app=", "").split("x");
						int x = Integer.parseInt(xy[0]);
						int y = Integer.parseInt(xy[1]);
						if (this.screen != null)
							this.screen.setApplicationRect(x, y);
					}
					else if (attr.endsWith("dpi"))
					{
						int dpi = Integer.parseInt(attr.replace("dpi", ""));
						if (this.screen != null)
							this.screen.setDPI(dpi);
					}
				}
				break;
			}
		}
		this.screen.printInfo();
	}
	
}
