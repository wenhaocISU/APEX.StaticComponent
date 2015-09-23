package apex.symbolic;

import java.util.ArrayList;

import apex.symbolic.context.VMContext;
import apex.symbolic.value.Value;

public class PathSummary {

	private String methodSignature;
	private int id;
	private boolean endsWithThrow;
	
	private ArrayList<String> executionLog = new ArrayList<String>();
	private ArrayList<Expression> pathCondition = new ArrayList<Expression>();
	private ArrayList<Value> symbolicStates = new ArrayList<Value>();
	
	
	public PathSummary(String methodSig, int id)
	{
		this.methodSignature = methodSig;
		this.id = id;
	}
	
	public void updatePathConstraint(VMContext vm, Expression cond)
	{
		
	}
	
	public void setSymbolicStates(VMContext vm)
	{
		
	}

	public void setExecutionLog(ArrayList<String> execLog)
	{
		this.executionLog = new ArrayList<String>(execLog);
	}
	
	public void print()
	{
		System.out.println("Path Summary No." + id + " for method " + methodSignature);
		System.out.println("Execution Log:");
		for (String s : this.executionLog)
			System.out.println(" " + s);
		System.out.println("Symbolic States:");
		for (Value v : this.symbolicStates)
			System.out.println(" " + v.getExpression().toYicesStatement());
		System.out.println("Path Conditions:");
		for (Expression cond : this.pathCondition)
			System.out.println(" " + cond.toYicesStatement());
	}
}
