# Introduction

Bluebit is an Android application that using Bluetooth 4.0 (BLE) to communicate ISSC EVB to use these BLE services

* Automation IO
* ISSC Transparent

To get more development information, please refer to the wiki of this project.

# Build

If you use [ant](http://ant.apache.org/) to build your Android application, just type

    $ ant debug install

And run activity by this command

    $ ant run-activity

Since we modified [custom_rules.xml](../blob/master/custom_rules.xml), you can run activity such easily by adding these lines to file **local.properties**

    sdk.dir=/path/to/your/android/sdk
    run.package="com.issc"
    run.activity=".ui.ActivityMain"
    run.parameters=""

# About

This project is funded by [ISSC Technology Corp.](http://www.issc-tech.com) and licensed under Apache 2.0.

This project use Android 4.3 (API Level 18) to use BLE.

In the previous release **v0.1.1** used Samsung BLE SDK so you have to download [Samsung BLE SDK](http://developer.samsung.com/ble) to run Bluebit in Galaxy S4.
