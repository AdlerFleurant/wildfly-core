/*
* JBoss, Home of Professional Open Source.
* Copyright 2011, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.subsystem.test.extrasubsystem.subsystem;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.capability.registry.RuntimeCapabilityRegistry;
import org.jboss.as.controller.extension.ExtensionRegistry;
import org.jboss.as.controller.extension.ExtensionRegistryType;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.as.subsystem.test.extrasubsystem.subsystem.dependency.Dependency;
import org.jboss.as.subsystem.test.extrasubsystem.subsystem.dependency.DependencySubsystemExtension;
import org.jboss.as.subsystem.test.extrasubsystem.subsystem.main.MainService;
import org.jboss.as.subsystem.test.extrasubsystem.subsystem.main.MainSubsystemExtension;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLMapper;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ExtraSubsystemTestCase extends AbstractSubsystemTest {

    public ExtraSubsystemTestCase() {
        super(MainSubsystemExtension.SUBSYSTEM_NAME, new MainSubsystemExtension());

    }

    /**
     * Tests that the xml is parsed into the correct operations
     */
    @Test
    public void testParseSubsystem() throws Exception {
        //Parse the subsystem xml into operations
        String subsystemXml =
                "<subsystem xmlns=\"" + DependencySubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>" +
                "<subsystem xmlns=\"" + MainSubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>";
        List<ModelNode> operations = super.parse(new DependencyAdditionalInitialization(), subsystemXml);

        ///Check that we have the expected number of operations
        Assert.assertEquals(2, operations.size());

        //Check that each operation has the correct content
        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(DependencySubsystemExtension.SUBSYSTEM_NAME, element.getValue());

        addSubsystem = operations.get(1);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(MainSubsystemExtension.SUBSYSTEM_NAME, element.getValue());
    }

    /**
     * Test that the model created from the xml looks as expected
     */
    @Test
    public void testInstallIntoController() throws Exception {
        //Parse the subsystem xml and install into the controller
        String subsystemXml =
                "<subsystem xmlns=\"" + DependencySubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>" +
                "<subsystem xmlns=\"" + MainSubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>";
        KernelServices services = createKernelServicesBuilder(new DependencyAdditionalInitialization())
                .setSubsystemXml(subsystemXml)
                .build();

        //Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(DependencySubsystemExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(MainSubsystemExtension.SUBSYSTEM_NAME));

        Assert.assertNotNull(services.getContainer().getService(Dependency.NAME));
        Assert.assertNotNull(services.getContainer().getService(MainService.NAME));
        MainService mainService = (MainService)services.getContainer().getService(MainService.NAME).getValue();
        Assert.assertNotNull(mainService.dependencyValue.getValue());

    }

    /**
     * Starts a controller with a given subsystem xml and then checks that a second
     * controller started with the xml marshalled from the first one results in the same model
     */
    @Test
    public void testParseAndMarshalModel() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml =
                "<subsystem xmlns=\"" + DependencySubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>" +
                "<subsystem xmlns=\"" + MainSubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>";
        KernelServices servicesA = createKernelServicesBuilder(new DependencyAdditionalInitialization())
                .setSubsystemXml(subsystemXml)
                .build();
        //Get the model and the persisted xml from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        String marshalled = servicesA.getPersistedSubsystemXml();
        marshalled = "<subsystem xmlns=\"" + DependencySubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>" + marshalled;

        //Install the persisted xml from the first controller into a second controller
        KernelServices servicesB = createKernelServicesBuilder(new DependencyAdditionalInitialization())
                .setSubsystemXml(marshalled)
                .build();
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Starts a controller with the given subsystem xml and then checks that a second
     * controller started with the operations from its describe action results in the same model
     */
    @Test
    public void testDescribeHandler() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml =
                "<subsystem xmlns=\"" + DependencySubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>" +
                "<subsystem xmlns=\"" + MainSubsystemExtension.NAMESPACE + "\">" +
                "</subsystem>";
        KernelServices servicesA = createKernelServicesBuilder(new DependencyAdditionalInitialization())
                .setSubsystemXml(subsystemXml)
                .build();
        //Get the model and the describe operations from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, DependencySubsystemExtension.SUBSYSTEM_NAME)).toModelNode());
        ArrayList<ModelNode> allOps = new ArrayList<>();
        allOps.addAll(checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList());
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, MainSubsystemExtension.SUBSYSTEM_NAME)).toModelNode());
        allOps.addAll(checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList());


        //Install the describe options from the first controller into a second controller
        KernelServices servicesB = createKernelServicesBuilder(new DependencyAdditionalInitialization())
                .setBootOperations(allOps)
                .build();
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);

    }

    private static class DependencyAdditionalInitialization extends AdditionalInitialization {
        DependencySubsystemExtension dependency = new DependencySubsystemExtension();

        @Override
        public void addParsers(ExtensionRegistry extensionRegistry, XMLMapper xmlMapper) {
            extensionRegistry.initializeParsers(dependency, DependencySubsystemExtension.EXTENSION_NAME, xmlMapper);
        }

        @Override
        protected void initializeExtraSubystemsAndModel(ExtensionRegistry extensionRegistry, Resource rootResource, ManagementResourceRegistration rootRegistration, RuntimeCapabilityRegistry capabilityRegistry) {
            dependency.initialize(extensionRegistry.getExtensionContext(DependencySubsystemExtension.EXTENSION_NAME, rootRegistration, ExtensionRegistryType.SLAVE));
        }
    }
}
