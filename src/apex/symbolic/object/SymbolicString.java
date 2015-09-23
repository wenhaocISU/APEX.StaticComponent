package apex.symbolic.object;

import java.util.ArrayList;

import apex.symbolic.Expression;

public class SymbolicString extends SymbolicObject{

	private ArrayList<String> builderHistory;
	
	public SymbolicString(int birthday)
	{
		super(birthday);
	}
	
	public Expression getStringExpression()
	{
		return null;
	}

}
