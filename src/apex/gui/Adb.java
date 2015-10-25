package apex.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Adb {

	
	public static String adbPath = "adb";
	public static boolean debug = true;
	
	public static boolean emulator_only = true;
	public static String device_name = "";

	public static void install(String file)
	{
		exec("install " + file);
		pull_uiautomator_dump("");
	}
	
	public static void uninstall(String packageName)
	{
		exec("uninstall " + packageName);
	}
	
	public static void startActivity(String packageName, String activityName)
	{
		exec("shell am start -W -n " + packageName + "/" + activityName);
	}
	
	public static void click(int x, int y)
	{
		exec("shell input tap " + x + " " + y);
	}
	
	public static void hitHome()
	{
		exec("shell input keyevent 3");
		sleep(100);
	}
	
	public static void pull(String src, String tgt)
	{
		File tgtFile = new File(tgt);
		tgtFile.getParentFile().mkdirs();
		tgtFile.delete();
		exec("pull " + src + " " + tgt);
	}
	
	/**
	 * Retrieve output with series of commands:
	 * 
	 * 	[adb] shell uiautomator dump ***.xml
	 * 	[adb] pull ***.xml localPath
	 * 
	 * */
	public static void pull_uiautomator_dump(String localPath)
	{
		Process p = exec("shell uiautomator dump");
		ArrayList<String> output = readInputStream(p);
		if (!output.isEmpty() && output.get(0).startsWith("UI hierchary dumped to:"))
		{
			String xmlPath = output.get(0).substring(output.get(0).indexOf("/"));
			pull(xmlPath, localPath);
		}
		else
		{
			Thrower.throwException(output.get(0));
		}
	}

	
	/**
	 * Retrieve output with command: [adb] shell dumpsys window displays.
	 * Correct output should look like:

	 	WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)
	 	
  		Display: mDisplayId=0
  		
    	init=1080x1920 480dpi cur=1080x1920 app=1080x1776 rng=1080x1005-1794x1701

	 * */
	public static ArrayList<String> dumpsys_displays()
	{
		Process p = exec("shell dumpsys window displays");
		ArrayList<String> result = readInputStream(p);
		if (result.size() < 3)
		{
			System.out.println("Bad dump from \"dumpsys window displays\"");
			for (String s : result)
			{
				System.out.println("  " + s);
			}
			Thrower.throwException("BadDumpException");
		}
		return result;
	}
	
	public static void clearLogcat()
	{
		exec("logcat -c");
	}
	
	public static ArrayList<String> readLogcat(String filters)
	{
		Process p = exec("logcat -d " + filters);
		ArrayList<String> output = readInputStream(p);
		return output;
	}
	
	private static void sleep(int miliS)
	{
		try
		{
			Thread.sleep(miliS);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static ArrayList<String> readInputStream(Process p)
	{
		ArrayList<String> result = new ArrayList<String>();
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = in.readLine())!=null)
			{
				result.add(line);
			}
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	private static Process exec(String command)
	{
		try
		{
			String cmd = Adb.emulator_only? adbPath + " -e "+command: adbPath + " " + command;
			if (debug)
				System.out.println("[adb]\t" + cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			return p;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
}
