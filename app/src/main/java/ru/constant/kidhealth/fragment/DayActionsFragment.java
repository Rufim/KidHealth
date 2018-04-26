package ru.constant.kidhealth.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.PresenterType;
import com.arellomobile.mvp.presenter.ProvidePresenterTag;

import java.util.regex.Pattern;

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
import ru.kazantsev.template.util.TextUtils;

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
        View root =  super.onCreateView(inflater, container, savedInstanceState);
        if(getParentFragment().getArguments().containsKey(getWeekDay().name())) {
            itemList.onRestoreInstanceState(getArguments().getParcelable(getWeekDay().name()));
        }
        return root;
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
        return new DayActionsAdapter();
    }

    public class DayActionsAdapter extends LazyItemListAdapter<DayAction> {

        public DayActionsAdapter() {
            super(R.layout.item_day_action);
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
            GuiUtils.setText(holder, R.id.schedule_day_action_time, fixTime(item.getStartTime()) + " - " + fixTime(item.getFinishTime()));
            GuiUtils.setText(holder, R.id.schedule_day_action_title, item.getTitle());
            GuiUtils.setText(holder, R.id.schedule_day_action_comment, item.getComment());
            if(item.isRunning()) {
                GuiUtils.setVisibility(View.VISIBLE, (ViewGroup) holder.getItemView(), R.id.schedule_day_action_run);
            } else {
                GuiUtils.setVisibility(View.GONE, (ViewGroup) holder.getItemView(), R.id.schedule_day_action_run);
            }
        }
    }
}
