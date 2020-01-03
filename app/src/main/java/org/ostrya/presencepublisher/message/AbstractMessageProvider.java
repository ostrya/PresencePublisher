package org.ostrya.presencepublisher.message;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractMessageProvider {
    private final Context applicationContext;
    private final SharedPreferences sharedPreferences;
    private final String topicPreference;

    protected AbstractMessageProvider(Context context, String topicPreference) {
        applicationContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        this.topicPreference = topicPreference;
    }

    protected Context getApplicationContext() {
        return applicationContext;
    }

    protected SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public List<Message> getMessages() {
        String topic = sharedPreferences.getString(topicPreference, null);
        if (topic == null) {
            HyperLog.w(getClass().getSimpleName(), "No topic defined, not generating any messages");
            return Collections.emptyList();
        }
        List<Message> messages = new ArrayList<>();
        Message.MessageBuilder messageBuilder = Message.messageForTopic(topic);
        for (String content : getMessageContents()) {
            messages.add(messageBuilder.withContent(content));
        }
        return messages;
    }

    protected abstract List<String> getMessageContents();
}
