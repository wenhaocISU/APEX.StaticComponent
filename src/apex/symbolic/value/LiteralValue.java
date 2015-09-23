package apex.symbolic.value;

import apex.symbolic.Expression;

public class LiteralValue extends Value{

	private String type;
	
	
	public LiteralValue(Expression ex)
	{
		super(ex);
	}
	
	public String getType()
	{
		return this.type;
	}
	
}
