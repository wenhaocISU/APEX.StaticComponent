package apex.symbolic.object;

import apex.symbolic.Expression;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;

public class SymbolicStringBuilder extends SymbolicObject{

	
	private Expression stringEx;

	
	SymbolicStringBuilder()
	{};
	
	
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
		if (this.stringEx == null)
		{
			this.stringEx = toAppend.getExpression().clone();
		}
		else
		{
			Expression oldStringEx = this.stringEx.clone();
			this.stringEx = new Expression("$api");
			this.stringEx.add(appendSignature);
			this.stringEx.add(oldStringEx);
			this.stringEx.add(toAppend.getExpression());
		}
		return new ReferenceValue(new Expression(this.address), "Ljava/lang/StringBuilder;");
	}
	
	public Expression toStringExpression()
	{
		if (this.stringEx != null)
			return this.stringEx.clone();
		Expression result = new Expression("$const-string");
		result.add("\"\"");
		return result;
	}
	
	public SymbolicStringBuilder clone()
	{
		SymbolicStringBuilder result = new SymbolicStringBuilder();
		result.address = this.address;
		if (this.expression != null)
			result.expression = this.expression.clone();
		if (this.stringEx != null)
			result.stringEx = this.stringEx.clone();
		return result;
	}

	public void print()
	{
		System.out.println("\n[" + this.address + "] *SymbolicStringBuilder");
		if (this.stringEx != null)
			System.out.println(" *string expression: " + this.stringEx.toYicesStatement());
	}

}
