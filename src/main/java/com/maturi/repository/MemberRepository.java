package com.maturi.repository;

import com.maturi.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,Long> {

    public Member findByEmail(String email);
    public Member findByEmailAndPasswd(String email,String passwd);
}
