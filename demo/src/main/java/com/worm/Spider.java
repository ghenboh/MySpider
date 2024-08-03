package com.worm;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.worm.Bean.Score;

public class Spider {
    WebClient webClient;
    ExecutorService executor;
    AtomicInteger codeNumber;
    long lastTime;
    long time = System.currentTimeMillis();
    public Spider() throws Exception{
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        executor = new ThreadPoolExecutor(6, 12, 1, TimeUnit.HOURS, new ArrayBlockingQueue<>(20));
        codeNumber = new AtomicInteger();
        lastTime = 0;
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true); 
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setRedirectEnabled(false);
        webClient.getCache().setMaxSize(100);
    }

    public void doSpiderLi() throws Exception {
        StringBuilder name = new StringBuilder("ret/");
        name.append(codeNumber.getAndIncrement());
        name.append(".txt");
        PrintWriter pw = new PrintWriter(name.toString());
        HtmlPage page = getPageFromLink("https://doosho.com/cn/162/3");
        while(page != null) {
            pw.println(page.getTitleText());
            List<Object> other = getPageFromXPath(page, "/html/body/div/div[3]/article/p");
            for(Object single: other) {
                String now = ((HtmlElement)single).asText();
                if(now.indexOf("林彪") != -1) {
                    pw.println(now);
                }
            }
            pw.close();
            name = new StringBuilder("ret/");
            name.append(codeNumber.getAndIncrement());
            name.append(".txt");
            pw = new PrintWriter(name.toString());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("/html/body/div/div[3]/article/p[");
            stringBuilder.append(other.size());
            stringBuilder.append("]/a[2]");
            page = testAndChangePage(page, stringBuilder.toString());
        }
        System.out.println(System.currentTimeMillis() - time);
        pw.close();
    }

    public synchronized void doNotice() {
        executor.shutdown();
    }

    public void addTask(String link) throws Exception, IOException{
        if(link.length() == 0) {
            doNotice();
            return;
        }
        executor.submit(() -> {
            try {
                doSpiderLiThread(link);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void doSpiderLiThread(String link) throws Exception {
        if(link.length() == 0) {
            doNotice();
            return;
        }
        HtmlPage page = getPageFromLink(link);
        List<Object> other = getPageFromXPath(page, "/html/body/div/div[3]/article/p");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/html/body/div/div[3]/article/p[");
        stringBuilder.append(other.size());
        stringBuilder.append("]/a[2]");
        addTask(getLinkFromXpath(page, stringBuilder.toString()));
        StringBuilder name = new StringBuilder("ret/");
        name.append(codeNumber.getAndIncrement());
        name.append(".txt");
        PrintWriter pw = new PrintWriter(name.toString());
        pw.println(page.getTitleText());
        for(Object single: other) {
            String now = ((HtmlElement)single).asText();
            if(now.indexOf("林彪") != -1) {
                pw.println(now);
            }
        }
        pw.close();
        lastTime = Math.max(lastTime, System.currentTimeMillis());
    }

    public SchoolScore doSpiderNwpu() throws Exception {
        SchoolScore ret = new SchoolScore("NWPU");
        HtmlPage page = getPageFromLink("https://zsb.nwpu.edu.cn/static/front/nwpu/basic/html_web/lnfs.html");
        List<Object> other = getPageFromXPath(page, "/html/body/div/div[2]/div[2]/div[2]/div/div[2]/div/div[1]/dl/dd/a");
        for(Object single: other) {
            HtmlPage another = getInLink(single);
            another = testAndChange(another, "//*[@id=\"filterPlace\"]/dl[2]/dd/a[3]", "选考物理") != null ? 
                testAndChange(another, "//*[@id=\"filterPlace\"]/dl[2]/dd/a[3]", "选考物理") : another;
            List<Object> element= getPageFromXPath(another, "//table[@id='sszygradeListPlace']/tbody/tr");
            for(Object test: element) {
                List<String> now = makeListByIterator(((HtmlElement)test).getChildElements().iterator());
                if(now.size() > 7) {
                    Score score = new Score(Integer.valueOf(now.get(0)), now.get(1), now.get(4), 
                    Integer.valueOf(now.get(5)), Integer.valueOf(now.get(8)), Double.valueOf(now.get(7)));
                    ret.addScore(score);
                }
            }
        }
        return ret;
    }

    public SchoolScore doSpiderSysu() throws Exception {
        SchoolScore ret = new SchoolScore("SYSU");
        HtmlPage page = getPageFromLink("https://zs.sysu.edu.cn/static/front/sysu/basic/html_web/lnfs.html");
        List<Object> other = getPageFromXPath(page, "/html/body/div/div[2]/div[2]/div[2]/div/div[2]/div/div[1]/dl/dd/a");
        for(Object single: other) {
            HtmlPage another = getInLink(single);
            List<Object> element= getPageFromXPath(another, "//*[@id=\"sszygradeListPlace\"]/tbody/tr");
            for(Object test: element) {
                List<String> now = makeListByIterator(((HtmlElement)test).getChildElements().iterator());
                if(now.size() > 7) {
                    Score score = new Score(Integer.valueOf(now.get(0)), now.get(1), now.get(4), 
                    Integer.valueOf(now.get(5)), Integer.valueOf(now.get(7)), Double.valueOf(now.get(6)));
                    ret.addScore(score);
                }
            }
        }
        return ret;
    }

    public SchoolScore doSpiderXmu() throws Exception {
        SchoolScore ret = new SchoolScore("SMU");
        HtmlPage page = getPageFromLink("https://zs.xmu.edu.cn/info/1045/26931.htm");
        List<Object> element= getPageFromXPath(page, "//*[@id=\"vsb_content\"]/div/table[2]/tbody/tr");
        for(Object test: element) {
            List<String> now = makeListByIterator(((HtmlElement)test).getChildElements().iterator());
            if(now.size() > 7 && now.get(1).equals("普通类") && 
                (now.get(2).indexOf("理") != -1 || now.get(2).equals("综合改革"))) {
                    Score score = new Score(2023, now.get(0), now.get(3),
                        Integer.valueOf(now.get(5)), Integer.valueOf(now.get(4)), Double.valueOf(now.get(6)));
                    ret.addScore(score);
            }
        }
        return ret;
    }

    public SchoolScore doSpiderBupt() throws Exception {
        SchoolScore ret = new SchoolScore("BUPT");
        HtmlPage page = getPageFromLink("https://zsb.bupt.edu.cn/lnfs/n2023.htm");
        List<HtmlPage> allPage = new ArrayList<>();
        allPage.add(page);
        while(page != null) {
            List<Object> element= getPageFromXPath(page, "/html/body/section/section[6]/div/div[2]/section[2]/div[2]/div/span[1]/span[6]/a");
            if(element.size() != 0) {
                page = ((HtmlElement)element.get(0)).click();
                allPage.add(((HtmlElement)element.get(0)).click());
            } else {
                break;
            }
        }
        for(HtmlPage single: allPage) {
            List<Object> element= getPageFromXPath(single, "/html/body/section/section[6]/div/div[2]/section[2]/ul/li/a");
            List<HtmlPage> provincePage = new ArrayList<>();
            for(Object other: element) {
                if(((HtmlElement)other).asText().indexOf("北京邮电大学2023年各专业录取分数") != -1) {
                    provincePage.add(getInLink(other));
                }
            }
            
            for(HtmlPage another: provincePage) {
                List<Object> transfer = getPageFromXPath(another, 
                    "/html/body/section/section[6]/div/div[2]/section[2]/form/div/div[1]/h3");
                if(transfer.size() == 0) {
                    continue;
                }
                String nowProvince = ((HtmlElement)transfer.get(0)).asText();
                int num = 0;
                for(int helper = nowProvince.length() - 1; helper >= 0 ; helper--) {
                    if(nowProvince.charAt(helper) == '—') {
                        num = helper + 1;
                        break;
                    }
                }
                nowProvince = nowProvince.substring(num, nowProvince.length() - 1);
                List<Object> with = getPageFromXPath(another, "//*[@id=\"vsb_content\"]/div/table/tbody/tr");
                for(Object test: with) {
                    List<String> now = makeListByIterator(((HtmlElement)test).getChildElements().iterator());
                    if(now.size() == 5 && (now.get(0).indexOf("普通理科") != -1 || now.get(0).indexOf("物理普通") != -1)) {
                        break;
                    } else if(now.size() == 5 && (now.get(0).indexOf("总计") != -1 || now.get(0).indexOf("名称") != -1)) {
                        continue;
                    } else if(now.size() == 0) {
                        continue;
                    }
                    int addNum = now.size() == 6 ? 1 : 0;
                    if(now.get(0 + addNum).indexOf("中外合作") != -1) {
                        continue;
                    }
                    Score score = new Score(2023, nowProvince, now.get(0 + addNum),
                        Integer.valueOf(now.get(3 + addNum)), Integer.valueOf(now.get(2 + addNum)), Double.valueOf(now.get(4 + addNum)));
                    ret.addScore(score);
                }
            }
        }
        return ret;
    }

    public HtmlPage testAndChange(HtmlPage page, String XPath, String testString) throws IOException{
        HtmlPage ret = null;
        List<Object> with = getPageFromXPath(page, XPath);
        if(with.size() > 0 && ((HtmlElement)with.get(0)).asText().equals(testString)) {
            ret = getInLink(with.get(0));
        }
        return ret;
    }

    public HtmlPage testAndChangePage(HtmlPage page, String XPath) throws IOException{
        HtmlPage ret = null;
        List<Object> with = getPageFromXPath(page, XPath);
        if(with.size() > 0) {
            String transfer = "https://doosho.com";
            transfer = transfer.concat(((DomElement)with.get(0)).getAttribute("href"));
            ret = getPageFromLink(transfer);
        }
        return ret;
    }

    public String getLinkFromXpath(HtmlPage page, String XPath) throws IOException{
        List<Object> with = getPageFromXPath(page, XPath);
        String transfer = "https://doosho.com";
        if(with.size() > 0) {
            transfer = transfer.concat(((DomElement)with.get(0)).getAttribute("href"));
        }
        return with.size() > 0 ? transfer : "";
    }

    public List<String> makeListByIterator(Iterator<DomElement> iterator) {
        List<String> ret = new ArrayList<>();
        while(iterator.hasNext()) {
            ret.add(((HtmlElement)iterator.next()).asText());
        }
        return ret;
    }

    public HtmlPage getPageFromLink(String link) throws IOException{
        HtmlPage ret = webClient.getPage(link);
        for(int helper = 0; helper < 10 && ret.getWebResponse().getStatusCode() == 502; helper++) {
            ret = webClient.getPage(link);
        }
        return ret;
    }

    public List<Object> getPageFromXPath(HtmlPage page, String XPath) {
        return page.getByXPath(XPath);
    }

    public HtmlPage getInLink(Object link) throws IOException{
        return ((DomElement)link).click();
    }


}