package apex.symbolic.context;

import apex.symbolic.value.Value;

public class Register {

	String name;
	Value value;
	boolean isParameter;
	boolean isLocked = false;
	
	Register(String name, boolean isParameter)
	{
		this.name = name;
		this.isParameter = isParameter;
	}
	
	void assign(Value v)
	{
		this.value = v;
		this.isLocked = false;
	}
	
	public Value getValue()
	{
		return this.value;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	
	public void printSnapshot()
	{
		System.out.println(" reg " + name);
		if (isParameter)
			System.out.println(" *parameter");
		if (isLocked)
			System.out.println(" *locked");
		if (this.value != null)
			this.value.print();
	}
	
}
