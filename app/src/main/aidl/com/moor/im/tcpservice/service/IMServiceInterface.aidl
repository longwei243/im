// IMServiceInterface.aidl
package com.moor.im.tcpservice.service;

// Declare any non-default types here with import statements

interface IMServiceInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void login(String username, String passwd);

    void logoff();

    void join(IBinder token);

    void leave();
}
