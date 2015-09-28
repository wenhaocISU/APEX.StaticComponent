package apex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import apex.staticFamily.StaticApp;

public class AdbTest {

	
	public static void Test(StaticApp a)
	{
		uninstall(a.getPackageName());
		install(a.getInstrumentedApkPath());
		hitHome();
		startActivity(a.getPackageName(), a.getMainActivity().getJavaName());
		clearLogcat();
		click(300, 730);
		ArrayList<String> output = readLogcat("System.out:I *:S");
		for (String s : output)
			System.out.println(s);
	}
	
	public static void install(String file)
	{
		exec("adb install " + file);
	}
	
	public static void uninstall(String packageName)
	{
		exec("adb uninstall " + packageName);
	}
	
	public static void startActivity(String packageName, String activityName)
	{
		exec("adb shell am start -W -n " + packageName + "/" + activityName);
	}
	
	public static void click(int x, int y)
	{
		exec("adb shell input tap " + x + " " + y);
	}
	
	public static void hitHome()
	{
		exec("adb shell input keyevent 3");
		sleep(100);
	}
	
	public static void clearLogcat()
	{
		exec("adb logcat -c");
	}
	
	public static ArrayList<String> readLogcat(String filters)
	{
		Process p = exec("adb logcat -d " + filters);
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
				System.out.println("[out]" + line);
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
			Process p = Runtime.getRuntime().exec(command);
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
