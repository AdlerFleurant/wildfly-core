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

import java.util.Arrays;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import org.jboss.as.cli.gui.FxManagementModelNode.UserObject;
import org.jboss.as.cli.gui.metacommand.FxExploreNodeAction;
import org.jboss.dmr.ModelNode;

/**
 * JPopupMenu that selects the available operations for a node address.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxOperationMenu extends ContextMenu {
    private static final String[] genericOps = {"add", "read-operation-description", "read-resource-description", "read-operation-names"};
    private static final List<String> genericOpList = Arrays.asList(genericOps);
    private static final String[] leafOps = {"write-attribute", "undefine-attribute"};
    private static final List<String> leafOpList = Arrays.asList(leafOps);

    public FxOperationMenu() {
//        setLightWeightPopupEnabled(true);
    }

    /**
     * Show the OperationMenu based on the selected node.
     * @param node The selected node.
     * @param x The x position of the selection.
     * @param y The y position of the selection.
     */
    public void show(TreeCell<FxManagementModelNode.UserObject> node, int x, int y) {
        FxCliGuiContext cliGuiCtx = FxCliGuiContext.getInstance();
        FxCommandExecutor executor = cliGuiCtx.getExecutor();
        this.getItems().removeAll();
        FxManagementModelNode item = (FxManagementModelNode) node.getTreeItem();
        addExploreOption(item);

        String addressPath = item.addressPath();
        try {
            ModelNode  opNames = executor.doCommand(addressPath + ":read-operation-names");
            if (opNames.get("outcome").asString().equals("failed")) return;

            for (ModelNode name : opNames.get("result").asList()) {
                String strName = name.asString();

                // filter operations
                if (item.isGeneric() && !genericOpList.contains(strName)) continue;
                if (item.isLeaf() && !leafOpList.contains(strName)) continue;
                if (!item.isGeneric() && !item.isLeaf() && strName.equals("add")) continue;

                ModelNode opDescription = getResourceDescription(addressPath, strName);
                MenuItem menuItem = new MenuItem(strName);
                menuItem.setOnAction(event -> {
                    StringProperty cmdText = cliGuiCtx.getCmdText();
                    ModelNode requestProperties = opDescription.get("result", "request-properties");

                    if (isNoArgOperation(requestProperties, strName)) {
                        cmdText.set(addressPath + ":" + strName);
//                        cmdText.requestFocus();
                        return;
                    }

                    if (item.isLeaf() && strName.equals("undefine-attribute")) {
                        UserObject usrObj = item.getValue();
                        cmdText.set(addressPath + ":" + strName + "(name=" + usrObj.getName() + ")");
//                        cmdText.requestFocus();
                        return;
                    }

                    FxOperationDialog dialog = new FxOperationDialog(cliGuiCtx, (FxManagementModelNode) node.getTreeItem(), strName, opDescription.get("result", "description").asString(), requestProperties);
//                    dialog.setLocationRelativeTo(cliGuiCtx.getMainWindow());
//                    dialog.setVisible(true);
                    dialog.showAndWait().ifPresent(dialog);
                });
                this.getItems().add(menuItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.show(node, x, y);
    }

    private boolean isNoArgOperation(ModelNode requestProperties, String opName) {
        // add operation has implicit 'name' param
        if (opName.equals("add")) return false;

        return (requestProperties == null) || (!requestProperties.isDefined()) || requestProperties.asList().isEmpty();
    }

    private void addExploreOption(FxManagementModelNode node) {
        if (node.isLeaf()) return;
        FxExploreNodeAction action = new FxExploreNodeAction();
        MenuItem item = new MenuItem(action.getName());
        item.setOnAction(event -> action.run());
        this.getItems().add(item);
        this.getItems().add(new SeparatorMenuItem());
    }

    private ModelNode getResourceDescription(String addressPath, String name) {
        try {
            return FxCliGuiContext.getInstance().getExecutor().doCommand(addressPath + ":read-operation-description(name=\"" + name + "\")");
        } catch (Exception e) {
            return null;
        }
    }
}
