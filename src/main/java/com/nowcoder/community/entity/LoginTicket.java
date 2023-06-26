package com.nowcoder.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class LoginTicket {
    private int id;// 主键
    private int userId;// 用户id
    private String ticket;// 登录凭证
    private int status; // 0:有效; 1:无效
    private Date expired; // 过期时间
}
