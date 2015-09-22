package apex.symbolic;

import java.util.ArrayList;
import java.util.List;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;

public class SymbolicExecution {

	private StaticApp staticApp;
	
	public SymbolicExecution(StaticApp staticApp)
	{
		this.staticApp = staticApp;
	}
	
	public List<PathSummary> doFullSymbolic(StaticMethod m)
	{
		
		return null;
	}
	
	public List<PathSummary> doFullSymbolic(ArrayList<String> execLog)
	{
		
		return null;
	}
	
	/**
	 * Generate a list of ToDoPath from StaticMethod
	 * from the given range of statements.
	 * If startingStmtID == 0 then start from beginning
	 * If endingStmtID == -1 then end in return or throw
	 * */
	public ArrayList<ToDoPath> generateToDoPaths(StaticMethod m, int startingStmtID, int endingStmtID)
	{
		ArrayList<ToDoPath> result = new ArrayList<ToDoPath>();
		ArrayList<ToDoPath> unexploredTDPs = new ArrayList<ToDoPath>();
		ToDoPath tdP = new ToDoPath(startingStmtID, endingStmtID);
		unexploredTDPs.add(tdP);
		while (!unexploredTDPs.isEmpty())
		{
			exploreTDP(result, tdP, m);
			if (tdP.isLegit)
			{
				result.add(tdP);
			}
		}
		return result;
	}
	
	private void exploreTDP(ArrayList<ToDoPath> tdPList, ToDoPath tdP, StaticMethod m)
	{
		int nextStmtID = tdP.startingStmtID;
		while (nextStmtID != tdP.endingStmtID)
		{
			StaticStmt s = m.getStatements().get(nextStmtID);
			if (s.isIfStmt() || s.isSwitchStmt())
			{
				
			}
			else if (s.isGotoStmt())
			{
				
			}
			else if (s.isReturnStmt())
			{
				
			}
			else if (s.isThrowStmt())
			{
				
			}
		}
	}
}
