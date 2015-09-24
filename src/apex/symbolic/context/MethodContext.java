package apex.symbolic.context;

import java.util.ArrayList;

import apex.parser.DEXParser;
import apex.staticFamily.StaticMethod;
import apex.staticFamily.StaticStmt;
import apex.symbolic.Expression;
import apex.symbolic.value.LiteralValue;
import apex.symbolic.value.ReferenceValue;
import apex.symbolic.value.Value;

public class MethodContext {

	ArrayList<Register> registers = new ArrayList<Register>();
	Value recentResult;
	private VMContext vm;
	private StaticMethod m;
	
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
					if (DEXParser.isPrimitiveType(paramType))
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
	/**
	 * Parse statement, and apply corresponding operations
	 * on method context and/or vm context.
	 * Possible operation types:
	 * 	vA = vB
	 * 	vA = $result
	 * 	vA = $number
	 * 	vA = $const-string
	 * 	vA = $const-class
	 * 	vA = $instance-of
	 * 	vA = $array-length
	 * 	vA = $new-instance
	 * 	vA = $array
	 * 	vA = $aget
	 * 	vA = $aput
	 * 	vA = $Finstance		$Finstance = vA
	 * 	vA = $Fstatic		$Fstatic = vA
	 * 	vA = [arithmatical op]
	 * 	$result = $array
	 * */
	public void applyStatement(StaticStmt s)
	{
		Expression ex = s.getOperationExpression();
		if (ex.getContent().equals(""))
			return;
		Expression left = ex.getChild(0);
		Expression right = ex.getChild(1);
// $result = $array
		if (left.getContent().equals("$result"))
		{
			if (right.getContent().equals("$array"))
			{
				String address = this.vm.createArrayObject(right);
				String arrayType = "[" + right.getChild(1);
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
				System.out.println("$Finstance object register not holding ReferenceValue at " + s.getUniqueID());
				System.exit(1);
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
/** 	vA = vB
	 * 	vA = $result
	 * 	vA = $number
	 * 	vA = $const-string
	 * 	vA = $const-class
	 * 	vA = $instance-of
	 * 	vA = $array-length
	 * 	vA = $new-instance
	 * 	vA = $array
	 * 	vA = $aget
	 * 	vA = $aput
	 * 	vA = $Finstance		$Finstance = vA
	 * 	vA = $Fstatic		$Fstatic = vA
	 * 	vA = [arithmatical op]*/
		else if (right.getContent().startsWith("v"))
		{
			Register sourceReg = this.getRegister(right.getContent());
			this.writeRegister(left.getContent(), sourceReg.getValue());
		}
		else if (right.getContent().equals("$Finstance"))
		{
			String fieldSig = right.getChild(0).getContent();
			String objRegName = right.getChild(1).getContent();
			Register objReg = this.getRegister(objRegName);
			if (!(objReg.getValue() instanceof ReferenceValue))
			{
				System.out.println("$Finstance object register not holding ReferenceValue at " + s.getUniqueID());
				System.exit(1);
			}
			ReferenceValue objRef = (ReferenceValue)objReg.getValue();
			Value fieldValue = this.vm.iget(objRef, fieldSig);
			this.writeRegister(left.getContent(), fieldValue.clone());
		}
		else if (right.getContent().equals("$Fstatic"))
		{
			
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
			LiteralValue v = new LiteralValue(numEx, type);
			this.writeRegister(left.getContent(), v);
		}
		else if (right.getContent().startsWith("$const"))
		{
			
		}
		else if (right.getContent().equals("$instance-of"))
		{
			
		}
		else if (right.getContent().equals("$array-length"))
		{
			
		}
		else if (right.getContent().equals("$new-instance"))
		{
			String classDexName = right.getChild(0).getContent();
			String address = vm.createObject(right.clone(), classDexName);
			ReferenceValue v = new ReferenceValue(new Expression(address), classDexName);
			this.writeRegister(left.getContent(), v);
		}
		else if (right.getContent().equals("$array"))
		{
			
		}
		else if (right.getContent().equals("$aget"))
		{
			
		}
		else if (right.getContent().equals("$aput"))
		{
			
		}
		else if (DEXParser.isArithmaticalOperator(right.getContent()))	// operation of the literals
		{
			Expression result = new Expression(ex.getContent());
			for (int i = 0; i < right.getChildCount(); i++)
			{
				String regName = right.getChild(i).getContent();
				Value v = this.getRegister(regName).getValue();
				if (!(v instanceof LiteralValue))
				{
					System.out.println("Arithmatical Operation reads non Literal Value at " + s.getUniqueID());
					System.exit(1);
				}
				result.add(v.getExpression().clone());
			}
			//TODO need to determine the type
			LiteralValue v = new LiteralValue(result, "");
			this.writeRegister(left.getContent(), v);
		}
	}
	
	public void putResult(Value v)
	{
		this.recentResult = v;
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
	
	public void writeRegister(String destName, Value value)
	{
		this.getRegister(destName).assign(value.clone());
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
	
}
