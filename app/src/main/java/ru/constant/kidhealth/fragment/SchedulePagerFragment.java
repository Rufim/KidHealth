package ru.constant.kidhealth.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.constant.kidhealth.R;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.mvp.presenters.SchedulePresenter;
import ru.kazantsev.template.adapter.FragmentPagerAdapter;
import ru.kazantsev.template.fragments.mvp.MvpPagerFragment;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;
import ru.kazantsev.template.util.GuiUtils;

public class SchedulePagerFragment extends MvpPagerFragment<WeekDay, DayActionsFragment> implements DataSourceView<WeekDay> {

    public static final String MOVE_TO_ID = "moveToId";

    @InjectPresenter
    SchedulePresenter schedulePresenter;
    String[] days = new DateFormatSymbols(new Locale("ru")).getWeekdays();
    Calendar calendar = Calendar.getInstance();

    @Override
    public void onStart() {
        super.onStart();
        getBaseActivity().getToolbarShadow().setVisibility(View.GONE);

    }

    @Override
    public void onStop() {
        super.onStop();
        getBaseActivity().getToolbarShadow().setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tabStripMode = false;
        View root = super.onCreateView(inflater, container, savedInstanceState);
        tabLayout.setBackgroundColor(GuiUtils.getThemeColor(getContext(), R.attr.colorPrimary));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    tab.getCustomView().findViewById(R.id.schedule_tab_day_of_month).setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_accent));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    tab.getCustomView().findViewById(R.id.schedule_tab_day_of_month).setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_active));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.getLayoutParams().height = (int) getResources().getDimension(R.dimen.schedule_tab);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int currentDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 7 - calendar.getFirstDayOfWeek()) % 7;
        calendar.add(Calendar.DAY_OF_YEAR, -currentDayOfWeek);
        return root;
    }

    @Override
    public void finishLoad(AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        super.finishLoad(onElementsLoadedTask, loadedTaskParams);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            replaceTabAt(i);
        }
    }

    private void replaceTabAt(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        View v = getTabView(position);
        if (tab.isSelected()) {
            v.findViewById(R.id.schedule_tab_day_of_month).setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_accent));
        } else {
            v.findViewById(R.id.schedule_tab_day_of_month).setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_active));
        }
        tab.setCustomView(v);
        v.requestLayout();
    }

    public View getTabView(int position) {
        ViewGroup v = GuiUtils.inflate(getContext(), R.layout.schedule_tab_layout);
        GuiUtils.setText(v, R.id.schedule_tab_day_of_month, getDayOfMonth(position) + "");
        GuiUtils.setText(v, R.id.schedule_tab_name_of_day, getDayName(position));
        return v;
    }

    private CharSequence getDayName(int position) {
        String name = days[(position + 2) < days.length ? position + 2 : 1];
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }


    private int getDayOfMonth(int position) {
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        return currentDay + position;
    }

    private class SchedulePagerAdapter extends FragmentPagerAdapter<WeekDay, DayActionsFragment> {

        public SchedulePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public DayActionsFragment getNewItem(int position) {
            DayActionsFragment fragment =  new DayActionsFragment();
            Bundle bundle = new Bundle();
            bundle.putString(DayActionsFragment.WEEK_DAY, WeekDay.values()[position].name());
            fragment.setArguments(bundle);
            return fragment;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return getDayName(position);
        }

    }

    @Override
    public FragmentPagerAdapter<WeekDay, DayActionsFragment> newAdapter(List<WeekDay> currentItems) {
        return new SchedulePagerAdapter(getChildFragmentManager());
    }


    @Override
    public DataSourcePresenter<DataSourceView<WeekDay>, WeekDay> getPresenter() {
        return schedulePresenter;
    }
}
