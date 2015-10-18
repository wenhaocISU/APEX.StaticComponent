package target;

import java.util.ArrayList;
import java.util.List;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticAppBuilder;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.PathSummary;
import apex.symbolic.SymbolicExecution;

public class Targets {

	
	public static StaticApp staticApp;
	public static SymbolicExecution sex;
	public static CallGraph cgBuilder;
	
	public static void main(String[] args)
	{
		String apkPaths[] =
		{
/* 0 */			"/home/wenhaoc/workspace/adt_eclipse/TheApp/bin/TheApp.apk",
				"/home/wenhaoc/AppStorage/APAC_engagement/CalcA.apk",
				"/home/wenhaoc/AppStorage/APAC_engagement/backupHelper.apk",
/* 3 */			"/home/wenhaoc/AppStorage/Jensen/net.mandaria.tippytipper.apk",
		};

		String apkPath = apkPaths[3];
		StaticAppBuilder.multiThreading = true;
		staticApp = StaticAppBuilder.fromAPK(apkPath);
		sex = new SymbolicExecution(staticApp);
		
		
		String[] newlyHitTargets = 
		{
				"net.mandaria.tippytipperlibrary.services.TipCalculatorService:158",
				"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:93",
				"net.mandaria.tippytipperlibrary.widgets.NumberPicker:275",
				"net.mandaria.tippytipperlibrary.widgets.NumberPicker$NumberPickerInputFilter:386",
				"net.mandaria.tippytipperlibrary.services.TipCalculatorService:175",
				"net.mandaria.tippytipperlibrary.widgets.NumberPicker:288",
				"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:76",
		};
		
		List<StaticStmt> trimmedTargets = getValidatedTargets(newlyHitTargets);
		for (StaticStmt s : trimmedTargets)
		{
			System.out.println(s.getUniqueID());
			List<StaticMethod> candidates = findSources(s);
			for (StaticMethod m : candidates)
			{
				System.out.println(" -candidate: " + m.getSignature());
				findDetail(m, s);
			}
		}
		
	}
	
	public static void findDetail(StaticMethod m, StaticStmt s)
	{
		List<PathSummary> psList = sex.doFullSymbolic(m);
		int count = 1;
		for (PathSummary ps : psList)
		{
			if (ps.containsStmt(s))
			{
				System.out.println("  *PS No." + count++ + " constraint:");
				for (Expression cond : ps.getPathConditions())
				{
					System.out.println("    " + cond.toYicesStatement());
				}
			}
		}
	}
	
	public static List<StaticMethod> findPossibleEntryPoints()
	{
		if (cgBuilder == null)
			cgBuilder = new CallGraph(staticApp);
		return cgBuilder.getSourceMethods();
	}
	
	public static List<StaticMethod> findSources(StaticStmt s)
	{
		StaticMethod m = s.getContainingMethod();
		if (cgBuilder == null)
			cgBuilder = new CallGraph(staticApp);
		return cgBuilder.getSourceMethods(m);
	}
	
	public static List<StaticStmt> getValidatedTargets(String[] original_targets)
	{
		List<StaticStmt> result = new ArrayList<StaticStmt>();
		
		for (String stmtInfo : original_targets)
		{
			StaticStmt s = staticApp.getStmtByLineNumber(stmtInfo);
			if (s != null)
			{
				result.add(s);
			}
		}
		return result;
	}

	
	

	
}
