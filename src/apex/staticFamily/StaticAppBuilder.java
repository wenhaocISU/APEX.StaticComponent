package apex.staticFamily;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tools.Apktool;
import apex.Paths;
import apex.parser.SmaliParser;
import apex.parser.XMLParser;

public class StaticAppBuilder {

	public static boolean decodeRes = true;
	public static boolean multiThreading = true;
	
	public static StaticApp fromAPK(String apkFilePath)
	{
		File apk = new File(apkFilePath);
		if (!apk.exists() || !apk.isFile() || !apkFilePath.endsWith(".apk"))
		{
			System.out.println("[ERROR] Not a valid APK file: " + apkFilePath);
			return null;
		}
		String outFolder = Paths.AppDataDir.replace("/", File.separator) + File.separator + apk.getName() + File.separator + "apktool"; 
		Apktool.extractAPK(apk.getAbsolutePath(), outFolder, decodeRes);
		StaticApp staticApp = fromApktoolOutFolder(outFolder);
		staticApp.setApkPath(apk.getAbsolutePath());
		return staticApp;
	}
	
	
	/**
	 * The format of 'apktoolOutFolder' must ends with: ".../[Name.apk]/apktool".
	 * */
	public static StaticApp fromApktoolOutFolder(String apktoolOutFolder)
	{
		StaticApp staticApp = new StaticApp();
		
		File smaliFolder = new File(apktoolOutFolder + File.separator + "oldSmali");
		if (!smaliFolder.exists())
		{
			smaliFolder = new File(apktoolOutFolder + File.separator + "smali");
		}
		
		System.out.print("Parsing smali files. Might take a while...");
		if (multiThreading)
		{
			doMultiThread(staticApp, smaliFolder);
		}
		else
		{
			parseSmaliFiles(staticApp, smaliFolder);
		}
		System.out.println(" Done.");
		
		XMLParser.parseAndroidManifest(staticApp, apktoolOutFolder + File.separator + "AndroidManifest.xml");
		
		String outFolder = apktoolOutFolder.substring(0, apktoolOutFolder.lastIndexOf("apktool"));
		staticApp.setDataFolder(outFolder);
		
		System.out.println("Parsing Finished.\n");
		
		return staticApp;
	}
	
	private static ArrayList<SmaliParser> smaliParsers = new ArrayList<SmaliParser>();
	private static void doMultiThread(StaticApp staticApp, File smaliFolder)
	{
		initSmaliParsers(smaliFolder);
		int threadCount = Runtime.getRuntime().availableProcessors();
		threadCount = 1;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		for (SmaliParser sp : smaliParsers)
		{
			try
			{
				StaticClass c = executor.submit(sp).get();
				staticApp.addClass(c);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		executor.shutdown();
		smaliParsers.clear();
	}
	
	private static void initSmaliParsers(File smaliFile)
	{
		if (smaliFile.isDirectory())
		{
			for (File f : smaliFile.listFiles())
			{
				initSmaliParsers(f);
			}
		}
		else if (smaliFile.isFile() && smaliFile.getName().endsWith(".smali"))
		{
			SmaliParser sp = new SmaliParser(smaliFile);
			smaliParsers.add(sp);
		}
	}
	
	private static void parseSmaliFiles(StaticApp staticApp, File smaliFile)
	{
		if (smaliFile.isDirectory())
		{
			for (File f : smaliFile.listFiles())
			{
				parseSmaliFiles(staticApp, f);
			}
		}
		else if (smaliFile.isFile() && smaliFile.getName().endsWith(".smali"))
		{
			SmaliParser sp = new SmaliParser(smaliFile);
			try
			{
				staticApp.addClass(sp.Parse());
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
