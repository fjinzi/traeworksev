package com.seckill.controller;

import com.seckill.dto.PageResult;
import com.seckill.dto.Result;
import com.seckill.dto.UserAddDTO;
import com.seckill.dto.UserListDTO;
import com.seckill.dto.UserQueryDTO;
import com.seckill.service.UserService;
import com.seckill.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public ResponseEntity<Result<PageResult<UserListDTO>>> getUserList(UserQueryDTO query) {
        log.info("管理员查询用户列表");
        try {
            PageResult<UserListDTO> result = userService.getUserList(query);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return ResponseEntity.ok(Result.fail(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<UserListDTO>> getUserById(@PathVariable Long id) {
        log.info("管理员查询用户详情：userId={}", id);
        try {
            UserListDTO result = userService.getUserById(id);
            if (result == null) {
                return ResponseEntity.ok(Result.fail("用户不存在"));
            }
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("查询用户详情失败", e);
            return ResponseEntity.ok(Result.fail(e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Result<UserListDTO>> addUser(@Valid @RequestBody UserAddDTO dto) {
        log.info("管理员添加用户：username={}", dto.getUsername());
        try {
            UserListDTO result = userService.addUser(dto);
            return ResponseEntity.ok(Result.success("添加成功", result));
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return ResponseEntity.ok(Result.fail(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Boolean>> deleteUser(@PathVariable Long id) {
        log.info("管理员删除用户：userId={}", id);
        try {
            boolean result = userService.deleteUser(id);
            return ResponseEntity.ok(Result.success("删除成功", result));
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ResponseEntity.ok(Result.fail(e.getMessage()));
        }
    }
}
