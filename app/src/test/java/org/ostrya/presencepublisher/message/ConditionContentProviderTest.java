package org.ostrya.presencepublisher.message;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ostrya.presencepublisher.PresencePublisher;
import org.ostrya.presencepublisher.network.NetworkService;
import org.ostrya.presencepublisher.test.LogDisablerRule;

@RunWith(MockitoJUnitRunner.class)
public class ConditionContentProviderTest {
    @Rule public final LogDisablerRule logDisablerRule = new LogDisablerRule();

    @Mock private PresencePublisher applicationContext;
    @Mock private SharedPreferences sharedPreferences;
    @Mock private NetworkService networkService;

    private ConditionContentProvider uut;

    @Before
    public void setup() {
        uut = new ConditionContentProvider(applicationContext, sharedPreferences, networkService);
    }

    @Test
    public void test() {
        uut.getConditionContents();
    }
}
