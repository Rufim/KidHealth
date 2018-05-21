package ru.constant.kidhealth.mvp.views;

import android.support.annotation.IdRes;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.AddToEndStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface DayActionView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void cleanState();

    @StateStrategyType(AddToEndStrategy.class)
    void switchStateButton(@IdRes int id, boolean state);

    void onStarted();

    void onCanceled();

    void updateTime(String time);

    void onFinish();

    void onFinished();

    void onPostpone();

    void onActionFailure();
}
