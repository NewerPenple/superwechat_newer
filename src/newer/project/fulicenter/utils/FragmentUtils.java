package newer.project.fulicenter.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import newer.project.fulicenter.R;

public class FragmentUtils {
    public static void startFragment(FragmentActivity context, Fragment fragment){
        FragmentManager manager = context.getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.fragment_container,fragment).commit();
    }

    public static void showFragment(FragmentActivity context,int currentTabIndex, int index, Fragment[] mFragments){
        FragmentTransaction trx = context.getSupportFragmentManager().beginTransaction();
        if(currentTabIndex>-1){
            trx.hide(mFragments[currentTabIndex]);
        }
        if (!mFragments[index].isAdded()) {
            trx.add(R.id.fragment_container, mFragments[index]);
        }
        trx.show(mFragments[index]).commit();
    }
}
