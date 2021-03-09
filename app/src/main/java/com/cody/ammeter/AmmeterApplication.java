package com.cody.ammeter;

import com.cody.ammeter.model.Repository;
import com.cody.component.app.BaseApplication;

public class AmmeterApplication extends BaseApplication {
    @Override
    public void onInit() {
        super.onInit();
        Repository.init(this);
    }
}
