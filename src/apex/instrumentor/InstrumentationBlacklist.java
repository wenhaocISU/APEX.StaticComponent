package apex.instrumentor;

import java.util.ArrayList;
import java.util.Arrays;

public class InstrumentationBlacklist {

	
	
	public static ArrayList<String> classes = new ArrayList<String>(Arrays.asList(
			"Landroid/app/*",
			"Landroid/support/v4/*",
			"Landroid/support/v7/*",
			"Landroid/support/annotation/*",
			"Lcom/example/androidtest/R*",
			"Lcom/actionbarsherlock/*",
			"Lcom/flurry/sdk/*",
			"Lorg/kobjects/*",
			"Lorg/ksoap2/*",
			"Lorg/kxml2/*",
			"Lorg/xmlpull/*",
			"Lnet/mandaria/tippytipperlibrary/activities/Total$5;",
			"Lcom/google/gson/*"
	));
	
	

	
	public static ArrayList<String> methods = new ArrayList<String>(Arrays.asList(
			"*doInBackground([Ljava/lang/Void;)Ljava/lang/Integer;",
			"Lcom/example/battlestat/NanoHTTPD;->serveAssets(Ljava/lang/String;Ljava/util/Properties;)Lcom/example/battlestat/NanoHTTPD$Response;",
			"Lcom/example/battlestat/NanoHTTPD;->serveFile(Ljava/lang/String;Ljava/util/Properties;Ljava/io/File;Z)Lcom/example/battlestat/NanoHTTPD$Response;",
			"Lcom/example/battlestat/NanoHTTPD$HTTPSession;->run()V"
			//"Lnet/mandaria/tippytipperlibrary/services/TipCalculatorService;->calculateTip()V",
			//"Lnet/mandaria/tippytipperlibrary/services/TipCalculatorService;->splitBill(I)V"
	));
		
	
	public static boolean classInBlackList(String className)
	{
		for (String c : classes)
		{
			if (c.endsWith("*"))
			{
				if (className.startsWith(c.substring(0, c.lastIndexOf("*"))))
					return true;
			}
			else if (c.endsWith(";"))
			{
				if (className.equals(c))
					return true;
			}
		}
		return false;
	}
	
	public static boolean methodInBlackList(String methodSig)
	{
		for (String m : methods)
		{
			if (m.startsWith("*"))
			{
				if (methodSig.endsWith(m.substring(1)))
					return true;
			}
			else
			{
				if (m.equals(methodSig))
					return true;
			}
		}
		return false;
	}
	

}
