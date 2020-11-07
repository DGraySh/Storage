package ru.gb.cloud_storage.storage_client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import ru.gb.cloud_storage.storage_client.UIX.DialogHelper;
import ru.gb.cloud_storage.storage_client.UIX.StringHelper;
import ru.gb.cloud_storage.storage_client.UIX.SystemIconsHelper;
import ru.gb.cloud_storage.storage_client.UIX.WatchServiceHelper;
import ru.gb.cloud_storage.storage_common.FileBrowser;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalListView extends ListView<String> {

    private File dir;
    ObservableList<String> childrenList = FXCollections.observableArrayList();

    public LocalListView(String path) {
        super();
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dir = new File(path);

        childrenList.addAll(new FileBrowser(".").getFileList());
        setItems(childrenList);


/*
        setOnKeyPressed(key -> {
            switch (key.getCode()) {
                case ENTER:
                    if (isFocused()) navigate(getSelectionModel().getSelectedItem());
                    break;
                case BACK_SPACE:
                    back();
                    break;
            }
        });

        setOnMouseClicked(m -> {
            if (m.getButton().equals(MouseButton.PRIMARY) && m.getClickCount() == 2)
                navigate(getSelectionModel().getSelectedItem());
        });
        setCellFactory(list -> new SystemIconsHelper.AttachmentListCell(ru.gb.cloud_storage.storage_client.UIX.ListView.this));
        mWatchServiceHelper = new WatchServiceHelper(this);
        refresh();
*/
    }


    @Override
    public void refresh() {

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
        return mDirectory.toPath();
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

    private String[] getCurrentFilesList() {
        File[] listFiles = mDirectory.listFiles(file -> !file.isHidden());

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
            mDirectory = file;
            refresh();
        } else if (file.isFile()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception e) {
                DialogHelper.showException(e);
            }
        }
    }

    private void navigate(String name) {
        String selectedPath = mDirectory.getAbsolutePath() + File.separator + name;
        File selectedFile = new File(selectedPath);
        if (selectedFile.isDirectory()) {
            mDirectory = selectedFile;
            refresh();
        } else {
            try {
                Desktop.getDesktop().open(selectedFile);
            } catch (Exception e) {
                DialogHelper.showException(e);
            }
        }
    }

    private void back() {
        File parent = mDirectory.getParentFile();
        if (parent != null) {
            mDirectory = parent;
            if (mDirectory.exists()) {
                refresh();
            } else {
                back();
            }
        }
    }
*/

}
