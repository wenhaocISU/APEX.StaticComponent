package apex.symbolic.object;

import apex.symbolic.Expression;
import apex.symbolic.value.Value;

public class SymbolicArray extends SymbolicObject{

	private int length;
	private String elementType;
	
	public SymbolicArray(int birthday, Expression initialArrayEx)
	{
		//TODO
		super(birthday);
		if (initialArrayEx.getContent().equals("$array"))
		{
			this.length = Integer.parseInt(initialArrayEx.getChild(0).getContent());
			this.elementType = initialArrayEx.getChild(1).getContent();
		}
		else
		{
			this.expression = initialArrayEx.clone();
		}
	}
	
	public Expression getArrayExpression()
	{
		return this.expression;
	}
	
	public String getArrayType()
	{
		return "[" + this.elementType;
	}
	
	public String getElementType()
	{
		return this.elementType;
	}
	
	public int getLength()
	{
		return this.length;
	}
	
	
	public void aput(Value index, Value value)
	{
		//TODO
	}
	
	public Value aget(Value index)
	{
		//TODO
		return null;
	}
	
}
