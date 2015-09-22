package apex.parser;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticClass;

public class XMLParser {

	public static void parseAndroidManifest(StaticApp staticApp, String AndroidManifestPath)
	{
		try {
			File manifestFile = new File(AndroidManifestPath);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestFile);
			doc.getDocumentElement().normalize();
			Node manifestNode = doc.getFirstChild();
			String pkgName = manifestNode.getAttributes().getNamedItem("package").getNodeValue();
			staticApp.setPackageName(pkgName);
			NodeList aList = doc.getElementsByTagName("activity");
			boolean mainActFound = false;
			for (int i = 0, len = aList.getLength(); i < len; i++)
			{
				Node a = aList.item(i);
				String aName = a.getAttributes().getNamedItem("android:name").getNodeValue();
				if (aName.startsWith("."))
					aName = pkgName + aName;
				if (!aName.contains("."))
					aName = pkgName + "." + aName;
				StaticClass c = staticApp.getClassByJavaName(aName);
				if (c == null)
				{
					System.out.println("  [WARNING] Could not find activity class " + aName);
					continue;
				}
				c.setIsActivity(true);
				Element e = (Element) a;
				NodeList actions = e.getElementsByTagName("action");
				for (int j = 0, len2 = actions.getLength(); j < len2; j++)
				{
					Node action = actions.item(j);
					if (action.getAttributes().getNamedItem("android:name")
							.getNodeValue().equals("android.intent.action.MAIN"))
					{
						c.setIsMainActivity(true);
						mainActFound = true;
						break;
					}
				}
			}
			if (!mainActFound)
			{
				NodeList aaList = doc.getElementsByTagName("activity-alias");
				for (int i = 0, len = aaList.getLength(); i < len; i++)
				{
					if (mainActFound)
						break;
					Node aa = aaList.item(i);
					String aName = aa.getAttributes().getNamedItem("android:targetActivity").getNodeValue();
					if (aName.startsWith("."))
						aName = aName.substring(1, aName.length());
					if (!aName.contains("."))
						aName = pkgName + "." + aName;
					Element e = (Element) aa;
					NodeList actions = e.getElementsByTagName("action");
					for (int j = 0, len2 = actions.getLength(); j < len2; j++)
					{
						Node action = actions.item(j);
						if (action.getAttributes().getNamedItem("android:name")
								.getNodeValue().equals("android.intent.action.MAIN"))
						{
							StaticClass c = staticApp.getClassByJavaName(aName);
							c.setIsMainActivity(true);
							mainActFound = true;
							break;
						}
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
