package com.example.demo;

import com.example.demo.config.domain.entity.MemberLogin;
import com.example.demo.config.repository.MemberLoginRepository;
import com.example.demo.members.domain.entity.Member;
import com.example.demo.members.domain.entity.QMember;
import com.example.demo.members.domain.request.LoginRequest;
import com.example.demo.members.domain.response.LoginResponse;
import com.example.demo.members.repository.MemberRepository;
import com.example.demo.members.service.MemberService;
import com.example.demo.todos.domain.dto.TodoCondition;
import com.example.demo.todos.domain.entity.QTodo;
import com.example.demo.todos.domain.entity.Todo;
import com.example.demo.todos.repository.TodoRepository;
import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
class DemoApplicationTests {

	@Test @Transactional
	void contextLoads() {
		memberLoginRepository.findFirstByMemberIdAndEndAtAfterOrderByEndAtDesc(1l, LocalDateTime.now());
	}
	@Test
	void test() {
		QMember member = new QMember("member"); // 변수  name 을 한번 써줌.
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager); // 가져온 엔티티메니져를 넣어준다.
		// select m from member m where name = "name" 이걸 가져와보려는 것.
		String name = null;
		BooleanExpression nameEq = name != null ? member.name.eq(name) : null; // where 가 null이 아니면 하고 null이면 나오지 않도록.
		member.name.like("%"+name+"%");
		Integer age = 20;
		BooleanExpression ageLoe = age == null ? null : member.age.loe(20); // 위에거랑 같다. null이 앞에 하는게 편해서 이렇게 쓴것.
		JPAQuery<Member> from = queryFactory.query()
				.select(member) //멤버를 찾을건데
				.from(member)  // 전체를 찾을거다?. 여기까지가 쿼리.
				.where(nameEq, ageLoe)
				;
		List<Member> fetch = from.fetch();  // fetch를 해줘야 꺼낼 수 있다.
		System.out.println();
	}

	@Test
	void test2() { // 내가 해본것
		// select member from member where age <= 10 and age > 5 and name != "na"
		QMember member1 = new QMember("member");
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		String name = "na";
		JPAQuery<Member> from = queryFactory.query()
				.select(member1)
				.from(member1)
				.where(member1.age.loe(10).and(member1.age.gt(5).and(member1.name.notLike("%"+name+"%"))));

		List<Member> fetch = from.fetch();  // fetch를 해줘야 꺼낼 수 있다.
		System.out.println();
	}

	@Test
	void test3() { // 정답
		// select member from member where age <= 10 and age > 5 and name != "na"
		QMember qMember = QMember.member; // 이렇게도 쓸 수 있다~
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		JPAQuery<Member> q = queryFactory.selectFrom(qMember) //from 까지 한번에 한것.
				.innerJoin(qMember.todos)
				.fetchJoin() 		// fetchJoin까지 해야 n+1을 방지할 수 있다.
				.where(qMember.age.loe(10) 		// loe : <=
						, qMember.age.gt(5) 		// gt : >
						, qMember.name.eq("na").not())
				.offset(0) // 페이지 설정
				.limit(20)
				.orderBy(qMember.age.desc()); // 나이 연장자부터 가지고 와라.
		queryFactory.select(qMember.count()).from(qMember); // 이렇게 전체 페이지도 만들어줘야 한다.

		List<Member> fetch = q.fetch();
		for (Member m : fetch) {
			System.out.println(m.getTodos().size());
		}
		System.out.println();
	}

	@Test
	void test5() { // concat을 써보자.
		QMember qMember = QMember.member; // 이렇게도 쓸 수 있다~
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		List<String> vv = queryFactory
				.select(qMember.name
						.concat("님")
						.concat(" " + qMember.age.stringValue()))
				.from(qMember).fetch();
		System.out.println(vv);

	}

	@Test
	void test6() {
		QMember qMember = QMember.member;
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		Member member1 = entityManager.find(Member.class, this.member.getId());//getId로 어떤놈 찾아와줘~ 하는것.
		Member member2 = memberRepository.findById(this.member.getId()).get();
		// select member from member where id = ? 이거 해보기
		Member member3 = queryFactory
				.selectFrom(qMember)
				.where(qMember.id.eq(this.member.getId()))
				.fetchOne();
		Assertions.assertEquals(member1, member2); // 세개의 결과는 다 같다.
		Assertions.assertEquals(member1, member3);
		Assertions.assertEquals(member2, member3);
		Assertions.assertNotEquals(member1, this.member);
	}

	@Test
	void tes7() {
		// 작성자 이름이 name이고,
		// 좋아요를 10개 이상 받았고, 내용에 t가 들어간 게시물
		// select * from todo
		// where todo.member.name = "name"
		// and todo.likeCount >= ?
		// and todo.content like %t%

/*		QTodo qTodo  = QTodo.todo;
				JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		List<Todo> fetch = queryFactory.selectFrom(qTodo) // // select * from todo
				.where(qTodo.member.name.eq("name"),
						qTodo.likeCount.goe(10),
						qTodo.content.contains("t")) // contains 이게 % % 이거 표시다.
				.fetch();
		// rewult 30 이 나와야한다.
		Assertions.assertEquals(fetch.size(), 30);
	}*/

// =========멤버로 바꿔서 해보기 ==========
		//select * from members m
		//left join todos t on t.member_id = m.id
		//where m.name = "name"
		// and t.like_count >= 10
		//and t.content like &t&
		QMember qMember = QMember.member;
		QTodo qTodo = QTodo.todo;
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		List<Member> fetch = queryFactory
				.select(qMember)
				.from(qMember)   // qMember에서 todo들을 꺼낼거다.
				.leftJoin(qMember.todos, qTodo)
				.fetchJoin()
				.where(qMember.name.eq("name"),
						qTodo.content.contains("t"),
						qTodo.likeCount.goe(10))
				.fetch();

		//.fetchJoin() 이 있을 때의 쿼리
		//select member, todos from member  -> .fetchJoin() 이 없을 때의 쿼리 select member from member만 들어감.
		//left join member.todos
		//where m.name = name and t.content like %t% and tt.likeCount >= 10

		// rewult 30 이 나와야한다.
		Assertions.assertEquals(fetch.size(), 30);

	}

	@Test
	void tes9() {
		QMember qMember = QMember.member;
//		select case when m.age >= 10 and m.age < 20 then '10대'
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		queryFactory.select(
				qMember.age.sum(),		//집계함수도 있다. 왠만해서는 쓰지말자.
				qMember.age.avg())
				.from(qMember);
		queryFactory
				.select(qMember)
				.from(qMember)
				.where(qMember.id.eq(
						JPAExpressions.select(QTodo.todo.member.id)
								.from(QTodo.todo)
								.where(QTodo.todo.content.contains("t"))// t들어간 양반의 글을 가져오자. 이렇게하면 안될거다. 이건 그냥 문장이니까.
				));

		//10대 20대 이런식 하는거  when m.age >= 10 and m.age < 20 then '10대' 여기 부분
		queryFactory.select(
				new CaseBuilder()
						.when(qMember.age.between(10, 20))
						.then("10대")
						.otherwise("노인")
		).from(qMember).fetchOne();
		System.out.println();
	}

		QMember qMember = QMember.member;
		QTodo qTodo = QTodo.todo;
	@Test
	void tes10() {
		// init data - 내가 지금 필요한 데이터 (given)
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		TodoCondition condition = TodoCondition.builder().build();
		PageRequest request = PageRequest.of(0, 20);
		JPAQuery<Todo> limit = queryFactory
				.select(qTodo)
				.from(qTodo)
				.leftJoin(qTodo.member, qMember)
				.fetchJoin()
				.where(
						contentContains(condition.getContent()),
						titleEq(condition.getTitle())
				)
				.offset(request.getPageNumber())
				.limit(request.getPageSize());
		List<Todo> fetch = limit.fetch();
		Assertions.assertEquals(fetch.size(),20);
	}

	private BooleanExpression contentContains(String content) {
		return content == null
				? null
				: qTodo.content.contains(content);
	}

	private BooleanExpression titleEq(String title) {
		return title == null
				? null
				: qTodo.title.eq(title);
	}





	@Autowired
	MemberRepository memberRepository;
	@Autowired
	TodoRepository todoRepository;
	@Autowired
	MemberLoginRepository memberLoginRepository;
	String email = "1111";
	String password = "1234";
	Member member;
	@Autowired
	EntityManager entityManager;
	@Autowired
	MemberService memberService;
	String token;
	Todo todo;
	@BeforeEach
	void init(){
		Member member =
				new Member(null, email, password
						, "name", 10, new ArrayList<>(), null);

		this.member = memberRepository.save(member);
		this.todo = todoRepository.save(
				new Todo(null, "a", "a"
						, false, 0, member)
		);
		for (int i = 0; i < 40; i++) {
			todoRepository.save(
					new Todo(null, "t" + i,"t" + i
							, false, i, member)
			);
		}

		MemberLogin entity = new MemberLogin(this.member, LocalDateTime.now());
		memberLoginRepository.save(entity);
		entityManager.flush();
		entityManager.clear();
		LoginResponse login = memberService.login(new LoginRequest(email, password));
		token = login.token();

	}
	@AfterEach
	void clean(){
		todoRepository.deleteAll();
		memberLoginRepository.deleteAll();
		memberRepository.deleteAll();
	}
}

