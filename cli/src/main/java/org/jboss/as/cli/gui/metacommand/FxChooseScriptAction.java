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
import java.io.File;

import javafx.stage.FileChooser;
import org.jboss.as.cli.gui.FxCliGuiContext;
import org.jboss.as.cli.gui.component.FxScriptMenu;

/**
 * Action that allows the user to choose a script from the file system.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxChooseScriptAction extends FxScriptAction {
    // make this static so that it always retains the last directory chosen
    private static FileChooser fileChooser;

    public FxChooseScriptAction(FxScriptMenu menu, FxCliGuiContext cliGuiCtx) {
        super(menu, "Choose CLI Script", cliGuiCtx);
//        putValue(SHORT_DESCRIPTION, "Choose a CLI script from the file system.");

    }

    public void actionPerformed(ActionEvent e) {
        // Do this here or it gets metal look and feel.  Not sure why.
        if (fileChooser == null) {
            fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("."));
        }

        File returnVal = fileChooser.showOpenDialog(cliGuiCtx.getMainWindow());
        if (returnVal!=null) return;

        runScript(returnVal);
    }

}
