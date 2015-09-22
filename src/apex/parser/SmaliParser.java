package apex.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import apex.staticFamily.StaticClass;
import apex.staticFamily.StaticField;
import apex.staticFamily.StaticMethod;

public class SmaliParser implements Callable<StaticClass>{
	
	private String originalSmaliPath = "";
	private String instrumentedSmaliPath = "";
	
	public SmaliParser(String smaliFilePath)
	{
		this.originalSmaliPath = smaliFilePath;
		this.instrumentedSmaliPath = smaliFilePath;
		this.moveSmaliFile();
	}
	
	// put a copy of the original smali files into "/oldSmali" folder
	// because later instrumentation will overwrite the "/smali" folder
	private void moveSmaliFile()
	{
		try
		{
			String targetPath = this.originalSmaliPath.replaceFirst("/smali/", "/oldSmali/");
			File outFile = new File(targetPath);
			outFile.getParentFile().mkdirs();
			PrintWriter out = new PrintWriter(new FileWriter(targetPath));
			BufferedReader in = new BufferedReader(new FileReader(this.originalSmaliPath));
			String line;
			while ((line = in.readLine())!=null)
			{
				out.write(line + "\n");
			}
			out.flush();
			out.close();
			in.close();
			this.originalSmaliPath = targetPath;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public StaticClass Parse() throws IOException
	{
		StaticClass c = null;
		BufferedReader in = new BufferedReader(new FileReader(this.originalSmaliPath));
		String line;
		while ((line = in.readLine())!=null)
		{
			if (line.startsWith(".class "))
			{
				c = new StaticClass(line);
			}
			else if (line.startsWith(".super "))
			{
				c.parseSuperClass(line);
			}
			else if (line.startsWith(".source "))
			{
				c.parseSourceFile(line);
			}
			else if (line.startsWith(".implements "))
			{
				c.parseInterface(line);
			}
			else if (line.startsWith(".annotation "))
			{
				ArrayList<String> classAnnotation = new ArrayList<String>();
				classAnnotation.add(line);
				while ((line = in.readLine())!= null && !line.equals(".end annotation"))
				{
					classAnnotation.add(line);
				}
				if (line != null && line.equals(".end annotation"))
					classAnnotation.add(line);
				c.parseAnnotation(classAnnotation);
			}
			else if (line.startsWith(".field "))
			{
				ArrayList<String> fieldDeclaration = new ArrayList<String>();
				fieldDeclaration.add(line);
				line = in.readLine();
				if (line != null && line.startsWith("    .annotation"))
				{
					fieldDeclaration.add(line);
					while ((line = in.readLine())!= null && !line.equals(".end field"))
					{
						fieldDeclaration.add(line);
					}
					if (line != null && line.equals(".end field"))
						fieldDeclaration.add(line);
				}
				StaticField f = new StaticField(fieldDeclaration, c);
				c.addField(f);
			}
			else if (line.startsWith(".method "))
			{
				ArrayList<String> methodDeclaration = new ArrayList<String>();
				methodDeclaration.add(line);
				while ((line = in.readLine())!= null && !line.equals(".end method"))
				{
					methodDeclaration.add(line);
				}
				if (line != null && line.equals(".end method"))
					methodDeclaration.add(line);
				StaticMethod m = new StaticMethod(methodDeclaration, c);
				c.addMethod(m);
			}
		}
		in.close();
		if (c != null)
		{
			c.setSmaliFilePath(this.originalSmaliPath);
			c.setInstrumentedSmaliFilePath(this.instrumentedSmaliPath);
		}
		return c;
	}

	@Override
	public StaticClass call() throws Exception
	{
		return this.Parse();
	}
	
	
}
