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

import javafx.scene.control.TabPane;
import org.jboss.as.cli.gui.component.CLIOutput;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class executes whatever command is on the command line.
 * It displays the result in the Output tab and sets "Output" to
 * be the currently selected tab.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxDoOperationActionListener implements  Runnable{

    private FxCliGuiContext cliGuiCtx;

    private CLIOutput output;

    private LinkedList<String> cmdHistory = new LinkedList<>();

    public FxDoOperationActionListener() {
        this.cliGuiCtx = FxCliGuiContext.getInstance();
        this.output = cliGuiCtx.getOutput();
    }

    @Override
    public void run() {
        String command = cliGuiCtx.getCmdText().get();
        try {
            cmdHistory.push(command);
            FxCommandExecutor.Response response = cliGuiCtx.getExecutor().doCommandFullResponse(command);
            postOutput(response);
        } catch (Exception e) {
            output.postCommandWithResponse(command, e.getMessage());
        } finally {
            TabPane tabs = cliGuiCtx.getTabs();
            tabs.getSelectionModel().select(tabs.getTabs().size() - 1);
        }
    }

    public List getCmdHistory() {
        return Collections.unmodifiableList(this.cmdHistory);
    }

    private void postOutput(FxCommandExecutor.Response response) {
        boolean verbose = cliGuiCtx.isVerbose().get();
        if (verbose) {
            postVerboseOutput(response);
        } else {
            output.postCommandWithResponse(response.getCommand(), response.getDmrResponse().toString());
        }
    }

    private void postVerboseOutput(FxCommandExecutor.Response response) {
        output.postAttributed(response.getDmrResponse().toString() + "\n\n", null);
        output.postAttributed(response.getDmrRequest().toString() + "\n\n", null);
        output.postBold(response.getCommand() + "\n");
    }

}
