package apex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import apex.staticFamily.StaticApp;

public class AdbTest {

	
	public static String adbPath = "/home/wenhaoc/bin/AndroidSDK/platform-tools/adb";
	
	public static void Test(StaticApp a, int x, int y)
	{
		uninstall(a.getPackageName());
		install(a.getInstrumentedApkPath());
		hitHome();
		startActivity(a.getPackageName(), a.getMainActivity().getJavaName());
		clearLogcat();
		click(x, y);
		ArrayList<String> output = readLogcat("System.out:I *:S");
		for (String s : output)
			System.out.println(s);
	}
	
	public static void install(String file)
	{
		exec("install " + file);
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
			Process p = Runtime.getRuntime().exec(adbPath + " " + command);
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
