public class BuggyLogic {
    public static int add(int a, int b) {
        // Incorrect logic: should be a + b, but uses subtraction
        return a - b;
    }

    public static void main(String[] args) {
        System.out.println("Sum: " + add(5, 3)); // Expected 8, will print 2
    }
}
