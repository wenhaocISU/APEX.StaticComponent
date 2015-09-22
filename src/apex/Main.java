package apex;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticAppBuilder;

public class Main {

	
	public static void main(String[] args)
	{
		String apkPaths[] = {
				"/home/wenhaoc/workspace/adt_eclipse/TheApp/bin/TheApp.apk",


		};
		
		String apkPath = apkPaths[0];
		StaticApp staticApp = StaticAppBuilder.fromAPK(apkPath);
		

	}
	

}
