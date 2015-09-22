package apex.staticFamily;

import java.util.ArrayList;

public class StmtDebugInfo {

	private boolean prologue = false;
	private int lineNumber = -1;
	
	// All of following text are in the original format from .smali files,
	// which means each line will have 4 space at the beginning
	private ArrayList<String> otherDebugInfo = new ArrayList<String>();
	private ArrayList<String> blockLabels = new ArrayList<String>();
	private ArrayList<String> comments = new ArrayList<String>();
	private String tryStartLabel = "";
	private String tryEndLabel = "";
	private String catchInfo = "";
	private String catchAllInfo = "";
	
	// some flags
	private boolean isFirstStmtOfBlock = false;

	StmtDebugInfo(ArrayList<String> stmtDebugInfo)
	{
		for (String line : stmtDebugInfo)
		{
			add(line);
		}
	}
	
	void add(String line)
	{
		if (line.equals("    .prologue"))
		{
			this.prologue = true;
		}
		else if (line.startsWith("    .line "))
		{
			this.lineNumber = Integer.parseInt(line.substring(line.lastIndexOf(" ")+1));
		}
		else if (line.startsWith("    :try_start_"))
		{
			this.tryStartLabel = line;
		}
		else if (line.startsWith("    :try_end_"))
		{
			this.tryEndLabel = line;
		}
		else if (line.startsWith("    :"))
		{
			this.blockLabels.add(line);
			this.isFirstStmtOfBlock = true;
		}
		else if (line.startsWith("    .catch "))
		{
			this.catchInfo = line;
		}
		else if (line.startsWith("    .catchall "))
		{
			this.catchAllInfo = line;
		}
		else if (line.startsWith("    #"))
		{
			this.comments.add(line);
		}
		else
		{
			this.otherDebugInfo.add(line);
		}
	}
	
	void copyBlockLabel(StmtDebugInfo debugInfo)
	{
		this.blockLabels = new ArrayList<String>(debugInfo.getBlockLabel());
	}

/**	Getters	*/
	ArrayList<String> getBlockLabel()
	{
		return blockLabels;
	}
	
	ArrayList<String> getPreStmtSection()
	{
		ArrayList<String> result = new ArrayList<String>();
		if (this.prologue)
		{
			result.add("    .prologue");
		}
		if (this.lineNumber > 0)
		{
			result.add("    .line " + this.lineNumber);
		}
		result.addAll(this.otherDebugInfo);
		if (this.isFirstStmtOfBlock)
		{
			result.addAll(this.blockLabels);
		}
		if (!this.tryStartLabel.equals(""))
		{
			result.add(this.tryStartLabel);
		}
		result.addAll(this.comments);
		return result;
	}
	
	ArrayList<String> getPostStmtSection()
	{
		ArrayList<String> result = new ArrayList<String>();
		if (!tryEndLabel.equals(""))
		{
			result.add(tryEndLabel);
		}
		if (!catchInfo.equals(""))
		{
			result.add(catchInfo);
		}
		if (!catchAllInfo.equals(""))
		{
			result.add(catchAllInfo);
		}
		return result;
	}
	
	public boolean isFirstStmtOfBlock()
	{
		return this.isFirstStmtOfBlock;
	}
	
	public int getOriginalLineNumber()
	{
		return this.lineNumber;
	}
	
	
	
}





















