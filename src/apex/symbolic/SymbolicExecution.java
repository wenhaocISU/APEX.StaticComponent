package apex.symbolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		int counter = 1;
		while (unexploredTDPs.size() > 0)
		{
			ToDoPath p = unexploredTDPs.remove(unexploredTDPs.size()-1);
			System.out.println("=============== symbolic execution No." + counter);
			exploreTDP(unexploredTDPs, p, m);
			if (p.isLegit)
			{
				result.add(p);
			}
		}
		return result;
	}
	
	private void exploreTDP(ArrayList<ToDoPath> tdPList, ToDoPath tdP, StaticMethod m)
	{
		int nextStmtID = tdP.startingStmtID;
		ArrayList<String> newChoices = new ArrayList<String>();
		while (nextStmtID != tdP.endingStmtID)
		{
			StaticStmt s = m.getStatements().get(nextStmtID++);
			if (tdP.endingStmtID == s.getStatementID())
			{
				tdP.isLegit = true;
				break;
			}
			else if (s.isIfStmt())
			{
				//TODO bug here, new if statement shouldn't be able to read stuff
				String direction = tdP.getDirection(s.getStatementID());
				if (direction.equals("jump"))
				{
					nextStmtID = s.getIfJumpTargetID();
				}
				else if (direction.equals("flow"))
				{
					//no action
				}
				else if (direction.startsWith("force")) // breaking out of loop
				{
					if (direction.endsWith("Jump"))
					{
						nextStmtID = s.getIfJumpTargetID();
					}
				}
				else // if no orders to follow, then choose flow through
				{
					ToDoPath newTDP = tdP.copy();
					newTDP.branchChoices.add(s.getStatementID() + ",jump");
					tdPList.add(newTDP);
					newChoices.add(s.getStatementID() + ",flow");
					System.out.println("At stmt id " + s.getStatementID() + " forks a new ToDoPath");
					System.out.println("=== old");
					tdP.print();
					System.out.println("=== new");
					newTDP.print();
				}
			}
			else if (s.isSwitchStmt())
			{
				String direction = tdP.getDirection(s.getStatementID());
				Map<Integer, String> switchMap = s.getSwitchMap();
				if (direction.equals("flow"))
				{}
				else if (direction.startsWith("case"))
				{
					int caseValue = Integer.parseInt(direction.substring(direction.indexOf("case")+4));
					String label = switchMap.get(caseValue);
					nextStmtID = m.getFirstStmtOfBlock(label).getStatementID();
				}
				else // no orders to follow, choose flow through
				{
					for (int caseValue : switchMap.keySet())
					{
						ToDoPath newTDP = tdP.copy();
						newTDP.branchChoices.add(s.getStatementID() + ",case" + caseValue);
						tdPList.add(newTDP);
					}
					newChoices.add(s.getStatementID() + ",flow");
				}
			}
			else if (s.isGotoStmt())
			{
				nextStmtID = s.getGotoTargetID();
			}
			else if (s.isReturnStmt() || s.isThrowStmt())
			{
				if (tdP.endingStmtID == -1)
				{
					tdP.endingStmtID = s.getStatementID();
					tdP.isLegit = true;
					break;
				}
				else // this isn't the ending we wanted, this ToDoPath will be discarded
				{
					tdP.isLegit = false;
					break;
				}
			}
		}
		tdP.branchChoices.addAll(newChoices);
	}
}
