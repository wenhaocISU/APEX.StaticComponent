package apex;

import java.util.ArrayList;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticClass;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.SymbolicExecutionBlacklist;

public class APITesting {

	
	
	public static void test(StaticApp staticApp, String libClass)
	{
		ArrayList<String> list = new ArrayList<String>();
		for (StaticClass c : staticApp.getClasses())
		{
			if (SymbolicExecutionBlacklist.classInBlackList(c.getDexName()))
				continue;
			for (StaticMethod m : c.getMethods())
			{
				for (StaticStmt s : m.getStatements())
				{
					if (s.isInvokeStmt() && s.getInvokeSignature().contains(libClass))
					{
						if (!list.contains(s.getInvokeSignature()))
						{
							System.out.println("\"" + s.getInvokeSignature() + "\",");
							list.add(s.getInvokeSignature());
						}
					}
				}
			}
		}
	}
	
}
