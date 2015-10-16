package target;

import java.util.ArrayList;
import java.util.List;

import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticAppBuilder;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;

public class Targets {

	
	public static StaticApp staticApp;
	public static CallGraphBuilder cgBuilder;
	
	public static void main(String[] args)
	{
		String apkPaths[] =
		{
				"C:/Users/Wenhao/Documents/juno_workspace/AndroidTest/bin/AndroidTest.apk",
				"C:/Users/Wenhao/Documents/juno_workspace/AndroidTest/bin/net.mandaria.tippytipper.apk",
				"C:/Users/Wenhao/Documents/juno_workspace/AndroidTest/bin/ArraySolvingAPK.apk"
		};

		String apkPath = apkPaths[1];
		StaticAppBuilder.multiThreading = true;
		staticApp = StaticAppBuilder.fromAPK(apkPath);
		
/*		List<StaticStmt> trimmedTargets = getValidatedTargets();
		for (StaticStmt s : trimmedTargets)
		{
			System.out.println(s.getUniqueID());
			List<StaticMethod> candidates = findSources(s);
			for (StaticMethod m : candidates)
			{
				System.out.println(" *candidate: " + m.getSignature());
			}
		}*/
		
		List<StaticMethod> entryPoints = findPossibleEntryPoints();
		for (StaticMethod m : entryPoints)
			System.out.println(m.getSignature());

	}
	
	public static List<StaticMethod> findPossibleEntryPoints()
	{
		if (cgBuilder == null)
			cgBuilder = new CallGraphBuilder(staticApp);
		return cgBuilder.getSources();
	}
	
	public static List<StaticMethod> findSources(StaticStmt s)
	{
		StaticMethod m = s.getContainingMethod();
		if (cgBuilder == null)
			cgBuilder = new CallGraphBuilder(staticApp);
		return cgBuilder.getSources(m);
	}
	
	public static List<StaticStmt> getValidatedTargets()
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

	
	
	static String[] original_targets = {
		"net.mandaria.tippytipperlibrary.R$styleable:608",
		"net.mandaria.tippytipperlibrary.TippyTipperApplication:65",
		"net.mandaria.tippytipperlibrary.activities.About:82",
		"net.mandaria.tippytipperlibrary.activities.Settings:96",
		"net.mandaria.tippytipperlibrary.activities.SplitBill:173",
		"net.mandaria.tippytipperlibrary.activities.SplitBill:169",
		"net.mandaria.tippytipperlibrary.activities.SplitBill:170",
		"net.mandaria.tippytipperlibrary.activities.SplitBill:110",
		"net.mandaria.tippytipperlibrary.activities.TippyTipper:251",
		"net.mandaria.tippytipperlibrary.activities.TippyTipper:253",
		"net.mandaria.tippytipperlibrary.activities.TippyTipper:257",
		"net.mandaria.tippytipperlibrary.activities.TippyTipper:258",
		"net.mandaria.tippytipperlibrary.activities.TippyTipper:262",
		"net.mandaria.tippytipperlibrary.activities.Total:464",
		"net.mandaria.tippytipperlibrary.activities.Total:267",
		"net.mandaria.tippytipperlibrary.errors.CustomExceptionHandler:117",
		"net.mandaria.tippytipperlibrary.errors.CustomExceptionHandler:132",
		"net.mandaria.tippytipperlibrary.preferences.DecimalPreference:68",
		"net.mandaria.tippytipperlibrary.preferences.DecimalPreference:104",
		"net.mandaria.tippytipperlibrary.preferences.DecimalPreference:141",
		"net.mandaria.tippytipperlibrary.preferences.DecimalPreference:149",
		"net.mandaria.tippytipperlibrary.preferences.NumberPickerPreference:71",
		"net.mandaria.tippytipperlibrary.preferences.NumberPickerPreference:100",
		"net.mandaria.tippytipperlibrary.preferences.NumberPickerPreference:132",
		"net.mandaria.tippytipperlibrary.preferences.NumberPickerPreference:140",
		"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:76",
		"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:93",
		"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:128",
		"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:111",
		"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:119",
		"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:142",
		"net.mandaria.tippytipperlibrary.preferences.SeekBarPreference:143",
		"net.mandaria.tippytipperlibrary.services.TipCalculatorService:158",
		"net.mandaria.tippytipperlibrary.services.TipCalculatorService:168",
		"net.mandaria.tippytipperlibrary.services.TipCalculatorService:175",
		"net.mandaria.tippytipperlibrary.services.TipCalculatorService:258",
		"net.mandaria.tippytipperlibrary.tasks.GetAdRefreshRateTask:110",
		"net.mandaria.tippytipperlibrary.tasks.GetAdRefreshRateTask:121",
		"net.mandaria.tippytipperlibrary.tasks.GetAdRefreshRateTask:39",
		"net.mandaria.tippytipperlibrary.tasks.GetInHouseAdsPercentageTask:110",
		"net.mandaria.tippytipperlibrary.tasks.GetInHouseAdsPercentageTask:38",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$3:114",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$3:120",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$3:116",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$3:117",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$NumberPickerInputFilter:375",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$NumberPickerInputFilter:383",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$NumberPickerInputFilter:380",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$NumberPickerInputFilter:386",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker$NumberRangeKeyListener:417",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:170",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:438",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:441",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:443",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:438",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:451",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:318",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:325",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:320",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:275",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:345",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:351",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:347",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:348",
		"net.mandaria.tippytipperlibrary.widgets.NumberPicker:288",
	};
	
}
