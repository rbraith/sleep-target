package com.rbraithwaite.sleepapp.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
                (testAVal, testBVal) -> (testAVal.equals(testA.getValue()) &&
                        testBVal.equals(testB.getValue())));
        
        TestUtils.activateLocalLiveData(result);
        assertThat(result.getValue(), is(true));
    }
    
    @Test
    public void merge_tracksChangesToInputs()
    {
        final MutableLiveData<String> testA = new MutableLiveData<>("wassup");
        final MutableLiveData<Integer> testB = new MutableLiveData<>(123);
    
        LiveData<String> result = LiveDataUtils.merge(
                testA,
                testB,
                (testAVal, testBVal) -> testAVal + testBVal);
    
        TestUtils.activateLocalLiveData(result);
        
        testA.setValue("heyhey");
        assertThat(result.getValue(), is(equalTo("heyhey123")));
        
        testB.setValue(321);
        assertThat(result.getValue(), is(equalTo("heyhey321")));
    }
}
