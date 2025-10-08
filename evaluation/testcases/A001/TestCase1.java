import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestCase1 {

    @Test
    public void testAddition() throws Exception {
        File tempRoot = new File(System.getProperty("java.io.tmpdir"));
        File evalDir = findLatestEvalFolder(tempRoot);
        System.out.println("Found evaluation folder: " + evalDir);

        if (evalDir == null) {
            throw new RuntimeException("Could not find evaluation folder in temp directory!");
        }

        Class<?> studentClass = loadStudentClass(evalDir);
        Object obj = studentClass.getDeclaredConstructor().newInstance();
        var addMethod = studentClass.getMethod("add", int.class, int.class);
        int result = (int) addMethod.invoke(obj, 2, 3);

        Assertions.assertEquals(5, result, "Addition logic is incorrect!");
        System.out.println("Test passed successfully!");
    }

    private File findLatestEvalFolder(File tempRoot) {
        File[] evalDirs = tempRoot.listFiles((dir, name) -> name.startsWith("eval_"));
        if (evalDirs == null || evalDirs.length == 0) return null;

        File latest = evalDirs[0];
        for (File dir : evalDirs) {
            if (dir.lastModified() > latest.lastModified()) {
                latest = dir;
            }
        }
        return latest;
    }

    private Class<?> loadStudentClass(File evalDir) throws Exception {
        Path studentClassPath = Files.walk(evalDir.toPath())
                .filter(p -> p.toString().endsWith(".class"))
                .filter(p -> !p.getFileName().toString().contains("TestCase"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No compiled student class found!"));

        String className = studentClassPath.getFileName().toString().replace(".class", "");
        System.out.println("Found compiled class: " + className);

        URLClassLoader loader = new URLClassLoader(new URL[]{evalDir.toURI().toURL()});
        return loader.loadClass(className);
    }
}
