package apex.symbolic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.context.VMContext;

public class SymbolicExecution {

	private StaticApp staticApp;
	public boolean debug = false;
	
	public SymbolicExecution(StaticApp staticApp)
	{
		this.staticApp = staticApp;
	}
	
	public List<PathSummary> doFullSymbolic(StaticMethod m)
	{
		List<PathSummary> result = new ArrayList<PathSummary>();
		List<ToDoPath> pathList = this.generateToDoPaths(m, 0, -1);
		int id = 0;
		for (ToDoPath p : pathList)
		{
			VMContext vm = new VMContext(this.staticApp);
			result.add(this.doFullSymbolic(vm, p, m.getSignature(), id++));
		}
		return result;
	}
	
	private void execute(PathSummary ps, ToDoPath p, VMContext vm)
	{
		for (int index = 0; index < p.execLog.size(); index++)
		{
			String stmtInfo = p.execLog.get(index);
			if (this.debug)
			{
				System.out.println("\n\n[" + stmtInfo + "]");
			}
			if (stmtInfo.contains(",")) //if or switch, need to update path constraint
			{
				String choice = stmtInfo.substring(stmtInfo.indexOf(",")+1);
				stmtInfo = stmtInfo.substring(0, stmtInfo.indexOf(","));
				StaticStmt s = staticApp.getStmt(stmtInfo);
				if (s.isIfStmt())
				{
					Expression cond = s.getIfJumpCondition();
					if (choice.equals("flow"))
						cond = cond.getReverseCondition();
					ps.updatePathConstraint(cond);
				}
				else if (s.isSwitchStmt())
				{
					if (choice.equals("flow"))
					{
						for (Expression cond : s.getSwitchFlowThroughConditions())
						{
							ps.updatePathConstraint(cond);
						}
					}
					else if (choice.startsWith("case"))
					{
						int caseValue = Integer.parseInt(choice.replace("case", ""));
						ps.updatePathConstraint(s.getSwitchCaseCondition(caseValue));
					}
				}
			}
			else	// this statement might change registers or objects
			{
				StaticStmt s = staticApp.getStmt(stmtInfo);
				vm.applyOperation(s);
			}
		}
	}
	
	public PathSummary doFullSymbolic(VMContext vm, ToDoPath p, String methodSig, int id)
	{
		PathSummary ps = new PathSummary(vm, p, methodSig, id);
		execute(ps, p, vm);
		return ps;
	}
	
	public PathSummary doFullSymbolic(VMContext vm, ToDoPath p, ArrayList<Expression> pathConditions, String methodSig, int id)
	{
		PathSummary ps = new PathSummary(vm, p, methodSig, id);
		ps.setPathCondition(pathConditions);
		execute(ps, p, vm);
		return ps;
	}
	
	public List<PathSummary> doFullSymbolic(ArrayList<String> execLog)
	{
		//TODO
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
		ArrayList<ToDoPath> unfinished = new ArrayList<ToDoPath>();
		ToDoPath tdP = new ToDoPath(startingStmtID, endingStmtID);
		unfinished.add(tdP);
		while (unfinished.size() > 0)
		{
			ToDoPath p = unfinished.remove(unfinished.size()-1);
			ArrayList<ToDoPath> finishedTDPs = exploreTDP(unfinished, p, m);
			if (p.isLegit)
				result.addAll(finishedTDPs);
		}
		result = removeDupe(result);
		return result;
	}
	
	private ArrayList<ToDoPath> generateToDoPaths(StaticMethod m, int startingStmtID, int endingStmtID, boolean print)
	{
		ArrayList<ToDoPath> result = new ArrayList<ToDoPath>();
		ArrayList<ToDoPath> unfinished = new ArrayList<ToDoPath>();
		ToDoPath tdP = new ToDoPath(startingStmtID, endingStmtID);
		unfinished.add(tdP);
		while (unfinished.size() > 0)
		{
			ToDoPath p = unfinished.remove(unfinished.size()-1);
			ArrayList<ToDoPath> finishedTDPs = exploreTDP(unfinished, p, m);
			if (p.isLegit)
				result.addAll(finishedTDPs);
		}
		result = removeDupe(result);
		return result;
	}
	
	
	private ArrayList<ToDoPath> exploreTDP(ArrayList<ToDoPath> tdPList, ToDoPath p, StaticMethod m)
	{
		ArrayList<ToDoPath> result = new ArrayList<ToDoPath>();
		result.add(p.clone());
		int nextStmtID = p.startingStmtID;
		while (nextStmtID != p.endingStmtID)
		{
			
			StaticStmt s = m.getStatements().get(nextStmtID++);
			if (s == null)
			{
				System.out.println("SymbolicExecution.exploreTDP() ran into null StaticStmt.");
				System.exit(1);
			}
			for (ToDoPath tdP : result)
				tdP.execLog.add(s.getUniqueID());
			if (p.endingStmtID == s.getStatementID()) // reached specified ending
			{
				for (ToDoPath tdP : result)
					tdP.isLegit = true;
				break;
			}
			else if (s.isIfStmt()) // follow order or make own choice
			{
				for (ToDoPath tdP : result)
				{
					String order = tdP.getOrder(s.getUniqueID());
					String choice = order;
					if (order.equals("jump"))
					{
						nextStmtID = s.getIfJumpTargetID();
					}
					else if (order.equals("flow"))
					{}
					else if (order.startsWith("force")) // breaking out of loop
					{
						choice = order.replace("force", "");
						if (order.endsWith("jump"))
						{
							nextStmtID = s.getIfJumpTargetID();
						}
					}
					else // if no orders to follow, then choose flow through
					{
						choice = "flow";
						ToDoPath newTDP = tdP.copy();
						newTDP.branchOrders.add(s.getUniqueID() + ",jump");
						tdPList.add(newTDP);
					}
					tdP.branchChoices.add(s.getUniqueID()+ "," + choice);
				}
			}
			else if (s.isSwitchStmt()) // follow order or make own choice
			{
				for (ToDoPath tdP : result)
				{
					String order = tdP.getOrder(s.getUniqueID());
					String choice = order;
					Map<Integer, String> switchMap = s.getSwitchMap();
					if (order.equals("flow"))
					{}
					else if (order.startsWith("case"))
					{
						int caseValue = Integer.parseInt(order.substring(order.indexOf("case")+4));
						String label = switchMap.get(caseValue);
						nextStmtID = m.getFirstStmtOfBlock(label).getStatementID();
					}
					else // no orders to follow, choose flow through
					{
						choice = "flow";
						for (int caseValue : switchMap.keySet())
						{
							ToDoPath newTDP = tdP.copy();
							newTDP.branchOrders.add(s.getUniqueID() + ",case" + caseValue);
							tdPList.add(newTDP);
						}
					}
					tdP.branchChoices.add(s.getUniqueID() + "," + choice);
				}
			}
			else if (s.isGotoStmt())
			{
				nextStmtID = s.getGotoTargetID();
			}
			else if (s.isReturnStmt() || s.isThrowStmt())
			{
				for (ToDoPath tdP : result)
				{
					if (tdP.endingStmtID == -1) // natural ending
					{
						tdP.endingStmtID = s.getStatementID();
						tdP.isLegit = true;
					}
					else // this isn't the ending we wanted, this ToDoPath will be discarded
					{
						tdP.isLegit = false;
					}
				}
				break;
			}
			else if (s.isInvokeStmt())
			{
				//TODO deal with inheritance sometime
				// can use ranged symbolic execution to find out the type of p0
				String targetSig = s.getInvokeSignature();
				StaticMethod targetM = this.staticApp.getMethod(targetSig);
				if (targetM != null && !targetM.isAbstract())
				{
					ArrayList<ToDoPath> invokedTDPs = this.generateToDoPaths(targetM, 0, -1, false);
					ArrayList<ToDoPath> newResult = new ArrayList<ToDoPath>();
					for (ToDoPath tdP : result)
					{
						for (ToDoPath invkTDP: invokedTDPs)
						{
							ToDoPath newP = tdP.clone();
							newP.branchChoices.addAll(invkTDP.branchChoices);
							newP.execLog.addAll(invkTDP.execLog);
							newP.orderIndex += invkTDP.branchChoices.size();
							newResult.add(newP);
						}
					}
					result = new ArrayList<ToDoPath>(newResult);
				}
			}
		}
		for (ToDoPath tdP : result)
		{
			if (nextStmtID == tdP.endingStmtID)
				tdP.execLog.add(m.getSignature() + ":" + nextStmtID);
			for (String choice : tdP.branchChoices)
			{
				String stmtInfo = choice.split(",")[0];
				int index = tdP.execLog.indexOf(stmtInfo);
				if (index > -1)
				{
					tdP.execLog.remove(index);
					tdP.execLog.add(index, choice);
				}
			}
		}
		return result;
	}
	
	private ArrayList<ToDoPath> removeDupe(ArrayList<ToDoPath> result)
	{
		ArrayList<String> choices = new ArrayList<String>();
		ArrayList<ToDoPath> newResult = new ArrayList<ToDoPath>();
		for (ToDoPath p : result)
		{
			String choice = p.branchChoicesToString();
			if (!choices.contains(choice))
			{
				choices.add(choice);
				p.id = newResult.size();
				newResult.add(p);
			}
		}
		return newResult;
	}
}
