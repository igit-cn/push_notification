package com.yidian.push.client_pull.servlets;

import com.yidian.push.client_pull.data.DocItem;
import com.yidian.push.client_pull.data.PullResponse;
import com.yidian.push.client_pull.utils.OuterServiceUtil;
import com.yidian.push.config.ClientPullPoolConfig;
import com.yidian.push.utils.HttpHelper;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by tianyuzhi on 16/3/22.
 */
@Log4j
public class GetPushPoolServlet extends HttpServlet {
    private Timer getNewsTimer = new Timer("getPushPoolTimer");
    private volatile List<DocItem> docItemPool = new ArrayList<>();
    private ClientPullPoolConfig config = null;

    public GetPushPoolServlet(ClientPullPoolConfig config) {
        this.config = config;
        getNewsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<DocItem> list = getNewPool();
                    if (list.size() > 0) {
                        log.info("get new pool with size:" + list.size() + ", old pool size:" + docItemPool.size());
                        docItemPool = list;
                    }
                } catch (Exception e) {
                    log.error("refresh pool failed.");
                }
            }
        }, 0, config.getRefreshPeriodInSeconds() * 1000);

    }

    public List<DocItem> getNewPool() {
        int batch = config.getNewsPoolBatch();
        int initSize = config.getNewsPoolInitSize();
        int maxSize = config.getNewsMaxPoolSize();
        String url = config.getNewsPoolURL();
        RequestConfig requestConfig = config.getRequestConfig();
        List<DocItem> list = OuterServiceUtil.getDocItems(url, initSize, maxSize, batch, requestConfig);
        List<String> docIdList = new ArrayList<>(list.size());
        for (DocItem docItem : list) {
            docIdList.add(docItem.getDocId());
        }
        Map<String, String> docTitles = OuterServiceUtil.getTitles(config.getDocInfoURL(), docIdList);
        List<DocItem> res = new ArrayList<>(list.size());
        for (DocItem item : list) {
            String title = docTitles.getOrDefault(item.getDocId(), "");
            if (StringUtils.isNotEmpty(title)) {
                item.setTitle(title);
                res.add(item);
            }
        }
        return res;
    }

    public List<DocItem> getDocListForUser() {
        int size = config.getUserNewsSize();
        List<DocItem> curPool = docItemPool;
        List<DocItem> res = new ArrayList<>(size);
        Set<Integer> fetchedIndexes = new HashSet<>();
        int index = 0;
        while (index < size && curPool.size() - index >= 0) {
            int i = (int)(Math.random() * curPool.size());
            if (!fetchedIndexes.contains(i)) {
                fetchedIndexes.add(i);
                res.add(curPool.get(i));
            }
            index ++;
        }
        return res;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = req.getParameter("userid");
        String platform = req.getParameter("platform");
        List<DocItem> res = getDocListForUser();
        PullResponse pullResponse = new PullResponse();
        pullResponse.setResult(res);
        pullResponse.markSuccess();
        HttpHelper.setResponseParameters(resp, pullResponse);

    }
}
