package com.yidian.serving.metrics.reporter.opentsdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Future;

/**
 * A client to send metrics to OpenTsdb using http interface.
 * @author jixu
 */
public class HttpOpenTsdbClient implements OpenTsdbClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(HttpOpenTsdbClient.class);

    private static final int OPENTSDB_SUCESS_CODE = 204;
    private static final int CHUNK_METRICS_NUM = 40;

    private final AsyncHttpClient asyncHttpClient;
    private final String url;

    public HttpOpenTsdbClient(AsyncHttpClient asyncHttpClient, String url) {
        this.asyncHttpClient = asyncHttpClient;
        this.url = url;
    }
    public boolean send(List<OpenTsdbMetric> metrics) {
        //@Override

        if (metrics.size() <= 0) {
            return true;
        }

        int batchNum = (metrics.size() - 1) / CHUNK_METRICS_NUM + 1;
        List<Future<Response>> responseFutures = Lists.newArrayListWithCapacity(batchNum);
        boolean allRight = true;
        for (int i = 0; i < batchNum; ++i) {
            int beg = CHUNK_METRICS_NUM * i;
            int end = (beg + CHUNK_METRICS_NUM) > metrics.size() ? metrics.size() : beg + CHUNK_METRICS_NUM;
            List<OpenTsdbMetric> subMetrics = metrics.subList(beg, end);
            try {
                String jsonStr = objectMapper.writeValueAsString(subMetrics);
                Future<Response> responseFuture = asyncHttpClient.preparePost(url).setBody(jsonStr).execute();
                responseFutures.add(responseFuture);
            } catch (JsonProcessingException e) {
                allRight = false;
                logger.warn("Cannot send metrics to OpenTsdb", e);
            }
        }

        for (Future<Response> responseFuture : responseFutures) {
            try {
                Response response = responseFuture.get();
                if (response.getStatusCode() != OPENTSDB_SUCESS_CODE) {
                    allRight = false;
                    logger.warn("OpenTsdb didn't response with success code: " + response.getStatusCode());
                }
            } catch (Exception e) {
                allRight = false;
                logger.warn("Cannot send metrics to OpenTsdb", e);
            }
        }

        return allRight;
    }
}
