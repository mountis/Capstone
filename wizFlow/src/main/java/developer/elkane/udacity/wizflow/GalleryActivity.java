

package developer.elkane.udacity.wizflow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import developer.elkane.udacity.wizflow.models.Attachment;
import developer.elkane.udacity.wizflow.models.listeners.OnViewTouchedListener;
import developer.elkane.udacity.wizflow.models.views.InterceptorFrameLayout;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.StorageHelper;
import it.feio.android.simplegallery.models.GalleryPagerAdapter;
import it.feio.android.simplegallery.views.GalleryViewPager;


public class GalleryActivity extends AppCompatActivity {


    private static final boolean AUTO_HIDE = false;


    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;


    private static final boolean TOGGLE_ON_CLICK = true;

    @BindView(R.id.gallery_root)
    InterceptorFrameLayout galleryRootView;
    @BindView(R.id.fullscreen_content)
    GalleryViewPager mViewPager;

    private List<Attachment> images;
    OnViewTouchedListener screenTouches = new OnViewTouchedListener() {
        private final int MOVING_THRESHOLD = 30;
        float x;
        float y;
        private boolean status_pressed = false;


        @Override
        public void onViewTouchOccurred(MotionEvent ev) {
            if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                x = ev.getX();
                y = ev.getY();
                status_pressed = true;
            }
            if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
                float dx = Math.abs(x - ev.getX());
                float dy = Math.abs(y - ev.getY());
                double dxy = Math.sqrt(dx * dx + dy * dy);
                Log.d(Constants.TAG, "Moved of " + dxy);
                if (dxy >= MOVING_THRESHOLD) {
                    status_pressed = false;
                }
            }
            if ((ev.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                if (status_pressed) {
                    click();
                    status_pressed = false;
                }
            }
        }


        private void click() {
            Attachment attachment = images.get(mViewPager.getCurrentItem());
            if (attachment.getMime_type().equals(Constants.MIME_TYPE_VIDEO)) {
                viewMedia();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        initViews();
        initData();
    }

    @Override
    public void onStart() {
        ((WizFlow) getApplication()).getAnalyticsHelper().trackScreenView(getClass().getName());
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gallery, menu);
        return true;
    }

    private void initViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        galleryRootView.setOnViewTouchedListener(screenTouches);

        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                getSupportActionBar().setSubtitle("(" + (arg0 + 1) + "/" + images.size() + ")");
            }


            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }


            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void initData() {
        String title = getIntent().getStringExtra(Constants.GALLERY_TITLE);
        images = getIntent().getParcelableArrayListExtra(Constants.GALLERY_IMAGES);
        int clickedImage = getIntent().getIntExtra(Constants.GALLERY_CLICKED_IMAGE, 0);

        ArrayList<Uri> imageUris = new ArrayList<>();
        for (Attachment mAttachment : images) {
            imageUris.add(mAttachment.getUri());
        }

        GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(this, imageUris);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(clickedImage);

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle("(" + (clickedImage + 1) + "/" + images.size() + ")");

        if (images.get(clickedImage).getMime_type().equals(Constants.MIME_TYPE_VIDEO)) {
            viewMedia();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_gallery_share:
                shareMedia();
                break;
            case R.id.menu_gallery:
                viewMedia();
                break;
            default:
                Log.e(Constants.TAG, "Wrong element choosen: " + item.getItemId());
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewMedia() {
        Attachment attachment = images.get(mViewPager.getCurrentItem());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(attachment.getUri(),
                StorageHelper.getMimeType(this, attachment.getUri()));
        startActivity(intent);
    }

    private void shareMedia() {
        Attachment attachment = images.get(mViewPager.getCurrentItem());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(StorageHelper.getMimeType(this, attachment.getUri()));
        intent.putExtra(Intent.EXTRA_STREAM, attachment.getUri());
        startActivity(intent);
    }
}
