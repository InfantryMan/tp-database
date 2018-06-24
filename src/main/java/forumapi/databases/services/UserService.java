package forumapi.databases.services;

import forumapi.databases.models.Queries;
import forumapi.databases.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final JdbcTemplate jdbc;


    @Autowired
    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer getCount() {
        try {
            return jdbc.queryForObject(Queries.selectUsersCount, Integer.class);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public void truncateTable() {
        try {
            jdbc.execute(Queries.truncateUsers);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public User addUser(User insertUser) {
        User user = null;
        try {
            user = jdbc.queryForObject(Queries.insertUser, new userMapper(),
                    insertUser.getNickname(), insertUser.getEmail(),
                    insertUser.getFullname(), insertUser.getAbout());
        }  catch (EmptyResultDataAccessException e) {
            return null;
        }
        return user;
    }

    public List<User> getDubUsers(User user) {
        List<User> dubUsers = null;
        try {
            dubUsers = jdbc.query(Queries.selectDubUsers, new userMapper(), user.getNickname(), user.getEmail());
        } catch (DataAccessException e) {
            return null;
        }
        return dubUsers;
    }

    public User getUserByNickName(String userNickName) {
        User user = null;
        try {
            user = jdbc.queryForObject(Queries.selectUser, new userMapper(), userNickName);
        } catch (DataAccessException e) {
            return null;
        }
        return user;
    }

    public Integer updateUser(String userNickName, User updateUser) {
        StringBuilder sql = new StringBuilder("UPDATE users SET ");

        String newUserEmail = updateUser.getEmail();
        String newUserFullname = updateUser.getFullname();
        String newUserAbout = updateUser.getAbout();
        String newUserNickname = updateUser.getNickname();

        if (newUserEmail != null) {
            sql.append("email = \'" + newUserEmail + "\', ");
        }
        if (newUserFullname != null) {
            sql.append("fullname = \'" + newUserFullname + "\', ");
        }
        if ( newUserAbout != null ) {
            sql.append("about = \'" + newUserAbout + "\', ");
        }
        if ( newUserNickname != null ) {
            sql.append("nickname = \'" + newUserNickname + "\', ");
        }

        sql.deleteCharAt(sql.toString().length() - 2);
        sql.append("WHERE lower(nickname) = lower(\'" + userNickName + "\'); ");

        try {
            jdbc.update(sql.toString());
        } catch (DuplicateKeyException e) {
            return -1;
        } catch (DataAccessException e) {
            return -2;
        }
        return 0;
    }

    public String listToString(List<String> usersNicknames) {
        StringBuilder nicknamesStringBuilder = new StringBuilder("(");
        for (String userName: usersNicknames) {
            nicknamesStringBuilder.append("\'");
            nicknamesStringBuilder.append(userName);
            nicknamesStringBuilder.append("\', ");
        }
        nicknamesStringBuilder.replace(nicknamesStringBuilder.length() - 2, nicknamesStringBuilder.length() - 1, ")");
        return nicknamesStringBuilder.toString();
    }

    public List<User> getUsersByForumSlug(String forumSlug, Integer limit, String since, Boolean desc) {
        String sql = null;
        if (since == null && !desc) sql = Queries.selectUserForum;
        if (since == null && desc)  sql = Queries.selectUserForumDesc;
        if (since != null && !desc) sql = Queries.selectUserForumSince;
        if (since != null && desc)  sql = Queries.selectUserForumDescSince;

        List<String> nicknames = null;
        try {
            if (since != null) {
                nicknames = jdbc.queryForList(sql, String.class, forumSlug, since, limit);
            } else {
                nicknames = jdbc.queryForList(sql, String.class, forumSlug, limit);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }

        if (nicknames.isEmpty()) {
            return new ArrayList<>();
        }

        String order = (desc) ? "DESC " : "ASC ";
        String usersNicknames = listToString(nicknames);
        StringBuilder sqlBuilderGetForumUsers = new StringBuilder();
        sqlBuilderGetForumUsers.append("SELECT * FROM users WHERE nickname IN " + usersNicknames + " ");
        sqlBuilderGetForumUsers.append("ORDER BY lower(nickname COLLATE \"C\") " + order);

        String sqlGetForumUsers = sqlBuilderGetForumUsers.toString();

        List<User> usersForum = null;
        try {
            usersForum = jdbc.query(sqlGetForumUsers, new userMapper());
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
        return usersForum;
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
