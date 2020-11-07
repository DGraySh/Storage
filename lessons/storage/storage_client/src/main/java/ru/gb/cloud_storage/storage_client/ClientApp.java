package ru.gb.cloud_storage.storage_client;


import io.netty.channel.Channel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.gb.cloud_storage.storage_common.ByteBufSender;
import ru.gb.cloud_storage.storage_common.FileBrowser;

import java.util.concurrent.CountDownLatch;


public class ClientApp extends Application {

    private StorageView storageView;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws InterruptedException {

        storageView = new StorageView();

        VBox rootVBox = new VBox(storageView);
        VBox.setVgrow(storageView, Priority.ALWAYS);

        Scene scene = new Scene(rootVBox, 840, 600);
        primaryStage.setTitle("Ololo");

        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }


    public static Channel initChannel(CallMeBack cb) throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter, cb)).start();
        networkStarter.await();
        return Network.getInstance().getCurrentChannel();
    }

    public static void requestFileList(Channel channel) {
        ByteBufSender.sendFileOpt(channel, (byte) 50);
    }


//    public static refresh
}
