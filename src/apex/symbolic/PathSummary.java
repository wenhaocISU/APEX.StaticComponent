package apex.symbolic;

import java.util.ArrayList;
import java.util.Map;

import apex.staticFamily.StaticStmt;
import apex.symbolic.context.VMContext;
import apex.symbolic.object.SymbolicObject;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Value;

public class PathSummary {

	private String methodSignature;
	private int id;
	
	private VMContext vm;
	private ToDoPath p;
	private ArrayList<String> executionLog = new ArrayList<String>();
	private ArrayList<Expression> pathCondition = new ArrayList<Expression>();
	private ArrayList<Expression> symbolicStates = null;

		
	public PathSummary(VMContext vm, ToDoPath p, String methodSig, int id)
	{
		this.vm = vm;
		this.p = p;
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
	
	void setPathCondition(ArrayList<Expression> pathConditions)
	{
		this.pathCondition = new ArrayList<Expression>();
		for (Expression ex : pathConditions)
		{
			this.pathCondition.add(ex.clone());
		}
	}
	
	public ArrayList<String> getExecutionLog()
	{
		return this.executionLog;
	}
	
	public ArrayList<String> getBranchExecutionLog()
	{
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < this.executionLog.size(); i++)
		{
			String log = this.executionLog.get(i);
			if (log.contains(",jump")
					|| log.contains(",flow")
					|| log.contains(",case"))
			{
				result.add(log);
			}
		}
		return result;
	}
	
	public boolean containsStmt(StaticStmt s)
	{
		for (String ex : this.executionLog)
		{
			String stmtInfo = ex.contains(",")? ex.substring(0, ex.indexOf(",")) : ex;
			if (s.getUniqueID().equals(stmtInfo))
				return true;
		}
		return false;
	}
	
	public ArrayList<String> getSourceCodeLog()
	{
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < this.executionLog.size(); i++)
		{
			String line = this.executionLog.get(i);
			String stmtInfo = line.contains(",")? line.substring(0, line.indexOf(",")) : line;
			StaticStmt s = this.vm.getStaticApp().getStmt(stmtInfo);
			if (s.hasSourceLineNumber())
			{
				result.add(s.getContainingMethod().getDeclaringClass().getJavaName() + ":" + s.getSourceLineNumber());
			}
		}
		return result;
	}
	
	public String getMethodSignature()
	{
		return this.methodSignature;
	}
	
	public ArrayList<Expression> getPathConditions()
	{
		return this.pathCondition;
	}
	
	public VMContext getVMContext()
	{
		return this.vm;
	}
		
	/**
	 * Return a PathSummary that is the concatenation of the two PathSummaries.
	 * Equivalent to performing a symbolic execution on the new
	 * execution log starting from the old VMContext.
	 * */
	public PathSummary concat(PathSummary ps)
	{
		SymbolicExecution sex = new SymbolicExecution(this.vm.getStaticApp());
		//sex.printStmtInfo = true;
		String concatSig = this.methodSignature + " concat " + ps.methodSignature;
		if (ps.methodSignature.contains(" concat "))
		{
			System.out.println();
		}
		PathSummary result = sex.doFullSymbolic(this.vm.copy(), this.p.concat(ps.p), this.pathCondition, concatSig, this.id + ps.id);
		result.executionLog = new ArrayList<String>(this.executionLog);
		result.executionLog.addAll(ps.executionLog);
		return result;
	}
	
	/**
	 * Return whether current PathSummary is relevant to the
	 * Path Conditions of the PathSummary parameter.
	 * */
	public boolean isRelevantToConstraint(PathSummary ps)
	{
		ArrayList<Expression> states = this.getSymbolicStates();
		for (Expression cond : ps.pathCondition)
		{
			for (int i = 0; i < cond.getChildCount(); i++)
			{
				Expression child = cond.getChild(i);
				for (Expression state : states)
				{
					if (state.getChild(0).equals(child))
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns whether this path summary's execution log is equivalent
	 * to the logcat output.
	 * 
	 * */
	public boolean matchesExecutionLog(ArrayList<String> logcatOutput)
	{
		SymbolicExecution sex = new SymbolicExecution(this.vm.getStaticApp());
		ArrayList<String> fullExecLog = sex.expandLogcatOutput(logcatOutput).execLog;
		for (String s : fullExecLog)
		{
			System.out.println(s);
		}
		System.out.println("------------------------------");
		for (String s : this.executionLog)
		{
			System.out.println(s);
		}
		if (fullExecLog.size() != this.executionLog.size())
		{
			return false;
		}
		for (int i = 0; i < fullExecLog.size(); i++)
		{
			if (!fullExecLog.get(i).equals(this.executionLog.get(i)))
				return false;
		}
		return true;
	}	
	
	public ArrayList<Expression> getSymbolicStates()
	{
		if (this.symbolicStates == null)
		{
			this.symbolicStates = new ArrayList<Expression>();
			for (SymbolicObject obj : this.vm.getSymbolicObjects())
			{
				obj.checked = false;
			}
			for (SymbolicObject obj : this.vm.getSymbolicObjects())
			{
				Expression objEx = obj.getExpression();
				if (objEx.getContent().equals("$this") || 
						objEx.getContent().startsWith("$p") ||
						objEx.getContent().equals("$static-fields"))
				{
					obj.checked = true;
					Map<String, Value> fields = obj.getFields();
					for (Map.Entry<String, Value> entry : fields.entrySet())
					{
						reportFieldState(entry, objEx);
					}
				}
			}
		}
		return this.symbolicStates;
	}
	
	/**
	 * Each field has its unique signature, which is called "symbolic state".
	 * If a field has a value that is different from its symbolic state, then
	 * it will be reported into PathSummary's Symbolic States.
	 * */
	private void reportFieldState(Map.Entry<String, Value> entry, Expression objSymbolicExpression)
	{
		Expression symbolicState = new Expression("$Finstance");
		symbolicState.add(entry.getKey());
		symbolicState.add(objSymbolicExpression.clone());
		if (objSymbolicExpression.getContent().equals("$static-fields"))
		{
			symbolicState = new Expression("$Fstatic");
			symbolicState.add(entry.getKey());
		}
		if (entry.getValue() instanceof LiteralValue)
		{
			Expression concreteState = entry.getValue().getExpression();
			if (!concreteState.equals(symbolicState))
			{
				Expression out = new Expression("=");
				out.add(symbolicState.clone());
				out.add(concreteState.clone());
				this.symbolicStates.add(out);
			}
		}
		else if (entry.getValue() instanceof ReferenceValue)
		{
			SymbolicObject fieldObj = this.vm.getObject(entry.getValue().getExpression().getContent());
			if (!fieldObj.checked)
			{
				Expression concreteState = fieldObj.getExpression();
				if (!concreteState.equals(symbolicState))
				{
					Expression out = new Expression("=");
					out.add(symbolicState.clone());
					out.add(concreteState.clone());
					this.symbolicStates.add(out);
				}
				fieldObj.checked = true;
				for (Map.Entry<String, Value> nextLevelEntry : fieldObj.getFields().entrySet())
				{
					reportFieldState(nextLevelEntry, symbolicState);
				}
			}
		}
	}	
	
	public boolean endsWithThrow()
	{
		return this.vm.endsWithThrow();
	}
	
	public void printVMContext()
	{
		this.vm.printSnapshot();
	}
	
	
	public PathSummary clone()
	{
		PathSummary result = new PathSummary(this.vm.clone(), this.p.clone(), this.methodSignature, this.id);
		for (Expression cond : this.pathCondition)
		{
			result.pathCondition.add(cond.clone());
		}
		return result;
	}
	
	public void print()
	{
		System.out.println("\n_______________________________________________________________________");
		System.out.println("Path Summary No." + id + " for method " + methodSignature);
		System.out.println("Execution Log:");
		for (String s : this.executionLog)
		{
			String stmtInfo = s.contains(",")? s.substring(0, s.indexOf(",")) : s;
			StaticStmt stmt = this.vm.getStaticApp().getStmt(stmtInfo);
			String id = stmtInfo.substring(stmtInfo.indexOf(":")+1);
			if (stmt.isFirstStmtOfMethod())
				System.out.println(stmt.getContainingMethod().getDeclaration());
			if (stmt.isIfStmt() || stmt.isSwitchStmt())
			{
				String choice = s.substring(s.indexOf(",")+1);
				System.out.println(" " + id + "\t" + stmt.getSmaliStmt() + "\t(" + choice + ")");
			}
			else
			{
				System.out.println(" " + id + "\t" + stmt.getSmaliStmt());
			}
			if (stmt.isReturnStmt() || stmt.isThrowStmt())
			{
				System.out.println(".end " + stmt.getContainingMethod().getDeclaration().substring(1));
			}
		}
		System.out.println("Source Code Log:");
		for (String s : this.getSourceCodeLog())
		{
			System.out.println(" " + s);
		}
		System.out.println("Symbolic States:");
		ArrayList<Expression> states = this.getSymbolicStates();
		if (!states.isEmpty())
		{
			for (Expression ex : this.getSymbolicStates())
				System.out.println(" " + ex.toYicesStatement());
		}
		else
		{
			System.out.println(" <empty>");
		}
		System.out.println("Path Conditions:");
		if (!this.pathCondition.isEmpty())
		{
			for (Expression cond : this.pathCondition)
				System.out.println(" " + cond.toYicesStatement());
		}
		else
		{
			System.out.println(" <empty>");
		}
		System.out.println("\n");
	}
}
