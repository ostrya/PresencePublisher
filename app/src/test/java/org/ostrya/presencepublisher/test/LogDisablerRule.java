package org.ostrya.presencepublisher.test;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.withSettings;

import org.junit.rules.ExternalResource;
import org.mockito.MockedStatic;
import org.ostrya.presencepublisher.log.DatabaseLogger;

public class LogDisablerRule extends ExternalResource {
    private MockedStatic<DatabaseLogger> staticLogMock;

    @Override
    protected void before() {
        staticLogMock = mockStatic(DatabaseLogger.class, withSettings().verboseLogging());
    }

    @Override
    protected void after() {
        staticLogMock.close();
    }
}
