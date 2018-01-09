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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.jboss.as.cli.gui.FxCliGuiContext;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A table model appropriate for standalone mode.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxStandaloneDeployment extends FxAbstractDeployment{

    private static ToggleGroup deploymentsButtonGroup = new ToggleGroup();

    private BooleanProperty enabled = new SimpleBooleanProperty();

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    protected List<Object[]> data = new ArrayList<>();

    public static List<FxStandaloneDeployment> getStandaloneDeployments(FxCliGuiContext cliGuiCtx) {

        ModelNode deploymentsQuery = null;
        String queryString = "/deployment=*/:read-resource";
        List<FxStandaloneDeployment> data = new ArrayList<>();
        try {
            deploymentsQuery = cliGuiCtx.getExecutor().doCommand(queryString);
            if (deploymentsQuery.get("outcome").asString().equals("failed")) return data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (ModelNode node : deploymentsQuery.get("result").asList()) {
            ModelNode deploymentNode = node.get("result"); // get the inner result
            FxStandaloneDeployment deployment = new FxStandaloneDeployment();

            data.add(deployment);

            String name = deploymentNode.get("name").asString();
            deployment.setName(name);

            RadioButton radio = new RadioButton(name);
            radio.setToggleGroup(deploymentsButtonGroup);

            deployment.setRuntimeName(deploymentNode.get("runtime-name").asString());

            ModelNode enabled = deploymentNode.get("enabled");
            if (enabled.isDefined()) deployment.setEnabled(enabled.asBoolean());
        }

//        if (data.size() > 0) {
//            RadioButton first = (RadioButton)data.get(0)[0];
//            first.setSelected(true);
//        }

        return data;
    }
}
