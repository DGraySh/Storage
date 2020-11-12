package ru.gb.cloud_storage.storage_client;

import io.netty.channel.Channel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.cloud_storage.storage_common.ByteBufSender;
import ru.gb.cloud_storage.storage_common.CallMeBack;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class RemoteListView extends ListView<String> {

    private static final Logger logger = LogManager.getLogger(ProtoHandler.class);
    private final Path userRootDir;
    private final ObservableList<String> childrenList = FXCollections.observableArrayList();
    private Path requestedDir;
//    private final WatchService remoteWatchService;


    public RemoteListView(String path) {
        super();
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userRootDir = Paths.get(path);
        requestedDir = userRootDir;

        Platform.runLater(() -> {
            try {
                requestFileList(initChannel((childrenList::addAll))); //receive FL from server
                setItems(childrenList);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

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

//        remoteWatchService = new WatchService(this);
//        refresh();
    }

//        mTextField = new TextField();
//        mTextField.setStyle("-fx-font-size: 10px;");

/*

        setOnMouseClicked(m -> {
            if (m.getButton().equals(MouseButton.PRIMARY) && m.getClickCount() == 2)
                navigate(getSelectionModel().getSelectedItem());
        });
        setCellFactory(list -> new SystemIconsHelper.AttachmentListCell(ru.gb.cloud_storage.storage_client.UIX.ListView.this));
        mWatchServiceHelper = new WatchServiceHelper(this);
        refresh();
*/


    @Override
    public void refresh() {
        childrenList.clear();
//        for (int i = 0; i < 100; i++) {
        try {
            requestFileList(initChannel(childrenList::addAll));
        } catch (InterruptedException e) {
            e.printStackTrace();
//            }
        }
    }
//        remoteWatchService.changeObservableDirectory(requestedDir.toPath());

    public Path getDirectory() {
        return requestedDir;
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

    private void navigate(String name) {
        String selectedPath = requestedDir + File.separator + name;
        Path selectedFile = Paths.get(selectedPath);
        if (selectedFile.toFile().isDirectory()) {
            try {
                requestedDir = selectedFile.toRealPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            refresh();
        } else {
            try {
                Desktop.getDesktop().open(selectedFile.toFile());
            } catch (Exception e) {
//                DialogHelper.showException(e);//TODO
            }
        }
    }

    private void back() {
        Path parent = requestedDir.getParent();
        if ((parent != null) && (parent.equals(userRootDir))) {
            requestedDir = parent;
            if (requestedDir.toFile().exists()) {
                refresh();
            } else {
                back();
            }
        }
    }


    public Channel initChannel(CallMeBack cb) throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter, cb)).start();
        networkStarter.await();
        return Network.getInstance().getCurrentChannel();
    }

    public void requestFileList(Channel channel) {
            ByteBufSender.sendFileOpt(channel, (byte) 50);
            ByteBufSender.sendFileName(channel, requestedDir);
    }

}
