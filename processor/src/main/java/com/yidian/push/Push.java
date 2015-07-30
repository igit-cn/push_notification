package com.yidian.push;

import com.yidian.push.push_request.PushRequest;

import java.io.IOException;

/**
 * Created by yidianadmin on 15-2-7.
 */
public interface Push {
    public void pushFile(PushRequest request) throws IOException;
}
