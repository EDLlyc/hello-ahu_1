package com.ahu.helloahu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlumniRepository extends JpaRepository<Alumni, Long> {
    // 魔法方法：根据名字进行模糊查询 (SQL: WHERE name LIKE %?%)
    List<Alumni> findByNameContaining(String name);
}