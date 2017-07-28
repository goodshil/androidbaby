package timeroute.androidbaby.ui.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import timeroute.androidbaby.R;
import timeroute.androidbaby.ui.view.ImageViewClickListener;
import timeroute.androidbaby.widget.TouchImageView;


public class ViewPagerImageAdapter extends PagerAdapter {

    private View mCurrentView;

    private ArrayList<String> IMAGES;
    private LayoutInflater inflater;
    private Context context;
    private ImageViewClickListener listener;


    public ViewPagerImageAdapter(Context context, ArrayList<String> IMAGES, ImageViewClickListener listener) {
        this.context = context;
        this.IMAGES=IMAGES;
        this.listener = listener;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.layout_image_view, view, false);

        assert imageLayout != null;
        final ImageView imageView = (TouchImageView) imageLayout
                .findViewById(R.id.image);

        Picasso.with(context)
                .load(IMAGES.get(position))
                .into(imageView);
        view.addView(imageLayout, 0);
        imageView.setOnClickListener(view1 -> {
            listener.toggleActionBar();
        });
        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mCurrentView = (View)object;
    }

    public View getPrimaryItem() {
        return mCurrentView;
    }
}