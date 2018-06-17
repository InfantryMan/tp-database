package forumapi.databases.services;

import forumapi.databases.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final JdbcTemplate jdbc;
    final String sqlCountUsers = "SELECT COUNT(nickname) FROM users ;";

    @Autowired
    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer getCount() {
        return jdbc.queryForObject(sqlCountUsers, Integer.class);
    }

    public void truncateTable() {
        String sql = "TRUNCATE TABLE users CASCADE ;";
        jdbc.execute(sql);
    }

    // Done
    public User addUser(User insertUser) {
        final String sql = "INSERT INTO users (nickname, email, fullname, about) VALUES (?, ?, ?, ?) RETURNING *;";
        User user = null;
        try {
            user = jdbc.queryForObject(sql, new userMapper(),
                    insertUser.getNickname(), insertUser.getEmail(),
                    insertUser.getFullname(), insertUser.getAbout());

        } catch (DuplicateKeyException e) {
            return null;
        }
        return user;
    }

    // Done
    public List<User> getDubUsers(User user) {
        final String sql = "SELECT * FROM users WHERE lower(nickname) = lower(?) OR lower(email) = lower(?);";
        final String nickName = user.getNickname();
        final String email = user.getEmail();
        List<User> dubUsers = null;
        try {
            dubUsers = jdbc.query(sql, new userMapper(), nickName, email);
        } catch (DataAccessException e) {
            return null;
        }
        return dubUsers;
    }

    //Done
    public User getUserByNickName(String userNickName) {
        final String sql = "SELECT * FROM users WHERE lower(nickname) = lower(?);";
        User user = null;
        try {
            user = jdbc.queryForObject(sql, new userMapper(), userNickName);
        } catch (DataAccessException e) {
            return null;
        }
        return user;
    }

    public User getUserByEmail(String email) {
        final String sql = "SELECT * FROM users WHERE lower(email) = lower(?);";
        User user = null;
        try {
            user = jdbc.queryForObject(sql, new Object [] {email}, new userMapper());
        } catch (DataAccessException e) {
            return null;
        }
        return user;
    }

    public User updateUser(String userNickName, User updateUser) {
        StringBuilder sql = new StringBuilder("UPDATE users SET ");

        String newUserEmail = updateUser.getEmail();
        String newUserFullname = updateUser.getFullname();
        String newUserAbout = updateUser.getAbout();
        String newUserNickname = updateUser.getNickname();

        Boolean isUpdate = false;

        if (newUserEmail != null) {
            sql.append("email = \'" + newUserEmail + "\', ");
            isUpdate = true;
        }
        if (newUserFullname != null) {
            sql.append("fullname = \'" + newUserFullname + "\', ");
            isUpdate = true;
        }
        if ( newUserAbout != null ) {
            sql.append("about = \'" + newUserAbout + "\', ");
            isUpdate = true;
        }
        if ( newUserNickname != null ) {
            sql.append("nickname = \'" + newUserNickname + "\', ");
            isUpdate = true;
        }

        sql.deleteCharAt(sql.toString().length() - 2);
        sql.append("WHERE nickname = \'" + userNickName + "\' RETURNING *;");

        User returnUser = null;
        if (isUpdate) {
            try {
                returnUser = jdbc.queryForObject(sql.toString(), new userMapper());
            } catch (DuplicateKeyException e) {
                return null;
            }
        } else {
            returnUser = getUserByNickName(userNickName);
        }
        return returnUser;
    }

    // Done
    public List<User> getUsersByForumSlug(String forumSlug, Integer limit, String since, Boolean desc) {
        final String [] queries = {
                "SELECT * FROM users u JOIN (SELECT author FROM users_forum WHERE LOWER(forum) = LOWER(?)) uf " +
                "ON u.nickname = uf.author " +
                "ORDER BY LOWER(nickname COLLATE \"C\") ASC " +
                "LIMIT ?; ",

                "SELECT * FROM users u JOIN (SELECT author FROM users_forum WHERE LOWER(forum) = LOWER(?)) uf " +
                "ON u.nickname = uf.author " +
                "ORDER BY LOWER(nickname COLLATE \"C\") DESC " +
                "LIMIT ?; ",

                "SELECT * FROM users u JOIN ( SELECT author FROM users_forum WHERE LOWER(forum) = LOWER(?) AND LOWER(author COLLATE \"C\") > LOWER(?) ) uf " +
                "ON u.nickname = uf.author " +
                "ORDER BY LOWER(nickname COLLATE \"C\") ASC " +
                "LIMIT ?; ",

                "SELECT * FROM users u JOIN ( SELECT author FROM users_forum WHERE LOWER(forum) = LOWER(?) AND LOWER(author COLLATE \"C\") < LOWER(?) ) uf " +
                "ON u.nickname = uf.author " +
                "ORDER BY LOWER(nickname COLLATE \"C\") DESC " +
                "LIMIT ?; ",
        };

        String sql = null;
        if (since == null && !desc) sql = queries[0];
        if (since == null && desc)  sql = queries[1];
        if (since != null && !desc) sql = queries[2];
        if (since != null && desc)  sql = queries[3];

        List<User> userList = null;
        try {
            if (since != null)
                userList = jdbc.query(sql, new userMapper(), forumSlug, since, limit);
            else
                userList = jdbc.query(sql, new userMapper(), forumSlug, limit);
        } catch (DataAccessException e) {
            return null;
        }
        return userList;
    }


    private final class userMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            final String nickName = rs.getString("nickname");
            final String about = rs.getString("about");
            final String email = rs.getString("email");
            final String fullName = rs.getString("fullname");
            return new User(about, email, fullName, nickName);

        }
    }
}
