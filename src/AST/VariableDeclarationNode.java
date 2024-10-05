package AST;

import java.util.List;

public class VariableDeclarationNode implements Node {
    public String type;
    public String name;

    public VariableDeclarationNode(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
