package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.statements.CatScriptProgram;
import org.objectweb.asm.Opcodes;
import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListLiteralExpression extends Expression {
    List<Expression> values;
    private CatscriptType type;

    public ListLiteralExpression(List<Expression> values) {
        this.values = new LinkedList<>();
        for (Expression value : values) {
            this.values.add(addChild(value));
        }
    }

    public List<Expression> getValues() {
        return values;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        for (Expression value : values) {
            value.validate(symbolTable);
        }
        if (values.size() > 0) {
            CatscriptType temp = values.get(0).getType();
            for(int i = 1; i < values.size(); i++){
                if(values.get(i).getType() != temp){
                    addError(ErrorType.INCOMPATIBLE_TYPES);
                }
            }
            type = CatscriptType.getListType(temp);
        } else {
            type = CatscriptType.getListType(CatscriptType.OBJECT);
        }
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        ArrayList<Object> list = new ArrayList<>();
        for(Expression expression : values){
            list.add(expression.evaluate(runtime));
        }
        return list;
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        code.addTypeInstruction(Opcodes.NEW, ByteCodeGenerator.internalNameFor(ArrayList.class));
        code.addInstruction(Opcodes.DUP);
        code.addMethodInstruction(Opcodes.INVOKESPECIAL, ByteCodeGenerator.internalNameFor(ArrayList.class), "<init>", "()V");
        for (Expression value : values){
            code.addInstruction(Opcodes.DUP);
            value.compile(code);
            box(code, value.getType());
            code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, internalNameFor(ArrayList.class), "add", "(Ljava/lang/Object;)Z");
            code.addInstruction(Opcodes.POP);
        }
    }


}
