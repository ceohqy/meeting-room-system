package com.meeting.meeting_room_system;

import com.meeting.meeting_room_system.entity.ApprovalRecord;
import com.meeting.meeting_room_system.entity.Equipment;
import com.meeting.meeting_room_system.repository.ApprovalRecordRepository;
import com.meeting.meeting_room_system.entity.Reservation;
import com.meeting.meeting_room_system.entity.Room;
import com.meeting.meeting_room_system.entity.Tenant;
import com.meeting.meeting_room_system.entity.User;
import com.meeting.meeting_room_system.repository.EquipmentRepository;
import com.meeting.meeting_room_system.repository.ReservationRepository;
import com.meeting.meeting_room_system.repository.RoomRepository;
import com.meeting.meeting_room_system.repository.TenantRepository;
import com.meeting.meeting_room_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class MeetingRoomSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetingRoomSystemApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(UserRepository userRepository,
									  TenantRepository tenantRepository,
									  RoomRepository roomRepository,
									  ReservationRepository reservationRepository,
									  EquipmentRepository equipmentRepository,
									  ApprovalRecordRepository approvalRecordRepository,
									  PasswordEncoder passwordEncoder) {
		return args -> {

			// 创建租户
			if (tenantRepository.count() == 0) {
				Tenant ta = new Tenant(); ta.setName("A公司"); ta.setCode("companyA"); tenantRepository.save(ta);
				Tenant tb = new Tenant(); tb.setName("B公司"); tb.setCode("companyB"); tenantRepository.save(tb);
				System.out.println("租户初始化完成");
			}

			Tenant tenantA = tenantRepository.findAll().stream().filter(t -> "companyA".equals(t.getCode())).findFirst().orElse(null);
			Tenant tenantB = tenantRepository.findAll().stream().filter(t -> "companyB".equals(t.getCode())).findFirst().orElse(null);
			if (tenantA == null || tenantB == null) return;
			Long tidA = tenantA.getId();
			Long tidB = tenantB.getId();

			// 超级管理员
			if (userRepository.findByUsername("superadmin") == null) {
				User u = new User(); u.setUsername("superadmin"); u.setPassword(passwordEncoder.encode("123456"));
				u.setRealName("超级管理员"); u.setRole("SUPER_ADMIN"); u.setTenantId(null);
				userRepository.save(u);
				System.out.println("超级管理员创建成功：superadmin / 123456");
			}

			// A公司管理员 (liyiru)
			// B公司管理员 (fuyj)
			// 普通用户（前26个A公司，后26个B公司，liyiru为A管理员，fuyj为B管理员）
			String[][] members = {
				{"liyiru","李亦如"},{"aoaoeat","嗷嗷吃"},{"xiao","晓"},{"fzzz","4zzz"},
				{"camp","叫我camp"},{"badriri77","badriri77"},{"liushao","刘少"},{"fairyroll","一只仙女卷"},
				{"promisezi","promise诺紫"},{"druu","Druu"},{"xiaowei","小违"},{"soowoo","sooWooyy_"},
				{"qinsiyang","覃思阳"},{"guolichen","果粒橙"},{"zhouzhou","我是粥粥"},{"qingaogao","亲爱的高"},
				{"nixizi","逆十字"},{"sleeptoast","失眠吐司"},{"daidaitu","带带土"},{"diandian","是点点呀"},
				{"xiguaye","西瓜椰合鸟"},{"hujiaqian","呼叫钱钱"},{"bingbing","一只饼饼"},{"meizifei","莓子非非"},
				{"xiaoning","晓宁睡着了"},{"rourou","肉肉不肉"},{"beishangba","悲伤霸王牛"},{"beishangy","悲伤玉米汤"},
				{"fuxiang","浮巷与猫"},{"huamiao","花喵er"},{"xiaotuj","小兔叽"},{"lichee","一颗荔叽"},
				{"chenzimi","陈子蜜"},{"fansi","烦死啦"},{"xieplus","蟹加一"},{"sweetcheng","sweet晨"},
				{"yuyuan","芋圆y"},{"gongzhuxuan","公主小暄"},{"qiuqiu","秋秋~"},{"unnu","unnu"},
				{"sunyutong","孙予桐"},{"shenxueqian","申雪倩"},{"ziyouji","孜由基"},{"luosifan","爱次螺蛳粉~"},
				{"jiashenme","叫什么好呢"},{"aurora","Aurora"},{"fuyj","Fuyj"},{"xiaoyu","小雨"},
				{"paidaxing","派大星"},{"zhounan","周南儿"},{"lengchi","冷炽"},{"xiaoying","小莹"}
			};
			for (int i = 0; i < members.length; i++) {
				if (userRepository.findByUsername(members[i][0]) == null) {
					User u = new User(); u.setUsername(members[i][0]); u.setPassword(passwordEncoder.encode("123456"));
					u.setRealName(members[i][1]);
					// liyiru(0) 是A公司管理员，fuyj(46) 是B公司管理员
					if (i == 0) { u.setRole("ADMIN"); u.setTenantId(tidA); }
					else if (i == 46) { u.setRole("ADMIN"); u.setTenantId(tidB); }
					else { u.setRole("USER"); u.setTenantId(i < 26 ? tidA : tidB); }
					userRepository.save(u);
				}
			}

			// A公司会议室
			if (roomRepository.countByTenantId(tidA) == 0) {
				String[][] roomsA = {
					{"A-大会议室","30","A栋3楼","投影仪,白板,视频会议"},
					{"A-小会议室","8","A栋2楼","白板,电视"},
					{"A-洽谈室","4","A栋1楼","白板"},
					{"A-培训室","50","A栋4楼","投影仪,麦克风,白板"},
					{"A-创新室","12","A栋2楼","电视,白板,视频会议"},
					{"A-头脑风暴室","10","A栋1楼","白板,电视"}
				};
				for (String[] r : roomsA) {
					Room room = new Room(); room.setRoomName(r[0]); room.setCapacity(Integer.parseInt(r[1]));
					room.setLocation(r[2]); room.setEquipment(r[3]); room.setStatus("1"); room.setTenantId(tidA);
					roomRepository.save(room);
				}
			}

			// B公司会议室
			if (roomRepository.countByTenantId(tidB) == 0) {
				String[][] roomsB = {
					{"B-大会议室","40","B栋5楼","投影仪,白板,视频会议"},
					{"B-小会议室","6","B栋3楼","白板,电视"},
					{"B-洽谈室","4","B栋1楼","白板"},
					{"B-培训室","60","B栋6楼","投影仪,麦克风,白板"},
					{"B-路演室","20","B栋2楼","投影仪,麦克风"},
					{"B-讨论室","8","B栋3楼","白板,电视"}
				};
				for (String[] r : roomsB) {
					Room room = new Room(); room.setRoomName(r[0]); room.setCapacity(Integer.parseInt(r[1]));
					room.setLocation(r[2]); room.setEquipment(r[3]); room.setStatus("1"); room.setTenantId(tidB);
					roomRepository.save(room);
				}
			}

			// A公司配件
			if (equipmentRepository.countByTenantId(tidA) == 0) {
				List<Room> roomsA = roomRepository.findAll().stream().filter(r -> tidA.equals(r.getTenantId())).toList();
				String[][] equipsA = {
					{"白板A-1","白板"},{"白板A-2","白板"},{"白板A-3","白板"},
					{"投影仪A-1","投影仪"},{"投影仪A-2","投影仪"},
					{"电视A-1","电视"},{"电视A-2","电视"},
					{"麦克风A-1","麦克风"},{"麦克风A-2","麦克风"},
					{"视频会议设备A-1","视频会议设备"},{"视频会议设备A-2","视频会议设备"},
					{"音响A-1","音响"},{"空调遥控器A-1","空调遥控器"},{"空调遥控器A-2","空调遥控器"}
				};
				for (int i = 0; i < equipsA.length; i++) {
					Equipment e = new Equipment(); e.setName(equipsA[i][0]); e.setType(equipsA[i][1]);
					e.setRoomId(roomsA.get(i % roomsA.size()).getId());
					e.setSerialNo(String.format("SN-A-%03d", i + 1)); e.setStatus(1); e.setTenantId(tidA);
					equipmentRepository.save(e);
				}
			}

			// B公司配件
			if (equipmentRepository.countByTenantId(tidB) == 0) {
				List<Room> roomsB = roomRepository.findAll().stream().filter(r -> tidB.equals(r.getTenantId())).toList();
				String[][] equipsB = {
					{"白板B-1","白板"},{"白板B-2","白板"},{"白板B-3","白板"},
					{"投影仪B-1","投影仪"},{"投影仪B-2","投影仪"},
					{"电视B-1","电视"},{"电视B-2","电视"},
					{"麦克风B-1","麦克风"},{"麦克风B-2","麦克风"},{"麦克风B-3","麦克风"},
					{"视频会议设备B-1","视频会议设备"},
					{"音响B-1","音响"},{"音响B-2","音响"},
					{"空调遥控器B-1","空调遥控器"}
				};
				for (int i = 0; i < equipsB.length; i++) {
					Equipment e = new Equipment(); e.setName(equipsB[i][0]); e.setType(equipsB[i][1]);
					e.setRoomId(roomsB.get(i % roomsB.size()).getId());
					e.setSerialNo(String.format("SN-B-%03d", i + 1)); e.setStatus(1); e.setTenantId(tidB);
					equipmentRepository.save(e);
				}
			}

			// 历史预约数据
			String[] purposes = {"技术方案讨论","月度总结会","需求评审","商务洽谈","团队周会","产品规划","培训分享","项目复盘","客户对接","头脑风暴","绩效面谈","新人入职培训"};
			// 统一使用业务枚举字符串，避免出现 "0/1/2" 这类种子数据导致统计/审批不一致
			String[] statuses = {"APPROVED","APPROVED","APPROVED","PENDING","PENDING","REJECTED"};
			int[][] slots = {{9,10},{10,11},{11,12},{14,15},{15,16},{16,17},{9,11},{14,16}};

			// A公司历史预约
			if (reservationRepository.countByTenantId(tidA) < 10) {
				List<Room> rooms = roomRepository.findAll().stream().filter(r -> tidA.equals(r.getTenantId())).toList();
				List<User> users = userRepository.findByTenantIdAndRole(tidA, "USER");
				if (!rooms.isEmpty() && !users.isEmpty()) {
					int idx = 0;
					for (int daysAgo = 29; daysAgo >= 0; daysAgo--) {
						LocalDate date = LocalDate.now().minusDays(daysAgo);
						int count = 2 + (idx % 4);
						for (int i = 0; i < count; i++) {
							int[] slot = slots[idx % slots.length];
							Reservation res = new Reservation();
							res.setUserId(users.get(idx % users.size()).getId());
							res.setRoomId(rooms.get(idx % rooms.size()).getId());
							res.setReservationDate(date);
							res.setStartTime(LocalTime.of(slot[0], 0));
							res.setEndTime(LocalTime.of(slot[1], 0));
							res.setApplicant(users.get(idx % users.size()).getRealName());
							res.setAttendees(3 + (idx % 8));
							res.setPurpose(purposes[idx % purposes.length]);
							res.setStatus(statuses[idx % statuses.length]);
							res.setTenantId(tidA);
							res.setCreateTime(LocalDateTime.of(date, LocalTime.of(8, 30)).minusDays(1));
							Reservation savedRes = reservationRepository.save(res);
							// 插入对应的审批记录
							ApprovalRecord ar = new ApprovalRecord();
							ar.setReservationId(savedRes.getId());
							ar.setApprovalResult(statuses[idx % statuses.length]);
							if (!"PENDING".equals(statuses[idx % statuses.length])) ar.setApprovalTime(LocalDateTime.of(date, LocalTime.of(9, 0)));
							approvalRecordRepository.save(ar);
							idx++;
						}
					}
					System.out.println("A公司历史预约初始化完成，共 " + idx + " 条");
				}
			}

			// B公司历史预约
			if (reservationRepository.countByTenantId(tidB) < 10) {
				List<Room> rooms = roomRepository.findAll().stream().filter(r -> tidB.equals(r.getTenantId())).toList();
				List<User> users = userRepository.findByTenantIdAndRole(tidB, "USER");
				if (!rooms.isEmpty() && !users.isEmpty()) {
					int idx = 0;
					for (int daysAgo = 29; daysAgo >= 0; daysAgo--) {
						LocalDate date = LocalDate.now().minusDays(daysAgo);
						int count = 2 + (idx % 3);
						for (int i = 0; i < count; i++) {
							int[] slot = slots[idx % slots.length];
							Reservation res = new Reservation();
							res.setUserId(users.get(idx % users.size()).getId());
							res.setRoomId(rooms.get(idx % rooms.size()).getId());
							res.setReservationDate(date);
							res.setStartTime(LocalTime.of(slot[0], 0));
							res.setEndTime(LocalTime.of(slot[1], 0));
							res.setApplicant(users.get(idx % users.size()).getRealName());
							res.setAttendees(2 + (idx % 6));
							res.setPurpose(purposes[idx % purposes.length]);
							res.setStatus(statuses[idx % statuses.length]);
							res.setTenantId(tidB);
							res.setCreateTime(LocalDateTime.of(date, LocalTime.of(9, 0)).minusDays(1));
							Reservation savedRes = reservationRepository.save(res);
							ApprovalRecord ar = new ApprovalRecord();
							ar.setReservationId(savedRes.getId());
							ar.setApprovalResult(statuses[idx % statuses.length]);
							if (!"PENDING".equals(statuses[idx % statuses.length])) ar.setApprovalTime(LocalDateTime.of(date, LocalTime.of(10, 0)));
							approvalRecordRepository.save(ar);
							idx++;
						}
					}
					System.out.println("B公司历史预约初始化完成，共 " + idx + " 条");
				}
			}
		};
	}
}
