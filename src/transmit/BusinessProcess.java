package transmit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.stream.FileImageInputStream;

import customexception.RequestParameterExcetion;
import db.EvenProcess;
import message.MessageContext;
import message.MessageHead;
import message.MessageModel;
import tablebeans.Friend;
import tablebeans.User;
import tablejson.ResponesImage;
import tablejson.UserFriendsInformation;
import tools.ObjectTool;
import tools.TransmitTool;

/**
 * 
 * @author zxk
 *
 */
public class BusinessProcess {
	
	//数据库查询结果缓存
	
	private static Map<String, MessageModel> $loginServer_Cache = 
			new HashMap<>();
	
	private static Map<String, MessageModel> $getFriendsIDServer_Cache = 
			new HashMap<>();
	
	private static Map<String, MessageModel> $getUserFriendInfoServer_Cache = 
			new HashMap<>();
	
	//key: userID value: imageByte
	private static Map<String, byte[]> $getUserFriendImageServer_Cache = 
			new HashMap<>();
	//key: userID value: imagePath
	private static Map<String, String> userFriendImage_Cache = 
			new HashMap<>();
	
	/**
	 * 处理用户登录请求
	 * @param messageModel
	 * @return
	 */
	public static MessageModel loginServer(MessageModel messageModel) {
		MessageHead requestMessageHead = messageModel.getMessageHead();
//		MessageContext loginMessageContext = messageModel.getMessageContext();
		
		String requestDescribe = requestMessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);
		
		String[] loginparameters = requestDescribe.split("\\?");
		
		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符?), 包含则替换相应字符
		if (loginparameters.length != 2) {
			System.err.println("parameter excetion..");
			//TODO
			return null;
		}
		
		MessageModel responesMessageModel = null;
		
		for (Entry<String, MessageModel> loginServerCache : $loginServer_Cache.entrySet()) {
			
			if(loginServerCache.getKey().equals(loginparameters[1])) {
				
				responesMessageModel = loginServerCache.getValue();
				
				MessageHead responesMessageHead = responesMessageModel.getMessageHead();
				responesMessageHead.setReplyTime(System.currentTimeMillis());
				responesMessageHead.setRequestTime(requestMessageHead.getRequestTime());
				responesMessageHead.setRequestNO(requestMessageHead.getRequestNO());
				
				return responesMessageModel;
			}
			
		}
		
		Map<String, String> parameters;
		String username = null;
		String password = null;
		try {
			parameters = TransmitTool.analysisRequestParameters(loginparameters[1]);
			
			username = parameters.get("userName");
			password = parameters.get("password");
			
		} catch (RequestParameterExcetion e) {
			e.printStackTrace();
			return null;
		}finally {
			if (ObjectTool.isNull(username) 
					|| ObjectTool.isNull(password)) {
				System.err.println("get parameter error..");
				return null;
			}
		}
		
//		String loginServerCacheKey = requestDescribe;
		
		User loginUser = EvenProcess.login(username, password);
		
		MessageHead responesMessageHead = new MessageHead();
		responesMessageHead.setReplyRequestResult(true);
		responesMessageHead.setType(1);
		responesMessageHead.setReplyTime(System.currentTimeMillis());
		responesMessageHead.setRequestTime(requestMessageHead.getRequestTime());
		responesMessageHead.setRequestNO(requestMessageHead.getRequestNO());
		
		if (ObjectTool.isNull(loginUser.getId())) {
			responesMessageHead.setReplyDataType(null);
			responesMessageHead.setReplyDescribe("user is non-existent..");
			responesMessageModel = new MessageModel(responesMessageHead, null);
			synchronized ($loginServer_Cache) {				
				$loginServer_Cache.put(requestDescribe, responesMessageModel);
			}
			return responesMessageModel;
		}
		
		responesMessageHead.setReplyDataType(User.class);
		responesMessageHead.setReplyDescribe("request success..");
		
		MessageContext responesMessageContext = new MessageContext();
		responesMessageContext.setObject(loginUser);
		System.out.println("loginUser: " + loginUser);
		
		responesMessageModel = new MessageModel(responesMessageHead, responesMessageContext);
		synchronized ($loginServer_Cache) {				
			$loginServer_Cache.put(loginparameters[1], responesMessageModel);
		}
		
		return responesMessageModel;
	}
	
	/**
	 * 处理用户好友列表请求
	 * @param messageModel
	 * @return
	 */
	public static MessageModel getFriendsIDServer(MessageModel messageModel) {
		MessageHead requestMessageHead = messageModel.getMessageHead();
//		MessageContext loginMessageContext = messageModel.getMessageContext();
		
		String requestDescribe = requestMessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);
		
		String[] getFriendIDParameters = requestDescribe.split("\\?");
		
		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符?), 包含则替换相应字符
		if (getFriendIDParameters.length != 2) {
			System.err.println("parameter excetion..");
			//TODO
			return null;
		}
		
		MessageModel responesMessageModel = null;
		
		for (Entry<String, MessageModel> friendsIDServerCache : $getFriendsIDServer_Cache.entrySet()) {
			
			if(friendsIDServerCache.getKey().equals(getFriendIDParameters[1])) {
				
				responesMessageModel = friendsIDServerCache.getValue();

				responesMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
				responesMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
				responesMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
				
				return responesMessageModel;
			}
			
		}
		
		Map<String, String> parameters;
		String userID = null;
		String group = null;
		
		try {
			parameters = TransmitTool.analysisRequestParameters(getFriendIDParameters[1]);
			
			userID = parameters.get("userID");
			group = parameters.get("group");
			
		} catch (RequestParameterExcetion e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			return null;
		}finally {
			if (ObjectTool.isNull(userID) 
					|| !ObjectTool.isInteger(userID)
					|| ObjectTool.isNull(group)) {
				System.err.println("get parameter error..");
				return null;
			}
		}
		
		List<Friend> friendsInfoList = EvenProcess.getFriendInfo(Integer.parseInt(userID));
		
		List<Integer> friendsIDList = new ArrayList<>();
		
		for (Friend friend : friendsInfoList) {
			if (friend.getUser_group().equalsIgnoreCase(group)) {
				friendsIDList.add(friend.getId().intValue());
			}
		}
		
		MessageHead responesMessageHead = new MessageHead();
		responesMessageHead.setReplyRequestResult(true);
		responesMessageHead.setReplyTime(System.currentTimeMillis());
		responesMessageHead.setType(2);
		responesMessageHead.setRequestTime(requestMessageHead.getRequestTime());
		responesMessageHead.setRequestNO(requestMessageHead.getRequestNO());
		
		if (ObjectTool.isNull(friendsIDList)) {
			responesMessageHead.setReplyDataType(null);
			responesMessageHead.setReplyDescribe("friendsList is non-existent..");
			
			responesMessageModel = new MessageModel(responesMessageHead, null);
			synchronized ($getFriendsIDServer_Cache) {
				$getFriendsIDServer_Cache.put(getFriendIDParameters[1], responesMessageModel);
			}
			return responesMessageModel;
		}
		
		responesMessageHead.setReplyDataType(List.class);
		responesMessageHead.setReplyDescribe("request success..");
		
		MessageContext responesMessageContext = new MessageContext();
		responesMessageContext.setObject(friendsIDList);
		System.out.println("friedsIDList: " + friendsIDList);
		
		responesMessageModel = new MessageModel(responesMessageHead, responesMessageContext);
		synchronized ($getFriendsIDServer_Cache) {
			$getFriendsIDServer_Cache.put(getFriendIDParameters[1], responesMessageModel);
		}
		
		return responesMessageModel;
	}

	/**
	 * 获得用户好友信息
	 * @param messageModel
	 * @return
	 */
	public static MessageModel getUserFriendInfoServer(MessageModel messageModel) {
		MessageHead requestMessageHead = messageModel.getMessageHead();
//		MessageContext loginMessageContext = messageModel.getMessageContext();
		
		String requestDescribe = requestMessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);
		
		String[] getFriendIDParameters = requestDescribe.split("\\?");
		
		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符?), 包含则替换相应字符
		if (getFriendIDParameters.length != 2) {
			System.err.println("parameter excetion..");
			//TODO
			return null;
		}
		
		MessageModel responesMessageModel = null;
		
		for (Entry<String, MessageModel> friendsIDServerCache : $getUserFriendInfoServer_Cache.entrySet()) {
			
			if(friendsIDServerCache.getKey().equals(getFriendIDParameters[1])) {
				
				responesMessageModel = friendsIDServerCache.getValue();

				responesMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
				responesMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
				responesMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
				
				return responesMessageModel;
			}
			
		}
		
		Map<String, String> parameters;
		String userID = null;
		
		try {
			parameters = TransmitTool.analysisRequestParameters(getFriendIDParameters[1]);
			
			userID = parameters.get("userID");
			
		} catch (RequestParameterExcetion e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			return null;
		}finally {
			if (ObjectTool.isNull(userID) 
					|| !ObjectTool.isInteger(userID)) {
				System.err.println("get parameter error..");
				return null;
			}
		}
		
		UserFriendsInformation userFriendsInfo = EvenProcess.getUserFriendInfo(Integer.parseInt(userID));
		
		MessageHead responesMessageHead = new MessageHead();
		responesMessageHead.setReplyRequestResult(true);
		responesMessageHead.setReplyTime(System.currentTimeMillis());
		responesMessageHead.setType(3);
		responesMessageHead.setRequestTime(requestMessageHead.getRequestTime());
		responesMessageHead.setRequestNO(requestMessageHead.getRequestNO());
		
		if (ObjectTool.isNull(userFriendsInfo)) {
			responesMessageHead.setReplyDataType(null);
			responesMessageHead.setReplyDescribe("userFriendsInfo is non-existent..");
			
			responesMessageModel = new MessageModel(responesMessageHead, null);
			synchronized ($getFriendsIDServer_Cache) {
				$getFriendsIDServer_Cache.put(getFriendIDParameters[1], responesMessageModel);
			}
			return responesMessageModel;
		}
		
		responesMessageHead.setReplyDataType(List.class);
		responesMessageHead.setReplyDescribe("request success..");
		
		MessageContext responesMessageContext = new MessageContext();
		responesMessageContext.setObject(userFriendsInfo);
		System.out.println("userFriendsInfo: " + userFriendsInfo);
		
		responesMessageModel = new MessageModel(responesMessageHead, responesMessageContext);
		synchronized ($getFriendsIDServer_Cache) {
			$getFriendsIDServer_Cache.put(getFriendIDParameters[1], responesMessageModel);

			synchronized (userFriendImage_Cache) {
				userFriendImage_Cache.put(userFriendsInfo.getId().toString(), userFriendsInfo.getUserImagepath());
			}
		}
		
		return responesMessageModel;
	}

	public static ResponesImage getUserFriendImageServer(MessageModel messageModel) {
		MessageHead requestessageHead = messageModel.getMessageHead();
		
		String requestDescribe = requestessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);
		
		String[] getUserFriendImageParameters = requestDescribe.split("\\?");
		
		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符?), 包含则替换相应字符
		if (getUserFriendImageParameters.length != 2) {
			System.err.println("parameter excetion..");
			//TODO
			return null;
		}
		
		ResponesImage responesImage = null;
		byte[] responesbyteArray = null;
		
		for (Entry<String, byte[]> userFriendsImageServerCache : $getUserFriendImageServer_Cache.entrySet()) {
			
			if(userFriendsImageServerCache.getKey().equals(getUserFriendImageParameters[1])) {
				
				responesbyteArray = userFriendsImageServerCache.getValue();
				
				responesImage = new ResponesImage(
						userFriendsImageServerCache.getKey(), responesbyteArray);
				return responesImage;
			}
			
		}
		
		
		Map<String, String> parameters;
		String userID = null;
		
		try {
			parameters = TransmitTool.analysisRequestParameters(getUserFriendImageParameters[1]);
			
			userID = parameters.get("userID");
			
		} catch (RequestParameterExcetion e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			return null;
		}finally {
			if (ObjectTool.isNull(userID) 
					|| !ObjectTool.isInteger(userID)) {
				System.err.println("get parameter error..");
				return null;
			}
		}
		
		String imagePath = null;
		for (Entry<String, String> userFriendImage : userFriendImage_Cache.entrySet()) {
			
			if(userFriendImage.getKey().equals(userID)) {
				imagePath = userFriendImage.getValue();
				break;
			}
			
		}
		
		if (ObjectTool.isNull(imagePath)) {
			
			UserFriendsInformation userFriendsInfo = EvenProcess.getUserFriendInfo(Integer.parseInt(userID));
			imagePath = userFriendsInfo.getUserImagepath();
			
			synchronized (userFriendImage_Cache) {
				userFriendImage_Cache.put(userFriendsInfo.getId().toString(), imagePath);
			}
		}
		
		if (ObjectTool.isNull(imagePath)) {
			return null;
		}
		
		byte[] imageByte = null;
		FileImageInputStream fileImageInputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		try {			
			fileImageInputStream = new FileImageInputStream(new File(imagePath));
			byteArrayOutputStream = new ByteArrayOutputStream();
			byte[] bufByte = new byte[1024];
			int imageByteLength = -1;
			
			while ((imageByteLength = fileImageInputStream.read(bufByte)) != -1) {
				
				byteArrayOutputStream.write(bufByte, 0, imageByteLength);
			}
			
//			while ((imageByteLength = fileImageInputStream.read()) != -1) {
//				
//				byteArrayOutputStream.write(imageByteLength);
//			}
			
			imageByte = byteArrayOutputStream.toByteArray();
			
			synchronized($getUserFriendImageServer_Cache) {
				$getUserFriendImageServer_Cache.put(userID, imageByte);
			}
					
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if (!ObjectTool.isNull(byteArrayOutputStream)) byteArrayOutputStream.close();
				if (!ObjectTool.isNull(fileImageInputStream)) fileImageInputStream.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		responesImage = new ResponesImage(userID, imageByte);
		return responesImage;
	}
}
