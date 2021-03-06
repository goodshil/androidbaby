package timeroute.androidbaby.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

import timeroute.androidbaby.ui.base.IBaseFragment;

/**
 * Created by chinesejar on 17-7-14.
 */

public class ViewPagerFgAdapter extends FragmentPagerAdapter {

    private String[] mTitles = new String[]{"评论", "点赞"};

    private List<IBaseFragment> fragmentList;

    public ViewPagerFgAdapter(FragmentManager supportFragmentManager, List<IBaseFragment> fragmentList){
        super(supportFragmentManager);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position){
        return fragmentList.get(position);
    }

    @Override
    public int getCount(){
        if(fragmentList!=null){
            return fragmentList.size();
        }
        return 0;
    }

    //ViewPager与TabLayout绑定后，这里获取到PageTitle就是Tab的Text
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        Log.d("destroy", "pos: "+position);
    }
}
