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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Creates a dialog that lets you build a deploy command.  This dialog
 * behaves differently depending on standalone or domain mode.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxDeployDialog implements Initializable {

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField runtimeNameTextField;

    @FXML
    private CheckBox forcedCheckBox;

    @FXML
    private TextField pathTextField;

    public void setWindow(Window window) {
        this.window = window;
    }

    // make this static so that it always retains the last directory chosen
//    private static JFileChooser fileChooser = new JFileChooser(new File("."));
//
//    private CliGuiContext cliGuiCtx;
//    private JPanel inputPanel = new JPanel(new GridBagLayout());
//    private JTextField pathField = new JTextField(40);
//    private JTextField nameField = new JTextField(40);
//    private JTextField runtimeNameTextField = new JTextField(40);
//    private JCheckBox forceCheckBox = new JCheckBox("force");
//    private JCheckBox disabledCheckBox = new JCheckBox("disabled");
//    private ServerGroupChooser serverGroupChooser;
//    private JCheckBox allServerGroups = new JCheckBox("all-server-groups");
    @FXML
    private CheckBox disabledCheckBox;

    private ReadOnlyStringWrapper path = new ReadOnlyStringWrapper("");

    private ReadOnlyStringWrapper name = new ReadOnlyStringWrapper("");

    private ReadOnlyStringWrapper runtimeName = new ReadOnlyStringWrapper("");

    private ReadOnlyBooleanWrapper forced = new ReadOnlyBooleanWrapper();

    private ReadOnlyBooleanWrapper disabled = new ReadOnlyBooleanWrapper();

    private FileChooser fileChooser = new FileChooser();

    public String getPath() {
        return path.get().trim();
    }

    public ReadOnlyStringProperty pathProperty() {
        return path.getReadOnlyProperty();
    }

    public String getName() {
        return name.get().trim();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }

    public String getRuntimeName() {
        return runtimeName.get().trim();
    }

    public ReadOnlyStringProperty runtimeNameProperty() {
        return runtimeName.getReadOnlyProperty();
    }

    public boolean isForced() {
        return forced.get();
    }

    public ReadOnlyBooleanProperty forcedProperty() {
        return forced.getReadOnlyProperty();
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public ReadOnlyBooleanProperty disabledProperty() {
        return disabled.getReadOnlyProperty();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser.setInitialDirectory(new File("."));

        forced.bind(forcedCheckBox.selectedProperty());

        path.bindBidirectional(pathTextField.textProperty());
        name.bindBidirectional(nameTextField.textProperty());
        runtimeName.bindBidirectional(runtimeNameTextField.textProperty());
        disabledCheckBox.disableProperty().bind(forced);
        disabled.bind(
                Bindings.and(
                        disabledCheckBox.selectedProperty(),
                        disabledCheckBox.disabledProperty().not()
                )
        );
    }

    @FXML
    private void browse() throws IOException {
        File file = fileChooser.showOpenDialog(this.window);

        if (file != null) {
            path.set(file.getCanonicalPath());
        }
    }

    private void setAllServerGroupsListener() {
//        allServerGroups.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (allServerGroups.isSelected()) {
//                    serverGroupChooser.setEnabled(false);
//                } else {
//                    serverGroupChooser.setEnabled(true);
//                }
//            }
//        });
    }

    private void setForceListener() {
//        forceCheckBox.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (forceCheckBox.isSelected()) {
//                    serverGroupChooser.setEnabled(false);
//                    allServerGroups.setEnabled(false);
//                    allServerGroups.setSelected(false);
//                    disabledCheckBox.setEnabled(false);
//                    disabledCheckBox.setSelected(false);
//                } else {
//                    serverGroupChooser.setEnabled(true);
//                    allServerGroups.setEnabled(true);
//                    disabledCheckBox.setEnabled(true);
//                }
//            }
//        });
    }

//    private JPanel makeInputPanel() {
//        GridBagConstraints gbConst = new GridBagConstraints();
//        gbConst.anchor = GridBagConstraints.WEST;
//        gbConst.insets = new Insets(5, 5, 5, 5);
//
//        JLabel pathLabel = new JLabel("File Path:");
//
//        gbConst.gridwidth = 1;
//        inputPanel.add(pathLabel, gbConst);
//
//        addStrut();
//        inputPanel.add(pathField, gbConst);
//
//        addStrut();
//        JButton browse = new BrowseButton(FxDeployDialog.this, pathField);
//        gbConst.gridwidth = GridBagConstraints.REMAINDER;
//        inputPanel.add(browse, gbConst);
//
//        JLabel nameLabel = new JLabel("Name:");
//        gbConst.gridwidth = 1;
//        inputPanel.add(nameLabel, gbConst);
//        addStrut();
//        gbConst.gridwidth = GridBagConstraints.REMAINDER;
//        inputPanel.add(nameField, gbConst);
//
//        JLabel runtimeNameLabel = new JLabel("Runtime Name:");
//        gbConst.gridwidth = 1;
//        inputPanel.add(runtimeNameLabel, gbConst);
//        addStrut();
//        gbConst.gridwidth = GridBagConstraints.REMAINDER;
//        inputPanel.add(runtimeNameTextField, gbConst);
//
//        JLabel forceLabel = new JLabel();
//        gbConst.gridwidth = 1;
//        inputPanel.add(forceLabel, gbConst);
//        gbConst.gridwidth = GridBagConstraints.REMAINDER;
//        inputPanel.add(forceCheckBox, gbConst);
//
//        JLabel disabledLabel = new JLabel();
//        gbConst.gridwidth = 1;
//        inputPanel.add(disabledLabel, gbConst);
//        gbConst.gridwidth = GridBagConstraints.REMAINDER;
//        inputPanel.add(disabledCheckBox, gbConst);
//
//        if (cliGuiCtx.isStandalone()) return inputPanel;
//
//        JLabel serverGroupLabel = new JLabel();
//        gbConst.gridwidth = 1;
//        inputPanel.add(serverGroupLabel, gbConst);
//        gbConst.gridwidth = GridBagConstraints.REMAINDER;
//        inputPanel.add(serverGroupChooser, gbConst);
//
//        gbConst.gridwidth = 1;
//        inputPanel.add(new JLabel(), gbConst);
//        gbConst.gridwidth = GridBagConstraints.REMAINDER;
//        inputPanel.add(allServerGroups, gbConst);
//
//        return inputPanel;
//    }
//
//    private void addStrut() {
//        inputPanel.add(Box.createHorizontalStrut(5));
//    }
//
//    private JPanel makeButtonPanel() {
//        JPanel buttonPanel = new JPanel();
//
//        JButton ok = new JButton("OK");
//        ok.addActionListener(this);
//        ok.setMnemonic(KeyEvent.VK_ENTER);
//
//        JButton cancel = new JButton("Cancel");
//        cancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent ae) {
//                FxDeployDialog.this.dispose();
//            }
//        });
//
//        buttonPanel.add(ok);
//        buttonPanel.add(cancel);
//        buttonPanel.add(new HelpButton("deploy.txt"));
//        return buttonPanel;
//    }

    public void actionPerformed() {
//        StringBuilder builder = new StringBuilder("deploy");
//
//        String path = pathField.getText();
//        if (!path.trim().isEmpty()) {
//            builder.append("  ").append(path);
//        } else {
//            JOptionPane.showMessageDialog(this, "A file must be selected.", "Empty File Path", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        String name = nameField.getText();
//        if (!name.trim().isEmpty()) builder.append("  --name=").append(name);
//
//        String runtimeName = runtimeNameTextField.getText();
//        if (!runtimeName.trim().isEmpty()) builder.append("  --runtime-name=").append(runtimeName);
//
//        if (forceCheckBox.isSelected()) builder.append("  --force");
//        if (disabledCheckBox.isSelected() && disabledCheckBox.isEnabled()) builder.append("  --disabled");
//
//        if (!cliGuiCtx.isStandalone()) {
//            if (allServerGroups.isSelected() && allServerGroups.isEnabled()) {
//                builder.append("  --all-server-groups");
//            } else if (serverGroupChooser.isEnabled()) {
//                builder.append(serverGroupChooser.getCmdLineArg());
//            }
//        }
//
//        JTextComponent cmdText = cliGuiCtx.getCommandLine().getCmdText();
//        cmdText.setText(builder.toString());
//        dispose();
//        cmdText.requestFocus();
    }

    private Window window;
}
