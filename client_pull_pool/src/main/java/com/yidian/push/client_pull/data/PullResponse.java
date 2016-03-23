package com.yidian.push.client_pull.data;

import com.yidian.push.data.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by tianyuzhi on 16/3/22.
 */
@Setter
@Getter
public class PullResponse extends Response {
    private List<DocItem> result = null;
}
