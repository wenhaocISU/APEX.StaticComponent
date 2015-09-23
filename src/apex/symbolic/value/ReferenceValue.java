package apex.symbolic.value;

import apex.symbolic.Expression;

public class ReferenceValue extends Value {

	
	public ReferenceValue(Expression address)
	{
		super(address);
	}
	
	public String getAddress()
	{
		return this.expression.getContent();
	}
	
}
