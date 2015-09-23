package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import apex.Paths;

public class Apktool {

	public static void extractAPK(String apkPath, String outDir, boolean decodeRes)
	{
		String command = 
			decodeRes?
				"java -jar " + Paths.apktoolPath
						+ " d -f -o " + outDir
						+ " " + apkPath
			: 	"java -jar " + Paths.apktoolPath
						+ " d -r -f -o " + outDir
						+ " " + apkPath;
		
		System.out.println("Using Apktool to extract " + apkPath + "...");
		try {
			Process pc = Runtime.getRuntime().exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			BufferedReader in_err = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
			String line;
			while ((line = in.readLine()) != null)
				System.out.println("   " + line);
			while ((line = in_err.readLine()) != null)
				System.out.println("   " + line);
			in.close();
			in_err.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void buildAPK(String sourceDir, String outPath)
	{
		//TODO check if newer version of apktool still
		//has the missing resource name error.
		String command = "java -jar " + Paths.apktoolPath
						//+ " b -f -a " + Paths.aaptPath
						+ " b -f"
						+ " -o " + outPath.replace("/", File.separator)
						+ " " + sourceDir.replace("/", File.separator);
		System.out.println("Compiling instrumented smali code into APK file...");
		try {
			Process pc = Runtime.getRuntime().exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			BufferedReader in_err = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
			String line;
			while ((line = in.readLine()) != null)
				System.out.println("   " + line);
			while ((line = in_err.readLine()) != null)
				System.out.println("   " + line);
			in.close();
			in_err.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
