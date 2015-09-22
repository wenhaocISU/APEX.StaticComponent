package apex.staticFamily;

import java.util.ArrayList;

import apex.parser.DEXParser;
import apex.symbolic.Expression;

public class StaticStmt {

	
	private String smaliStmt = "";
	
	private StmtDebugInfo debugInfo = null;
	
	private int id;
	
	private StaticMethod containingMethod;
	
	private ArrayList<String> array_or_switch_data = new ArrayList<String>();
	
	private boolean isInTryBlock = false;
	
	private ArrayList<String> instrumentedStmts_before = new ArrayList<String>();
	private ArrayList<String> instrumentedStmts_after = new ArrayList<String>();
	
	private Expression ex;
	
	public StaticStmt(String statement, int id, StaticMethod m)
	{
		smaliStmt = statement.trim();
		this.id = id;
		this.containingMethod = m;
	}
	
	public void setDebugInfo(ArrayList<String> debugInfo)
	{
		this.debugInfo = new StmtDebugInfo(debugInfo);
	}
	
	public void addDebugInfo(String newDebugInfo)
	{
		this.debugInfo.add(newDebugInfo);
	}
	
	public void copyBlockLabel(StaticStmt s)
	{
		this.debugInfo.copyBlockLabel(s.debugInfo);
	}
	
	public void addPrecedingStmt(String stmtToAdd)
	{
		this.instrumentedStmts_before.add(stmtToAdd);
	}
	
	public void addSucceedingStmt(String stmtToAdd)
	{
		this.instrumentedStmts_after.add(stmtToAdd);
	}
	
	public void setIsInTryBlock(boolean isInTryBlock)
	{
		this.isInTryBlock = isInTryBlock;
	}

	public void set_array_or_switch_data(ArrayList<String> array_or_switch_data)
	{
		this.array_or_switch_data = array_or_switch_data;
	}
	
/**	Getters	*/	
	public String getSmaliStmt()
	{
		return smaliStmt;
	}
	
	public String getSmaliStmtWithIDCommented()
	{
		String comment = "stmtID=[" + getStatementID() + "],blockName=[" + getBlockName() + "]";
		return smaliStmt + "    # " + comment;
	}
	
	public int getStatementID()
	{
		return id;
	}
	
	public StaticMethod getContainingMethod()
	{
		return this.containingMethod;
	}
	
	public String getBytecodeOperator()
	{
		return smaliStmt.contains(" ")? 
				 smaliStmt.substring(0, smaliStmt.indexOf(" "))
				:smaliStmt;
	}
	
	public String getBlockName()
	{
		String result = "";
		for (String s : this.debugInfo.getBlockLabel())
		{
			if (s.contains(" "))
				result += s.trim();
			else
				result += s;
		}
		return result;
	}
	
	public ArrayList<String> getPreStmtDebugInfo()
	{
		return this.debugInfo.getPreStmtSection();
	}
	
	public ArrayList<String> getPostStmtDebugInfo()
	{
		return this.debugInfo.getPostStmtSection();
	}
	
	public ArrayList<String> getInstrumentedPrecedingStmts()
	{
		return this.instrumentedStmts_before;
	}
	
	public ArrayList<String> getInstrumentedSucceedingStmts()
	{
		return this.instrumentedStmts_after;
	}

	public boolean isInTryBlock()
	{
		return isInTryBlock;
	}

	public ArrayList<String> get_array_or_switch_data()
	{
		return array_or_switch_data;
	}
	
	public ArrayList<String> getBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(this.getPreStmtDebugInfo());
		result.add("    " + this.getSmaliStmt());
		result.addAll(this.getPostStmtDebugInfo());
		return result;
	}
	
	public ArrayList<String> getInstrumentedBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		if (!this.getInstrumentedPrecedingStmts().isEmpty())
		{
			result.addAll(this.getInstrumentedPrecedingStmts());
			result.add("");
		}
		result.addAll(this.getPreStmtDebugInfo());
		result.add("    " + this.getSmaliStmtWithIDCommented());
		result.addAll(this.getPostStmtDebugInfo());
		if (!this.getInstrumentedSucceedingStmts().isEmpty())
		{
			result.add("");
			result.addAll(this.getInstrumentedSucceedingStmts());
			result.add("");
		}
		return result;
	}
	
/**	Queries	*/
	public boolean isFirstStmtOfBlock()
	{
		return this.debugInfo.isFirstStmtOfBlock();
	}
	
	public boolean isFirstStmtOfMethod()
	{
		return (this.id == 0);
	}
	
	public boolean isReturnStmt()
	{
		return (this.getBytecodeOperator().startsWith("return"));
	}
	
	public boolean isThrowStmt()
	{
		return (this.getBytecodeOperator().startsWith("throw"));
	}
	
	public boolean isIfStmt()
	{
		return (this.getBytecodeOperator().startsWith("if"));
	}
	
	public boolean isSwitchStmt()
	{
		return (this.getBytecodeOperator().endsWith("switch"));
	}
	
	public boolean hasOriginalLineNumber()
	{
		return (this.debugInfo.getOriginalLineNumber() > 0);
	}
	
	public boolean hasNoBlockLabel()
	{
		return this.debugInfo.getBlockLabel().isEmpty();
	}

	public Expression getExpression()
	{
		if (this.ex == null)
		{
			this.ex = DEXParser.generateExpression(this).clone();
		}
		return this.ex;
	}

}
