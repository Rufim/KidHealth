package ru.constant.kidhealth.dagger;

import android.content.Context;

import org.robolectric.RuntimeEnvironment;

public class MockContextModule extends ContextModule {

    public MockContextModule() {
        super(RuntimeEnvironment.application);
    }
}
