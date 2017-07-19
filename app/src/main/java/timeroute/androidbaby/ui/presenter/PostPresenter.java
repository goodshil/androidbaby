package timeroute.androidbaby.ui.presenter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.qiniu.android.storage.UploadManager;

import java.io.File;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timeroute.androidbaby.bean.feed.Feed;
import timeroute.androidbaby.bean.user.ImageToken;
import timeroute.androidbaby.ui.adapter.FeedListAdapter;
import timeroute.androidbaby.ui.base.BasePresenter;
import timeroute.androidbaby.ui.view.IDiscoveryView;
import timeroute.androidbaby.ui.view.IFeedView;
import timeroute.androidbaby.ui.view.IPostView;
import timeroute.androidbaby.util.SharedPreferenceUtils;

/**
 * Created by chinesejar on 17-7-14.
 */

public class PostPresenter extends BasePresenter<IPostView> {

    private static final String TAG = "PostPresenter";

    private Context context;
    private IPostView postView;
    private SharedPreferenceUtils sharedPreferenceUtils;

    public PostPresenter(Context context){
        this.context = context;
        sharedPreferenceUtils = new SharedPreferenceUtils(this.context, "user");
    }

    public void getImageToken(List<Map<String, Object>> list, String content){
        String token = sharedPreferenceUtils.getString("token");
        postView = getView();
        if(postView != null){
            feedApi.getImageToken("Token "+token, list.size())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(tokens -> {
                        for(int i=0;i<tokens.size();i++){
                            Log.d(TAG, tokens.get(i).getToken());
                        }
                        postImage(list, content, tokens);
                    }, this::loadError);
        }
    }

    public void postImage(List<Map<String, Object>> list, String content, List<ImageToken> tokens){
        UploadManager uploadManager = new UploadManager();
        int id = sharedPreferenceUtils.getInt("id");
        long cur_timestamp = System.currentTimeMillis();
        for(int i=0;i<tokens.size();i++){
            String name = id+"_"+cur_timestamp+"_"+i+".jpg";
            uploadManager.put((File)list.get(i).get("pic"), name, tokens.get(i).getToken(), (key, info, response) -> {
                if(info.isOK()) {
                    Log.i("qiniu", "Upload Success");
                    Feed feed = new Feed();
                    int id = sharedPreferenceUtils.getInt("id");
                    String token = sharedPreferenceUtils.getString("token");
                    feedApi.postFeed("Token "+token, id, profile)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(avoid -> {
                                Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show();
                                sharedPreferenceUtils.setString("avatar", baseAvatarUrl+name);
                                mineView.setAvatar(baseAvatarUrl+name);
                            }, this::loadError);
                } else {
                    Log.i("qiniu", "Upload Fail");
                    Toast.makeText(context, "更新失败", Toast.LENGTH_SHORT).show();
                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                }
                Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + response);
            }, null);
        }
    }

    private void loadError(Throwable throwable) {
        throwable.printStackTrace();
        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
    }

}