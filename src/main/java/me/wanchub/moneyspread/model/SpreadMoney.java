package me.wanchub.moneyspread.model;

import java.util.Date;
import java.util.List;

public class SpreadMoney {
	public Date spread_time;
	public int total_money;
	public int finish_money;
	public List<SpreadMoneyDetail> user_detail_info;
}
