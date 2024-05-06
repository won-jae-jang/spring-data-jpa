package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    void testMember() {

        //given
        Member member = new Member("memberA");

        //when
        Member savedMember = memberRepository.save(member);

        //then
        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }


    @Test
    void basicCRUD() {

        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        //when
        memberRepository.save(member1);
        memberRepository.save(member2);

        //then

        //단건조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    void findByUsernameAndAgeGreaterThan() {

        //given
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("aaa", 20);

        //when
        memberRepository.save(m1);
        memberRepository.save(m2);

        //then
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("aaa", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("aaa");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void findUser(){

        //given
        Member member = new Member("memberA", 10);

        //when
        memberRepository.save(member);

        //then
        List<Member> findMember = memberRepository.findUser("memberA", 10);
        assertThat(findMember.size()).isEqualTo(1);
    }

    @Test
    void findByUsernameAndAge(){

        //given
        Member member = new Member("memberA", 10);

        //when
        memberRepository.save(member);

        //then
        List<Member> findMember = memberRepository.findByUsernameAndAge("memberA", 10);
        assertThat(findMember.size()).isEqualTo(1);
    }


    @Test
    void findUsernameList (){

        //given
        Member member = new Member("memberA");

        //when
        memberRepository.save(member);

        //then
        List<String> result = memberRepository.findUsernameList();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void findMemberDto (){

        //given
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("memberA", 10, team);
        memberRepository.save(member);

        //when
        List<MemberDto> memberDto = memberRepository.findMemberDto();

        //then
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    void findByNames() {

        //given
        Member m1 = new Member("member1", 20);
        Member m2 = new Member("member2", 30);

        //when
        memberRepository.save(m1);
        memberRepository.save(m2);

        //then
        List<Member> result = memberRepository.findByNames(Arrays.asList("member1", "member2"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    void returnType() {

        //given
        Member member1 = new Member("aaa", 10);
        Member member2 = new Member("bbb", 20);

        //when
        memberRepository.save(member1);
        memberRepository.save(member2);

        //then
//        List<Member> listResult = memberRepository.findListByUsername("aaa");
//        System.out.println("listResult = " + listResult.size());

//        Member findMember = memberRepository.findMemberByUsername("aaa");
        Optional<Member> findMember = memberRepository.findOptionalByUsername("aaa");
        System.out.println("findMember = " + findMember.get());

    }

    @Test
    void page() {

        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(10, pageRequest);
        Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        //then
        List<Member> content = page.getContent();
        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void bulkUpdate() {

        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20); //벌크 연산
        Member member5 = memberRepository.findMemberByUsername("member5");
        System.out.println("member5 = " + member5);

        //then
        assertThat(resultCount).isEqualTo(3);
    }


    @Test
    void findMemberLazy() {

        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member1", 20, teamB));

        em.flush();
        em.clear();

        //when
        List<Member> members = memberRepository.findByUsername("member1");

        //then
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    void queryHint() {

        //given
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2"); //update 쿼리 안나감

        em.flush();
    }

    @Test
    void customRepo() {

        //given
        memberRepository.save(new Member("member1", 10));

        //when
        memberRepository.findMemberCustom();
    }
}