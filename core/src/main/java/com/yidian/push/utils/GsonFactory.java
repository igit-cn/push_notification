package com.yidian.push.utils;

/**
 * Created by yidianadmin on 14-8-4.
 */


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * <p>
 * Factory class for All Gson Objects
 * </p>
 *
 */
public class GsonFactory {

    private static final GsonBuilder gsonBuilder = new GsonBuilder()
            .disableHtmlEscaping();

    private static final Gson gsonNonPretty;
    private static final Gson gsonPretty;
    private static final Gson gson;

    static {
        gsonNonPretty = gsonBuilder.create();
        gsonPretty = gsonBuilder.setPrettyPrinting().create();
        gson = new Gson();
    }

    public static Gson getPrettyGson() { return gsonPretty; }
    public static Gson getNonPrettyGson() { return gsonNonPretty; }
    public static Gson getDefaultGson() { return gson; }

    /**
     * Disable Cloning this object
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new RuntimeException("Clone for class " + this.getClass() + " Disabled");
    }
}

