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
package org.jboss.as.cli.gui.component;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import org.jboss.dmr.ModelNode;

/**
 * Editor for parameters that are of ModelType.LIST.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxListEditor extends GridPane {

    private ListView list = new ListView<>();

    private Button addButton = new Button("Add...");
    private Button editButton = new Button("Edit...");
    private Button removeButton = new Button("Remove");
    private Button moveUpButton = new Button("\u25B2"); // unicode for solid triangle
    private Button moveDownButton = new Button("\u25BC"); // unicode for solid inverted triangle
    private ListProperty listProperty = new SimpleListProperty(list.getItems());

    public FxListEditor() {

        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.setCellFactory(param -> {
            ListCell cell = new ListCell();
            cell.setPrefWidth(30);
            return cell;
        });// TODO: about 30 characters wide

        BooleanBinding noSelection = list.getSelectionModel().selectedItemProperty().isNull();
        editButton.disableProperty().bind(noSelection);
        removeButton.disableProperty().bind(noSelection);
        moveUpButton.disableProperty().bind(
                Bindings.and(noSelection, list.getSelectionModel().selectedIndexProperty().isNotEqualTo(0))
        );

        moveDownButton.disableProperty().bind(
                Bindings.and(noSelection, list.getSelectionModel().selectedIndexProperty().isNotEqualTo(
                        listProperty.sizeProperty()
                ))
        );

        Pane buttonColumn = makeButtonColumn();

        ScrollPane scroller = new ScrollPane(list);
        Pane moveUpDownColumn = makeMoveUpDownColumn();


//        GridBagConstraints gbConst = new GridBagConstraints();

//        gbConst.gridx = 0;
//        gbConst.weightx = 1.0;
//        gbConst.weighty = 1.0;
        add(buttonColumn, 0, 0);

//        add(Box.createHorizontalStrut(5));

//        gbConst.fill = GridBagConstraints.BOTH;
//        gbConst.gridx = GridBagConstraints.RELATIVE;
//        gbConst.weightx = 10.0;
        add(scroller, 0, 1);

//        add(Box.createHorizontalStrut(5));

//        gbConst.fill = GridBagConstraints.NONE;
//        gbConst.weightx = 1.0;
        add(moveUpDownColumn, 0, 2);
    }

    private Pane makeButtonColumn() {
        GridPane buttonColumn = new GridPane();
        buttonColumn.setHgap(5);
        buttonColumn.setVgap(5);

        addButton.setTooltip(new Tooltip("Add an item to the list."));
        addButton.setOnAction(event -> {
            ItemEditor editor = new ItemEditor();
            editor.show();
            list.getSelectionModel().select(listProperty.size() - 1);
        });

        editButton.setTooltip(new Tooltip("Edit selected item."));
        editButton.setDisable(true);
        editButton.setOnAction(event -> {
            ItemEditor editor = new ItemEditor(list.getSelectionModel().getSelectedItem().toString());
            editor.showAndWait().ifPresent(item->{
                if (editor.isAddMode) {
                    list.getItems().add(item);
                } else {
                    list.getItems().set(list.getSelectionModel().getSelectedIndex(), item);
                }
            });
        });

        removeButton.setTooltip(new Tooltip("Remove selected item."));
        removeButton.setDisable(true);
        removeButton.setOnAction(event -> list.getItems().remove(list.getSelectionModel().getSelectedIndex()));

        buttonColumn.add(addButton, 0, 0);
        buttonColumn.add(editButton, 0, 1);
        buttonColumn.add(removeButton, 0, 2);
        return buttonColumn;
    }

    private Pane makeMoveUpDownColumn() {
        GridPane buttonColumn = new GridPane();
        buttonColumn.setVgap(5);
        buttonColumn.setHgap(5);

        moveUpButton.setTooltip(new Tooltip("Move selected item up."));
        moveUpButton.setDisable(true);
        moveUpButton.setOnAction(event -> {
                    int selectedIndex = list.getSelectionModel().getSelectedIndex();
                    Object toBeMoved = list.getItems().remove(selectedIndex);
                    list.getItems().add(selectedIndex - 1, toBeMoved);
                    list.getSelectionModel().select(selectedIndex - 1);
                }
        );

        moveDownButton.setTooltip(new Tooltip("Move selected item down."));
        moveDownButton.setDisable(true);
        moveDownButton.setOnAction(event -> {
                    int selectedIndex = list.getSelectionModel().getSelectedIndex();
                    Object toBeMoved = list.getItems().remove(selectedIndex);
                    list.getItems().add(selectedIndex + 1, toBeMoved);
                    list.getSelectionModel().select(selectedIndex + 1);
                }
        );

        buttonColumn.add(moveUpButton, 0, 0);
        buttonColumn.add(moveDownButton, 0, 1);
        return buttonColumn;
    }

    public ModelNode getValue() {
        ModelNode value = new ModelNode();
        list.getItems().forEach(e -> value.add(e.toString()));
        return value;
    }

    public void setValue(ModelNode value) {
        if (!value.isDefined()) return;
        for (ModelNode item : value.asList()) {
            list.getItems().add(item.asString());
        }
    }

    private class ItemEditor extends Dialog<String> {

        private boolean isAddMode = true;
        private TextField itemField = new TextField();

        public ItemEditor() {
            this("Add Item", "");
        }

        public ItemEditor(String item) {
            this("Edit Item", item);

            isAddMode = false;
        }

        private ItemEditor(String label, String item) {
            super();
            this.itemField.textProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if(newValue.length() > 30){
                            this.itemField.setText(newValue.substring(0,30));
                        }
                    });
            this.initOwner(FxListEditor.this.getScene().getWindow());
            this.initModality(Modality.APPLICATION_MODAL);
            this.setTitle(label);

//            setLocationRelativeTo(parent);
//            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            itemField.setText(item);

            BorderPane content = new BorderPane();
            content.setCenter(itemField);
            makeButtonPanel();
            this.getDialogPane().setContent(content);
//            contentPane.setLayout(new BorderLayout(10, 10));
            setResizable(false);
        }

        private void makeButtonPanel() {
            ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            this.getDialogPane().getButtonTypes().addAll(ok, cancel);
        }

    }
}
