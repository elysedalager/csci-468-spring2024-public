package edu.montana.csci.csci468.demo;

import edu.montana.csci.csci468.CatscriptTestBase;
import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.CatScriptProgram;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PartnerTests extends CatscriptTestBase {

    @Test
    public void parseEqualityAdditiveExpression() {
        EqualityExpression expr = parseExpression("(1 + 1) == (0 + 2)");
        assertTrue(expr.getLeftHandSide() instanceof ParenthesizedExpression);
        assertTrue(expr.getRightHandSide() instanceof ParenthesizedExpression);
        assertTrue(expr.isEqual());
    }

    @Test
    public void parseListLiteralAdditiveExpression(){
        ListLiteralExpression expr = parseExpression("[[0, 1], [2]]");
        assertEquals(2, expr.getValues().size());
        ListLiteralExpression innerList = (ListLiteralExpression) expr.getValues().get(0);
        assertEquals(2, innerList.getValues().size());
    }

    @Test
    public void parseUnaryExpression() {
        UnaryExpression expr = parseExpression("not not not true");
        assertTrue(expr.isNot());
        assertTrue(expr.getRightHandSide() instanceof UnaryExpression);
    }
}