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
			"Lcom/flurry/android/*",
			"Lorg/kobjects/*",
			"Lorg/ksoap2/*",
			"Lorg/kxml2/*",
			"Lorg/xmlpull/*"
	));
	
	
	public static ArrayList<String> methods = new ArrayList<String>(Arrays.asList(
			"Lnet/mandaria/tippytipperlibrary/services/TipCalculatorService;->calculateTip()V",
			"Lnet/mandaria/tippytipperlibrary/services/TipCalculatorService;->splitBill(I)V"
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
