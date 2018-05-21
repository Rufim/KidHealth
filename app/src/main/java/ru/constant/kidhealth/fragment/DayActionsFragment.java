package ru.constant.kidhealth.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;

import net.vrallev.android.cat.Cat;

import java.net.SocketTimeoutException;
import java.util.List;

import ru.constant.kidhealth.R;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.domain.models.WeekDay;
import ru.constant.kidhealth.mvp.presenters.DayActionsPresenter;
import ru.kazantsev.template.adapter.ItemListAdapter;
import ru.kazantsev.template.adapter.LazyItemListAdapter;
import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.fragments.mvp.MvpListFragment;
import ru.kazantsev.template.mvp.presenter.DataSourcePresenter;
import ru.kazantsev.template.mvp.view.DataSourceView;
import ru.kazantsev.template.util.AndroidSystemUtils;
import ru.kazantsev.template.util.GuiUtils;

import static ru.constant.kidhealth.utils.AppUtils.fixTime;


public class DayActionsFragment extends MvpListFragment<DayAction> implements DataSourceView<DayAction> {

    @InjectPresenter
    DayActionsPresenter presenter;

    public static final String WEEK_DAY = "weekday";
    public static final String PAST_VISIBLE_ITEMS = "past_visible";
    private WeekDay weekDay;

    public DayActionsFragment() {
        retainInstance = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        presenter.setWeekDay(weekDay = getWeekDay());
        pastVisibleItems = getArg(PAST_VISIBLE_ITEMS, 0);
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if (getParentFragment().getArguments().containsKey(getWeekDay().name())) {
            itemList.onRestoreInstanceState(getArguments().getParcelable(getWeekDay().name()));
            adapter.notifyDataSetChanged();
            isEnd = true;
        }
        if(weekDay == null) {
            showEmptyView(R.string.error_network);
            isEnd = true;
        }
        return root;
    }

    @Override
    public void onDataTaskException(Throwable ex) {
        Cat.e(ex);
        if(ex instanceof SocketTimeoutException) {
            SchedulePagerFragment fragment = (SchedulePagerFragment)getParentFragment();
            if (fragment != null && weekDay != null && fragment.hasDayActions(weekDay)) {
                List<DayAction> actions = fragment.getDayActions(weekDay);
                if (actions != null && actions.size() > 0) {
                    addFinalItems(actions);
                } else {
                    showEmptyView(R.string.day_action_no_actions);
                }
            }
            getBaseActivity().showSnackbar(R.string.error_network);
        }
    }

    private WeekDay getWeekDay() {
        if (weekDay != null) return weekDay;
        return WeekDay.valueOf(getArguments().getString(WEEK_DAY, WeekDay.MONDAY.name()));
    }

    @Override
    public void onDestroyView() {
        getParentFragment().getArguments().putParcelable(getWeekDay().name(), itemList.onSaveInstanceState());
        super.onDestroyView();
    }

    @Override
    public DataSourcePresenter<DataSourceView<DayAction>, DayAction> getPresenter() {
        return presenter;
    }

    @Override
    protected ItemListAdapter<DayAction> newAdapter() {
        SchedulePagerFragment fragment = (SchedulePagerFragment)getParentFragment();
        if (fragment != null && weekDay != null && fragment.hasDayActions(weekDay)) {
            isEnd = true;
            List<DayAction> actions = fragment.getDayActions(weekDay);
            if (actions == null || actions.size() == 0) {
                showEmptyView(R.string.day_action_no_actions);
            }
            return new DayActionsAdapter(actions);
        } else {
            return new DayActionsAdapter();
        }
    }

    @Override
    public void finishLoad(List<DayAction> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        super.finishLoad(items, onElementsLoadedTask, loadedTaskParams);
        ((SchedulePagerFragment)getParentFragment()).updateDayActions(weekDay, items);
    }

    public class DayActionsAdapter extends LazyItemListAdapter<DayAction> {

        public DayActionsAdapter() {
            super(R.layout.item_day_action);
            bindOnlyRootViews = false;
        }

        public DayActionsAdapter(List<DayAction> initial) {
            super(initial, R.layout.item_day_action);
            bindOnlyRootViews = false;
        }

        @Override
        public boolean onClick(View view, @Nullable DayAction item) {
            if (view.getId() == R.id.schedule_day_action_run && item.isRunning()) {
                DayActionFragment.show((BaseFragment) getParentFragment(), item);
                return true;
            }
            return false;
        }

        @Override
        public void onBindHolder(ViewHolder holder, @Nullable DayAction item) {
            GuiUtils.setText(holder, R.id.schedule_day_action_time, "--" + " - " + "--");
            GuiUtils.setText(holder, R.id.schedule_day_action_title, "");
            GuiUtils.setText(holder, R.id.schedule_day_action_comment, "");
            GuiUtils.setText(holder, R.id.schedule_day_action_time, fixTime(item.getStartTime()) + " - " + fixTime(item.getFinishTime()));
            GuiUtils.setText(holder, R.id.schedule_day_action_title, item.getTitle());
            GuiUtils.setText(holder, R.id.schedule_day_action_comment, item.getComment());
            ViewGroup root =(ViewGroup) holder.getItemView();
            if (item.isRunning()) {
                GuiUtils.setVisibility(View.VISIBLE, root, R.id.schedule_day_action_run);
            } else {
                GuiUtils.setVisibility(View.GONE, root, R.id.schedule_day_action_run);
            }
            if(item.isFinished()) {
                root.setBackgroundColor(getResources().getColor(R.color.md_green_400));
            } else if(item.isStopped()) {
                root.setBackgroundColor(getResources().getColor(R.color.md_red_400));
            } else if(item.isPostponed()) {
                root.setBackgroundColor(getResources().getColor(R.color.md_grey_400));
            } else {
                root.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
        }
    }
}
