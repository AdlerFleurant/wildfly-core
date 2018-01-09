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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import org.jboss.dmr.ModelNode;

/**
 * JTree that knows how to find context-sensitive help and display as ToolTip for
 * each node.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxCommandBuilderTree extends TreeView<FxManagementModelNode.UserObject> {
    private FxManagementModelNode currentNode = null;
    private StringProperty currentDescription = new SimpleStringProperty();

    public FxCommandBuilderTree(FxCliGuiContext cliGuiCtx, FxManagementModelNode root) {
        super(root);
        Tooltip tip = new Tooltip();
        tip.textProperty().bind(currentDescription);
        this.setCellFactory(param -> {
            TreeCell<FxManagementModelNode.UserObject> tc = new TreeCell<>();
            tc.setOnMouseEntered(e->{
                try {
                    FxManagementModelNode treeItem = (FxManagementModelNode) tc.getTreeItem();
                    String addressPath = treeItem.addressPath();
                    ModelNode readResource = cliGuiCtx.getExecutor().doCommand(addressPath + ":read-resource-description");
                    FxManagementModelNode.UserObject usrObj = treeItem.getValue();
                    if (treeItem.isGeneric()) {
                        currentDescription.set("Used for generic operations on " + usrObj.getName() + ", such as 'add'");
                    } else if (!treeItem.isLeaf()) {
                        currentDescription.set(readResource.get("result", "description").asString());
                    } else {
                        ModelNode description = readResource.get("result", "attributes", usrObj.getName(), "description");
                        if (description.isDefined()) {
                            currentDescription.set(description.asString());
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            return tc;
        });
        this.cellFactoryProperty().get();
        this.setTooltip(tip);

        this.tooltipProperty();
    }

}
