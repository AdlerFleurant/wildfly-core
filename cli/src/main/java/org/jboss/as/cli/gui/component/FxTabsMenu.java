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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TabPane;
import org.jboss.as.cli.gui.FxCliGuiContext;
import org.jboss.as.cli.gui.metacommand.FxExploreNodeAction;

/**
 * Extension of JMenu that creates and manages tabs.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxTabsMenu extends Menu {

    private FxCliGuiContext cliGuiCtx;

    public FxTabsMenu(FxCliGuiContext cliGuiCtx) {
        super("_Tabs");
        this.cliGuiCtx = cliGuiCtx;
        this.setOnAction(new TabsMenuListener());
    }

    private class TabsMenuListener implements EventHandler<ActionEvent> {


        @Override
        public void handle(ActionEvent event) {
            FxTabsMenu fxTabsMenu = FxTabsMenu.this;
            fxTabsMenu.getItems().clear();
            FxExploreNodeAction exploreAction = new FxExploreNodeAction();
            MenuItem exploreSelectedNode = new MenuItem("_" + exploreAction.getName());

            if ((exploreAction.getSelectedNode() == null) || exploreAction.getSelectedNode().isLeaf()) {
                exploreSelectedNode.setDisable(true);
            }

            fxTabsMenu.getItems().add(exploreSelectedNode);
            fxTabsMenu.getItems().add(new SeparatorMenuItem());

            TabPane tabPane = cliGuiCtx.getTabs();
            tabPane.getTabs().forEach(t->{
                MenuItem item = new MenuItem(t.getText());
                item.setOnAction(ae->{
                    tabPane.getSelectionModel().select(t);
                });
                //TODO: item.setToolTipText(tabPane.getToolTipTextAt(i));
                fxTabsMenu.getItems().add(item);
            });
        }
    }
}
