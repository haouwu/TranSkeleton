import AST.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private TranNode tran;
    private final TokenManager tokens;

    public Parser(TranNode top, List<Token> Tokens) {
        tokens = new TokenManager(Tokens);
        tran = top;}

    public void RequireNewLine() throws SyntaxErrorException{
        if(!tokens.done()){
            Optional<Token> newLineCheck = tokens.matchAndRemove(Token.TokenTypes.NEWLINE);
            while(newLineCheck.isPresent()){
                newLineCheck = tokens.matchAndRemove(Token.TokenTypes.NEWLINE);
            }
        }
    }

    private Optional<InterfaceNode> interfaceNode() throws SyntaxErrorException {
        Optional<InterfaceNode> interface1 = Optional.of(new InterfaceNode());

        Optional<Token> nameCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(nameCheck.isEmpty()){
            throw new SyntaxErrorException("interface requires a name", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
        interface1.get().name = nameCheck.get().getValue();
        RequireNewLine();
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("interface requires a indent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
        Optional<MethodHeaderNode> methodCheck = methodHeaderNode();
        while(methodCheck.isPresent()) {
            interface1.get().methods.add(methodCheck.get());
            methodCheck = methodHeaderNode();
        }
        if(tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()){
            throw new SyntaxErrorException("interface requires a dedent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
        }
        RequireNewLine();
        return interface1;
    }

    public Optional<ClassNode> classNode() throws SyntaxErrorException {
        Optional<ClassNode> class1 = Optional.of(new ClassNode());
        Optional<Token> nameCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(nameCheck.isEmpty()){
            throw new SyntaxErrorException("class requires a name", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
        }
        class1.get().name = nameCheck.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.IMPLEMENTS).isPresent()){
            Optional<Token> interfaceCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
            if(interfaceCheck.isEmpty()){
                throw new SyntaxErrorException("implementation requires a name", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
            class1.get().interfaces.add(interfaceCheck.get().getValue());
            if(tokens.matchAndRemove(Token.TokenTypes.COMMA).isPresent()){
                do {
                    interfaceCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
                    class1.get().interfaces.add(interfaceCheck.get().getValue());
                }while((tokens.matchAndRemove(Token.TokenTypes.COMMA).isPresent()));
            }
        }
        RequireNewLine();
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("class requires a indent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
        }
         while(!tokens.done() && tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()){
            Optional<ConstructorNode> constructorCheck = constructorNode();
            Optional<MemberNode> memberCheck = memberNode();
            Optional<MethodDeclarationNode> methodCheck = methodNode();

            if (constructorCheck.isPresent()) {
                while (constructorCheck.isPresent()) {
                    class1.get().constructors.add(constructorCheck.get());
                    constructorCheck = constructorNode();
                    if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                        throw new SyntaxErrorException("constructor requires a dedent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
                    }
                }
            } else if (memberCheck.isPresent()) {
                memberCheck.ifPresent(memberNode -> class1.get().members.add(memberNode));
                RequireNewLine();
                while ((memberCheck = memberNode()).isPresent()) {
                    class1.get().members.add(memberCheck.get());
                    if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                        RequireNewLine();
                    } else {
                        return class1;
                    }
                }
            }else if(methodCheck.isPresent()){
                class1.get().methods.add(methodCheck.get());
            }
        }
        return class1;
    }

    private Optional<ConstructorNode> constructorNode() throws SyntaxErrorException {
        Optional<ConstructorNode> constructor =Optional.of(new ConstructorNode());
        if(tokens.matchAndRemove(Token.TokenTypes.CONSTRUCT).isEmpty()){
            return Optional.empty();}
        if(tokens.matchAndRemove(Token.TokenTypes.LPAREN).isEmpty()){
            throw new SyntaxErrorException("constructor requires a left paren", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
        Optional<VariableDeclarationNode> variableCheck = VariableDeclarationNode();
        variableCheck.ifPresent(variableDeclarationNode -> constructor.get().parameters.add(variableDeclarationNode));
        variableCheck = VariableDeclarationNodes();
        while(variableCheck.isPresent()) {
            constructor.get().parameters.add(variableCheck.get());
            variableCheck = VariableDeclarationNodes();}
        if(tokens.matchAndRemove(Token.TokenTypes.RPAREN).isEmpty()){
            throw new SyntaxErrorException("constructor requires a right paren", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
        RequireNewLine();
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("constructor requires a indent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
        variableCheck.ifPresent(variableDeclarationNode -> constructor.get().parameters.add(variableDeclarationNode));
        variableCheck = VariableDeclarationNodes();
        while(variableCheck.isPresent()) {
            constructor.get().parameters.add(variableCheck.get());
            variableCheck = VariableDeclarationNodes();}
        Optional<StatementNode> statementCheck = statementNode();
        if(statementCheck.isPresent()) {
            constructor.get().statements.add(statementCheck.get());
            if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                throw new SyntaxErrorException("constructor requires a dedent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
            }
        }
        return constructor;
    }

    private Optional<MethodDeclarationNode> methodNode() throws SyntaxErrorException {
        Optional<MethodDeclarationNode> method = Optional.of(new MethodDeclarationNode());
        if (tokens.matchAndRemove(Token.TokenTypes.PRIVATE).isPresent()) {
            method.get().isPrivate = true;
        }
        if (tokens.matchAndRemove(Token.TokenTypes.SHARED).isPresent()) {
            method.get().isShared = true;
        }
        Optional<MethodHeaderNode> MethodHeaderCheck = methodHeaderNode();
        if (MethodHeaderCheck.isEmpty()) {
            return Optional.empty();
        }
        if (MethodHeaderCheck.isPresent()) {
            method.get().parameters = MethodHeaderCheck.get().parameters;
            method.get().returns =  MethodHeaderCheck.get().returns;
            method.get().name = MethodHeaderCheck.get().name;
        }
        if (tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()) {
            throw new SyntaxErrorException("method requires a indent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
        }
        Optional<VariableDeclarationNode> variableCheck = VariableDeclarationNode();
        if (variableCheck.isPresent()) {
            while (variableCheck.isPresent()) {
                method.get().locals.add(variableCheck.get());
                RequireNewLine();
                variableCheck = VariableDeclarationNode();
            }
        }
        Optional<StatementNode> statementCheck = statementNode();
        if(statementCheck.isPresent()) {
            while(statementCheck.isPresent()) {
                method.get().statements.add(statementCheck.get());
                RequireNewLine();
                statementCheck = statementNode();
            }
        }
        if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
            throw new SyntaxErrorException("method requires a dedent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
        }
        RequireNewLine();
        return method;
    }

    private Optional<MemberNode> memberNode() throws SyntaxErrorException {
        Optional<MemberNode> member = Optional.of(new MemberNode());
        Optional<VariableDeclarationNode> variableCheck = VariableDeclarationNode();
        if(variableCheck.isEmpty()) {
            return Optional.empty();}
        member.get().declaration = variableCheck.get();
        Optional<Token> accessCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(accessCheck.isPresent() && accessCheck.get().getValue().equals("accessor")){
            if(tokens.matchAndRemove(Token.TokenTypes.COLON).isEmpty()){
                throw new SyntaxErrorException("accessor needs a colon", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
            }
            Optional<StatementNode> statementCheck = statementNodes();
            if(statementCheck.isPresent()) {
                member.get().accessor.get().add(statementCheck.get());
                RequireNewLine();
                if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                    throw new SyntaxErrorException("method requires a dedent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
                }
            }
        }
        if(accessCheck.isPresent() && accessCheck.get().getValue().equals("mutator")){
            if(tokens.matchAndRemove(Token.TokenTypes.COLON).isEmpty()){
                throw new SyntaxErrorException("accessor needs a colon", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
            }
            Optional<StatementNode> statementCheck = statementNodes();
            if(statementCheck.isPresent()) {
                member.get().mutator.get().add(statementCheck.get());
                RequireNewLine();
                if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                    throw new SyntaxErrorException("method requires a dedent", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
                }
            }
        }
        return member;
    }

    private Optional<StatementNode> statementNode() throws SyntaxErrorException {
        if(tokens.matchAndRemove(Token.TokenTypes.IF).isPresent()){
           return ifNode();
        }
        if(tokens.matchAndRemove(Token.TokenTypes.LOOP).isPresent()){
            return loopNode();
        }
        Optional<StatementNode> disambiguate = disambiguate();
        if(disambiguate.isPresent()){
            return disambiguate;
        }
        return Optional.empty();
    }

    private Optional<StatementNode> statementNodes() throws SyntaxErrorException{
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isPresent()){
            return statementNode();
        }
        return Optional.empty();
    }


    private Optional<StatementNode> ifNode() throws SyntaxErrorException {
        Optional<IfNode> ifNode = Optional.of(new IfNode());
        Optional<BooleanOpNode> conditionCheck = booleanOpNode();
        ifNode.get().statements = new ArrayList<>();
        ifNode.get().elseStatement = Optional.empty();
        if(conditionCheck.isEmpty()){
            throw new SyntaxErrorException("if needs a condition", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
        ifNode.get().condition = conditionCheck.get();
        RequireNewLine();
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isPresent()){
            Optional<StatementNode> statementCheck = statementNode();
            if(statementCheck.isPresent()){
                ifNode.get().statements.add(statementCheck.get());
                statementCheck = statementNodes();
                RequireNewLine();
                while(statementCheck.isPresent()){
                    ifNode.get().statements.add(statementCheck.get());
                    statementCheck = statementNodes();
                    RequireNewLine();
                }
            }
        }
        /*
        Optional<Token> elseNode = tokens.matchAndRemove(Token.TokenTypes.ELSE);
        elseNode.ifPresent(token -> ifNode.get().statements.add((StatementNode) token));
        return Optional.of(new IfNode());
         */
        return Optional.of(ifNode.get());
    }
    /*
    private Optional<ElseNode> elseNode() throws SyntaxErrorException {
        Optional<ElseNode> elseNode = Optional.of(new ElseNode());
        if(tokens.matchAndRemove(Token.TokenTypes.ELSE).isEmpty()){
            throw new SyntaxErrorException("else requires a else",0,0);
        }
        return elseNode;
    }
    */
    private Optional<StatementNode> loopNode() throws SyntaxErrorException {
        return Optional.of(new LoopNode());
    }


    private Optional<AssignmentNode> assignmentNode() throws SyntaxErrorException{
        Optional<AssignmentNode> assignment = Optional.of(new AssignmentNode());
        if(tokens.matchAndRemove(Token.TokenTypes.ASSIGN).isEmpty()){
            return Optional.empty();}
        Optional<ExpressionNode> expressionCheck = expressionNode();
        if(expressionCheck.isEmpty()) {
            throw new SyntaxErrorException("assignment requires an expression", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
        assignment.get().expression = expressionCheck.get();
        return assignment;
    }
    private Optional<ExpressionNode> expressionNode() throws SyntaxErrorException {
        Optional<VariableReferenceNode> variableCheck = VariableReferenceNode();
        if(variableCheck.isPresent()) {
            return Optional.of(variableCheck.get());
        }
        return Optional.empty();
    }

    private Optional<BooleanOpNode> booleanOpNode() throws SyntaxErrorException {
        Optional<BooleanOpNode> BooleanOp = Optional.of(new BooleanOpNode());
        Optional<ExpressionNode> compareCheck = compareNode();
        if(compareCheck.isPresent()) {
            BooleanOp.get().left = compareCheck.get();
            if (tokens.matchAndRemove(Token.TokenTypes.AND).isPresent()) {
                BooleanOp.get().op = BooleanOpNode.BooleanOperations.and;
            }else if (tokens.matchAndRemove(Token.TokenTypes.OR).isPresent()) {
                BooleanOp.get().op = BooleanOpNode.BooleanOperations.or;
            }
            compareCheck = compareNode();
            if (compareCheck.isPresent()) {
                BooleanOp.get().right = compareCheck.get();
                Optional<BooleanOpNode> newBooleanOp = booleanOpNode();
                if (newBooleanOp.isPresent()) {
                    newBooleanOp.get().left = BooleanOp.get();
                }
                return Optional.of(newBooleanOp.get());
            }
        }else if(tokens.matchAndRemove(Token.TokenTypes.AND).isPresent()) {
            BooleanOp.get().op = BooleanOpNode.BooleanOperations.and;
            compareCheck = compareNode();
            if (compareCheck.isPresent()) {
                BooleanOp.get().right = compareCheck.get();}
        }else if(tokens.matchAndRemove(Token.TokenTypes.OR).isPresent()) {
            BooleanOp.get().op = BooleanOpNode.BooleanOperations.or;
            compareCheck = compareNode();
            if (compareCheck.isPresent()) {
                BooleanOp.get().right = compareCheck.get();}
        }else if(tokens.matchAndRemove(Token.TokenTypes.NOT).isPresent()){
            Optional<BooleanOpNode> newBooleanOp = booleanOpNode();
            if(newBooleanOp.isPresent()) {
                BooleanOp.get().left = newBooleanOp.get();
            }
        }
        return Optional.of(BooleanOp.get());
    }

    private Optional<ExpressionNode> compareNode() throws SyntaxErrorException {
        Optional<CompareNode> compare = Optional.of(new CompareNode());
        Optional<MethodCallExpressionNode> methodCallCheck = MethodCallExpressionNode();
        Optional<ExpressionNode> expressionCheck = expressionNode();
        Optional<VariableReferenceNode> variableCheck = VariableReferenceNode();

        if (methodCallCheck.isPresent()) {
            return Optional.of(compare.get());
        } else if (expressionCheck.isPresent()) {
            if (expressionCheck.isPresent()) {
                compare.get().left = expressionCheck.get();
                if (tokens.matchAndRemove(Token.TokenTypes.LESSTHAN).isPresent()) {
                    compare.get().op = CompareNode.CompareOperations.lt;
                } else if (tokens.matchAndRemove(Token.TokenTypes.LESSTHANEQUAL).isPresent()) {
                    compare.get().op = CompareNode.CompareOperations.le;
                } else if (tokens.matchAndRemove(Token.TokenTypes.GREATERTHAN).isPresent()) {
                    compare.get().op = CompareNode.CompareOperations.gt;
                } else if (tokens.matchAndRemove(Token.TokenTypes.GREATERTHANEQUAL).isPresent()) {
                    compare.get().op = CompareNode.CompareOperations.ge;
                } else if (tokens.matchAndRemove(Token.TokenTypes.EQUAL).isPresent()) {
                    compare.get().op = CompareNode.CompareOperations.eq;
                } else if (tokens.matchAndRemove(Token.TokenTypes.NOTEQUAL).isPresent()) {
                    compare.get().op = CompareNode.CompareOperations.ne;
                } else {
                    throw new SyntaxErrorException("unexpected token", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
                }
            }
            expressionCheck = expressionNode();
            compare.get().right = expressionCheck.get();
            return Optional.of(compare.get());
        } else if (variableCheck.isPresent()) {
          compare.get().left = variableCheck.get();
            return Optional.of(compare.get());
        }else{
            return Optional.empty();
        }
        // call expressionNode() here and check the result

    }


    private Optional<MethodCallExpressionNode> MethodCallExpressionNode() throws SyntaxErrorException {
        return Optional.empty();
        /*
        Optional<MethodCallExpressionNode> MethodCall = Optional.of(new MethodCallExpressionNode());
        tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(tokens.matchAndRemove(Token.TokenTypes.DOT).isPresent()){
            tokens.matchAndRemove(Token.TokenTypes.WORD);}
        if(tokens.matchAndRemove(Token.TokenTypes.LPAREN).isPresent()){
            throw new SyntaxErrorException("method call requires a parenthesis",0,0);}


        if(tokens.matchAndRemove(Token.TokenTypes.RPAREN).isPresent()){
            throw new SyntaxErrorException("method call requires a parenthesis",0,0);}
         */
    }

    private Optional<MethodCallStatementNode> MethodCallStatementNode() throws SyntaxErrorException {
        Optional<MethodCallStatementNode> methodCall = Optional.of(new MethodCallStatementNode());
        Optional<VariableReferenceNode> variableCheck = VariableReferenceNode();
        if(variableCheck.isPresent()){
            methodCall.get().returnValues.add(variableCheck.get());
            while(tokens.matchAndRemove(Token.TokenTypes.COMMA).isPresent()){
                variableCheck = VariableReferenceNode();
                methodCall.get().returnValues.add(variableCheck.get());
            }
        }else{
            return Optional.empty();
        }
        if(tokens.matchAndRemove(Token.TokenTypes.EQUAL).isEmpty()){
            throw new SyntaxErrorException("method requires a equal", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());
        }
        Optional<MethodCallExpressionNode> methodCallExpressionNode = MethodCallExpressionNode();
        if(methodCallExpressionNode.isPresent()){
            methodCall.get().methodName = methodCallExpressionNode.get().methodName;
        }
        return methodCall;
    }
    /*
    private Optional<Node> FactorNode() throws SyntaxErrorException {
        if(NumericLiteralNode().isPresent()){
            return Optional.of(NumericLiteralNode().get());}
        if(VariableReferenceNode().isPresent()){
            return Optional.of(VariableReferenceNode().get());}

    }

     */
    private Optional<StatementNode> disambiguate() throws SyntaxErrorException {
        Optional<MethodCallExpressionNode> methodCallCheck = MethodCallExpressionNode();
        if(methodCallCheck.isPresent()){
             Optional<MethodCallStatementNode> methodCallStatementNode = MethodCallStatementNode();
             if(methodCallStatementNode.isPresent()){
                 return Optional.of(methodCallStatementNode.get());
             }
        }else{
            Optional<VariableReferenceNode> variableCheck = VariableReferenceNode();
            if(variableCheck.isEmpty()){
                return Optional.empty();}
            if(tokens.matchAndRemove(Token.TokenTypes.COMMA).isPresent()){
                Optional<MethodCallStatementNode> methodCall = MethodCallStatementNode();
                if(methodCall.isPresent()){
                    return Optional.of(methodCall.get());
                }
            }else{
                Optional<AssignmentNode> assignmentNode = assignmentNode();
                if(assignmentNode.isPresent()){
                    assignmentNode.get().target = variableCheck.get();
                    return Optional.of(assignmentNode.get());
                }
            }
        }
        return Optional.empty();
    }

    private Optional<NumericLiteralNode> NumericLiteralNode() throws SyntaxErrorException {
        Optional<NumericLiteralNode> NumericLiteral = Optional.of(new NumericLiteralNode());
        Optional<Token> numberCheck = tokens.matchAndRemove(Token.TokenTypes.NUMBER);
        if(numberCheck.isEmpty()) {
            return Optional.empty();}
        NumericLiteral.get().value = Float.parseFloat(numberCheck.get().getValue());
        return NumericLiteral;
    }

    private Optional<VariableReferenceNode> VariableReferenceNode() throws SyntaxErrorException {
        Optional<VariableReferenceNode> VariableReference = Optional.of(new VariableReferenceNode());
        Optional<Token> nameCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(nameCheck.isEmpty()) {
            return Optional.empty();}
        VariableReference.get().name = nameCheck.get().getValue();
        return VariableReference;
    }

    private Optional<MethodHeaderNode> methodHeaderNode() throws SyntaxErrorException {
        Optional<MethodHeaderNode> method = Optional.of(new MethodHeaderNode());
        Optional<Token> PeekCheck = tokens.peek(1);
        if(PeekCheck.isEmpty() || PeekCheck.get().getType() != Token.TokenTypes.LPAREN) {
            return Optional.empty();}
        Optional<Token> wordCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(wordCheck.isEmpty()) {
            return Optional.empty();}
        method.get().name = wordCheck.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.LPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}
        Optional<VariableDeclarationNode> paramCheck = VariableDeclarationNode();
        paramCheck.ifPresent(variableDeclarationNode -> method.get().parameters.add(variableDeclarationNode));
        paramCheck = VariableDeclarationNodes();
        while(paramCheck.isPresent()) {
            method.get().parameters.add(paramCheck.get());
            paramCheck = VariableDeclarationNodes();
        }
        if(tokens.matchAndRemove(Token.TokenTypes.RPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis", tokens.getCurrentLine(), tokens.getCurrentColumnNumber());}

        if(tokens.matchAndRemove(Token.TokenTypes.COLON).isPresent()) {
            Optional<VariableDeclarationNode> returnCheck = VariableDeclarationNode();
            returnCheck.ifPresent(variableDeclarationNode -> method.get().returns.add(variableDeclarationNode));
            returnCheck = VariableDeclarationNodes();
            while(returnCheck.isPresent()) {
                method.get().returns.add(returnCheck.get());
                returnCheck = VariableDeclarationNodes();
            }
        }
        RequireNewLine();
        return method;
    }

    private Optional<VariableDeclarationNode> VariableDeclarationNode() throws SyntaxErrorException {
        Optional<VariableDeclarationNode> variable = Optional.of(new VariableDeclarationNode());
        Optional<Token> PeekCheck = tokens.peek(1);
        if(PeekCheck.isPresent() && PeekCheck.get().getType() != Token.TokenTypes.WORD ) {
            return Optional.empty();
        }
        Optional<Token> TypeCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(TypeCheck.isEmpty()) {
            return Optional.empty();
        }
        variable.get().type = TypeCheck.get().getValue();

        Optional<Token> NameCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(NameCheck.isEmpty()) {
            return Optional.empty();
        }
        variable.get().name = NameCheck.get().getValue();
        return variable;
    }

    private Optional<VariableDeclarationNode> VariableDeclarationNodes() throws SyntaxErrorException {
        if(tokens.matchAndRemove(Token.TokenTypes.COMMA).isPresent()) {
            return VariableDeclarationNode();
        }
        return Optional.empty();
    }

    // Tran = { Class | Interface }
    public void Tran() throws SyntaxErrorException {
        while(!tokens.done() && tokens.matchAndRemove(Token.TokenTypes.INTERFACE).isPresent()) {
            tran.Interfaces.add(interfaceNode().get());
        }
        if(!tokens.done() && tokens.matchAndRemove(Token.TokenTypes.CLASS).isPresent() && !tokens.done()) {
            tran.Classes.add(classNode().get());
        }
    }
}