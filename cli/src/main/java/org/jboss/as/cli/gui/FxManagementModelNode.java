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

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A node in the management tree.  Non-leaves are addressable entities in a DMR command.  Leaves are attributes.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class FxManagementModelNode extends TreeItem<FxManagementModelNode.UserObject> {

    private static EventHandler<TreeModificationEvent<UserObject>> EXPANDED_HANDLER = e -> {
        FxManagementModelNode node = (FxManagementModelNode) e.getTreeItem();
        node.explore();
    };

    private boolean isLeaf = false;

    private boolean isGeneric = false;

    /**
     * Constructor for root node only.
     */
    public FxManagementModelNode() {
        this(new UserObject());
    }

    private FxManagementModelNode(UserObject userObject) {
        this.isLeaf = userObject.isLeaf;
        this.isGeneric = userObject.isGeneric;
//        if (isGeneric) setAllowsChildren(false);
        this.setValue(userObject);

        this.addEventHandler(TreeItem.branchExpandedEvent(), EXPANDED_HANDLER);
    }

    /**
     * Clone as a root node
     *
     * @return The cloned node.
     */
    @Override
    public FxManagementModelNode clone() {
        UserObject toBeCloned = this.getValue();
        UserObject clonedUsrObj = new UserObject(toBeCloned, addressPath(), true);
        return new FxManagementModelNode(clonedUsrObj);
    }

    /**
     * Find a node in the managementTreeView.  The node must be "visible" to be found.
     *
     * @param address The full address of the node matching ManagementModelNode.addressPath()
     * @return The node, or null if not found.
     */
    public FxManagementModelNode findNode(String address) {
        return this.depthFirstList().stream()
                .map(item-> (FxManagementModelNode) item)
                .filter(item->item.addressPath().equals(address))
                .findFirst()
                .orElse(null);
    }

    /**
     * Refresh children using read-resource operation.
     */
    public void explore() {
        if (isLeaf) return;
        if (isGeneric) return;
        this.getChildren().clear();

        try {
            FxCommandExecutor executor = FxCliGuiContext.getInstance().getExecutor();
            String addressPath = addressPath();
            ModelNode resourceDesc = executor.doCommand(addressPath + ":read-resource-description");
            resourceDesc = resourceDesc.get("result");
            ModelNode response = executor.doCommand(addressPath + ":read-resource(include-runtime=true,include-defaults=true)");
            ModelNode result = response.get("result");
            if (!result.isDefined()) return;

            List<String> childrenTypes = getChildrenTypes(addressPath);
            for (ModelNode node : result.asList()) {
                Property prop = node.asProperty();
                if (childrenTypes.contains(prop.getName())) { // resource node
                    if (hasGenericOperations(addressPath, prop.getName())) {
                        this.getChildren().add(new FxManagementModelNode(new UserObject(node, prop.getName())));
                    }
                    if (prop.getValue().isDefined()) {
                        for (ModelNode innerNode : prop.getValue().asList()) {
                            UserObject usrObj = new UserObject(innerNode, prop.getName(), innerNode.asProperty().getName());
                            this.getChildren().add(new FxManagementModelNode(usrObj));
                        }
                    }
                } else { // attribute node
                    UserObject usrObj = new UserObject(node, resourceDesc, prop.getName(), prop.getValue().asString());
                    this.getChildren().add(new FxManagementModelNode(usrObj));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasGenericOperations(String addressPath, String resourceName) throws Exception {
        FxCommandExecutor executor = FxCliGuiContext.getInstance().getExecutor();
        ModelNode response = executor.doCommand(addressPath + resourceName + "=*/:read-operation-names");
        if (response.get("outcome").asString().equals("failed")) return false;

        for (ModelNode node : response.get("result").asList()) {
            if (node.asString().equals("add")) return true;
        }

        return false;
    }

    private List<String> getChildrenTypes(String addressPath) throws Exception {
        List<String> childrenTypes = new ArrayList<>();
        FxCommandExecutor executor = FxCliGuiContext.getInstance().getExecutor();
        ModelNode readChildrenTypes = executor.doCommand(addressPath + ":read-children-types");
        for (ModelNode type : readChildrenTypes.get("result").asList()) {
            childrenTypes.add(type.asString());
        }
        return childrenTypes;
    }

    /**
     * Get the DMR path for this node.  For leaves, the DMR path is the path of its parent.
     *
     * @return The DMR path for this node.
     */
    public String addressPath() {
        if (isLeaf) {
            FxManagementModelNode parent = (FxManagementModelNode) getParent();
            return parent.addressPath();
        }

        StringBuilder addressPathBuilder = new StringBuilder();
        FxManagementModelNode node = this;
        do {
            UserObject userObject = node.getValue();
            addressPathBuilder.insert(0, userObject.getName());
            if (userObject.isRoot()) {
                continue;
            }

            addressPathBuilder.insert(userObject.getName().length(), "=" + userObject.getEscapedValue() + "/");

        } while (((node = (FxManagementModelNode) node.getParent()) != null));

        return addressPathBuilder.toString();
    }

    public List<TreeItem<UserObject>> depthFirstList() {
        List<TreeItem<UserObject>> visitedItems = new LinkedList<>();
        List<TreeItem<UserObject>> unvisitedItems = new LinkedList<>();
        unvisitedItems.add(this);

        while (!unvisitedItems.isEmpty()) {
            TreeItem<UserObject> currentItem = unvisitedItems.remove(0);
            unvisitedItems.addAll(0, currentItem.getChildren());
            visitedItems.add(currentItem);
        }

        return visitedItems;
    }

    @Override
    public boolean isLeaf() {
        return this.isLeaf;
    }

    public boolean isGeneric() {
        return this.isGeneric;
    }

    public static String escapeAddressElement(String element) {
        element = element.replace(":", "\\:");
        element = element.replace("/", "\\/");
        element = element.replace("=", "\\=");
        element = element.replace(" ", "\\ ");
        element = element.replace("$", "\\$");
        return element;
    }

    /**
     * Encapsulates name/value pair.  Also encapsulates escaping of the value.
     */
    public static class UserObject {
        private ModelNode backingNode;

        private String name;

        private String value;

        private boolean isLeaf;

        private boolean isGeneric = false;

        private boolean isRoot = false;

        private String separator;

        private AttributeDescription attribDesc = null;

        /**
         * Constructor for the root node.
         */
        public UserObject() {
            this.backingNode = new ModelNode();
            this.name = "/";
            this.value = "";
            this.isLeaf = false;
            this.isRoot = true;
            this.separator = "";
        }

        /**
         * Constructor for cloning purposes.
         *
         * @param usrObj The object to be cloned.
         */
        public UserObject(UserObject usrObj, String addressPath, boolean isRoot) {
            if (usrObj.backingNode != null) this.backingNode = usrObj.backingNode.clone();
            this.name = addressPath;
            this.value = usrObj.value;
            this.isLeaf = usrObj.isLeaf;
            this.isRoot = isRoot;
            this.isGeneric = usrObj.isGeneric;
            this.separator = usrObj.separator;
            if (usrObj.attribDesc != null) this.attribDesc = new AttributeDescription(usrObj.attribDesc);
        }

        /**
         * Constructor for generic folder where resource=*.
         *
         * @param name The name of the resource.
         */
        public UserObject(ModelNode backingNode, String name) {
            this.backingNode = backingNode;
            this.name = name;
            this.value = "*";
            this.isLeaf = false;
            this.isGeneric = true;
            this.separator = "=";
        }

        // resource node such as subsystem=weld
        public UserObject(ModelNode backingNode, String name, String value) {
            this.backingNode = backingNode;
            this.name = name;
            this.value = value;
            this.isLeaf = false;
            this.separator = "=";
        }

        // attribute
        public UserObject(ModelNode backingNode, ModelNode resourceDesc, String name, String value) {
            this.attribDesc = new AttributeDescription(resourceDesc.get("attributes", name));
            this.backingNode = backingNode;
            this.name = name;
            this.value = value;
            this.isLeaf = true;

            if (this.attribDesc.isGraphable()) {
                this.separator = " \u2245 ";
            } else {
                this.separator = " => ";
            }
        }

        public ModelNode getBackingNode() {
            return this.backingNode;
        }

        public AttributeDescription getAttributeDescription() {
            return this.attribDesc;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public String getEscapedValue() {
            return FxManagementModelNode.escapeAddressElement(this.value);
        }

        public boolean isRoot() {
            return this.isRoot;
        }

        public boolean isLeaf() {
            return this.isLeaf;
        }

        public boolean isGeneric() {
            return this.isGeneric;
        }

        @Override
        public String toString() {
            if (isRoot) {
                return this.name;
            } else {
                return this.name + this.separator + this.value;
            }
        }
    }

    static class AttributeDescription {

        private ModelNode attributes;

        AttributeDescription(ModelNode attributes) {
            this.attributes = attributes;
        }

        // Used for cloning
        AttributeDescription(AttributeDescription attrDesc) {
            this.attributes = attrDesc.attributes.clone();
        }

        /**
         * Is this a runtime attribute?
         */
        public boolean isRuntime() {
            return attributes.get("storage").asString().equals("runtime");
        }

        public ModelType getType() {
            return attributes.get("type").asType();
        }

        public boolean isGraphable() {
            return isRuntime() && isNumeric();
        }

        public boolean isNumeric() {
            ModelType type = getType();
            return (type == ModelType.BIG_DECIMAL) ||
                    (type == ModelType.BIG_INTEGER) ||
                    (type == ModelType.DOUBLE) ||
                    (type == ModelType.INT) ||
                    (type == ModelType.LONG);
        }
    }

}