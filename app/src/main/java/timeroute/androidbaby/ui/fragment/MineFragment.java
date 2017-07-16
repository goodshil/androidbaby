package timeroute.androidbaby.ui.fragment;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import timeroute.androidbaby.R;
import timeroute.androidbaby.ui.activity.MainActivity;
import timeroute.androidbaby.ui.activity.MineProfileActivity;
import timeroute.androidbaby.ui.base.IBaseFragment;
import timeroute.androidbaby.ui.presenter.MinePresenter;
import timeroute.androidbaby.ui.view.IMineView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MineFragment extends IBaseFragment<IMineView, MinePresenter> {

    private LinearLayoutManager mLayoutManager;

    @Bind(R.id.avatar)
    ImageView avatar_imageView;
    @Bind(R.id.nickname)
    TextView nickname_TextView;
    @Bind(R.id.assignment)
    TextView assignment_TextView;
    @Bind(R.id.mine_feed)
    LinearLayout feed_layout;
    @Bind(R.id.mine_profile)
    LinearLayout profile_layout;
    @Bind(R.id.mine_setting)
    LinearLayout setting_layout;
    @Bind(R.id.mine_about)
    LinearLayout about_layout;

    public MineFragment() {
        // Required empty public constructor
    }

    @Override
    protected MinePresenter createPresenter() {
        return new MinePresenter(getContext());
    }

    @Override
    protected int createViewLayoutId() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void initView(View rootView) {
        mLayoutManager = new LinearLayoutManager(getContext());
        profile_layout.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), MineProfileActivity.class);
            startActivity(intent);
        });
    }
}
