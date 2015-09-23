package apex.symbolic.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.object.SymbolicObject;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Value;

public class VMContext {

	Stack<MethodContext> methods = new Stack<MethodContext>();
	List<SymbolicObject> objects = new ArrayList<SymbolicObject>();
	
	
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
	 * id
	 * */
	public String createObject(Expression ex)
	{
		SymbolicObject obj = new SymbolicObject(this.objects.size(), ex);
		this.objects.add(obj);
		return obj.getAddress();
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
			address = this.createObject(thisEx);
		}
		return address;
	}
	
	public void iput(ReferenceValue objRef, String fieldSig, Value value)
	{
		for (SymbolicObject obj : this.objects)
		{
			if (obj.getAddress().equals(objRef.getAddress()))
			{
				obj.putField(fieldSig, value.clone());
				break;
			}
		}
	}
	
	public void sput(String fieldSig, Value value)
	{
		String className = fieldSig.substring(0, fieldSig.indexOf("->"));
		Expression staticClassEx = new Expression("$static-fields");
		staticClassEx.add(className);
		String address = this.getAddressOfObject(staticClassEx);
		if (address.equals(""))
		{
			address = this.createObject(staticClassEx);
		}
		this.getObject(address).putField(fieldSig, value);
	}
	
	public void createArrayObject(Expression arrayEx)
	{
		
	}
	
	public void createStringObject(Expression stringEx)
	{
		
	}
	
	public void applyOperation(StaticStmt s)
	{
		StaticMethod m = s.getContainingMethod();
		if (s.isThrowStmt() || s.isReturnStmt())
		{
			//TODO process throw and return
			
			// Then throw away current method context
			this.pop();
			return;
		}
		MethodContext mc = this.pop();
		this.push(mc);
		if (!mc.getStaticMethod().equals(m))
		{
			if (s.isFirstStmtOfMethod())
			{
				mc = new MethodContext(m, this);
				this.push(mc);
			}
		}
		mc.applyStatement(s);
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


}
