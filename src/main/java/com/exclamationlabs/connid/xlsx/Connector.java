/*
 * Copyright (c) 2019. Exclamation Labs https://www.exclamationlabs.com/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.exclamationlabs.connid.xlsx;

import java.io.IOException;
import java.util.*;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;

/**
 * This sample connector provides (empty) implementations for all ConnId operations, but this is not mandatory: any
 * connector can choose which operations are actually to be implemented.
 */
@ConnectorClass(configurationClass = Configuration.class, displayNameKey = "xlsx.connector.display")
public class Connector implements org.identityconnectors.framework.spi.Connector, SchemaOp, TestOp, SearchOp<Filter> {

    private static final Log LOG = Log.getLog(Connector.class);

    private Configuration configuration;

    private Reader reader;

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(final org.identityconnectors.framework.spi.Configuration configuration) {
        this.configuration = (Configuration) configuration;
        String filePath = ((Configuration) configuration).getFilePath();

        try {
            this.reader = new Reader(filePath);
        } catch (IOException e) {
            throw new ConnectorIOException(e);
        }

        LOG.ok("Connector {0} successfully inited", getClass().getName());
    }

    @Override
    public void dispose(){
        try {
			reader.closeReader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        reader = null;
    }

    @Override
    public void test() {

        if (reader != null) {
            try {
				reader.closeReader();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        try {
            reader.openReader();
        } catch (IOException e) {
            throw new ConnectorIOException("Unable to open XLSX Reader");
        }
    }

    @Override
    public Schema schema() {
        SchemaBuilder schemaBuilder = new SchemaBuilder(Connector.class);

        ObjectClassInfoBuilder objectClassBuilderUser = new ObjectClassInfoBuilder();
        objectClassBuilderUser.setType(ObjectClass.ACCOUNT_NAME);
        objectClassBuilderUser.addAttributeInfo(infoBuilder(Uid.NAME, configuration.getIdentifierProperty()));
        objectClassBuilderUser.addAttributeInfo(infoBuilder(Name.NAME, configuration.getIdentifierProperty()));
        objectClassBuilderUser.addAttributeInfo(infoBuilder(PredefinedAttributes.GROUPS_NAME, configuration.getGroupIdentifierProperty()));
        for (String colName: reader.getHeading(configuration.getIncludesHeaderProperty())) {
            if(!(colName.equals(configuration.getIdentifierProperty()) || colName.equals(configuration.getIdentifierProperty()))){
                objectClassBuilderUser.addAttributeInfo(infoBuilder(colName,colName));
            }
        }

        ObjectClassInfo userOci = objectClassBuilderUser.build();
        schemaBuilder.defineObjectClass(userOci);

        ObjectClassInfoBuilder objectClassBuilderGroup = new ObjectClassInfoBuilder();
        objectClassBuilderGroup.setType(ObjectClass.GROUP_NAME);
        objectClassBuilderGroup.addAttributeInfo(infoBuilder(Uid.NAME,configuration.getIdentifierProperty()));
        objectClassBuilderGroup.addAttributeInfo(infoBuilder(Name.NAME,configuration.getIdentifierProperty()));

        ObjectClassInfo groupOci = objectClassBuilderGroup.build();
        schemaBuilder.defineObjectClass(groupOci);

        schemaBuilder.defineOperationOption(OperationOptionInfoBuilder.buildAttributesToGet(), SearchOp.class);

        return schemaBuilder.build();
    }

    private AttributeInfo infoBuilder(String name, String nativeName){
        return infoBuilder(name, nativeName, true);
    }

    private AttributeInfo infoBuilder(String name, String nativeName, Boolean required){
        return infoBuilder(name, nativeName, String.class, required, false);
    }

    private AttributeInfo infoBuilder(String name, String nativeName, Class<?> type, Boolean required, Boolean multi){
        AttributeInfoBuilder info = new AttributeInfoBuilder(name);
        info.setNativeName(nativeName);
        info.setType(type);
        info.setRequired(required);
        info.setCreateable(true);
        info.setUpdateable(true);
        info.setMultiValued(multi);
        return info.build();
    };



    @Override
    public FilterTranslator<Filter> createFilterTranslator(
            final ObjectClass objectClass,
            final OperationOptions options) {

        return new AbstractFilterTranslator<Filter>() {
        };
    }

    @Override
    public void executeQuery(
            final ObjectClass objectClass,
            final Filter query,
            final ResultsHandler handler,
            final OperationOptions options) {

        if (objectClass.equals(ObjectClass.ACCOUNT)) {
            for (ConnectorObject accountConnectorObject : getConnectorObjectListOfAccounts()) {
                handler.handle(accountConnectorObject);
            }

        } else if (objectClass.equals(ObjectClass.GROUP)) {
            for (ConnectorObject groupConnectorObject : getConnectorObjectListOfGroups()) {
                handler.handle(groupConnectorObject);
            }

        } else {
            LOG.info("Unsupported objectClass {0} passed to query", objectClass.getDisplayNameKey());
            throw new IllegalArgumentException("Unsupported object class "+objectClass.getDisplayNameKey());
        }

    }

    private ConnectorObject getAccountConnectorObjectFromAccount(Account account) {
        ConnectorObjectBuilder cob = new ConnectorObjectBuilder();
        cob.setName(account.getIdentifier());
        cob.setUid(account.getIdentifier());
        cob.addAttribute(PredefinedAttributes.GROUPS_NAME, account.getGroups());
        account.forEach(cob::addAttribute);

        return cob.build();
    }


    private ConnectorObject getGroupConnectorObjectFromAccount(String groupName) {
        ConnectorObjectBuilder cob = new ConnectorObjectBuilder();
        cob.setName(groupName);
        cob.setUid(groupName);
        cob.setObjectClass(ObjectClass.GROUP);

        return cob.build();
    }


    private List<ConnectorObject> getConnectorObjectListOfAccounts() {
        Collection<Account> users;
        List<ConnectorObject> connectorObjects = new ArrayList<>();

        users = reader.getAccounts(configuration);

        for (Account account : users) {
            connectorObjects.add(getAccountConnectorObjectFromAccount(account));
        }

        return connectorObjects;
    }

    private List<ConnectorObject> getConnectorObjectListOfGroups() {
        Collection<Account> groups;
        List<ConnectorObject> connectorObjects = new ArrayList<>();

        groups = reader.getAccounts(configuration);
        Set<String> groupNames = new HashSet<>();

        for (Account account : groups) {
            for (String group : account.getGroups()) {
                if (groupNames.add(group)) {
                    connectorObjects.add(getGroupConnectorObjectFromAccount(group));
                }
            }
        }

        return connectorObjects;
    }
}
