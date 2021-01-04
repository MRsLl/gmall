package com.atguigu.gmall.ums.exception;

import com.atguigu.gmall.ums.entity.UserEntity;
import lombok.Data;

@Data
public class UserException extends RuntimeException{
    public UserException() {
        super();
    }

    public UserException(String message) {
        super(message);
    }
}
