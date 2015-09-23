package apex.symbolic.value;

import apex.symbolic.Expression;

public class Value {

	Expression expression;
	String type = "";
	
	public Value(Expression ex)
	{
		this.expression = ex;
	}
	
	public Expression getExpression()
	{
		return this.expression;
	}
	
	void SetExpression(Expression ex)
	{
		this.expression = ex;
	}
	
	public boolean equals(Value v)
	{
		return this.getExpression().equals(v.getExpression());
	}
	
	public void print()
	{
		if (expression != null)
			System.out.println("     *value " + expression.toYicesStatement());
		else
			System.out.println("     *value null");
		System.out.println("      (" + type + ")");
	}
	
	public String toString()
	{
		String result = "";
		if (expression != null)
			result += ("     value: " + expression.toYicesStatement() + "\n");
		else
			result+= ("     value: null\n");
		result += ("      (" + type + ")");
		return result;
	}
	
	public Value clone()
	{
		Value result = new Value(this.expression.clone());
		result.type = this.type;
		return result;
	}
}
