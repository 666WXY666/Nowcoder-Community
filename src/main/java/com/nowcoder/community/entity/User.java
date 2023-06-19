package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    // 0-普通用户; 1-超级管理员; 2-版主;
    private int type;
    // 0-未激活; 1-已激活;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;
}
