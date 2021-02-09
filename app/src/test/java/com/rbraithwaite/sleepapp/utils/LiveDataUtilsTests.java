package com.rbraithwaite.sleepapp.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class LiveDataUtilsTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void Merger_receivesCorrectValuesFrom_merge()
    {
        final LiveData<String> testA = new MutableLiveData<>("wassup");
        final LiveData<Integer> testB = new MutableLiveData<>(123);
        
        LiveData<Boolean> result = LiveDataUtils.merge(
                testA,
                testB,
                new LiveDataUtils.Merger<String,
                        Integer, Boolean>()
                {
                    @Override
                    public Boolean applyMerge(
                            String testAVal,
                            Integer testBVal)
                    {
                        return (testAVal.equals(testA.getValue()) &&
                                testBVal.equals(testB.getValue()));
                    }
                });
        
        TestUtils.activateLocalLiveData(result);
        assertThat(result.getValue(), is(true));
    }
}
