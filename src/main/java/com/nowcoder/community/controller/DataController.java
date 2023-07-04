package com.nowcoder.community.controller;

import com.nowcoder.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    // 统计页面-GET/POST
    // 访问路径：http://localhost:8080/community/data
    // 因为会有POST请求forward到这个路径，所以要同时支持GET和POST请求
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "site/admin/data";
    }

    // 统计网站UV-POST
    // 访问路径：http://localhost:8080/community/data/uv
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long uv = dataService.calculateUV(start, end);
        if (uv == -1) {
            model.addAttribute("uvResult", "日期格式错误");
        } else {
            model.addAttribute("uvResult", uv);
        }
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);
        // 使用forward定向到/data，继续处理
        // 而不是重定向到/data，因为重定向会丢失model中的数据
        return "forward:/data";
    }

    // 统计网站DAU-GET
    // 访问路径：http://localhost:8080/community/data/dau
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        long dau = dataService.calculateDAU(start, end);
        if (dau == -1) {
            model.addAttribute("dauResult", "日期格式错误");
        } else {
            model.addAttribute("dauResult", dau);
        }
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        return "forward:/data";
    }
}
