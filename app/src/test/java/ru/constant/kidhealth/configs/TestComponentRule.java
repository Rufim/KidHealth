package ru.constant.kidhealth.configs;

import android.support.annotation.NonNull;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.dagger.AppComponent;
import ru.constant.kidhealth.dagger.TestComponent;

public class TestComponentRule implements TestRule {

    private AppComponent appComponent;

    public TestComponentRule() {
        appComponent = new TestComponent();
    }

    public TestComponentRule(@NonNull AppComponent component) {
        this.appComponent = component;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                App.setAppComponent(appComponent);
                base.evaluate();
            }
        };
    }
}
