package timeroute.androidbaby.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timeroute.androidbaby.R;
import timeroute.androidbaby.api.exception.ApiException;
import timeroute.androidbaby.api.exception.ExceptionEngine;
import timeroute.androidbaby.bean.feed.Comment;
import timeroute.androidbaby.bean.feed.CommentTimeLine;
import timeroute.androidbaby.bean.feed.Feed;
import timeroute.androidbaby.bean.feed.Like;
import timeroute.androidbaby.bean.user.Profile;
import timeroute.androidbaby.support.MyObserver;
import timeroute.androidbaby.ui.adapter.CommentListAdapter;
import timeroute.androidbaby.ui.base.BasePresenter;
import timeroute.androidbaby.ui.view.IFeedDetailView;
import timeroute.androidbaby.ui.view.RecyclerViewClickListener;
import timeroute.androidbaby.util.SharedPreferenceUtils;

import static timeroute.androidbaby.ui.adapter.CommentListAdapter.LOAD_MORE;
import static timeroute.androidbaby.ui.adapter.CommentListAdapter.LOAD_NONE;
import static timeroute.androidbaby.ui.adapter.CommentListAdapter.LOAD_PULL_TO;

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

    public void getFeedDetail(int feed_id) {
        feedDetailView = getView();
        if(feedDetailView != null){
            mRecyclerView = feedDetailView.getRecyclerView();
            layoutManager = feedDetailView.getLayoutManager();

            feedApi.getFeed("JWT "+token, feed_id)
                    .onErrorResumeNext(throwable -> Observable.error(ExceptionEngine.handleException(throwable)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<Feed>() {
                        @Override
                        protected void onError(ApiException ex) {
                            Toast.makeText(context, ex.getDisplayMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(Feed feed) {
                            Log.d(TAG, feed.toString());
                            feedDetailView.setFeed(feed);
                        }
                    });
        }
    }

    public void getLatestComment(int feed_id) {
        feedDetailView = getView();
        if(feedDetailView != null){
            mRecyclerView = feedDetailView.getRecyclerView();
            layoutManager = feedDetailView.getLayoutManager();

            feedApi.getLatestComment("JWT "+token, feed_id)
                    .onErrorResumeNext(throwable -> Observable.error(ExceptionEngine.handleException(throwable)))
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
                            disPlayCommentList(commentTimeLine, context, feedDetailView, mRecyclerView);
                        }
                    });
        }
    }

    public void postLike(Feed feed){
        feedDetailView = getView();
        if(feedDetailView!=null){
            mRecyclerView = feedDetailView.getRecyclerView();
            layoutManager = feedDetailView.getLayoutManager();
            Like like = new Like();
            like.setFeed_id(feed);
            feedApi.postLike("JWT "+ token, like)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Response<Object>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Response<Object> objectResponse) {
                            if(objectResponse.code() == 201){
                                feed.setLike_count(feed.getLikeCount()+1);
                                feedDetailView.updateLikeStatus();
                            }else if(objectResponse.code() == 200){
                                Toast.makeText(context, context.getString(R.string.like_again), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void postComment(int feed_id, int user_id, String content) {
        feedDetailView = getView();
        if(feedDetailView != null){
            mRecyclerView = feedDetailView.getRecyclerView();
            layoutManager = feedDetailView.getLayoutManager();

            Comment comment = new Comment();
            Feed feed = new Feed();
            feed.setFeedId(feed_id);
            comment.setFeedId(feed);
            comment.setContent(content);
            if(user_id != -1){
                Profile at = new Profile();
                at.setId(user_id);
                comment.setAt(at);
            }
            Log.d(TAG, comment.toString());
            feedApi.postComment("JWT "+token, comment)
                    .onErrorResumeNext(throwable -> Observable.error(ExceptionEngine.handleException(throwable)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<Comment>() {
                        @Override
                        protected void onError(ApiException ex) {
                            Toast.makeText(context, ex.getDisplayMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(Comment comment) {
                            Log.d(TAG, comment.toString());
                            timeLine.getComments().add(0, comment);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(context, context.getString(R.string.comment_success), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void deleteFeed(Feed feed) {
        feedDetailView = getView();
        if(feedDetailView != null) {
            feedApi.deleteFeed("JWT "+token, feed.getFeedId())
                    .onErrorResumeNext(throwable -> Observable.error(ExceptionEngine.handleException(throwable)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<Response<Object>>() {
                        @Override
                        protected void onError(ApiException ex) {
                            Toast.makeText(context, ex.getDisplayMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onNext(Response<Object> objectResponse) {
                            Toast.makeText(context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                            feedDetailView.backToParent();
                        }
                    });
        }
    }

    private void disPlayCommentList(CommentTimeLine commentTimeLine, Context context, IFeedDetailView feedDetailView, RecyclerView recyclerView) {
        Log.d(TAG, "next: "+next);
        if (isLoadMore) {
            if (next == null) {
                adapter.updateLoadStatus(LOAD_NONE);
                feedDetailView.setDataRefresh(false);
                return;
            }
            else {
                timeLine.getComments().addAll(commentTimeLine.getComments());
            }
            adapter.notifyDataSetChanged();
        } else {
            timeLine = commentTimeLine;
            adapter = new CommentListAdapter(context, timeLine, new RecyclerViewClickListener() {
                @Override
                public void onAvatarClicked(int user_id, String nickname, String assignment, String avatar) {
                    feedDetailView.goToUser(user_id, nickname, assignment, avatar);
                }

                @Override
                public void onLikeClicked(Feed feed) {
                }

                @Override
                public void onCommentClicked(Feed feed) {

                }

                @Override
                public void onCardViewClick(Feed feed) {

                }

                @Override
                public void onImageViewClick(int i, String[] images) {

                }

                @Override
                public void onAtClicked(Profile profile) {
                    feedDetailView.openCommentDialog(true, profile);
                }
            });
            recyclerView.setAdapter(adapter);
            new Handler().postDelayed(() -> adapter.notifyDataSetChanged(), 2000);
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
                        adapter.updateLoadStatus(LOAD_NONE);
                        return;
                    }
                    if(next == null){
                        if(adapter != null){
                            adapter.updateLoadStatus(LOAD_NONE);
                        }
                        return;
                    }
                    if (lastVisibleItem + 1 == layoutManager
                            .getItemCount()) {
                        adapter.updateLoadStatus(LOAD_PULL_TO);
                        isLoadMore = true;
                        adapter.updateLoadStatus(LOAD_MORE);
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
