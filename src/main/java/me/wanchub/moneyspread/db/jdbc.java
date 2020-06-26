package me.wanchub.moneyspread.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Random;

import me.wanchub.moneyspread.model.SpreadMoney;
import me.wanchub.moneyspread.model.SpreadMoneyDetail;

public class jdbc {
	public static Connection conn;
	
	/*************************
	 * DB Connection
	 *************************/
	public static void ConnectDB() throws ClassNotFoundException
	{
		try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String connectionUrl = "jdbc:sqlserver://localhost:10161;database=largeDB;integratedSecurity=true";
            conn = DriverManager.getConnection(connectionUrl);
        } catch (SQLException sqle) {
            System.out.println("SQLException : " + sqle);
        }
	}
	
	/*************************
	 * 참가자 Insert
	 *************************/
	public static int InsertRoomMember(String x_room_id, int x_user_id) throws SQLException
	{
		PreparedStatement pstmt = null;
		int result = 0;
		
		try {
			if (conn == null)
				return -1;
			
			String sql = "insert into dbo.money_room_member(x_room_id, x_user_id, reg_dt) values(?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, x_room_id);
			pstmt.setInt(2, x_user_id);
			pstmt.setTimestamp(4, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
			
			result = pstmt.executeUpdate();
        } catch (SQLException sqle) {
            System.out.println("SQLException : " + sqle);
        }
		finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		
		return result;
	}
	
	/*************************
	 * 참가자 확인
	 *************************/
	public static ResultSet GetRoomMember(String x_room_id) throws SQLException
	{
		ResultSet result = null;
		Statement stmt = null;
		
		try {
			if (conn == null)
				return null;
			
			String sql = "select * from dbo.money_room_member where x_room_id = " + x_room_id;
			stmt = conn.createStatement();
			result = stmt.executeQuery(sql);
			
        } catch (SQLException sqle) {
            System.out.println("SQLException : " + sqle);
        }
		finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		
		return result;
	}
	
	/*************************
	 * 참가자 확인
	 *************************/
	public static Boolean ExistRoomMember(String x_room_id) throws SQLException
	{
		ResultSet rs = null;
		Statement stmt = null;
		Boolean flag = false;
		
		try {
			if (conn == null)
				return null;
			
			String sql = "select x_room_id from money_room_member where x_room_id = " + x_room_id;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if (rs != null) {
				flag = true;
			}
        } catch (SQLException sqle) {
            System.out.println("SQLException : " + sqle);
        }
		finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		
		return flag;
	}
	
	/*************************
	 * 돈 뿌리기
	 * x_user_id 와 x_room_id 는 header 에다가 추가하도록 변경
	 *************************/
	public static String SpreadMoney(int x_user_id, String x_room_id, int money, int member_cnt) throws SQLException
	{
		PreparedStatement pstmt = null;
		Random rd = new Random();
		String tokens = "";
		
		try {
			if (conn == null)
				return "";
			
			int random_seqno = 0;
			while(random_seqno <= 100) {
				random_seqno = rd.nextInt(999); // token 은 3자리 까지만 나오도록 최대 수 999 까지만 나오도록 처리
			}
			
			// 돈뿌리기 master 테이블에 등록
			String sql = "insert into dbo.money_spread_mst(x_room_id, x_user_id, spread_money, reg_dt, token) values(?,?,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, x_room_id);
			pstmt.setInt(2, x_user_id);
			pstmt.setDouble(3, money);
			pstmt.setTimestamp(4, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
			pstmt.setString(5, Integer.toString(random_seqno));
			
			int update_rs = pstmt.executeUpdate();	
			
			// master 테이블에 등록이 정상적으로 되면 detail 테이블에 등록
			if (update_rs > 0) {
				// 현재 룸에 초대된 인원들 확인
				ResultSet rs = GetRoomMember(x_room_id);
				
				// 현재 룸의 인원이 뿌릴 인원보다 작은 경우 에러처리
				if (rs.getRow() < member_cnt) {
					return ""; 
				}
				
				// 상세로 돈 뿌리기
				int remain_money = money;
				while(rs.next())
				{
					// 자기 자신은 돈을 부여받지 못함
					if (rs.getInt("user_id") == x_user_id)
						continue;
				
					// 각 인원이 부여받는 뿌릴 금액 랜덤으로 추출 및 등록
					int each_money = 0;
					
					if (remain_money > 0) {
						each_money = rd.nextInt(money);
					}
					
					if (remain_money > 0 && (remain_money - each_money < 0)) {
						remain_money -= each_money;
						each_money = remain_money;
					} 
					
					sql = "insert into dbo.money_spread_dtl(x_room_id, each_spread, chk_flag, reg_dt) values(?,?,?,?)";
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, x_room_id);
					pstmt.setInt(2, each_money);
					pstmt.setString(3, "N");
					pstmt.setTimestamp(4, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
					pstmt.executeUpdate();
				}
				
				tokens = Integer.toString(random_seqno);
			}
        } catch (SQLException sqle) {
            System.out.println("SQLException : " + sqle);
            tokens = "";
        }
		finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		
		return tokens;
	}
	
	/*************************
	 * Token 확인
	 *************************/
	public static int CheckToken(int user_id, String x_room_id, String token, boolean is_user_id_check) throws SQLException
	{
		int result = 0;
		
		ResultSet rs = null;
		Statement stmt = null;
		
		try {
			if (conn == null)
				return -1;
			
			String sql = 
					" select chk_flag, x_user_id" + 
					" from dbo.money_spread_mst msm with(nolock)inner join dbo.money_spread_dtl msd with(nolock) on msm.x_room_id = msd.x_room_id" + 
					" where msm.x_room_id = " + x_room_id +
					" and msm.token = " + token;
			
			if (is_user_id_check == true)
				sql += " and msd.user_id = " + is_user_id_check;
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if (rs.next()) {
				if (is_user_id_check == true) {
					result = 1; // user_id 가 이미 부여받음
				}
				else if (rs.getInt("x_user_id") == user_id) {
					result = 2;
				}
				else {
					result = 3;
				}
			}
			else {
				result = -1;
			}
        } catch (SQLException sqle) {
            System.out.println("SQLException : " + sqle);
            result = -1;
        }
		finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		
		return result;
	}
	
	/*************************
	 * 뿌리기 돈 받기
	 * @throws SQLException 
	 *************************/
	public static int GetMoney(int x_user_id, String x_room_id, String token) throws SQLException
	{
		PreparedStatement pstmt = null;
		int result = 0;
		
		try {
			if (conn == null)
				return -1;
			
			String sql = 
					" update money_spread_dtl" + 
					" set user_id = x_user_id " + 
					" where seqno = (" + 
					"	select top 1 seq_no" + 
					"	from money_spread_dtl msd with(nolock)" + 
					"	inner join money_spread_mst msm with(nolock) on msd.x_room_id = msm.x_room_id" + 
					"	where msm.x_room_id = " + x_room_id + 
					"	and msd.token = " + token + 
					"	and msd.chk_flag = 'N'" + 
					"	and dateadd(mm, 10, msd.reg_dt) <= getdate() " + 
					" ) ";
			
			pstmt = conn.prepareStatement(sql);
			result = pstmt.executeUpdate();
        } catch (SQLException sqle) {
            System.out.println("SQLException : " + sqle);
            result = -1;
        }
		finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		
		return result;
	}
	
	/*************************
	 * 돈 뿌리기 현황 확인하기
	 *************************/
	public static SpreadMoney CheckSpreadMoneyInfo(String token, int x_user_id) throws SQLException
	{
		ResultSet rs = null;
		Statement stmt = null;
		SpreadMoney spread_money = null;
		
		try {
			if (conn == null)
				return spread_money;
			
			String sql = 
					" select msm.reg_dt, msm.spread_money, msd.each_spread, msd.user_id " + 
					" from dbo.money_spread_mst msm with(nolock) " + 
					"     inner join dbo.money_spread_dtl msd with(nolock) " + 
					" on msm.x_room_id = msd.x_room_id " + 
					" where msm.x_token =  " + token +
					" and msm.x_user_id = " + x_user_id +
					" and msd.chk_flag = 'Y' " +
					" and msm.reg_dt <= dateadd(dd, 7, msm.reg_dt) ";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if (rs != null) {
				spread_money = new SpreadMoney();
				
				
				int finish_money = 0;
				while(rs.next()) {
					spread_money.spread_time = rs.getDate("msm.reg_dt");
					spread_money.total_money = rs.getInt("msm.spread_money");
					
					SpreadMoneyDetail dtl = new SpreadMoneyDetail();
					dtl.money = rs.getInt("msd.each_spread");
					dtl.user_id = rs.getInt("msd.user_id");
					
					finish_money += dtl.money;
				}
				
				spread_money.finish_money = finish_money;
			}			
        } catch (SQLException sqle) {
            System.out.println("SQLException : " + sqle);
        }
		finally {
			if (stmt != null) {
				stmt.close();
			}
			
			if (rs != null) {
				rs.close();
			}
		}
		
		return spread_money;
	}
}


