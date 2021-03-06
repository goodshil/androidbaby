package timeroute.androidbaby.ui.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import timeroute.androidbaby.R;
import timeroute.androidbaby.bean.feed.Feed;
import timeroute.androidbaby.bean.feed.FeedTimeLine;
import timeroute.androidbaby.ui.view.RecyclerViewClickListener;
import timeroute.androidbaby.util.RoundTransform;
import timeroute.androidbaby.util.ScreenUtil;

/**
 * Created by chinesejar on 17-7-8.
 */

public class FeedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FeedListAdapter";
    private RecyclerViewClickListener listener;
    private Context context;
    private FeedTimeLine feedTimeLine;
    private int status = 1;

    private List<Map<String, Object>> list;
    private FeedListPicAdapter feedListPicAdapter;

    public static final int LOAD_MORE = 0;
    public static final int LOAD_PULL_TO = 1;
    public static final int LOAD_NONE = 2;
    public static final int LOAD_END = 3;
    private static final int TYPE_FOOTER = -1;

    public FeedListAdapter(Context context, FeedTimeLine feedTimeLine, RecyclerViewClickListener listener) {
        this.listener = listener;
        this.context = context;
        this.feedTimeLine = feedTimeLine;
    }

    @Override
    public int getItemViewType(int position) {
        if (feedTimeLine.getFeeds() != null) {
            if (position + 1 == getItemCount()) {
                return TYPE_FOOTER;
            } else {
                return position;
            }
        } else if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return position;
        }
    }

    public void updateLoadStatus(int status) {
        this.status = status;
        notifyDataSetChanged();
    }

    public void updateLikeStatus(Feed feed) {
        List<Feed> feeds = feedTimeLine.getFeeds();
        int position = feeds.indexOf(feed);
        feeds.get(position).setLike_count(feed.getLikeCount()+1);
        notifyDataSetChanged();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.card_feed)
        CardView cardView;
        @Bind(R.id.avatar)
        ImageView avatar;
        @Bind(R.id.nickname)
        TextView nickname;
        @Bind(R.id.content)
        TextView content;
        @Bind(R.id.feed_pic_layout)
        RelativeLayout layout_feed_pic;
        @Bind(R.id.feed_pic)
        RecyclerView recyclerView;
        @Bind(R.id.image_count)
        TextView image_count;
        @Bind(R.id.like)
        TextView like;
        @Bind(R.id.imageButtonLike)
        ImageButton imageButtonLike;
        @Bind(R.id.comment)
        TextView comment;
        @Bind(R.id.imageButtonComment)
        ImageButton imageButtonComment;
        @Bind(R.id.create_time)
        TextView create_time;

        public FeedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            ScreenUtil screenUtil = ScreenUtil.instance(context);
            int screenWidth = screenUtil.getScreenWidth();
            cardView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

        }

        public void bindItem(Feed feed) {
            cardView.setOnClickListener(view -> {
                Log.d(TAG, "card click "+feed.getFeedId());
                listener.onCardViewClick(feed);
            });
            loadCirclePic(context, feed.getUser().getAvatar(), avatar);
            avatar.setOnClickListener(view -> {
                if(listener != null){
                    listener.onAvatarClicked(feed.getUser().getId(), feed.getUser().getNickname(), feed.getUser().getAssignment(), feed.getUser().getAvatar());
                }
            });
            nickname.setText(feed.getUser().getNickname());
            content.setText(feed.getContent());
            if (feed.getFeedPic().size() > 0) {
                layout_feed_pic.setVisibility(View.VISIBLE);
                image_count.setText(String.format(context.getString(R.string.image_count), feed.getFeedPic().size()));
                list = new ArrayList<>();
                String[] images = new String[feed.getFeedPic().size()];
                for (int i = 0; i < feed.getFeedPic().size(); i++) {
                    images[i] = feed.getFeedPic().get(i).getUrl();
                    Map<String, Object> map = new HashMap<>();
                    map.put("url", feed.getFeedPic().get(i).getUrl());
                    list.add(map);
                }

                feedListPicAdapter = new FeedListPicAdapter(context, list);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(feedListPicAdapter);
            }

            like.setText(String.valueOf(feed.getLikeCount()));
            imageButtonLike.setOnClickListener(view -> {
                listener.onLikeClicked(feed);
            });
            comment.setText(String.valueOf(feed.getCommentCount()));
            imageButtonComment.setOnClickListener(view -> {
                listener.onCommentClicked(feed);
            });
            create_time.setText(String.valueOf(feed.getCreate_time()));
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_load_prompt)
        TextView tv_load_prompt;
        @Bind(R.id.progress)
        ProgressBar progress;

        public FooterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtil.instance(context).dip2px(40));
            itemView.setLayoutParams(params);
        }

        private void bindItem() {
            switch (status) {
                case LOAD_MORE:
                    progress.setVisibility(View.VISIBLE);
                    tv_load_prompt.setText(context.getString(R.string.loading));
                    itemView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_PULL_TO:
                    progress.setVisibility(View.GONE);
                    tv_load_prompt.setText(context.getString(R.string.load_more));
                    itemView.setVisibility(View.VISIBLE);
                    break;
                case LOAD_NONE:
                    progress.setVisibility(View.GONE);
                    tv_load_prompt.setText(context.getString(R.string.load_none));
                    break;
                case LOAD_END:
                    itemView.setVisibility(View.GONE);
                default:
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return feedTimeLine == null ? 0 : feedTimeLine.getFeeds().size() + 1;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = View.inflate(parent.getContext(), R.layout.activity_view_footer, null);
            return new FooterViewHolder(view);
        } else {
            View view = View.inflate(parent.getContext(), R.layout.layout_feed, null);
            return new FeedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FeedViewHolder) {
            FeedViewHolder feedHolder = (FeedViewHolder) holder;
            feedHolder.bindItem(feedTimeLine.getFeeds().get(position));
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.bindItem();
        }
    }

    public static void loadCirclePic(final Context context, String url, ImageView imageView) {
        if(url != null){
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_android)
                    .transform(new RoundTransform())
                    .into(imageView);
        }

    }
}
