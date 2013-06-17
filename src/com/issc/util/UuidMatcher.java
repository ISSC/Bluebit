// vim: et sw=4 sts=4 tabstop=4
package com.issc.util;

import com.issc.Bluebit;
import com.issc.R;

import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class UuidMatcher {

    Set<UUID> mRules;

    private String mPkgName;
    private String mActName;

    private CharSequence mTitle;
    private CharSequence mDesc;

    private final static String sPREFIX = "0000";
    private final static String sPOSTFIX = "-0000-1000-8000-00805f9b34fb";

    public UuidMatcher() {
        mRules = new TreeSet<UUID>();
    }

    public void setTarget(String pkg, String act) {
        mPkgName = pkg;
        mActName = act;
    }

    public void setInfo(CharSequence title, CharSequence desc) {
        mTitle = title;
        mDesc  = desc;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public CharSequence getDesc() {
        return mDesc;
    }

    /**
     * Add rule to this matcher.
     *
     * @param str short Uuid in string. i.e: "1800" for GAP.
     *        It will be converted to 00001800-0000-1000-8000-00805f9b34fb
     */
    public boolean addShortRule(String str) {
        if (!str.matches(".{4}")) {
            return false;
        } else {
            return addRule(fromUuid16(str));
        }
    }

    /**
     * Add rule to this matcher.
     *
     * @param str Uuid in string. It should be 128-bit UUID such as
     *        00001800-0000-1000-8000-00805f9b34fb
     */
    public boolean addRule(String uuidStr) {
        // regulare expression to match something like
        // 00001800-0000-1000-8000-00805f9b34fb
        if (!uuidStr.matches(".{8}-.{4}-.{4}-.{4}-.{12}")) {
            return false;
        } else {
            return addRule(UUID.fromString(uuidStr));
        }
    }

    /**
     * Add rule to this matcher.
     */
    public boolean addRule(UUID newRule) {
        if (mRules.contains(newRule)) {
            return false;
        } else {
            mRules.add(newRule);
            return true;
        }
    }

    public boolean containsShort(String str) {
        if (!str.matches(".{4}")) {
            return false;
        } else {
            return contains(fromUuid16(str));
        }
    }

    public boolean contains(String uuidStr) {
        // regulare expression to match something like
        // 00001800-0000-1000-8000-00805f9b34fb
        if (!uuidStr.matches(".{8}-.{4}-.{4}-.{4}-.{12}")) {
            return false;
        } else {
            return mRules.contains(UUID.fromString(uuidStr));
        }
    }

    public boolean contains(UUID target) {
        return mRules.contains(target);
    }

    /**
     * To determine the relationship between the List and this Rule.
     *
     * @return True if the List is exactly match this Rule.
     */
    public boolean equals(List<UUID> uuids) {
        if (uuids.size() != mRules.size()) {
            return false;
        }

        return (encloses(uuids) && isEnclosedBy(uuids));
    }

    /**
     * To determine the relationship between the List and this Rule.
     *
     * @return True if each uuids of the List is enclosed by this UuidMatcher.
     */
    public boolean encloses(List<UUID> uuids) {
        return mRules.containsAll(uuids);
    }

    /**
     * To determine the relationship between the List and this Rule.
     *
     * @return True if each uuids of this UuidMatcher is enclosed by List.
     *         the List might has more Uuids than this rule.
     */
    public boolean isEnclosedBy(List<UUID> uuids) {
        return uuids.containsAll(mRules);
    }

    /**
     * Expand String from uuid16 to 128-bit UUID.
     *
     * SIG defines BASE_UUID as 00000000-0000-1000-8000-00805F9B34FB.
     * This function helps you to convert 16-bit "0001" to 128-bti UUID.
     */
    public final static String fromUuid16(String shortStr) {
        StringBuilder sb = new StringBuilder();
        sb.append(sPREFIX);
        sb.append(shortStr);
        sb.append(sPOSTFIX);
        return sb.toString();
    }
}

