public class FullPassProgram {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
    
    public static void main(String[] args) {
        FullPassProgram obj = new FullPassProgram();
        System.out.println("Addition: " + obj.add(5, 3));
        System.out.println("Subtraction: " + obj.subtract(5, 3));
    }
}
