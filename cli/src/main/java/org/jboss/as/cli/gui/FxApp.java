package org.jboss.as.cli.gui;

import java.io.InputStream;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jboss.as.cli.CommandContext;

public class FxApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        FxUtil.init(this);

        Pane root = FxUtil.getInstance().loadResource(getClass(),"FxGuiMain.fxml");
        InputStream iconStream = getClass().getResourceAsStream("/icon/wildfly.png");

        FxCliGuiContext.getInstance().setMainPanel(root);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("CLI GUI");
        primaryStage.getIcons().add(new Image(iconStream));

        primaryStage.show();
    }

    public static void start(CommandContext cmdCtx) {
        FxCliGuiContext.getInstance().setCommandContext(cmdCtx);
        FxCommandExecutor executor = new FxCommandExecutor(null);
        FxCliGuiContext.getInstance().setExecutor(executor);
        launch();
    }
}
