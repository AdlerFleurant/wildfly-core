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

import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.jboss.as.cli.gui.FxCliGuiContext;
import org.jboss.as.cli.gui.component.FxDeploymentChooser;
import org.jboss.as.cli.gui.component.FxServerGroupChooser;

/**
 * Dialog for creating an undeploy command.  This dialog behaves differently for
 * standalone or domain mode.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxUndeployCommandDialog extends Dialog {

    private FxCliGuiContext cliGuiCtx;
    private FxServerGroupChooser serverGroupChooser;
    private FxDeploymentChooser deploymentChooser;

    private CheckBox keepContent = new CheckBox("Keep Content");
    private CheckBox allRelevantServerGroups = new CheckBox("All RelevantServer Groups");

    public FxUndeployCommandDialog(FxCliGuiContext cliGuiCtx) {
        super();
        this.initModality(Modality.APPLICATION_MODAL);
        this.initOwner(cliGuiCtx.getMainWindow());
        this.setTitle("undeploy");

        this.cliGuiCtx = cliGuiCtx;
        this.serverGroupChooser = new FxServerGroupChooser(cliGuiCtx);
        this.deploymentChooser = new FxDeploymentChooser(cliGuiCtx, cliGuiCtx.isStandalone());

        BorderPane content = new BorderPane();

        this.getDialogPane().setContent(content);
//        Container contentPane = getContentPane();
        //TODO: contentPane.setLayout(new BorderLayout(10, 10));
//        content.setCenter(makeInputPanel());
//        setRelevantServerGroupsListener();
//        content.setBottom(makeButtonPanel());
        setResizable(false);
    }

//    private void setRelevantServerGroupsListener() {
//        allRelevantServerGroups.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                serverGroupChooser.setEnabled(!allRelevantServerGroups.isSelected());
//            }
//        });
//    }
//
//    private Pane makeInputPanel() {
//        GridPane inputPanel = new GridPane();
//// TODO:
//// GridBagConstraints gbc = new GridBagConstraints();
////        gbc.gridwidth = GridBagConstraints.REMAINDER;
////        gbc.anchor = GridBagConstraints.WEST;
////        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        if (!deploymentChooser.hasDeployments()) {
//            inputPanel.add(new Label("NO DEPLOYMENTS AVAILABLE TO UNDEPLOY"), 0, 0);
//            return inputPanel;
//        }
//
//        inputPanel.add(deploymentChooser, 0, 1);
//        inputPanel.add(deploymentChooser, 0, 2);
//        inputPanel.add(keepContent, 0, 3);
//
//        if (!cliGuiCtx.isStandalone()) {
////          TODO:  inputPanel.add(Box.createVerticalStrut(30), gbc);
//            inputPanel.add(serverGroupChooser, 0, 4);
//            inputPanel.add(allRelevantServerGroups, 0, 5);
//        }
//
//        return inputPanel;
//    }
//
//    private JPanel makeButtonPanel() {
//        JPanel buttonPanel = new JPanel();
//
//        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
//        ok.setDefaultButton(true);
//        this.getDialogPane().
//        ok.addActionListener(this);
//        ok.setMnemonic(KeyEvent.VK_ENTER);
//
//        JButton cancel = new JButton("Cancel");
//        cancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent ae) {
//                FxUndeployCommandDialog.this.dispose();
//            }
//        });
//
//        if (deploymentChooser.hasDeployments()) {
//            buttonPanel.add(ok);
//        }
//
//        buttonPanel.add(cancel);
//        buttonPanel.add(new HelpButton("undeploy.txt"));
//        return buttonPanel;
//    }

    public void actionPerformed() {
//        StringBuilder builder = new StringBuilder("undeploy  ");
//
//        String name = deploymentChooser.getSelectedDeployment();
//        builder.append(name);
//
//        if (keepContent.isSelected()) builder.append("  --keep-content");
//
//        if (!cliGuiCtx.isStandalone()) {
//            addDomainParams(builder);
//        }
//
//        JTextComponent cmdText = cliGuiCtx.getCommandLine().getCmdText();
//        cmdText.setText(builder.toString());
//        dispose();
//        cmdText.requestFocus();
    }

    private void addDomainParams(StringBuilder builder) {
        if (!allRelevantServerGroups.isSelected()) {
            builder.append(serverGroupChooser.getCmdLineArg());
        } else {
            builder.append("  --all-relevant-server-groups");
        }
    }

}
