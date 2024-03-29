package com.example.auth.api;

import com.example.auth.domain.constant.ResponseStatus;
import com.example.auth.vo.response.BasicResponse;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author Anthony Jinhyuk Kim
 * @version 1.0.0
 * @since 2018-11-06
 */
@RestController
@RequestMapping("/api/v1/admins")
public class AdminRestController {
    @GetMapping(value = "test")
    @ApiOperation(httpMethod = "GET", value = "GET test", response = BasicResponse.class)
    public Mono<ResponseEntity<?>> test() {
        return Mono.just(ResponseEntity.ok().body(new BasicResponse(ResponseStatus.OK)));
    }
}
