package com.rbraithwaite.sleepapp.ui.interruption_details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.common.views.details_fragment.DetailsFragment;
import com.rbraithwaite.sleepapp.ui.common.views.details_fragment.DetailsResult;

public class InterruptionDetailsFragment
        extends DetailsFragment<InterruptionWrapper, InterruptionDetailsFragmentViewModel>
{
//*********************************************************
// public helpers
//*********************************************************

    public static class Result
            extends DetailsResult<InterruptionWrapper> {}
    
    public static class Args
            extends DetailsFragment.Args<InterruptionWrapper> {}
    
//*********************************************************
// overrides
//*********************************************************

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.interruption_details_fragment, container, false);
    }
    
    @Override
    protected DetailsFragment.Args<InterruptionWrapper> getDetailsArgs()
    {
        InterruptionDetailsFragmentArgs safeArgs =
                InterruptionDetailsFragmentArgs.fromBundle(getArguments());
        return safeArgs.getArgs();
    }
    
    @Override
    protected Class<? extends DetailsResult<InterruptionWrapper>> getResultClass()
    {
        return InterruptionDetailsFragment.Result.class;
    }
    
    @Override
    protected DeleteDialogParams getDeleteDialogParams()
    {
        DeleteDialogParams params = new DeleteDialogParams();
        params.titleId = R.string.interruption_details_delete_title;
        params.messageId = R.string.permanent_operation_message;
        return params;
    }
    
    @Override
    protected Properties<InterruptionDetailsFragmentViewModel> initProperties()
    {
        return new Properties<>(false, InterruptionDetailsFragmentViewModel.class);
    }
}
