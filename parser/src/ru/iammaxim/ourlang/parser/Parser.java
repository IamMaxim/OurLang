package ru.iammaxim.ourlang.Parser;

import ru.iammaxim.ourlang.Logger;
import ru.iammaxim.ourlang.Parser.expression.*;
import ru.iammaxim.ourlang.Parser.type.Type;
import ru.iammaxim.ourlang.Parser.type.TypeInt;
import ru.iammaxim.ourlang.Parser.type.TypeVoid;
import ru.iammaxim.ourlang.Parser.value.Value;

import java.util.ArrayList;

/**
 * Created by maxim on 2/12/17 at 3:03 PM.
 */
public class Parser {
    private Tokener tokener;
    private Logger logger;

    public Parser(Logger logger, ArrayList<Token> tokens) {
        this.logger = logger;
        tokener = new Tokener(tokens);
    }

    private void assertToken(Tokener tokener, Token token) throws InvalidTokenException {
        if (!tokener.peek().eq(token))
            throw new InvalidTokenException("Expected " + token.token);
    }

    private void assertType(Tokener tokener, TokenType type) throws InvalidTokenException {
        if (tokener.peek().type != type)
            throw new InvalidTokenException("Expected " + type);
    }

    private Expression indentAndParseExpression(Tokener tokener) throws InvalidTokenException {
        logger.increateIndent();
        Expression exp = parseExpression(tokener);
        logger.decreaseIndent();
        return exp;
    }

    private ArrayList<Expression> indentAndParseExpressions(ArrayList<Tokener> tokeners) throws InvalidTokenException {
        logger.increateIndent();
        ArrayList<Expression> exps = parseExpressions(tokeners);
        logger.decreaseIndent();
        return exps;
    }

    private Expression parseExpression(Tokener tokener) throws InvalidTokenException {
        logger.log("parsing " + tokener + " with line number: " + tokener.currentLineNumber);
        tokener.trimParentheses();

        // check if this is value (reference is value too)
        if (tokener.size() == 1) {
            logger.log("parsing value");
            Value val = new Value(tokener.eat().token);
            return new ExpressionValue(val);
        }

        // check if this is return
        if (tokener.size() >= 2) {
            if (tokener.peek().eq("return")) {
                tokener.eat(); // eat return
                logger.log("parsing return");
                return new ExpressionReturn(indentAndParseExpression(tokener.subtokener(1, tokener.size())));
            }
        }

        // check if this is variable declaration
        if (tokener.size() >= 4) {
            if (tokener.peek().eq("var")) {
                tokener.eat(); // eat var
                logger.log("parsing variable declaration");
                String varName = eat(tokener, TokenType.IDENTIFIER, "Expected variable name").token;
                if (!tokener.eat().eq(":"))
                    throw new InvalidTokenException("Expected ':' after variable name");
                Type varType = getTypeFor(eat(tokener, TokenType.IDENTIFIER, "Expected variable type").token);
                return new ExpressionVarDeclaration(varType, varName);
            }
        }

        // check if this is condition
        if (tokener.hasNext() && tokener.peek().eq("if")) {
            tokener.eat(); // eat if
            assertType(tokener, TokenType.SCOPE_PARENS);
            Tokener condition = new Tokener(((TokenScope) tokener.eat()).tokens, tokener.preEatenLineNumber);

            logger.log("parsed condition: " + condition);

            ArrayList<Expression> bodyExps = new ArrayList<>();
            ArrayList<Tokener> bodyTokeners = readBody(tokener);

            for (Tokener t : bodyTokeners)
                bodyExps.add(indentAndParseExpression(t));

            logger.log("parsed body: " + bodyExps);

            ArrayList<Expression> elseBodyExps = new ArrayList<>();
            if (tokener.left() >= 2 /* else, braces scope/single statement */ && tokener.peek().eq("else")) {
                tokener.eat(); // eat else

                ArrayList<Tokener> elseBodyTokeners = readBody(tokener);

                logger.log("parsed elseBody: " + elseBodyTokeners);

                for (Tokener t : elseBodyTokeners)
                    elseBodyExps.add(indentAndParseExpression(t));

                // check situation when semicolon is not set after if () {} else {}
                Tokener nextExpr = tokener.readTo(new Token(";"));
                if (!nextExpr.isEmpty())
                    throw new InvalidTokenException("Expected ';' after 'if () {} else {}'");
            } else {
                // check situation when semicolon is not set after if () {}
                Tokener nextExpr = tokener.readTo(new Token(";"));
                if (!nextExpr.isEmpty())
                    throw new InvalidTokenException("Expected ';' after 'if () {}'");
            }

            return new ExpressionCondition(
                    indentAndParseExpression(condition),
                    bodyExps,
                    elseBodyExps);
        }

        // check if this is for loop
        if (tokener.left() > 0 && tokener.peek().eq("for")) {
            logger.log("parsing for loop");
            tokener.eat(); //eat 'for'
            assertType(tokener, TokenType.SCOPE_PARENS);
            Tokener args = new Tokener(((TokenScope) tokener.eat()).tokens, tokener.preEatenLineNumber);
            ArrayList<Tokener> argTokens = args.splitSkippingScopes(new Token(";"));
            logger.log("for loop args: " + argTokens);
            if (argTokens.size() != 3)
                throw new InvalidTokenException("Expected 3 args in 'for' construction, but got " + argTokens.size());
            ArrayList<Tokener> body = readBody(tokener);

            // check situation when semicolon is not set after for(){}
            Tokener nextExpr = tokener.readTo(new Token(";"));
            if (!nextExpr.isEmpty())
                throw new InvalidTokenException("Expected ';' after 'for () {}'");

            return new ExpressionForLoop(
                    indentAndParseExpression(argTokens.get(0)),
                    indentAndParseExpression(argTokens.get(1)),
                    indentAndParseExpression(argTokens.get(2)),
                    indentAndParseExpressions(body));
        }

        int level = 0;
        Token t, highest = null;
        int highestPriorityIndex = -1;
        for (int i = 0; tokener.left() > 0; i++) {
            t = tokener.eat();
            if (t.eq("(")) {
                level++;
            } else if (t.eq(")")) {
                level--;
            }
            if (level == 0) {
                if (t.type == TokenType.OPERATOR) {
                    if (highest == null) {
                        highest = t;
                        highestPriorityIndex = i;
                    } else {
                        if (isOrderHigher(highest, t)) {
                            highest = t;
                            highestPriorityIndex = i;
                        }
                    }
                }
            }
        }

        if (highest == null) {
            // check if this is function call
            tokener.index = 0;
            while (tokener.left() > 0) {
                t = tokener.eat();
                if (t.type == TokenType.IDENTIFIER && tokener.hasNext() && tokener.peekNext().type == TokenType.SCOPE_PARENS) {
                    logger.log("parsing function call");
                    // check if this is function declaration
                    if (tokener.left() >= 2) {
                        if (tokener.peekNextNext().type == TokenType.SCOPE_BRACES) {
                            throw new InvalidTokenException("Line " + tokener.preEatenLineNumber + ": Unexpected function declaration");
                        }
                    }
                    int index = tokener.index;
                    Tokener argsTokener = new Tokener(((TokenScope) tokener.peekNext()).tokens, tokener.prePeekedLineNumber);
                    ArrayList<Tokener> args = argsTokener.splitSkippingScopes(new Token(","));
                    tokener.index = index;

                    return new ExpressionFunctionCall(
                            t,
                            indentAndParseExpressions(args));
                }

                if (t.type == TokenType.IDENTIFIER && tokener.hasNext() && tokener.peekNext().type == TokenType.SCOPE_BRACKETS) {
                    logger.log("parsing value at");
                    Tokener argTokener = new Tokener(((TokenScope) tokener.peekNext()).tokens, tokener.prePeekedLineNumber);

                    ArrayList<Tokener> list = new ArrayList<>();
                    list.add(argTokener);
                    return new ExpressionValueAt(t.token,
                            (ExpressionValue) indentAndParseExpressions(list).get(0));
                }
            }

            if (tokener.isEmpty())
                return new ExpressionTree();
            return new ExpressionTree(tokener.tokens.get(0));
        }

        return new ExpressionTree(highest,
                indentAndParseExpression(tokener.subtokener(0, highestPriorityIndex)),
                indentAndParseExpression(tokener.subtokener(highestPriorityIndex + 1, tokener.size())));
    }

    private ArrayList<Tokener> readBody(Tokener tokener) throws InvalidTokenException {
        Tokener body;
        ArrayList<Tokener> BodyTokeners;
        if (tokener.peek().type != TokenType.SCOPE_BRACES) { // no braces; read 1 statement
            body = tokener.readTo(new Token(";"));
            BodyTokeners = new ArrayList<>();
            BodyTokeners.add(body);
        } else {
            body = new Tokener(((TokenScope) tokener.eat()).tokens, tokener.preEatenLineNumber);
            BodyTokeners = body.splitSkippingScopes(new Token(";"));
        }
        return BodyTokeners;
    }

    /**
     * @param first  first token
     * @param second second token
     * @return true if second's order is higher than first's
     * @throws InvalidTokenException if one of operators unknown
     */
    private boolean isOrderHigher(Token first, Token second) throws InvalidTokenException {
        return getOrder(first) < getOrder(second);
    }

    /**
     * @param t token to check
     * @return operator's order
     * @throws InvalidTokenException if operator unknown
     */
    private int getOrder(Token t) throws InvalidTokenException {
        int level;
        switch (t.token) {
            case "*":
            case "/":
                level = 1;
                break;
            case "+":
            case "-":
                level = 2;
                break;
            case "++":
            case "--":
                level = 3;
                break;
            case "==":
                level = 4;
                break;
            case "=":
            case "+=":
            case "-=":
                level = 5;
                break;
            default:
                throw new InvalidTokenException("Expected operator, but got " + t.token);
        }

        return level;
    }

    private Token eat() {
        return tokener.eat();
    }

    public static Type getTypeFor(String token) throws InvalidTokenException {
        // TODO: generify type resolve
        switch (token) {
            case "void":
                return new TypeVoid();
            case "int":
                return new TypeInt();
            default:
                throw new InvalidTokenException("No type '" + token + "' found");
        }
    }

    private Token eat(Tokener tokener, TokenType type, String onError) throws InvalidTokenException {
        Token t = tokener.eat();
        if (t.type != type)
            throw new InvalidTokenException("Line " + tokener.preEatenLineNumber + ": " + onError);
        return t;
    }

    public ArrayList<ParsedFunction> parse() throws InvalidTokenException {
        ArrayList<ParsedFunction> functions = new ArrayList<>();
        while (tokener.left() > 0) { // while there are expressions left
            Token token = eat();

            // check function declaration
            if (token.eq("function")) {
                Type returnType = new TypeVoid();
                String functionName;
                ArrayList<Variable> args = new ArrayList<>();
                ArrayList<Expression> exps;
                ArrayList<Variable> localVars = new ArrayList<>();

                // read function name
                functionName = eat(tokener, TokenType.IDENTIFIER, "Expected identifier as function name").token;

                logger.log("parsing function " + functionName);

                // read function args
                if (tokener.peek().type != TokenType.SCOPE_PARENS)
                    throw new InvalidTokenException("Expected parens scope as arguments");
                TokenScope argsToken = (TokenScope) tokener.eat();
                ArrayList<Tokener> argsTokeners = new Tokener(argsToken.tokens, tokener.preEatenLineNumber).splitSkippingScopes(new Token(","));

                logger.log("parsed function args: " + argsTokeners);

                for (int i = 0; i < argsTokeners.size(); i++) {
                    Tokener argTokener1 = argsTokeners.get(i);
                    if (argTokener1.size() != 2)
                        throw new InvalidTokenException("Expected <Type> <arg_name>, got while parsing argument");

                    args.add(new Variable(
                            getTypeFor(argTokener1.tokens.get(0).token),
                            argTokener1.tokens.get(1).token));
                }

                // check if return type is non-void
                if (tokener.peek().eq(":")) {
                    if (tokener.peekNextNext().type != TokenType.IDENTIFIER)
                        throw new InvalidTokenException("Expected return type after ':'");
                    tokener.eat(); // eat ':'
                    returnType = getTypeFor(tokener.eat().token);
                }

                // read function body
                Tokener body = new Tokener(
                        ((TokenScope) eat(tokener, TokenType.SCOPE_BRACES, "Expected braces scope as function body")).tokens,
                        tokener.preEatenLineNumber); // TODO: check if this is needed to be changed to currentLineNumber

                logger.log("parsed function body: " + body);

                // process body
                exps = indentAndParseExpressions(body.splitSkippingScopes(new Token(";")));

                logger.log("parsed expressions:");
                logger.increateIndent();
                exps.forEach(e -> logger.log(e.toString()));
                logger.decreaseIndent();
                logger.log("parsed expressions end");


                // find all local variables and cut their declaration from code;
                // they will be stored in the activation record of a function
                for (int i = exps.size() - 1; i >= 0; i--) {
                    if (exps.get(i) instanceof ExpressionVarDeclaration) {
                        ExpressionVarDeclaration exp = (ExpressionVarDeclaration) exps.get(i);
                        localVars.add(new Variable(exp.type, exp.name));
                        exps.remove(i);
                    }
                }

                // parse function
                functions.add(new ParsedFunction(returnType, functionName, args, exps, localVars));
                continue;
            }

            // TODO: more root declarations here

            if (token.eq(";")) // skip processing of ";"
                continue;

            throw new InvalidTokenException("Couldn't find parsing block for " + token);
        }
        return functions;
    }

    private ArrayList<Expression> parseExpressions(ArrayList<Tokener> tokeners) throws InvalidTokenException {
        ArrayList<Expression> exps = new ArrayList<>(tokeners.size());
        for (Tokener t : tokeners) {
            exps.add(parseExpression(t));
        }
        return exps;
    }
}
