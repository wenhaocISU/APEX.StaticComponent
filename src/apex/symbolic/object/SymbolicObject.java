package apex.symbolic.object;

import java.util.HashMap;
import java.util.Map;

import apex.symbolic.Expression;
import apex.symbolic.value.Value;

public class SymbolicObject{

	String address;
	Expression expression;
	Map<String, Value> members = new HashMap<String, Value>();
	

	
	private SymbolicObject()
	{}
	
	public SymbolicObject(int birthday)
	{
		this.address = "#obj_" + birthday;
	}
	
	public SymbolicObject(int birthday, Expression ex)
	{
		this(birthday);
		this.expression = ex.clone();
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
	
	public Value getField(String fieldSig)
	{
		return members.get(fieldSig);
	}
	
	public Map<String, Value> getFields()
	{
		return this.members;
	}
	
	public SymbolicObject clone()
	{
		SymbolicObject result = new SymbolicObject();
		result.address = this.address;
		result.expression = this.expression==null?null:this.expression.clone();
		for (Map.Entry<String, Value> entry : this.members.entrySet())
		{
			result.members.put(entry.getKey(), entry.getValue().clone());
		}
		return result;
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
				System.out.println("  **Value:\n" + entry.getValue().toString());
			}
		}
	}
	
}
