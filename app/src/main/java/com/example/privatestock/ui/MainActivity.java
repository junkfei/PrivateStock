package com.example.privatestock.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.privatestock.R;
import com.example.privatestock.R.array;
import com.example.privatestock.R.id;
import com.example.privatestock.R.layout;
import com.example.privatestock.R.menu;
import com.example.privatestock.dao.StockCode;
import com.example.privatestock.util.DataSource;
import com.example.privatestock.util.MySharedPreference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener {

	public static final String TAG = "MainActivity";

	private List<StockCode> stockCodes;

	private AutoCompleteTextView stockCodeText;

	private TextView stock_sh_index, stock_sh_change, stock_sz_index,
			stock_sz_change, stock_chuang_index, stock_chuang_change;

	private static final String STOCK_CODES_FILE = "stockcodes";

	private List<Map<String, Object>> list;

	private String stockCodeString = "";
	private String[] stockCodeStrings = null;
	private final static String stockSHCode = "sh000001";
	private final static String stockSZCode = "sz399001";
	private final static String stockChuangCode = "sz399006";

	private MyShowAdapter adapter;
	private int i = 0;
	private long exitTime = 0;

	private ListView stockListView;

	private Handler handler;
	private Runnable runnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		closeStrictMode();
		initUI();
		getData();
		getstocksh();
		getstocksz();
		getstockchuang();
		initHandler();

	}

	private void initUI() {

		stockListView = (ListView) findViewById(R.id.listview);
		stockListView.setOnItemClickListener(this);
		registerForContextMenu(stockListView);
		stockCodeText = (AutoCompleteTextView) findViewById(R.id.editText_stockId);

		Resources res = getResources();
		String[] stock_names = res.getStringArray(R.array.stocknames);
		String[] stock_codes = res.getStringArray(R.array.stockcodes);
		String[] stockcode_sina = res.getStringArray(R.array.stockcodes_sina);
		String[] stock_letter = res.getStringArray(R.array.stock_letters);

		stockCodes = new ArrayList<StockCode>();

		for (int i = 0; i < stock_names.length; i++) {
			StockCode stockcode = new StockCode(stock_names[i], stock_codes[i],
					stockcode_sina[i], stock_letter[i]);
			stockCodes.add(stockcode);
		}

		StockCodeAdapter adapterAuto = new StockCodeAdapter(stockCodes,
				getApplicationContext(), stockCodeText);
		stockCodeText.setAdapter(adapterAuto);
		stockCodeText.setOnItemClickListener(this);

		stockCodeStrings = new String[1024];

		list = new ArrayList<Map<String, Object>>();
		adapter = new MyShowAdapter(this);
	}

	private void initHandler() {
		handler = new Handler();
		runnable = new Runnable() {
			public void run() {
				getstocksh();
				getstocksz();
				getstockchuang();
				reflashStock();
				handler.postDelayed(this, 1000);
			}
		};
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, 1000);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

		Intent intent = new Intent(MainActivity.this, StockDetailActivity.class);
		String stockCode = list.get(position).get("id").toString();
		Log.i(TAG, "--->" + stockCode);
		intent.putExtra("id", stockCode);
		startActivity(intent);

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {

		case R.id.del:
			handler.removeCallbacks(runnable);
			int position = info.position;
			list.remove(position);
			Log.i("MainActivity","1"+stockCodeStrings.toString());
			stockCodeStrings = delStockCode(stockCodeStrings, position);
			Log.i("MainActivity","2"+stockCodeStrings.toString());
			adapter.notifyDataSetChanged();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handler.postDelayed(runnable, 1000);

		}

		return super.onMenuItemSelected(featureId, item);

	}

	private String[] delStockCode(String[] stockCodeStrings, int position) {
		// TODO Auto-generated method stub
		String[] str=stockCodeStrings;
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < str.length; i++) {
			list.add(str[i]);
		}
		list.remove(position); 
		String[] newStr = list.toArray(new String[1]); // 返回一个包含所有对象的指定类型的数组
		
		return newStr;
	}

	private void reflashStock() {
		list = DataSource.getDataSource(stockCodeStrings);

		adapter.setList(list);

		stockListView.setAdapter(adapter);

		adapter.notifyDataSetChanged();

	}

	public class MyShowAdapter extends BaseAdapter {

		private Context context;
		private List<Map<String, Object>> list;

		public MyShowAdapter(Context context) {
			this.context = context;

		}

		public void setList(List<Map<String, Object>> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();

		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			Map<String, Object> map = new HashMap<String, Object>();

			String stock_nowprice_String, stock_yestodayPrice_String;

			ViewHolder viewHolder = null;
			if (null == convertView) {

				convertView = View.inflate(context, R.layout.stock_itemlist,
						null);
				viewHolder = new ViewHolder();

				viewHolder.stockName_item = (TextView) convertView
						.findViewById(R.id.stock_name);
				viewHolder.stockID_item = (TextView) convertView
						.findViewById(R.id.stock_id);
				viewHolder.stockNow_item = (TextView) convertView
						.findViewById(R.id.stock_now);
				viewHolder.stockYes_item = (TextView) convertView
						.findViewById(R.id.stock_yesprice);
				viewHolder.stockTo_item = (TextView) convertView
						.findViewById(R.id.stock_toprice);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			map = list.get(position);
			stock_nowprice_String = map.get("nowprice").toString();
			stock_yestodayPrice_String = map.get("yestodayOverPrice")
					.toString();

			String increaseString = getIncrease(stock_nowprice_String,
					stock_yestodayPrice_String);
			String increasePercentString = getIncreasePercent(
					stock_nowprice_String, stock_yestodayPrice_String);
			viewHolder.stockName_item
					.setText(map.get("name").toString().trim());
			viewHolder.stockID_item.setText(map.get("id").toString().trim());
			viewHolder.stockNow_item.setText(stock_nowprice_String);
			viewHolder.stockYes_item.setText(increaseString);
			viewHolder.stockTo_item.setText(increasePercentString);

			float increase = Float.parseFloat(increaseString);
			if (increase < 0) {
				viewHolder.stockNow_item.setTextColor(Color.GREEN);
				viewHolder.stockYes_item.setTextColor(Color.GREEN);
				viewHolder.stockTo_item.setTextColor(Color.GREEN);
			} else if (increase > 0) {
				viewHolder.stockNow_item.setTextColor(Color.RED);
				viewHolder.stockYes_item.setTextColor(Color.RED);
				viewHolder.stockTo_item.setTextColor(Color.RED);

			} else {
				viewHolder.stockNow_item.setTextColor(Color.WHITE);
				viewHolder.stockYes_item.setTextColor(Color.WHITE);
				viewHolder.stockTo_item.setTextColor(Color.WHITE);
			}

			return convertView;
		}

	}

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

	public class ViewHolder {
		TextView stockName_item;
		TextView stockID_item;
		TextView stockNow_item;
		TextView stockYes_item;
		TextView stockTo_item;

	}

	private void saveData() {
		// TODO Auto-generated method stub
		Context context = getBaseContext();

		MySharedPreference mySharedPreference = new MySharedPreference(context);

		Map<String, Object> map = new HashMap<String, Object>();
		String savedStockCodes = "";
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				map = list.get(i);
				savedStockCodes += map.get("id").toString() + ",";

			}
			savedStockCodes = savedStockCodes.substring(0,
					(savedStockCodes.length() - 1));
		}

		boolean flag = mySharedPreference.saveData(STOCK_CODES_FILE,
				savedStockCodes);

		if (flag) {
			Toast.makeText(MainActivity.this, "保存数据成功", 1).show();
		} else {
			Toast.makeText(MainActivity.this, "保存数据失败", 1).show();
		}
	}

	private void getData() {
		// TODO Auto-generated method stub
		Context context = getBaseContext();

		MySharedPreference mySharedPreference = new MySharedPreference(context);

		Map<String, Object> map = mySharedPreference.getData(STOCK_CODES_FILE);
		String savedStockCodes = map.get("stockCodesValue").toString();
		if (savedStockCodes != null && !savedStockCodes.isEmpty()) {

			String[] stocksavedCodeString = savedStockCodes.split(",");

			for (int j = 0; j < stocksavedCodeString.length; j++) {
				stockCodeStrings[j] = stocksavedCodeString[j];
			}
			i = stocksavedCodeString.length;

		} else {
			stockCodeStrings[0] = stockSHCode; // 如果股票列表为空，增加上证指数
		}

		if (stockCodeStrings != null) {
			reflashStock();
		}
		list = DataSource.getDataSource(stockCodeStrings);
	}

	private void getstocksh() {
		// TODO Auto-generated method stub

		stock_sh_index = (TextView) findViewById(R.id.stock_sh_index);
		stock_sh_change = (TextView) findViewById(R.id.stock_sh_change);

		List<Map<String, Object>> list_sh_data = new ArrayList<Map<String, Object>>();
		list_sh_data = DataSource.getDataSource(new String[] { stockSHCode });

		Map<String, Object> map = new HashMap<String, Object>();
		map = list_sh_data.get(0);

		String nowPrice = map.get("nowprice").toString().trim();
		String yestodayPrice = map.get("yestodayOverPrice").toString().trim();
		String changeString = " " + getIncreasePercent(nowPrice, yestodayPrice)
				+ "  " + getIncrease(nowPrice, yestodayPrice) + " ";
		stock_sh_index.setText(nowPrice);
		stock_sh_change.setText(changeString);

		changeSHStockColor(nowPrice, yestodayPrice);

	}

	private void getstocksz() {
		// TODO Auto-generated method stub

		stock_sz_index = (TextView) findViewById(R.id.stock_sz_index);
		stock_sz_change = (TextView) findViewById(R.id.stock_sz_change);

		List<Map<String, Object>> list_sz_data = new ArrayList<Map<String, Object>>();
		list_sz_data = DataSource.getDataSource(new String[] { stockSZCode });

		Map<String, Object> map = new HashMap<String, Object>();
		map = list_sz_data.get(0);

		String nowPrice = map.get("nowprice").toString().trim();
		String yestodayPrice = map.get("yestodayOverPrice").toString().trim();
		String changeString = " " + getIncreasePercent(nowPrice, yestodayPrice)
				+ "  " + getIncrease(nowPrice, yestodayPrice) + " ";
		stock_sz_index.setText(nowPrice);
		stock_sz_change.setText(changeString);

		changeSZStockColor(nowPrice, yestodayPrice);

	}

	private void getstockchuang() {
		// TODO Auto-generated method stub
		stock_chuang_index = (TextView) findViewById(R.id.stock_chuang_index);
		stock_chuang_change = (TextView) findViewById(R.id.stock_chuang_change);

		List<Map<String, Object>> list_chuang_data = new ArrayList<Map<String, Object>>();
		list_chuang_data = DataSource
				.getDataSource(new String[] { stockChuangCode });

		Map<String, Object> map = new HashMap<String, Object>();
		map = list_chuang_data.get(0);

		String nowPrice = map.get("nowprice").toString().trim();
		String yestodayPrice = map.get("yestodayOverPrice").toString().trim();
		String changeString = " " + getIncreasePercent(nowPrice, yestodayPrice)
				+ "  " + getIncrease(nowPrice, yestodayPrice) + " ";
		stock_chuang_index.setText(nowPrice);
		stock_chuang_change.setText(changeString);

		changeChuangStockColor(nowPrice, yestodayPrice);

	}

	private void changeSHStockColor(String nowPrice, String yestodayPrice) {
		// TODO Auto-generated method stub
		String increaseString = getIncrease(nowPrice, yestodayPrice);
		float increase = Float.parseFloat(increaseString);
		if (increase < 0) {
			stock_sh_index.setTextColor(Color.GREEN);
			stock_sh_change.setTextColor(Color.GREEN);
		} else if (increase > 0) {
			stock_sh_index.setTextColor(Color.RED);
			stock_sh_change.setTextColor(Color.RED);
		} else {
			stock_sh_index.setTextColor(Color.WHITE);
			stock_sh_change.setTextColor(Color.WHITE);
		}

	}

	private void changeSZStockColor(String nowPrice, String yestodayPrice) {
		// TODO Auto-generated method stub
		String increaseString = getIncrease(nowPrice, yestodayPrice);
		float increase = Float.parseFloat(increaseString);
		if (increase < 0) {
			stock_sz_index.setTextColor(Color.GREEN);
			stock_sz_change.setTextColor(Color.GREEN);
		} else if (increase > 0) {
			stock_sz_index.setTextColor(Color.RED);
			stock_sz_change.setTextColor(Color.RED);
		} else {
			stock_sz_index.setTextColor(Color.WHITE);
			stock_sz_change.setTextColor(Color.WHITE);
		}

	}

	private void changeChuangStockColor(String nowPrice, String yestodayPrice) {
		// TODO Auto-generated method stub
		String increaseString = getIncrease(nowPrice, yestodayPrice);
		float increase = Float.parseFloat(increaseString);
		if (increase < 0) {
			stock_chuang_index.setTextColor(Color.GREEN);
			stock_chuang_change.setTextColor(Color.GREEN);
		} else if (increase > 0) {
			stock_chuang_index.setTextColor(Color.RED);
			stock_chuang_change.setTextColor(Color.RED);
		} else {
			stock_chuang_index.setTextColor(Color.WHITE);
			stock_chuang_change.setTextColor(Color.WHITE);
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);

		getMenuInflater().inflate(R.menu.action, menu);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		saveData();
		super.onDestroy();
	}

	/*
	 * @Override public boolean dispatchTouchEvent(MotionEvent ev) { // TODO
	 * Auto-generated method stub
	 * 
	 * if (ev.getAction() == MotionEvent.ACTION_MOVE) {
	 * stockListView.dispatchTouchEvent(ev); }
	 * 
	 * return super.dispatchTouchEvent(ev); }
	 */

	public static void closeStrictMode() {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll().penaltyLog().build());
	}

	// 退出程序
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();

			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class StockCodeAdapter extends BaseAdapter implements Filterable {

		private ArrayFilter mFilter;
		private List<StockCode> mList;
		private Context context;
		private ArrayList<StockCode> stockData;
		private AutoCompleteTextView textview;

		public StockCodeAdapter(List<StockCode> list, Context context,
				AutoCompleteTextView textview) {
			// TODO Auto-generated constructor stub
			this.mList = list;
			this.context = context;
			this.textview = textview;

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mList == null ? 0 : mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view;
			ViewHolderAuto holder;

			final int i = position;

			if (convertView == null) {
				view = View.inflate(context, R.layout.stockcode_item, null);

				holder = new ViewHolderAuto();
				holder.stock_name = (TextView) view
						.findViewById(R.id.stock_name);
				holder.stockcodeSina = (TextView) view
						.findViewById(R.id.stock_code_sina);
				holder.btnAdd = (Button) view.findViewById(R.id.btnAdd);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolderAuto) view.getTag();
			}

			StockCode stockcode = mList.get(position);
			holder.stock_name.setText(stockcode.getName());
			holder.stockcodeSina.setText(stockcode.getStockcode_sina());

//			view.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					String Stockcode_sinaText = mList.get(i)
//							.getStockcode_sina().toString();
//					String StockNametext = mList.get(i).getName().toString();
//					addStockList(Stockcode_sinaText);
//					Toast.makeText(context, StockNametext + " 添加成功", 1).show();
//					textview.setText(Stockcode_sinaText.substring(2,
//							Stockcode_sinaText.length()));
//				}
//			});

			holder.btnAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String Stockcode_sinaText = mList.get(i)
							.getStockcode_sina().toString();
					String StockNametext = mList.get(i).getName().toString();
					addStockList(Stockcode_sinaText);
					
//					textview.setText(Stockcode_sinaText.substring(2,
//							Stockcode_sinaText.length()));
					textview.setText("");
				}
			});

			return view;
		}

		@Override
		public Filter getFilter() {
			// TODO Auto-generated method stub
			if (mFilter == null) {
				mFilter = new ArrayFilter();
			}
			return mFilter;
		}

		private class ArrayFilter extends Filter {

			@Override
			protected FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();

				if (stockData == null) {
					stockData = new ArrayList<StockCode>(mList);
				}

				if (prefix == null || prefix.length() == 0) {
					ArrayList<StockCode> list = stockData;
					results.values = list;
					results.count = list.size();
				} else {
					String prefixString = prefix.toString().toUpperCase();

					ArrayList<StockCode> unfilteredValues = stockData;
					int count = unfilteredValues.size();

					ArrayList<StockCode> newValues = new ArrayList<StockCode>(
							count);

					for (int i = 0; i < count; i++) {
						StockCode stockcode = unfilteredValues.get(i);
						if (stockcode != null) {

							if (stockcode.getName() != null
									&& stockcode.getName().startsWith(
											prefixString)) {
								newValues.add(stockcode);
							} else if (stockcode.getStockcode() != null
									&& stockcode.getStockcode().startsWith(
											prefixString)) {
								newValues.add(stockcode);
							} else if (stockcode.getStock_letter() != null
									&& stockcode.getStock_letter().startsWith(
											prefixString)) {
								newValues.add(stockcode);
							}
						}
					}

					results.values = newValues;
					results.count = newValues.size();
				}

				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {

				mList = (List<StockCode>) results.values;
				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}

		}

	}

	public void addStockList(String Stockcode_sinaText) {
		boolean flag = true;
		stockCodeString = Stockcode_sinaText;

		if (!stockCodeString.isEmpty() && !stockCodeString.equals("")) {
			for (int k = 0; k < stockCodeStrings.length; k++) {
				if (stockCodeString.equals(stockCodeStrings[k])) {
					flag = false;
				}
			}
			if (flag) {
				Toast.makeText(MainActivity.this, " 添加成功", 1).show();
				handler.removeCallbacks(runnable);
				stockCodeStrings[i++] = stockCodeString;
				reflashStock();
				handler.postDelayed(runnable, 1000);
			} else {
				Toast.makeText(MainActivity.this, "股票代码已存在,不要重复添加", 0).show();

			}

		} else {
			Toast.makeText(MainActivity.this, "请输入股票代码,如:sh600637", 0).show();
		}
	}

	static class ViewHolderAuto {
		public TextView stock_name;
		public TextView stockcodeSina;
		public Button btnAdd;
	}

}
