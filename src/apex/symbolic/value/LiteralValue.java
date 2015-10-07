package apex.symbolic.value;

import apex.symbolic.Expression;

public class LiteralValue implements Value{

	Expression expression;
	String type = "";
	
	public LiteralValue(Expression ex, String type)
	{
		this.expression = ex.clone();
		this.type = type;
	}
	
	@Override
	public String getType()
	{
		return this.type;
	}

	@Override
	public Expression getExpression()
	{
		return this.expression;
	}

	@Override
	public void SetExpression(Expression ex)
	{
		this.expression = ex;
	}

	@Override
	public boolean equals(Value v)
	{
		return this.expression.equals(v.getExpression());
	}

	@Override
	public void print()
	{
		if (expression != null)
			System.out.println("  *literal value: " + expression.toYicesStatement());
		else
			System.out.println("  *literal value: null");
		System.out.println("  *type: " + type);
	}
	
	@Override 
	public String toString()
	{
		String result = "";
		if (expression != null)
			result+=("  *literal value: " + expression.toYicesStatement()+"\n");
		else
			result+=("  *literal value: null\n");
		result+=("  *type: " + type);
		return result;
	}

	@Override
	public LiteralValue clone()
	{
		return new LiteralValue(this.expression.clone(), this.type);
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}
	
}
