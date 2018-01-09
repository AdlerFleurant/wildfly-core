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
package org.jboss.as.cli.gui.metacommand;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import org.jboss.as.cli.gui.FxCliGuiContext;
import org.jboss.as.cli.gui.FxManagementModel;
import org.jboss.as.cli.gui.FxManagementModelNode;
//import org.jboss.as.cli.gui.component.ButtonTabComponent;

/**
 * Action that creates a new tab with the given node as its root.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class FxExploreNodeAction implements Runnable {

    private final String actionName;
    private FxManagementModelNode node;
    private FxCliGuiContext cliGuiCtx;

    public FxExploreNodeAction() {
        this.actionName = calcActionName(cliGuiCtx);
        this.node = findSelectedNode(cliGuiCtx);
        this.cliGuiCtx = FxCliGuiContext.getInstance();
    }

    @Override
    public void run() {
        TabPane tabs = cliGuiCtx.getTabs();
        int newTabIndex = tabs.getSelectionModel().getSelectedIndex() + 1;
        FxManagementModelNode newRoot = node.clone();
//        Tab tab = new Tab(calcTabName(this.node), new FxManagementModel(newRoot, cliGuiCtx));
//        tab.setTooltip(new Tooltip(newRoot.addressPath()));
//       TODO: tabs.setTabComponentAt(newTabIndex, new ButtonTabComponent(tabs));
//        tabs.setSelectedIndex(newTabIndex);
//        tabs.getSelectionModel().select(tab);
    }

    public FxManagementModelNode getSelectedNode() {
        return this.node;
    }

    private static String calcActionName(FxCliGuiContext cliGuiCtx) {
        FxManagementModelNode node = findSelectedNode(cliGuiCtx);
        if (node == null) return "Explore selected node";
        return "Explore " + calcTabName(node);
    }

    private static FxManagementModelNode findSelectedNode(FxCliGuiContext cliGuiCtx) {
        Node selectedComponent = cliGuiCtx.getTabs().getSelectionModel().getSelectedItem().getContent();
        return null;
//        if (selectedComponent == null) return null;
//        if (!(selectedComponent instanceof FxManagementModel)) return null;
//
//        return ((FxManagementModel)selectedComponent).getSelectedNode();
    }

    private static String calcTabName(FxManagementModelNode node) {
        FxManagementModelNode.UserObject usrObj = node.getValue();
        if (usrObj.isGeneric()) {
            return node.toString();
        }
        if (usrObj.isLeaf()) {
            return usrObj.getName();
        }
        return node.toString();
    }

    public String getName() {
        return actionName;
    }
}
