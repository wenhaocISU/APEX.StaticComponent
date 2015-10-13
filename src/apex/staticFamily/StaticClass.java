package apex.staticFamily;

import java.util.ArrayList;
import java.util.List;

import apex.instrumentor.InstrumentationBlacklist;
import apex.instrumentor.Instrumentor;



public class StaticClass {

	/**
	 * Class Attributes from smali file
	 * */
	private String smaliFilePath = "";
	private String instrumentedSmaliPath = "";
	private String declaration = "";
	private String superClass = "";
	private List<String> interfaces = new ArrayList<String>();
	private String sourceFileName = "";
	private ArrayList<String> classAnnotations = new ArrayList<String>();
	//private String outerClass = "";
	//private List<String> innerClasses = new ArrayList<String>();
	
	/**
	 * Class Attributes from AndroidManifest.xml
	 * */
	private boolean isActivity, isMainActivity;
	
	/**
	 * Field members and method members
	 * */
	private List<StaticField> fields = new ArrayList<StaticField>();
	private List<StaticMethod> methods = new ArrayList<StaticMethod>();
	public List<StaticField> tempFields = new ArrayList<StaticField>();
	
	
	public StaticClass(String declaration)
	{
		this.declaration = declaration;
	}
	
	public StaticClass()
	{}
	
	

/**	Setters	**/
	public void parseSuperClass(String line)
	{
		this.superClass = line.substring(line.lastIndexOf(" ")+1);
	}
	
	public void parseSourceFile(String line)
	{
		this.sourceFileName = line.substring(line.lastIndexOf(".source ")+8).replace("\"", "");
	}
	
	public void parseInterface(String line)
	{
		String interfaceName = line.substring(line.indexOf(".implements ")+12);
		this.interfaces.add(interfaceName);
	}
	
	public void parseAnnotation(ArrayList<String> classAnnotation)
	{
		//Annotations to parse:
		//".annotation system Ldalvik/annotation/MemberClasses;"
		//".annotation system Ldalvik/annotation/InnerClass;"
		//".annotation system Ldalvik/annotation/EnclosingMethod;"
		//".annotation system Ldalvik/annotation/EnclosingClass;"
		this.classAnnotations.addAll(classAnnotation);
		this.classAnnotations.add("");
	}
	
	public void addField(StaticField f)
	{
		this.fields.add(f);
	}
	
	public void addMethod(StaticMethod m)
	{
		this.methods.add(m);
	}

	public void setIsActivity(boolean isActivity)
	{
		this.isActivity = isActivity;
	}

	public void setIsMainActivity(boolean isMainActivity)
	{
		this.isMainActivity = isMainActivity;
	}
	
	public void setSmaliFilePath(String smaliFilePath)
	{
		this.smaliFilePath = smaliFilePath;
	}
	
	public void setInstrumentedSmaliFilePath(String newSmaliFilePath)
	{
		this.instrumentedSmaliPath = newSmaliFilePath;
	}
	
/**	Instrumentation related	*/
	public StaticField getTempField(String type, boolean isStatic)
	{
		for (StaticField f : this.tempFields)
		{
			if (f.getType().equals(type) && f.isStatic()==isStatic)
				return f;
		}
		String fieldName = "APEX_tempField_" + this.tempFields.size();
		String declaration = isStatic? ".field private static ": ".field private ";
		declaration += fieldName + ":" + type;
		ArrayList<String> declarations = new ArrayList<String>();
		declarations.add(declaration);
		StaticField f = new StaticField(declarations, this);
		this.tempFields.add(f);
		return f;
	}

/**	Attribute Getters **/	
	
	public String getSmaliFilePath()
	{
		return this.smaliFilePath;
	}
	
	public String getInstrumentedSmaliPath()
	{
		return this.instrumentedSmaliPath;
	}
	
	public String getJavaName()
	{
		String result = declaration.substring(
				declaration.lastIndexOf(" ")+2, 
				declaration.length()-1);
		return result.replace("/", ".");
	}
	
	public String getDexName() {
		String result = declaration.substring(
				declaration.lastIndexOf(" ")+1, 
				declaration.length());
		return result;
	}
	
	public List<StaticField> getFields() {
		return fields;
	}
	
	public List<StaticMethod> getMethods() {
		return methods;
	}
	
	public boolean isActivity()
	{
		return isActivity;
	}
	
	public boolean isMainActivity()
	{
		return isMainActivity;
	}
	
	public boolean isPublic() {
		return declaration.contains(" public ");
	}
	
	public boolean isPrivate() {
		return declaration.contains(" private ");
	}
	
	public boolean isInterface() {
		return declaration.contains(" interface ");
	}
	
	public boolean isFinal() {
		return declaration.contains(" final ");
	}
	
	public boolean isAbstract() {
		return declaration.contains(" abstract ");
	}
	
	public boolean isProtected() {
		return declaration.contains(" protected ");
	}
	
	public String getSuperClass() {
		return superClass;
	}
	
	public List<String> getInterfaces() {
		return interfaces;
	}
	
	public String getSourceFileName()
	{
		return sourceFileName;
	}

	public ArrayList<String> getBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add(this.declaration);
		result.add(".super " + this.superClass);
		if (!this.sourceFileName.equals(""))
			result.add(".source \"" + this.sourceFileName + "\"");
		result.add("");
		if (this.interfaces.size()>0)
		{
			for (String s : this.interfaces)
				result.add(".implements " + s);
			result.add("");
		}
		if (this.classAnnotations.size()>0)
		{
			for (String s : this.classAnnotations)
				result.add(s);
		}
		for (StaticField f : this.fields)
		{
			result.addAll(f.getFullDeclaration());
			result.add("");
		}
		for (StaticMethod m : this.methods)
		{
			result.addAll(m.getBody());
			result.add("");
		}
		return result;
	}
	
	public void instrument(StaticApp staticApp, Instrumentor instrumentor)
	{
		for (StaticMethod m : this.methods)
		{
			if (instrumentor.blackListOn && InstrumentationBlacklist.methodInBlackList(m.getSignature()))
			{
				m.simpleInstrument(staticApp, instrumentor);
			}
			else
			{
				m.instrument(staticApp, instrumentor);
			}
		}
	}
	
	public ArrayList<String> getInstrumentedBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add(this.declaration);
		result.add(".super " + this.superClass);
		if (!this.sourceFileName.equals(""))
			result.add(".source \"" + this.sourceFileName + "\"");
		result.add("");
		if (this.interfaces.size()>0)
		{
			for (String s : this.interfaces)
				result.add(".implements " + s);
			result.add("");
		}
		if (this.classAnnotations.size()>0)
		{
			for (String s : this.classAnnotations)
				result.add(s);
		}
		for (StaticField f : this.tempFields)
		{
			result.addAll(f.getFullDeclaration());
			result.add("");
		}
		for (StaticField f : this.fields)
		{
			result.addAll(f.getFullDeclaration());
			result.add("");
		}
		for (StaticMethod m : this.methods)
		{
			result.addAll(m.getInstrumentedBody());
			result.add("");
		}
		return result;
	}
	
/**	Field/Method Queries **/
	public StaticMethod getMethodBySignature(String methodSignature)
	{
		for (StaticMethod m : this.methods)
		{
			if (m.getSignature().equals(methodSignature))
				return m;
		}
		return null;
	}
	
	public StaticMethod getMethodBySubsignature(String methodSubsig)
	{
		for (StaticMethod m : this.methods)
		{
			if (m.getSubSignature().equals(methodSubsig))
				return m;
		}
		return null;
	}
	
	public StaticField getFieldBySubsignature(String fieldSubSig)
	{
		for (StaticField f : this.fields)
		{
			if (f.getSubSignature().equals(fieldSubSig))
				return f;
		}
		return null;
	}
	
	public StaticField getFieldBySignature(String fieldSignature)
	{
		for (StaticField f : this.fields)
		{
			if (f.getSignature().equals(fieldSignature))
				return f;
		}
		return null;
	}
	
}
