/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.piscicultura.monitoreo;

/**
 *
 * @author Cristian
 */


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PisciculturaMonitoreo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piscicultura/monitoreo/view/Login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Sistema de Monitoreo - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
