package apex.symbolic.value;

import apex.symbolic.Expression;

public interface Value {

	public Expression getExpression();
	
	void SetExpression(Expression ex);
	
	public boolean equals(Value v);
	
	public void print();
	
	public String toString();
	
	public Value clone();
	
}
