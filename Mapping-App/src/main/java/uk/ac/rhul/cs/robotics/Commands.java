package uk.ac.rhul.cs.robotics;

public enum Commands {

    POSE('P'), DESTINATION('D'), START('B'), STOP('E'), EXIT('X'), MAP('M');

    private final char keyCode;

    private Commands(char k) {
        keyCode = k;
    }

    public final char getCode() {
        return keyCode;
    }
    
}
