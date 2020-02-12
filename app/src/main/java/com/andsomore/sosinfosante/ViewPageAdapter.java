package com.andsomore.sosinfosante;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPageAdapter extends FragmentPagerAdapter {
    private final List<Fragment> listFragment=new ArrayList<>();
    private final List<String> listFragmentTitles=new ArrayList<>();

    public ViewPageAdapter(FragmentManager fm) {
        super(fm);
    }




    @Override
    public Fragment getItem(int position) {
        return listFragment.get(position);
    }
    @Override
    public int getItemPosition(Object object) { return POSITION_NONE; }

    @Override
    public int getCount() {
        return listFragmentTitles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return listFragmentTitles.get(position);
    }

    public void AddFragment(Fragment fragment, String title){
        listFragment.add(fragment);
        listFragmentTitles.add(title);
    }

}