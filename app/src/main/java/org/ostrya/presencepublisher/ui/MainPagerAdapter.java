package org.ostrya.presencepublisher.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.ostrya.presencepublisher.R;

public class MainPagerAdapter extends FragmentStateAdapter
        implements TabLayoutMediator.TabConfigurationStrategy {

    public MainPagerAdapter(FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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
                return new AboutFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    @Override
    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
        int titleId;
        switch (position) {
            case 0:
            default:
                titleId = R.string.tab_connection_title;
                break;
            case 1:
                titleId = R.string.tab_schedule_title;
                break;
            case 2:
                titleId = R.string.tab_condition_title;
                break;
            case 3:
                titleId = R.string.tab_log_title;
                break;
            case 4:
                titleId = R.string.tab_about_title;
                break;
        }
        tab.setText(titleId);
    }
}
