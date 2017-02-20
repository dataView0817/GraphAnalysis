package com.uniplore.graph.dsm.db.service.impl;

import com.uniplore.graph.dsm.db.entity.DbPO;
import com.uniplore.graph.dsm.db.service.IDbService;
import java.sql.Connection;
import java.sql.DriverManager;
import org.springframework.stereotype.Service;

@Service
public class DbService implements IDbService {

  @Override
  public String connectDataBase(DbPO dbPo) throws Exception {
    // 使用JDBC连接数据库
    String driverName = dbPo.getDriverName();// 首先应该得到其驱动，判断究竟是何种数据库‘
    String url = null;
    if (driverName != null && driverName.contains("mysql")) {
      url = "jdbc:mysql://" + dbPo.getIpAddress() + ":" + dbPo.getPortNumber()
        + "?connectTimeout=3000&socketTimeout=3000"; // 设置连接超时的时间均是3s，如果3s未连接成功则直接终止连接
    }
    String user = dbPo.getUserName();
    String password = dbPo.getPassword();
    try {
      Class.forName(driverName);
      // 连接数据库
      Connection connection = DriverManager.getConnection(url, user, password);
      if (dbPo.getIpAddress().length() != 0 && connection != null) {
        return "数据库连接成功";
      }
    } catch (Exception ex) {
      String message = ex.getMessage(); // 会打印出真实的数据库连接错误信息
      if (message.contains("Access denied")) {
        return "用户名和密码无效";
      } else if (message.contains("Communications link failure")) {
        return "与数据库通信时出错，不能连接到数据库服务器，请检查服务器是否正在运行以及您是否有权访问请求的数据库";
      } else {
        return "数据库连接失败";
      }
    }
    return "数据库连接失败";
  }

}
