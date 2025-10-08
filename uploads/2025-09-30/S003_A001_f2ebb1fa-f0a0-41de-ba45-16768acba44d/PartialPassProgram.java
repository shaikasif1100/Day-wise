public class PartialPassProgram {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        // Intentional logic mistake: returns sum instead of difference
        return a + b;
    }
    
    public static void main(String[] args) {
        PartialPassProgram obj = new PartialPassProgram();
        System.out.println("Addition: " + obj.add(5, 3));
        System.out.println("Subtraction: " + obj.subtract(5, 3));
    }
}
