package apex.staticFamily;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import tools.Apktool;
import tools.Jarsigner;
import apex.instrumentor.InstrumentationBlacklist;
import apex.instrumentor.Instrumentor;

public class StaticApp {

	private String packageName;
	private List<StaticClass> classes = new ArrayList<StaticClass>();
	private String dataFolder = "";
	private String apkPath = "";

	StaticApp() {}
	
/**	Setters	*/
	public void addClass(StaticClass c)
	{
		if (c != null)
			this.classes.add(c);
	}
	
	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}
	
	void setApkPath(String apkPath)
	{
		this.apkPath = apkPath;
	}
	
	void setDataFolder(String dataFolder)
	{
		this.dataFolder = dataFolder;
	}
	
	/**
	 * Instrumentation:
	 * 1. add "libs/Println.smali" into Classes.dex
	 * 2. add System.out.println() in each methods at important points. Check
	 * apex.instrumentor.Instrumentor for details
	 * */
	public void instrument()
	{
		System.out.println("Instrumenting...");
		Instrumentor instrumentor = new Instrumentor();
		instrumentor.addSmaliFile(
				new File("libs"+File.separator+"Println.smali"),
				this.dataFolder+"apktool"+File.separator+"smali"
				+File.separator+"apex"+File.separator+"instrumented"
				+File.separator+"Println.smali");
		for (StaticClass c : this.classes)
		{
			if (instrumentor.blackListOn && InstrumentationBlacklist.classInBlackList(c.getDexName()))
				continue;
			File smaliFile = new File(c.getInstrumentedSmaliPath());
			smaliFile.getParentFile().mkdirs();
			try
			{
				c.instrument(this, instrumentor);
				PrintWriter out = new PrintWriter(new FileWriter(smaliFile));
				ArrayList<String> newSmali = c.getInstrumentedBody();
				for (String s : newSmali)
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
		File unsignedApp = new File(this.getUnsignedApkPath());
		File signedApp = new File(this.getInstrumentedApkPath());
		if (unsignedApp.exists())
			unsignedApp.delete();
		if (signedApp.exists())
			signedApp.delete();
		Apktool.buildAPK(this.dataFolder + File.separator + "apktool", this.getUnsignedApkPath());
		Jarsigner.signAPK(this.getUnsignedApkPath(), this.getInstrumentedApkPath());
	}
	
	/**	debugging purpose only	*/
	public void instrumentSome(int limit)
	{
		int i = 0;
		for (StaticClass c : this.classes)
		{
			System.out.println("====== Instrumented  " + c.getDexName());
			File smaliFile = new File(c.getInstrumentedSmaliPath());
			smaliFile.getParentFile().mkdirs();
			try
			{
				PrintWriter out = new PrintWriter(new FileWriter(smaliFile));
				ArrayList<String> newSmali = c.getInstrumentedBody();
				for (String s : newSmali)
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
			i++;
			if (i > limit)
				break;
		}
		File unsignedApp = new File(this.getUnsignedApkPath());
		File signedApp = new File(this.getInstrumentedApkPath());
		if (unsignedApp.exists())
			unsignedApp.delete();
		if (signedApp.exists())
			signedApp.delete();
		Apktool.buildAPK(this.dataFolder + File.separator + "apktool", this.getUnsignedApkPath());
		Jarsigner.signAPK(this.getUnsignedApkPath(), this.getInstrumentedApkPath());
	}
	
	/**	debugging purpose only	*/
	public void instrumentWithout(String classDexName)
	{
		for (StaticClass c : this.classes)
		{
			if (c.getDexName().equals(classDexName))
				continue;
			File smaliFile = new File(c.getInstrumentedSmaliPath());
			smaliFile.getParentFile().mkdirs();
			try
			{
				PrintWriter out = new PrintWriter(new FileWriter(smaliFile));
				ArrayList<String> newSmali = c.getInstrumentedBody();
				for (String s : newSmali)
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
		File unsignedApp = new File(this.getUnsignedApkPath());
		File signedApp = new File(this.getInstrumentedApkPath());
		if (unsignedApp.exists())
			unsignedApp.delete();
		if (signedApp.exists())
			signedApp.delete();
		Apktool.buildAPK(this.dataFolder + "/apktool", this.getUnsignedApkPath());
		Jarsigner.signAPK(this.getUnsignedApkPath(), this.getInstrumentedApkPath());
	}
	
	
/**	Getters	*/
	public String getPackageName()
	{
		return packageName;
	}
	
	public String getApkPath()
	{
		return this.apkPath;
	}
	
	public String getDataFolder()
	{
		return this.dataFolder;
	}
	
	public List<StaticClass> getClasses()
	{
		return classes;
	}
	
	public StaticClass getClassByJavaName(String classJavaName)
	{
		for (StaticClass c : classes)
			if (c.getJavaName().equals(classJavaName))
				return c;
		return null;
	}

	public StaticClass getClassByDexName(String classDexName)
	{
		for (StaticClass c : classes)
			if (c.getDexName().equals(classDexName))
				return c;
		return null;
	}
	
	public StaticMethod getMethod(String methodSignature)
	{
		if (!methodSignature.contains("->"))
			return null;
		String className = methodSignature.substring(0, methodSignature.indexOf("->"));
		StaticClass c = getClassByDexName(className);
		if (c != null)
		{
			return c.getMethodBySignature(methodSignature);
		}
		return null;
	}
	
	public StaticField getField(String fieldSignature)
	{
		if (!fieldSignature.contains("->"))
			return null;
		String className = fieldSignature.substring(0, fieldSignature.indexOf("->"));
		String subSig = fieldSignature.substring(fieldSignature.indexOf("->")+2);
		StaticClass c = getClassByDexName(className);
		if (c != null)
			return c.getFieldBySubsignature(subSig);
		return null;
	}
	
	public StaticClass getMainActivity()
	{
		for (StaticClass c : this.classes)
			if (c.isMainActivity())
				return c;
		return null;
	}
	
	public String getInstrumentedApkPath() {
		String result = this.dataFolder + File.separator;
		result += apkPath.substring(apkPath.lastIndexOf(File.separator)+1, apkPath.lastIndexOf(".apk"));
		result += "_instrumented.apk";
		
		return result;
	}

	public String getUnsignedApkPath() {
		String result = this.dataFolder + File.separator;
		result += apkPath.substring(apkPath.lastIndexOf(File.separator)+1, apkPath.lastIndexOf(".apk"));
		result += "_unsigned.apk";
		return result;
	}

	public StaticStmt getStmt(String stmtInfo) {
		String methodSig = stmtInfo.split(":")[0];
		int stmtID = Integer.parseInt(stmtInfo.split(":")[1]);
		StaticMethod m = this.getMethod(methodSig);
		if (stmtID >= 0 && m != null && m.getStatements().size() > stmtID)
			return m.getStatements().get(stmtID);
		return null;
	}
}
