package com.togocourier.vollyemultipart

import android.content.Context
import android.graphics.Bitmap
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

/**
 * Created by chiranjib on 5/12/17.
 */
class VolleySingleton
/**
 * Private constructor, only initialization from getInstance.
 *
 * @param context parent context
 */
private constructor(context: Context) {
    private var mRequestQueue: RequestQueue? = null
    /**
     * Get image loader.
     *
     * @return ImageLoader
     */
    val imageLoader: ImageLoader

    /**
     * Get current request queue.
     *
     * @return RequestQueue
     */
    // getApplicationContext() is key, it keeps you from leaking the
    // Activity or BroadcastReceiver if someone passes one in.
    val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(mCtx.applicationContext)
            }
            return mRequestQueue
        }

    init {
        mCtx = context
        mRequestQueue = requestQueue

        imageLoader = ImageLoader(mRequestQueue,
                object : ImageLoader.ImageCache {
                    private val cache = LruBitmapCache(mCtx)

                    override fun getBitmap(url: String): Bitmap? {
                        return cache.get(url)
                    }

                    override fun putBitmap(url: String, bitmap: Bitmap) {
                        cache.put(url, bitmap)
                    }
                })
    }

    /**
     * Add new request depend on type like string, json object, json array request.
     *
     * @param req new request
     * @param <T> request type
    </T> */
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue?.add(req)
    }

    companion object {

        private var mInstance: VolleySingleton? = null
        private lateinit var mCtx: Context

        /**
         * Singleton construct design pattern.
         *
         * @param context parent context
         * @return single instance of VolleySingleton
         */
        @Synchronized
        fun getInstance(context: Context): VolleySingleton {
            if (mInstance == null) {
                mInstance = VolleySingleton(context)
            }
            return mInstance as VolleySingleton
        }
    }

}
