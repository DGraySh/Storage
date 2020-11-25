package ru.gb.cloud_storage.storage_client;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ClientApp extends Application {

    private StorageView storageView;

    public static void main(String[] args) {
        Application.launch(args);
        /*CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter, null)).start();
        try {
            networkStarter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void start(Stage primaryStage) throws InterruptedException {

        storageView = new StorageView();

        VBox rootVBox = new VBox(storageView);
        VBox.setVgrow(storageView, Priority.ALWAYS);

        Scene scene = new Scene(rootVBox, 840, 600);
        primaryStage.setTitle("CloudStorage");

        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }
}
