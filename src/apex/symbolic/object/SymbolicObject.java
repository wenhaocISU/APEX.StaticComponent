package apex.symbolic.object;

import java.util.HashMap;
import java.util.Map;

import apex.symbolic.Expression;
import apex.symbolic.value.Value;

public class SymbolicObject{

	String address;
	Expression expression;
	Map<String, Value> members = new HashMap<String, Value>();
	
	public SymbolicObject(int birthday)
	{
		this.address = "#obj_" + birthday;
	}
	
	public SymbolicObject(int birthday, Expression ex)
	{
		this.address = "#obj_" + birthday;
		this.expression = ex;
	}
	
	public String getAddress()
	{
		return this.address;
	}
	
	public Expression getExpression()
	{
		return this.expression;
	}
	
	public void putField(String fieldSig, Value value)
	{
		members.put(fieldSig, value);
	}
	
	public void getField(String fieldSig)
	{
		//TODO
	}
	
	public void print()
	{
		System.out.println("\n[" + this.address + "]");
		if (this.expression != null)
		{
			System.out.println(" *expression: " + this.expression.toYicesStatement());
		}
		if (!this.members.isEmpty())
		{
			System.out.println(" *members:");
			for (Map.Entry<String, Value> entry : this.members.entrySet())
			{
				System.out.println("  **key: " + entry.getKey());
				System.out.println("  **Value: " + entry.getValue().toString());
			}
		}
	}
	
}
