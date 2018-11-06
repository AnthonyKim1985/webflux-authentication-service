package com.example.auth;

import com.example.auth.domain.User;
import com.example.auth.domain.constant.ResponseStatus;
import com.example.auth.domain.constant.Role;
import com.example.auth.persistence.ReactiveUserRepository;
import com.example.auth.vo.request.AuthRequest;
import com.example.auth.vo.response.AuthResponse;
import com.example.auth.vo.response.BasicResponse;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

/**
 * Integration test for {@link ReactiveUserRepository} using Project Reactor types and operators.
 *
 * @author Anthony Jinhyuk Kim
 * @version 1.0.0
 * @since 2018-10-08
 */
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebfluxAuthenticationServiceApplicationTests {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveUserRepository userRepository;

    @Autowired
    private ReactiveMongoOperations operations;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Before
    public void makeDummy() {
        final Mono<MongoCollection<Document>> recreateCollection = operations.collectionExists(User.class)
                .flatMap(exists -> exists ? operations.dropCollection(User.class) : Mono.just(exists))
                .then(operations.createCollection(User.class, CollectionOptions.empty().size(Integer.MAX_VALUE).maxDocuments(Long.MAX_VALUE).capped()));

        StepVerifier.create(recreateCollection).expectNextCount(1L).verifyComplete();

        final long nowDateMilli = new Date().getTime();
        final Date accountExpirationDate = new Date(nowDateMilli + 1000L * 60L * 60L * 24L * 365L);
        final Flux<User> insertAll = operations.insertAll(
                Flux.just(
                        new User("hyuk0628", passwordEncoder.encode("1234"), "hyuk0628@gmail.com", accountExpirationDate, true, Arrays.asList(Role.ADMIN, Role.MANAGER, Role.STAFF)),
                        new User("jdh3060", passwordEncoder.encode("1234"), "jdh3060@gmail.com", accountExpirationDate, true, Collections.singletonList(Role.MANAGER)),
                        new User("ryujeonguk", passwordEncoder.encode("1234"), "ryujeonguk@gmail.com", accountExpirationDate, true, Collections.singletonList(Role.STAFF))
                ).collectList());

        StepVerifier.create(insertAll).expectNextCount(3L).verifyComplete();
    }

    public AuthResponse testA_shouldTheAuthRequestReturnsTheOKTokenIssuedCode_WhenTheAuthRequestIsGiven() {
        // Given
        final AuthRequest authRequest = new AuthRequest("hyuk0628", "1234");

        // When
        final AuthResponse authResponse = webTestClient
                .post().uri("/api/v1/auth/signin")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(fromObject(authRequest))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        // Then
        assert authResponse != null;
        assertThat(authResponse.getCode(), equalTo(ResponseStatus.OK_TOKEN_ISSUED.getCode()));

        return authResponse;
    }

    @Test
    public void testB_shouldTheAdminHelloRequestReturnsTheOkCode_WhenTheIssuedTokenIsGivenOnHttpAuthorizationHeader() {
        // Given
        final AuthResponse authResponse = testA_shouldTheAuthRequestReturnsTheOKTokenIssuedCode_WhenTheAuthRequestIsGiven();
        final String token = authResponse.getToken();
        assert token != null;

        // When
        final BasicResponse basicResponse = webTestClient
                .get().uri("/api/v1/admins/test")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BasicResponse.class)
                .returnResult()
                .getResponseBody();

        // Then
        assert basicResponse != null;
        assertThat(basicResponse.getCode(), equalTo(ResponseStatus.OK.getCode()));
    }
}
