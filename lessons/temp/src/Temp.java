import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Temp {
    public static void main(String[] args) throws IOException {
//        System.out.println(Files.walkFileTree(Paths.get("."), EnumSet.noneOf(FileVisitOption.class),
//                1, new MyFileVisitor()));
        //Files.walkFileTree(Paths.get("."), EnumSet.noneOf(FileVisitOption.class),
               // 1, new MyFileVisitor());
//        List<Path> list = Files.walkFileTree(Paths.get("."), EnumSet.noneOf(FileVisitOption.class),
//                1, new MyFileVisitor()).
//                .filter(Files::isRegularFile)
//                .collect(Collectors.toList());
//        System.out.println(list.toString());

//        List<Path> list = null;
//        try {
//            Path path = Paths.get(".");
////            list = Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class),1, new MyFileVisitor())//collect(Collectors.toList());
//        } catch (IOException e) {
//            // TODO implement better exception handling
//            e.printStackTrace();
//        }
//        System.out.println(list);

        //Files.walkFileTree(Paths.get("."), new MyFileVisitor());
    }

    static class MyFileVisitor extends SimpleFileVisitor<Path>{
        @Override
        public FileVisitResult visitFile (Path path, BasicFileAttributes attribs) {
            System.out.println(path);
            return FileVisitResult.CONTINUE;
        }
    }
}