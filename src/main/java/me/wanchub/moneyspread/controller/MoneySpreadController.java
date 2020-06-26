package me.wanchub.moneyspread.controller;

import java.sql.SQLException;
import java.util.Random;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import me.wanchub.moneyspread.db.jdbc;
import me.wanchub.moneyspread.model.SpreadMoney;
import me.wanchub.moneyspread.model.SpreadMoneyDetail;

@RestController
public class MoneySpreadController {
	// 룸 입장 API
	@GetMapping(path="/room/in")
	public void EnterRoom(HttpHeaders header) throws ClassNotFoundException, SQLException {
		if (jdbc.conn == null) {
			jdbc.ConnectDB();
		}
		
		String x_room_id = header.getFirst("X-ROOM-ID");
		int x_user_id = Integer.parseInt(header.getFirst("X-USER-ID"));
		
		jdbc.InsertRoomMember(x_room_id, x_user_id);
	}
	
	// 뿌리기 API
	@GetMapping(path="/money/spread")
	public String SpreadMoney(HttpHeaders header, int money, int member_cnt) throws ClassNotFoundException, SQLException {
		if (jdbc.conn == null) {
			jdbc.ConnectDB();
		}
		
		String x_room_id = header.getFirst("X-ROOM-ID");
		int x_user_id = Integer.parseInt(header.getFirst("X-USER-ID"));
		
		// 이미 방이 구현되어 있는지 확인
		boolean exist_room = true;
		while(exist_room) {
			exist_room = jdbc.ExistRoomMember(x_room_id);
		}
		
		// 뿌리기 
		return jdbc.SpreadMoney(x_user_id, x_room_id, money, member_cnt);
	}
	
	// 받기 API
	@GetMapping(path="/money/get")
	public int GetMoney(HttpHeaders header, String token) throws ClassNotFoundException, SQLException
	{
		int result = 0;
		
		if (jdbc.conn == null) {
			jdbc.ConnectDB();
		}
		
		String x_room_id = header.getFirst("X-ROOM-ID");
		int x_user_id = Integer.parseInt(header.getFirst("X-USER-ID"));
		
		// 뿌리기 방이 자기 자신이 주도한건지 확인
		result = jdbc.CheckToken(x_user_id, x_room_id, token, false);
		if (result != 3) {
			return 9000;
		}
		
		// 이미 이 유저가 토크을 부여받았는지 확인
		result = jdbc.CheckToken(x_user_id, x_room_id, token, true);
		if (result == 1) {
			return 9001;
		}
		
		// 해당사항 없으면 부여받음
		return jdbc.GetMoney(x_user_id, x_room_id, token);
	}
	
	//조회 API
	@GetMapping(path="/money/search")
	public SpreadMoney SearchMoney(HttpHeaders header, String token) throws ClassNotFoundException, SQLException
	{
		if (jdbc.conn == null) {
			jdbc.ConnectDB();
		}
		
		String x_room_id = header.getFirst("X-ROOM-ID");
		int x_user_id = Integer.parseInt(header.getFirst("X-USER-ID"));
		
		return jdbc.CheckSpreadMoneyInfo(token, x_user_id);
	}
}
