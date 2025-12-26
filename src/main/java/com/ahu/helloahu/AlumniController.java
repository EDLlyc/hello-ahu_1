package com.ahu.helloahu;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // 记得导入这个
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Controller
public class AlumniController {

    @Autowired
    private AlumniRepository repository;

    @GetMapping("/alumni-wall")
    public String showWall(Model model) {
        List<Alumni> list = repository.findAll();
        // 核心：使用 JPA 自带的 count() 方法获取总人数
        long totalCount = repository.count();

        model.addAttribute("alumniList", list);
        model.addAttribute("total", totalCount); // 把总数传给前端
        return "alumni";
    }
    // 新增：处理网页表单提交的数据
// 记得导入：import org.springframework.web.multipart.MultipartFile;
// import java.io.File; import java.util.UUID;

    @PostMapping("/add-alumni")
    public String addAlumni(Alumni alumni, @RequestParam("imageFile") MultipartFile file, HttpSession session, RedirectAttributes ra) throws Exception {

        if (session.getAttribute("isAdmin") == null) {
            // 关键：给重定向的目标带上一个消息
            ra.addFlashAttribute("error", "权限不足！请先登录管理员账号。");
            return "redirect:/login";
        }
        if (!file.isEmpty()) {
            // 1. 确定保存路径（比如保存到 D:/uploads/）
            String folder = "D:/uploads/";
            File dir = new File(folder);
            if (!dir.exists()) dir.mkdirs();

            // 2. 给图片起个唯一的名字，防止重名覆盖
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 3. 把文件从内存存到硬盘
            file.transferTo(new File(folder + fileName));

            // 4. 把文件名记在校友信息里
            alumni.setAvatar(fileName);
        }
        repository.save(alumni);
        return "redirect:/alumni-wall";
    }



    // 展示登录页
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }




    // 处理登录逻辑
    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String user,
                          @RequestParam("password") String pwd,
                          HttpSession session,
                          Model model) {
        // 简单设置一个账号密码：admin / 123456
        if ("admin".equals(user) && "123456".equals(pwd)) {
            session.setAttribute("isAdmin", true); // 发放管理员身份标识
            return "redirect:/alumni-wall"; // 登录成功，跳回主页
        } else {
            model.addAttribute("error", "用户名或密码错误！");
            return "login"; // 登录失败，留在当前页
        }
    }

    // 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 销毁 Session
        return "redirect:/alumni-wall";
    }


// 修改后：显式指定参数名为 "keyword"
    @GetMapping("/search")
    public String searchAlumni(@RequestParam("keyword") String keyword, Model model) {
        // 逻辑保持不变
        List<Alumni> searchResult = repository.findByNameContaining(keyword);
        model.addAttribute("alumniList", searchResult);
        model.addAttribute("currentKeyword", keyword);
        return "alumni";
    }
    // 记得导入：import org.springframework.web.bind.annotation.PathVariable;

    @GetMapping("/delete/{id}") // 绑定路径，{id} 是一个占位符
    public String deleteAlumni(@PathVariable("id") Long id, HttpSession session) {
        // 1. 调用 Repository 的内置方法，按 ID 删除
        repository.deleteById(id);

        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/login";
        }


        // 2. 删完之后，重定向回到列表页，你会发现那一行不见了
        return "redirect:/alumni-wall";
    }



    // 1. 展示编辑页面
    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable("id") Long id, Model model, HttpSession session) {
        // 根据 ID 查找校友，如果找不到就报错
        if (session.getAttribute("isAdmin") == null) {
            return "redirect:/login";
        }

        Alumni alumni = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("无效的 ID:" + id));
        model.addAttribute("alumni", alumni);
        return "edit-alumni";
    }

    // 2. 处理更新请求
    @PostMapping("/update-alumni")
    public String updateAlumni(Alumni alumni, @RequestParam("imageFile") MultipartFile file) throws Exception {
        // 处理图片逻辑（跟添加时一样）
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            file.transferTo(new File("D:/uploads/" + fileName));
            alumni.setAvatar(fileName);
        }

        // 因为 alumni 对象里已经带了 ID（隐藏域传过来的），save 会自动识别为 UPDATE 操作
        repository.save(alumni);
        return "redirect:/alumni-wall";
    }

}