package org.ostrya.presencepublisher.initialization;

import static android.content.Context.POWER_SERVICE;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.os.PowerManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ostrya.presencepublisher.MainActivity;
import org.ostrya.presencepublisher.preference.about.LocationConsentPreference;
import org.ostrya.presencepublisher.test.LogDisablerRule;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class InitializationHandlerTest {
    @Rule public final LogDisablerRule logDisablerRule = new LogDisablerRule();

    @Mock private MainActivity context;

    @Mock private PowerManager powerManager;

    @Mock private SharedPreferences sharedPreferences;

    @Mock private SharedPreferences.Editor editor;

    private final LinkedList<AbstractChainedHandler<?, ?>> handlerSpies = new LinkedList<>();

    @Test
    public void chain_is_set_up_correctly() {
        List<HandlerFactory> handlerChain =
                InitializationHandler.HANDLER_CHAIN.stream()
                        .map(this::wrapAsSpy)
                        .collect(Collectors.toList());

        InitializationHandler.getHandler(context, handlerChain);

        assertThat(handlerSpies).hasSize(7);
        assertThat(handlerSpies.get(0)).isInstanceOf(EnsureLocationPermission.class);
        assertThat(handlerSpies.get(1)).isInstanceOf(EnsureBackgroundLocationPermission.class);
        assertThat(handlerSpies.get(2)).isInstanceOf(EnsureLocationServiceEnabled.class);
        assertThat(handlerSpies.get(3)).isInstanceOf(EnsureBluetoothPermission.class);
        assertThat(handlerSpies.get(4)).isInstanceOf(EnsureBluetoothServiceEnabled.class);
        assertThat(handlerSpies.get(5)).isInstanceOf(EnsureBatteryOptimizationDisabled.class);
        assertThat(handlerSpies.get(6)).isInstanceOf(CreateSchedule.class);
    }

    @Test
    public void all_states_are_passed() {
        List<HandlerFactory> handlerChain =
                InitializationHandler.HANDLER_CHAIN.stream()
                        .map(this::wrapAsSpy)
                        .collect(Collectors.toList());

        when(context.getApplicationContext()).thenReturn(context);
        when(context.isLocationPermissionNeeded()).thenReturn(false);
        when(context.getSystemService(POWER_SERVICE)).thenReturn(powerManager);
        when(context.getSharedPreferences(any(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.getBoolean(LocationConsentPreference.LOCATION_CONSENT, false))
                .thenReturn(true);
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);

        InitializationHandler handler = InitializationHandler.getHandler(context, handlerChain);
        // scheduler needs too many android classes to mock properly for now
        doNothing().when(handlerSpies.get(6)).initialize();

        handler.initialize();

        InOrder inOrder = inOrder(handlerSpies.toArray());
        handlerSpies.forEach(handlerSpy -> inOrder.verify(handlerSpy).initialize());

        inOrder.verifyNoMoreInteractions();
    }

    HandlerFactory wrapAsSpy(HandlerFactory original) {
        return (activity, handlerChain) -> {
            InitializationHandler spyHandler = spy(original.create(activity, handlerChain));
            // as each handler creates all its next handlers in its constructor,
            // the spies are created in reverse order to the initial chain
            handlerSpies.addFirst((AbstractChainedHandler<?, ?>) spyHandler);
            return spyHandler;
        };
    }
}
