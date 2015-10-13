package apex.staticFamily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apex.parser.DEXParser;
import apex.symbolic.Expression;

public class StaticStmt {

	/**	basic or unparsed information	*/
	private String smaliStmt = "";
	private StmtDebugInfo debugInfo = null;
	private int id;
	private StaticMethod containingMethod;
	private ArrayList<String> array_or_switch_data = new ArrayList<String>();
	private boolean isInTryBlock = false;
	
	/**	instrumentation related	*/
	private ArrayList<String> instrumentedStmts_before = new ArrayList<String>();
	private ArrayList<String> instrumentedStmts_after = new ArrayList<String>();
	
	/**	symbolic execution related	*/
	private Expression ex;
	private List<String> regsToRead = null;
	private List<String> regsToWrite = null;
	
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
	
	public int getStatementID()
	{
		return id;
	}
	
	/**	return [method signature],[statement id]	*/
	public String getUniqueID()
	{
		return this.containingMethod.getSignature()+ ":" + this.id;
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
	
	public boolean isInCatchBlock()
	{
		return this.getBlockName().contains(":catch_");
	}

	public ArrayList<String> get_array_or_switch_data()
	{
		return array_or_switch_data;
	}
	
	public ArrayList<String> getBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(this.getPreStmtDebugInfo());
		result.add("    " + this.smaliStmt);
		result.addAll(this.getPostStmtDebugInfo());
		return result;
	}
	
	public ArrayList<String> getInstrumentedBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(this.getPreStmtDebugInfo());
		if (!this.getInstrumentedPrecedingStmts().isEmpty())
		{
			result.addAll(this.getInstrumentedPrecedingStmts());
			result.add("");
		}
		result.add("    #id " + this.id);
		result.add("    " + this.smaliStmt);
		result.addAll(this.getPostStmtDebugInfo());
		if (!this.getInstrumentedSucceedingStmts().isEmpty())
		{
			result.add("");
			result.addAll(this.getInstrumentedSucceedingStmts());
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
	
	public boolean isGotoStmt()
	{
		return (this.getBytecodeOperator().startsWith("goto"));
	}
	
	public boolean isInvokeStmt()
	{
		return (this.getBytecodeOperator().startsWith("invoke"));
	}
	
	public boolean hasSourceLineNumber()
	{
		return (this.debugInfo.getSourceLineNumber() > 0);
	}
	
	public int getSourceLineNumber()
	{
		return this.debugInfo.getSourceLineNumber();
	}
	
	public boolean hasNoBlockLabel()
	{
		return this.debugInfo.getBlockLabel().isEmpty();
	}
	
	public String getTryStartLabel()
	{
		return this.debugInfo.getTryStartLabel();
	}
	
	public String getTryEndLabel()
	{
		return this.debugInfo.getTryEndLabel();
	}

	public Expression getOperationExpression()
	{
		if (this.ex == null)
		{
			this.ex = DEXParser.generateExpression(this).clone();
		}
		return this.ex;
	}
	
	public List<String> getRegsToRead()
	{
		if (this.regsToRead == null)
		{
			List<List<String>> access = DEXParser.getRegisterAccess(this);
			this.regsToRead = new ArrayList<String>(access.get(0));
			this.regsToWrite = new ArrayList<String>(access.get(1));
		}
		return this.regsToRead;
	}
	
	public List<String> getRegsToWrite()
	{
		if (this.regsToWrite == null)
		{
			List<List<String>> access = DEXParser.getRegisterAccess(this);
			this.regsToRead = new ArrayList<String>(access.get(0));
			this.regsToWrite = new ArrayList<String>(access.get(1));
		}
		return this.regsToWrite;
	}
	
	public int getIfJumpTargetID()
	{
		if (!this.isIfStmt())
			return -1;
		String targetLabel = this.smaliStmt.substring(this.smaliStmt.indexOf(":"));
		return this.containingMethod.getFirstStmtOfBlock(targetLabel).getStatementID();
	}
	
	public Expression getIfJumpCondition()
	{
		if (!this.isIfStmt())
			return null;
		return DEXParser.getIfJumpCondition(this);
	}
	
	public ArrayList<String> getInvokeParameters()
	{
		if (!this.isInvokeStmt())
			return null;
		String stmt = this.smaliStmt;
		String params = stmt.substring(stmt.indexOf("{")+1, stmt.indexOf("}"));
		ArrayList<String> result = new ArrayList<String>();
		if (params.contains(", "))
		{
			result.addAll(Arrays.asList(params.split(", ")));
		}
		else if (params.contains(" .. "))
		{
			String firstV = params.substring(0, params.indexOf(" .. "));
			String lastV = params.substring(params.indexOf(" .. ")+4);
			String prefix = firstV.substring(0, 1);
			int first = Integer.parseInt(firstV.substring(1));
			int last = Integer.parseInt(lastV.substring(1));
			while (first <= last)
			{
				result.add(prefix + first);
				first++;
			}
		}
		else if (!params.equals(""))
		{
			result.add(params);
		}
		return result;
	}
	
	public ArrayList<Expression> getSwitchFlowThroughConditions()
	{
		ArrayList<Expression> result = new ArrayList<Expression>();
		if (!this.isSwitchStmt())
			return null;
		String stmt = this.smaliStmt;
		String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
		Map<Integer, String> switchMap = this.getSwitchMap();
		for (int value : switchMap.keySet())
		{
			Expression cond = new Expression("/=");
			cond.add(vA);
			cond.add(value+"");
			result.add(cond);
		}
		return result;
	}
	
	public Expression getSwitchCaseCondition(int caseValue)
	{
		if (!this.isSwitchStmt())
			return null;
		String stmt = this.smaliStmt;
		String vA = stmt.substring(stmt.indexOf(" ")+1, stmt.indexOf(", "));
		Expression cond = new Expression("=");
		cond.add(vA);
		cond.add(caseValue+"");
		return cond;
	}
	
	public int getGotoTargetID()
	{
		if (!this.isGotoStmt())
			return -1;
		String targetLabel = this.smaliStmt.substring(this.smaliStmt.indexOf(":"));
		return this.containingMethod.getFirstStmtOfBlock(targetLabel).getStatementID();

	}
	
	public String getInvokeSignature()
	{
		if (!this.isInvokeStmt())
			return "";
		return this.smaliStmt.substring(this.smaliStmt.lastIndexOf(", ")+2);
	}
	
	public Map<Integer, String> getSwitchMap()
	{
		Map<Integer, String> switchMap = new HashMap<Integer, String>();
		if (this.getBytecodeOperator().equals("sparse-switch"))
		{
			for (String line : this.array_or_switch_data)
			{
				if (!line.contains(" -> "))
					continue;
				String link = line.trim();
				String hexValue = link.substring(0, link.indexOf(" -> "));
				String caseTargetLabel = link.substring(link.indexOf(" -> ")+4);
				int decValue = Integer.parseInt(hexValue.replace("0x", ""), 16);
				switchMap.put(decValue, caseTargetLabel);
			}
		}
		else if (this.getBytecodeOperator().equals("packed-switch"))
		{
			int caseValue = 0;
			for (int i = 0; i < this.array_or_switch_data.size(); i++)
			{
				String line = this.array_or_switch_data.get(i);
				if (line.startsWith("    .packed-switch"))
				{
					String initValueString = line.substring(line.lastIndexOf(" ")+1).replace("0x", "");
					caseValue = Integer.parseInt(initValueString, 16);
				}
				else if (line.startsWith("        :"))
				{
					switchMap.put(caseValue++, line.trim());
				}
			}
		}
		return switchMap;
	}
	
	public String getReturnedVariable()
	{
		if (!this.isReturnStmt() || this.smaliStmt.equals("return-void"))
			return "";
		String vA = this.smaliStmt.substring(this.smaliStmt.indexOf(" ")+1);
		return vA;
	}

}
