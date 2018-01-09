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

import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.gui.component.FxServerLogsTableModel;
import org.jboss.as.cli.gui.metacommand.FxDownloadServerLogDialog;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationResponse;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

//import org.jboss.as.cli.gui.metacommand.FxDownloadServerLogDialog;

/**
 * The main panel for listing the server logs.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class FxServerLogsPanel implements Initializable {


    private static final char[] SIZE_CHARS = {'K', 'M', 'G', 'T', 'P', 'E'};

    @FXML
    private Button downloadButton;

    @FXML
    private TableView<FxServerLogsTableModel> serverLogsTableView;

    @FXML
    private TableColumn<FxServerLogsTableModel, String> filenameColumn, lastModifiedColumn, sizeColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        downloadButton.disableProperty().bind(serverLogsTableView.getSelectionModel().selectedItemProperty().isNull());
        filenameColumn.setCellValueFactory(param -> param.getValue().fileProperty());
        lastModifiedColumn.setCellValueFactory(param -> Bindings.createStringBinding(
                () -> param.getValue().lastModifiedProperty().get().format(
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                )));

        sizeColumn.setCellValueFactory(param -> {
            final int unit = 1000;
            LongProperty sizeProperty = param.getValue().sizeProperty();
            long len = sizeProperty.get();

            final int exp = (int) (Math.log(len) / Math.log(unit));
            return Bindings.format("%.1f %sB", len / Math.pow(unit, exp), SIZE_CHARS[exp - 1]);
        });
        refresh();
    }

    @FXML
    private void refresh() {
        try {
            final ModelControllerClient client = FxCliGuiContext.getInstance().getCommmandContext().getModelControllerClient();
            final ModelNode address = new ModelNode().setEmptyList();
            address.add("subsystem", "logging");
            // address.add("log-file", "*");
            final ModelNode op = Operations.createOperation("read-children-names", address);
            op.get("child-type").set("log-file");
            ModelNode response = client.execute(op);
            if (Operations.isSuccessfulOutcome(response)) {
                ModelNode result = Operations.readResult(response);
                final Collection<String> files = new ArrayList<>();
                final Operations.CompositeOperationBuilder builder = Operations.CompositeOperationBuilder.create();
                for (ModelNode file : result.asList()) {
                    files.add(file.asString());
                    builder.addStep(Operations.createReadAttributeOperation(address.clone().add("log-file", file.asString()), "file-size"))
                            .addStep(Operations.createReadAttributeOperation(address.clone().add("log-file", file.asString()), "last-modified-time"));
                }
                result = client.execute(builder.build());
                List<FxServerLogsTableModel> serverLogFiles = new ArrayList<>();
                if (Operations.isSuccessfulOutcome(result)) {
                    final List<ModelNode> attributes = Operations.readResult(result).asList();
                    int i = 0;
                    if (attributes.size() != (files.size() * 2)) {
                        throw new IllegalStateException("Error occurred reading the file attributes");
                    }
                    // Each file result will have two step results from the composite operation
                    for (String file : files) {
                        final ModelNode node = new ModelNode();
                        node.get("file-name").set(file);
                        node.get("file-size").set(Operations.readResult(attributes.get(i++).get(0)));
                        node.get("last-modified-time").set(Operations.readResult(attributes.get(i++).get(0)));
                        LocalDateTime lastModified = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(node.get("last-modified-time").asLong()),
                                ZoneId.systemDefault()
                        );
                        long fileSize = node.get("file-size").asLong();
                        String fileName = node.get("file-name").asString();
                        FxServerLogsTableModel serverLogFile = new FxServerLogsTableModel();
                        serverLogFile.setFile(fileName);
                        serverLogFile.setSize(fileSize);
                        serverLogFile.setLastModified(lastModified);
                        serverLogFiles.add(serverLogFile);
                    }
                }

                serverLogsTableView.getItems().clear();
                serverLogsTableView.getItems().addAll(serverLogFiles);

            } else {
                throw new RuntimeException(Operations.getFailureDescription(response).asString());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private void doubleClick(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY))
            if (event.getClickCount() == 2)
                downloadDialog();
    }

    /**
     * Does the server support log downloads?
     *
     * @param cliGuiCtx The context.
     * @return <code>true</code> if the server supports log downloads,
     * <code>false</code> otherwise.
     */
    public static boolean isLogDownloadAvailable(FxCliGuiContext cliGuiCtx) {
        ModelNode readOps = null;
        try {
            readOps = cliGuiCtx.getExecutor().doCommand("/subsystem=logging:read-children-types");
        } catch (CommandFormatException | IOException e) {
            return false;
        }
        if (!readOps.get("result").isDefined()) return false;
        for (ModelNode op : readOps.get("result").asList()) {
            if ("log-file".equals(op.asString())) return true;
        }
        return false;
    }

//    private String getSelectedFileName() {
//        return table.getSelectionModel().getSelectedItem().getFile();
//    }
//
//    private Long getSelectedFileSize() {
//        return table.getSelectionModel().getSelectedItem().getSize();
//    }

    @FXML
    private void downloadDialog() {
        Pair<DialogPane, FxDownloadServerLogDialog> resourceControllerPair = FxUtil.getInstance().loadResourceControllerPair(getClass(), "FxDownloadServerLogDialog.fxml");
        DialogPane downloadDialogPane = resourceControllerPair.getKey();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(downloadDialogPane);
        FxServerLogsTableModel selectedItem = serverLogsTableView.getSelectionModel().getSelectedItem();
        String file = selectedItem.getFile();
        dialog.setTitle("Download " + file);

        FxDownloadServerLogDialog controller = resourceControllerPair.getValue();

        controller.setFile(file);
        controller.setWindow(downloadDialogPane.getScene().getWindow());

        dialog.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).ifPresent(bt -> {
            String path = controller.getFile();
            if (path.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Empty File Path");
                alert.setContentText("A file path must be selected.");
                downloadDialog();
                return;
            }

            File selectedFile = new File(path);
            DownloadLogTask downloadLogTask = new DownloadLogTask(selectedFile, controller.getViewAfterDownload());
            if (selectedFile.exists()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Overwrite?");
                alert.setContentText("Overwrite " + path);

                alert.showAndWait().ifPresent(bd -> {
                    if (bd.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                        new Thread(downloadLogTask).run();
                    } else {
                        downloadDialog();
                    }
                });
            } else {
                new Thread(downloadLogTask).run();
            }
        });
//        FxDownloadServerLogDialog dialog = new FxDownloadServerLogDialog(cliGuiCtx, getSelectedFileName(), getSelectedFileSize());
//        dialog.setLocationRelativeTo(cliGuiCtx.getMainWindow());
//        dialog.setVisible(true);
    }


    private class DownloadLogTask extends Task<Void> {
        private final File selectedFile;
        private final boolean openAfterDownload;

        public DownloadLogTask(File selectedFile, boolean openAfterDownload) {
            this.selectedFile = selectedFile;
            this.openAfterDownload = openAfterDownload;
        }

        @Override
        protected Void call() throws Exception {
            try {
                FxServerLogsTableModel selectedItem = serverLogsTableView.getSelectionModel().getSelectedItem();
                String command = "/subsystem=logging/log-file=" + selectedItem.getFile() + ":read-attribute(name=stream)";
                final FxCommandExecutor.Response response = FxCliGuiContext.getInstance().getExecutor().doCommandFullResponse(command);
                final ModelNode outcome = response.getDmrResponse();
                if (!Operations.isSuccessfulOutcome(outcome)) {
                    cancel(false);
                    String error = "Failure at server: " + Operations.getFailureDescription(outcome).asString();
                    Alert alert = new Alert(Alert.AlertType.ERROR, error);
                    alert.setTitle("Download Failed");
                    alert.show();
                    return null;
                }

                // Get the UUID of the stream
                final String uuid = Operations.readResult(outcome).asString();

                // Should only be a single entry
                final byte[] buffer = new byte[512];
                try (
                        final OperationResponse.StreamEntry entry = response.getOperationResponse().getInputStream(uuid);
                        final InputStream in = entry.getStream();
                        final OutputStream out = Files.newOutputStream(selectedFile.toPath(), StandardOpenOption.CREATE);
                ) {
                    int bytesRead = 0;
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        bytesRead += len;

                        this.updateProgress(bytesRead, selectedItem.getSize());
                    }
                }
            } catch (IOException | CommandFormatException ex) {
                throw new RuntimeException(ex);
            } finally {

                if (isCancelled()) {
                    selectedFile.delete();
                }
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            FxServerLogsTableModel selectedItem = serverLogsTableView.getSelectionModel().getSelectedItem();
            String message = "Download " + selectedItem.getFile() + " ";
            if (isCancelled()) {
                String contentText = message + "cancelled.";
                Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText);
                alert.setTitle(contentText);
                alert.showAndWait();
                return;
            }

            if (!this.openAfterDownload) {
                String contentText = message + "complete.";
                Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText);
                alert.setTitle(contentText);
                alert.showAndWait();
                return;
            }

            FxUtil.getInstance().open(this.selectedFile.getAbsolutePath());
            // try to open in file manager for destination directory
//                try {
//                    Desktop.getDesktop().open(fileChooser.getCurrentDirectory());
//                } catch (IOException ioe2) {
//                    JOptionPane.showMessageDialog(cliGuiCtx.getMainWindow(), "Download success.  No registered application to view " + fileName, "Can't view file.", JOptionPane.ERROR_MESSAGE);
//                }
        }
    }
}
