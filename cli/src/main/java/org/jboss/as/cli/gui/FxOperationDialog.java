/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.cli.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import org.jboss.as.cli.gui.FxManagementModelNode.UserObject;
import org.jboss.as.cli.gui.component.FxBrowsePanel;
import org.jboss.as.cli.gui.component.FxListEditor;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;

/**
 * JDialog that allows the user to specify the params for an operation.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxOperationDialog extends Dialog implements Consumer{

    private FxCliGuiContext cliGuiCtx;
    private FxManagementModelNode node;
    private String opName;
    private SortedSet<RequestProp> props;

    public FxOperationDialog(FxCliGuiContext cliGuiCtx, FxManagementModelNode node, String opName, String strDescription, ModelNode requestProperties) {
        super();
        this.initOwner(cliGuiCtx.getMainWindow());
        this.setTitle(opName);
        this.initModality(Modality.APPLICATION_MODAL);
        this.cliGuiCtx = cliGuiCtx;
        this.node = node;
        this.opName = opName;

        try {
            setProps(requestProperties);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        BorderPane borderPane = new BorderPane();

        Label opDescription = new Label(strDescription);
        opDescription.setWrapText(true);
        opDescription.setMaxWidth(400);
        borderPane.setTop(opDescription);

        borderPane.setCenter(makeInputPanel());
        makeButtonPanel();

        this.getDialogPane().setContent(borderPane);

//        contentPane.setLayout(new BorderLayout(10, 10));

        setResizable(true);

    }

    public void setVisible(boolean isVisible) {
        if (node.isLeaf()) {
            // "rightSide" field should have focus for write-attribute dialog
            // where "leftSide" field is already populated
            for (RequestProp prop : props) {
                if (prop.getName().equals("value")) {
                    prop.getValueComponent().requestFocus();
                }
            }
        }

        super.show();
    }

    /**
     * Set the value of the underlying component.  Note that this will
     * not work for ListEditor components.  Also, note that for a JComboBox,
     * The value object must have the same identity as an object in the drop-down.
     *
     * @param propName The DMR property name to set.
     * @param value    The value.
     */
    public void setValue(String propName, Object value) {
        for (RequestProp prop : props) {
            if (prop.getName().equals(propName)) {
                Node valComp = prop.getValueComponent();
                if (valComp instanceof TextInputControl) {
                    ((TextInputControl) valComp).setText(value.toString());
                }
                if (valComp instanceof Toggle) {
                    ((Toggle) valComp).setSelected((Boolean) value);
                }
                if (valComp instanceof ComboBox) {
                    ((ComboBox) valComp).getSelectionModel().select(value);
                }
                return;
            }
        }
    }

    private void setProps(ModelNode requestProperties) throws Exception {
        props = new TreeSet<>();
        if (opName.equals("add")) {
            UserObject usrObj = node.getValue();
            props.add(new RequestProp("/" + usrObj.getName() + "=<name>/", "Resource name for the new " + usrObj.getName(), true, ModelType.STRING));
        }

        if (opName.equals("write-attribute") && node.isLeaf()) {
            ModelNode nameNode = requestProperties.get("name");
            nameNode.get("type").set(ModelType.UNDEFINED); // undefined type will display as uneditable String
            UserObject usrObj = node.getValue();
            ModelNode nameNodeValue = new ModelNode();
            nameNodeValue.set(usrObj.getName());
            props.add(new RequestProp("name", requestProperties.get("name"), nameNodeValue));

            ModelNode rscDesc = cliGuiCtx.getExecutor().doCommand(node.addressPath() + ":read-resource-description");
            ModelNode valueNode = rscDesc.get("result", "attributes", usrObj.getName());
            valueNode.get("required").set(false); // value is never required for write-attribute
            ModelNode valueNodeValue = usrObj.getBackingNode().get(usrObj.getName());
            props.add(new RequestProp("value", valueNode, valueNodeValue));
            return;
        }

        for (Property prop : requestProperties.asPropertyList()) {
            props.add(new RequestProp(prop.getName(), prop.getValue(), null));
        }
    }

    private ScrollPane makeInputPanel() {
        boolean hasRequiredFields = false;
        GridPane inputPane = new GridPane();
        inputPane.setOpaqueInsets(new Insets(5, 5, 5, 5));
        GridPane inputPanel = new GridPane();
//        GridBagConstraints gbConst = new GridBagConstraints();
//        gbConst.anchor = GridBagConstraints.WEST;
//        gbConst.insets = ;

        int row = 0;
        for (RequestProp prop : props) {
            int column = 0;
            Label label = prop.getLabel();
//            gbConst.gridwidth = 1;
            inputPanel.add(label, column++, row);

//            inputPanel.add(Box.createHorizontalStrut(5));

            Node comp = prop.getValueComponent();
//            gbConst.gridwidth = GridBagConstraints.REMAINDER;
            inputPanel.add(comp, column, row);

            if (prop.isRequired) hasRequiredFields = true;
            row++;
        }

        if (hasRequiredFields) {
            inputPanel.add(new Label(" * = Required Field"), 0, row);
        }

        return new ScrollPane(inputPanel);
    }

    private void makeButtonPanel() {
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().addAll(ok, cancel);
    }

    @Override
    public void accept(Object o) {
        String addressPath = this.node.addressPath();
        if (this.opName.equals("add")) {
            FxManagementModelNode.UserObject usrObj = this.node.getValue();
            FxManagementModelNode parent = (FxManagementModelNode) this.node.getParent();
            RequestProp resourceProp = this.props.first();
            String value = resourceProp.getValueAsString();
            value = ManagementModelNode.escapeAddressElement(value);
            addressPath = parent.addressPath() + usrObj.getName() + "=" + value + "/";
            this.props.remove(resourceProp);
        }

        StringBuilder command = new StringBuilder();
        command.append(addressPath);
        command.append(":");
        command.append(this.opName);
        addRequestProps(command, this.props);

        cliGuiCtx.getCmdText().set(command.toString());
//        cmdText.requestFocus();
    }

    private void addRequestProps(StringBuilder command, SortedSet<RequestProp> reqProps) {
        boolean addedProps = false;
        command.append("(");
        for (RequestProp prop : reqProps) {
            String submittedValue = prop.getValueAsString();

            if (submittedValue == null) continue;
            if (submittedValue.equals("")) continue;

            // Don't display boolean values that are already the default.
            // This only works if the default value is provided by read-operation-description.
            if (prop.type == ModelType.BOOLEAN && !prop.expressionsAllowed) {
                ModelNode defaultValue = prop.getDefaultValue();
                if ((defaultValue != null) && (defaultValue.asBoolean() == Boolean.parseBoolean(submittedValue))) continue;
            }

            addedProps = true;
            command.append(prop.getName());
            command.append("=");
            command.append(submittedValue);

            command.append(",");
        }

        if (addedProps) {
            // replace training comma with close paren
            command.replace(command.length()-1, command.length(), ")");
        } else {
            // remove opening paren
            command.deleteCharAt(command.length() - 1);
        }
    }

    /**
     * Request property class.  This class contains all known information about each operation attribute.
     * <p>
     * It is also responsible for building the input component for the attribute.
     */
    private class RequestProp implements Comparable {
        private final String name;
        private ModelNode props;
        private ModelType type;
        private String description;
        private boolean isRequired = false;
        private boolean nillable = false;
        private boolean expressionsAllowed = false;
        private ModelNode defaultValue = null;
        private ModelNode value = null;

        private Label label;
        private Node valueComponent;

        private boolean isResourceName = false;

        /**
         * Constructor used for resource name property.
         *
         * @param name        Property name
         * @param description Description for tool tip text.
         * @param required    Is this an isRequired property?
         */
        public RequestProp(String name, String description, boolean required, ModelType type) {
            this.name = name;
            this.props = new ModelNode();
            this.description = description;
            this.type = type;
            this.isRequired = required;
            this.isResourceName = true;
            setInputComponent();
            setInputComponentValue();
        }

        public RequestProp(String name, ModelNode props, ModelNode value) {
            this.name = name;
            this.props = props;
            this.value = value;
            this.type = props.get("type").asType();

            if (props.hasDefined("description")) {
                this.description = props.get("description").asString();
            }

            if (props.hasDefined("required")) {
                this.isRequired = props.get("required").asBoolean();
            }

            if (props.hasDefined("nillable")) {
                this.nillable = props.get("nillable").asBoolean();
            }

            if (props.hasDefined("expressions-allowed")) {
                this.expressionsAllowed = props.get("expressions-allowed").asBoolean();
            }

            if (props.hasDefined("default")) {
                this.defaultValue = props.get("default");
            }

            setInputComponent();
            setInputComponentValue();
        }

        public String getName() {
            return name;
        }

        public Node getValueComponent() {
            return valueComponent;
        }

        public Label getLabel() {
            return this.label;
        }

        public ModelNode getDefaultValue() {
            return this.defaultValue;
        }

        public String getValueAsString() {
            if (valueComponent instanceof Label) {
                return ((Label) valueComponent).getText();
            }

            if (valueComponent instanceof TextInputControl) {
                return ((TextInputControl) valueComponent).getText();
            }

            if (valueComponent instanceof Toggle) {
                return Boolean.toString(((Toggle) valueComponent).isSelected());
            }

            if (valueComponent instanceof ComboBox) {
                return ((ComboBox) valueComponent).getSelectionModel().getSelectedItem().toString();
            }

            if (valueComponent instanceof FxListEditor) {
                ModelNode list = ((FxListEditor) valueComponent).getValue();
                if (list.isDefined()) return list.asString();
                return "";
            }

            if (valueComponent instanceof Label) {
                return ((Label) valueComponent).getText();
            }

            if (valueComponent instanceof FxBrowsePanel) {
                return ((FxBrowsePanel) valueComponent).getText();
            }

            return null;
        }

        private void setInputComponent() {
            this.label = makeLabel();
            if (type == ModelType.BOOLEAN && !expressionsAllowed) {
                this.valueComponent = new CheckBox(makeLabelString(false));
                ((CheckBox)this.valueComponent).setTooltip(new Tooltip(description));
                this.label = new Label(); // checkbox doesn't need a label
            } else if (type == ModelType.UNDEFINED) {
                Label jLabel = new Label();
                this.valueComponent = jLabel;
            } else if (props.get("allowed").isDefined()) {
                ComboBox<String> comboBox = makeJComboBox(props.get("allowed").asList());
                this.valueComponent = comboBox;
            } else if (type == ModelType.LIST) {
                FxListEditor listEditor = new FxListEditor();
                this.valueComponent = listEditor;
            } else if (type == ModelType.BYTES) {
                this.valueComponent = new FxBrowsePanel(FxOperationDialog.this);
            } else {
                TextField textField = new TextField(); //length of 30
                this.valueComponent = textField;
            }
        }

        private void setInputComponentValue() {
            ModelNode valueToSet = defaultValue;
            if (value != null) valueToSet = value;
            if (valueToSet == null) return;

            if (valueComponent instanceof Label) {
                ((Label) valueComponent).setText(valueToSet.asString());
            }

            if (valueComponent instanceof FxListEditor) {
                ((FxListEditor) valueComponent).setValue(valueToSet);
            }

            if (!valueToSet.isDefined()) return;

            if (valueComponent instanceof TextInputControl) {
                ((TextInputControl) valueComponent).setText(valueToSet.asString());
            }

            if (valueComponent instanceof CheckBox) {
                ((CheckBox) valueComponent).setSelected(valueToSet.asBoolean());
            }

            if (valueComponent instanceof ComboBox) {
                ((ComboBox) valueComponent).getSelectionModel().select(valueToSet.asString());
            }

            if (valueComponent instanceof FxBrowsePanel) {
                ((FxBrowsePanel) valueComponent).setText(valueToSet.asString());
            }
        }

        private String makeLabelString(boolean addColon) {
            String labelString = name;
            if (addColon) labelString += ":";
            if (isRequired) labelString += " *";
            return labelString;
        }

        private Label makeLabel() {
            Label label = new Label(makeLabelString(true));
            label.setTooltip(new Tooltip(description));
            return label;
        }

        private ComboBox<String> makeJComboBox(List<ModelNode> values) {
            List<String> valueVector = new ArrayList<>(values.size());
            if (!isRequired) {
                valueVector.add("");
            }

            for (ModelNode node : values) {
                valueVector.add(node.asString());
            }
            return new ComboBox<>(FXCollections.observableList(valueVector));
        }

        // fill in form fields for write-attribute when an attribute node is selected.
        private void setWriteAttributeValues() {
            if (!FxOperationDialog.this.node.isLeaf()) return;
            if (!FxOperationDialog.this.opName.equals("write-attribute")) return;

            UserObject usrObj = FxOperationDialog.this.node.getValue();

            if (this.name.equals("name")) {
                ((TextField) valueComponent).setText(usrObj.getName());
                return;
            }

            if (usrObj.getValue().equals("undefined")) return;

            if (this.name.equals("value")) ((TextField) valueComponent).setText(usrObj.getValue());
        }

        @Override
        public int compareTo(Object t) {
            if (this.equals(t)) return 0;
            if (this.isResourceName) return -1;
            RequestProp compareTo = (RequestProp) t;
            if (this.isRequired && compareTo.isRequired) return 1;
            if (this.isRequired) return -1;
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof RequestProp)) return false;
            RequestProp compareTo = (RequestProp) obj;
            return this.name.equals(compareTo.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

    }

}
