package apex.symbolic.context;

import java.util.ArrayList;

import apex.parser.DEXParser;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.SymbolicExecutionBlacklist;
import apex.symbolic.object.SymbolicArray;
import apex.symbolic.object.SymbolicObject;
import apex.symbolic.object.model.Controller;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Thrower;
import apex.symbolic.value.Value;

public class MethodContext {

	ArrayList<Register> registers = new ArrayList<Register>();
	Value recentResult;
	VMContext vm;
	private StaticMethod m;
	
	private MethodContext()
	{};
	
	public MethodContext(StaticMethod m, VMContext vm)
	{
		this.m = m;
		this.vm = vm;
		for (int i = 0; i < m.getLocalRegisterCount(); i++)
		{
			Register reg = new Register("v"+i, false);
			this.registers.add(reg);
		}
		if (vm.invokeParams.isEmpty())	// no initialization of the parameters
		{
			int paramIndex = 0;
			for (int i = 0; i < m.getParamTypes().size(); i++)
			{
				String paramType = m.getParamTypes().get(i);
				Register reg = new Register("p"+paramIndex++, true);
				if (i == 0 && !m.isStatic())
				{
					String address = this.vm.createOrFindObjectThis(m.getDeclaringClass().getDexName());
					ReferenceValue v = new ReferenceValue(new Expression(address), paramType);
					reg.assign(v);
				}
				else
				{
					Expression ex = new Expression("$" + reg.name);
					if (DEXParser.isPrimitiveType(paramType) || paramType.equals("Ljava/lang/String;"))
					{
						LiteralValue v = new LiteralValue(ex, paramType);
						reg.assign(v);
					}
					else
					{
						String address = vm.createObject(ex, paramType);
						ReferenceValue v = new ReferenceValue(new Expression(address), paramType);
						reg.assign(v);
					}
				}
				this.registers.add(reg);
				if (paramType.equals("J") || paramType.equals("D"))
				{
					Register secondHalf = new Register("p"+paramIndex++, true);
					secondHalf.isLocked = true;
					this.registers.add(secondHalf);
				}
			}
		}
		else	// since this is a nested invoke, we can initialize the parameters
		{		// clear the invokeParams field in the end
			MethodContext outerMC = vm.pop();
			vm.push(outerMC);
			int paramRegIndex = 0;
			for (int i = 0; i < m.getParamTypes().size(); i++)
			{
				String paramType = m.getParamTypes().get(i);
				Register reg = new Register("p"+paramRegIndex, true);
				Register sourceReg = outerMC.getRegister(vm.invokeParams.get(paramRegIndex));
				paramRegIndex++;
				reg.assign(sourceReg.getValue());
				this.registers.add(reg);
				if (paramType.equals("J") || paramType.equals("D"))
				{
					Register secondHalf = new Register("p"+paramRegIndex++, true);
					secondHalf.isLocked = true;
					this.registers.add(secondHalf);
				}
			}
			vm.invokeParams.clear();
		}
	}
	

	public void applyStatement(StaticStmt s)
	{
//deal with APIs
		if (s.isInvokeStmt())
		{
			modelInvokeStatement(s);
			return;
		}
		Expression ex = s.getOperationExpression();
		if (ex.getContent().equals(""))
		{
			return;
		}
		Expression left = ex.getChild(0);
		Expression right = ex.getChild(1);
// $result = $array
		if (left.getContent().equals("$result"))
		{
			if (right.getContent().equals("$array"))
			{
				String arrayType = "[" + right.getChild(1).getContent();
				String address = this.vm.createObject(right, arrayType);
				ReferenceValue arrayRef = new ReferenceValue(new Expression(address), arrayType);
				this.putResult(arrayRef.clone());
			}
		}
// $Finstance = vA
		else if (left.getContent().equals("$Finstance"))
		{
			Register sourceReg = this.getRegister(right.getContent());
			String fieldSig = left.getChild(0).getContent();
			String objRegName = left.getChild(1).getContent();
			Register objReg = this.getRegister(objRegName);
			if (!(objReg.getValue() instanceof ReferenceValue))
			{
				Thrower.throwException("$Finstance object register not holding ReferenceValue at " + s.getUniqueID());
			}
			ReferenceValue objRef = (ReferenceValue)objReg.getValue();
			this.vm.iput(objRef, fieldSig, sourceReg.getValue().clone());
		}
// $Fstatic = vA
		else if (left.getContent().equals("$Fstatic"))
		{
			Register sourceReg = this.getRegister(right.getContent());
			String fieldSig = left.getChild(0).getContent();
			this.vm.sput(fieldSig, sourceReg.getValue().clone());
		}
// vA = ...
		else if (right.getContent().startsWith("v") || right.getContent().startsWith("p"))
		{
			Register sourceReg = this.getRegister(right.getContent());
			this.writeRegister(left.getContent(), sourceReg.getValue().clone());
		}
		else if (right.getContent().equals("$this"))
		{
			String className = right.getChild(0).getContent();
			String address = this.vm.createOrFindObjectThis(className);
			ReferenceValue v = new ReferenceValue(new Expression(address), className);
			this.writeRegister(left.getContent(), v);
		}
		else if (right.getContent().equals("$Finstance"))
		{
			String fieldSig = right.getChild(0).getContent();
			String objRegName = right.getChild(1).getContent();
			Register objReg = this.getRegister(objRegName);
			if (!(objReg.getValue() instanceof ReferenceValue))
			{
				Thrower.throwException("$Finstance object register not holding ReferenceValue at " + s.getUniqueID());
			}
			ReferenceValue objRef = (ReferenceValue)objReg.getValue();
			Value fieldValue = this.vm.iget(objRef, fieldSig);
			this.writeRegister(left.getContent(), fieldValue.clone());
		}
		else if (right.getContent().equals("$Fstatic"))
		{
			String fieldSig = right.getChild(0).getContent();
			Value fieldValue = this.vm.sget(fieldSig);
			this.writeRegister(left.getContent(), fieldValue.clone());
		}
		else if (right.getContent().equals("$result"))
		{
			this.writeRegister(left.getContent(), this.recentResult.clone());
			this.recentResult = null;
		}
		else if (right.getContent().equals("$number"))
		{
			Expression numEx = right.getChild(0);
			String type = right.getChild(1).getContent();
			LiteralValue v = new LiteralValue(numEx.clone(), type);
			this.writeRegister(left.getContent(), v);
		}
		else if (right.getContent().equals("$const-class"))
		{
			LiteralValue v = new LiteralValue(right.getChild(0).clone(), "const-class");
			this.writeRegister(left.getContent(), v);
		}
		else if (right.getContent().equals("$const-string"))
		{
			LiteralValue v = new LiteralValue(right.clone(), "Ljava/lang/String;");
			this.writeRegister(left.getContent(), v);
		}
		else if (right.getContent().equals("$instance-of"))
		{	// Just gonna try to solve it right here
			Expression instanceEx = right.getChild(0);
			String targetType = right.getChild(1).getContent();
			String thisType = this.getRegister(instanceEx.getContent()).getValue().getType();
			if (thisType.equals(targetType))
			{
				this.writeRegister(left.getContent(), new LiteralValue(new Expression("1"), "Z"));
			}
			else
			{
				this.writeRegister(left.getContent(), new LiteralValue(new Expression("0"), "Z"));
			}
		}
		else if (right.getContent().equals("$array-length"))
		{
			String arrayRegName = right.getChild(0).getContent();
			SymbolicArray arrayObj = getArrayObject(s, arrayRegName);
			int length = arrayObj.getLength();
			LiteralValue v = new LiteralValue(new Expression(length+""), "I");
			this.writeRegister(left.getContent(), v);
		}
		else if (right.getContent().equals("$new-instance"))
		{
			String classDexName = right.getChild(0).getContent();
			String address = vm.createNewInstance(right.clone(), classDexName);
			ReferenceValue v = new ReferenceValue(new Expression(address), classDexName);
			this.writeRegister(left.getContent(), v);
		}
		else if (right.getContent().equals("$array"))
		{
			//first initiate the array with length and type
			String arrayType = "[" + right.getChild(1).getContent();
			Expression arrayEx = new Expression("$array");
			String length = right.getChild(0).getContent();
			if (length.startsWith("v") || length.startsWith("p"))
			{
				Expression realLengthEx = this.getRegister(length).getValue().getExpression();
				arrayEx.add(realLengthEx.clone());
			}
			else
			{
				arrayEx.add(right.getChild(0).clone());
			}
			arrayEx.add(right.getChild(1).clone());
			String address = this.vm.createObject(arrayEx, arrayType);
			ReferenceValue v = new ReferenceValue(new Expression(address), arrayType);
			this.writeRegister(left.getContent(), v);
			// then put each element in (if there are any)
			SymbolicArray arrayObj = (SymbolicArray) this.vm.getObject(address);
			for (int i = 2; i < right.getChildCount(); i++)
			{
				Expression elementEx = right.getChild(i);
				int index = Integer.parseInt(elementEx.getChild(0).getContent());
				LiteralValue indexV = new LiteralValue(new Expression(index+""), "I");
				Expression valueEx = elementEx.getChild(1);
				if (valueEx.getContent().startsWith("v") || valueEx.getContent().startsWith("p"))
				{
					Value valueToPut = this.getRegister(valueEx.getContent()).getValue();
					arrayObj.aput(indexV, valueToPut);
				}
				else if (valueEx.getContent().equals("$number"))// if it's not a register name, then it's definitely a number
				{
					Expression decEx = valueEx.getChild(0);
					String type = valueEx.getChild(1).getContent();
					Value valueToPut = new LiteralValue(decEx.clone(), type);
					arrayObj.aput(indexV, valueToPut);
				}
				else
				{
					Thrower.throwException("$array Expression element is neither register nor number");
				}
			}

		}
		else if (right.getContent().equals("$aget"))
		{
			String arrayRegName = right.getChild(0).getContent();
			String indexRegName = right.getChild(1).getContent();
			SymbolicArray arrayObj = getArrayObject(s, arrayRegName);
			Value index = this.getRegister(indexRegName).getValue();
			if (!(index instanceof LiteralValue))
			{
				Thrower.throwException("aget index is not LiteralValue at " + s.getUniqueID());
			}
			Value elementValue = arrayObj.aget((LiteralValue)index);
			this.writeRegister(left.getContent(), elementValue);
		}
		else if (right.getContent().equals("$aput"))
		{
			String arrayRegName = right.getChild(0).getContent();
			String indexRegName = right.getChild(1).getContent();
			SymbolicArray arrayObj = getArrayObject(s, arrayRegName);
			Value index = this.getRegister(indexRegName).getValue();
			if (!(index instanceof LiteralValue))
			{
				Thrower.throwException("aput index is not LiteralValue at " + s.getUniqueID());
			}
			Value toPut = this.getRegister(left.getContent()).getValue();
			arrayObj.aput((LiteralValue)index, toPut);
		}
		else if (DEXParser.isArithmaticalOperator(right.getContent()))	// operation of the literals
		{
			if (right.getContent().equals("to"))	// this one has 2 op fields
			{
				String srcReg = right.getChild(0).getContent();
				String type = right.getChild(1).getContent();
				LiteralValue v = new LiteralValue(this.getRegister(srcReg).getValue().getExpression(), type);
				this.writeRegister(left.getContent(), v);
			}
			else	// the rest has 3 op fields
			{
				Expression result = new Expression(right.getContent());
				for (int i = 0; i < 2; i++)
				{
					String regName = right.getChild(i).getContent();
					if (regName.startsWith("v") || regName.startsWith("p"))
					{
						Value v = this.getRegister(regName).getValue();
						if (!(v instanceof LiteralValue))
						{
							Thrower.throwException("Arithmatical Operation reads non Literal Value at " + s.getUniqueID());
						}
						result.add(v.getExpression().clone());
					}
					else
					{
						result.add(right.getChild(i).clone());
					}
				}
				String type = right.getChild(2).getContent();
				LiteralValue v = new LiteralValue(result, type);
				this.writeRegister(left.getContent(), v);
			}
		}
	}
	
	
	private SymbolicArray getArrayObject(StaticStmt s, String arrayRegName)
	{
		Value arrayRef = this.getRegister(arrayRegName).getValue();
		if (!(arrayRef instanceof ReferenceValue))
		{
			Thrower.throwException(s.getBytecodeOperator() + " got non-Reference value array at " + s.getUniqueID(), this.vm);
		}
		SymbolicObject arrayObj = this.vm.getObject(arrayRef.getExpression().getContent());
		if (!(arrayObj instanceof SymbolicArray))
		{
			Thrower.throwException(s.getBytecodeOperator() + " got non-SymbolicArray object array at " + s.getUniqueID(), this.vm);
		}
		return (SymbolicArray)arrayObj;
	}
	
	public void modelInvokeStatement(StaticStmt s)
	{
		if (!s.isInvokeStmt())
			return;
		String invokeSig = s.getInvokeSignature();
		StaticMethod targetM = this.vm.staticApp.getMethod(invokeSig);
		if (targetM != null && !SymbolicExecutionBlacklist.classInBlackList(targetM.getDeclaringClass().getDexName()))
			return;
		if (Controller.tryAllModelers(vm, this, s))
		{}
		else if (!invokeSig.endsWith(")V"))
		{
			ArrayList<String> params = s.getInvokeParameters();
			String returnType = invokeSig.substring(invokeSig.lastIndexOf(")")+1);
			if (	params.size() == 1 && 
					!s.getBytecodeOperator().startsWith("invoke-static") &&
					this.vm.staticApp.getClassByDexName(this.getRegister(params.get(0)).getValue().getType())!= null
				)
			{
				// in this case, we just create a fake field for that object
				// and change invoke into iget, and put result
				ReferenceValue thisReference = (ReferenceValue) this.getRegister(params.get(0)).getValue();
				String fakeFieldName = "apex_" + invokeSig.substring(invokeSig.indexOf("->")+2, invokeSig.indexOf("("));
				String fakeFieldSubSig = fakeFieldName + ":" + returnType;
				String fakeFieldSig = thisReference.getType() + "->" + fakeFieldSubSig;
				this.putResult(this.vm.iget(thisReference, fakeFieldSig));
			}
			else
			{
				// just replace each parameter reg name with its value
				// create a corresponding Literal/Reference value
				// and put it in this.recentInvokeResult
				Expression resultEx = new Expression("$api");
				resultEx.add(invokeSig);
				for (int i = 0; i < params.size(); i++)
				{
					String piRegName = params.get(i);
					Value piValue = this.getRegister(piRegName).getValue();
					if (piValue != null)
					{
						if (piValue instanceof LiteralValue)
						{
							resultEx.add(piValue.getExpression().clone());
						}
						else if (piValue instanceof ReferenceValue)
						{
							SymbolicObject obj = this.vm.getObject(((ReferenceValue) piValue).getAddress()); 
							resultEx.add(obj.getExpression());
						}
					}
				}
				if (DEXParser.isPrimitiveType(returnType) || returnType.equals("Ljava/lang/String;"))
				{
					LiteralValue v = new LiteralValue(resultEx, returnType);
					this.putResult(v);
				}
				else
				{
					String address = this.vm.createObject(resultEx, returnType);
					ReferenceValue v = new ReferenceValue(new Expression(address), returnType);
					this.putResult(v);
				}
			}
		}
	}
	
	public void putResult(Value v)
	{
		this.recentResult = v.clone();
	}
	
	public Register getRegister(String name)
	{
		for (Register reg : this.registers)
		{
			if (reg.name.equals(name))
				return reg;
		}
		return null;
	}
	
	public ArrayList<Register> getRegisters()
	{
		return this.registers;
	}
	
	public void writeRegister(String destName, Value value)
	{
		this.getRegister(destName).assign(value.clone());
		if (value.getType().equals("J") || value.getType().equals("D"))
		{
			int vAIndex = Integer.parseInt(destName.substring(1));
			this.lockRegister(destName.substring(0, 1) + (vAIndex+1));
		}
	}
	
	public void lockRegister(String regName)
	{
		this.getRegister(regName).lock();
	}
	
	public void printSnapshot()
	{
		System.out.println("\nMethodContext snapshot - " + m.getSignature());
		for (Register reg : this.registers)
		{
			reg.printSnapshot();
		}
	}
	
	public StaticMethod getStaticMethod()
	{
		return this.m;
	}
	
	public MethodContext clone()
	{
		MethodContext result = new MethodContext();
		
		for (Register reg : this.registers)
		{
			result.registers.add(reg.clone());
		}
		
		if (this.recentResult == null)
			result.recentResult = null;
		else
			result.recentResult = this.recentResult.clone();
		
		result.m = this.m;
		
		return result;
	}
	
}
