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

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/**
 * Standard Browse button.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class FxBrowseButton extends Button {

    private static final FileChooser fileChooser = new FileChooser();

    static {
        fileChooser.setInitialDirectory(new File("."));
    }

    public FxBrowseButton(final Dialog parentDialog, final TextField targetField) {
        super("Browse ...");
        this.setOnAction(ae -> {
            File chosen = fileChooser.showOpenDialog(parentDialog.getOwner());
            if (chosen != null) {
                try {
                    targetField.setText(chosen.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
