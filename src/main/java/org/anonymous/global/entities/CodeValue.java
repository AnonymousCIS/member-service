package org.anonymous.global.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@RedisHash(timeToLive = 300)
public class CodeValue implements Serializable {
    @Id
    private String code;
    private Object value;
}
