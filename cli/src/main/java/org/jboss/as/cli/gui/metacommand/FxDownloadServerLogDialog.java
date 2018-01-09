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
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Dialog to choose destination file and download log.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class FxDownloadServerLogDialog implements Initializable {

    private static boolean VIEW_IN_LOG_VIEWER = true;
    private static final ChangeListener<Boolean> VIEW_IN_LOG_VIEWER_CHANGE_LISTENER =
            (observable, oldValue, newValue) -> VIEW_IN_LOG_VIEWER = newValue;

    @FXML
    private TextField pathTextField;

    @FXML
    private CheckBox defaultViewer;

    private StringProperty file = new SimpleStringProperty();

    public void setFile(String file) {
        this.file.setValue(Paths.get(file).toAbsolutePath().toString());
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    public String getFile() {
        return file.getValue();
    }

    // implements ActionListener, PropertyChangeListener {
    // make these static so that they always retains the last value chosen


    private GridPane inputPanel = new GridPane();

    private ProgressIndicator progressMonitor;

//    private DownloadLogTask downloadTask;

//    private boolean openInViewerSupported = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.pathTextField.textProperty().bindBidirectional(file);
        this.defaultViewer.selectedProperty().setValue(VIEW_IN_LOG_VIEWER);

        this.defaultViewer.selectedProperty().addListener(VIEW_IN_LOG_VIEWER_CHANGE_LISTENER);
    }

    @FXML
    private void browse() {
        FileChooser fileChooser = new FileChooser();
        String fileName = file.getValue();
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showOpenDialog(this.window);
        if (file != null) {
            try {
                fileName = file.getCanonicalPath();
                pathTextField.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private Pane makeInputPanel() {
//        GridBagConstraints gbConst = new GridBagConstraints();
//        gbConst.anchor = GridBagConstraints.WEST;
//        Insets insets = new Insets(5, 5, 5, 5);
//        gbConst.insets = insets;

//        Label pathLabel = new Label("Download To:");
//        pathLabel.setPadding(insets);
//        gbConst.gridwidth = 1;
//        inputPanel.add(pathLabel,0,0);
//
//        inputPanel.add(pathField, 0,1);
//
//        Button browse = new Button("Browse ...");
//        browse.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {


//        });

//        gbConst.gridwidth = GridBagConstraints.REMAINDER;
//        inputPanel.add(browse, 0,2);

//        if (openInViewerSupported) {
//            JLabel emptyLabel = new JLabel("");
//            gbConst.gridwidth = 1;
//            inputPanel.add(emptyLabel, gbConst);
//            addStrut();
//            gbConst.gridwidth = GridBagConstraints.REMAINDER;
//            inputPanel.add(VIEW_IN_LOG_VIEWER, gbConst);
//        }

//        return inputPanel;
//    }

//    private void addStrut() {
//        inputPanel.add(Box.createHorizontalStrut(5));
//    }

    //    @Override
    public void actionPerformed(ActionEvent event) {


//            int option = JOptionPane.showConfirmDialog(, , , JOptionPane.YES_NO_OPTION);
//            if (option == JOptionPane.NO_OPTION) {
//                this.setVisible(true);
//                return;
//            }
//        }
//
//        this.dispose();
//
//        progressMonitor = new ProgressIndicator(cliGuiCtx.getMainWindow(), "Downloading " + fileName, "", 0, 100);
//        progressMonitor.setProgress(0);
//        downloadTask = new DownloadLogTask(selectedFile);
//        downloadTask.addPropertyChangeListener(this);
//        downloadTask.execute();
    }

    //    @Override
//    public void propertyChange(PropertyChangeEvent evt) {
//        if ("progress".equals(evt.getPropertyName())) {
//            int percentRead = (Integer) evt.getNewValue();
//            progressMonitor.setProgress(percentRead);
//        }
//
//        if ("bytesRead".equals(evt.getPropertyName())) {
//            progressMonitor.setNote(evt.getNewValue() + " of " + fileSize + " bytes received.");
//        }
//
//        if (progressMonitor.isCanceled()) {
//            downloadTask.cancel(false);
//        }
//    }
//
//    class DownloadLogTask extends SwingWorker<Void, Void> {
//        private final File selectedFile;
//
//        public DownloadLogTask(File selectedFile) {
//            this.selectedFile = selectedFile;
//        }
//
//        @Override
//        public Void doInBackground() {
//            try {
//                String command = "/subsystem=logging/log-file=" + fileName + ":read-attribute(name=stream)";
//                final Response response = cliGuiCtx.getExecutor().doCommandFullResponse(command);
//                final ModelNode outcome = response.getDmrResponse();
//                if (!Operations.isSuccessfulOutcome(outcome)) {
//                    cancel(false);
//                    String error = "Failure at server: " + Operations.getFailureDescription(outcome).asString();
//                    JOptionPane.showMessageDialog(cliGuiCtx.getMainWindow(), error, "Download Failed", JOptionPane.ERROR_MESSAGE);
//                    return null;
//                }
//
//                // Get the UUID of the stream
//                final String uuid = Operations.readResult(outcome).asString();
//
//                // Should only be a single entry
//                final byte[] buffer = new byte[512];
//                try (
//                        final StreamEntry entry = response.getOperationResponse().getInputStream(uuid);
//                        final InputStream in = entry.getStream();
//                        final OutputStream out = Files.newOutputStream(selectedFile.toPath(), StandardOpenOption.CREATE);
//                ) {
//                    int bytesRead = 0;
//                    int len;
//                    while ((len = in.read(buffer)) != -1) {
//                        out.write(buffer, 0, len);
//                        final int oldValue = bytesRead;
//                        bytesRead += len;
//                        firePropertyChange("bytesRead", oldValue, bytesRead);
//                        setProgress(Math.max(Math.round(((float) bytesRead / (float) fileSize) * 100), 100));
//                    }
//                }
//            } catch (IOException | CommandFormatException ex) {
//                throw new RuntimeException(ex);
//            } finally {
//
//                if (isCancelled()) {
//                    selectedFile.delete();
//                }
//            }
//
//            return null;
//        }
//
//        @Override
//        public void done() {
//            String message = "Download " + fileName + " ";
//            if (isCancelled()) {
//                JOptionPane.showMessageDialog(cliGuiCtx.getMainWindow(), message + "cancelled.", message + "cancelled.", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            if (!VIEW_IN_LOG_VIEWER.isSelected() || !openInViewerSupported) {
//                JOptionPane.showMessageDialog(cliGuiCtx.getMainWindow(), message + "complete.");
//                return;
//            }
//
//            try {
//                Desktop.getDesktop().open(selectedFile);
//            } catch (IOException ioe) {
//                // try to open in file manager for destination directory
//                try {
//                    Desktop.getDesktop().open(fileChooser.getCurrentDirectory());
//                } catch (IOException ioe2) {
//                    JOptionPane.showMessageDialog(cliGuiCtx.getMainWindow(), "Download success.  No registered application to view " + fileName, "Can't view file.", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        }
//    }
    private Window window;

    public boolean getViewAfterDownload() {
        return VIEW_IN_LOG_VIEWER;
    }
}



