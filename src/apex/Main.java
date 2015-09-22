package apex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import apex.parser.SmaliParser;
import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticAppBuilder;
import apex.staticFamily.StaticClass;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;

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
	
	private static void debugging(StaticApp staticApp)
	{
		for (StaticClass c : staticApp.getClasses())
		{
			ArrayList<String> originalSmali = new ArrayList<String>();
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(c.getSmaliFilePath()));
				String line;
				while ((line = in.readLine())!= null)
				{
					originalSmali.add(line);
				}
				in.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			ArrayList<String> oldSmali = c.getBody();
			ArrayList<String> newSmali = c.getInstrumentedBody();
			
			int original = 0, old = 0, New = 0;
			for (String line : originalSmali)
			{
				if (line.startsWith("    :array_") || line.startsWith("    :pswitch_data_") || line.startsWith("    :sswitch_data_"))
					original++;
			}
			for (String line : oldSmali)
			{
				if (line.startsWith("    :array_") || line.startsWith("    :pswitch_data_") || line.startsWith("    :sswitch_data_"))
					old++;
			}
			for (String line : newSmali)
			{
				if (line.startsWith("    :array_") || line.startsWith("    :pswitch_data_") || line.startsWith("    :sswitch_data_"))
					New++;
			}
			if (original != old || original != New || old != New)
				System.out.println("======== " + c.getDexName());
		}
	}
	
	private static void testClass()
	{
		String path = "/home/wenhaoc/AppData/" +
		"AndBible.apk/apktool/oldSmali/org/apache/lucene/index/DirectoryReader.smali";
		File f = new File(path);
		SmaliParser sp = new SmaliParser(path);
		try
		{
			StaticClass c = sp.Parse();
			PrintWriter out = new PrintWriter("/home/wenhaoc/test.smali");
			for (String s : c.getInstrumentedBody())
			{
				out.write(s + "\n");
			}
			out.flush();
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
