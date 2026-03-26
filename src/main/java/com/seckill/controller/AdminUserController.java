package com.seckill.controller;

import com.seckill.dto.Result;
import com.seckill.dto.UserAddDTO;
import com.seckill.dto.UserInfoDTO;
import com.seckill.entity.User;
import com.seckill.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public ResponseEntity<Result<List<User>>> getUserList() {
        log.info("管理员查询用户列表");
        try {
            List<User> userList = userService.getUserList();
            return ResponseEntity.ok(Result.success("查询成功", userList));
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return ResponseEntity.ok(Result.fail("查询失败：" + e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Result<UserInfoDTO>> addUser(@Valid @RequestBody UserAddDTO dto) {
        log.info("管理员添加用户：username={}", dto.getUsername());
        try {
            UserInfoDTO result = userService.addUser(dto);
            return ResponseEntity.ok(Result.success("添加成功", result));
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return ResponseEntity.ok(Result.fail(e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Result<Boolean>> deleteUser(@PathVariable Long userId) {
        log.info("管理员删除用户：userId={}", userId);
        try {
            boolean result = userService.deleteUser(userId);
            if (result) {
                return ResponseEntity.ok(Result.success("删除成功", true));
            } else {
                return ResponseEntity.ok(Result.fail("用户不存在或已删除"));
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ResponseEntity.ok(Result.fail("删除失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Result<User>> getUserById(@PathVariable Long userId) {
        log.info("管理员查询用户详情：userId={}", userId);
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                return ResponseEntity.ok(Result.success("查询成功", user));
            } else {
                return ResponseEntity.ok(Result.fail("用户不存在"));
            }
        } catch (Exception e) {
            log.error("查询用户详情失败", e);
            return ResponseEntity.ok(Result.fail("查询失败：" + e.getMessage()));
        }
    }
}
