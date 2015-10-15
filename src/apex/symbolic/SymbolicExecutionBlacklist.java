package apex.symbolic;

import java.util.ArrayList;
import java.util.Arrays;

public class SymbolicExecutionBlacklist {

	
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
			"Lnet/mandaria/tippytipperlibrary/activities/Total$5;"
	));
	
	
	public static ArrayList<String> methods = new ArrayList<String>(Arrays.asList(
			""
	));
	
	
	public static boolean classInBlackList(String className) {
		for (String c : classes) {
			if (c.endsWith("*")) {
				if (className.startsWith(c.substring(0, c.lastIndexOf("*"))))
					return true;
			}
			else if (c.endsWith(";")) {
				if (className.equals(c))
					return true;
			}
		}
		return false;
	}
	
	public static boolean methodInBlackList(String methodSig) {
		return methods.contains(methodSig);
	}
	
}
