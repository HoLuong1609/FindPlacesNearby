package fragments;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.motthoidecode.findplacesnearby.R;

import java.util.Stack;

/**
 * Created by Administrator on 7/2/2016.
 */
public class MyFragmentManager {
    private static Stack<Fragment> fragmentStack;
    private static FragmentManager fmgr;

    public static void displayFragment(Fragment pFragment, FragmentActivity activity) {
        // String backStateName = pFragment.getClass().getName();
        try {
            if (!activity.isFinishing()) {
                final FragmentTransaction ft= getFmgr().beginTransaction();
                ft.add(R.id.main_container, pFragment);
                if (getFragmentStack().size() > 0) {
                    getFragmentStack().lastElement().onPause();
                    ft.hide(getFragmentStack().lastElement());

                }
                getFragmentStack().push(pFragment);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        //ft.commit();
                        ft.commitAllowingStateLoss();
                    }
                });
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public static void initFragmentStack() {
        fragmentStack = new Stack<Fragment>();
    }

    public static void backToPreviousFragment(){
        if (getFragmentStack().size() >= 2) {
            FragmentTransaction ft = getFmgr().beginTransaction();
            getFragmentStack().lastElement().onPause();
            ft.remove(getFragmentStack().pop());
            getFragmentStack().lastElement().onResume();
            ft.show(getFragmentStack().lastElement());
            ft.commit();

        }
    }

    public static FragmentManager getFmgr() {
        return fmgr;
    }

    public static void setFragmentManager(FragmentManager fmgr) {
        MyFragmentManager.fmgr = fmgr;
    }


    public static Stack<Fragment> getFragmentStack() {
        return fragmentStack;
    }

    public static void setFragmentStack(Stack<Fragment> fragmentStack) {
        MyFragmentManager.fragmentStack = fragmentStack;
    }
}
