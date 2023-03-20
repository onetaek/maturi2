package com.maturi.controller.article;

import com.maturi.dto.article.ArticleDTO;
import com.maturi.dto.article.RestaurantDTO;
import com.maturi.service.article.ArticleService;
import com.maturi.util.argumentresolver.Login;
import com.maturi.util.constfield.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
@Controller
public class ArticleController {

    final private ArticleService articleService;

    @GetMapping("/articles")
    public String articlesPage(@Login Long memberId,
                               Model model){
        //검색조건 추가
        //1. 관심지역을 선택했는지 확인 -> 있다면? -> 관심지역에 값추가
        //                            없다면? -> 전체로 선택
        //2.


        log.info("findMember = {}",articleService.memberInfo(memberId));
        model.addAttribute("member", articleService.memberInfo(memberId));
        return "/article/welcome";
    }

    @GetMapping("/article")
    public String writeForm(@Login Long memberId,
                            Model model,
                            @ModelAttribute(name = "restaurant") RestaurantDTO restaurantDTO){
        log.info("restaurantDTO={}",restaurantDTO);
        model.addAttribute("member", articleService.memberInfo(memberId));
        return "/article/write";
    }

    @PostMapping("/article")
    public String write(@Login Long memberId,ArticleDTO articleDTO, Model model) throws IOException {
        log.info("articleDTO={}",articleDTO);
        Long articleId = articleService.write(memberId, articleDTO);
        log.info("articleId={}",articleId);
        model.addAttribute("articleId", articleId);
        return "redirect:/article/"+articleId;
    }

    @GetMapping("/article/{articleId}")
    public String articlePage(@Login Long memberId,
                              @PathVariable Long articleId,
                              Model model){
        log.info("articleId={}",articleId);
        model.addAttribute("article", articleService.articleInfo(articleId));
        model.addAttribute("restaurant", articleService.restaurantByArticle(articleId));
        model.addAttribute("member", articleService.memberInfo(memberId));
        model.addAttribute("isLikedArticle", articleService.isLikedArticle(articleId, memberId));
        return "/article/article";
    }


}
