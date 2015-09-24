package apex.symbolic.value;

import apex.symbolic.Expression;

public class ReferenceValue implements Value {

	Expression expression;
	String type = "";
	
	public ReferenceValue(Expression address, String type)
	{
		this.expression = address.clone();
		this.type = type;
	}
	
	public String getAddress()
	{
		return this.expression.getContent();
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
			System.out.println("     *ref value: " + expression.toYicesStatement());
		else
			System.out.println("     *ref value: null");
		System.out.println("     *type: " + type);
	}

	@Override 
	public String toString()
	{
		String result = "";
		if (expression != null)
			result+=("     *ref value: [" + expression.toYicesStatement()+"]\n");
		else
			result+=("     *ref value: null\n");
		result+=("     *type: " + type);
		return result;
	}
	
	@Override
	public ReferenceValue clone()
	{
		ReferenceValue result = new ReferenceValue(this.expression.clone(), this.type);
		return result;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}
	
}
