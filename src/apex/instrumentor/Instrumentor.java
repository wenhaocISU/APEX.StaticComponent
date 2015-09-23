package apex.instrumentor;

import java.util.ArrayList;

import apex.staticFamily.StaticClass;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;

public class Instrumentor {
	
	/**
	 * Things to do:
	 * 0. add statement id into comments above statement "#id 123"
	 * 1. print "Method_Starting,[method signature]" before 1st statement
	 * 2. print "Method_Returning,[method signature]" before return statement
	 * 3. print "Method_Throwing,[method signature]" before throw statement
	 * 4. print "ExecLog,[Method signature],branch,[stmt id]" before if/switch
	 * 5. print "ExecLog,[Method signature],flew_through,[stmt id]" after if/switch
	 * 6. print "ExecLog,[Method signature],try,[stmt id]" before stmts in try block
	 * 7. print "ExecLog,[Method signature],exception_caught,[stmt id]" before 1st stmt in catch block
	 * */

	public void instrumentStmt(StaticClass c, StaticMethod m, StaticStmt s)
	{
		// Job 0
		s.addDebugInfo("    #id " + s.getStatementID());
		// Job 1
		if (s.isFirstStmtOfMethod())
		{
			addPrintLnBefore(c, m, s, "Method_Starting," + m.getSignature());
		}
		
		// Job 2,3
		if (s.isReturnStmt())
		{
			addPrintLnBefore(c, m, s, "Method_Returning," + m.getSignature());
		}
		else if (s.isThrowStmt())
		{
			addPrintLnBefore(c, m, s, "Method_Throwing," + m.getSignature());
		}
		
		// Job 4,5
		if (s.isIfStmt() || s.isSwitchStmt())
		{
			addPrintLnBefore(c, m, s, "execLog,branch," + s.getStatementID() + "," + m.getSignature());
			addPrintLnAfter(c, m, s, "execLog,flew_through," + s.getStatementID() + "," + m.getSignature());
		}
		
		// Job 6
		if (s.isInTryBlock())
		{
			addPrintLnBefore(c, m, s, "execLog,try," + s.getStatementID() + "," + m.getSignature());
		}
		
		// Job 7
		if (s.getBlockName().contains(":catch_") && s.isFirstStmtOfBlock())
		{
			addPrintLnBefore(c, m, s, "execLog,exception_caught," + s.getStatementID() + "," + m.getSignature());
		}

	}
	
	private void addPrintLnBefore(StaticClass c, StaticMethod m, StaticStmt s, String text)
	{
		ArrayList<String> printlnStmts = generatePrintLnStmts(c, m, s, text);
		for (String stmt : printlnStmts)
		{
			s.addPrecedingStmt(stmt);
		}
	}
	
	private void addPrintLnAfter(StaticClass c, StaticMethod m, StaticStmt s, String text)
	{
		ArrayList<String> printlnStmts = generatePrintLnStmts(c, m, s, text);
		for (String stmt : printlnStmts)
		{
			s.addSucceedingStmt(stmt);
		}
	}
	
	private ArrayList<String> generatePrintLnStmts(StaticClass c, StaticMethod m, StaticStmt s, String text)
	{
		ArrayList<String> result = new ArrayList<String>();
		//result.add("    #print: " + text);
		
		String regInfo = m.findUsableRegister(s);
		String regName = "", regType = "";
		if (regInfo.contains(":"))
		{
			regName = regInfo.split(":")[0];
			regType = regInfo.split(":")[1];
		}
		else
		{
			regName = regInfo;
		}
		//TODO
		String[] sigs = {
				"Landroid/support/v4/view/ViewPager;->infoForCurrentScrollPosition()Landroid/support/v4/view/ViewPager$ItemInfo;",
				"Landroid/support/v4/view/ViewPager;->dataSetChanged()V",
				"Landroid/support/v7/internal/view/menu/ActionMenuPresenter;->flagActionItems()Z",
				"Landroid/support/v7/internal/widget/ActivityChooserModel;->readHistoricalDataImpl()V",
				"Landroid/support/v7/internal/widget/AdapterViewICS;->findSyncPosition()I",
				"Landroid/support/v7/internal/widget/ListPopupWindow;->buildDropDown()I",
				"Landroid/support/v7/internal/widget/ScrollingTabContainerView$TabView;->update()V"
		};
		
		
		return result;
	}
	
}
