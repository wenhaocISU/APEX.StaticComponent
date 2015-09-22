package apex.symbolic.context;

import apex.symbolic.Expression;

public class Value {

	private Expression expression;
	
	
	public Expression getExpression()
	{
		return this.expression;
	}
	
	public boolean equals(Value v)
	{
		return this.getExpression().equals(v.getExpression());
	}
	
}
