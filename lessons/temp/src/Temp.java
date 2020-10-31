import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class Temp {
    public static void main(String[] args) throws IOException {

        //Files.createFile(Paths.get("1/3.txt"));
//        Files.createDirectory(Paths.get("1/2/31.txt"));
        String filename = "111.txt";
        Path path = Paths.get("./dir");
        Files.createDirectories(path.getParent());
//        System.out.println(path.resolveSibling("2"));
        System.out.println(path.toRealPath());
        System.out.println(path.resolve(filename).toString());


//        if (Files.notExists(path1)){
//            try
//                (FileWriter wr = new FileWriter(path1.toString())){
//
//        }
//        }
/*
        ArrayList<Path> list = new ArrayList<>();
        try {
            Files.walkFileTree(Paths.get("."), new HashSet<>(Collections.singletonList(FileVisitOption.FOLLOW_LINKS)),
                    *//*Integer.MAX_VALUE*//* 1, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            list.add(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException e) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        for (Path path : list) {
            out.writeUTF(path.toString());
        }
        byte[] bytes = baos.toByteArray();*/
//        System.out.println(Arrays.toString(bytes));
//        System.out.println(baos);
//        System.out.println(out.toString());

//        static class MyFileVisitor extends SimpleFileVisitor<Path> {
//            @Override
//            public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) {
//                System.out.println(path);
//                return FileVisitResult.CONTINUE;
//            }
//        }
//        byte[] fileContent = Files.readAllBytes(file.toPath());
    }
}