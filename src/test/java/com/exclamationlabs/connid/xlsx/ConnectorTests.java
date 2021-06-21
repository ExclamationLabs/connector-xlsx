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

import java.util.ArrayList;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.test.common.TestHelpers;
import org.junit.Assert;
import org.junit.Test;

public class ConnectorTests {

    private static final Log LOG = Log.getLog(ConnectorTests.class);

    private static ArrayList<ConnectorObject> results = new ArrayList<>();

    protected Configuration newConfiguration() {
        return new Configuration();
    }

    protected APIConfiguration apiConfig() {
        APIConfiguration impl = TestHelpers.createTestConfiguration( Connector.class, newConfiguration());
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        impl.getConfigurationProperties().setPropertyValue("directoryPathProperty","target/test-classes" );
        impl.getConfigurationProperties().setPropertyValue("fileNameProperty","example" );
        impl.getConfigurationProperties().setPropertyValue("includesHeaderProperty",true );
        impl.getConfigurationProperties().setPropertyValue("uidSortedProperty",true );
        impl.getConfigurationProperties().setPropertyValue("mergeProperty","role" );
        impl.getConfigurationProperties().setPropertyValue("identifierProperty","id" );
        return impl;
    }

    public static SearchResultsHandler handler = new SearchResultsHandler() {

        @Override
        public boolean handle(ConnectorObject connectorObject) {
            results.add(connectorObject);
            return true;
        }

        @Override
        public void handleResult(SearchResult result) {
            LOG.info("Im handling {0}", result.getRemainingPagedResults());
        }
    };

    @Test
    public void schema() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        Schema schema = factory.newInstance(apiConfig()).schema();
        Assert.assertNotNull(schema);
        LOG.info(schema.toString());
    }

    @Test
    public void test() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        factory.newInstance(apiConfig()).test();
        Assert.assertTrue(true);
    }

    @Test
    public void validate() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        factory.newInstance(apiConfig()).validate();
        Assert.assertTrue(true);
    }

    @Test
    public void search() {
        results = new ArrayList<>();

        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();

        factory.newInstance(apiConfig()).search( ObjectClass.ACCOUNT, null, handler, null);
        Assert.assertEquals(10, results.size());
    }
    @Test
    public void searchIgnore() {
        results = new ArrayList<>();

        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration ignoreConfig = apiConfig();

        ignoreConfig.getConfigurationProperties().setPropertyValue("ignoreProperty","active" );
        ignoreConfig.getConfigurationProperties().setPropertyValue("ignoreValueProperty","FALSE" );

        factory.newInstance(ignoreConfig).search( ObjectClass.ACCOUNT, null, handler, null);
        Assert.assertEquals(5, results.size());
    }

    @Test(expected = ConnectorIOException.class)
    public void resourceFileNotFound() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = apiConfig();
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        impl.getConfigurationProperties().setPropertyValue("directoryPathProperty","target/test-classes/nodirectory" );
        impl.getConfigurationProperties().setPropertyValue("fileNameProperty","nofilehere" );

        factory.newInstance(impl).schema();

        Assert.assertTrue(true);
    }

}
