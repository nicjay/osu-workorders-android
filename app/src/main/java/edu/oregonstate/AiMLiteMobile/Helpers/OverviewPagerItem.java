package edu.oregonstate.AiMLiteMobile.Helpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import edu.oregonstate.AiMLiteMobile.Fragments.OverviewListFragment;

/**
 * Created by sellersk on 5/4/2015.
 */
public class OverviewPagerItem {

    private CharSequence mTitle;
    private final int mIndicatorColor;
    private final int mDividerColor;
    private final Fragment mFragment;

    public OverviewPagerItem(CharSequence title, int sectionCount, int indicatorColor, int dividerColor) {
        mTitle = title;// + " - " +String.valueOf(sectionCount);
        mIndicatorColor = indicatorColor;
        mDividerColor = dividerColor;
        mFragment = new OverviewListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sectionFilter", title.toString());
        mFragment.setArguments(bundle);
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void updateTitle(CharSequence title, int sectionCount){
        mTitle = title + " - " +String.valueOf(sectionCount);
    };


    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    int getDividerColor() {
        return mDividerColor;
    }

    public Fragment getFragment() {
        return mFragment;
    }
}