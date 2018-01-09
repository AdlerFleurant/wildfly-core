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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jboss.as.cli.gui.FxManagementModelNode.UserObject;

/**
 * This class contains a JTree view of the management model that allows you to build commands by
 * clicking nodes and operations.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxManagementModel implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        managementTreeView.setRoot(new FxManagementModelNode());
        filterTextField.textProperty().addListener((observable, oldValue, newValue) -> this.filter(newValue));

        managementTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    FxCliGuiContext.getInstance()
                            .getCmdText().set(((FxManagementModelNode)newValue).addressPath());
                });

//        managementTreeView.setCellFactory(param -> {
//            TreeCell<UserObject> cell = new TreeCell<>();
//            cell.treeItemProperty().addListener((observable, oldValue, newValue) -> cell.setText(newValue.getValue().getName()));
//
//            cell.onMousePressedProperty()
//                    .setValue(event -> {
//                        if(event.isPopupTrigger()){
//                            FxOperationMenu popupMenu = new FxOperationMenu();
//                            popupMenu.show(cell, event.getX(), event.getY());
//                        }
//                    });
//            return cell;
//        });
    }

    @FXML
    private TextField filterTextField;

    private static final String GENERAL_HELP_TEXT = "Right-click a node to choose an operation.  Close/Open a folder to refresh.  Hover for help.";

    private static final String FILTER_TOOLTIP_HELP = "Display the root nodes containing the given text.";

    @FXML
    private TreeView<FxManagementModelNode.UserObject> managementTreeView;

    // used for the true root '/'

//    public FxManagementModel(FxManagementModelNode root, FxCliGuiContext cliGuiCtx) {
//        this.cliGuiCtx = cliGuiCtx;
////        setLayout(new BorderLayout(10,10));
//        this.managementTreeView = makeTree(root);
//        this.setTop(new Label(GENERAL_HELP_TEXT));
//        this.setCenter(new ScrollPane(managementTreeView));
//        this.setBottom(makeFilterPanel(managementTreeView));
//    }

    @FXML
    private void clearFilterText(){
        filterTextField.setText("");
    }

    private void filter(String newValue){

        FxManagementModelNode root = (FxManagementModelNode) managementTreeView.getRoot();
        root.explore(); // refresh all children

        ObservableList<TreeItem<UserObject>> nodes = root.getChildren();
        nodes.removeIf(n->!n.getValue().toString().contains(newValue));
    }

    /**
     * Find a node in the managementTreeView.  The node must be "visible" to be found.
     *
     * @param address The full address of the node matching ManagementModelNode.addressPath()
     * @return The node, or null if not found.
     */
    public FxManagementModelNode findNode(String address) {
        FxManagementModelNode root = (FxManagementModelNode) managementTreeView.getRoot();
        return root.findNode(address);
    }

    private GridPane makeFilterPanel(TreeView<FxManagementModelNode.UserObject> tree) {
        HBox filterBox = new HBox();
        Label filterLabel = new Label("Filter:");
        filterLabel.setTooltip(new Tooltip(FILTER_TOOLTIP_HELP));
        filterBox.getChildren().add(filterLabel);
//      TODO:  filterBox.getChildren().add(Box.createHorizontalStrut(5));
        TextField filterInput = new TextField();
        filterInput.setPrefColumnCount(30);

        filterBox.getChildren().add(filterInput);
        Button clearButton = new Button("Clear");
        clearButton.setTooltip(new Tooltip("Clear the filter"));
        clearButton.setOnAction(ae -> filterInput.setText(""));
        clearButton.setDisable(true);

        clearButton.disableProperty().bind(filterInput.textProperty().isEmpty());
        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {

            FxManagementModelNode root = (FxManagementModelNode) tree.getRoot();
            root.explore(); // refresh all children

            root.getChildren().stream()
                    .filter(node->!node.getValue().toString().contains(newValue))
                    .forEach(node->root.getChildren().remove(node));
        });

        filterBox.getChildren().add(clearButton);

        // Make filterBox half of width of panel
        GridPane filterPanel = new GridPane();
        filterPanel.add(filterBox, 0, 0);
//      TODO:  filterPanel.add(Box.createGlue());
        return filterPanel;
    }

    /**
     * Get the node that has been selected by the user, or null if
     * nothing is selected.
     *
     * @return The node or <code>null</code>
     */
    public FxManagementModelNode getSelectedNode() {
        return (FxManagementModelNode) managementTreeView.getSelectionModel().getSelectedItem();
    }
}
