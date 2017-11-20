package com.maihehd.sdk.vast;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

/**
 * Created by roger on 7/6/15.
 */
public class Assets {

    //data:image/png;base64,
    //public static final String CLOSE_PNG = "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAALEgAACxIB0t1+/AAAAB50RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNS4xqx9I6wAAABR0RVh0Q3JlYXRpb24gVGltZQA3LzgvMTVnnnzKAAABI0lEQVQ4ja3UwWqDQBDG8WlDX2cv8SKkb5AH8wVzCkWkOSkUIqEQiP5z6Ldls7obpV3w4O7Mz5lRfAHsXxdgwAX4BtwfDPstTtgIfAC7FZBT7iUGnbABaIHtAmynnFGoPYxPAS0/q81VCmwVMwh1EzAK9O1PZjr34MkME60AXIEimplvs1UB05cygzphI3AESqDQHuHLWwQqsBA2AF/Apx5wDUexGFRwKcyvUziCOfA1T9rGzG5hvpm9PcnJVudbbtUywBkoUxXmsLOAI/Ae7I3AwaNPQSUelBhXE571uk+DCujjKhIxAHXyw9ZBrcB+DovQWvNtQjTEGgXUOSwqoFEBnUf9QaeDhnW/L597AyoPVtro1mARWgF7D+61sRoT+HDdAXmUxkVsCIhPAAAAAElFTkSuQmCC";
    public static final String CLOSE_PNG = "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAuIwAALiMBeKU/dgAAABx0RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNui8sowAAAAYdEVYdENyZWF0aW9uIFRpbWUAMjAxNS4xMi4xNJjiMyMAAAEKSURBVDiNzZQxagMxEEW/coAcIgdQ40pgnyQH9glSmbhytUXA2NgQ2H0p8mVkebUbExMimGJnvt58jcQGQI9cTw+l/QXwKOkkKYYQNBWSorXHKwJQxgkYgHdg2XIBLK0ZvOfCqIHRwh7ogMUIbOFab21sAovuHd+rK52O1SpDt8DKRT5+LNwP2X0NawKrOQF8OijnexfQG2IByuDYuFABs+/wWdKh+D44114zR97Z2YcD51Z3HRlIwMZPY2d4btC7ln4EtHBvN5vsxrWVc1hzA607JODNT2MPpJER5YaDtWkK+Aqcs3Bi7rnx2XsujFBdxoukJGkbQljPXGayfi1pm/M18Nfr//9gvwA6tWZCDJ/2igAAAABJRU5ErkJggg==";
    public static final String MUTE_PNG = "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAUxJREFUOBHtkr1KA0EUhTOJCiGxUhCFoGAaiyC+gEQJprFKERK18w0s7NQXUBQs9AmsrfQxtLEy4hYpRRBBEqOu3wmb4QprEIJdLnycmXvvnPnZTSSG8e8vEIZhARb/ulGyXyNGReoXUOjXZ2u/GmK2ReMZTMC7FpH70c98RHkbPkFxnkIOHKzAJnzBh5R6Fj1Eb51z52iZ+Ta6yzxg3A1vyOwAVkGn0UmkISg+YQaKUMJkDl2DKViCALphryDzTkQb7ZmpcRTuYQ/eYAPG4QiuwYc1tAa+oTfgWqo/Qwr0FFr7Sr6F+rCGahyLsE+h5g7XzKOnoNNewQvsk19HfdiFl2QfQSfRf7cA+iAKbfwEN3AHx1CBGgQQH+zmIAmzcAINaEJVK9BJSEfjFOPpeKeYLM0Z2IEH0EcYPDDSaeuwPLibccBQH2wY8S/wDamSlMwUEm5NAAAAAElFTkSuQmCC";
    public static final String SPEAKER_PNG = "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAYRJREFUOBHt0ztLQzEYxvGe1ioIClp0ExUFURARcfU2CQ66OIg6K+g3cPALOIiD7SCOgot0EURw6CS4ODiIFxBLEScXJy80/p96EmIt9EAHF1/4kTcnyXuS0zQW+49qX8AYE8cMeqvNrTjOwkEM2UHyBuSQRaN9HqllwQSusIQAcxiAdpjHSqRCmsRkFbnGE+aho57gGEmkcY5mv2id7TDQQ96BAJNYRBGfCIgic3bIM5jGHo4wijOUwhWkt4kpvCMetobWj1M6l5jFGp4xDFdQC22o+Efojba8mLap3V5Ap1Geh07lwi/4q4Cb9TN5pZsMH+nF9f6wXzARDmqC/yn8+cq1uxfo+6bCnOY7/IVZHj1AO9W964eO5YIfpY3OOA7Rii4coHKwQHdN16MT27hHAfbabJHfohvL0HifX80/sj660fXAI5M2sAt9J3uSFvI0dOR15HCHaMHbtdsFjGkFbTuakMENRqJVKpvFQv1gpSDXf3kfq/ZZzS3FUipcc6E/KfAF7NXzSCMvJRAAAAAASUVORK5CYII=";

    public static Drawable getDrawableFromBase64(Resources resources, String encodedString) {
        byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return new BitmapDrawable(resources, bitmap);
    }
}
