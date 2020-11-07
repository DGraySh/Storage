package ru.gb.cloud_storage.storage_common;

import javafx.scene.control.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileBrowser extends ListView<String> {

    File dir;

    public FileBrowser(String dir) {
        this.dir = new File(dir);
    }

    public List<String> getFileList() {

        File[] listFiles = dir.listFiles(file -> !file.isHidden());

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

        ArrayList<String> list = new ArrayList<>();//new String[listFiles.length + 1];
        list.add(0,"..");
        for (int i = 0; i < listFiles.length; i++) {
            list.add(i + 1, listFiles[i].getName());
        }
        return list;
    }
}
