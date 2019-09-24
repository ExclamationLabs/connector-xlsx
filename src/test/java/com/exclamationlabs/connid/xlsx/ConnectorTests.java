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

    protected ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration( Connector.class, newConfiguration());
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        impl.getConfigurationProperties().setPropertyValue("directoryPathProperty","target/test-classes" );
        impl.getConfigurationProperties().setPropertyValue("fileNameProperty","Employee Roles for MidPoint" );
        impl.getConfigurationProperties().setPropertyValue("includesHeaderProperty",true );
        impl.getConfigurationProperties().setPropertyValue("groupIdentifierProperty","Role Name" );
        impl.getConfigurationProperties().setPropertyValue("identifierProperty","Employee Number" );
        return factory.newInstance(impl);
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
        Schema schema = newFacade().schema();
        Assert.assertNotNull(schema);
    }

    @Test
    public void test() {
        newFacade().test();
        Assert.assertTrue(true);
    }

    @Test
    public void validate() {
        newFacade().validate();
        Assert.assertTrue(true);
    }

    @Test
    public void search() {
        results = new ArrayList<>();

        newFacade().search( ObjectClass.ACCOUNT, null, handler, null);

        Assert.assertEquals(18, results.size());
    }

    @Test(expected = ConnectorIOException.class)
    public void resourceFileNotFound() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration( Connector.class, newConfiguration());
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        impl.getConfigurationProperties().setPropertyValue("directoryPathProperty","target/test-classes/nodirectory" );
        impl.getConfigurationProperties().setPropertyValue("fileNameProperty","nofilehere" );
        impl.getConfigurationProperties().setPropertyValue("includesHeaderProperty",true );
        impl.getConfigurationProperties().setPropertyValue("groupIdentifierProperty","Role Name" );
        impl.getConfigurationProperties().setPropertyValue("identifierProperty","Employee Number" );

        factory.newInstance(impl).schema();

        Assert.assertTrue(true);
    }

}
