import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;

public class Lexer {
    private final TextManager text;
    private final HashMap<String, Token.TokenTypes> keywords;
    private int lineNumber = 1;
    private int columnNumber;
    private int previousColumn = 0;
    private int indentLevel = 0;

    public Lexer(String input) {
        text = new TextManager(input);
        keywords = new HashMap<>();

        keywords.put("=", Token.TokenTypes.ASSIGN);
        keywords.put("(", Token.TokenTypes.LPAREN);
        keywords.put(")", Token.TokenTypes.RPAREN);
        keywords.put(":", Token.TokenTypes.COLON);
        keywords.put(".", Token.TokenTypes.DOT);

        keywords.put("+", Token.TokenTypes.PLUS);
        keywords.put("-", Token.TokenTypes.MINUS);
        keywords.put("*", Token.TokenTypes.TIMES);
        keywords.put("/", Token.TokenTypes.DIVIDE);
        keywords.put("%", Token.TokenTypes.MODULO);
        keywords.put(",", Token.TokenTypes.COMMA);


        keywords.put("==", Token.TokenTypes.EQUAL);
        keywords.put("!", Token.TokenTypes.NOT);
        keywords.put("!=", Token.TokenTypes.NOTEQUAL);
        keywords.put("<", Token.TokenTypes.LESSTHAN);
        keywords.put("<=", Token.TokenTypes.LESSTHANEQUAL);
        keywords.put(">", Token.TokenTypes.GREATERTHAN);
        keywords.put(">=", Token.TokenTypes.GREATERTHANEQUAL);
        keywords.put("&&", Token.TokenTypes.AND);
        keywords.put("||", Token.TokenTypes.OR);

        keywords.put("accessor", Token.TokenTypes.ACCESSOR);
        keywords.put("mutator", Token.TokenTypes.MUTATOR);
        keywords.put("implements", Token.TokenTypes.IMPLEMENTS);
        keywords.put("class", Token.TokenTypes.CLASS);
        keywords.put("interface", Token.TokenTypes.INTERFACE);
        keywords.put("loop", Token.TokenTypes.LOOP);
        keywords.put("if", Token.TokenTypes.IF);
        keywords.put("else", Token.TokenTypes.ELSE);

        keywords.put("private", Token.TokenTypes.PRIVATE);
        keywords.put("shared", Token.TokenTypes.SHARED);
        keywords.put("construct", Token.TokenTypes.CONSTRUCT);

        keywords.put("true", Token.TokenTypes.TRUE);
        keywords.put("false", Token.TokenTypes.FALSE);
        keywords.put("new", Token.TokenTypes.NEW);

        keywords.put("\"", Token.TokenTypes.QUOTEDSTRING);
        keywords.put("'", Token.TokenTypes.QUOTEDCHARACTER);
        keywords.put("\n", Token.TokenTypes.NEWLINE);

    }

    public List<Token> Lex() throws Exception {
        List<Token> ListOfTokens = new LinkedList<>();

        while(!text.isAtEnd()){
            Character C = text.peekCharacter();
            if( previousColumn > columnNumber && !Character.isWhitespace(C) && columnNumber == 0){
                for(int i = indentLevel; i > 0; i--){
                    ListOfTokens.add(new Token(Token.TokenTypes.DEDENT, lineNumber, columnNumber));
                    indentLevel--;
                }
                previousColumn = columnNumber;
            }
            if (Character.isLetter(C)){
                ListOfTokens.add(parseWord());

            }else if(C == '.') {
                text.position++;
                columnNumber++;
                if (!text.isAtEnd()) {
                    C = text.peekCharacter();
                    text.position--;
                    columnNumber--;
                    if (Character.isDigit(C)) {
                        ListOfTokens.add(parseNumber());
                    } else {
                        ListOfTokens.add(parsePunctuation());
                    }
                } else {
                    text.position--;
                    columnNumber--;
                    ListOfTokens.add(parsePunctuation());
                }

            }else if(C.toString().equals("\"") || C.toString().equals("'")) {
                ListOfTokens.add(parseQuotated());

            }else if(C.toString().equals("{")) {
                while (!C.toString().equals("}")) {
                    C = text.getCharacter();
                }
            }else if(keywords.containsKey(Character.toString(C)) || C.toString().equals("|") || C.toString().equals("&")){
                ListOfTokens.add(parsePunctuation());

            }else if(Character.isDigit(C)){
                ListOfTokens.add(parseNumber());

            }else if(columnNumber == 0){
                while(C.toString().equals("\t")){
                    C = text.getCharacter();
                    C = text.peekCharacter();
                    columnNumber += 4;
                    if(previousColumn < columnNumber){
                        ListOfTokens.add(new Token(Token.TokenTypes.INDENT, lineNumber, columnNumber));
                        indentLevel++;
                    }
                }
                while(Character.isWhitespace(C) && C != '\n'){
                    C = text.getCharacter();
                    C = text.peekCharacter();
                    columnNumber++;
                    if(previousColumn < columnNumber && columnNumber % 4 == 0) {
                        ListOfTokens.add(new Token(Token.TokenTypes.INDENT, lineNumber, columnNumber));
                        indentLevel++;
                    }
                }
                if(!Character.isWhitespace(C) && columnNumber%4 != 0){
                    throw new SyntaxErrorException("Indent error",lineNumber,columnNumber);
                }
                if(previousColumn > columnNumber) {
                    int CurrentIndents = (previousColumn - columnNumber) / 4;
                    if(CurrentIndents < indentLevel){
                        for(int i=0 ; i < CurrentIndents; i++){
                            ListOfTokens.add(new Token(Token.TokenTypes.DEDENT, lineNumber, columnNumber));
                            indentLevel--;
                        }
                    }
                }

                previousColumn = columnNumber;
            }else if (Character.isWhitespace(C)) {
                text.position++;
                columnNumber++;
            }
        }
        if(indentLevel > 0){
            for(int i=0 ; i < indentLevel; i++){
                ListOfTokens.add(new Token(Token.TokenTypes.DEDENT, lineNumber, columnNumber));
            }
        }

        return ListOfTokens;
    }

    public Token parseWord() throws Exception{
        StringBuilder CurrentWord = new StringBuilder();
        Character C = text.peekCharacter();

        while(!text.isAtEnd() && Character.isLetter(C)){
            CurrentWord.append(C);
            C = text.getCharacter();
            columnNumber++;
            if(!text.isAtEnd()){
                C = text.peekCharacter();
            }
        }

        if(keywords.containsKey(CurrentWord.toString())){
            return new Token(keywords.get(CurrentWord.toString()), lineNumber, columnNumber);
        }else{
            return new Token(Token.TokenTypes.WORD, lineNumber, columnNumber, CurrentWord.toString());
        }
    }

    public Token parseNumber() throws Exception{
        StringBuilder CurrentWord = new StringBuilder();
        Character C = text.peekCharacter();
        boolean dotUsed = false;

        if(C == '.'){
            CurrentWord.append(C);
            text.getCharacter();
            columnNumber++;
            C = text.peekCharacter();
            dotUsed = true;
        }

        while(!text.isAtEnd() && Character.isDigit(C)){
            CurrentWord.append(C);
            C = text.getCharacter();
            columnNumber++;
            if(!text.isAtEnd()){
                C = text.peekCharacter();
            }
        }

        if (keywords.containsKey(C.toString()) && C == '.' && !dotUsed) {
            CurrentWord.append(C);
            text.getCharacter();
            columnNumber++;
            // if not at end loop
            if(!text.isAtEnd()){
                C = text.peekCharacter();
                while(!text.isAtEnd() && Character.isDigit(C)){
                    CurrentWord.append(C);
                    C = text.getCharacter();
                    columnNumber++;
                    if(!text.isAtEnd()){
                        C = text.peekCharacter();
                    }
                }
            }
        }
        return new Token(Token.TokenTypes.NUMBER, lineNumber, columnNumber, CurrentWord.toString());
    }

    public Token parsePunctuation() throws Exception{
        StringBuilder CurrentWord = new StringBuilder();
        StringBuilder Buffer = new StringBuilder();

        //consumes first punctuation goes to next
        Character C = text.getCharacter();
        columnNumber++;
        CurrentWord.append(C);

        if(!text.isAtEnd()){
            C = text.peekCharacter();

            //check the first character plus second
            Buffer.append(CurrentWord);
            Buffer.append(C);
            if(keywords.containsKey(Buffer.toString())){
                C = text.getCharacter();
                columnNumber++;
                CurrentWord.append(C);
            }
            if(keywords.containsKey(CurrentWord.toString()) && CurrentWord.toString().equals("\n")){
                lineNumber ++;
                columnNumber = 0;
            }
        }
        return new Token(keywords.get(CurrentWord.toString()), lineNumber, columnNumber);
    }

    public Token parseQuotated() throws Exception {
        StringBuilder currentWord = new StringBuilder();
        Character C = text.peekCharacter();

        if(C.toString().equals("\"")) {
            C = text.getCharacter();
            C = text.peekCharacter();
            while (!C.toString().equals("\"")) {
                C = text.getCharacter();
                if(!C.toString().equals("\"")) {
                    currentWord.append(C);
                }
            }
        }

        if(C.toString().equals("'")) {
            C = text.getCharacter();
            C = text.peekCharacter();
            currentWord.append(C);
            C = text.getCharacter();
            C = text.peekCharacter();
            if(!C.toString().equals("'")) {
                throw new SyntaxErrorException("",lineNumber,columnNumber);
            }
        }
        if(C.toString().equals("\"")) {
            return new Token(Token.TokenTypes.QUOTEDSTRING, lineNumber, columnNumber, currentWord.toString());
        }else {
            C = text.getCharacter();
            return new Token(Token.TokenTypes.QUOTEDCHARACTER, lineNumber, columnNumber, currentWord.toString());
        }
    }
}