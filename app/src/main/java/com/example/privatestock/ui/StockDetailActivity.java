package com.example.privatestock.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HttpException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.privatestock.R;
import com.example.privatestock.R.id;
import com.example.privatestock.R.layout;
import com.example.privatestock.dao.SinaStockInfo;
import com.example.privatestock.dao.SinaStockInfo.BuyOrSellInfo;
import com.example.privatestock.util.DataSource;
import com.example.privatestock.util.MySinaStockClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
//import android.widget.TabHost;
import android.widget.Toast;
//import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

public class StockDetailActivity extends Activity implements OnClickListener
//		,OnTabChangeListener 
{

	private TextView titleTextView;
	private TextView nowPriceTextView, stock_increaseTextView,
			stock_increase_percentTextView;
	private TextView todayOpenPriceTextView, highestPriceTextView,
			lowestPriceTextView;
	private TextView tradeCountTextView, tradeMoneyTextView,
			yestodayOverPriceTextView;

	private TextView sell1priceTextView, sell2priceTextView,
			sell3priceTextView, sell4priceTextView, sell5priceTextView;
//	private TextView sell1countTextView, sell2countTextView,
//			sell3countTextView, sell4countTextView, sell5countTextView;

	private TextView buy1priceTextView, buy2priceTextView, buy3priceTextView,
			buy4priceTextView, buy5priceTextView;
//	private TextView buy1countTextView, buy2countTextView, buy3countTextView,
//			buy4countTextView, buy5countTextView;
	
	private ListView listview;

	private Button backBtn;
//	private TabHost tabHost;
//	private TabWidget tabWidget;
	private List<Map<String, Object>> stockList;
	private String[] stockCodeDetailStrings = null;
	private ImageView stockNowKPic;
	private ImageView stockDayKPic;
	private ImageView stockWeekKPic;
	private ImageView stockMonthKPic;
	private Bitmap bm = null;
	private MySinaStockClient mSinaStockClient;
	private String StockName = "";
	private  String stockCode = null;
	private String nowprice = "";
	private String todayOpenPrice = "";

	private String highestPrice = "";
	private String lowestPrice = "";
	private String tradeCount = "";
	private String tradeMoney = "";
	private String yestodayOverPrice = "";

	private String[] sellCount;
	private String[] sellPrice;

	private String[] buyCount;
	private String[] buyPrice;
	
    Handler handler = new Handler();
	private Runnable runnable;
	private Map<String, Object> map;
	
	private List<String> wholePointPricelist =  new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stockdetail);
//		closeStrictMode();

		initDetailUI();
		initData();
//		initTabHost();
		
//		new MyTask().execute(0, mSinaStockClient.IMAGE_TYPE_MINITE);
		reflashData(map);
		initPC();
		initHandler(stockCode);
	}

	private void initPC() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					String content = null;
					HttpURLConnection connection = null;
					String code = null;
					
					int isSH000001 = stockCode.indexOf("sh000001");
					if(isSH000001 != -1){
						code = stockCode;
					} else{
						code = stockCode.substring(2);
					}
					try {
						URL url = new URL("http://www.shdjt.com/gpdmfs.asp?gpdm=" + code);
						connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("GET");

						connection.setRequestProperty("User-Agent",
								"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
						connection.setRequestProperty("Accept",
								"image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-powerpoint, application/vnd.ms-excel, application/msword, */*");
						connection.setRequestProperty("Accept-Language", "zh-cn");
						connection.setRequestProperty("UA-CPU", "x86");
						// 为什么没有deflate呢
						connection.setRequestProperty("Accept-Encoding", "gzip");
						connection.setRequestProperty("Content-type", "text/html");
						// keep-Alive，有什么用呢，你不是在访问网站，你是在采集。嘿嘿。减轻别人的压力，也是减轻自己。
						connection.setRequestProperty("Connection", "close");
						// 不要用cache，用了也没有什么用，因为我们不会经常对一个链接频繁访问。（针对程序）
						connection.setUseCaches(false);
						connection.setConnectTimeout(6 * 1000);
						connection.setReadTimeout(6 * 1000);
						connection.setDoOutput(true);
						connection.setDoInput(true);
						connection.setRequestProperty("Charset", "gb2312");

						connection.connect();

						if (200 == connection.getResponseCode()) {
							InputStream inputStream = null;
							if (!TextUtils.isEmpty(connection.getContentEncoding())) {
								String encode = connection.getContentEncoding().toLowerCase();
								if (!TextUtils.isEmpty(encode) && encode.indexOf("gzip") >= 0) {
									inputStream = new GZIPInputStream(connection.getInputStream());
								}
							}

							if (null == inputStream) {
								inputStream = connection.getInputStream();
							}

							BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "gbk"));
							StringBuilder builder = new StringBuilder();
							String line = null;
							while ((line = reader.readLine()) != null) {
								builder.append(line).append("\n");
							}
							content = builder.toString();
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (connection != null) {
							connection.disconnect();
						}
					}

					Document doc = Jsoup.parse(content);
					// 获得table
					Element table = doc.getElementById("gpdm_divid");
					// 获得所有行
					Elements rows = table.getElementsByTag("tr");

//					int day = -1;
					int now_hour = 8;
					Calendar now = Calendar.getInstance(); 
					for (int i = rows.size() - 2; i > 0; i--) {
						// 获得时间
						String String_date = rows.get(i).getElementsByTag("td").get(0).text();
						// 获得价格
						String price = rows.get(i).getElementsByTag("td").get(3).text();

						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date = sdf.parse(String_date);
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);

//						if (i == rows.size() - 2) {
//							day = cal.get(Calendar.DATE);
//						}
						if (cal.get(Calendar.DATE) == now.get(Calendar.DAY_OF_MONTH)) {
							if (cal.get(Calendar.HOUR_OF_DAY) != now_hour) {
								SimpleDateFormat sdf2 = new SimpleDateFormat("H:mm");
//								Log.e("时间: ", sdf2.format(date));
//								Log.e("价格: ", price);	
								wholePointPricelist.add("时间: "+ sdf2.format(date) + "    " + "价格: " + price);
								now_hour = cal.get(Calendar.HOUR_OF_DAY);
							}
						}
//						Log.e("", wholePointPricelist.toString());
					}
//					arrayAdapter = new ArrayAdapter<String> (StockDetailActivity.this, android.R.layout.simple_list_item_1, wholePointPricelist);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				handler.post(new Runnable() {  
                    @Override  
                    public void run() {  
                    	listview.setAdapter(new ArrayAdapter<String> (StockDetailActivity.this, android.R.layout.simple_list_item_1, wholePointPricelist));
                    }  
                });
				
			}
		}.start();
	}

	private void initHandler(final String stockCode) {
		runnable = new Runnable() {
			public void run() {
				map = getMap(stockCode);
				reflashData(map);
//				new MyTask().execute(0, mSinaStockClient.IMAGE_TYPE_MINITE);
				handler.postDelayed(this, 1000);
			}
		};
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, 1000);
	}

	private void initData() {
		// TODO Auto-generated method stub
		stockCodeDetailStrings = new String[1024];
		sellCount = new String[5];
		sellPrice = new String[5];
		buyCount = new String[5];
		buyPrice = new String[5];

		mSinaStockClient = MySinaStockClient.getInstance();
		Intent intent = getIntent();
		
		stockCode = intent.getStringExtra("id");
		stockList = new ArrayList<Map<String, Object>>();
		map = new HashMap<String, Object>();
		map=getMap(stockCode);
		initDetailTitle(stockCode, map);
		
	}

	private Map<String, Object> getMap(String stockCode) {
		// TODO Auto-generated method stub
		Map<String,Object> map=new HashMap<String, Object>();
		stockCodeDetailStrings[0] = stockCode;
		stockList = DataSource.getDataSource(stockCodeDetailStrings);
		map = stockList.get(0);
		return map;
	}

	private void initDetailTitle(String stockCode, Map<String, Object> map) {
		StockName = map.get("name").toString();

		String stockCodeNum = stockCode.substring(2, stockCode.length());
		String stockCodeExchange = stockCode.substring(0, 2).toUpperCase();

		String StockCodeTitle = stockCodeNum + "." + stockCodeExchange;

		if (stockCode != null && !stockCode.isEmpty()) {
			titleTextView.setText(StockName + " " + StockCodeTitle);
		}
	}

	private void initDetailUI() {
		titleTextView = (TextView) findViewById(R.id.titleTextview);

		nowPriceTextView = (TextView) findViewById(R.id.nowpriceValue);
		stock_increaseTextView = (TextView) findViewById(R.id.stock_increaseValue);
		stock_increase_percentTextView = (TextView) findViewById(R.id.stock_increase_percentValue);

		todayOpenPriceTextView = (TextView) findViewById(R.id.todayOpenPriceValue);
		highestPriceTextView = (TextView) findViewById(R.id.highestPriceValue);
		lowestPriceTextView = (TextView) findViewById(R.id.lowestPriceValue);

		tradeCountTextView = (TextView) findViewById(R.id.tradeCountValue);
		tradeMoneyTextView = (TextView) findViewById(R.id.tradeMoneyValue);
		// yestodayOverPriceTextView=(TextView)findViewById(R.id.yestodayOverPriceValue);

		backBtn = (Button) findViewById(R.id.backBtn);
		backBtn.setOnClickListener(this);
		
		listview = (ListView) findViewById(R.id.listview);
	}

	private void reflashData(Map<String, Object> map) {
		// 获取数据
		nowprice = map.get("nowprice").toString();
		todayOpenPrice = map.get("todayOpenPrice").toString();
		highestPrice = map.get("highestPrice").toString();
		lowestPrice = map.get("lowestPricePrice").toString();

		tradeCount = map.get("tradeCount").toString();
		tradeMoney = map.get("tradeMoney").toString();
		yestodayOverPrice = map.get("yestodayOverPrice").toString();

		BuyOrSellInfo[] sellInfo = (BuyOrSellInfo[]) map.get("sellInfo");

		for (int i = 0; i < sellInfo.length; i++) {
			sellCount[i] = sellInfo[i].mCount / 100 + "";
			sellPrice[i] = sellInfo[i].mPrice + "";
		}

		BuyOrSellInfo[] buyInfo = (BuyOrSellInfo[]) map.get("buyInfo");

		for (int i = 0; i < buyInfo.length; i++) {
			buyCount[i] = buyInfo[i].mCount / 100 + "";
			buyPrice[i] = buyInfo[i].mPrice + "";
		}

		// 更新UI数据
		nowPriceTextView.setText(nowprice);
		String increaseString = getIncrease(nowprice, yestodayOverPrice);
		stock_increaseTextView.setText(increaseString);
		String increasePercentString = getIncreasePercent(nowprice,
				yestodayOverPrice);
		stock_increase_percentTextView.setText(increasePercentString);

		todayOpenPriceTextView.setText(todayOpenPrice);
		highestPriceTextView.setText(highestPrice);
		lowestPriceTextView.setText(lowestPrice);

		tradeCountTextView.setText(tradeCount);
		tradeMoneyTextView.setText(tradeMoney);
		// yestodayOverPriceTextView.setText(yestodayOverPrice);

//		sell5priceTextView.setText(sellPrice[4]);
//		sell4priceTextView.setText(sellPrice[3]);
//		sell3priceTextView.setText(sellPrice[2]);
//		sell2priceTextView.setText(sellPrice[1]);
//		sell1priceTextView.setText(sellPrice[0]);
//
//		sell5countTextView.setText(sellCount[4]);
//		sell4countTextView.setText(sellCount[3]);
//		sell3countTextView.setText(sellCount[2]);
//		sell2countTextView.setText(sellCount[1]);
//		sell1countTextView.setText(sellCount[0]);
//
//		buy5priceTextView.setText(buyPrice[4]);
//		buy4priceTextView.setText(buyPrice[3]);
//		buy3priceTextView.setText(buyPrice[2]);
//		buy2priceTextView.setText(buyPrice[1]);
//		buy1priceTextView.setText(buyPrice[0]);
//
//		buy5countTextView.setText(buyCount[4]);
//		buy4countTextView.setText(buyCount[3]);
//		buy3countTextView.setText(buyCount[2]);
//		buy2countTextView.setText(buyCount[1]);
//		buy1countTextView.setText(buyCount[0]);
//
//		for (int i = 0; i < sellPrice.length; i++) {
//			changePanKouSellColor(i, sellPrice[i], yestodayOverPrice);
//		}
//
//		for (int i = 0; i < buyPrice.length; i++) {
//			changePanKouBuyColor(i, buyPrice[i], yestodayOverPrice);
//		}

	}

//	private void changePanKouBuyColor(int position, String nowPrice,
//			String yestodayPrice) {
//		// TODO Auto-generated method stub
//		String increaseString = getIncrease(nowPrice, yestodayPrice);
//		float increase = Float.parseFloat(increaseString);
//		if (increase < 0) {
//			switch (position) {
//			case 0:
//				buy1priceTextView.setTextColor(Color.GREEN);
//				break;
//			case 1:
//				buy2priceTextView.setTextColor(Color.GREEN);
//				break;
//			case 2:
//				buy3priceTextView.setTextColor(Color.GREEN);
//				break;
//			case 3:
//				buy4priceTextView.setTextColor(Color.GREEN);
//				break;
//			case 4:
//				buy5priceTextView.setTextColor(Color.GREEN);
//				break;
//			}
//
//		} else if (increase > 0) {
//
//			switch (position) {
//			case 0:
//				buy1priceTextView.setTextColor(Color.RED);
//				break;
//			case 1:
//				buy2priceTextView.setTextColor(Color.RED);
//				break;
//			case 2:
//				buy3priceTextView.setTextColor(Color.RED);
//				break;
//			case 3:
//				buy4priceTextView.setTextColor(Color.RED);
//				break;
//			case 4:
//				buy5priceTextView.setTextColor(Color.RED);
//				break;
//			}
//
//		} else {
//			switch (position) {
//			case 0:
//				buy1priceTextView.setTextColor(Color.WHITE);
//				break;
//			case 1:
//				buy2priceTextView.setTextColor(Color.WHITE);
//				break;
//			case 2:
//				buy3priceTextView.setTextColor(Color.WHITE);
//				break;
//			case 3:
//				buy4priceTextView.setTextColor(Color.WHITE);
//				break;
//			case 4:
//				buy5priceTextView.setTextColor(Color.WHITE);
//				break;
//			}
//
//		}
//
//	}

//	private void changePanKouSellColor(int position, String nowPrice,
//			String yestodayPrice) {
//		// TODO Auto-generated method stub
//		String increaseString = getIncrease(nowPrice, yestodayPrice);
//		float increase = Float.parseFloat(increaseString);
//		if (increase < 0) {
//			switch (position) {
//			case 0:
//				sell1priceTextView.setTextColor(Color.GREEN);
//				break;
//			case 1:
//				sell2priceTextView.setTextColor(Color.GREEN);
//				break;
//			case 2:
//				sell3priceTextView.setTextColor(Color.GREEN);
//				break;
//			case 3:
//				sell4priceTextView.setTextColor(Color.GREEN);
//				break;
//			case 4:
//				sell5priceTextView.setTextColor(Color.GREEN);
//				break;
//			}
//
//		} else if (increase > 0) {
//
//			switch (position) {
//			case 0:
//				sell1priceTextView.setTextColor(Color.RED);
//				break;
//			case 1:
//				sell2priceTextView.setTextColor(Color.RED);
//				break;
//			case 2:
//				sell3priceTextView.setTextColor(Color.RED);
//				break;
//			case 3:
//				sell4priceTextView.setTextColor(Color.RED);
//				break;
//			case 4:
//				sell5priceTextView.setTextColor(Color.RED);
//				break;
//			}
//
//		} else {
//			switch (position) {
//			case 0:
//				sell1priceTextView.setTextColor(Color.WHITE);
//				break;
//			case 1:
//				sell2priceTextView.setTextColor(Color.WHITE);
//				break;
//			case 2:
//				sell3priceTextView.setTextColor(Color.WHITE);
//				break;
//			case 3:
//				sell4priceTextView.setTextColor(Color.WHITE);
//				break;
//			case 4:
//				sell5priceTextView.setTextColor(Color.WHITE);
//				break;
//			}
//
//		}
//
//	}

	private String getIncrease(String nowPrice, String yestodayPrice) {
		// TODO Auto-generated method stub
		String increaseString = null;

		float stock_now_float = Float.parseFloat(nowPrice);
		float stock_yestoday_float = Float.parseFloat(yestodayPrice);

		float increaseFloag = stock_now_float - stock_yestoday_float;
		DecimalFormat df = new DecimalFormat("0.00");

		increaseString = df.format(increaseFloag);
		return increaseString;
	}

	private String getIncreasePercent(String nowPrice, String yestodayPrice) {
		// TODO Auto-generated method stub
		String increasePercentString = null;

		float stock_now_float = Float.parseFloat(nowPrice);
		float stock_yestoday_float = Float.parseFloat(yestodayPrice);

		float increaseFloag = (stock_now_float - stock_yestoday_float)
				/ stock_yestoday_float;
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMaximumFractionDigits(2); // 设置小数点保留几位
		increasePercentString = nf.format(increaseFloag);
		return increasePercentString;
	}

//	private void initTabHost() {
//		tabHost = (TabHost) findViewById(android.R.id.tabhost);
//		tabHost.setup();
//
//		tabWidget = tabHost.getTabWidget();
//
//		LayoutInflater inflater = LayoutInflater.from(this);
//
//		inflater.inflate(R.layout.tab1, tabHost.getTabContentView());
//		inflater.inflate(R.layout.tab2, tabHost.getTabContentView());
//		inflater.inflate(R.layout.tab3, tabHost.getTabContentView());
//		inflater.inflate(R.layout.tab4, tabHost.getTabContentView());
//		inflater.inflate(R.layout.tab5, tabHost.getTabContentView());
//
//		tabHost.addTab(tabHost.newTabSpec("tab01").setIndicator("分明")
//				.setContent(R.id.linearLayout01));
//		tabHost.addTab(tabHost.newTabSpec("tab02").setIndicator("盘口")
//				.setContent(R.id.linearLayout02));
//		tabHost.addTab(tabHost.newTabSpec("tab03").setIndicator("日K")
//				.setContent(R.id.linearLayout03));
//		tabHost.addTab(tabHost.newTabSpec("tab04").setIndicator("周K")
//				.setContent(R.id.linearLayout04));
//		tabHost.addTab(tabHost.newTabSpec("tab05").setIndicator("月K")
//				.setContent(R.id.linearLayout05));
//
//		stockNowKPic = (ImageView) tabHost.findViewById(R.id.nowKPic);
//		stockDayKPic = (ImageView) tabHost.findViewById(R.id.dayKPic);
//		stockWeekKPic = (ImageView) tabHost.findViewById(R.id.weekKPic);
//		stockMonthKPic = (ImageView) tabHost.findViewById(R.id.monthKPic);
//
//		sell1priceTextView = (TextView) tabHost.findViewById(R.id.sell1price);
//		sell2priceTextView = (TextView) tabHost.findViewById(R.id.sell2price);
//		sell3priceTextView = (TextView) tabHost.findViewById(R.id.sell3price);
//		sell4priceTextView = (TextView) tabHost.findViewById(R.id.sell4price);
//		sell5priceTextView = (TextView) tabHost.findViewById(R.id.sell5price);
//
//		sell1countTextView = (TextView) tabHost.findViewById(R.id.sell1count);
//		sell2countTextView = (TextView) tabHost.findViewById(R.id.sell2count);
//		sell3countTextView = (TextView) tabHost.findViewById(R.id.sell3count);
//		sell4countTextView = (TextView) tabHost.findViewById(R.id.sell4count);
//		sell5countTextView = (TextView) tabHost.findViewById(R.id.sell5count);
//
//		buy1priceTextView = (TextView) tabHost.findViewById(R.id.buy1price);
//		buy2priceTextView = (TextView) tabHost.findViewById(R.id.buy2price);
//		buy3priceTextView = (TextView) tabHost.findViewById(R.id.buy3price);
//		buy4priceTextView = (TextView) tabHost.findViewById(R.id.buy4price);
//		buy5priceTextView = (TextView) tabHost.findViewById(R.id.buy5price);
//
//		buy1countTextView = (TextView) tabHost.findViewById(R.id.buy1count);
//		buy2countTextView = (TextView) tabHost.findViewById(R.id.buy2count);
//		buy3countTextView = (TextView) tabHost.findViewById(R.id.buy3count);
//		buy4countTextView = (TextView) tabHost.findViewById(R.id.buy4count);
//		buy5countTextView = (TextView) tabHost.findViewById(R.id.buy5count);
//
//		for (int i = 0; i < tabWidget.getChildCount(); i++) {
//			tabWidget.getChildAt(i).getLayoutParams().height = 45;
//			tabWidget.getChildAt(i).getLayoutParams().width = 65;
//		}
//		tabHost.setOnTabChangedListener(this);
//		tabHost.setCurrentTab(0);
//
//	}

//	@Override
//	public void onTabChanged(String tabId) {
//		// TODO Auto-generated method stub
//		if (tabId.equals("tab01")) {
//			new MyTask().execute(0, mSinaStockClient.IMAGE_TYPE_MINITE);
//		} else if (tabId.equals("tab02")) {
//			// new MyTask().execute(0,mSinaStockClient.IMAGE_TYPE_MINITE);
//		} else if (tabId.equals("tab03")) {
//			new MyTask().execute(0, mSinaStockClient.IMAGE_TYPE_DAILY);
//		} else if (tabId.equals("tab04")) {
//			new MyTask().execute(0, mSinaStockClient.IMAGE_TYPE_WEEKLY);
//		} else if (tabId.equals("tab05")) {
//			new MyTask().execute(0, mSinaStockClient.IMAGE_TYPE_MONTHLY);
//		}
//
//	}

//	public class MyTask extends AsyncTask<Integer, Integer, Bitmap> {
//
//		private int KFlag = 0;
//
//		@Override
//		protected void onPreExecute() {
//			// TODO Auto-generated method stub
//			super.onPreExecute();
//		}
//
//		@Override
//		protected Bitmap doInBackground(Integer... params) {
//			// TODO Auto-generated method stub
//
//			Map<String, Object> map = new HashMap<String, Object>();
//			map = stockList.get(params[0]);
//			String stockcode = map.get("id").toString();
//			try {
//				KFlag = params[1];
//				bm = mSinaStockClient.getStockImage(stockcode, KFlag);
//
//			} catch (HttpException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			return bm;
//		}
//
//		protected void onProgressUpdate(Integer... values) {
//			// TODO Auto-generated method stub
//			super.onProgressUpdate(values);
//
//		}
//
//		@Override
//		protected void onPostExecute(Bitmap result) {
//			// TODO Auto-generated method stub
//			super.onPostExecute(result);
//			if (result != null) {
//				if (KFlag == mSinaStockClient.IMAGE_TYPE_MINITE) {
//					stockNowKPic.setImageBitmap(result);
//				} else if (KFlag == mSinaStockClient.IMAGE_TYPE_DAILY) {
//					stockDayKPic.setImageBitmap(result);
//				} else if (KFlag == mSinaStockClient.IMAGE_TYPE_WEEKLY) {
//					stockWeekKPic.setImageBitmap(result);
//				} else if (KFlag == mSinaStockClient.IMAGE_TYPE_MONTHLY) {
//					stockMonthKPic.setImageBitmap(result);
//				}
//
//			} else {
//				if (KFlag == mSinaStockClient.IMAGE_TYPE_MINITE) {
//					stockNowKPic.setImageBitmap(result);
//				} else if (KFlag == mSinaStockClient.IMAGE_TYPE_DAILY) {
//					stockDayKPic.setImageBitmap(result);
//				} else if (KFlag == mSinaStockClient.IMAGE_TYPE_WEEKLY) {
//					stockWeekKPic.setImageBitmap(result);
//				} else if (KFlag == mSinaStockClient.IMAGE_TYPE_MONTHLY) {
//					stockMonthKPic.setImageBitmap(result);
//				}
//			}
//
//		}
//
//	}
//
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.backBtn:
			finish();
			break;
		default:
			break;
		}

	}
//
//	public static void closeStrictMode() {
//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//				.detectAll().penaltyLog().build());
//	}

}
