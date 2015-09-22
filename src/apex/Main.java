package apex;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticAppBuilder;

public class Main {

	
	public static void main(String[] args)
	{
		String apkPaths[] = {
/* 0 */				"/home/wenhaoc/workspace/adt_eclipse/TheApp/bin/TheApp.apk",
					"/home/wenhaoc/AppStorage/APAC_engagement/CalcA.apk",
					"/home/wenhaoc/AppStorage/APAC_engagement/backupHelper.apk",
/* 3 */				"/home/wenhaoc/AppStorage/Jensen/net.mandaria.tippytipper.apk",
					"/home/wenhaoc/AppStorage/Jensen/com.nexes.manager.apk",
/* 5 */				"/home/wenhaoc/AppStorage/Jensen/info.bpace.munchlife.apk",
					"/home/wenhaoc/AppStorage/Jensen/net.logomancy.diedroid.apk",
					"/home/wenhaoc/AppStorage/Jensen/org.connectbot.apk",
/* 8 */				"/home/wenhaoc/AppStorage/Dragon2.apk",
					"/home/wenhaoc/Downloads/TestField.apk",
					"/home/wenhaoc/AppStorage/Dragon_API2.apk",
					"/home/wenhaoc/AppStorage/MalGenome/DroidDream/c6b7ec91f6e237978552a478306fb6e01c9f15e9.apk",
/* 12 */			"/home/wenhaoc/AppStorage/CpuspyPlus_EXPERIMENTS.apk",
					"/home/wenhaoc/AppStorage/file-explorer.apk",
					"/home/wenhaoc/AppStorage/app-debug.apk",
					"/home/wenhaoc/AppStorage/TicTacToe.apk",
/* 16 */			"/home/wenhaoc/AppStorage/com.tictactoe.wintrino-8.0.49-APK4Fun.com.apk",
					"/home/wenhaoc/AppStorage/ArrayAssignInvestigation.apk",
					"/home/wenhaoc/AppStorage/com.softcraft.englishbible-2.1.1.apk",
/* 19 */			"/home/wenhaoc/AppStorage/com.weather.Weather.apk",
					"/home/wenhaoc/AppStorage/com.rovio.angrybirds.apk",
					"/home/wenhaoc/AppStorage/notepad.apk",
/* 22 */			"/home/wenhaoc/AppStorage/com.google.android.apps.translate.apk",
					"/home/wenhaoc/AppStorage/com.facebook.katana.apk"
		};
		
		String apkPath = apkPaths[0];
		StaticApp staticApp = StaticAppBuilder.fromAPK(apkPath);
		

	}
	

}
