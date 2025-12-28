package com.ahu.helloahu;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 【MP 重构点】导入条件构造器
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference; // 选这个 Jackson 的，别选 MyBatis 的
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.concurrent.TimeUnit; // 必须导入这个，解决 TimeUnit 报错
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.redis.connection.util.DecodeUtils.convertToList;

@Controller
public class AlumniController {

    @Autowired
    private AlumniMapper alumniMapper; // 【MP 重构点】注入 Mapper 而非 Repository

    @Autowired
    private StringRedisTemplate redisTemplate;
// 记得导入这个分页类

    @GetMapping("/alumni-wall")
    public String showWall(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo, Model model) {
        // 1. Redis 处理总数逻辑 (维持现状，减少数据库压力)
        String cacheKey = "alumni:total";
        String totalStr = redisTemplate.opsForValue().get(cacheKey);
        long total;
        if (totalStr == null) {
            total = alumniMapper.selectCount(null); //
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(total), 5, TimeUnit.MINUTES);
        } else {
            total = Long.parseLong(totalStr);
        }
        model.addAttribute("total", total);

        // 2. 【核心改动】使用 selectPage 获取当前页的校友名单
        // 创建 Page 对象：(当前页码, 每页显示 8 条)
        Page<Alumni> alumniPage = new Page<>(pageNo, 8);

        // 执行查询：数据会自动填充进 alumniPage 对象中
        alumniMapper.selectPage(alumniPage, null);

        // 3. 将名单和分页信息传给前端
        model.addAttribute("alumniList", alumniPage.getRecords());

// 【关键修改点】名字要和 HTML 里的 ${current} 和 ${pages} 对应
        model.addAttribute("current", pageNo);           // 对应 HTML 里的 ${current}
        model.addAttribute("pages", alumniPage.getPages()); // 对应 HTML 里的 ${pages}
        model.addAttribute("total", total);

        return "alumni";
    }


    @PostMapping("/add-alumni")
    public String addAlumni(Alumni alumni, @RequestParam("imageFile") MultipartFile file, HttpSession session, RedirectAttributes ra) throws Exception {

        if (session.getAttribute("isAdmin") == null) {
            ra.addFlashAttribute("error", "权限不足！请先登录管理员账号。");
            return "redirect:/login";
        }
        if (!file.isEmpty()) {
            String folder = "D:/uploads/";
            File dir = new File(folder);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            file.transferTo(new File(folder + fileName));
            // 注意：如果你的实体类字段名是 avatar，请确保 Alumni.java 里也是这个名字
            alumni.setAvatar(fileName);
        }

        // 【MP 重构点】使用 insert 方法
        alumniMapper.insert(alumni);

        // 2. 【核心动作】删除 Redis 里的旧缓存
        // 下次有人访问荣誉墙时，发现缓存没了，就会去数据库查出最新的 17 并重新存入
        redisTemplate.delete("alumni:total");// 对应 HTML 里的 ${total}


        return "redirect:/alumni-wall";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String user,
                          @RequestParam("password") String pwd,
                          HttpSession session,
                          Model model) {
        if ("admin".equals(user) && "123456".equals(pwd)) {
            session.setAttribute("isAdmin", true);
            return "redirect:/alumni-wall";
        } else {
            model.addAttribute("error", "用户名或密码错误！");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/alumni-wall";
    }


    @Autowired
    private ObjectMapper objectMapper; // Spring Boot 自动帮你创建好的“翻译官”

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) throws JsonProcessingException {
        String cacheKey = "search:alumni:" + keyword;

        // 1. 先去 Redis (我们的缓存间) 看看
        String jsonResult = redisTemplate.opsForValue().get(cacheKey);
        List<Alumni> results;

        if (jsonResult == null) {
            // 2. 没搜过：去 MySQL 辛苦查一下
            results = alumniMapper.selectList(new QueryWrapper<Alumni>().like("name", keyword));

            // 3. 翻译成 JSON 存入 Redis，有效期 10 分钟
            String jsonToSave = objectMapper.writeValueAsString(results); // 这里的真实方法替代了 convertToJson
            redisTemplate.opsForValue().set(cacheKey, jsonToSave, 10, TimeUnit.MINUTES);
            System.out.println(">>> 搜索 [" + keyword + "] 缓存未命中，查库并存入 Redis");
        } else {
            // 4. 搜过：直接把 JSON 翻译回 List 集合
            // 这里的真实方法替代了 convertToList
            results = objectMapper.readValue(jsonResult, new TypeReference<List<Alumni>>() {});
            System.out.println(">>> 搜索 [" + keyword + "] 结果秒开！直接从 Redis 提取");
        }

        model.addAttribute("alumniList", results);
        model.addAttribute("currentKeyword", keyword); // 把关键词还给前端，显示“搜到了 X 位校友”
        return "alumni";
    }

    @GetMapping("/delete/{id}")
    public String deleteAlumni(@PathVariable("id") Long id, HttpSession session) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/login";
        }

        // 【MP 重构点】使用 deleteById
        alumniMapper.deleteById(id);
        return "redirect:/alumni-wall";
    }

    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable("id") Long id, Model model, HttpSession session) {
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/login";
        }

        // 【MP 重构点】使用 selectById
        Alumni alumni = alumniMapper.selectById(id);
        if (alumni == null) {
            throw new IllegalArgumentException("无效的 ID:" + id);
        }
        model.addAttribute("alumni", alumni);
        return "edit-alumni";
    }

    @PostMapping("/update-alumni")
    public String updateAlumni(Alumni alumni, @RequestParam("imageFile") MultipartFile file) throws Exception {
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            file.transferTo(new File("D:/uploads/" + fileName));
            alumni.setAvatar(fileName);
        }

        // 【MP 重构点】使用 updateById 明确执行更新操作
        alumniMapper.updateById(alumni);
        return "redirect:/alumni-wall";
    }
}