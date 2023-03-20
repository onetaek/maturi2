package com.maturi.controller.member;

import com.maturi.dto.member.MemberJoinDTO;
import com.maturi.dto.member.MemberLoginDTO;
import com.maturi.entity.member.Member;
import com.maturi.service.member.EmailService;
import com.maturi.service.member.MemberService;
import com.maturi.util.validator.MemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.maturi.util.constfield.SessionConst.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/member")
public class MemberController {
  final private MemberService memberService;
  final private MemberValidator memberValidator;
  final private EmailService emailService;

  @InitBinder("memberJoinDTO")
  public void init(WebDataBinder dataBinder) {
    dataBinder.addValidators(memberValidator);
  }
  @GetMapping("/join")
  public String getJoin(Model model){
    model.addAttribute("member",new MemberJoinDTO());
    return "/member/join";
  }

  @PostMapping("/join")
  public String join(
          @Validated @ModelAttribute(name = "member") MemberJoinDTO memberJoinDTO,
          BindingResult bindingResult){
    log.info("memberJoinRequest = {}", memberJoinDTO.toString());
    memberValidator.validate(memberJoinDTO, bindingResult);

    //검증에 실패하면 다시 입력 폼으로
    if (bindingResult.hasErrors()) {
      log.info("errors={} ", bindingResult);
      return "/member/join";
    }

    memberService.join(memberJoinDTO);
    return "redirect:/member/login";
  }

  @GetMapping("/login")
  public String loginPage(@RequestParam(defaultValue = "/") String redirectURL,Model model){
    model.addAttribute("redirectURL",redirectURL);
    model.addAttribute("member",new MemberLoginDTO());
    return "/member/login";
  }

  @PostMapping("/login")
  public String login(
          @Validated @ModelAttribute(name = "member") MemberLoginDTO memberLoginDTO,
          BindingResult bindingResult,
          @RequestParam(defaultValue = "/") String redirectURL,
          HttpServletRequest request){

    //검증에 실패하면 다시 입력 폼으로
    if (bindingResult.hasErrors()) {
      log.info("errors={} ", bindingResult);
      return "/member/login";
    }

    //정상 로직
    Member findMember = memberService.login(memberLoginDTO);
    HttpSession session = request.getSession();
    session.setAttribute(MEMBER_ID,findMember.getId());

    log.info("redirectURL = {}",redirectURL);
    return "redirect:" + redirectURL;
  }

  @PostMapping("/logout")
  public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Member LoginMember = memberService.getMemberById((Long) request.getSession().getAttribute(MEMBER_ID));
    request.getSession().invalidate();

    return "redirect:/member/login";
  }


}
