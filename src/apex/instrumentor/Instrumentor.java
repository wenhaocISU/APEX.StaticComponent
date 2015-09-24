package apex.instrumentor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticField;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;

public class Instrumentor {
	
	
	public boolean blackListOn = true;

	
	public void addSmaliFile(File smaliFile, String path)
	{
		if (!smaliFile.exists() || !smaliFile.getAbsolutePath().endsWith(".smali"))
			return;
		File newFile = new File(path);
		newFile.getParentFile().mkdirs();
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(smaliFile));
			PrintWriter out = new PrintWriter(new FileWriter(newFile));
			String line;
			while ((line = in.readLine())!=null)
			{
				out.write(line+"\n");
			}
			in.close();
			out.flush();
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Things to do:
	 * 1. print "Method_Starting,[method signature]" before 1st statement
	 * 2. print "Method_Returning,[method signature]" before return statement
	 * 3. print "Method_Throwing,[method signature]" before throw statement
	 * 4. print "ExecLog,[stmt info]" before if/switch
	 * 5. print "ExecLog,[stmt info],flow_through" after if/switch
	 * 6. print "ExecLog,[stmt info],try" before stmts in try block
	 * 7. print "ExecLog,[stmt info],caught_exception" before 1st stmt in catch block
	 * */

	public void instrumentStmt(StaticApp staticApp, StaticStmt s)
	{
		StaticMethod m = s.getContainingMethod();
		// Job 1
		if (s.isFirstStmtOfMethod())
		{
			addPrintLnBefore(staticApp, s, "Method_Starting," + m.getSignature());
		}
		
		// Job 2,3
		if (s.isReturnStmt())
		{
			addPrintLnBefore(staticApp, s, "Method_Returning," + m.getSignature());
		}
		else if (s.isThrowStmt())
		{
			addPrintLnBefore(staticApp, s, "Method_Throwing," + m.getSignature());
		}
		
		// Job 4,5
		if (s.isIfStmt() || s.isSwitchStmt())
		{
			addPrintLnBefore(staticApp, s, "execLog," + s.getUniqueID());
			addPrintLnAfter(staticApp, s, "execLog," + s.getUniqueID() + ",flow_through");
		}
		
		// Job 6
		if (s.isInTryBlock())
		{
			addPrintLnBefore(staticApp, s, "execLog," + s.getUniqueID() + ",try");
		}
		
		// Job 7
		if (s.getBlockName().contains(":catch_") && s.isFirstStmtOfBlock())
		{
			addPrintLnBefore(staticApp, s, "execLog," + s.getUniqueID() + ",caught_exception");
		}

	}
	
	private void addPrintLnBefore(StaticApp staticApp, StaticStmt s, String text)
	{
		ArrayList<String> printlnStmts = generatePrintLnStmts(staticApp, s, text);
		for (String stmt : printlnStmts)
		{
			s.addPrecedingStmt(stmt);
		}
	}
	
	private void addPrintLnAfter(StaticApp staticApp, StaticStmt s, String text)
	{
		ArrayList<String> printlnStmts = generatePrintLnStmts(staticApp, s, text);
		for (String stmt : printlnStmts)
		{
			s.addSucceedingStmt(stmt);
		}
	}
	
	/** Some tricky methods to instrument:
	String[] sigs = {
		"Landroid/support/v4/view/ViewPager;->infoForCurrentScrollPosition()Landroid/support/v4/view/ViewPager$ItemInfo;",
		"Landroid/support/v4/view/ViewPager;->dataSetChanged()V",
		"Landroid/support/v7/internal/view/menu/ActionMenuPresenter;->flagActionItems()Z",
		"Landroid/support/v7/internal/widget/ActivityChooserModel;->readHistoricalDataImpl()V",
		"Landroid/support/v7/internal/widget/AdapterViewICS;->findSyncPosition()I",
		"Landroid/support/v7/internal/widget/ListPopupWindow;->buildDropDown()I",
		"Landroid/support/v7/internal/widget/ScrollingTabContainerView$TabView;->update()V"
	};
	 * */
	private ArrayList<String> generatePrintLnStmts(StaticApp staticApp, StaticStmt s, String text)
	{
		ArrayList<String> result = new ArrayList<String>();
		StaticMethod m = s.getContainingMethod();
		String regInfo = m.findUsableRegister(staticApp, s);
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
		String assignStmt = "    const-string " + regName + ", \"" + text + "\"";
		String invokeStmt = "    invoke-static {" + regName + "}, Lapex/instrumented/Println;->print(Ljava/lang/String;)V";
		if (!regType.equals(""))
		{
			StaticField f = s.getContainingMethod().getDeclaringClass().getTempField(regType, true);
			String sputOp = "s" + getFieldOpKeyword(regType);
			String sputStmt = "    " + sputOp + " " + regName + ", " + f.getSignature();
			String sgetStmt = "    " + sputStmt.replaceFirst("sput", "sget");
			result.add(sputStmt);
			result.add(assignStmt);
			result.add(invokeStmt);
			result.add(sgetStmt);
		}
		else
		{
			result.add(assignStmt);
			result.add(invokeStmt);
		}
		return result;
	}
	
	private String getFieldOpKeyword(String type)
	{
		if (type.equals("I") || type.equals("F"))
			return "put";
		if (type.equals("Z"))
			return "put-boolean";
		if (type.equals("B"))
			return "put-byte";
		if (type.equals("C"))
			return "put-char";
		if (type.equals("S"))
			return "put-short";
		if (type.equals("J") || type.equals("D"))
			return "put-wide";
		return "put-object";
	}
	
}
