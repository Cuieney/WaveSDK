package com.feetsdk.android.feetsdk.stepcount;

/**
 * Created by cuieney on 16/12/21.
 */
public interface IController {
    void destroyService();
    void getStepChange(IStepChange change);
}
