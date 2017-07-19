package timeroute.androidbaby.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timeroute.androidbaby.R;
import timeroute.androidbaby.ui.base.IBaseActivity;
import timeroute.androidbaby.ui.presenter.PostPresenter;
import timeroute.androidbaby.ui.view.IPostView;
import timeroute.androidbaby.widget.HorizontalListView;

public class PostActivity extends IBaseActivity<IPostView, PostPresenter> implements IPostView {

    private static final String TAG = "PostActivity";

    private List<Uri> pics;
    List<Map<String, Object>> list;
    private SimpleAdapter simpleAdapter;

    @Bind(R.id.editTextContent)
    EditText editTextContent;
    @Bind(R.id.imageButtonPick)
    ImageButton imageButtonPick;
    @Bind(R.id.listViewPic)
    HorizontalListView listViewPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        getSupportActionBar().setTitle(getString(R.string.activity_post));
        list = new ArrayList<Map<String, Object>>();
        simpleAdapter = new SimpleAdapter(this,
                list,
                R.layout.layout_pic,
                new String[]{"pic"},
                new int[]{R.id.image_view_pic});
        imageButtonPick.setOnClickListener(view -> {
            int count = 9-list.size();
            if(count == 0){
                Toast.makeText(this, "已添加至最大數量", Toast.LENGTH_SHORT).show();
            }else {
                openGallery(count);
            }
        });
    }

    private void openGallery(int i) {
        Matisse.from(PostActivity.this)
                .choose(MimeType.ofAll())
                .countable(true)
                .addFilter(new Filter() {
                    @Override
                    protected Set<MimeType> constraintTypes() {
                        return new HashSet<MimeType>() {{
                            add(MimeType.GIF);
                        }};
                    }

                    @Override
                    public IncapableCause filter(Context context, Item item) {
                        if (!needFiltering(context, item))
                            return null;

                        Point size = PhotoMetadataUtils.getBitmapBound(context.getContentResolver(), item.getContentUri());
                        if (size.x < 320 || size.y < 320 || item.size > 5) {
                            return new IncapableCause(IncapableCause.DIALOG, context.getString(R.string.error_gif, 320,
                                    String.valueOf(PhotoMetadataUtils.getSizeInMB(5))));
                        }
                        return null;
                    }
                })
                .theme(R.style.Matisse_Dracula)
                .maxSelectable(i)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(20);
    }

    @Override
    public boolean canBack() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    protected PostPresenter createPresenter() {
        return new PostPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_post;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        switch (requestCode) {
            case 20:
                listViewPic.setVisibility(View.VISIBLE);
                setData(Matisse.obtainResult(data));
                listViewPic.setAdapter(simpleAdapter);
                listViewPic.setOnItemClickListener((adapterView, view, i, l) -> {
                    Log.d("pic", view.findViewById(R.id.image_view_pic).toString());
                    new AlertDialog.Builder(this)
                            .setTitle("删除")
                            .setMessage("删除该图片?")
                            .setPositiveButton("确定", (dialog, j) -> {
                                list.remove(i);
                                simpleAdapter.notifyDataSetChanged();
                            })
                            .setNegativeButton("取消", null)
                    .show();
                });
                Log.d(TAG, "pics:\t"+pics);
                break;
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
    }

    public void setData(List<Uri> pics){
        for(int i=0;i<pics.size();i++){
            Map<String, Object> map = new HashMap<String, Object>();
            new Compressor(this)
                    .compressToFileAsFlowable(new File(getImagePath(pics.get(i), null)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((file) -> {
                        map.put("pic", file);
                        list.add(map);
                        simpleAdapter.notifyDataSetChanged();
                    }, (throwable) -> {
                        throwable.printStackTrace();
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_send:
                String content = editTextContent.getText().toString();
                if(content.length() == 0){
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                }else {
                    mPresenter.getImageToken(list, content);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}