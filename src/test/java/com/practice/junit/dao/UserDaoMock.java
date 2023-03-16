package com.practice.junit.dao;

import org.example.dao.UserDao;

public class UserDaoMock extends UserDao {
    @Override
    public boolean delete(Integer userId) {
        return false;
    }
}
