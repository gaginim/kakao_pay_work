
개요
======================
## 프로젝트 명
  - SpreadMoney
  
## 사용언어 
  - Java(Spring boot), Mssql
  
## 사용 Tool	
  - Eclipse


파일구조
======================
## Controller
	- MoneySpraedController.java : 총 4개의 API 사용
		1. Room 입장 API
			1.1 방에 입장한 사용자들은 money_room_member 에 insert
			
		2. 뿌리기 API
			2.1 방의 존재유무 체크
			2.2 token 생성 뒤 money_spread_mst 테이블에 등록일시
			2.3 방 정보 조회해서 방 인원 체크
			2.4 각 방의 인원들에게 랜덤으로 부여받은 돈 안에서 나누어 money_spread_dtl 에 insert
			
		3. 받기 API
			3.1 뿌리기 방이 자신이 만든건지 확인
			3.2 받기 API 를 호출한 사용자가 이미 부여받았는지 확인
			3.3 받아야 할 대상 사용자인 경우 money_spread_dtl 테이블에 상태와 함께 update
			
			
		4. 조회 API
	
## Model
	- MoneySpraed.java 
		: 조회 API 를 위한 model 
		
	- MoneySpraedDetail.java 
		: 조회 API 를 위한 model
		: MoneySpraed.java model 안에서 detail 한 정보를 위해 생성
		
	
## DB Connector
	- jdbc.java 
		: Mssql 연결
		: DB 작업 시 jdbc.java 파일에서 처리


DB 구조
======================
- 같은 방의 맴버 정보
~~~~
create table money_room_member
(
	x_room_id varchar(10) not null, -- room number
	user_id int not null, -- user id
	reg_dt null -- 등록일시
)
go
~~~~

- 뿌리기 master 테이블
~~~~
create table money_spread_mst 
(
	token varchar(3) not null, -- 사용자에게 전달할 token 정보
	x_room_id varchar(10) not null,	-- room number
	x_user_id int not null, -- user id
	spread_money money not null, -- 전체 뿌릴 돈
	reg_dt datetime null, -- 등록일시
	chg_dt datetime null -- 수정일시
)
go
~~~~

- 뿌리기 상세 테이블
~~~~
create table money_spread_dtl 
(
	seqno int identity(1,1), -- 일련번호
	x_room_id varchar(10) not null -- room number
	user_id varchar(10) not null, -- user id
	each_spread money null, -- 각 사용자별 뿌릴 돈
	chk_flag char(1) not null, -- y or n 로만 표현
	reg_dt datetime null, -- 등록일시
	chg_dt datetime null -- 수정일시
)
go
~~~~ 
