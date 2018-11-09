
package developer.elkane.udacity.wizflow.models;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;


public class ThumbnailLruCache extends LruCache<String, Bitmap> {

    final static int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    final static int cacheSize = maxMemory / 8;

    private static ThumbnailLruCache instance = null;

    private static LruCache<String, Bitmap> mMemoryCache;


    public ThumbnailLruCache(int maxSize) {
        super(maxSize);
    }


    public static synchronized ThumbnailLruCache getInstance() {
        if (instance == null) {
            instance = new ThumbnailLruCache(cacheSize);
        }

        mMemoryCache = new LruCache<>(cacheSize);

        return instance;
    }


    public void addBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }


    public Bitmap getBitmap(String key) {
        return mMemoryCache.get(key);
    }

}
