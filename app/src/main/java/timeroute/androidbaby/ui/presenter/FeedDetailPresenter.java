package timeroute.androidbaby.ui.presenter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timeroute.androidbaby.api.exception.ApiException;
import timeroute.androidbaby.api.exception.ExceptionEngine;
import timeroute.androidbaby.bean.feed.Comment;
import timeroute.androidbaby.bean.feed.CommentTimeLine;
import timeroute.androidbaby.support.MyObserver;
import timeroute.androidbaby.ui.adapter.CommentListAdapter;
import timeroute.androidbaby.ui.base.BasePresenter;
import timeroute.androidbaby.ui.view.IFeedDetailView;
import timeroute.androidbaby.ui.view.RecyclerViewClickListener;
import timeroute.androidbaby.util.SharedPreferenceUtils;

/**
 * Created by chinesejar on 17-7-30.
 */

public class FeedDetailPresenter extends BasePresenter<IFeedDetailView> {
    private static final String TAG = "FeedDetailPresenter";

    private SharedPreferenceUtils sharedPreferenceUtils;
    private String token;

    private Context context;
    private IFeedDetailView feedDetailView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private CommentTimeLine timeLine;
    private CommentListAdapter adapter;
    private int lastVisibleItem;
    private boolean isLoadMore = false;
    private String next;

    public FeedDetailPresenter(Context context){
        this.context = context;
        sharedPreferenceUtils = new SharedPreferenceUtils(this.context, "user");
        token = sharedPreferenceUtils.getString("token");
    }

    public void getLatestComment(int feed_id) {
        feedDetailView = getView();
        if(feedDetailView != null){
            mRecyclerView = feedDetailView.getRecyclerView();
            layoutManager = feedDetailView.getLayoutManager();

            feedApi.getLatestComment("JWT "+token, feed_id)
                    .onErrorResumeNext(new Func1<Throwable, Observable<? extends CommentTimeLine>>() {
                        @Override
                        public Observable<? extends CommentTimeLine> call(Throwable throwable) {
                            return Observable.error(ExceptionEngine.handleException(throwable));
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<CommentTimeLine>() {
                        @Override
                        protected void onError(ApiException ex) {
                            Toast.makeText(context, ex.getDisplayMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(CommentTimeLine commentTimeLine) {
                            Log.d(TAG, commentTimeLine.toString());
                            disPlayFeedList(commentTimeLine, context, feedDetailView, mRecyclerView);
                        }
                    });
        }
    }

    private void disPlayFeedList(CommentTimeLine commentTimeLine, Context context, IFeedDetailView feedDetailView, RecyclerView recyclerView) {
        Log.d(TAG, "next: "+next);
        if (isLoadMore) {
            if (next == null) {
                adapter.updateLoadStatus(adapter.LOAD_NONE);
                feedDetailView.setDataRefresh(false);
                return;
            }
            else {
                timeLine.getComments().addAll(commentTimeLine.getComments());
            }
            adapter.notifyDataSetChanged();
        } else {
            timeLine = commentTimeLine;
            adapter = new CommentListAdapter(context, timeLine);
            recyclerView.setAdapter(adapter);
            new Handler().postDelayed(() -> {
                adapter.notifyDataSetChanged();
            }, 2000);
        }
        next = commentTimeLine.getNext();
        if(next == "null"){
            next = null;
        }
        feedDetailView.setDataRefresh(false);
    }

    public void scrollRecycleView() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    lastVisibleItem = layoutManager
                            .findLastVisibleItemPosition();
                    if (layoutManager.getItemCount() == 1) {
                        adapter.updateLoadStatus(adapter.LOAD_NONE);
                        return;
                    }
                    if(next == null){
                        if(adapter != null){
                            adapter.updateLoadStatus(adapter.LOAD_NONE);
                        }
                        return;
                    }
                    if (lastVisibleItem + 1 == layoutManager
                            .getItemCount()) {
                        adapter.updateLoadStatus(adapter.LOAD_PULL_TO);
                        isLoadMore = true;
                        adapter.updateLoadStatus(adapter.LOAD_MORE);
                        //new Handler().postDelayed(() -> getNextComment(feedDetailView.getUserId()), 1000);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });
    }
}