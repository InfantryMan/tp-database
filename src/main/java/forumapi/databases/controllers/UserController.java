package forumapi.databases.controllers;

import forumapi.databases.messages.Message;
import forumapi.databases.messages.MessageStates;
import forumapi.databases.models.User;
import forumapi.databases.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Done
    @RequestMapping(path="{nickName}/create", method = RequestMethod.POST)
    public ResponseEntity userCreate(@PathVariable("nickName") String nickName,
                                     @RequestBody User userBody) {

        userBody.setNickname(nickName);
        if (!userBody.checkUser())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        User user = userService.addUser(userBody);

        if (user == null) {
            List<User> dubUsers = userService.getDubUsers(userBody);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(dubUsers);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @RequestMapping(path="{nickName}/profile", method = RequestMethod.GET)
    public ResponseEntity userProfileGet(@PathVariable("nickName") String nickName) {

        User user = userService.getUserByNickName(nickName);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.USER_NOT_FOUND.getMessage() + nickName));
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @RequestMapping(path="{nickName}/profile", method = RequestMethod.POST)
    public ResponseEntity userProfilePost(@PathVariable("nickName") String nickName,
                                      @RequestBody User updateUser) {

        User userDB = userService.getUserByNickName(nickName);
        if (userDB == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(MessageStates.USER_NOT_FOUND.getMessage() + nickName));
        }

        if (updateUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(userDB);
        }

        Integer code = userService.updateUser(nickName, updateUser);

        if (code == -1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message(MessageStates.EMAIL_OR_LOGIN_ALREADY_REGISTERED.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByNickName(nickName));
    }

}
