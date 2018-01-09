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

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.jboss.as.cli.gui.FxCliGuiContext;
import org.jboss.as.cli.gui.component.CLIOutput;
import org.jboss.as.cli.gui.component.FxScriptMenu;

/**
 * Abstract action that runs scripts.
 *
 * @author ssilvert
 */
public abstract class FxScriptAction {

    protected FxCliGuiContext cliGuiCtx;

    private CLIOutput output;

    private FxScriptMenu menu;

    public FxScriptAction(FxScriptMenu menu, String name, FxCliGuiContext cliGuiCtx) {
//        super(name);
        this.cliGuiCtx = cliGuiCtx;
        this.menu = menu;
        output = cliGuiCtx.getOutput();
    }

    /**
     * Subclasses should override this method and then call runScript() when a script File
     * is determined.
     */
    public abstract void actionPerformed(ActionEvent e);

    /**
     * Run a CLI script from a File.
     *
     * @param script The script file.
     */
    protected void runScript(File script) {
        if (!script.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(cliGuiCtx.getMainWindow());
            alert.setContentText(script.getAbsolutePath() + " does not exist.");
            alert.setTitle("Unable to run script.");
            alert.show();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.initOwner(cliGuiCtx.getMainWindow());
        confirmation.setTitle("Confirm run script");
        confirmation.setContentText("Run CLI script " + script.getName() + "?");

        Optional<ButtonType> buttonType = confirmation.showAndWait();

        if (buttonType.isPresent())
            if (buttonType.get().getButtonData() != ButtonBar.ButtonData.OK_DONE)
                return;


        menu.addScript(script);

        cliGuiCtx.getTabs().getSelectionModel().select(1); // set to Output tab to view the output
        output.post("\n");

//        SwingWorker scriptRunner = new ScriptRunner(script);
//        scriptRunner.execute();
    }

    // read the file as a list of text lines
    private List<String> getCommandLines(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to process file " + file.getAbsolutePath(), e);
        }
        return lines;
    }

    // We need this class because we have to pass on whether or not a
    // message should be displayed in bold or not.
//    private class OutMessage {
//        private boolean isBold;
//        private String message;
//
//        OutMessage(String message, boolean isBold) {
//            this.message = message;
//            this.isBold = isBold;
//        }
//    }
//
//    /**
//     * Use a SwingWorker to run the script in the background.  Some commands such as deploy can take a long time,
//     * so they can tie up the event dispatch thread and freeze the UI.
//     */
//    protected class ScriptRunner extends SwingWorker<Object, OutMessage> {
//        private File file;
//        private int caretPosition = 0;
//
//        private ScriptRunner(File file) {
//            this.file = file;
//        }
//
//        @Override
//        protected Object doInBackground() throws Exception {
//            ByteArrayOutputStream scriptOut = new ByteArrayOutputStream();
//            PrintStream printOut = new PrintStream(scriptOut);
//
//            // We capture the output stream from System.out so we can give the user feedback
//            // that comes from core CLI.  The publish() method allows us to send messages
//            // to the event dispatch thread safely and in order.
//            try {
//                cliGuiCtx.getCommmandContext().captureOutput(printOut);
//                cliGuiCtx.getCommandLine().setDisable(true);
//
//                publish(new OutMessage(">>> Execute CLI script " + file, true));
//
//                boolean failure = false;
//                for (String command : getCommandLines(file)) {
//                    publish(new OutMessage(command, true));
//
//                    try {
//                        cliGuiCtx.getCommmandContext().handle(command.trim());
//                    } catch (Throwable t) {
//                        t.printStackTrace(printOut);
//                        failure = true;
//                    }
//
//                    if (scriptOut.size() > 0) {
//                        publish(new OutMessage(scriptOut.toString(), false));
//                        scriptOut.reset();
//                    }
//
//                    if (failure) {
//                        publish(new OutMessage(">> Command Failed:  " + command, true));
//                        break;
//                    }
//                }
//
//                publish(new OutMessage(">>> Script complete.", true));
//            } finally {
//                cliGuiCtx.getCommmandContext().releaseOutput();
//                cliGuiCtx.getCommandLine().setDisable(false);
//                cliGuiCtx.getCommmandContext().handle("cd /"); // reset to root directory
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void process(List<OutMessage> messages) {
//            // output messages on the event dispatch thread
//            for (OutMessage msg : messages) {
//                if (msg.isBold) {
//                    caretPosition = output.postBoldAt(msg.message + "\n", caretPosition);
//                } else {
//                    caretPosition = output.postAt(msg.message + "\n", caretPosition);
//                }
//            }
//        }
}
