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

import java.time.LocalDateTime;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * JTable to hold the server logs.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class FxServerLogsTable extends TableView<FxServerLogsTableModel> {

    public FxServerLogsTable() {
        this.setStyle("-fx-cell-size: 30px;");
//      TODO:  setAutoCreateRowSorter(true);
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<FxServerLogsTableModel, String> fileNameColumn = new TableColumn<>();
        fileNameColumn.setCellValueFactory(param -> param.getValue().fileProperty());

        TableColumn<FxServerLogsTableModel, LocalDateTime> lastModifiedColumn = new TableColumn<>();
        lastModifiedColumn.setCellValueFactory(param -> param.getValue().lastModifiedProperty());

        TableColumn<FxServerLogsTableModel, Long> sizeColumn = new TableColumn<>();
        sizeColumn.setCellValueFactory(param -> param.getValue().sizeProperty().asObject());
    }
}
