package com.example.cp_main_be.user.presentation;

<<<<<<< HEAD
import com.example.cp_main_be.config.AbstractContainerBaseTest;
=======
>>>>>>> a79dc75 (feat: User 엔티티 개발, 테스트 코드 작성, 닉네임으로 user 정보 저장하는 api 개발. 테스트 코드용 의존성 추가 h2 db)
import com.example.cp_main_be.user.domain.User;
import com.example.cp_main_be.user.domain.UserStatus;
import com.example.cp_main_be.user.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
<<<<<<< HEAD
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
=======
import org.springframework.test.context.TestPropertySource;
>>>>>>> a79dc75 (feat: User 엔티티 개발, 테스트 코드 작성, 닉네임으로 user 정보 저장하는 api 개발. 테스트 코드용 의존성 추가 h2 db)
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
<<<<<<< HEAD
class UserControllerTest extends AbstractContainerBaseTest {

  @Autowired private UserService userService;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("replicate.api.token", () -> "dummy-token-for-user-test");
  }

  @Test
  public void 회원_생성시_uuid가_자동으로_생기는가() throws Exception {
    // given
    User user = new User();
    // when
    user.setUsername("test");
    userService.saveUser(user);
    // then
    //        System.out.println("user.getUuid = " + user.getUuid());
    Assertions.assertNotNull(user.getUuid());
  }

  @Test
  public void 유저_이름이_없으면_오류() throws Exception {
    // given
    User user = new User();
    // when
    user.setEmail("test@example.com");
    user.setPasswordHash("hashedpassword");
    user.setLevel(1L); // '1L' 대신 '1'로 변경하여 불필요한 경고 제거
    user.setExperiencePoints(0);
    user.setTemperatureScore(0);
    user.setStatus(UserStatus.ACTIVE);

    // then
    Assertions.assertThrows(
        DataIntegrityViolationException.class,
        () -> {
          userService.saveUser(user);
        });
  }

  @Test
  public void 유저생성성공() throws Exception {
    // given
    User user = new User();
    // when
    user.setUsername("test");
    userService.saveUser(user);
    User user1 = userService.findUserById(user.getId());
    // then
    Assertions.assertEquals(user.getId(), user1.getId());
  }
}
=======
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserControllerTest {

    @Autowired
    private UserService userService;

    @Test
    public void 회원_생성시_uuid가_자동으로_생기는가() throws Exception {
        //given
        User user = new User();
        //when
        user.setUsername("test");
        userService.saveUser(user);
        //then
//        System.out.println("user.getUuid = " + user.getUuid());
        Assertions.assertNotNull(user.getUuid());
    }

    @Test
    public void 유저_이름이_없으면_오류() throws Exception {
        //given
        User user = new User();
        //when
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword");
        user.setLevel(1L); // '1L' 대신 '1'로 변경하여 불필요한 경고 제거
        user.setExperiencePoints(0);
        user.setTemperatureScore(0);
        user.setStatus(UserStatus.ACTIVE);

        //then
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            userService.saveUser(user);
        });
    }

    @Test
    public void 유저생성성공() throws Exception {
        //given
        User user = new User();
        //when
        user.setUsername("test");
        userService.saveUser(user);
        User user1 = userService.findUserById(user.getId());
        //then
        Assertions.assertEquals(user.getId(), user1.getId());
    }
}
>>>>>>> a79dc75 (feat: User 엔티티 개발, 테스트 코드 작성, 닉네임으로 user 정보 저장하는 api 개발. 테스트 코드용 의존성 추가 h2 db)
