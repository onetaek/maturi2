package com.maturi.service.member;

import com.maturi.dto.member.*;
import com.maturi.entity.member.Area;
import com.maturi.entity.member.Member;
import com.maturi.entity.member.MemberStatus;
import com.maturi.repository.member.MemberRepository;
import com.maturi.util.FileStore;
import com.maturi.util.PasswdEncry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Transactional
@Service
public class MemberService {

  final private MemberRepository memberRepository;
  final private ModelMapper modelMapper;
  final private FileStore fileStore;

  public Member join(MemberJoinDTO memberJoinDTO){

    // 비밀번호 암호화
    memberJoinDTO.setPasswd(getPasswdEncry(memberJoinDTO));

    // 닉네임 난수 생성
    memberJoinDTO.setNickName(getRandomNick());

    /* status 세팅 */
    memberJoinDTO.setStatus(MemberStatus.NORMAL);

    // dto를 entity로 변환
    Member mappedMember = modelMapper.map(memberJoinDTO,Member.class);

    // db에 저장
    Member savedMember = memberRepository.save(mappedMember);

    return savedMember;
  }

  public Member login(MemberLoginDTO memberLoginDTO) {
      String email = memberLoginDTO.getEmail();
      Member findMemberByEmail = memberRepository.findByEmail(email);
      String salt = findMemberByEmail.getSalt();

      String passwd = memberLoginDTO.getPasswd();
      /* 비밀번호 암호화 */
      PasswdEncry passwdEncry = new PasswdEncry();


      // 입력받은 비번 + 난수 => 암호화
      log.info("memberLogin passwd = {}", memberLoginDTO.getPasswd());
      String SHA256Pw =
              memberLoginDTO.getPasswd() != null?
                      passwdEncry.getEncry(memberLoginDTO.getPasswd(), salt) : null;
      memberLoginDTO.setPasswd(SHA256Pw);

      Member findMember = memberRepository.findByEmailAndPasswd(email,SHA256Pw);

      return findMember;
  }

  public Member getMemberById(Long id){
    return memberRepository.findById(id).orElse(null);
  }

  public boolean emailDuplCheck(String email){
    /* 이메일 중복 검사 */
    return memberRepository.findByEmail(email) != null;
  }


  public String getPasswdEncry(MemberJoinDTO memberJoinDTO) {
    /* 비밀번호 암호화 */
    PasswdEncry passwdEncry = new PasswdEncry();
    // 난수 생성 및 dto에 세팅
    String salt = passwdEncry.getSalt();
    memberJoinDTO.setSalt(salt);
    // 입력받은 비번 + 난수 => 암호화
    String SHA256Pw = passwdEncry.getEncry(memberJoinDTO.getPasswd(), salt);
    return SHA256Pw;
  }

  public String getRandomNick() {
    /* 닉네임 난수 생성 */
    boolean duplNick = true; // 닉네임 중복검사에 사용될 변수
    String nickName = null;
    while (duplNick){
      duplNick = false;
      nickName = "@user-" + UUID.randomUUID().toString().substring(0, 8);
      List<Member> memberList = memberRepository.findAll();
      for(Member member : memberList){
        if(nickName.equals(member.getNickName())){
          duplNick = true;
        }
      }
    }
    return nickName;
  }

  public void changeInsertArea(Long memberId, AreaInterDTO areaInterDTO) {
      Member findMember = memberRepository.findById(memberId).orElseThrow(() ->
              new IllegalArgumentException("맴버가 없습니다!"));
      findMember.changeInterArea(modelMapper.map(areaInterDTO, Area.class));
  }

  public AreaInterDTO selectInterLocation(Long memberId) {
    Member findMember = memberRepository.findById(memberId).orElseThrow(() ->
            new IllegalArgumentException("맴버가 없습니다!"));
    if(findMember.getArea() == null){
        return null;
    }else{
        return modelMapper.map(findMember.getArea(), AreaInterDTO.class);
    }
  }

  public void removeArea(Long memberId) {
      Member findMember = memberRepository.findById(memberId).orElseThrow(() ->
              new IllegalArgumentException("맴버가 없습니다!"));
      findMember.removeArea();
  }

  public MemberMyPageDTO myPageMemberInfo(Long memberId) {
    Member findMember = memberRepository.findById(memberId).orElseThrow(()->
            new IllegalArgumentException("맴버가 없습니다!"));

    return modelMapper.map(findMember, MemberMyPageDTO.class);
  }

  public boolean nickNameDuplCheck(String nickName) {
    Member findMember = memberRepository.findByNickName(nickName);

    if(findMember == null){
      return false;
    } else {
      return true;
    }
  }

  public void editMemberProfileInfo(Long memberId,
                                    MemberEditMyPageDTO memberEditMyPageDTO) throws IOException {
    Member findMember = memberRepository.findById(memberId).orElseThrow(()->
            new IllegalArgumentException("맴버가 없습니다!"));


    findMember.changeSimpleInfo(memberEditMyPageDTO.getNickName(),
                                memberEditMyPageDTO.getName(),
                                memberEditMyPageDTO.getProfile());

    // 이미지 바꿨을 경우 (파일 업로드 로직)
    if(!memberEditMyPageDTO.getCoverImg().isEmpty()) {
      String storeCoverImg = fileStore.storeFile(memberEditMyPageDTO.getCoverImg());
      findMember.changeCoverImg(storeCoverImg);
    }
    if(!memberEditMyPageDTO.getProfileImg().isEmpty()){
      String storeprofileImg = fileStore.storeFile(memberEditMyPageDTO.getProfileImg());
      findMember.changeProfileImg(storeprofileImg);
    }

    log.info("editMemberInfo = {}", findMember);

    memberRepository.save(findMember);
  }

  public boolean isBanMember(Long member_id){ // 밴된 멤버 -> true
    Member findMember = memberRepository.findByIdAndStatus(member_id, MemberStatus.BAN);

    return findMember != null;
  }

  public MemberDetailDTO memberDetailInfo(Long memberId) {
    Member findMember = memberRepository.findById(memberId).orElseThrow(()->
            new IllegalArgumentException("맴버가 없습니다!"));

    return modelMapper.map(findMember, MemberDetailDTO.class);
  }

  public boolean passwdCheck(Long memberId, String passwd) {
    Member findMember = memberRepository.findById(memberId).orElseThrow(()->
            new IllegalArgumentException("맴버가 없습니다!"));

    String salt = findMember.getSalt();

    String findPasswd = findMember.getPasswd();

    /* 비밀번호 암호화 */
    PasswdEncry passwdEncry = new PasswdEncry();

    // 입력받은 비번 + 난수 => 암호화
    String SHA256Pw = (passwd != null)?
                    passwdEncry.getEncry(passwd, salt) : null;

    return findPasswd.equals(SHA256Pw);
  }
}
