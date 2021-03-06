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

import net.vrallev.android.cat.Cat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ru.constant.kidhealth.R;
import ru.constant.kidhealth.domain.event.UpdateAction;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.mvp.presenters.SchedulePresenter;
import ru.kazantsev.template.adapter.FragmentPagerAdapter;
import ru.kazantsev.template.fragments.mvp.MvpPagerFragment;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;
import ru.kazantsev.template.util.GuiUtils;

public class SchedulePagerFragment extends MvpPagerFragment<List<DayAction>, DayActionsFragment> implements DataSourceView<List<DayAction>> {

    public static final String MOVE_TO_ID = "moveToId";

    @InjectPresenter
    SchedulePresenter schedulePresenter;
    String[] days = new DateFormatSymbols(new Locale("ru")).getWeekdays();
    Calendar calendar = Calendar.getInstance();


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        getBaseActivity().getToolbarShadow().setVisibility(View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateAction updateAction) {
        DayAction action = updateAction.message;
        if (action != null) {
            action.load();
            if (action.prevDayAction() != null && updateAction.getDirection() != UpdateAction.NEXT) {
                onEvent(new UpdateAction(action.prevDayAction(), UpdateAction.PREVIOUS));
            }
            if (action.nextDayAction() != null && updateAction.getDirection() != UpdateAction.PREVIOUS) {
                onEvent(new UpdateAction(action.nextDayAction(), UpdateAction.NEXT));
            }
            List<DayAction> dayActions = getAdapter().getItemTag(action.getDayOfWeek().ordinal());
            if (dayActions != null) {
                for (int i = 0; i < dayActions.size(); i++) {
                    if (dayActions.get(i).getId().equals(action.getId())) {
                        dayActions.set(i, action);
                        DayActionsFragment dayActionsFragment = getAdapter().getRegisteredFragment(action.getDayOfWeek().ordinal());
                        if (dayActionsFragment != null && dayActionsFragment.getAdapter() != null && dayActionsFragment.getAdapter().getItems() != null) {
                            List<DayAction> dayActionsSrc = dayActionsFragment.getAdapter().getItems();
                            for (int j = 0; j < dayActionsSrc.size(); j++) {
                                if (dayActionsSrc.get(j).getId().equals(action.getId())) {
                                    dayActionsSrc.set(j, action);
                                    dayActionsFragment.getAdapter().notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public void setPage(WeekDay weekDay) {
        if (adapter.getItems().size() > 0)
            pager.setCurrentItem(weekDay.ordinal());
    }

    @Override
    public void onStop() {
        super.onStop();
        getBaseActivity().getToolbarShadow().setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        autoLoadMore = false;
        currentItem = DateTime.now().getDayOfWeek() - 1;
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tabStripMode = false;
        View root = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
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
    public void finishLoad(List<List<DayAction>> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        super.finishLoad(items, onElementsLoadedTask, loadedTaskParams);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            replaceTabAt(i);
        }
        pager.setCurrentItem(currentItem);
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

    public void updateDayActions(WeekDay weekDay, List<DayAction> items) {
        if (getAdapter().getItems().size() == WeekDay.values().length) {
            getAdapter().getItems().set(weekDay.ordinal(), items);
        }
    }

    public List<DayAction> getDayActions(WeekDay weekDay) {
        return getAdapter().getItemTag(weekDay.ordinal());
    }

    public boolean hasDayActions(WeekDay weekDay) {
        return getAdapter() != null && getAdapter().getItemTag(weekDay.ordinal()) != null;
    }

    private class SchedulePagerAdapter extends FragmentPagerAdapter<List<DayAction>, DayActionsFragment> {

        public SchedulePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return items.size() == 0 ? 0 : WeekDay.values().length;
        }

        @Override
        public DayActionsFragment getNewItem(int position) {
            DayActionsFragment fragment = new DayActionsFragment();
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
    public FragmentPagerAdapter<List<DayAction>, DayActionsFragment> newAdapter(List<List<DayAction>> currentItems) {
        return new SchedulePagerAdapter(getChildFragmentManager());
    }


    @Override
    public DataSourcePresenter<DataSourceView<List<DayAction>>, List<DayAction>> getPresenter() {
        return schedulePresenter;
    }

    @Override
    public void onDataTaskException(Throwable ex) {
        Cat.e(ex);
    }
}
