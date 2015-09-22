package apex.symbolic.context;

import apex.symbolic.Expression;

public class SymbolicArray extends SymbolicObject{

	private Value length;
	private Expression arrayExpression;
	
	public Expression getArrayExpression()
	{
		return this.arrayExpression;
	}
	
	public Value getLength()
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
