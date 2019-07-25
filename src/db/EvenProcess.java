package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import row.RowMode;
import tablebeans.Friend;
import tablebeans.User;
import tablejson.JsonInterface;
import tablejson.UserFriendsInformation;
import tools.ObjectTool;

public class EvenProcess {
	
	/**
	 * 处理登录业务
	 * @param userName 用户名
	 * @param passWord 密码
	 * @return
	 */
	public static User login(String userName, String passWord){
		Connection _conn = ExeSql.createExeSql().getConnection();
		PreparedStatement prepareStatement = null;
		ResultSet result = null;
		User user = new User();
		try {
			prepareStatement = _conn.prepareStatement("select * from user z where username = ? and password = ?");
			prepareStatement.setString(1, userName);
			prepareStatement.setString(2, passWord);
			result = prepareStatement.executeQuery();
			if (result.next()) {
				user.setRow(result);
			}
		}
		catch (SQLException e) {
			System.out.println("execute sql error..");
			e.printStackTrace();
		}
		return user;
	}
	
	/**
	 * 通过userid从user表中获得用户信息
	 * @param userID 用户名ID
	 * @return
	 */
	public static User getUserInfo(int userID){
		//TODO privilege management
		User user = new User();
		Connection conn = ExeSql.createExeSql().getConnection();
		PreparedStatement prepareStatement = null;
		ResultSet result = null;
		try {
			prepareStatement = conn.prepareStatement("select * from user z where z.id = ?");
			prepareStatement.setInt(1, userID);
			result = prepareStatement.executeQuery();
			while (result.next()) {
				user.setRow(result);
			}
		}catch (SQLException e) {
			System.out.println("get sql exception..");
			e.printStackTrace();
		}
		return user;
	}
	
	/**
	 * 通过userID获得当前用户的好友信息
	 * @param userID
	 * @return
	 */
	public static UserFriendsInformation getUserFriendInfo(int userID){
		UserFriendsInformation userFriendsInformation = new UserFriendsInformation();
		Connection conn = ExeSql.createExeSql().getConnection();
		PreparedStatement prepareStatement = null;
		ResultSet result = null;
		try {
			prepareStatement = conn.prepareStatement("select * from user z where z.id = ?");
			prepareStatement.setInt(1, userID);
			result = prepareStatement.executeQuery();
			while (result.next()) {
				userFriendsInformation.setRow(result);
			}
		}catch (SQLException e) {
			System.out.println("get sql exception..");
			e.printStackTrace();
		}
		return userFriendsInformation;
	}
	
	/**
	 * 通过username从user表中获得用户信息
	 * @param userName 用户名
	 * @return
	 */
	public static ArrayList<User> getUserInfo(String userName){
		ArrayList<User>  arrList = new ArrayList<User>();
		Connection conn = ExeSql.createExeSql().getConnection();
		PreparedStatement prepareStatement = null;
		ResultSet result = null;
		User user = new User();
		try {
			prepareStatement = conn.prepareStatement("select * from user z where z.username = ?");
			prepareStatement.setString(1, userName);
			result = prepareStatement.executeQuery();
			while (result.next()) {
				user.setRow(result);
				arrList.add(user);
			}
		}catch (SQLException e) {
			System.out.println("get UserImagePath exception..");
			e.printStackTrace();
		}
		return arrList;
	}
	
	/**
	 * 通过userID从friend表中获得用户的好友信息
	 * @param userID 用户ID
	 * @return 存放friend表信息的list
	 */
	public static ArrayList<Friend> getFriendInfo(int userID){
		ArrayList<Friend> arrList = new ArrayList<Friend>();
		Connection conn = ExeSql.createExeSql().getConnection();
		PreparedStatement prepareStatement = null;
		ResultSet result = null;
		try {
			prepareStatement = conn.prepareStatement("select * from friend f where f.user_id = ?");
			prepareStatement.setInt(1, userID);
			result = prepareStatement.executeQuery();
			while (result.next()) {
				//arraylistadd元素, 该元素需要时一个新的对象, 否则会产生数据紊乱
				Friend friend = new Friend();
				friend.setRow(result);
				arrList.add(friend);
			}
		}catch (SQLException e) {
//			throw new SQLException("sql exception..");
			e.printStackTrace();
		}
		return arrList;
	}

	/**
	 * 更新user表数据
	 * @param userID
	 * @param userName
	 * @param password
	 * @param userNick
	 * @param userImagePath
	 * @param userState
	 * @param gender
	 * @param personLabel
	 * @return
	 */
	public static boolean updateUserInfo(int userID
			, String userName
			, String password
			, String userNick
			, String userImagePath
			, String userState
			, Integer gender
			, String personLabel){
		//TODO privilege management
//		User user = new User();
		Connection conn = ExeSql.createExeSql().getConnection();
		PreparedStatement prepareStatement = null;
		int result = 0;
		try {
			String sql = "update user set ";
			ArrayList<Integer> parameters = new ArrayList<Integer>();
			if (!ObjectTool.isNull(userName)) {
				sql += "username = ?,";
				parameters.add(1);
			}
			if (!ObjectTool.isNull(password)) {
				sql += "password = ?,";
				parameters.add(2);
			}
			if (!ObjectTool.isNull(userNick)) {
				sql += "usernick = ?,";
				parameters.add(3);
			}
			if (!ObjectTool.isNull(userImagePath)){
				sql += "userimagepath = ?,";
				parameters.add(4);
			}
			if (!ObjectTool.isNull(userState)) {
				sql += "userstate = ?,";
				parameters.add(5);
			}
			if (!ObjectTool.isNull(gender)){
				sql += "gender = ?,";
				parameters.add(6);
			}
			if (!ObjectTool.isNull(personLabel)){
				sql += "personlabel = ?,";
				parameters.add(7);
			}

			sql += "where id = ?";
			parameters.add(8);
			sql = sql.replace(",where"," where");

			prepareStatement = conn.prepareStatement(sql);

			int i = 0;
//			for(;i<parameters.toArray().length;i++)
			for (Integer index : parameters) {
				switch (index){
					case 1:
						prepareStatement.setString(++i,userName);
						break;
					case 2:
						prepareStatement.setString(++i,password);
						break;
					case 3:
						prepareStatement.setString(++i,userNick);
						break;
					case 4:
						prepareStatement.setString(++i,userImagePath);
						break;
					case 5:
						prepareStatement.setString(++i,userState);
						break;
					case 6:
						prepareStatement.setInt(++i,gender);
						break;
					case 7:
						prepareStatement.setString(++i,personLabel);
						break;
					case 8:
						prepareStatement.setInt(++i,userID);
						break;
				}
			}
//			prepareStatement.setInt(1, userID);
			result = prepareStatement.executeUpdate();
			while (result != 1) {
				return  false;
			}
		}catch (SQLException e) {
			System.out.println("get sql exception..");
			e.printStackTrace();
			return  false;
		}
		return true;
	}
}
