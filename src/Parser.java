import AST.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    private TranNode root;
    private final TokenManager tokens;

    public Parser(TranNode top, List<Token> Tokens) {
        tokens = new TokenManager(Tokens);
        root = top;
    }

    public void RequireNewLine(){
        while(!tokens.done()){
            tokens.matchAndRemove(Token.TokenTypes.NEWLINE);
            tokens.position++;
        }
    }

    private Optional<InterfaceNode> interfaceNode() throws SyntaxErrorException {
        if(tokens.matchAndRemove(Token.TokenTypes.INTERFACE).isEmpty()) {
            return Optional.empty();}
        Optional<Token> nameToken = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(nameToken.isEmpty()){
            throw new SyntaxErrorException("interface requires a name",0,0);}
        String name = nameToken.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.NEWLINE).isEmpty()){
            throw new SyntaxErrorException("interface requires a newline",0,0);}
        if(tokens.matchAndRemove(Token.TokenTypes.INDENT).isEmpty()){
            throw new SyntaxErrorException("interface requires a indent",0,0);}
        List<MethodHeaderNode> methods = new ArrayList<>();
        Optional<MethodHeaderNode> methodCheck = methodHeaderNode();
        while(methodCheck.isPresent()) {
            methods.add(methodCheck.get());
            methodCheck = methodHeaderNode();
        }
        return Optional.of(new InterfaceNode(name, methods));
    }

    private Optional<MethodHeaderNode> methodHeaderNode() throws SyntaxErrorException {
        Optional<Token> wordCheck = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(wordCheck.isEmpty()) {
            return Optional.empty();}
        String word = wordCheck.get().getValue();
        if(tokens.matchAndRemove(Token.TokenTypes.LPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis",0,0);}
        ArrayList<VariableDeclarationNode> parameters = new ArrayList<>();
        Optional<VariableDeclarationNode> paramCheck = VariableDeclarationNode();
        if(paramCheck.isPresent()) {
            parameters.add(paramCheck.get());
        }
        if(tokens.matchAndRemove(Token.TokenTypes.RPAREN).isEmpty()) {
            throw new SyntaxErrorException("method requires a parenthesis",0,0);}
        if(tokens.matchAndRemove(Token.TokenTypes.COLON).isEmpty()) {
            throw new SyntaxErrorException("method requires a colon",0,0);}
        ArrayList<VariableDeclarationNode> returns = new ArrayList<>();
        Optional<VariableDeclarationNode> returnCheck = VariableDeclarationNode();
        if(returnCheck.isPresent()) {
            parameters.add(returnCheck.get());
        }else{
            throw new SyntaxErrorException("method requires a parameter",0,0);
        }
        VariableDeclarationNodes(returns);
        return Optional.of(new MethodHeaderNode(word, parameters, returns));
    }

    private Optional<VariableDeclarationNode> VariableDeclarationNode() throws SyntaxErrorException {
        Optional<Token> Type = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(Type.isEmpty()) {
            return Optional.empty();
        }
        String type = Type.get().getValue();
        Optional<Token> Name = tokens.matchAndRemove(Token.TokenTypes.WORD);
        if(Name.isEmpty()) {
            throw new SyntaxErrorException("variable requires a name",0,0);
        }
        String name = Name.get().getValue();
        return Optional.of(new VariableDeclarationNode(type, name));
    }

    private List<VariableDeclarationNode> VariableDeclarationNodes(ArrayList<VariableDeclarationNode> variables) throws SyntaxErrorException {
        while(true){
            Optional<Token> comma = tokens.matchAndRemove(Token.TokenTypes.COMMA);
            if (comma.isEmpty()) {
                break;
            }
            Optional<VariableDeclarationNode> nextVariable = VariableDeclarationNode();
            if(nextVariable.isEmpty()) {
                throw new SyntaxErrorException("variable requires a variable name",0,0);
            }
            variables.add(nextVariable.get());
        }
        return variables;
    }


    // Tran = { Class | Interface }
    public void Tran() throws SyntaxErrorException {

    }
}