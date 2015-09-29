package apex.symbolic.object;

import apex.symbolic.Expression;
import apex.symbolic.value.Value;

public class SymbolicArray extends SymbolicObject{

	private Value length;
	
	public SymbolicArray(int birthday)
	{
		super(birthday);
	}
	
	public Expression getArrayExpression()
	{
		return this.expression;
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
