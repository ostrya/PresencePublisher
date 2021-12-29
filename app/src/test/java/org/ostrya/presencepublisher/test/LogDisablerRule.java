package org.ostrya.presencepublisher.test;

import static org.mockito.Mockito.mockStatic;

import com.hypertrack.hyperlog.HyperLog;

import org.junit.rules.ExternalResource;
import org.mockito.MockedStatic;

public class LogDisablerRule extends ExternalResource {
    private MockedStatic<HyperLog> staticLogMock;

    @Override
    protected void before() {
        staticLogMock = mockStatic(HyperLog.class);
    }

    @Override
    protected void after() {
        staticLogMock.close();
    }
}
