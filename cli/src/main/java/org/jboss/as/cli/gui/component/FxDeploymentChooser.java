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

import java.util.List;

import javafx.collections.FXCollections;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import org.jboss.as.cli.gui.FxCliGuiContext;

/**
 * This component produces a JPanel containing a sortable table that allows choosing
 * a deployment that exists on the server.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxDeploymentChooser extends ScrollPane {

    private TableView<? extends FxAbstractDeployment> tableView;

    public FxDeploymentChooser(FxCliGuiContext cliGuiCtx, boolean isStandalone) {
        if (isStandalone) {
            List<FxStandaloneDeployment> standaloneDeployments = FxStandaloneDeployment.getStandaloneDeployments(cliGuiCtx);
            tableView = new FxDeploymentTable<>(FXCollections.observableList(standaloneDeployments), isStandalone);
            this.setContent(tableView);
        } else {
            List<FxDomainDeployment> domainDeployments = FxDomainDeployment.getDomainDeployments(cliGuiCtx);
            tableView = new FxDeploymentTable<>(FXCollections.observableList(domainDeployments), isStandalone);
            this.setContent(tableView);
        }
    }

    /**
     * Get the name of the selected deployment.
     *
     * @return The name or null if there are no deployments.
     */
    public String getSelectedDeployment() {
        return tableView.getSelectionModel()
                .getSelectedItem()
                .getName();
    }

    public boolean hasDeployments() {
        return !tableView.getItems().isEmpty();
    }

}
