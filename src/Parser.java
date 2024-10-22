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
            throw new SyntaxErrorException("interface requires a name",0,0);}
        interface1.get().name = nameCheck.get().getValue();
        RequireNewLine();
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("interface requires a indent",0,0);}
        Optional<MethodHeaderNode> methodCheck = methodHeaderNode();
        while(methodCheck.isPresent()) {
            interface1.get().methods.add(methodCheck.get());
            methodCheck = methodHeaderNode();
        }
        if(tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()){
            throw new SyntaxErrorException("interface requires a dedent",0,0);
        }
        RequireNewLine();
        return interface1;
    }

    public Optional<ClassNode> classNode() throws SyntaxErrorException {
        Optional<ClassNode> class1 = Optional.of(new ClassNode());

        Optional<Token> nameCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(nameCheck.isEmpty()){
            throw new SyntaxErrorException("class requires a name",0,0);
        }
        class1.get().name = nameCheck.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.IMPLEMENTS).isPresent()){
            Optional<Token> interfaceCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
            if(interfaceCheck.isEmpty()){
                throw new SyntaxErrorException("implementation requires a name",0,0);}
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
            throw new SyntaxErrorException("class requires a indent",0,0);
        }

        Optional<MemberNode> memberCheck = memberNode();
        memberCheck.ifPresent(memberNode -> class1.get().members.add(memberNode));
        RequireNewLine();
        while((memberCheck = memberNode()).isPresent()){
            class1.get().members.add(memberCheck.get());
            if(tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()){
                RequireNewLine();
            }else{
                return class1;
            }
        }

        Optional<ConstructorNode> constructorCheck = constructorNode();
        while(constructorCheck.isPresent()){
            class1.get().constructors.add(constructorCheck.get());
            constructorCheck = constructorNode();
            if(tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()){
                throw new SyntaxErrorException("constructor requires a dedent",0,0);
            }
        }

        if(tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
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
        }
        if(!tokens.done()) {
            Optional<MethodDeclarationNode> methodCheck = methodNode();
            while (methodCheck.isPresent()) {
                class1.get().methods.add(methodCheck.get());
                methodCheck = methodNode();
                if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                    throw new SyntaxErrorException("method requires a dedent", 0, 0);
                }
            }
        }


        return class1;
    }

    private Optional<ConstructorNode> constructorNode() throws SyntaxErrorException {
        Optional<ConstructorNode> constructor =Optional.of(new ConstructorNode());
        if(tokens.matchAndRemove(Token.TokenTypes.CONSTRUCT).isEmpty()){
            return Optional.empty();}
        if(tokens.matchAndRemove(Token.TokenTypes.LPAREN).isEmpty()){
            throw new SyntaxErrorException("constructor requires a left paren",0,0);}
        Optional<VariableDeclarationNode> variableCheck = VariableDeclarationNode();
        variableCheck.ifPresent(variableDeclarationNode -> constructor.get().parameters.add(variableDeclarationNode));
        variableCheck = VariableDeclarationNodes();
        while(variableCheck.isPresent()) {
            constructor.get().parameters.add(variableCheck.get());
            variableCheck = VariableDeclarationNodes();}
        if(tokens.matchAndRemove(Token.TokenTypes.RPAREN).isEmpty()){
            throw new SyntaxErrorException("constructor requires a right paren",0,0);}
        RequireNewLine();
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("constructor requires a indent",0,0);}
        variableCheck.ifPresent(variableDeclarationNode -> constructor.get().parameters.add(variableDeclarationNode));
        variableCheck = VariableDeclarationNodes();
        while(variableCheck.isPresent()) {
            constructor.get().parameters.add(variableCheck.get());
            variableCheck = VariableDeclarationNodes();}
        Optional<StatementNode> statementCheck = statementNode();
        if(statementCheck.isPresent()) {
            constructor.get().statements.add(statementCheck.get());
            if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                throw new SyntaxErrorException("constructor requires a dedent", 0, 0);
            }
        }
        return constructor;
    }

    private Optional<MethodDeclarationNode> methodNode() throws SyntaxErrorException {
        Optional<MethodDeclarationNode> method = Optional.of(new MethodDeclarationNode());
        if(tokens.matchAndRemove(Token.TokenTypes.PRIVATE).isPresent()){
            method.get().isPrivate = true;}
        if(tokens.matchAndRemove(Token.TokenTypes.SHARED).isPresent()){
            method.get().isShared = true;}
        Optional<MethodHeaderNode> MethodHeaderCheck = methodHeaderNode();
        if(MethodHeaderCheck.isEmpty()){
            return Optional.empty();}
        if(MethodHeaderCheck.isPresent()){
            MethodHeaderCheck.get().parameters = method.get().parameters;
            MethodHeaderCheck.get().returns = method.get().returns;
            MethodHeaderCheck.get().name = method.get().name;
        }
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("method requires a indent",0,0);}
        Optional<VariableDeclarationNode> variableCheck = VariableDeclarationNode();
        variableCheck.ifPresent(variableDeclarationNode -> method.get().locals.add(variableDeclarationNode));
        RequireNewLine();
        variableCheck = VariableDeclarationNode();
        while(variableCheck.isPresent()) {
            method.get().locals.add(variableCheck.get());
            variableCheck = VariableDeclarationNode();}
        Optional<StatementNode> statementCheck = statementNodes();
        if(statementCheck.isPresent()) {
            method.get().statements.add(statementCheck.get());
            RequireNewLine();
            if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                throw new SyntaxErrorException("method requires a dedent", 0, 0);
            }
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
                throw new SyntaxErrorException("accessor needs a colon",0,0);
            }
            Optional<StatementNode> statementCheck = statementNodes();
            if(statementCheck.isPresent()) {
                member.get().accessor.get().add(statementCheck.get());
                RequireNewLine();
                if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                    throw new SyntaxErrorException("method requires a dedent", 0, 0);
                }
            }
        }
        if(accessCheck.isPresent() && accessCheck.get().getValue().equals("mutator")){
            if(tokens.matchAndRemove(Token.TokenTypes.COLON).isEmpty()){
                throw new SyntaxErrorException("accessor needs a colon",0,0);
            }
            Optional<StatementNode> statementCheck = statementNodes();
            if(statementCheck.isPresent()) {
                member.get().mutator.get().add(statementCheck.get());
                RequireNewLine();
                if (tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty()) {
                    throw new SyntaxErrorException("method requires a dedent", 0, 0);
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
        return Optional.empty();
    }

    private Optional<StatementNode> statementNodes() throws SyntaxErrorException{
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isPresent()){
            return statementNode();
        }
        return Optional.empty();
    }


    private Optional<StatementNode> ifNode() throws SyntaxErrorException {
        //Optional<IfNode> conditonCheck = ifNode();
        /*
        if(conditonCheck.isEmpty()){
            throw new SyntaxErrorException("if requires a conditon",0,0);}
        ifNode.get().condition = (ExpressionNode) conditonCheck.get();
        do{
            statementNode();
        }while(tokens.matchAndRemove(Token.TokenTypes.DEDENT).isEmpty());
        Optional<Token> elseNode = tokens.matchAndRemove(Token.TokenTypes.ELSE);
        elseNode.ifPresent(token -> ifNode.get().statements.add((StatementNode) token));
         */
        return Optional.of(new IfNode());
    }

    private Optional<StatementNode> loopNode() throws SyntaxErrorException {
        return Optional.of(new LoopNode());
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

    private Optional<ExpressionNode> expressionNode() throws SyntaxErrorException {
        Optional<VariableReferenceNode> variableCheck = VariableReferenceNode();
        if(variableCheck.isPresent()) {
            return Optional.of(variableCheck.get());
        }
        return Optional.empty();
    }

    private Optional<BooleanOpNode> booleanOpNode() throws SyntaxErrorException {
        Optional<BooleanOpNode> BooleanOp = Optional.of(new BooleanOpNode());
        Optional<CompareNode> compareCheck = compareNode();
        if(compareCheck.isPresent()) {
            BooleanOp.get().left = compareCheck.get();
            if(tokens.matchAndRemove(Token.TokenTypes.AND).isPresent()){
                BooleanOp.get().op = BooleanOpNode.BooleanOperations.and;
            }else if(tokens.matchAndRemove(Token.TokenTypes.OR).isPresent()){
                BooleanOp.get().op = BooleanOpNode.BooleanOperations.or;
            }
            Optional<BooleanOpNode> newBooleanOp = booleanOpNode();
            if(newBooleanOp.isPresent()) {
                BooleanOp.get().right = newBooleanOp.get();
            }
        }
        if(tokens.matchAndRemove(Token.TokenTypes.NOT).isPresent()){
            Optional<BooleanOpNode> newBooleanOp = booleanOpNode();
            if(newBooleanOp.isPresent()) {
                BooleanOp.get().left = newBooleanOp.get();
            }

        }
        return BooleanOp;
    }

    private Optional<CompareNode> compareNode() throws SyntaxErrorException{
        Optional<CompareNode> compare = Optional.of(new CompareNode());
        Optional<MethodCallExpressionNode> methodCallCheck = MethodCallExpressionNode();
        if(methodCallCheck.isPresent()) {
            return Optional.of(compare.get());}
        Optional<ExpressionNode> expressionCheck = expressionNode();
        if(expressionCheck.isPresent()) {
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
            }else if(tokens.matchAndRemove(Token.TokenTypes.NOTEQUAL).isPresent()) {
                compare.get().op = CompareNode.CompareOperations.ne;
            }else{
                throw new SyntaxErrorException("unexpected token",0,0);
            }
        }
        Optional<VariableReferenceNode> variableCheck = VariableReferenceNode();
        if(variableCheck.isPresent()) {
            return Optional.of(compare.get());
        }
        return Optional.empty();
    }


    private Optional<MethodCallExpressionNode> MethodCallExpressionNode() throws SyntaxErrorException {
        return Optional.of(new MethodCallExpressionNode());
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
        }
        if(tokens.matchAndRemove(Token.TokenTypes.EQUAL).isEmpty()){
            throw new SyntaxErrorException("method requires a equal",0,0);
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
        }
        Optional<VariableReferenceNode> variableCheck = VariableReferenceNode();
        if(variableCheck.isPresent()){
            return Optional.empty();}
        if(tokens.matchAndRemove(Token.TokenTypes.COMMA).isPresent()){
            Optional<MethodCallStatementNode> methodCall = MethodCallStatementNode();
            if(methodCall.isPresent()){
                return Optional.of(methodCall.get());
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
        if(PeekCheck.get().getType() != Token.TokenTypes.LPAREN ) {
            return Optional.empty();}
        Optional<Token> wordCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(wordCheck.isEmpty()) {
            return Optional.empty();}
        method.get().name = wordCheck.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.LPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis",0,0);}
        Optional<VariableDeclarationNode> paramCheck = VariableDeclarationNode();
        paramCheck.ifPresent(variableDeclarationNode -> method.get().parameters.add(variableDeclarationNode));
        paramCheck = VariableDeclarationNodes();
        while(paramCheck.isPresent()) {
            method.get().parameters.add(paramCheck.get());
            paramCheck = VariableDeclarationNodes();
        }
        if(tokens.matchAndRemove(Token.TokenTypes.RPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis",0,0);}

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
        if(PeekCheck.get().getType() != Token.TokenTypes.WORD ) {
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