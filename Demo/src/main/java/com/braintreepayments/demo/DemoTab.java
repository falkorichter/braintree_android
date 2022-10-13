package com.braintreepayments.demo;

public enum DemoTab {
    FEATURES(0),
    CONFIG(1),
    SETTINGS(2);

    private final int value;

    DemoTab(int value) {
        this.value = value;
    }

    public static DemoTab from(int value) {
        for (DemoTab demoTab : DemoTab.values()) {
            if (demoTab.value == value) {
                return demoTab;
            }
        }
        return null;
    }
}
