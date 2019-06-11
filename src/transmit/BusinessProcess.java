package transmit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.stream.FileImageInputStream;

import customexception.RequestParameterExcetion;
import db.EvenProcess;
import message.ChatMessages;
import message.MessageContext;
import message.MessageHead;
import message.MessageModel;
import tablebeans.Friend;
import tablebeans.User;
import tablejson.ResponseImage;
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
	
	private static Map<String, MessageModel> $getUserFriendInfoListServer_Cache = 
			new HashMap<>();
	
	//key: userID value: imageByte
	private static Map<String, byte[]> $getUserFriendImageServer_Cache = 
			new HashMap<>();
	
	//TODO 查询结果缓存移动到查询类中
	//key: userID value: imagePath
	private static Map<String, String> userFriendImage_Cache = 
			new HashMap<>();
	//key: userID value: UserFriendsInformation
	private static Map<Integer, UserFriendsInformation> userFriendsInformation_Cache = 
			new HashMap<>();
	
	private static Map<Integer, List<Friend>> friendsInfoList_Cache = 
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

		String key = loginparameters[1];
		if ($loginServer_Cache.containsKey(key)) {
			responesMessageModel = $loginServer_Cache.get(key);
			
			responesMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
			responesMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
			responesMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
			return responesMessageModel;
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
		
//		for (Entry<String, MessageModel> friendsIDServerCache : $getFriendsIDServer_Cache.entrySet()) {
//			
//			if(friendsIDServerCache.getKey().equals(getFriendIDParameters[1])) {
//				
//				responesMessageModel = friendsIDServerCache.getValue();
//
//				responesMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
//				responesMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
//				responesMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
//				
//				return responesMessageModel;
//			}
//			
//		}
		
		String key = getFriendIDParameters[1];
		if ($getFriendsIDServer_Cache.containsKey(key)) {
			responesMessageModel = $getFriendsIDServer_Cache.get(key);
			
			responesMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
			responesMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
			responesMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
			return responesMessageModel;
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
		synchronized (friendsInfoList_Cache) {
			friendsInfoList_Cache.put(Integer.parseInt(userID), friendsInfoList);
		}
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
		
//		for (Entry<String, MessageModel> friendsIDServerCache : $getUserFriendInfoServer_Cache.entrySet()) {
//			
//			if(friendsIDServerCache.getKey().equals(getFriendIDParameters[1])) {
//				
//				responesMessageModel = friendsIDServerCache.getValue();
//
//				responesMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
//				responesMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
//				responesMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
//				
//				return responesMessageModel;
//			}
//			
//		}
		

		String key = getFriendIDParameters[1];
		if ($getUserFriendInfoServer_Cache.containsKey(key)) {
			responesMessageModel = $getUserFriendInfoServer_Cache.get(key);
			
			responesMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
			responesMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
			responesMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
			return responesMessageModel;
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
		
		synchronized (userFriendsInformation_Cache) {
			userFriendsInformation_Cache.put(Integer.valueOf(userID), userFriendsInfo);
		}
		
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
			synchronized ($getUserFriendInfoServer_Cache) {
				$getUserFriendInfoServer_Cache.put(getFriendIDParameters[1], responesMessageModel);
			}
			return responesMessageModel;
		}
		
		responesMessageHead.setReplyDataType(List.class);
		responesMessageHead.setReplyDescribe("request success..");
		
		MessageContext responesMessageContext = new MessageContext();
		responesMessageContext.setObject(userFriendsInfo);
		System.out.println("userFriendsInfo: " + userFriendsInfo);
		
		responesMessageModel = new MessageModel(responesMessageHead, responesMessageContext);
		synchronized ($getUserFriendInfoServer_Cache) {
			$getUserFriendInfoServer_Cache.put(getFriendIDParameters[1], responesMessageModel);

			synchronized (userFriendImage_Cache) {
				userFriendImage_Cache.put(userFriendsInfo.getId().toString(), userFriendsInfo.getUserImagepath());
			}
		}
		
		return responesMessageModel;
	}

	public static ResponseImage getUserFriendImageServer(MessageModel messageModel) {
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
		
		ResponseImage responseImage = null;
		byte[] responesbyteArray = null;

		String key = getUserFriendImageParameters[1];
		if ($getUserFriendImageServer_Cache.containsKey(key)) {
			responesbyteArray = $getUserFriendImageServer_Cache.get(key);
			
			responseImage = new ResponseImage(key, responesbyteArray);
			return responseImage;
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
		if (userFriendImage_Cache.containsKey(userID)) {
			imagePath = userFriendImage_Cache.get(userID.toString());
		}
		
		if (ObjectTool.isNull(imagePath)) {
			int userIDKey = Integer.parseInt(userID);
			UserFriendsInformation userFriendsInfo;
			if (userFriendsInformation_Cache.containsKey(userIDKey)) {
				userFriendsInfo = userFriendsInformation_Cache.get(userIDKey);
			}else {
				userFriendsInfo = EvenProcess.getUserFriendInfo(userIDKey);
			}
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
		
		responseImage = new ResponseImage(userID, imageByte);
		return responseImage;
	}

	public static MessageModel getUserFriendInfoListServer(MessageModel messageModel) {
		MessageHead requestMessageHead = messageModel.getMessageHead();
		
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

		String key = getFriendIDParameters[1];
		if ($getUserFriendInfoListServer_Cache.containsKey(key)) {
			responesMessageModel = $getUserFriendInfoListServer_Cache.get(key);
			
			responesMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
			responesMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
			responesMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
			
			return responesMessageModel;
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
		
		List<UserFriendsInformation> userFriendsInfoList = new ArrayList<>();
		
		for (Friend friend : friendsInfoList) {
			if (friend.getUser_group().equalsIgnoreCase(group)) {
				int id = friend.getFriend_id().intValue();
				UserFriendsInformation userFriendsInfo;
				
				if(userFriendsInformation_Cache.containsKey(id)) {
					userFriendsInfo = userFriendsInformation_Cache.get(id);
				}else {					
					userFriendsInfo = EvenProcess.getUserFriendInfo(id);
				}
				userFriendsInfoList.add(userFriendsInfo);
			}
		}
		
		MessageHead responesMessageHead = new MessageHead();
		responesMessageHead.setReplyRequestResult(true);
		responesMessageHead.setReplyTime(System.currentTimeMillis());
		responesMessageHead.setType(6);
		responesMessageHead.setRequestTime(requestMessageHead.getRequestTime());
		responesMessageHead.setRequestNO(requestMessageHead.getRequestNO());
		
		if (ObjectTool.isNull(userFriendsInfoList)) {
			responesMessageHead.setReplyDataType(null);
			responesMessageHead.setReplyDescribe("friendsList is non-existent..");
			
			responesMessageModel = new MessageModel(responesMessageHead, null);
			synchronized ($getUserFriendInfoListServer_Cache) {
				$getUserFriendInfoListServer_Cache.put(getFriendIDParameters[1], responesMessageModel);
			}
			return responesMessageModel;
		}
		
		responesMessageHead.setReplyDataType(List.class);
		responesMessageHead.setReplyDescribe("request success..");
		
		MessageContext responesMessageContext = new MessageContext();
		responesMessageContext.setObject(userFriendsInfoList);
		System.out.println("friedsIDList: " + userFriendsInfoList);
		
		responesMessageModel = new MessageModel(responesMessageHead, responesMessageContext);
		synchronized ($getUserFriendInfoListServer_Cache) {
			$getUserFriendInfoListServer_Cache.put(getFriendIDParameters[1], responesMessageModel);
		}
		
		return responesMessageModel;
	}

	public static MessageModel chatServer(MessageModel messageModel) {
		MessageHead requestMessageHead = messageModel.getMessageHead();

		String requestDescribe = requestMessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);

		String[] getFriendIDParameters = requestDescribe.split("\\?");

		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符?), 包含则替换相应字符

		MessageContext messageContext = (ChatMessages) messageModel.getMessageContext();
		MessageContext responseMessageContext = new ChatMessages();


		return  null;
	}
}
