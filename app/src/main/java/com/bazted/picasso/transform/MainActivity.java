package com.bazted.picasso.transform;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;


public class MainActivity extends Activity {


    public static final String TRANSFORM = "transform";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageView image = (ImageView) findViewById(R.id.image_view);
        final int maxTexture = Device.maxTextureSize(this);
        Log.e(TRANSFORM, "maxTexture=" + maxTexture);

        Transformation transformation = new Transformation() {

            @Override
            public Bitmap transform(Bitmap source) {
                int targetWidth = image.getWidth();
                Log.e(TRANSFORM, "view w=" + image.getWidth() + "|h=" + image.getHeight());
                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                Log.e(TRANSFORM, "bitmap w=" + source.getWidth() + "|h=" + source.getHeight());
                int targetHeight = (int) (targetWidth * aspectRatio);

                Log.e(TRANSFORM, "target w=" + targetWidth + "|aspect=" + aspectRatio + "|H=" + targetHeight);
                Bitmap result;
                if (targetHeight > maxTexture && targetWidth > maxTexture) {
                    result = Bitmap.createBitmap(source, 0, 0, maxTexture, maxTexture);
                } else if (targetHeight > maxTexture && !(targetWidth > maxTexture)) {
                    result = Bitmap.createBitmap(source, 0, 0, targetWidth, maxTexture);
                } else if (!(targetHeight > maxTexture) && targetWidth > maxTexture) {
                    result = Bitmap.createBitmap(source, 0, 0, maxTexture, targetHeight);
                } else {
                    result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                }
                if (result != source) {
                    // Same bitmap is returned if sizes are the same
                    source.recycle();
                }
                return result;
            }

            @Override
            public String key() {
                return "transformation" + " desiredWidth";
            }
        };

        String mMessage_pic_url = "http://img.zszywka.pl/1/0277/3781.jpg";
//        String mMessage_pic_url = "http://img.zszywka.pl/0/0274/7583/dzialam-dzialam-.jpg";
        final Picasso with = Picasso.with(this);
        with.setDebugging(true);
        with.load(mMessage_pic_url)
                .error(android.R.drawable.stat_notify_error)
                .transform(transformation)
                .into(image);
    }
}
