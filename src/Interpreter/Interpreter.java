package Interpreter;

import AST.*;

import java.util.*;

public class Interpreter {
    private final TranNode tran;
    /** Constructor - get the interpreter ready to run. Set members from parameters and "prepare" the class.
     *
     * Store the tran node.
     * Add any built-in methods to the AST
     * @param top - the head of the AST
     */
    public Interpreter(TranNode top) {
        tran = top;
        ConsoleWrite write = new ConsoleWrite();
        write.isVariadic = true;
        write.isShared = true;
        write.name = "write";

        ClassNode classNode = new ClassNode();
        classNode.name = "console";
        classNode.methods.add(write);

        tran.Classes.add(classNode);

    }

    /**
     * This is the public interface to the interpreter. After parsing, we will create an interpreter and call start to
     * start interpreting the code.
     *
     * Search the classes in Tran for a method that is "isShared", named "start", that is not private and has no parameters
     * Call "InterpretMethodCall" on that method, then return.
     * Throw an exception if no such method exists.
     */
    public void start() throws Exception{
        for(int i = 0; i < tran.Classes.size(); i++) {
            ClassNode classNode = tran.Classes.get(i);
            for(int j = 0; j < classNode.methods.size(); j++) {
                MethodDeclarationNode methodNode= classNode.methods.get(j);
                if (methodNode.isShared && methodNode.name.equals("start") && !methodNode.isPrivate && (methodNode.parameters.isEmpty()) ){
                    List<InterpreterDataType> parameters = new LinkedList<InterpreterDataType>();
                    interpretMethodCall(Optional.of(new ObjectIDT(classNode)), methodNode, parameters);
                    return;
                }
            }
        }
        throw new Exception("no such method exists");
    }

    //              Running Methods

    /**
     * Find the method (local to this class, shared (like Java's system.out.print), or a method on another class)
     * Evaluate the parameters to have a list of values
     * Use interpretMethodCall() to actually run the method.
     *
     * Call GetParameters() to get the parameter value list
     * Find the method. This is tricky - there are several cases:
     * someLocalMethod() - has NO object name. Look in "object"
     * console.write() - the objectName is a CLASS and the method is shared
     * bestStudent.getGPA() - the objectName is a local or a member
     *
     * Once you find the method, call InterpretMethodCall() on it. Return the list that it returns.
     * Throw an exception if we can't find a match.
     * @param object - the object we are inside right now (might be empty)
     * @param locals - the current local variables
     * @param mc - the method call
     * @return - the return values
     */
    private List<InterpreterDataType> findMethodForMethodCallAndRunIt(Optional<ObjectIDT> object, HashMap<String, InterpreterDataType> locals, MethodCallStatementNode mc) throws Exception {
        List<InterpreterDataType> parameters = getParameters(object, locals, mc);
        MethodDeclarationNode method = getMethodFromObject(object.get(), mc, parameters);
        String objectName = object.get().astNode.name;
        if(mc.objectName == null) {
            return interpretMethodCall(object, method, parameters);
        }else if(getClassByName(objectName).isPresent() && method.isShared){
            return interpretMethodCall(object, method, parameters);
        }else if(object.get().members.containsKey(mc.objectName.get())){
            return interpretMethodCall(object, method, parameters);
        }else if(locals.containsKey(mc.objectName.get())){
            InterpreterDataType reference = locals.get(mc.objectName.get());
            if(reference instanceof ReferenceIDT) {
                Optional<ObjectIDT> localObject = ((ReferenceIDT) reference).refersTo;
                return interpretMethodCall(localObject, method, parameters);
            }
            return interpretMethodCall(object, method, parameters);
        }
        throw new Exception("no such method exists");
    }

    /**
     * Run a "prepared" method (found, parameters evaluated)
     * This is split from findMethodForMethodCallAndRunIt() because there are a few cases where we don't need to do the finding:
     * in start() and dealing with loops with iterator objects, for example.
     *
     * Check to see if "m" is a built-in. If so, call Execute() on it and return
     * Make local variables, per "m"
     * If the number of passed in values doesn't match m's "expectations", throw
     * Add the parameters by name to locals.
     * Call InterpretStatementBlock
     * Build the return list - find the names from "m", then get the values for those names and add them to the list.
     * @param object - The object this method is being called on (might be empty for shared)
     * @param m - Which method is being called
     * @param values - The values to be passed in
     * @return the returned values from the method
     */
    private List<InterpreterDataType> interpretMethodCall(Optional<ObjectIDT> object, MethodDeclarationNode m, List<InterpreterDataType> values) throws Exception {
        var retVal = new LinkedList<InterpreterDataType>();
        HashMap<String, InterpreterDataType> locals = new HashMap<>();
        if(m instanceof BuiltInMethodDeclarationNode && m.isShared) {
            if(m instanceof ConsoleWrite){
                return ((ConsoleWrite) m).Execute(values);
            }
        }
        for(int i = 0 ; i < m.locals.size(); i++) {
            locals.put(m.locals.get(i).name, instantiate(m.locals.get(i).type));
        }
        if(m.parameters.size() != values.size()) {
            throw new Exception("types do not match");
        }
        interpretStatementBlock(object, m.statements,locals);
        for(int i = 0; i< m.returns.size(); i++) {
            InterpreterDataType returnVal = object.get().members.get(m.returns.get(i).name);
            retVal.add(returnVal);
        }
        return retVal;
    }

    //              Running Constructors

    /**
     * This is a special case of the code for methods. Just different enough to make it worthwhile to split it out.
     *
     * Call GetParameters() to populate a list of IDT's
     * Call GetClassByName() to find the class for the constructor
     * If we didn't find the class, throw an exception
     * Find a constructor that is a good match - use DoesConstructorMatch()
     * Call InterpretConstructorCall() on the good match
     * @param callerObj - the object that we are inside when we called the constructor
     * @param locals - the current local variables (used to fill parameters)
     * @param mc  - the method call for this construction
     * @param newOne - the object that we just created that we are calling the constructor for
     */
    private void findConstructorAndRunIt(Optional<ObjectIDT> callerObj, HashMap<String, InterpreterDataType> locals, MethodCallStatementNode mc, ObjectIDT newOne) throws Exception {
        List<InterpreterDataType> IDTs = getParameters(callerObj,locals,mc);
        Optional<ClassNode> className = getClassByName(callerObj.get().astNode.name);
        if(className.isEmpty()) {
            throw new Exception("no such class");
        }
        for(int i = 0; i < className.get().constructors.size(); i++) {
            if (doesConstructorMatch(className.get().constructors.get(i), mc,IDTs)){
                interpretConstructorCall(newOne, className.get().constructors.get(i), IDTs);
            }
        }
    }

    /**
     * Similar to interpretMethodCall, but "just different enough" - for example, constructors don't return anything.
     *
     * Creates local variables (as defined by the ConstructorNode), calls Instantiate() to do the creation
     * Checks to ensure that the right number of parameters were passed in, if not throw.
     * Adds the parameters (with the names from the ConstructorNode) to the locals.
     * Calls InterpretStatementBlock
     * @param object - the object that we allocated
     * @param c - which constructor is being called
     * @param values - the parameter values being passed to the constructor
     */
    private void interpretConstructorCall(ObjectIDT object, ConstructorNode c, List<InterpreterDataType> values) throws Exception {
        HashMap<String, InterpreterDataType> locals = new HashMap<>();
        for(int i = 0; i < c.parameters.size(); i++) {
            VariableDeclarationNode variable = c.parameters.get(i);
            locals.put(variable.name,instantiate(variable.type));
        }
        if(values.size() != locals.size()) {
            throw new Exception("number of values do not match");
        }
        for(int i = 0; i < c.parameters.size(); i++) {
            locals.put(c.parameters.get(i).name,values.get(i));
        }
        interpretStatementBlock(Optional.of(object),c.statements, locals);
    }

    //              Running Instructions

    /**
     * Given a block (which could be from a method or an "if" or "loop" block, run each statement.
     * Blocks, by definition, do ever statement, so iterating over the statements makes sense.
     *
     * For each statement in statements:
     * check the type:
     *      For AssignmentNode, FindVariable() to get the target. Evaluate() the expression. Call Assign() on the target with the result of Evaluate()
     *      For MethodCallStatementNode, call doMethodCall(). Loop over the returned values and copy the into our local variables
     *      For LoopNode - there are 2 kinds.
     *          Setup:
     *          If this is a Loop over an iterator (an Object node whose class has "iterator" as an interface)
     *              Find the "getNext()" method; throw an exception if there isn't one
     *          Loop:
     *          While we are not done:
     *              if this is a boolean loop, Evaluate() to get true or false.
     *              if this is an iterator, call "getNext()" - it has 2 return values. The first is a boolean (was there another?), the second is a value
     *              If the loop has an assignment variable, populate it: for boolean loops, the true/false. For iterators, the "second value"
     *              If our answer from above is "true", InterpretStatementBlock() on the body of the loop.
     *       For If - Evaluate() the condition. If true, InterpretStatementBlock() on the if's statements. If not AND there is an else, InterpretStatementBlock on the else body.
     * @param object - the object that this statement block belongs to (used to get member variables and any members without an object)
     * @param statements - the statements to run
     * @param locals - the local variables
     */
    private void interpretStatementBlock(Optional<ObjectIDT> object, List<StatementNode> statements, HashMap<String, InterpreterDataType> locals) throws Exception {
        for(StatementNode stmt : statements) {
            if(stmt instanceof AssignmentNode) {
                InterpreterDataType target = findVariable(((AssignmentNode) stmt).target.name, locals, object);
                InterpreterDataType variable = evaluate(locals, object, ((AssignmentNode)stmt).expression);
                target.Assign(variable);
            } else if (stmt instanceof MethodCallStatementNode) {
                List<InterpreterDataType> methodReturns = findMethodForMethodCallAndRunIt(object, locals, ((MethodCallStatementNode) stmt));
                for(int i = 0; i < methodReturns.size(); i++) {
                    locals.put(((MethodCallStatementNode) stmt).returnValues.get(i).name, methodReturns.get(i));
                }
            }else if (stmt instanceof LoopNode) {
                ExpressionNode expression = ((LoopNode) stmt).expression;
                if(expression instanceof BooleanOpNode){
                    BooleanIDT expressionEqual = (BooleanIDT) evaluate(locals, object, expression);
                    while(expressionEqual.Value) {
                        interpretStatementBlock(object, ((LoopNode) stmt).statements, locals);
                        expressionEqual = (BooleanIDT) evaluate(locals, object, expression);
                    }
                }else if(expression instanceof AssignmentNode){
                    InterpreterDataType target = findVariable(((AssignmentNode) expression).target.name, locals, object);
                    target.Assign(evaluate(locals, object, ((AssignmentNode)expression).expression));
                    BooleanIDT expressionEqual = (BooleanIDT) evaluate(locals, object, expression);
                    while(expressionEqual.Value) {
                        interpretStatementBlock(object, ((LoopNode) stmt).statements, locals);
                        expressionEqual = (BooleanIDT) evaluate(locals, object, expression);
                    }
                }else if(expression instanceof VariableReferenceNode){
                    BooleanIDT expressionEqual = (BooleanIDT) evaluate(locals, object, expression);
                    while(expressionEqual.Value) {
                        interpretStatementBlock(object, ((LoopNode) stmt).statements, locals);
                        expressionEqual = (BooleanIDT) evaluate(locals, object, expression);
                    }
                }

            }else if (stmt instanceof IfNode){
                ExpressionNode condition = ((IfNode) stmt).condition;
                BooleanIDT expressionEqual = (BooleanIDT) evaluate(locals, object, condition);

                if(expressionEqual.Value) {
                    interpretStatementBlock(object, ((IfNode) stmt).statements, locals);
                }else{
                    interpretStatementBlock(object, ((IfNode) stmt).elseStatement.get().statements, locals);
                }
            }
        }
    }

    /**
     *  evaluate() processes everything that is an expression - math, variables, boolean expressions.
     *  There is a good bit of recursion in here, since math and comparisons have left and right sides that need to be evaluated.
     *
     * See the How To Write an Interpreter document for examples
     * For each possible ExpressionNode, do the work to resolve it:
     * BooleanLiteralNode - create a new BooleanLiteralNode with the same value
     *      - Same for all of the basic data types
     * BooleanOpNode - Evaluate() left and right, then perform either and/or on the results.
     * CompareNode - Evaluate() both sides. Do good comparison for each data type
     * MathOpNode - Evaluate() both sides. If they are both numbers, do the math using the built-in operators. Also handle String + String as concatenation (like Java)
     * MethodCallExpression - call doMethodCall() and return the first value
     * VariableReferenceNode - call findVariable()
     * @param locals the local variables
     * @param object - the current object we are running
     * @param expression - some expression to evaluate
     * @return a value
     */
    private InterpreterDataType evaluate(HashMap<String, InterpreterDataType> locals, Optional<ObjectIDT> object, ExpressionNode expression) throws Exception {
        if(expression instanceof BooleanLiteralNode bool) {
            return new BooleanIDT(bool.value);
        }else if(expression instanceof NumericLiteralNode numeric) {
            return new NumberIDT(numeric.value);
        }else if(expression instanceof CharLiteralNode charLiteral) {
            return new CharIDT(charLiteral.value);
        }else if(expression instanceof StringLiteralNode stringLiteral) {
            return new StringIDT(stringLiteral.value);
        }else if (expression instanceof VariableReferenceNode variable) {
            return findVariable(variable.name, locals, object);
        }else if(expression instanceof BooleanOpNode) {
            BooleanIDT left = (BooleanIDT) evaluate(locals, object, ((BooleanOpNode) expression).left);
            BooleanIDT right = (BooleanIDT) evaluate(locals, object, ((BooleanOpNode) expression).right);
            if(((BooleanOpNode) expression).op == BooleanOpNode.BooleanOperations.or) {
                return new BooleanIDT(left.Value || right.Value);}
            if(((BooleanOpNode) expression).op == BooleanOpNode.BooleanOperations.and) {
                return new BooleanIDT(left.Value && right.Value);}
        }else if(expression instanceof CompareNode) {
            NumberIDT left = (NumberIDT) evaluate(locals, object, ((CompareNode) expression).left);
            NumberIDT right = (NumberIDT) evaluate(locals, object, ((CompareNode) expression).right);
            if (((CompareNode) expression).op == CompareNode.CompareOperations.eq) {
                return new BooleanIDT(left.Value == right.Value);
            } else if (((CompareNode) expression).op == CompareNode.CompareOperations.ne) {
                return new BooleanIDT(left.Value != right.Value);
            } else if (((CompareNode) expression).op == CompareNode.CompareOperations.gt) {
                return new BooleanIDT(left.Value > right.Value);
            } else if (((CompareNode) expression).op == CompareNode.CompareOperations.ge) {
                return new BooleanIDT(left.Value >= right.Value);
            } else if (((CompareNode) expression).op == CompareNode.CompareOperations.lt) {
                return new BooleanIDT(left.Value < right.Value);
            } else if (((CompareNode) expression).op == CompareNode.CompareOperations.le) {
                return new BooleanIDT(left.Value <= right.Value);
            }else {
                throw new Exception("Unknown compare operator");
            }
        }else if(expression instanceof MathOpNode){
            NumberIDT left = (NumberIDT) evaluate(locals, object, ((MathOpNode) expression).left);
            NumberIDT right = (NumberIDT) evaluate(locals, object, ((MathOpNode) expression).right);
            if(((MathOpNode) expression).op == MathOpNode.MathOperations.add){
                return new NumberIDT(left.Value + right.Value);
            }else if(((MathOpNode) expression).op == MathOpNode.MathOperations.subtract){
                return new NumberIDT(left.Value - right.Value);
            }else if(((MathOpNode) expression).op == MathOpNode.MathOperations.multiply){
                return new NumberIDT(left.Value * right.Value);
            }else if(((MathOpNode) expression).op == MathOpNode.MathOperations.divide){
                return new NumberIDT(left.Value / right.Value);
            } else if (((MathOpNode) expression).op == MathOpNode.MathOperations.modulo) {
                return new NumberIDT(left.Value % right.Value);
            }else{
                throw new Exception("Unknown math operator");
            }
        }else if(expression instanceof NewNode){
            MethodCallStatementNode method = new MethodCallStatementNode();
            method.methodName = ((NewNode) expression).className;
            method.parameters = ((NewNode) expression).parameters;
            ObjectIDT newObject = new ObjectIDT(getClassByName(method.methodName).get());
            findConstructorAndRunIt(object, locals, method, newObject);
            return newObject;
        }else if(expression instanceof MethodCallExpressionNode){
            MethodCallStatementNode method = new MethodCallStatementNode();
            method.methodName = ((MethodCallExpressionNode) expression).methodName;
            method.parameters = ((MethodCallExpressionNode) expression).parameters;
            List<InterpreterDataType> methodReturns = findMethodForMethodCallAndRunIt(object, locals, method);
            return methodReturns.get(0);
        }
        throw new IllegalArgumentException();
    }

    //              Utility Methods

    /**
     * Used when trying to find a match to a method call. Given a method declaration, does it match this methoc call?
     * We double check with the parameters, too, although in theory JUST checking the declaration to the call should be enough.
     *
     * Match names, parameter counts (both declared count vs method call and declared count vs value list), return counts.
     * If all of those match, consider the types (use TypeMatchToIDT).
     * If everything is OK, return true, else return false.
     * Note - if m is a built-in and isVariadic is true, skip all of the parameter validation.
     * @param m - the method declaration we are considering
     * @param mc - the method call we are trying to match
     * @param parameters - the parameter values for this method call
     * @return does this method match the method call?
     */
    private boolean doesMatch(MethodDeclarationNode m, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
        if (mc.parameters.size() != parameters.size()) {
            return false;
        }
        Iterator<InterpreterDataType> iterator = parameters.listIterator();
        if(m.name.equals(mc.methodName)){
            for(ExpressionNode p : mc.parameters) {
                InterpreterDataType parameter = iterator.next();
                if (typeMatchToIDT(p.toString(),parameter)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Very similar to DoesMatch() except simpler - there are no return values, the name will always match.
     * @param c - a particular constructor
     * @param mc - the method call
     * @param parameters - the parameter values
     * @return does this constructor match the method call?
     */
    private boolean doesConstructorMatch(ConstructorNode c, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
        if (mc.parameters.size() != parameters.size()) {
            return false;
        }
        Iterator<InterpreterDataType> iterator = parameters.listIterator();
        for(ExpressionNode p : mc.parameters) {
            InterpreterDataType parameter = iterator.next();
            if (typeMatchToIDT(p.toString(),parameter)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Used when we call a method to get the list of values for the parameters.
     *
     * for each parameter in the method call, call Evaluate() on the parameter to get an IDT and add it to a list
     * @param object - the current object
     * @param locals - the local variables
     * @param mc - a method call
     * @return the list of method values
     */
    private List<InterpreterDataType> getParameters(Optional<ObjectIDT> object, HashMap<String,InterpreterDataType> locals, MethodCallStatementNode mc) throws Exception {
        List<InterpreterDataType> result = new ArrayList<InterpreterDataType>();
        for(ExpressionNode parameters : mc.parameters){
            result.add(evaluate(locals, object, parameters));
        }
        return result;
    }

    /**
     * Used when we have an IDT and we want to see if it matches a type definition
     * Commonly, when someone is making a function call - do the parameter values match the method declaration?
     *
     * If the IDT is a simple type (boolean, number, etc) - does the string type match the name of that IDT ("boolean", etc)
     * If the IDT is an object, check to see if the name matches OR the class has an interface that matches
     * If the IDT is a reference, check the inner (refered to) type
     * @param type the name of a data type (parameter to a method)
     * @param idt the IDT someone is trying to pass to this method
     * @return is this OK?
     */
    private boolean typeMatchToIDT(String type, InterpreterDataType idt) {
        if(idt instanceof BooleanIDT || idt instanceof StringIDT  || idt instanceof NumberIDT || idt instanceof CharIDT ) {
            return instantiate(type) == idt;
        }
        if(idt instanceof ObjectIDT){
            return instantiate(type) == idt || ((ObjectIDT) idt).astNode.interfaces.equals(instantiate(type));
        }
        if(idt instanceof ReferenceIDT){
            return ((ReferenceIDT) idt).refersTo.get().astNode.interfaces.equals(instantiate(type));
        }
        throw new RuntimeException("Unable to resolve type " + type);
    }

    /**
     * Find a method in an object that is the right match for a method call (same name, parameters match, etc. Uses doesMatch() to do most of the work)
     *
     * Given a method call, we want to loop over the methods for that class, looking for a method that matches (use DoesMatch) or throw
     * @param object - an object that we want to find a method on
     * @param mc - the method call
     * @param parameters - the parameter value list
     * @return a method or throws an exception
     */
    private MethodDeclarationNode getMethodFromObject(ObjectIDT object, MethodCallStatementNode mc, List<InterpreterDataType> parameters) {
        for(ClassNode classes : tran.Classes){
            for (MethodDeclarationNode method : classes.methods) {
                if(method.name.equals(mc.methodName)) {
                    return method;
                }
            }
        }
        throw new RuntimeException("Unable to resolve method call " + mc);
    }

    /**
     * Find a class, given the name. Just loops over the TranNode's classes member, matching by name.
     *
     * Loop over each class in the top node, comparing names to find a match.
     * @param name Name of the class to find
     * @return either a class node or empty if that class doesn't exist
     */
    private Optional<ClassNode> getClassByName(String name) {
        for(ClassNode c : tran.Classes){
            if(c.name.equals(name)){
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    /**
     * Given an execution environment (the current object, the current local variables), find a variable by name.
     *
     * @param name  - the variable that we are looking for
     * @param locals - the current method's local variables
     * @param object - the current object (so we can find members)
     * @return the IDT that we are looking for or throw an exception
     */
    private InterpreterDataType findVariable(String name, HashMap<String,InterpreterDataType> locals, Optional<ObjectIDT> object) {
        if(locals.containsKey(name)) {
            return locals.get(name);
        }
        if(object.get().members.containsKey(name)){
            return object.get().members.get(name);
        }
        List<MethodDeclarationNode> methods = object.get().astNode.methods;
        for (MethodDeclarationNode method : methods) {
            if (!method.returns.isEmpty()) {
                for (int j = 0; j < method.returns.size(); j++) {
                    if (method.returns.get(j).name.equals(name)) {
                        InterpreterDataType returnValue = instantiate(method.returns.get(j).type);
                        object.get().members.put(name, returnValue);
                        return object.get().members.get(name);
                    }
                }
            }
        }
        for (int i = 0; i < object.get().astNode.members.size(); i++) {
            if(object.get().astNode.members.get(i).declaration.name.equals(name)){
                InterpreterDataType member = instantiate(object.get().astNode.members.get(i).declaration.type);
                object.get().members.put(name, member);
                return object.get().members.get(name);
            }
        }
        throw new RuntimeException("Unable to find variable " + name);
    }

    /**
     * Given a string (the type name), make an IDT for it.
     *
     * @param type The name of the type (string, number, boolean, character). Defaults to ReferenceIDT if not one of those.
     * @return an IDT with default values (0 for number, "" for string, false for boolean, ' ' for character)
     */
    private InterpreterDataType instantiate(String type) {
        return switch (type) {
            case "string" -> new StringIDT("");
            case "number" -> new NumberIDT(0);
            case "boolean" -> new BooleanIDT(false);
            case "character" -> new CharIDT(' ');
            default -> new ReferenceIDT();
        };
    }
}
