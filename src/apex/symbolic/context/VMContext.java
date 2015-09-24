package apex.symbolic.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import apex.parser.DEXParser;
import apex.staticFamily.StaticApp;
import apex.staticFamily.StaticClass;
import apex.staticFamily.StaticField;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.object.SymbolicObject;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Value;

public class VMContext {

	private StaticApp staticApp;
	
	Stack<MethodContext> methods = new Stack<MethodContext>();
	List<SymbolicObject> objects = new ArrayList<SymbolicObject>();
	private boolean endsWithThrow = false;
	Value methodReturnedValue;
	ArrayList<String> invokeParams = new ArrayList<String>();
	
	public VMContext(StaticApp staticApp)
	{
		this.staticApp = staticApp;
	}
	
	public void push(MethodContext mc)
	{
		this.methods.push(mc);
	}
	
	public MethodContext pop()
	{
		return this.methods.pop();
	}
	
	/**
	 * create an Object with given Expression, such
	 * as: $this-Lcom/my/Class;. Returns the object's
	 * address
	 * */
	public String createObject(Expression ex, String classDexName)
	{
		SymbolicObject obj = new SymbolicObject(this.objects.size(), ex);
		this.objects.add(obj);
		StaticClass c = this.staticApp.getClassByDexName(classDexName);
		if (c != null)
		{
			for (StaticField f : c.getFields())
			{
				if (f.isStatic())
					continue;
				this.initializeField(obj, f);
			}
		}
		return obj.getAddress();
	}
	
	private void initializeField(SymbolicObject obj, StaticField f)
	{
		Expression fieldEx = new Expression("$Finstance");
		fieldEx.add(f.getSignature());
		fieldEx.add(obj.getExpression());
		if (DEXParser.isPrimitiveType(f.getType()))
		{
			LiteralValue v = new LiteralValue(fieldEx, f.getType());
			obj.putField(f.getSignature(), v);
		}
		else
		{
			String address = this.createObject(fieldEx, f.getType());
			ReferenceValue v = new ReferenceValue(new Expression(address), f.getType());
			obj.putField(f.getSignature(), v);
		}
	}
	
	private void initializeField(SymbolicObject obj, String fieldSig)
	{
		Expression fieldEx = new Expression("$Finstance");
		fieldEx.add(fieldSig);
		fieldEx.add(obj.getExpression());
		String fieldType = fieldSig.substring(fieldSig.lastIndexOf(":")+1);
		if (DEXParser.isPrimitiveType(fieldType))
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
		MethodContext mc = this.pop();
		this.push(mc);
		
		// Left part
		Value leftV = mc.getRegister(leftReg).getValue();
		if (leftV instanceof LiteralValue)
		{
			result.add(leftV.getExpression().clone());
		}
		else
		{
			ReferenceValue refV = (ReferenceValue) leftV;
			result.add(this.getObject(refV.getAddress()).getExpression().clone());
		}
		
		// Right part. Could be 0, and it could mean 'null'
		if (rightReg.equals("0"))
		{
			if ((leftV instanceof ReferenceValue))
				result.add("null");
			else
				result.add("0");
		}
		else
		{
			Value rightV = mc.getRegister(rightReg).getValue();
			if (rightV instanceof LiteralValue)
			{
				result.add(rightV.getExpression().clone());
			}
			else
			{
				ReferenceValue refV = (ReferenceValue) rightV;
				result.add(this.getObject(refV.getAddress()).getExpression().clone());
			}
		}
		return result;
	}
	
	public void iput(ReferenceValue objRef, String fieldSig, Value value)
	{
		SymbolicObject obj = this.getObject(objRef.getAddress());
		if (obj == null)
		{
			System.out.println("iput can't find the object!!!");
			System.exit(1);
		}
		obj.putField(fieldSig, value);
	}
	
	public Value iget(ReferenceValue objRef, String fieldSig)
	{
		SymbolicObject obj = this.getObject(objRef.getAddress());
		if (obj != null)
		{
			Value fieldValue = obj.getField(fieldSig);
			while (fieldValue == null)
			{
				this.initializeField(obj, fieldSig);
				fieldValue = obj.getField(fieldSig);
			}
			return fieldValue;
		}
		return null;
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
	
	public String createArrayObject(Expression arrayEx)
	{
		//TODO
		return "";
	}
	
	public String createStringObject(Expression stringEx)
	{
		//TODO
		return "";
	}
	
	public void applyOperation(StaticStmt s)
	{
		StaticMethod m = s.getContainingMethod();
		if (s.isFirstStmtOfMethod())
		{
			MethodContext mc = new MethodContext(m, this);
			this.methods.push(mc);
			mc.applyStatement(s);
		}
		if (s.isThrowStmt())
		{
			this.endsWithThrow = true;
			this.methods.pop();
		}
		else if (s.isReturnStmt())
		{
			MethodContext mc = this.methods.pop();
			String returnedVariable = s.getReturnedVariable();
			if (!returnedVariable.equals(""))
			{
				Value returnedValue = mc.getRegister(returnedVariable).getValue();
				this.methodReturnedValue = returnedValue.clone();
			}
		}
		else if (s.isInvokeStmt())
		{
			this.invokeParams = s.getInvokeParameters();
		}
		else
		{
			MethodContext mc = this.pop();
			mc.applyStatement(s);
			this.push(mc);
		}
	}
	
	public void printSnapshot()
	{
		System.out.println("---------------VM Snapshot---------------");
		System.out.println("=== method contexts");
		for (MethodContext mc : methods)
			mc.printSnapshot();
		System.out.println("=== objects");
		for (SymbolicObject obj : objects)
			obj.print();
		System.out.println("-----------End of VM Snapshot------------");
	}

	public boolean endsWithThrow()
	{
		return this.endsWithThrow;
	}






}
