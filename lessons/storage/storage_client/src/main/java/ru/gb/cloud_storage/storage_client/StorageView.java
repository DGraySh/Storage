package ru.gb.cloud_storage.storage_client;

import io.netty.channel.Channel;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.gb.cloud_storage.storage_common.ByteBufSender;
import ru.gb.cloud_storage.storage_common.CallMeBack;

import java.util.concurrent.CountDownLatch;

public class StorageView extends HBox{

    public StorageView() throws InterruptedException {

        LocalListView leftListView = new LocalListView(".");
        RemoteListView rightListView = new RemoteListView("user_dir_on_server");

/*
        ObservableList<String> leftChildrenList = FXCollections.observableArrayList();
//        ObservableList<String> rightChildrenList = FXCollections.observableArrayList();

        requestFileList(initChannel((rightChildrenList::addAll))); //receive FL from server

        leftChildrenList.addAll(new FileBrowser(".").getFileList());

        leftListView.setItems(leftChildrenList);
        rightListView.setItems(rightChildrenList);
*/

        VBox vboxL = new VBox(leftListView);
        VBox vboxR = new VBox(rightListView);

        VBox.setVgrow(leftListView, Priority.ALWAYS);
        VBox.setVgrow(rightListView, Priority.ALWAYS);

        getChildren().addAll(vboxL, vboxR);

        HBox.setHgrow(vboxL, Priority.ALWAYS);
        HBox.setHgrow(vboxR, Priority.ALWAYS);

    }

/*
    public void copy() {
        if (leftListView.isFocused()) {
            List<Path> source = leftListView.getSelection();
            Path target = rightListView.getDirectory();
            FileHelper.copy(source, target);
        } else if (rightListView.isFocused()) {
            List<Path> source = rightListView.getSelection();
            Path target = leftListView.getDirectory();
            FileHelper.copy(source, target);
        }
    }

    public void move() {
        if (mLeftPane.isFocused()) {
            List<Path> source = mLeftPane.getSelection();
            Path target = mRightPane.getDirectory();
            FileHelper.move(source, target);
        } else if (mRightPane.isFocused()) {
            List<Path> source = mRightPane.getSelection();
            Path target = mLeftPane.getDirectory();
            FileHelper.move(source, target);
        }
    }

    public void delete() {
        LocalListView focusedPane = getFocusedPane();
        if (focusedPane != null) FileHelper.delete(focusedPane.getSelection());
    }

    public void rename() {
        ru.gb.cloud_storage.storage_client.UIX.ListView focusedPane = getFocusedPane();
        if (focusedPane != null) {
            List<Path> selection = focusedPane.getSelection();
            if (selection.size() == 1) FileHelper.rename(selection.get(0));
        }
    }

    public void createDirectory() {
        ru.gb.cloud_storage.storage_client.UIX.ListView focusedPane = getFocusedPane();
        if (focusedPane != null) FileHelper.createDirectory(focusedPane.getDirectory());
    }

    public void createFile() {
        ru.gb.cloud_storage.storage_client.UIX.ListView focusedPane = getFocusedPane();
        if (focusedPane != null) FileHelper.createFile(focusedPane.getDirectory());
    }

    public void focusTextField() {
        ru.gb.cloud_storage.storage_client.UIX.ListView focusedPane = getFocusedPane();
        if (focusedPane != null) focusedPane.getTextField().requestFocus();
    }

*/
/*
    public void openHtml() {
        ListView focusedPane = getFocusedPane();
        if (focusedPane == null) return;
        List<Path> selection = focusedPane.getSelection();
        if (selection.size() != 1) return;
        File file = selection.get(0).toFile();
        mTextEditor.open(file);
    }
*//*


    public void countWords() {
        Path path = getSelectedPath();
        if (path != null && path.toString().endsWith(".txt")) {
            Path resultPath = path.getParent().resolve("[Word Count] " + path.getFileName());
            try (PrintWriter printWriter = new PrintWriter(resultPath.toFile())) {
                Arrays.stream(new String(Files.readAllBytes(path), StandardCharsets.UTF_8).toLowerCase().split("\\W+"))
                        .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()))
                        .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .forEach(printWriter::println);
                Desktop.getDesktop().open(resultPath.toFile());
            } catch (IOException e) {
                DialogHelper.showException(e);
            }
        }
    }

    private LocalListView getFocusedPane() {
        if (mLeftPane.isFocused() || mLeftPane.getTextField().isFocused()) {
            return mLeftPane;
        } else if (mRightPane.isFocused() || mRightPane.getTextField().isFocused()) {
            return mRightPane;
        } else {
            return null;
        }
    }

    private ru.gb.cloud_storage.storage_client.UIX.ListView getFocusedPane(TextField textField) {
        if (textField == mLeftPane.getTextField()) {
            return mLeftPane;
        } else {
            return mRightPane;
        }
    }

    @Nullable
    private Path getSelectedPath() {
        ru.gb.cloud_storage.storage_client.UIX.ListView focusedPane = getFocusedPane();
        if (focusedPane == null) return null;
        List<Path> selection = focusedPane.getSelection();
        if (selection.size() != 1) return null;
        return selection.get(0);
    }
*/


//    public static Channel initChannel(CallMeBack cb) throws InterruptedException {
//        CountDownLatch networkStarter = new CountDownLatch(1);
//        new Thread(() -> Network.getInstance().start(networkStarter, cb)).start();
//        networkStarter.await();
//        return Network.getInstance().getCurrentChannel();
//    }

/*
    public static void requestFileList(Channel channel) {
        ByteBufSender.sendFileOpt(channel, (byte) 50);
    }
*/

}
