package demo.action;

import demo.model.Student;
import demo.util.MyBatisSqlSession;
import org.apache.ibatis.session.SqlSession;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator
 * 下午2:04 16-5-12
 */
@WebServlet(urlPatterns = "/student")
public class StudentAction extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (action.equals("signUp")) {
            signUp(req, resp);
        }
        if (action.equals("login")) {
            login(req, resp);
        }
        if (action.equals("signUp")) {
            method(req, resp);
        }
    }

    protected void signUp(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String password = req.getParameter("password");
        StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
        password = encryptor.encryptPassword(password);

        Student student = new Student();
        student.setEmail(req.getParameter("email").trim());
        student.setUsername(req.getParameter("username").trim());
        student.setPassword(password);
        student.setLastIp(req.getRemoteAddr());

        try (SqlSession sqlSession = MyBatisSqlSession.getSqlSession(true)) {
            sqlSession.insert("student.create", student);
        }

        resp.sendRedirect("/index.jsp");
    }

    protected void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try (SqlSession sqlSession = MyBatisSqlSession.getSqlSession(false)) {
            List<Student> students = sqlSession.selectList("student.login", email);
            if (students.size() == 1) {
                Student student = students.get(0);
                String encryptedPassword = student.getPassword();
                StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
                if (encryptor.checkPassword(password, encryptedPassword)) {

                    String lastIp = req.getRemoteAddr();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String lastLogin = simpleDateFormat.format(new Date());

                    // update student last_ip, last_login time
                    student.setLastIp(lastIp);
                    student.setLastLogin(lastLogin);
                    sqlSession.update("student.last", student);
                    sqlSession.commit();

                    student.setPassword(null);
                    req.getSession().setAttribute("student", student);
                    resp.sendRedirect("/student/index.jsp");
                    return;
                }
            }
        }
        req.setAttribute("message", "invalid email or password!");
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }

    protected void method(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
