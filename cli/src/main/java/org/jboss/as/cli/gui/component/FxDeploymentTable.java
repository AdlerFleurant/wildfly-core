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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Pair;

/**
 * A JTable that displays all deployments for standalone or domain.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxDeploymentTable<T extends FxAbstractDeployment> extends TableView<T> {

    private boolean isStandalone;

    public FxDeploymentTable(ObservableList<T> dm, boolean isStandalone) {
        super(dm);
        this.isStandalone = isStandalone;
        ObservableList<TableColumn<T, ?>> columns = this.getColumns();

        TableColumn<T, String> nameColumn = new TableColumn<>();
        nameColumn.setText("Name");
        nameColumn.setEditable(false);
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        nameColumn.setEditable(true);
        columns.add(nameColumn);

        TableColumn<T, String> runtimeNameColumn = new TableColumn<>();
        runtimeNameColumn.setText("Runtime Name");
        runtimeNameColumn.setEditable(false);
        runtimeNameColumn.setCellValueFactory(param -> param.getValue().runtimeNameProperty());
        columns.add(runtimeNameColumn);

        Type type = ((ParameterizedType) dm.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        if (type.equals(FxStandaloneDeployment.class)) {
            TableColumn<T, Boolean> enabledColumn = new TableColumn<>();
            enabledColumn.setText("Enabled");
            enabledColumn.setEditable(true);
            enabledColumn.setCellValueFactory(param -> ((FxStandaloneDeployment) param.getValue()).enabledProperty());
            columns.add(enabledColumn);
        }

        if (type.equals(FxDomainDeployment.class)) {
            //TODO: make editable
            TableColumn<T, ObservableList<Pair<String, Boolean>>> enabledServerGroupColumn = new TableColumn<>();
            enabledServerGroupColumn.setText("Enabled");
//            enabledServerGroupColumn.setCellValueFactory(param -> ((FxDomainDeployment) param.getValue()).getItems().get(0));
            enabledServerGroupColumn.setCellFactory(param -> new EnabledServerGroupCell());
            enabledServerGroupColumn.setEditable(true);
        }

        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setPrefSize(700, 200);
    }

//    class ComboBoxEditor extends DefaultCellEditor {
//        ComboBoxEditor() {
//            super(new JComboBox());
//        }
//
//        @Override
//        public Component getTableCellEditorComponent(JTable table, Object value,
//                                                     boolean isSelected, int row, int column) {
//            return new JComboBox(new Vector((List) value));
//        }
//    }

//    class RadioButtonEditor extends DefaultCellEditor implements ItemListener {
//
//        private JRadioButton button;
//
//        public RadioButtonEditor(JCheckBox checkBox) {
//            super(checkBox);
//        }
//
//        @Override
//        public Component getTableCellEditorComponent(JTable table, Object value,
//                                                     boolean isSelected, int row, int column) {
//            if (value == null) return null;
//
//            button = (JRadioButton) value;
//            button.addItemListener(this);
//            return (Component) value;
//        }
//
//        @Override
//        public Object getCellEditorValue() {
//            button.removeItemListener(this);
//            return button;
//        }
//
//        public void itemStateChanged(ItemEvent e) {
//            super.fireEditingStopped();
//        }
//    }

    private class EnabledServerGroupCell extends TableCell<T, ObservableList<Pair<String, Boolean>>> {
        private ListView<Pair<String, Boolean>> view = new ListView<>();

        public EnabledServerGroupCell() {
            super();
            this.setGraphic(view);
            view.setCellFactory(param -> new EnabledServerGroupListCell());
        }

        @Override
        protected void updateItem(ObservableList<Pair<String, Boolean>> item, boolean empty) {
            super.updateItem(item, empty);
            view.getItems().clear();
            view.getItems().addAll(item);
        }

        private class EnabledServerGroupListCell extends CheckBoxListCell<Pair<String, Boolean>> {
            @Override
            public void updateItem(Pair<String, Boolean> item, boolean empty) {
                super.updateItem(item, empty);
                this.setText(item.getKey());
                this.updateSelected(item.getValue());
            }
        }
    }
}
