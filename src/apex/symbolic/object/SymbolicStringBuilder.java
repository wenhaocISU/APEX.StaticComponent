package apex.symbolic.object;

import apex.symbolic.Expression;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;

public class SymbolicStringBuilder extends SymbolicObject{

	
	private Expression stringEx;
	
	public SymbolicStringBuilder(int birthday, Expression ex)
	{
		super(birthday, ex);
	}
	
	
	public void init(LiteralValue v)
	{
		this.stringEx = v.getExpression().clone();
	}
	
	public ReferenceValue append(String appendSignature, LiteralValue toAppend)
	{
		Expression oldStringEx = this.stringEx.clone();
		this.stringEx = new Expression("$api");
		this.stringEx.add(appendSignature);
		this.stringEx.add(oldStringEx);
		this.stringEx.add(toAppend.getExpression());
		return new ReferenceValue(new Expression(this.address), "Ljava/lang/StringBuilder;");
	}
	
	public Expression toStringExpression()
	{
		return this.stringEx.clone();
	}

}
