package ru.gb.cloud_storage.storage_client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import ru.gb.cloud_storage.storage_common.FileBrowser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class LocalListView extends ListView<String> {

    ObservableList<String> childrenList = FXCollections.observableArrayList();
    private File dir;

    public LocalListView(String path) {
        super();
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        try {
            dir = new File(path).getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        childrenList.addAll(new FileBrowser(dir.toString()).getFileList());
        childrenList.addAll(new FileBrowser("/").getFileList(dir.toString()));
        setItems(childrenList);

        setOnKeyPressed(key -> {
            switch (key.getCode()) {
                case ENTER:
//                    if (isFocused()) navigate(getSelectionModel().getSelectedItem());
                    if (isFocused()) navigate(Paths.get(getSelectionModel().getSelectedItem()));
                    break;
                case BACK_SPACE:
                    back();
                    break;
            }
        });

        setOnMouseClicked(m -> {
            if (m.getButton().equals(MouseButton.PRIMARY) && m.getClickCount() == 2)
//                navigate(getSelectionModel().getSelectedItem());
                navigate(Paths.get(getSelectionModel().getSelectedItem()));
        });


/*


        setCellFactory(list -> new SystemIconsHelper.AttachmentListCell(ru.gb.cloud_storage.storage_client.UIX.ListView.this));
        mWatchServiceHelper = new WatchServiceHelper(this);
        refresh();
*/
    }


    @Override
    public void refresh() {
        childrenList.clear();
//        childrenList.addAll(new FileBrowser(dir.toString()).getFileList());
        childrenList.addAll(new FileBrowser("/").getFileList(dir.toString()));
    }

/*
    public List<Path> getSelection() {
        List<Path> selection = new ArrayList<>();
        for (String item : getSelectionModel().getSelectedItems()) {
            selection.add(dir.toPath().resolve(item));
        }
        return selection;
    }
*/

    /*
        public TextField getTextField() {
            return mTextField;
        }
    
    
        public Path getDirectory() {
            return dir.toPath();
        }
    
        public void select(String regex) {
            if (regex.startsWith("*")) regex = "." + regex;
            getSelectionModel().clearSelection();
            for (int i = 0; i < mChildrenList.size(); ++i) {
                String item = mChildrenList.get(i);
                if (item.matches(regex) || StringHelper.containsWord(item, regex)) {
                    getSelectionModel().select(i);
                }
            }
        }
    
       
        private void showList(String[] list) {
            if (list != null) {
                mChildrenList.clear();
                mChildrenList.addAll(list);
            } else {
                mChildrenList.clear();
            }
        }
    
        public void openFile(File file) {
            if (!file.exists()) {
                refresh();
                return;
            }
            if (file.isDirectory()) {
                dir = file;
                refresh();
            } else if (file.isFile()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (Exception e) {
                    DialogHelper.showException(e);
                }
            }
        }
    

    */
/*
    private String[] getCurrentFilesList() {
        File[] listFiles = dir.listFiles(file -> !file.isHidden());

        if (listFiles == null) {
            listFiles = new File[0];
        }

        Arrays.sort(listFiles, (f1, f2) -> {
            if ((f1.isDirectory() && f2.isDirectory()) || (f1.isFile() && f2.isFile())) {
                return f1.compareTo(f2);
            }
            return f1.isDirectory() ? -1 : 1;
        });

        String[] list = new String[listFiles.length];
        for (int i = 0; i < list.length; ++i) {
            list[i] = listFiles[i].getName();
        }

        return list;
    }
*/
    private void navigate(Path path) {
        Path selectedPath = Paths.get(dir.getAbsolutePath() + File.separator + path);
        File selectedFile = selectedPath.toFile();

        if (selectedFile.isDirectory()) {
            dir = selectedFile;
            refresh();
        } else {
            try {
                Desktop.getDesktop().open(selectedFile);
            } catch (Exception e) {
//                DialogHelper.showException(e);//TODO
            }
        }
    }

    private void back() {
        File parent = dir.getParentFile();
        if (parent != null) {
            dir = parent;
            if (dir.exists()) {
                refresh();
            } else {
                back();
            }
        }
    }

}
