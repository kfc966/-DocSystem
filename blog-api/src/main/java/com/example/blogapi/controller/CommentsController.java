package com.example.blogapi.controller;

import com.example.blogapi.service.CommentService;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.CommentParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("comments")
public class CommentsController {
    @Autowired
    private CommentService commentService;
    @GetMapping ("/article/{id}")
    public Result comments(@PathVariable("id") Long id)
    {
        return  commentService.commentsByArticleId(id);
    }

    @PostMapping("create/change")
    public Result comment(@RequestBody CommentParam commentParam){
        return commentService.comment(commentParam);
    }
}
