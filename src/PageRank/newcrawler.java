package PageRank;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class newcrawler {

    static int MaxUrlnum = 139;
    static int timeout = 1000;

    static void clear(int num, int to) {
        MaxUrlnum = num;
        timeout = to;
        LinkQueue.visitedUrl = new HashSet<String>();
        LinkQueue.unVisitedUrl = new Queue();
    }

    /**
     * 队列，保存将要访问的URL
     */
    public static class Queue {
        //使用链表实现队列

        private LinkedList<Object> queue = new LinkedList<Object>();
        //入队列

        public void enQueue(Object t) {
            queue.addLast(t);
        }
        //出队列

        public Object deQueue() {
            return queue.removeFirst();
        }
        //判断队列是否为空

        public boolean isQueueEmpty() {
            return queue.isEmpty();
        }
        //判断队列是否包含t

        public boolean contains(Object t) {
            return queue.contains(t);
        }

        public boolean empty() {
            return queue.isEmpty();
        }

        public int size() {
            return queue.size();
        }
    }

    public static class LinkQueue {
        //已访问的url 集合

        private static Set<String> visitedUrl = new HashSet<String>();
        //待访问的url 集合
        private static Queue unVisitedUrl = new Queue();
        //获得URL 队列

        public static Queue getUnVisitedUrl() {
            return unVisitedUrl;
        }
        //获得已访问URL集合

        public static Set<String> getVisitedUrl() {
            return visitedUrl;
        }
        //添加到访问过的URL 队列中

        public static void addVisitedUrl(String url) {
            visitedUrl.add(url);
        }
        //移除访问过的URL

        public static void removeVisitedUrl(String url) {
            visitedUrl.remove(url);
        }
        //未访问的URL 出队列

        public static Object unVisitedUrlDeQueue() {
            return unVisitedUrl.deQueue();
        }
        // 保证每个URL 只被访问一次

        public static void addUnvisitedUrl(String url) {
            if (url != null && !url.trim().equals("")
                    && !visitedUrl.contains(url)
                    && !unVisitedUrl.contains(url)) {
                unVisitedUrl.enQueue(url);
            }
        }
        //获得已经访问的URL 数目

        public static int getVisitedUrlNum() {
            return visitedUrl.size();
        }
        //获得已经访问的URL 数目

        public static int getunVisitedUrlNum() {
            return unVisitedUrl.size();
        }
        //判断未访问的URL 队列中是否为空

        public static boolean unVisitedUrlsEmpty() {
            return unVisitedUrl.empty();
        }
    }

    public static class HtmlParserTool {// 获取一个网页上所有的链接和图片链接

        @SuppressWarnings("serial")
        public static Set<String> extracLinks(String url, LinkFilter filter) throws MalformedURLException, IOException {
            Set<String> links = new HashSet<String>();
            try {
                URL urlPage = new URL(url); //新建连接，设置延时
                HttpURLConnection conn = (HttpURLConnection) urlPage.openConnection();
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                Parser parser = new Parser(conn);//使用连接
                parser.setEncoding("utf-8");
                // 过滤 <frame> 标签的 filter，用来提取 frame 标签里的 src 属性所、表示的链接 
                NodeFilter frameFilter = new NodeFilter() {
                    @Override
                    public boolean accept(Node node) {
                        if (node.getText().startsWith("frame src=")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
                // OrFilter 来设置过滤 <a> 标签和 <frame> 标签，两个标签是 or 的关系 
                OrFilter linkFilter = new OrFilter(new NodeClassFilter(
                        LinkTag.class), frameFilter);
                // 得到所有经过过滤的标签 
                NodeList list = parser.extractAllNodesThatMatch(linkFilter);
                for (int i = 0; i < list.size(); i++) {
                    Node tag = list.elementAt(i);
                    if (tag instanceof LinkTag)// <a> 标签 
                    {
                        LinkTag link = (LinkTag) tag;
                        String linkUrl = link.getLink();// url 
                        if (filter.accept(linkUrl)) {
                            //String text = link.getLinkText();// 链接文字 
                            //System.out.println(url + "\t--->\t" + linkUrl + "         " + text);
                            if (!linkUrl.startsWith("http")) {
                                break;
                            }
                            int reposition = linkUrl.indexOf("#");
                            if (reposition != -1) {
                                linkUrl = linkUrl.substring(0, (reposition));//带#号的之后可以忽略
                            }
                            if (linkUrl.contains(":80/")) {
                                linkUrl = linkUrl.replace(":80", "");//带80端口的和不带80端口的是一样的URL
                            }
                            if (linkUrl.endsWith(":80")) {
                                linkUrl = linkUrl.substring(0, (linkUrl.length() - 3));//以斜杠结束的和没有斜杠的是一样的URL
                            }
                            if (linkUrl.endsWith("/")) {
                                linkUrl = linkUrl.substring(0, (linkUrl.length() - 1));
                            }
                            if (linkUrl.contains("@")||linkUrl.endsWith(".zip")||linkUrl.endsWith(".rar")||linkUrl.endsWith(".docx")||linkUrl.endsWith(".xls")||linkUrl.endsWith(".doc")||linkUrl.endsWith(".pdf")||linkUrl.endsWith(".jpg")||linkUrl.endsWith(".gif")||linkUrl.endsWith(".ppt")) {
                                break;//去除邮件、ftp、文件地址，虽然可能去掉了一些奇特的URL
                            }
                            links.add(linkUrl);
                        }
                    } else// <frame> 标签 
                    {
                        // 提取 frame 里 src 属性的链接如 <frame src="test.html"/> 
                        String frame = tag.getText();
                        int start = frame.indexOf("src=");
                        frame = frame.substring(start);
                        int end = frame.indexOf(" ");
                        if (end == -1) {
                            end = frame.indexOf(">");
                        }
                        frame = frame.substring(frame.length(), end - 1); //?

                        if (filter.accept(frame)) {
                            if (!frame.startsWith("http")) {
                                break;
                            }
                            int reposition = frame.indexOf("#");
                            if (reposition != -1) {
                                frame = frame.substring(0, (reposition));//带#号的之后可以忽略
                            }
                            if (frame.contains(":80/")) {
                                frame = frame.replace(":80", "");
                            }
                            if (frame.endsWith(":80")) {
                                frame = frame.substring(0, (frame.length() - 3));//带80端口的和不带80端口的是一样的URL
                            }
                            if (frame.endsWith("/")) {
                                frame = frame.substring(0, (frame.length() - 1));//以斜杠结束的和没有斜杠的是一样的URL
                            }
                            if (frame.contains("@")||frame.endsWith(".zip")||frame.endsWith(".rar")||frame.endsWith(".docx")||frame.endsWith(".xls")||frame.endsWith(".doc")||frame.endsWith(".pdf")||frame.endsWith(".jpg")||frame.endsWith(".gif")||frame.endsWith(".ppt")) {
                                break;//去除邮件、ftp、文件地址，虽然可能去掉了一些奇特的URL
                            }							
                            //System.out.println(url + "\t--->\t" + frame);
                            links.add(frame);
                        }
                    }
                }
            } catch (ParserException e) {
            }
            return links;
        }
    };

    public interface LinkFilter {

        public boolean accept(String url);
    }
    //定义过滤器，提取包含nju.edu.cn 开头的链接
    static LinkFilter filter = new LinkFilter() {
        @Override
        public boolean accept(String url) {
            if (url.contains("nju.edu.cn")) {
                return true;
            } else {
                return false;
            }
        }
    };

    public static class MyCrawler {

        /**
         * 使用种子初始化URL 队列
         *
         * @return
         * @param seeds 种子URL
         */
        private static void initCrawlerWithSeeds(String[] seeds) {
            for (int i = 0; i < seeds.length; i++) {
                LinkQueue.addUnvisitedUrl(seeds[i]);
            }
        }

        /**
         * 抓取过程
         *
         * @return
         * @param seeds
         * @throws IOException
         */
        public static void crawling(String[] seeds) throws IOException {
            FileWriter fw = new FileWriter("院系链接关系.txt", false);
            //初始化URL 队列
            initCrawlerWithSeeds(seeds);
            //循环条件：待抓取的链接不空且抓取的网页不多于MaxUrlnum
            while (!LinkQueue.unVisitedUrlsEmpty()
                    && LinkQueue.getVisitedUrlNum() <= MaxUrlnum) {

                //队头URL 出队列
                String visitUrl = (String) LinkQueue.unVisitedUrlDeQueue();
                if (visitUrl == null) {
                    continue;
                }
                int now = LinkQueue.getVisitedUrlNum();
                MainFrame.setprogress(now, visitUrl);//输出当前进行到多少个链接到GUI界面
                //System.out.println("链接" + LinkQueue.getVisitedUrlNum() + ":\t" + visitUrl);
                //该URL 放入已访问的URL 中
                LinkQueue.addVisitedUrl(visitUrl);
                //提取出下载网页中的URL//并对相应的URL入队
                Set<String> links = HtmlParserTool.extracLinks(visitUrl, filter);
                //新的未访问的URL 入队
                for (String link : links) {
                    //如果将访问和已访问的URL总量达到MaxUrlnum时，不再添加新的URL
                    //且对剩下的为访问的URL操作时，只对其与已有的URL的关系做处理
                    if ((LinkQueue.getVisitedUrlNum() + LinkQueue.getunVisitedUrlNum() + 1) <= MaxUrlnum) {//若未满
                        //System.out.println(visitUrl + "\t--->\t" + link);
                        fw.write(visitUrl + "\n");
                        fw.write(link + "\n");
                        if ((!(LinkQueue.unVisitedUrl.contains(link))) && (!(LinkQueue.visitedUrl.contains(link)))) {
                            LinkQueue.addUnvisitedUrl(link);
                        }
                        /*else
                         {
                         System.out.println("已存在");
                         }*/
                    } else {
                        if ((LinkQueue.unVisitedUrl.contains(link)) || (LinkQueue.visitedUrl.contains(link))) {
                            //System.out.println(visitUrl + "\t--->\t" + link);
                            fw.write(visitUrl + "\n");
                            fw.write(link + "\n");
                        }
                    }
                }
            }
            fw.close();
        }
    }

    //测试的 main 方法  
    public static void main(String[] args) throws IOException {
        //OutputStream output = new FileOutputStream("院系链接关系.txt");
        //OutputStream allurloutput = new FileOutputStream("院系所有链接.txt");
        //PrintStream   old   =   System.out;
        //PrintStream myOut = new PrintStream(output);
        //System.setOut(myOut);


        //newcrawler craw = new newcrawler();
        //Crawler crawler = new Crawler();
        newcrawler.MyCrawler.crawling(new String[]{"http://www.nju.edu.cn"});//设置种子，运行 爬虫

        //PrintStream allurlOut = new PrintStream(allurloutput);
        //System.setOut(allurlOut);
        //System.out.println("所有链接:");
        //int i = 0;
        FileWriter fwall = new FileWriter("院系所有链接.txt", false);
        for (String Visitedlink : LinkQueue.getVisitedUrl())//另外一个文件存储所有爬到的URL
        {
            fwall.write(Visitedlink + "\n");
            //System.out.println(Visitedlink);
            //System.out.println("链接"+ i + ": " + Visitedlink);
            //++i;
        }
        //System.setOut(old);
        fwall.close();
        System.out.println("The End.");
    }
}
