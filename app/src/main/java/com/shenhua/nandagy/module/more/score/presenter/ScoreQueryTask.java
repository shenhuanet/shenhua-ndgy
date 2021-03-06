package com.shenhua.nandagy.module.more.score.presenter;

import android.content.Context;
import android.os.AsyncTask;

import com.shenhua.commonlibs.mvp.HttpManager;
import com.shenhua.commonlibs.utils.CheckUtils;
import com.shenhua.nandagy.bean.scorebean.ScoreCETBean;
import com.shenhua.nandagy.bean.scorebean.ScoreCETParams;
import com.shenhua.nandagy.bean.scorebean.ScoreMandarinParams;
import com.shenhua.nandagy.bean.scorebean.ScoreQueryResult;
import com.shenhua.nandagy.callback.OnScoreQueryListener;
import com.shenhua.nandagy.service.HttpService;
import com.shenhua.nandagy.widget.LoadingAlertDialog;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Map;

/**
 * 成绩查询的异步线程
 * 第一个参数：传入doInBackground()方法的参数类型
 * 第二个参数：传入onProgressUpdate()方法的参数类型
 * 第三个参数：传入onPostExecute()方法的参数类型，也是doInBackground()方法返回的类型
 * Created by Shenhua on 9/23/2016.
 */
public class ScoreQueryTask<T> extends AsyncTask<Integer, Integer, ScoreQueryResult> {

    private static final String TAG = "ScoreQueryTask";
    private T data;
    private Context mContext;
    private OnScoreQueryListener onScoreQueryListener;

    public ScoreQueryTask(Context context, T data, OnScoreQueryListener onScoreQueryListener) {
        this.mContext = context;
        this.data = data;
        this.onScoreQueryListener = onScoreQueryListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        LoadingAlertDialog.getInstance(mContext).showLoadDialog("查询中，请稍后...", true);
    }

    @Override
    protected ScoreQueryResult doInBackground(Integer... params) {
        if (params[0] == 1) {
            return doQueryCet4();
        }
        if (params[0] == 2) {
            return doQueryMandarin();
        }


        // other type of params...
        return null;
    }

    @Override
    protected void onPostExecute(ScoreQueryResult result) {
        super.onPostExecute(result);
        LoadingAlertDialog.getInstance(mContext).dissmissLoadDialog();
        if (result != null) {
            if (result.getCode() > 0)
                onScoreQueryListener.onQuerySuccess(result.getData());
            else
                onScoreQueryListener.onQueryFailed(result.getCode(), result.getErrInfo());
        } else {
            onScoreQueryListener.onQueryFailed(-100, "null");
        }
    }

    private ScoreQueryResult doQueryMandarin() {
        ScoreQueryResult<String> resultBean = new ScoreQueryResult<>();
        Document doc;
        Map<String, String> cookies;
        Connection.Response res;
        try {
            res = Jsoup.connect(HttpService.SCORE_QUERY_URL_MANDARIN).timeout(5000).execute();
            cookies = res.cookies();
            String __VIEWSTATE = Jsoup.parse(res.body()).getElementById("__VIEWSTATE").attr("value");
            System.out.println("shenhua sout:" + __VIEWSTATE);
            doc = Jsoup.connect(HttpService.SCORE_QUERY_URL_MANDARIN)
                    .data("__VIEWSTATE", __VIEWSTATE)
                    .data("txtStuID", "3242342443134232525")
                    .data("txtName", CheckUtils.URLEncode(((ScoreMandarinParams) data).getName()))
                    .data("txtIDCard", "423423525252525252")
                    .data("btnLogin", CheckUtils.URLEncode("查  询"))
                    .data("txtCertificateNO", "")
                    .data("txtCardNO", "")
                    .timeout(5000)
                    .cookies(cookies)
                    .post();
            String __VIEWSTATE2 = doc.getElementById("__VIEWSTATE").attr("value");
            System.out.println("shenhua sout:" + __VIEWSTATE2);
            String scoreList = doc.getElementById("LooUpSocreList_Div").text();
            if (scoreList.equals("")) {
                System.out.println("shenhua sout:" + "查询失败，请确认所填写信息是否正确！");
                resultBean.setCode(0);
                resultBean.setErrInfo("查询失败，请确认所填写信息是否正确！");
                return resultBean;
            }
            resultBean.setCode(1);
            // TODO: 9/28/2016 解析数据
            resultBean.setData(null);
            return resultBean;
        } catch (IOException e) {
            e.printStackTrace();
            resultBean.setCode(0);
            resultBean.setErrInfo("连接到服务器异常");
            return resultBean;
        }
    }

    private ScoreQueryResult doQueryCet4() {
        ScoreQueryResult resultBean = new ScoreQueryResult();
        Document doc;
        try {
            doc = Jsoup.connect(HttpService.SCORE_QUERY_URL_CET)
                    .data("zkzh", ((ScoreCETParams) data).getZkzh())
                    .data("xm", ((ScoreCETParams) data).getName())
                    .cookie("auth", "token")
                    .userAgent(HttpManager.USER_AGENT)
                    .timeout(5000)
                    .header("Referer", "http://www.chsi.com.cn/cet/")
                    .post();
        } catch (Exception e) {
            resultBean.setCode(0);
            resultBean.setErrInfo("连接到服务器异常");
            return resultBean;
        }
        Element divElement;
        try {
            divElement = doc.getElementsByClass("cetTable").get(0);
        } catch (Exception e) {
            resultBean.setCode(0);
            resultBean.setErrInfo("准考证号或姓名输入有误");
            return resultBean;
        }
        ScoreCETBean cetBean = new ScoreCETBean();
        try {
            cetBean.setName(divElement.getElementsByAttributeValue("colspan", "2").get(0).text());// name
            cetBean.setSchool(divElement.getElementsByAttributeValue("colspan", "2").get(1).text());// school
            cetBean.setExamType(divElement.getElementsByAttributeValue("colspan", "2").get(2).text());// type
            cetBean.setExamNum(divElement.getElementsByAttributeValue("colspan", "2").get(3).text());// num
            cetBean.setSum(divElement.getElementsByAttributeValue("colspan", "2").get(4).text());// sum

            cetBean.setExamTime(divElement.getElementsByAttributeValue("colspan", "2").get(5).text());// time
            cetBean.setListen(divElement.getElementsByTag("td").get(6).text());
            cetBean.setReading(divElement.getElementsByTag("td").get(8).text());
            cetBean.setCompos(divElement.getElementsByTag("td").get(10).text());

            resultBean.setCode(1);
            resultBean.setData(cetBean);
            return resultBean;
        } catch (Exception e) {
            resultBean.setCode(-1);
            resultBean.setErrInfo("数据解析失败");
            return resultBean;
        }
    }

}