package apex.staticFamily;

import java.util.ArrayList;
import java.util.List;

public class StaticField {

	private String declaration = "";
	private ArrayList<String> fullDeclaration = new ArrayList<String>();
	
	private StaticClass declaringClass;
	
	private List<String> inCallSourceSigs = new ArrayList<String>();
	
	
	
	public StaticField(ArrayList<String> fieldDeclaration, StaticClass c)
	{
		if (fieldDeclaration.isEmpty())
			return;
		this.fullDeclaration= new ArrayList<String>(fieldDeclaration);
		this.declaration = this.fullDeclaration.get(0);
		this.declaringClass = c;
		//TODO might need to parse initial value in declaration line
		//TODO might need to parse following annotation section in the future
	}

	public void addInCallSourceSigs(String inCallSourceSig) {
		if (!this.inCallSourceSigs.contains(inCallSourceSig))
			this.inCallSourceSigs.add(inCallSourceSig);
	}
	
	
/**	Getters	*/
	public String getDeclarationLine()
	{
		return this.declaration;
	}
	
	public ArrayList<String> getFullDeclaration()
	{
		return this.fullDeclaration;
	}
	
	public List<String> getInCallSourceSigs() {
		return inCallSourceSigs;
	}
	
	public String getSubSignature()
	{
		return getDeclarationLine().substring(getDeclarationLine().lastIndexOf(" ")+1);
	}
	
	public String getSignature()
	{
		return this.declaringClass.getDexName() + "->" + this.getSubSignature();
	}
	
	public String getName() {
		String subSignature = getSubSignature();
		return subSignature.substring(0, subSignature.indexOf(":"));
	}
	
	public String getType() {
		String subSignature = getSubSignature();
		return subSignature.substring(
				subSignature.indexOf(":")+1, subSignature.length());
	}
	
	public StaticClass getDeclaringClass()
	{
		return declaringClass;
	}
	
	public boolean initalValueDeclared()
	{
		return this.declaration.contains(" = ");
	}
	
	public boolean isPublic() {
		return declaration.contains(" public ");
	}
	
	public boolean isPrivate() {
		return declaration.contains(" private ");
	}
	
	public boolean isProtected() {
		return declaration.contains(" protected ");
	}
	
	public boolean isFinal() {
		return declaration.contains(" final ");
	}
	
	public boolean isStatic() {
		return declaration.contains(" static ");
	}
	
	public boolean isSynthetic() {
		return declaration.contains(" synthetic ");
	}
}
