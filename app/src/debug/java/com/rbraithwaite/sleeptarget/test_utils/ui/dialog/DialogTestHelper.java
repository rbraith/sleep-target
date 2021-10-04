/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.test_utils.ui.dialog;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.test.core.app.ActivityScenario;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;



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
// public constants
//*********************************************************

    public static final String DIALOG_TAG = "test dialog";

//*********************************************************
// public helpers
//*********************************************************

    public interface SyncedDialogAction<D extends DialogFragment>
    {
        public void perform(D dialogFragment);
    }
    
    public static class Activity
            extends AppCompatActivity {}

//*********************************************************
// constructors
//*********************************************************

    private DialogTestHelper(final Class<DialogType> dialogType, final Bundle args)
    {
        mScenario = ActivityScenario.launch(DialogTestHelper.Activity.class);
        TestUtils.performSyncedActivityAction(
                mScenario, activity -> setupActivityWithDialog(activity, dialogType, args));
    }
    
    private DialogTestHelper(final DialogType dialogInstance)
    {
        mScenario = ActivityScenario.launch(DialogTestHelper.Activity.class);
        TestUtils.performSyncedActivityAction(
                mScenario,
                activity -> dialogInstance.show(activity.getSupportFragmentManager(), DIALOG_TAG)
        );
    }


//*********************************************************
// api
//*********************************************************

    
    /**
     * I needed this as some dialogs (eg DatePickerFragment) required initialization logic outside
     * of the provided arguments. (eg DatePickerFragment.setOnDateSetListener)
     */
    public static <DialogType extends DialogFragment> DialogTestHelper<DialogType> launchProvidedInstance(
            DialogType instance)
    {
        return new DialogTestHelper<>(instance);
    }
    
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
    
    public void performSyncedDialogAction(final SyncedDialogAction<DialogType> syncedDialogAction)
    {
        TestUtils.performSyncedActivityAction(
                mScenario,
                activity -> {
                    DialogType dialog = (DialogType) activity.getSupportFragmentManager()
                            .findFragmentByTag(DIALOG_TAG);
                    syncedDialogAction.perform(dialog);
                }
        );
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
            dialog.show(activity.getSupportFragmentManager(), DIALOG_TAG);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
