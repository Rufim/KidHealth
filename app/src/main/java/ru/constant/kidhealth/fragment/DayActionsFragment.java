package ru.constant.kidhealth.fragment;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.AlignmentSpan;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.arellomobile.mvp.presenter.InjectPresenter;

import net.vrallev.android.cat.Cat;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
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
import ru.kazantsev.template.mvp.view.DataSourceViewNoPersist;
import ru.kazantsev.template.util.GuiUtils;
import ru.kazantsev.template.util.VerticalAlignmentSpan;

import static ru.constant.kidhealth.utils.AppUtils.fixTime;


public class DayActionsFragment extends MvpListFragment<DayAction> implements DataSourceViewNoPersist<DayAction> {

    @InjectPresenter
    DayActionsPresenter presenter;

    public static final String WEEK_DAY = "weekday";
    public static final String PAST_VISIBLE_ITEMS = "past_visible";
    private WeekDay weekDay;

    public DayActionsFragment() {
        autoLoadMoreOnScroll = false;
        autoLoadMoreOnFinish = false;
        retainInstance = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        presenter.setWeekDay(weekDay = getWeekDay());
        pastVisibleItems = getArg(PAST_VISIBLE_ITEMS, 0);
        View root = super.onCreateView(inflater, container, savedInstanceState);
        if(weekDay == null) {
            showEmptyView(R.string.error);
            finishLoad();
        } else if (getParentFragment().getArguments().containsKey(getWeekDay().name())) {
            itemList.onRestoreInstanceState(getArguments().getParcelable(getWeekDay().name()));
            if(adapter.getItems().isEmpty()) {
                addFinalItems(getCachedActions());
            }
            finishLoad();
        }
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
        return root;
    }

    @Override
    public void onDataTaskException(Throwable ex) {
        Cat.e(ex);
        stopLoading();
        if(ex instanceof SocketTimeoutException || ex instanceof ConnectException) {
            List<DayAction> actions = getCachedActions();
            if (actions != null && actions.size() > 0) {
                if(adapter.getItems().isEmpty()) {
                    addFinalItems(actions);
                }
            } else {
                showEmptyView(R.string.error_connection_failure);
            }
        }
    }

    private List<DayAction> getCachedActions() {
        SchedulePagerFragment fragment = (SchedulePagerFragment) getParentFragment();
        if (fragment != null && weekDay != null && fragment.hasDayActions(weekDay)) {
            return  fragment.getDayActions(weekDay);
        }
        return new ArrayList<>(0);
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
    public DataSourcePresenter<DataSourceViewNoPersist<DayAction>, DayAction> getPresenter() {
        return presenter;
    }

    @Override
    protected ItemListAdapter<DayAction> newAdapter() {
        List<DayAction> actions = getCachedActions();
        if (actions == null || actions.size() == 0) {
            showEmptyView(R.string.day_action_no_actions);
        } else {
            finishLoad();
        }
        return new DayActionsAdapter(actions);
    }

    private void finishLoad() {
        isEnd = true;
        stopLoading();
    }

    @Override
    public void finishLoad(List<DayAction> items, AsyncTask onElementsLoadedTask, Object[] loadedTaskParams) {
        super.finishLoad(items, onElementsLoadedTask, loadedTaskParams);
        ((SchedulePagerFragment) getParentFragment()).updateDayActions(weekDay, items);
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
            if (view.getId() == R.id.schedule_day_action_time_next && item.getNextDayAction() != null) {
                ((SchedulePagerFragment)getParentFragment()).setPage(item.getNextDayAction().getDayOfWeek());
                return true;
            }
            if(view.getId() == R.id.schedule_day_action_time_previous && item.getPrevDayAction() != null) {
                ((SchedulePagerFragment)getParentFragment()).setPage(item.getPrevDayAction().getDayOfWeek());
                return false;
            }
            if (view.getId() == R.id.schedule_day_action_run && item.isRunning()) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.fab_anim);
                view.startAnimation(animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        DayActionFragment.show((BaseFragment) getParentFragment(), item);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                return true;
            }
            return false;
        }

        @Override
        public void onBindHolder(ViewHolder holder, @Nullable DayAction item) {
            ViewGroup root =(ViewGroup) holder.getItemView();
            GuiUtils.setText(holder, R.id.schedule_day_action_time, "--" + " - " + "--");
            GuiUtils.setText(holder, R.id.schedule_day_action_title, "");
            GuiUtils.setText(holder, R.id.schedule_day_action_comment, "");
            GuiUtils.setVisibility(View.GONE, root, R.id.schedule_day_action_time_next);
            GuiUtils.setVisibility(View.GONE, root, R.id.schedule_day_action_time_previous);
            SpannableStringBuilder time = new SpannableStringBuilder();
            if(item.getPrevDayAction()  == null) {
                time.append(fixTime(item.getStartTime()));
            } else {
               // GuiUtils.appendSpannableText(time, "(", new RelativeSizeSpan(0.75f),  new VerticalAlignmentSpan(0.1));
                time.append(fixTime(item.getPrevDayAction().getStartTime()));
                GuiUtils.setVisibility(View.VISIBLE, root, R.id.schedule_day_action_time_previous);
                GuiUtils.appendSpannableText(time," " + getString(R.string.yesterday), new RelativeSizeSpan(0.3f), new VerticalAlignmentSpan(1.5));
              //  GuiUtils.appendSpannableText(time, ")", new RelativeSizeSpan(0.75f),  new VerticalAlignmentSpan(0.1));
            }
            time.append(" - ");
            if(item.getNextDayAction()  == null) {
                time.append(fixTime(item.getFinishTime()));
            } else {
              //  GuiUtils.appendSpannableText(time, "(", new RelativeSizeSpan(0.75f),  new VerticalAlignmentSpan(0.1));
                GuiUtils.appendSpannableText(time, getString(R.string.tomorrow) + " ", new RelativeSizeSpan(0.3f), new VerticalAlignmentSpan(1.5));
                time.append(fixTime(item.getNextDayAction().getFinishTime()));
                GuiUtils.setVisibility(View.VISIBLE, root, R.id.schedule_day_action_time_next);
              //  GuiUtils.appendSpannableText(time, ")", new RelativeSizeSpan(0.75f),  new VerticalAlignmentSpan(0.1));
            }
            GuiUtils.setText(holder, R.id.schedule_day_action_time, time);
            GuiUtils.setText(holder, R.id.schedule_day_action_title, item.getTitle());
            GuiUtils.setText(holder, R.id.schedule_day_action_comment, item.getDescription());
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
