package apex.symbolic.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import apex.parser.DEXParser;
import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticField;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.object.SymbolicArray;
import apex.symbolic.object.SymbolicObject;
import apex.symbolic.object.SymbolicStringBuilder;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Thrower;
import apex.symbolic.value.Value;

public class VMContext {

	public static int MaxObjectCount = 300;
	
	StaticApp staticApp;
	
	private Stack<MethodContext> methods = new Stack<MethodContext>();
	private List<SymbolicObject> objects = new ArrayList<SymbolicObject>();
	private boolean endsWithThrow = false;
	Value methodReturnedValue;
	ArrayList<String> invokeParams = new ArrayList<String>();
	private int objID = 0;
	MethodContext lastMethodContext;
	
	public VMContext(StaticApp staticApp)
	{
		this.staticApp = staticApp;
	}

	public void setRecentInvokeResult(Value v)
	{
		this.methodReturnedValue = v.clone();
	}
	
	public void push(MethodContext mc)
	{
		this.methods.push(mc);
	}
	
	public boolean stackIsEmpty()
	{
		return this.methods.isEmpty();
	}
	
	public MethodContext peek()
	{
		return this.methods.peek();
	}
	
	public MethodContext pop()
	{
		this.lastMethodContext = this.methods.pop();
		return this.lastMethodContext;
	}
	
	public MethodContext getRecentMethodContext()
	{
		if (!this.methods.isEmpty())
			return this.methods.peek();
		return this.lastMethodContext;
	}
	
	public List<SymbolicObject> getSymbolicObjects()
	{
		return this.objects;
	}
	
	public boolean endsWithThrow()
	{
		return this.endsWithThrow;
	}
	
	public StaticApp getStaticApp()
	{
		return this.staticApp;
	}
	
	private void addObject(SymbolicObject obj)
	{
		if (this.objects.size() > VMContext.MaxObjectCount)
		{
			//TODO garbage collection
		}
		this.objects.add(obj);
	}
	
	/**
	 * create an Object with given Expression, such
	 * as: $this-Lcom/my/Class;. Returns the object's
	 * address
	 * */
	//public String createObject(Expression ex, String type, boolean createInstanceFields)
	public String createObject(Expression ex, String type)
	{
		if (type.equals("Ljava/lang/StringBuilder;"))
		{
			SymbolicStringBuilder stringObj = new SymbolicStringBuilder(this.objID++, ex);
			this.addObject(stringObj);
			return stringObj.getAddress();
		}
		if (type.startsWith("["))
		{
			SymbolicArray arrayObj = new SymbolicArray(this.objID++, ex, type.substring(1));
			this.addObject(arrayObj);
			return arrayObj.getAddress();
		}
		SymbolicObject obj = new SymbolicObject(this.objID++, ex);
		this.addObject(obj);
/*		if (createInstanceFields)
		{
			StaticClass c = this.staticApp.getClassByDexName(type);
			if (c != null)
			{
				for (StaticField f : c.getFields())
				{
					if (f.isStatic())
						continue;
					this.initializeField(obj, f);
				}
			}
		}*/
		return obj.getAddress();
	}
	
	//public String createNewInstance(Expression ex, String classDexName, boolean createInstanceFields)
	public String createNewInstance(Expression ex, String classDexName)
	{
		Expression objEx = ex.clone();
		objEx.add("#"+this.objID);
		return this.createObject(objEx, classDexName/*, createInstanceFields*/);
	}
	
	public void initializeField(SymbolicObject obj, StaticField f)
	{
		this.initializeField(obj, f.getSignature());
	}
	
	public void initializeField(SymbolicObject obj, String fieldSig)
	{
		String fieldName = fieldSig.substring(fieldSig.indexOf("->")+2, fieldSig.indexOf(":"));
		String fieldType = fieldSig.substring(fieldSig.lastIndexOf(":")+1);
		if (fieldName.startsWith("this$"))
		{
			// this field should point to the $this object
			String address = this.createOrFindObjectThis(fieldType);
			ReferenceValue v = new ReferenceValue(new Expression(address), fieldType);
			obj.putField(fieldSig, v);
			return;
		}
		Expression fieldEx = new Expression("$Finstance");
		fieldEx.add(fieldSig);
		fieldEx.add(obj.getExpression());
		
		if (DEXParser.isPrimitiveType(fieldType) || fieldType.equals("Ljava/lang/String;"))
		{
			LiteralValue v = new LiteralValue(fieldEx, fieldType);
			obj.putField(fieldSig, v);
		}
		else
		{
			String address = this.createObject(fieldEx, fieldType);
			ReferenceValue v = new ReferenceValue(new Expression(address), fieldType);
			obj.putField(fieldSig, v);
		}
	}
	
	public void initializeStaticField(SymbolicObject obj, String fieldSig)
	{
		if (!obj.getExpression().getContent().equals("$static-fields"))
		{
			Thrower.throwException("trying to initialize static fields in an instance object!");
		}
		Expression fieldEx = new Expression("$Fstatic");
		fieldEx.add(fieldSig);
		String fieldType = fieldSig.substring(fieldSig.lastIndexOf(":")+1);
		if (DEXParser.isPrimitiveType(fieldType) || fieldType.equals("Ljava/lang/String;"))
		{
			LiteralValue v = new LiteralValue(fieldEx, fieldType);
			obj.putField(fieldSig, v);
		}
		else
		{
			String address = this.createObject(fieldEx, fieldType);
			ReferenceValue v = new ReferenceValue(new Expression(address), fieldType);
			obj.putField(fieldSig, v);
		}
	}
	
	public String getAddressOfObject(Expression ex)
	{
		for (SymbolicObject obj : this.objects)
		{
			if (obj.getExpression().equals(ex))
				return obj.getAddress();
		}
		return "";
	}
	
	public SymbolicObject getObject(String address)
	{
		for (SymbolicObject obj : this.objects)
		{
			if (obj.getAddress().equals(address))
			{
				return obj;
			}
		}
		return null;
	}
	
	public String createOrFindObjectThis(String classDexName)
	{
		Expression thisEx = new Expression("$this");
		thisEx.add(classDexName);
		String address = this.getAddressOfObject(thisEx);
		if (address.equals(""))
		{
			address = this.createObject(thisEx, classDexName);
		}
		return address;
	}
	
	public Expression getPathCondition(Expression cond)
	{
		Expression result = new Expression(cond.getContent());
		String leftReg = cond.getChild(0).getContent();
		String rightReg = cond.getChild(1).getContent();
		MethodContext mc = this.methods.peek();
		
		// Left part can only be a register
		Value leftV = mc.getRegister(leftReg).getValue();
		if (leftV instanceof LiteralValue)
		{
			result.add(leftV.getExpression().clone());
		}
		else
		{
			ReferenceValue refV = (ReferenceValue) leftV;
			SymbolicObject lobj = this.getObject(refV.getAddress());
			if (lobj == null)
			{
				result.add("null");
			}
			else
			{
				result.add(lobj.getExpression().clone());
			}
		}
		
		// Right part. Could be 0/null, or a register, or a number(from switch statement)
		if (rightReg.equals("0"))
		{
			if ((leftV instanceof ReferenceValue) || leftV.getType().equals("Ljava/lang/String;"))
				result.add("null");
			else
				result.add("0");
		}
		else if (rightReg.startsWith("v") || rightReg.startsWith("p"))	// registers
		{
			Register rreg = mc.getRegister(rightReg);
			Value rightV = rreg.getValue();
			if (rightV instanceof LiteralValue)
			{
				result.add(rightV.getExpression().clone());
			}
			else
			{
				ReferenceValue refV = (ReferenceValue) rightV;
				SymbolicObject obj = this.getObject(refV.getAddress());
				if (obj == null)
				{
					result.add("null");
				}
				else
				{
					result.add(obj.getExpression().clone());
				}
			}
		}
		else	// numbers
		{
			result.add(rightReg);
		}
		return result;
	}
	
	public void iput(ReferenceValue objRef, String fieldSig, Value value)
	{
		SymbolicObject obj = this.getObject(objRef.getAddress());
		if (obj == null)
		{
			Thrower.throwException("VMContext.iput() can't find the object!");
		}
		obj.putField(fieldSig, value);
	}
	
	public Expression getTrueExpression(Value v)
	{
		if (v instanceof LiteralValue)
			return v.getExpression();
		if (v instanceof ReferenceValue)
		{
			return this.getObject(v.getExpression().getContent()).getExpression();
		}
		return null;
	}
	
	public Value iget(ReferenceValue objRef, String fieldSig)
	{
		SymbolicObject obj = this.getObject(objRef.getAddress());
		if (obj == null)
		{
			Thrower.throwException("VMContext.iget() can't find the object!");
		}
		Value fieldValue = obj.getField(fieldSig);
		while (fieldValue == null)
		{
			this.initializeField(obj, fieldSig);
			fieldValue = obj.getField(fieldSig);
		}
		return fieldValue;
	}
	
	public void sput(String fieldSig, Value value)
	{
		String className = fieldSig.substring(0, fieldSig.indexOf("->"));
		Expression staticClassEx = new Expression("$static-fields");
		staticClassEx.add(className);
		String address = this.getAddressOfObject(staticClassEx);
		if (address.equals(""))
		{
			address = this.createObject(staticClassEx, className);
		}
		this.getObject(address).putField(fieldSig, value);
	}
	
	public Value sget(String fieldSig)
	{
		String className = fieldSig.split("->")[0];
		Expression staticClassEx = new Expression("$static-fields");
		staticClassEx.add(className);
		String address = this.getAddressOfObject(staticClassEx);
		if (address.equals(""))
		{
			address = this.createObject(staticClassEx, className);
		}
		SymbolicObject obj = this.getObject(address);
		Value result = obj.getField(fieldSig);
		if (result == null)
		{
			this.initializeStaticField(obj, fieldSig);
			result = obj.getField(fieldSig);
		}
		return result;
	}
	

	
	public void applyOperation(StaticStmt s)
	{
		StaticMethod m = s.getContainingMethod();
		if (s.isFirstStmtOfMethod())
		{
			MethodContext mc = new MethodContext(m, this);
			this.methods.push(mc);
		}
		if (s.isThrowStmt())
		{
			this.endsWithThrow = true;
			this.pop();
			this.invokeParams.clear();
		}
		else if (s.isReturnStmt())
		{
			this.endsWithThrow = false;
			MethodContext mc = this.pop();
			this.invokeParams.clear();
			String returnedVariable = s.getReturnedVariable();
			if (!returnedVariable.equals(""))
			{
				Value returnedValue = mc.getRegister(returnedVariable).getValue();
				this.methodReturnedValue = returnedValue.clone();
				if (!this.methods.isEmpty())
				{
					this.methods.peek().putResult(returnedValue);
				}
			}
		}
		else
		{
			if (s.isInvokeStmt())
			{
				this.invokeParams = s.getInvokeParameters();
			}
			if (!this.methods.isEmpty())
			{
				MethodContext mc = this.methods.peek();
				mc.applyStatement(s);
			}
		}
	}
	
	public VMContext clone()
	{
		VMContext result = new VMContext(this.staticApp);
		for (MethodContext mc : this.methods)
		{
			MethodContext newMC = mc.clone();
			newMC.vm = this;
			result.methods.add(newMC);
		}
		for (SymbolicObject obj : objects)
		{
			result.objects.add(obj.clone());
		}
		result.endsWithThrow = this.endsWithThrow;
		result.methodReturnedValue = this.methodReturnedValue==null?null:this.methodReturnedValue.clone();
		result.invokeParams = new ArrayList<String>(this.invokeParams);
		result.objID = this.objID;
		return result;
	}
	
	public VMContext copy()
	{
		VMContext result = new VMContext(this.staticApp);
		for (SymbolicObject obj : objects)
		{
			result.objects.add(obj.clone());
		}
		result.objID = this.objID;
		return result;
	}
	
	
	public void printSnapshot()
	{
		System.out.println("---------------VM Snapshot---------------");
		System.out.println("<method contexts>");
		for (MethodContext mc : methods)
			mc.printSnapshot();
		System.out.println("\n<objects>");
		for (SymbolicObject obj : objects)
			obj.print();
		System.out.println("-----------End of VM Snapshot------------");
	}


}
