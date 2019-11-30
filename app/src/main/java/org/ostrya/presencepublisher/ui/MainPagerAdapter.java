package org.ostrya.presencepublisher.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import org.ostrya.presencepublisher.R;

public class MainPagerAdapter extends FragmentPagerAdapter {
    private Context context;

    public MainPagerAdapter(FragmentManager fm, Context context) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.context = context;
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
            default:
                return new ConnectionFragment();
            case 1:
                return new ScheduleFragment();
            case 2:
                return new ConditionFragment();
            case 3:
                return new LogFragment();
            case 4:
                return new LicenseFragment();
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.tab_connection_title);
            case 1:
                return context.getString(R.string.tab_schedule_title);
            case 2:
                return context.getString(R.string.tab_condition_title);
            case 3:
                return context.getString(R.string.tab_log_title);
            case 4:
                return context.getString(R.string.tab_licenses_title);
        }
        return null;
    }
}
