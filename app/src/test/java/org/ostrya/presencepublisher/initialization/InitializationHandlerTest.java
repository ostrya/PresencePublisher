package org.ostrya.presencepublisher.initialization;

import android.os.PowerManager;
import com.hypertrack.hyperlog.HyperLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.ostrya.presencepublisher.Application;
import org.ostrya.presencepublisher.MainActivity;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static android.content.Context.POWER_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InitializationHandlerTest {
    @Mock
    MainActivity context;

    @Mock
    PowerManager powerManager;

    private final LinkedList<AbstractChainedHandler> handlerSpies = new LinkedList<>();

    private MockedStatic<HyperLog> staticLogMock;

    @Before
    public void disableLog() {
        staticLogMock = mockStatic(HyperLog.class);
    }

    @After
    public void reset() {
        staticLogMock.close();
    }

    @Test
    public void chain_is_set_up_correctly() {
        List<HandlerFactory> handlerChain = InitializationHandler.HANDLER_CHAIN.stream()
                .map(this::wrapAsSpy)
                .collect(Collectors.toList());

        InitializationHandler handler = InitializationHandler.getHandler(handlerChain);

        assertThat(handlerSpies).hasSize(6);
        assertThat(handlerSpies.get(0)).isInstanceOf(EnsureLocationPermission.class)
                .extracting(AbstractChainedHandler::getRequestCode)
                .isEqualTo(Application.LOCATION_PERMISSION_REQUEST_CODE);
        assertThat(handlerSpies.get(1)).isInstanceOf(EnsureBackgroundLocationPermission.class)
                .extracting(AbstractChainedHandler::getRequestCode)
                .isEqualTo(Application.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
        assertThat(handlerSpies.get(2)).isInstanceOf(EnsureLocationServiceEnabled.class)
                .extracting(AbstractChainedHandler::getRequestCode)
                .isEqualTo(Application.LOCATION_REQUEST_CODE);
        assertThat(handlerSpies.get(3)).isInstanceOf(EnsureBluetoothServiceEnabled.class)
                .extracting(AbstractChainedHandler::getRequestCode)
                .isEqualTo(Application.START_BLUETOOTH_REQUEST_CODE);
        assertThat(handlerSpies.get(4)).isInstanceOf(EnsureBatteryOptimizationDisabled.class)
                .extracting(AbstractChainedHandler::getRequestCode)
                .isEqualTo(Application.BATTERY_OPTIMIZATION_REQUEST_CODE);
        assertThat(handlerSpies.get(5)).isInstanceOf(CreateSchedule.class)
                .extracting(AbstractChainedHandler::getRequestCode)
                .isEqualTo(-1);
        assertThat(handlerSpies)
                .extracting(AbstractChainedHandler::getRequestCode)
                .doesNotHaveDuplicates();
    }

    @Test
    public void all_states_are_passed() {
        List<HandlerFactory> handlerChain = InitializationHandler.HANDLER_CHAIN.stream()
                .map(this::wrapAsSpy)
                .collect(Collectors.toList());

        doReturn(false).when(context).isLocationServiceNeeded();
        doReturn(powerManager).when(context).getSystemService(POWER_SERVICE);

        InitializationHandler handler = InitializationHandler.getHandler(handlerChain);
        // scheduler needs too many android classes to mock properly for now
        doNothing().when(handlerSpies.get(5)).initialize(context);

        handler.initialize(context);

        InOrder inOrder = inOrder(handlerSpies.toArray());
        handlerSpies.forEach(handlerSpy -> inOrder.verify(handlerSpy).initialize(context));

        handler.handleResult(context, -1, -2);
        handlerSpies.forEach(handlerSpy -> inOrder.verify(handlerSpy).handleResult(context, -1, -2));
        inOrder.verify(handlerSpies.getLast()).doHandleResult(context, -2);

        inOrder.verifyNoMoreInteractions();
    }

    HandlerFactory wrapAsSpy(HandlerFactory original) {
        return handlerChain -> {
            InitializationHandler spyHandler = spy(original.create(handlerChain));
            // as each handler creates all its next handlers in its constructor,
            // the spies are created in reverse order to the initial chain
            handlerSpies.addFirst((AbstractChainedHandler) spyHandler);
            return spyHandler;
        };
    }
}
