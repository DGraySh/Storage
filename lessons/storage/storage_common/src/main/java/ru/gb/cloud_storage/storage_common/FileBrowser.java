package ru.gb.cloud_storage.storage_common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileBrowser {

    private File userRootDir;//TODO take it from auth

    public FileBrowser(String userRootDir) {
        try {
            this.userRootDir = new File(userRootDir).getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFileList() {

        File[] listFiles = userRootDir.listFiles(file -> !file.isHidden());

        if (listFiles == null) {
            listFiles = new File[0];
        }

        //sort list of files
        Arrays.sort(listFiles, (f1, f2) -> {
            if ((f1.isDirectory() && f2.isDirectory()) || (f1.isFile() && f2.isFile())) {
                return f1.compareTo(f2);
            }
            return f1.isDirectory() ? -1 : 1;
        });

        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < listFiles.length; i++) {
            list.add(i, listFiles[i].getName());
        }
        /*ArrayList<String> list = new ArrayList<>();
        list.add(0, "..");
        for (int i = 0; i < listFiles.length; i++) {
            list.add(i + 1, listFiles[i].getName());
        }*/
        System.out.println(list);
        return list;
    }

    public List<String> getFileList(String dir) {

        File requestedDir = null;
        try {
            requestedDir = new File(dir).getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("userRootDir " +userRootDir);
        System.out.println("requestedDir " +requestedDir);

        if (userRootDir.compareTo(requestedDir) == 0) {
            return getFileList();
        } else {

            File[] listFiles = requestedDir.listFiles(file -> !file.isHidden());

            if (listFiles == null) {
                listFiles = new File[0];
            }

            //sort list of files
            Arrays.sort(listFiles, (f1, f2) -> {
                if ((f1.isDirectory() && f2.isDirectory()) || (f1.isFile() && f2.isFile())) {
                    return f1.compareTo(f2);
                }
                return f1.isDirectory() ? -1 : 1;
            });
//        if (userRootDir.compareTo(requestedDir) != 0) {
//            ArrayList<String> list = new ArrayList<>();//new String[listFiles.length + 1];
//            list.add(0, "..");
//            for (int i = 0; i < listFiles.length; i++) {
//                list.add(i + 1, listFiles[i].getName());
//            }
//            return list;
//        }
//        else {
            ArrayList<String> list = new ArrayList<>();
            list.add(0, "..");
            for (int i = 0; i < listFiles.length; i++) {
                list.add(i + 1, listFiles[i].getName());
            }
            System.out.println(list);
            return list;
        }
    }
}
