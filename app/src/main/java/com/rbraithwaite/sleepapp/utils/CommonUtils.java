package com.rbraithwaite.sleepapp.utils;

public class CommonUtils
{
//*********************************************************
// constructors
//*********************************************************

    private CommonUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    
    /**
     * obj is required to have a default no-arg ctor.
     */
    // IDEA [21-02-9 12:21AM] -- it would be nice if I could use Constructor.newInstance() here
    //  to lazy init any class with any constructor args.
    //  --
    //  alt solution: pass an abstract Factory <T> { T createInstance() } interface instead of a
    //  class and use that to construct the instance (idea from https://stackoverflow.com/a/300526).
    public static <T> T lazyInit(T obj, Class<T> objClass) throws
            InstantiationException,
            IllegalAccessException
    {
        if (obj == null) {
            obj = objClass.newInstance();
        }
        return obj;
    }
}
