package ru.constant.kidhealth.mvp.views;

import android.support.annotation.IdRes;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface DayActionView extends MvpView {

    void hideButton(@IdRes int id, long delay);

    void onStarted();

    void updateTime(String time);

    void onFinished();

}
