package org.jboss.as.cli.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.util.Pair;

public class FxUtil {

    private static final FxUtil INSTANCE = new FxUtil();

    private FxUtil(){}

    public static FxUtil getInstance(){
        if(INSTANCE.application == null){
            throw new RuntimeException("Utility has not been initialized!");
        }
        return INSTANCE;
    }

    public <T> T loadResource(Class c, String resource){
        try {
            return FXMLLoader.load(c.getResource(resource));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <R, C> Pair<R, C> loadResourceControllerPair(Class c, String resource){
        try{
            FXMLLoader loader = new FXMLLoader(c.getResource(resource));
            return new Pair<>(loader.load(), loader.getController());
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void open(String link) {
        INSTANCE.application.getHostServices().showDocument(link);
    }

    public static void init(Application application) {
        INSTANCE.application = application;
    }

    private Application application;
}
