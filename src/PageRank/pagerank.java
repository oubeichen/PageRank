package PageRank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;


public class pagerank 
{
	static int TOTAL = 139;
	static HashMap<String,Integer> hash = new HashMap<String,Integer>();//url到Integer的hash表
	static String [] urlset = new String[TOTAL];
	static final float ALPHA = 0.85f;	//阻尼系数
	static int[][] link = new int[TOTAL][TOTAL];	//邻接表表示链接情况
	static double[] pr = new double [TOTAL];	//储存pagerank值的数组
	static double[] prtmp = new double[TOTAL];	//储存链入页面提供的pagerank值
	static int[] outlink = new int[TOTAL];	//储存链出页面数目的数组
        public static void clear(int num)//重新开始计算之前的初始化
	{
                TOTAL = num;
		urlset = new String[TOTAL];
		link = new int[TOTAL][TOTAL];
                pr = new double [TOTAL];
                prtmp = new double[TOTAL];
                outlink = new int[TOTAL];
	}
	public static void setHashMap() throws IOException	//建立hash表
	{
		FileReader fr = new FileReader(new File("院系所有链接.txt"));
		LineNumberReader lr = new LineNumberReader(fr);
		Integer i=0;
		String url=lr.readLine();
		while(url != null)
		{
			hash.put(url,i);
			urlset[i]=url;
			i++;
			url=lr.readLine();
		}
	}
	
	public static void setLinks() throws IOException	//建立邻接表
	{
		FileReader fr = new FileReader(new File("院系链接关系.txt"));
		LineNumberReader lr = new LineNumberReader(fr);
		String url1=lr.readLine(),url2;
		int no1,no2;
		while(url1 != null)
		{
			no1 = hash.get(url1);
			url2 = lr.readLine();
			no2 = hash.get(url2);
			link[no1][outlink[no1]]=no2;
			outlink[no1]++;
			url1 = lr.readLine();			
		}
	}
	
	public static void doPageRank() 
	{
		for (int i=0; i<TOTAL; i++)
		{
			pr[i]=1.0f;
			prtmp[i]=0.15f;
		}
		for (int p=0; p<TOTAL; p++)	//总的迭代循环
		{
			for (int i=0; i<TOTAL; i++)	
			{
				for (int j=0; j<outlink[i]; j++)
					prtmp[j]+=(pr[i]*ALPHA)/outlink[i];
			}
			for (int i=0; i<TOTAL; i++)
			{
				pr[i]=prtmp[i];
				prtmp[i]=0.15f;
			}
		}
	}
	
	public static void outputResult() throws IOException
	{
		double max=0;
		int maxadd=0;
		OutputStream out = new FileOutputStream("pagerank结果.txt");
		PrintStream  printout = new PrintStream(out);
		System.setOut(printout);
		
                MainFrame.settext("");
                for (int i=0; i<TOTAL; i++)	
		{
			for (int j=0; j<TOTAL; j++)
				if (pr[j]>max)
				{max=pr[j]; maxadd=j;}
			System.out.println((i + 1) + "\t" + urlset[maxadd] + "\t" + pr[maxadd]);
                        MainFrame.appendaline((i + 1) + "\t" + urlset[maxadd] + "\t" + pr[maxadd]);
			pr[maxadd]=0; max=0;
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		//InputStream alllink = new FileInputStream("院系链接关系.txt");
		//InputStream allurlin = new FileInputStream("院系所有链接.txt");		
		//System.setIn(alllink);		
		//System.setIn(allurlin);
		
		newcrawler.MyCrawler.crawling(new String[]{"http://www.nju.edu.cn"});
		FileWriter fwall=new FileWriter("院系所有链接.txt",false);
		for(String Visitedlink:newcrawler.LinkQueue.getVisitedUrl())
		{
			fwall.write(Visitedlink+"\n");
		}
		fwall.close();
		
		setHashMap();
		setLinks();
		doPageRank();
		outputResult();
	}
}
