package apex.symbolic.object;

import java.util.ArrayList;

import apex.symbolic.Expression;

public class SymbolicString extends SymbolicObject{

	private ArrayList<String> builderHistory;
	private String initValue = "";
	
	public SymbolicString(int birthday, String initValue)
	{
		super(birthday);
		this.initValue = initValue;
	}
	
	public Expression getStringExpression()
	{
		return null;
	}

}
