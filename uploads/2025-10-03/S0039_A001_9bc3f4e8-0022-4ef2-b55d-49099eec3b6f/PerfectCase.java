public class PerfectCase {
    // Fully correct logic to pass all test cases
    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public double divide(int a, int b) {
        if (b == 0) throw new IllegalArgumentException("Division by zero not allowed");
        return (double) a / b;
    }
}
