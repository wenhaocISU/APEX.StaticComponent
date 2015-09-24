package apex.symbolic;

import java.util.ArrayList;

import apex.symbolic.context.VMContext;
import apex.symbolic.value.Value;

public class PathSummary {

	private String methodSignature;
	private int id;
	
	private VMContext vm;
	private ArrayList<String> executionLog = new ArrayList<String>();
	private ArrayList<Expression> pathCondition = new ArrayList<Expression>();
	private ArrayList<Value> symbolicStates = new ArrayList<Value>();
	
	
	public PathSummary(VMContext vm, ToDoPath p, String methodSig, int id)
	{
		this.vm = vm;
		this.setExecutionLog(p.execLog);
		this.methodSignature = methodSig;
		this.id = id;
	}
	
	void updatePathConstraint(Expression cond)
	{
		this.pathCondition.add(this.vm.getPathCondition(cond));
	}

	void setExecutionLog(ArrayList<String> execLog)
	{
		this.executionLog = new ArrayList<String>(execLog);
	}
	
	public VMContext getVMContext()
	{
		return this.vm;
	}
	
	public boolean endsWithThrow()
	{
		return this.vm.endsWithThrow();
	}
	
	public void print()
	{
		System.out.println("\nPath Summary No." + id + " for method " + methodSignature);
		System.out.println("Execution Log:");
		for (String s : this.executionLog)
			System.out.println(" " + s);
		System.out.println("Symbolic States:");
		for (Value v : this.symbolicStates)
			System.out.println(" " + v.getExpression().toYicesStatement());
		System.out.println("Path Conditions:");
		for (Expression cond : this.pathCondition)
			System.out.println(" " + cond.toYicesStatement());
		System.out.println("\n");
	}
}
