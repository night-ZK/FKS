package transmit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import customexception.RequestParameterExcetion;
import db.EvenProcess;
import message.MessageContext;
import message.MessageHead;
import message.MessageModel;
import model.UpdateInformation;
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
@SuppressWarnings("Duplicates")
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
		MessageModel responesMessageModel = null;

		String requestDescribe = requestMessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);

		String[] loginparameters = requestDescribe.split("\\?");

		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符?), 包含则替换相应字符
		if (loginparameters.length != 2) {
			System.err.println("parameter excetion..");
			//TODO
			return null;
		}


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
		
		MessageHead responseMessageHead = new MessageHead();
		responseMessageHead.setReplyRequestResult(true);
		responseMessageHead.setType(1);
		responseMessageHead.setReplyTime(System.currentTimeMillis());
		responseMessageHead.setRequestTime(requestMessageHead.getRequestTime());
		responseMessageHead.setRequestNO(requestMessageHead.getRequestNO());
		
		if (ObjectTool.isNull(loginUser.getId())) {
			responseMessageHead.setReplyDataType(null);
			responseMessageHead.setReplyDescribe("user is non-existent..");
			responesMessageModel = new MessageModel(responseMessageHead, null);
			synchronized ($loginServer_Cache) {				
				$loginServer_Cache.put(key, responesMessageModel);
			}
			return responesMessageModel;
		}
		
		responseMessageHead.setReplyDataType(User.class);
		responseMessageHead.setReplyDescribe("request success..");
		
		MessageContext responseMessageContext = new MessageContext();
		ArrayList loginResponseContextList = new ArrayList();
		loginResponseContextList.add(0, loginUser);

		byte[] imageByte = TransmitTool.getImageBytesByPath(loginUser.getUserImagepath());
		loginResponseContextList.add(1, imageByte);

		synchronized ($getUserFriendImageServer_Cache) {
			$getUserFriendImageServer_Cache.put(loginUser.getId()+"", imageByte);
		}

		responseMessageContext.setObject(loginResponseContextList);
		responesMessageModel = new MessageModel(responseMessageHead, responseMessageContext);
		synchronized ($loginServer_Cache) {				
			$loginServer_Cache.put(key, responesMessageModel);
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
		MessageModel responseMessageModel = null;

		String requestDescribe = requestMessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);

		String[] getFriendIDParameters = requestDescribe.split("\\?");

		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符?), 包含则替换相应字符
		if (getFriendIDParameters.length != 2) {
			System.err.println("parameter exception..");
			//TODO
			return null;
		}


		String key = getFriendIDParameters[1];
		if ($getFriendsIDServer_Cache.containsKey(key)) {
			responseMessageModel = $getFriendsIDServer_Cache.get(key);
			
			responseMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
			responseMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
			responseMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
			return responseMessageModel;
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
		
		MessageHead responseMessageHead = new MessageHead();
		responseMessageHead.setReplyRequestResult(true);
		responseMessageHead.setReplyTime(System.currentTimeMillis());
		responseMessageHead.setType(2);
		responseMessageHead.setRequestTime(requestMessageHead.getRequestTime());
		responseMessageHead.setRequestNO(requestMessageHead.getRequestNO());
		
		if (ObjectTool.isNull(friendsIDList)) {
			responseMessageHead.setReplyDataType(null);
			responseMessageHead.setReplyDescribe("friendsList is non-existent..");
			
			responseMessageModel = new MessageModel(responseMessageHead, null);
			synchronized ($getFriendsIDServer_Cache) {
				$getFriendsIDServer_Cache.put(getFriendIDParameters[1], responseMessageModel);
			}
			return responseMessageModel;
		}
		
		responseMessageHead.setReplyDataType(List.class);
		responseMessageHead.setReplyDescribe("request success..");
		
		MessageContext responseMessageContext = new MessageContext();
		responseMessageContext.setObject(friendsIDList);
		System.out.println("friendsIDList: " + friendsIDList);
		
		responseMessageModel = new MessageModel(responseMessageHead, responseMessageContext);
		synchronized ($getFriendsIDServer_Cache) {
			$getFriendsIDServer_Cache.put(getFriendIDParameters[1], responseMessageModel);
		}
		
		return responseMessageModel;
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
		
		MessageContext responseMessageContext = new MessageContext();
		responseMessageContext.setObject(userFriendsInfo);
		System.out.println("userFriendsInfo: " + userFriendsInfo);
		
		responesMessageModel = new MessageModel(responesMessageHead, responseMessageContext);
		synchronized ($getUserFriendInfoServer_Cache) {
			$getUserFriendInfoServer_Cache.put(getFriendIDParameters[1], responesMessageModel);

			synchronized (userFriendImage_Cache) {
				userFriendImage_Cache.put(userFriendsInfo.getId().toString(), userFriendsInfo.getUserImagepath());
			}
		}
		
		return responesMessageModel;
	}

	public static ResponseImage getUserFriendImageServer(MessageModel messageModel) {
		MessageHead requestMessageHead = messageModel.getMessageHead();
		
		String requestDescribe = requestMessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);
		
		String[] getUserFriendImageParameters = requestDescribe.split("\\?");
		
		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符?), 包含则替换相应字符
		if (getUserFriendImageParameters.length != 2) {
			System.err.println("parameter excetion..");
			//TODO
			return null;
		}
		
		ResponseImage responseImage = null;
		byte[] responseByteArray = null;

		String key = getUserFriendImageParameters[1];
		if ($getUserFriendImageServer_Cache.containsKey(key)) {
			responseByteArray = $getUserFriendImageServer_Cache.get(key);
			
			responseImage = new ResponseImage(key, responseByteArray);
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
		
		byte[] imageByte = TransmitTool.getImageBytesByPath(imagePath);

		synchronized($getUserFriendImageServer_Cache) {
			$getUserFriendImageServer_Cache.put(userID, imageByte);
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
		
		MessageModel responseMessageModel;

//		String key = getFriendIDParameters[1];
//		if ($getUserFriendInfoListServer_Cache.containsKey(key)) {
//			responseMessageModel = $getUserFriendInfoListServer_Cache.get(key);
//
//			responseMessageModel.getMessageHead().setReplyTime(System.currentTimeMillis());
//			responseMessageModel.getMessageHead().setRequestTime(requestMessageHead.getRequestTime());
//			responseMessageModel.getMessageHead().setRequestNO(requestMessageHead.getRequestNO());
//
//			return responseMessageModel;
//		}
		
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
				
//				if(userFriendsInformation_Cache.containsKey(id)) {
//					userFriendsInfo = userFriendsInformation_Cache.get(id);
//				}else {
//					userFriendsInfo = EvenProcess.getUserFriendInfo(id);
//				}
                userFriendsInfo = EvenProcess.getUserFriendInfo(id);
				userFriendsInfoList.add(userFriendsInfo);
			}
		}
		
		MessageHead responseMessageHead = new MessageHead();
		responseMessageHead.setReplyRequestResult(true);
		responseMessageHead.setReplyTime(System.currentTimeMillis());
		responseMessageHead.setType(6);
		responseMessageHead.setRequestTime(requestMessageHead.getRequestTime());
		responseMessageHead.setRequestNO(requestMessageHead.getRequestNO());
		responseMessageHead.setRequestDescribe("userId:" + userID);

		if (ObjectTool.isNull(userFriendsInfoList)) {
			responseMessageHead.setReplyDataType(null);
			responseMessageHead.setReplyDescribe("friendsList is non-existent..");
			
			responseMessageModel = new MessageModel(responseMessageHead, null);
			synchronized ($getUserFriendInfoListServer_Cache) {
				$getUserFriendInfoListServer_Cache.put(getFriendIDParameters[1], responseMessageModel);
			}
			return responseMessageModel;
		}
		
		responseMessageHead.setReplyDataType(List.class);
		responseMessageHead.setReplyDescribe("request success..");
		
		MessageContext responseMessageContext = new MessageContext();
		responseMessageContext.setObject(userFriendsInfoList);
		System.out.println("friendsIDList: " + userFriendsInfoList);
		
		responseMessageModel = new MessageModel(responseMessageHead, responseMessageContext);
		synchronized ($getUserFriendInfoListServer_Cache) {
			$getUserFriendInfoListServer_Cache.put(getFriendIDParameters[1], responseMessageModel);
		}
		
		return responseMessageModel;
	}

	public static MessageModel updateUserInformationServer(MessageModel messageModel) {
		MessageHead requestMessageHead = messageModel.getMessageHead();

		String requestDescribe = requestMessageHead.getRequestDescribe();
		System.out.println("requestDescribe: " + requestDescribe);

		MessageModel responseMessageModel = null;


		UpdateInformation updateInformation = (UpdateInformation) messageModel.getMessageContext().getObject();
		User userInfo = updateInformation.getUserInfo();
		int id = userInfo.getId().intValue();
		String path = "../resources/iconCache/" +  id + ".png";

//		Runnable saveImage = ()->{
//		};
//		ThreadConsole.useThreadPool().execute(saveImage);
		boolean saveIsSuccess = TransmitTool.saveImage(updateInformation.getIconBytes(), path);

		boolean isUpdateSuccess = false;
		if (saveIsSuccess){
			userInfo.setUserImagepath(path);

			isUpdateSuccess = EvenProcess.updateUserInfo(id,
					userInfo.getUserName(),
					userInfo.getPassWord(),
					userInfo.getUserNick(),
					userInfo.getUserImagepath(),
					userInfo.getUserState(),
					userInfo.getGender().intValue(),
					userInfo.getPersonLabel());
		}

		User user = null;
		if(isUpdateSuccess){
			user = EvenProcess.getUserInfo(id);
			String key = "userName=" + user.getUserName() + "&" + "password=" + user.getPassWord();
			if($loginServer_Cache.containsKey(key)){
				$loginServer_Cache.remove(key);
			}
		}

		MessageHead responseMessageHead = new MessageHead();
		responseMessageHead.setReplyRequestResult(true);
		responseMessageHead.setReplyTime(System.currentTimeMillis());
		responseMessageHead.setType(8);
		responseMessageHead.setRequestTime(requestMessageHead.getRequestTime());
		responseMessageHead.setRequestNO(requestMessageHead.getRequestNO());

		responseMessageHead.setReplyDataType(String.class);

		responseMessageHead.setReplyDescribe(isUpdateSuccess ? "1" : "0");

//		MessageContext responseMessageContext = new MessageContext();
//		responseMessageContext.setObject(user);

		responseMessageModel = new MessageModel(responseMessageHead, null);

		return responseMessageModel;
	}

	public static void signOut(int id){

		EvenProcess.updateUserInfo(id,
				null,
				null,
				null,
				null,
				"2",
				null,
				null);
	}


	public static void signIn(int id){

		EvenProcess.updateUserInfo(id,
				null,
				null,
				null,
				null,
				"0",
				null,
				null);
	}

	public static MessageModel getSignInfoRemindModel(String onOrOff, int id){
		MessageHead responseMessageHead = new MessageHead();
		responseMessageHead.setReplyRequestResult(true);
		responseMessageHead.setReplyTime(System.currentTimeMillis());
		responseMessageHead.setType(9);
//		responseMessageHead.setRequestTime(requestMessageHead.getRequestTime());
//		responseMessageHead.setRequestNO(requestMessageHead.getRequestNO());

		responseMessageHead.setReplyDataType(Object.class);
		responseMessageHead.setReplyDescribe(onOrOff + ":" + id);
		MessageModel responseMessageModel = new MessageModel(responseMessageHead, null);
		return responseMessageModel;
	}
}
