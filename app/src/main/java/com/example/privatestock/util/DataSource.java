package com.example.privatestock.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;

import com.example.privatestock.dao.SinaStockInfo;
import com.example.privatestock.dao.SinaStockInfo.ParseStockInfoException;

public class DataSource {
	
	public static List<Map<String,Object>> getDataSource(String[] stockCodes) {
		List<Map<String, Object>> list=new ArrayList<Map<String,Object>>();
		Map<String, Object> map=null;
		MySinaStockClient sinaStockClient=MySinaStockClient.getInstance();
		SinaStockInfo stockInfo=null;
		String[] myStockcodes=stockCodes;
		try {
			 for(int i=0;i<myStockcodes.length;i++){
				 stockInfo =sinaStockClient.getStockInfo(
						 myStockcodes[i],"gbk");
				 if(stockInfo.toString()!=null&&!stockInfo.toString().isEmpty()){
						DecimalFormat df = new DecimalFormat("0");
						String tradeMoney=df.format(stockInfo.getTradeMoney());
						map=new HashMap<String, Object>();
						map.put("name",stockInfo.getName());
						map.put("id",myStockcodes[i]);
						map.put("nowprice",stockInfo.getNowPrice());
						map.put("todayOpenPrice",stockInfo.getTodayPrice());
						map.put("yestodayOverPrice",stockInfo.getYestodayPrice());
						map.put("highestPrice",stockInfo.getHighestPrice());
						map.put("lowestPricePrice",stockInfo.getLowestPrice());
						map.put("tradeCount",stockInfo.getTradeCount());
						map.put("tradeMoney",tradeMoney);
						map.put("sellInfo",stockInfo.getSellInfo());
						map.put("buyInfo",stockInfo.getBuyInfo());
					}
				 list.add(map);
			 }
			 
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseStockInfoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		return list;

	}
}
