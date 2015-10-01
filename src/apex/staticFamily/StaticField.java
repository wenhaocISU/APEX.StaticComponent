package apex.staticFamily;

import java.util.ArrayList;
import java.util.List;

import apex.parser.DEXParser;
import apex.symbolic.Expression;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.Thrower;

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
		return subSignature.substring(subSignature.indexOf(":")+1);
	}
	
	public StaticClass getDeclaringClass()
	{
		return declaringClass;
	}
	
	public boolean hasInitialValueInDeclaration()
	{
		return this.declaration.contains(" = ");
	}
	
	public LiteralValue getInitialValue()
	{
		if (this.hasInitialValueInDeclaration())
		{
			String type = this.getType();
			String value = this.declaration.substring(this.declaration.indexOf(" = ")+3);
			if (DEXParser.isPrimitiveType(type))
			{
				Expression ex = null;
				switch (type)
				{
					case "Z":
					case "B":
					case "S":
					case "C":
					case "I":
					{
						value = value.replace("0x", "");
						int literal = Integer.parseInt(value, 16);
						ex = new Expression(literal+"");
						break;
					}
					case "J":
					{
						value = value.replace("0x", "").replace("L", "");
						long literal = Long.parseLong(value, 16);
						ex = new Expression(literal+"");
						break;
					}
					case "F":
					case "D":
					{
						value = value.replace("f", "");
						if (value.startsWith("0x"))
						{
							Thrower.throwException("field initial value float/double starts with 0x...");
						}
						ex = new Expression(value);
						break;
					}
				}
				return new LiteralValue(ex, type);
			}
			else if (type.equals("Ljava/lang/String;"))
			{
				return new LiteralValue(new Expression(value), type);
			}
			else
			{
				Thrower.throwException("A non-literal type field has initial value in its declaration!");
			}
			
		}
		return null;
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
