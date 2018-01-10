
package com.example.privatestock.dao;

import java.text.DecimalFormat;

import android.util.Log;



public class SinaStockInfo {
	
	//股票名字
	private String mName;
	//今日开盘价
	private float mTodayPrice;
	//昨日收盘价
	private float mYestodayPrice;
	//当前价
	private float mNowPrice;
	//今日最高价
	private float mHighestPrice;
	//今日最低价
	private float mLowestPrice;
	//买一价
	private float mBuy1Price;
	//卖一价
	private float mSell1Price;
	//成交股票数，单位“股” 100股为1手
	private long mTradeCount;
	//成交额，单位“元”
	private Double mTradeMoney;
	
	//买单情况
	private BuyOrSellInfo[] mBuy;
	
	//卖单情况
	private BuyOrSellInfo[] mSell;
	
	//日期
	private String mDate;
	//时间
	private String mTime;
	
	private SinaStockInfo(String name, float todayPrice, float yestodayPrice, 
							float nowPrice, float highestPrice, float lowestPrice,
							float buy1Price, float sell1Price, long tradeCount, Double tradeMoney,
							BuyOrSellInfo[] buy, BuyOrSellInfo[] sell, String date, String time) {
		
		mName = name;
		mTodayPrice = todayPrice;
		mYestodayPrice = yestodayPrice;
		
		mNowPrice = nowPrice;
		mHighestPrice = highestPrice;
		mLowestPrice = lowestPrice;
		
		mBuy1Price = buy1Price;
		mSell1Price = sell1Price;
		
		mTradeCount = tradeCount;
		mTradeMoney = tradeMoney;
		
		mBuy = buy;
		mSell = sell;
		
		mDate = date;
		mTime = time;
	}
	

	public static SinaStockInfo parseStockInfo(String source) throws ParseStockInfoException {
		int start = source.indexOf('\"');
		
		String targetString = source.substring(start+1, source.length()-3);
	
		
		
		
		String[] infoStr = targetString.split(",");
		
		
		if(infoStr.length != 33) {
			
			throw new ParseStockInfoException();
			
			
		}
		
		final String name = infoStr[0];
		
		final float todayPrice = Float.parseFloat(infoStr[1]);
		final float yestodayPrice = Float.parseFloat(infoStr[2]);
		final float nowPrice = Float.parseFloat(infoStr[3]);
		final float highestPrice = Float.parseFloat(infoStr[4]);

		final float lowestPrice = Float.parseFloat(infoStr[5]);
		final float buy1Price = Float.parseFloat(infoStr[6]);
		final float sell1Price = Float.parseFloat(infoStr[7]);
		
		final long tradeCount = Long.parseLong(infoStr[8]);
		
		final Double tradeMoney = Double.parseDouble(infoStr[9]);
		
		BuyOrSellInfo buy1 = new BuyOrSellInfo(Long.parseLong(infoStr[10]), Float.parseFloat(infoStr[11]));
		BuyOrSellInfo buy2 = new BuyOrSellInfo(Long.parseLong(infoStr[12]), Float.parseFloat(infoStr[13]));
		BuyOrSellInfo buy3 = new BuyOrSellInfo(Long.parseLong(infoStr[14]), Float.parseFloat(infoStr[15]));
		BuyOrSellInfo buy4 = new BuyOrSellInfo(Long.parseLong(infoStr[16]), Float.parseFloat(infoStr[17]));
		BuyOrSellInfo buy5 = new BuyOrSellInfo(Long.parseLong(infoStr[18]), Float.parseFloat(infoStr[19]));
		
		BuyOrSellInfo sell1 = new BuyOrSellInfo(Long.parseLong(infoStr[20]), Float.parseFloat(infoStr[21]));
		BuyOrSellInfo sell2 = new BuyOrSellInfo(Long.parseLong(infoStr[22]), Float.parseFloat(infoStr[23]));
		BuyOrSellInfo sell3 = new BuyOrSellInfo(Long.parseLong(infoStr[24]), Float.parseFloat(infoStr[25]));
		BuyOrSellInfo sell4 = new BuyOrSellInfo(Long.parseLong(infoStr[26]), Float.parseFloat(infoStr[27]));
		BuyOrSellInfo sell5 = new BuyOrSellInfo(Long.parseLong(infoStr[28]), Float.parseFloat(infoStr[29]));
		
		final String date = infoStr[30];
		final String time = infoStr[31];
		
	
		
		SinaStockInfo stockInfo = new SinaStockInfo(name, todayPrice, yestodayPrice, nowPrice, 
										highestPrice, lowestPrice, buy1Price, sell1Price, 
										tradeCount, tradeMoney, 
										new BuyOrSellInfo[]{buy1, buy2, buy3, buy4, buy5}, 
										new BuyOrSellInfo[]{sell1, sell2, sell3, sell4, sell5}, 
										date, time);
	
		
		return stockInfo;
	}
	
	/**
	 * 获取股票名称
	 * @return 股票名称
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * 获取今日开盘价
	 * @return 今日股票开盘价
	 */
	public float getTodayPrice() {
		return mTodayPrice;
	}
	
	/**
	 * 获取昨日收盘价
	 * @return 昨日收盘价
	 */
	public float getYestodayPrice() {
		return mYestodayPrice;
	}
	
	/**
	 * 获取当前股价
	 * @return 当前股价
	 */
	public float getNowPrice() {
		return mNowPrice;
	}
	
	/**
	 * 获取今日最高价
	 * @return 今日最高价
	 */
	public float getHighestPrice() {
		return mHighestPrice;
	}
	
	/**
	 * 获取今日最低价
	 * @return 今日最低价
	 */
	public float getLowestPrice() {
		return mLowestPrice;
	}
	
	
	public long getTradeCount() {
		return mTradeCount;
	}
	

	public Double getTradeMoney() {
		
		return mTradeMoney;
	}
	

	public BuyOrSellInfo[] getBuyInfo() {
		return mBuy;
	}
	
	
	public BuyOrSellInfo[] getSellInfo() {
		return mSell;
	}
	

	public String getDate() {
		return mDate;
	}
	

	public String getTime() {
		return mTime;
	}
	
	
	public static class ParseStockInfoException extends Exception{
		public ParseStockInfoException(){
			super("Parse StockInfo error!");
		}
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		DecimalFormat df = new DecimalFormat("0");
		String tradeMoney=df.format(getTradeMoney());
		StringBuilder sb = new StringBuilder();
		sb.append("股票名称:" + getName() + "\n");
		sb.append("今日开盘价: " + getTodayPrice() + "元\n");
		sb.append("昨日收盘价： " + getYestodayPrice() + "元\n");
		sb.append("当前股价:" + getNowPrice() + "元\n");
		sb.append("今日最高价:" + getHighestPrice() + "元\n");
		sb.append("今日最低价:" + getLowestPrice() + "元\n");
		sb.append("今日交易量： " + getTradeCount() + "股\n");
		sb.append("今日成交量： " + tradeMoney + "元\n"); 
		
		BuyOrSellInfo[] buy = getBuyInfo();
		sb.append("买一：\n" + buy[0] + "\n");
		sb.append("买二：\n" + buy[1] + "\n");
		sb.append("买三：\n" + buy[2] + "\n");
		sb.append("买四：\n" + buy[3] + "\n");
		sb.append("买五：\n" + buy[4] + "\n");
		
		BuyOrSellInfo[] sell = getSellInfo();
		sb.append("卖一：\n" + sell[0] + "\n");
		sb.append("卖二：\n" + sell[1] + "\n");
		sb.append("卖三：\n" + sell[2] + "\n");
		sb.append("卖四：\n" + sell[3] + "\n");
		sb.append("卖五：\n" + sell[4] + "\n");
		
		sb.append("时间:" + getTime() + "\n");
		sb.append("日期:" + getDate() + "\n");
		
		return sb.toString();
	}


	//买单或卖单信息
	public static class BuyOrSellInfo{
		//数量。单位为“股”
		public long mCount;
		//价格
		public float mPrice;
		
		public BuyOrSellInfo(long count, float price){
			mCount = count;
			mPrice = price;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("数量:" + mCount + "股");
			sb.append("价格:" + mPrice + "元");
			return sb.toString();
		}
	}
}
