package org.jboss.as.cli.gui.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class FxAbstractDeployment {
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getRuntimeName() {
        return runtimeName.get();
    }

    public StringProperty runtimeNameProperty() {
        return runtimeName;
    }

    public void setRuntimeName(String runtimeName) {
        this.runtimeName.set(runtimeName);
    }

    private StringProperty name = new SimpleStringProperty();

    private StringProperty runtimeName = new SimpleStringProperty();
}
