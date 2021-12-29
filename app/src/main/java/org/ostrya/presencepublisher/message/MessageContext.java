package org.ostrya.presencepublisher.message;

import android.content.Context;

public class MessageContext {
    private final BatteryLevelProvider batteryLevelProvider;
    private final ConditionContentProvider conditionContentProvider;

    public MessageContext(Context context) {
        Context applicationContext = context.getApplicationContext();
        batteryLevelProvider = new BatteryLevelProvider(applicationContext);
        conditionContentProvider = new ConditionContentProvider(applicationContext);
    }

    public BatteryLevelProvider getBatteryLevelProvider() {
        return batteryLevelProvider;
    }

    public ConditionContentProvider getConditionContentProvider() {
        return conditionContentProvider;
    }
}
