package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;
import static edu.montana.csci.csci468.tokenizer.TokenType.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch(RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        Statement statement = parseFunctionDefinition();
        if (statement != null) {
            return statement;
        }
        statement = parseStatement();
        if (statement != null){
            return statement;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseFunctionDefinition(){
        if (tokens.match(FUNCTION)){
            FunctionDefinitionStatement functionDefinitionStatement = new FunctionDefinitionStatement();
            functionDefinitionStatement.setStart(tokens.consumeToken());
            Token function = require(IDENTIFIER, functionDefinitionStatement);
            functionDefinitionStatement.setName(function.getStringValue());

            require(LEFT_PAREN, functionDefinitionStatement);
            if(!tokens.match(RIGHT_PAREN)){
                do {
                    Token paramName = require(IDENTIFIER, functionDefinitionStatement);
                    TypeLiteral typeLiteral = null;
                    if(tokens.matchAndConsume(COLON)){
                        typeLiteral = parseTypeLiteral();
                    }
                    functionDefinitionStatement.addParameter(paramName.getStringValue(), typeLiteral);
                } while (tokens.matchAndConsume(COMMA));
            }
            require(RIGHT_PAREN, functionDefinitionStatement);
            TypeLiteral typeLiteral = null;
            if(tokens.matchAndConsume(COLON)){
                typeLiteral = parseTypeLiteral();
            }
            functionDefinitionStatement.setType(typeLiteral);
            currentFunctionDefinition = functionDefinitionStatement;

            require(LEFT_BRACE, functionDefinitionStatement);
            LinkedList<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && tokens.hasMoreTokens()){
                statements.add(parseStatement());
            }

            require(RIGHT_BRACE, functionDefinitionStatement);
            functionDefinitionStatement.setBody(statements);
            return functionDefinitionStatement;
        }
        else{
            return null;
        }
    }

    private Statement parseStatement(){
        try{
            Statement statement = parsePrintStatement();
            if(statement != null){
                return statement;
            }
            statement = parseForStatement();
            if(statement != null){
                return statement;
            }
            statement = parseIfStatement();
            if(statement != null){
                return statement;
            }
            statement = parseVarStatement();
            if(statement != null){
                return statement;
            }
            statement = parseAssignmentOrFunctionCallStatement();
            if(statement != null){
                return statement;
            }
            if(currentFunctionDefinition != null){
                statement = parseReturnStatement();
                if(statement != null){
                    return statement;
                }
            }
            return new SyntaxErrorStatement(tokens.consumeToken());
        } catch (UnknownExpressionParseException e){
            SyntaxErrorStatement syntaxErrorStatement = new SyntaxErrorStatement(tokens.consumeToken());
            while(tokens.hasMoreTokens()){
                if(tokens.match(VAR,FOR,IF,ELSE,PRINT)){
                    break;
                } else{
                    tokens.consumeToken();
                }
            }
            return syntaxErrorStatement;
        }
    }

    private Statement parseReturnStatement(){
        if(tokens.matchAndConsume(RETURN)){
            ReturnStatement returnStatement = new ReturnStatement();
            returnStatement.setFunctionDefinition(currentFunctionDefinition);
            if(!tokens.match(RIGHT_BRACE)){
                Expression expression = parseExpression();
                returnStatement.setExpression(expression);
            }
            return returnStatement;
        }
        return null;
    }

    private Statement parseForStatement(){
        if(tokens.match(FOR)){
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);
            forStatement.setVariableName(tokens.consumeToken().getStringValue());
            require(IN, forStatement);
            forStatement.setExpression(parseExpression());
            require(RIGHT_PAREN, forStatement);
            require(LEFT_BRACE, forStatement);
            LinkedList<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)){
                Statement statement = parseStatement();
                statements.add(statement);
            }
            forStatement.setBody(statements);
            forStatement.setEnd(require(RIGHT_BRACE, forStatement));
            return forStatement;
        } else{
            return null;
        }
    }

    private Statement parseIfStatement(){
        if(tokens.match(IF)){
            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, ifStatement);
            ifStatement.setExpression(parseExpression());
            require(RIGHT_PAREN, ifStatement);
            require(LEFT_BRACE, ifStatement);
            LinkedList<Statement> ifStatements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)){
                Statement statement = parseStatement();
                ifStatements.add(statement);
            }
            ifStatement.setTrueStatements(ifStatements);
            ifStatement.setEnd(require(RIGHT_BRACE, ifStatement));
            if(tokens.match(ELSE)){
                tokens.consumeToken();
                if(tokens.match(IF)){
                    parseIfStatement();
                } else{
                    require(LEFT_BRACE, ifStatement);
                    LinkedList<Statement> elseStatements = new LinkedList<>();
                    while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)){
                        Statement statement = parseStatement();
                        elseStatements.add(statement);
                    }
                    ifStatement.setElseStatements(elseStatements);
                    ifStatement.setEnd(require(RIGHT_BRACE, ifStatement));
                }
            }
            return ifStatement;
        } else{
            return null;
        }
    }

    private Statement parseVarStatement(){
        if(tokens.match(VAR)){
            VariableStatement variableStatement = new VariableStatement();
            variableStatement.setStart(tokens.consumeToken());
            variableStatement.setVariableName(tokens.consumeToken().getStringValue());
            if(tokens.match(COLON)){
                require(COLON, variableStatement);
                variableStatement.setExplicitType(parseTypeLiteral().getType());
            }
            require(EQUAL, variableStatement);
            variableStatement.setExpression(parseExpression());
            variableStatement.setEnd(variableStatement.getExpression().getEnd());
            return variableStatement;
        } else{
            return null;
        }
    }

    private Statement parseAssignmentOrFunctionCallStatement(){
        if(tokens.match(IDENTIFIER)){
            AssignmentStatement assignmentStatement = new AssignmentStatement();
            assignmentStatement.setStart(tokens.consumeToken());
            assignmentStatement.setVariableName(assignmentStatement.getStart().getStringValue());
            if(!tokens.match(EQUAL)){
                return parseFunctionCallStatement();
            }
            require(EQUAL, assignmentStatement);
            assignmentStatement.setExpression(parseExpression());
            assignmentStatement.setEnd(assignmentStatement.getExpression().getEnd());
            return assignmentStatement;
        } else{
            return null;
        }
    }

    private Statement parseFunctionCallStatement(){
        Token function = tokens.lastToken();
        if(tokens.match(LEFT_PAREN)){
            tokens.consumeToken();
            ArrayList<Expression> expressions = new ArrayList<>();
            if(!tokens.match(RIGHT_PAREN)){
                expressions.add(parseExpression());
            }
            while(tokens.match(COMMA)){
                tokens.consumeToken();
                expressions.add(parseExpression());
            }
            FunctionCallStatement functionCallStatement = new FunctionCallStatement(new FunctionCallExpression(function.getStringValue(), expressions));
            functionCallStatement.setStart(function);
            require(RIGHT_PAREN, functionCallStatement);
            functionCallStatement.setEnd(tokens.lastToken());
            return functionCallStatement;
        } else{
            return null;
        }
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }

    //============================================================
    //  Expressions
    //============================================================
    private TypeLiteral parseTypeLiteral(){
        if(tokens.match("int")){
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.INT);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        } else if(tokens.match("string")){
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.STRING);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        } else if(tokens.match("bool")){
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.BOOLEAN);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        } else if(tokens.match("object")){
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.OBJECT);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        } else if(tokens.match("list")){
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
            typeLiteral.setToken(tokens.consumeToken());
            if(tokens.matchAndConsume(LESS)){
                TypeLiteral component = parseTypeLiteral();
                typeLiteral.setType(CatscriptType.getListType(component.getType()));
                require(GREATER, typeLiteral);
            }
            return typeLiteral;
        }
        TypeLiteral typeLiteral = new TypeLiteral();
        typeLiteral.setType(CatscriptType.OBJECT);
        typeLiteral.setToken(tokens.consumeToken());
        typeLiteral.addError(ErrorType.BAD_TYPE_NAME);
        return typeLiteral;
    }

    private Expression parseExpression() {
        return parseEqualityExpression();
    }

    private Expression parseEqualityExpression(){
        Expression expression = parseComparisonExpression();
        while(tokens.match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }

    private Expression parseComparisonExpression(){
        Expression expression = parseAdditiveExpression();
        if(tokens.match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHandSide);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(rightHandSide.getEnd());
            expression = comparisonExpression;
        }
        return expression;
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;
    }

    private Expression parseFactorExpression(){
        Expression expression = parseUnaryExpression();
        while(tokens.match(SLASH, STAR)){
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression();
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        return expression;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rightHandSide);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rightHandSide.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(IDENTIFIER)) {
            Token identifierToken = tokens.consumeToken();
            IdentifierExpression identifierExpression = new IdentifierExpression(identifierToken.getStringValue());
            identifierExpression.setToken(identifierToken);
            List<Expression> expressions = new ArrayList<>();
            if (tokens.match(LEFT_PAREN)) {
                tokens.consumeToken();
                if(!tokens.match(RIGHT_PAREN)) {
                    expressions.add(parseExpression());
                }
                while (tokens.match(COMMA)) {
                    tokens.consumeToken();
                    expressions.add(parseExpression());
                }
                FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierToken.getStringValue(), expressions);
                if (tokens.match(RIGHT_PAREN)) {
                    tokens.consumeToken();
                } else {
                    functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                }
                return functionCallExpression;
            }
            return identifierExpression;
        }
        if (tokens.match(STRING)) {
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringLiteralExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringLiteralExpression.setToken(stringToken);
            return stringLiteralExpression;
        } else if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerLiteralExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerLiteralExpression.setToken(integerToken);
            return integerLiteralExpression;
        } else if (tokens.match(TRUE, FALSE)) {
            Token booleanToken = tokens.consumeToken();
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(Boolean.valueOf((booleanToken.getStringValue())));
            booleanLiteralExpression.setToken(booleanToken);
            return booleanLiteralExpression;
        } else if (tokens.match(NULL)) {
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            nullLiteralExpression.setToken(nullToken);
            return nullLiteralExpression;
        } else if (tokens.match(LEFT_PAREN)) {
            tokens.consumeToken();
            ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(parseExpression());
            if (tokens.match(RIGHT_PAREN)) {
                tokens.consumeToken();
                return parenthesizedExpression;
            } else {
                SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
                return syntaxErrorExpression;
            }
        } else if (tokens.match(LEFT_BRACKET)) {
            List<Expression> listVals = new ArrayList<>();
            tokens.consumeToken();
            if(!tokens.match(RIGHT_BRACKET)) {
                listVals.add(parseExpression());
            }
            while (tokens.match(COMMA)) {
                tokens.consumeToken();
                listVals.add(parseExpression());
            }
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(listVals);
            if (tokens.match(RIGHT_BRACKET)) {
                tokens.consumeToken();
            } else {
                listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
            }
            return listLiteralExpression;
        } else {
            throw new UnknownExpressionParseException();
        }
    }

    class UnknownExpressionParseException extends RuntimeException{

    }

    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
