package apex.staticFamily;

import java.util.ArrayList;
import java.util.List;

import apex.instrumentor.Instrumentor;


public class StaticMethod {

	/**	Method Attributes	*/
	private String declaration = "";
	private StaticClass declaringClass;
	private ArrayList<String> paramTypes = new ArrayList<String>();
	private int localRegisterCount = -1;
	private ArrayList<String> paramDeclarations = new ArrayList<String>();
	private ArrayList<String> methodAnnotations = new ArrayList<String>();
	
	/**	Call Graph Elements	*/
	private List<String> inCallSourceSigs = new ArrayList<String>();
	private List<String> outCallTargetSigs = new ArrayList<String>();
	private List<String> fieldRefSigs = new ArrayList<String>();
	
	/**	Statement members	*/
	private ArrayList<StaticStmt> statements = new ArrayList<StaticStmt>();
	private ArrayList<ArrayList<String>> supplementalData = new ArrayList<ArrayList<String>>();
	
	/**	Instrumentation related	*/
	private int instrumentedLocalRegCount;
	private boolean registerAdded = false;
	
	public StaticMethod(ArrayList<String> methodDeclaration, StaticClass c)
	{
		if (methodDeclaration.isEmpty())
			return;
		this.declaration = methodDeclaration.get(0);
		this.declaringClass = c;
		parseParams();
		parseBody(methodDeclaration);
	}
	
	private void parseParams()
	{
		String subSig = getSubSignature();
		String params = subSig.substring(subSig.indexOf("(") + 1, subSig.indexOf(")"));
		int index = 0;
		while (index < params.length())
		{
			char c = params.charAt(index++);
			if (c == 'L')
			{	// Non-primitive type
				String type = c + "";
				while (c != ';')
				{
					c = params.charAt(index++);
					type += c;
				}
				this.paramTypes.add(type);
			}
			else if (c == '[')
			{	// Array
				String type = c + "";
				while (c == '[')
				{
					c = params.charAt(index++);
					type += c;
				}
				if (c == 'L')
				{
					while (c != ';')
					{
						c = params.charAt(index++);
						type += c;
					}
				}
				this.paramTypes.add(type);
			}
			else
			{	// Primitive type
				this.paramTypes.add(c+"");
			}
		}
		if (!this.isStatic())
		{
			this.paramTypes.add(0, this.declaringClass.getDexName());
		}
	}

	private void parseBody(ArrayList<String> body)
	{
		if (body.size() <= 2)
			return;
		int i = 1;
		ArrayList<String> debugInfo = new ArrayList<String>();
		boolean inTryBlock = false;
		while (i < body.size())
		{
			String line = body.get(i);
			if (line.equals("") || line.equals(".end method"))
			{}
			else if (line.startsWith("    .locals "))
			{
				this.localRegisterCount = Integer.parseInt(line.substring(line.lastIndexOf(" ")+1));
				this.instrumentedLocalRegCount = this.localRegisterCount;
			}
			else if (line.startsWith("    .param "))
			{
				this.paramDeclarations.add(line);
				if (body.get(i+1).startsWith("        .annotation "))
				{
					while (!line.equals("    .end param"))
					{
						line = body.get(++i);
						this.paramDeclarations.add(line);
					}
				}
			}
			else if (line.startsWith("    .annotation "))
			{
				this.methodAnnotations.add(line);
				while (!line.equals("    .end annotation"))
				{
					line = body.get(++i);
					this.methodAnnotations.add(line);
				}
				this.methodAnnotations.add("");
			}
			else if (line.startsWith("    .") || line.startsWith("    :") || line.startsWith("    #"))
			{
				if (line.startsWith("    :array_"))
				{
					ArrayList<String> chunk = new ArrayList<String>(debugInfo);
					debugInfo.clear();
					while (!line.equals("    .end array-data"))
					{
						chunk.add(line);
						line = body.get(++i);
					}
					chunk.add(line);
					this.supplementalData.add(chunk);
					String label = line.trim();
					for (StaticStmt s : this.statements)
					{
						if (s.getSmaliStmt().endsWith(label))
						{
							s.set_array_or_switch_data(chunk);
							break;
						}
					}
				}
				else if (line.startsWith("    :pswitch_data_"))
				{
					ArrayList<String> chunk = new ArrayList<String>(debugInfo);
					debugInfo.clear();
					while (!line.equals("    .end packed-switch"))
					{
						chunk.add(line);
						line = body.get(++i);
					}
					chunk.add(line);
					this.supplementalData.add(chunk);
					String label = line.trim();
					for (StaticStmt s : this.statements)
					{
						if (s.getSmaliStmt().endsWith(label))
						{
							s.set_array_or_switch_data(chunk);
							break;
						}
					}
				}
				else if (line.startsWith("    :sswitch_data_"))
				{
					ArrayList<String> chunk = new ArrayList<String>(debugInfo);
					debugInfo.clear();
					while (!line.equals("    .end sparse-switch"))
					{
						chunk.add(line);
						line = body.get(++i);
					}
					chunk.add(line);
					this.supplementalData.add(chunk);
					String label = line.trim();
					for (StaticStmt s : this.statements)
					{
						if (s.getSmaliStmt().endsWith(label))
						{
							s.set_array_or_switch_data(chunk);
							break;
						}
					}
				}
				else if (line.startsWith("    :try_start_"))
				{
					inTryBlock = true;
					debugInfo.add(line);
				}
				else if (line.startsWith("    :try_end_") || line.startsWith("    .catch"))
				{
					this.statements.get(this.statements.size()-1).addDebugInfo(line);
					inTryBlock = false;
				}
				else
				{
					debugInfo.add(line);
				}
			}
			else
			{
				StaticStmt s = new StaticStmt(line, this.statements.size(), this);
				s.setDebugInfo(debugInfo);
				debugInfo.clear();
				if (s.hasNoBlockLabel() && !this.statements.isEmpty())
				{
					s.copyBlockLabel(this.statements.get(this.statements.size()-1));
				}
				s.setIsInTryBlock(inTryBlock);
				this.statements.add(s);
			}
			i++;
		}
	}
	
	public void instrument(StaticClass c, Instrumentor instrumentor)
	{
		for (StaticStmt s : this.statements)
		{
			instrumentor.instrumentStmt(c, this, s);
		}
	}
	

	public String findUsableRegister(StaticStmt s)
	{
		if (this.localRegisterCount < 0)
			return "";
		
		// use added register if we can
		if (this.localRegisterCount+this.getParamRegCount() < 15)
		{
			if (!this.registerAdded)
			{
				this.instrumentedLocalRegCount = this.localRegisterCount+1;
				this.registerAdded = true;
			}
			return "v"+this.localRegisterCount;
		}
		
		// if we can't add new register, see if there's
		// a parameter register we can use
		if (this.isStatic() && this.paramTypes.size()>0)
		{
			return "p0:" + this.paramTypes.get(0);
		}
		else if (!this.isStatic()&&this.paramTypes.size()>1)
		{
			return "p1" + this.paramTypes.get(1);
		}
		
		// Now there are a lot of local registers, and 0 usable parameter registers
		
		// Some corners to cut
		if (s.getStatementID() == 0)
		{
			return "v0";
		}
		else if (s.isReturnStmt() || s.isThrowStmt())
		{
			for (int i = 0; i < this.localRegisterCount; i++)
			{
				if (!s.getRegsToRead().contains("v"+i))
				{
					return "v"+i;
				}
			}
		}
		// Now we have branch statements, try block statements
		// that are not the first statement of the method
		// We have to do a simple symbolic execution
		boolean finished = false;
		StaticStmt currentStmt = s;
		ArrayList<String> reg_read = new ArrayList<String>();
		ArrayList<String> reg_write = new ArrayList<String>();
		while (!finished)
		{
			//First, collect all exec log from current stmt to return/throw
			//	for each exec_log:
			//		for each statement:
			//			update reg_read and reg_write;
			//			if contains writing of a register and it's not in reg_read
			//			then
			//				this reg is the one;
			//				NOTE(Sep21): no need to know the type because it's safe to write
			//				break;
			//			end if
			//		end for
			//		if found a reg
			//			break;
			//	end for
			///////////////////////////
			//	If previous step fails, find one exec log from start to current stmt
			//	do symbolic execution from this exec log
			//	find the first register that isn't empty
			//	get its name and type
			//TODO API: get exec log from a given index, to an end index
			//TODO API: given an exec log, do symbolic execution
		}
		
		return "";
	}
	
	
/**	Setters	*/
	public void addSmaliStmt(StaticStmt smaliStmt) {
		this.statements.add(smaliStmt);
	}
	
	public void addInCallSourceSig(String inCallSourceSig) {
		if (!this.inCallSourceSigs.contains(inCallSourceSig))
			this.inCallSourceSigs.add(inCallSourceSig);
	}
	
	public void addOutCallTargetSig(String outCallTargetSig) {
		if (!this.outCallTargetSigs.contains(outCallTargetSig))
			this.outCallTargetSigs.add(outCallTargetSig);
	}

	public void addFieldRefSig(String fieldRefSig) {
		if (!this.fieldRefSigs.contains(fieldRefSig))
			this.fieldRefSigs.add(fieldRefSig);
	}
	
	public void setInstrumentLocalRegisters(int newLocalCount)
	{
		this.instrumentedLocalRegCount = newLocalCount;
	}
	
/**	Getters	*/
	public String getDeclaration()
	{
		return this.declaration;
	}
	
	public StaticClass getDeclaringClass()
	{
		return this.declaringClass;
	}
	
	public String getSignature()
	{
		return this.declaringClass.getDexName() + "->" + this.getSubSignature();
	}
	
	public String getSubSignature()
	{
		return this.declaration.substring(this.declaration.lastIndexOf(" ")+1);
	}
	
	public boolean isStatic()
	{
		return this.declaration.contains(" static ");
	}
	
	public boolean isAbstract()
	{
		return this.declaration.contains(" abstract ");
	}
	
	public boolean isNative()
	{
		return this.declaration.contains(" native ");
	}
	
	public boolean isPrivate()
	{
		return this.declaration.contains(" private ");
	}
	
	public boolean isProtected()
	{
		return this.declaration.contains(" protected ");
	}
	
	public int getLocalRegisterCount() {
		return localRegisterCount;
	}
	
	public ArrayList<StaticStmt> getStatements() {
		return this.statements;
	}

	public ArrayList<String> getParamTypes() {
		return paramTypes;
	}
	
	public int getParamRegCount()
	{
		int result = 0;
		for (String pType : paramTypes)
		{
			if (pType.equals("J") || pType.equals("D"))
				result += 2;
			else
				result += 1;
		}
		return result;
	}
	
	public List<String> getInCallSourceSigs()
	{
		return inCallSourceSigs;
	}
	
	public List<String> getOutCallTargetSigs()
	{
		return outCallTargetSigs;
	}
	
	public List<String> getFieldRefSigs()
	{
		return fieldRefSigs;
	}
	
	public ArrayList<String> getDataChunk(String label)
	{
		ArrayList<String> result = new ArrayList<String>();
		for (ArrayList<String> chunk : this.supplementalData)
		{
			int index = 0;
			String line = "";
			while (!line.startsWith("    :"))
			{
				line = chunk.get(index++);
			}
			if (!line.trim().equals(label))
			{
				continue;
			}
			while (index < chunk.size())
			{
				result.add(chunk.get(index++));
			}
			return result;
		}
		return result;
	}
	
	public ArrayList<String> getBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add(this.declaration);
		if (this.localRegisterCount > -1)
			result.add("    .locals " + this.localRegisterCount);
		result.addAll(this.paramDeclarations);
		if (this.methodAnnotations.size()>0)
		{
			result.addAll(this.methodAnnotations);
		}
		else
		{
			result.add("");
		}
		for (int i = 0; i < this.statements.size(); i++)
		{
			StaticStmt s = this.statements.get(i);
			result.addAll(s.getBody());
			if (i < this.statements.size()-1 || !this.supplementalData.isEmpty())
				result.add("");
		}
		for (int i = 0; i < this.supplementalData.size(); i++)
		{
			result.addAll(this.supplementalData.get(i));
			if (i < this.supplementalData.size()-1)
				result.add("");
		}
		result.add(".end method");
		return result;
	}
	
	public ArrayList<String> getInstrumentedBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add(this.declaration);
		if (this.localRegisterCount > -1)
			result.add("    .locals " + this.instrumentedLocalRegCount);
		result.addAll(this.paramDeclarations);
		if (this.methodAnnotations.size()>0)
		{
			result.addAll(this.methodAnnotations);
		}
		else
		{
			result.add("");
		}
		for (int i = 0; i < this.statements.size(); i++)
		{
			StaticStmt s = this.statements.get(i);
			result.addAll(s.getInstrumentedBody());
			if (i < this.statements.size()-1 || !this.supplementalData.isEmpty())
				result.add("");
		}
		for (int i = 0; i < this.supplementalData.size(); i++)
		{
			result.addAll(this.supplementalData.get(i));
			if (i < this.supplementalData.size()-1)
				result.add("");
		}
		result.add(".end method");
		return result;
	}
}
