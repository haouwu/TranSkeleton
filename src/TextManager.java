public class TextManager {
    String text;
    int position;


    public TextManager(String input) {
        text = input;
        position = 0;
    }

    public boolean isAtEnd() {
        return position >= text.length();
    }

    public char peekCharacter() {
       return text.charAt(position);
    }

    public char peekCharacter(int distance) {
        return '~';
    }

    public char getCharacter() {
        return text.charAt(position++);
    }
}
