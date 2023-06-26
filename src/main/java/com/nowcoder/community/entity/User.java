package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class User {
    private int id;// 用户id
    private String username;// 用户名
    private String password;// 密码
    private String salt;// 密码加密的随机字符串
    private String email;// 邮箱
    private int type;// 0-普通用户; 1-超级管理员; 2-版主;
    private int status;// 0-未激活; 1-已激活;
    private String activationCode;// 激活码
    private String headerUrl;// 头像地址
    private Date createTime;// 创建时间
}
