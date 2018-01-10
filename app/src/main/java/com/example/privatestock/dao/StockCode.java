package com.example.privatestock.dao;



public class StockCode {

	private String name;
	private String stockcode;
	private String stockcode_sina;
	private String stock_letter;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStockcode() {
		return stockcode;
	}

	public void setStockcode(String stockcode) {
		this.stockcode = stockcode;
	}

	public String getStockcode_sina() {
		return stockcode_sina;
	}

	public void setStockcode_sina(String stockcode_sina) {
		this.stockcode_sina = stockcode_sina;
	}

	public String getStock_letter() {
		return stock_letter;
	}

	public void setStock_letter(String stock_letter) {
		this.stock_letter = stock_letter;
	}

	public StockCode(String name, String stockcode, String stockcode_sina,
			String stock_letter) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.stockcode = stockcode;
		this.stockcode_sina = stockcode_sina;
		this.stock_letter = stock_letter;

	}

	@Override
	public String toString() {
		return "StockCode [name=" + name + ", stockcode=" + stockcode
				+ ", stockcode_sina=" + stockcode_sina + ", stock_letter="
				+ stock_letter + "]";
	}

}
