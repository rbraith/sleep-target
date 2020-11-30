package com.rbraithwaite.sleepapp.test_utils.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.test.core.app.ActivityScenario;

import com.rbraithwaite.sleepapp.TestUtils;


/**
 * This simply launches a specified dialog fragment in an otherwise blank activity scenario.
 */
public class DialogTestHelper<DialogType extends DialogFragment>
{
//*********************************************************
// private properties
//*********************************************************

    private ActivityScenario<DialogTestHelper.Activity> mScenario;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Activity
            extends AppCompatActivity {}
    
//*********************************************************
// constructors
//*********************************************************

    private DialogTestHelper(final Class<DialogType> dialogType, final Bundle args)
    {
        mScenario = ActivityScenario.launch(DialogTestHelper.Activity.class);
        TestUtils.performSyncedActivityAction(
                mScenario, new TestUtils.SyncedActivityAction<DialogTestHelper.Activity>()
                {
                    @Override
                    public void perform(DialogTestHelper.Activity activity)
                    {
                        setupActivityWithDialog(activity, dialogType, args);
                    }
                });
    }
    
//*********************************************************
// api
//*********************************************************

    public static <DialogType extends DialogFragment> DialogTestHelper<DialogType> launchDialog(
            final Class<DialogType> dialogType)
    {
        return launchDialogWithArgs(dialogType, null);
    }
    
    public static <DialogType extends DialogFragment> DialogTestHelper<DialogType> launchDialogWithArgs(
            final Class<DialogType> dialogType,
            final Bundle args)
    {
        return new DialogTestHelper<>(dialogType, args);
    }
    
    public ActivityScenario<DialogTestHelper.Activity> getScenario()
    {
        return mScenario;
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void setupActivityWithDialog(
            DialogTestHelper.Activity activity,
            final Class<DialogType> dialogType,
            final Bundle args)
    {
        try {
            DialogType dialog = dialogType.newInstance();
            if (args != null) {
                dialog.setArguments(args);
            }
            dialog.show(activity.getSupportFragmentManager(), "test dialog");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
