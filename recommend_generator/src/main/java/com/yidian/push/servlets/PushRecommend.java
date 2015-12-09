package com.yidian.push.servlets;

import com.yidian.push.config.Config;
import com.yidian.push.config.RecommendGeneratorConfig;
import com.yidian.push.recommend_gen.Generator;
import com.yidian.push.recommend_gen.OnlineGenerator;
import com.yidian.push.recommend_gen.RunningInstance;
import com.yidian.push.response.Response;
import com.yidian.push.util.HttpHelper;
import com.yidian.serving.metrics.MetricsFactoryUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/11/14.
 */
@Log4j
public class PushRecommend extends HttpServlet {
    private static final String BATCH_RECOMMEND = "batch_recommend";
    private static final String ONLINE_RECOMMEND = "online_recommend";

    public String getInputFile(String base, int lookBackDay)
    {
        for (int i = 0; i < lookBackDay; i ++) {
            String day = DateTime.now().minusDays(i).toString("yyyy-MM-dd");
            String fileName = base + "/" + day;
            File file = new File(fileName);
            if (file.exists() && file.isFile()) {
                log.info("get input file :" + fileName);
                return fileName;
            }
            else {
                log.info("input file :" + fileName + " does not exist");
            }
        }
        return null;
    }

    public String getOutputPath(String base, int times) {
        String day = DateTime.now().toString("yyyy-MM-dd");
        for (int i = 0; i < times; i ++) {
            String dirName = base + "/" + day + "_" + i;
            File file = new File(dirName);
            if (!file.exists()) {
                log.info("get output dir:" + dirName);
                return dirName;
            }
            else {
                log.info("output dir " + dirName + " already exists");
            }
        }
        return null;
    }

    public void runBatchRecommend() {
        Generator generator = null;
        long start = System.currentTimeMillis();
        try {
            generator = new Generator();
            RecommendGeneratorConfig config = Config.getInstance().getRecommendGeneratorConfig();
            String inputFile = getInputFile(config.getInputDataPath(), config.getInputLookBackDays());
            String outputPath = getOutputPath(config.getOutputDataPath(), config.getOutputLookBackTimes());
            log.info("input file:[" + inputFile + "], output dir:[" + outputPath + "]");
            if (StringUtils.isEmpty(inputFile) || StringUtils.isEmpty(outputPath)) {
                log.info("invalid input file or output path");
            } else {

                generator.processFile(inputFile, outputPath);
                log.info("shut down the process...");

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != generator) {
                generator.clear();
            }
            long end = System.currentTimeMillis();
            log.info("ROUND_TIME: runBatchRecommend elapsed time：" + (end - start) / (1000.0 * 60) + " minute");
        }
    }

    public void runOnlineRecommend(String round) {
        OnlineGenerator generator = null;
        long start = System.currentTimeMillis();
        try {
            generator = new OnlineGenerator(round);
            RecommendGeneratorConfig config = Config.getInstance().getRecommendGeneratorConfig();
            String inputFile = getInputFile(config.getOnlineInputDataPath(), config.getInputLookBackDays());
            log.info("input file:[" + inputFile + "]");
            if (StringUtils.isEmpty(inputFile)) {
                log.info("invalid input file or output path");
                //throw new RuntimeException("invalid input file or output path");
            } else {
                generator.processFile(inputFile);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (null != generator) {
                try {
                    generator.clear();
                } catch (Exception e) {
                    // ignore
                }
            }
            long end = System.currentTimeMillis();
            log.info("ROUND_TIME: runOnlineRecommend elapsed time：" + (end - start) / (1000.0 * 60) + " minute");
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response recordResponse = new Response();
        String task = req.getParameter("task");
        String round = req.getParameter("round");
        log.info("RECOMMEND_TASK: " + task);
        int runningInstances = RunningInstance.getRunningNumber();
        if (runningInstances >= 2) {
            recordResponse.markFailure();
            recordResponse.setDescription("there already 2 running instances, just skip this request.");
            HttpHelper.setResponseParameters(resp, recordResponse);
            return;
        }

        if (BATCH_RECOMMEND.equals(task)) {
            recordResponse.markSuccess();
            recordResponse.setDescription("got the batch_recommend request");
            HttpHelper.setResponseParameters(resp, recordResponse);
            log.info("run batch recommend");
            MetricsFactoryUtil.getRegisteredFactory().getMeter("push_notification."+BATCH_RECOMMEND+".qps").mark();
            long start = System.currentTimeMillis();
            runBatchRecommend();
            long end = System.currentTimeMillis();
            MetricsFactoryUtil.getRegisteredFactory().getHistogram("push_notification."+BATCH_RECOMMEND+".latency").update(end - start);
        }
        else if (ONLINE_RECOMMEND.equals(task)) {
            recordResponse.markSuccess();
            recordResponse.setDescription("got the online_recommend request");
            HttpHelper.setResponseParameters(resp, recordResponse);
            log.info("run online recommend");
            long start = System.currentTimeMillis();
            MetricsFactoryUtil.getRegisteredFactory().getMeter("push_notification."+ONLINE_RECOMMEND+".qps").mark();
            runOnlineRecommend(round);
            long end = System.currentTimeMillis();
            MetricsFactoryUtil.getRegisteredFactory().getHistogram("push_notification."+ONLINE_RECOMMEND+".latency").update(end - start);
        }
        else {
            recordResponse.markFailure();
            recordResponse.setDescription("unknown task");
            HttpHelper.setResponseParameters(resp, recordResponse);
        }
    }
}
