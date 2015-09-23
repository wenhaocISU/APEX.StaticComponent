package apex.symbolic.context;

import apex.symbolic.value.Value;

public class Register {

	String name;
	Value value;
	boolean isParameter;
	boolean isLocked;
	
	Register(String id, boolean isParameter)
	{
		this.name = id;
		this.isParameter = isParameter;
	}
	
	void putValue(Value v)
	{
		this.value = v;
	}
	
	Value getValue()
	{
		return this.value;
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
