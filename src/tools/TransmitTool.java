package tools;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import customexception.RequestParameterExcetion;
import message.*;
import tablejson.ResponseImage;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

public class TransmitTool {

	private static String charsetName;
	static{
		 charsetName = "UTF-8";
	}
	/**
	 * 获取requestMap的key值
	 * @param messageHead
	 * @return
	 */
	public static String getRequestMapKey(MessageHead messageHead) {
		
		Integer headType = messageHead.getType();
		Long sendTime = messageHead.getRequestTime();

		return "Type: " + headType + ", sendTime: " + sendTime; 
		
	}
	
	/**
	 * 对象转化成byte[]
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static byte[] ObjectToByteArrays(Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		
		byte[] messageModelByteArrays = baos.toByteArray();
		
		if(!ObjectTool.isNull(oos)) oos.close();
		if(!ObjectTool.isNull(baos)) baos.close();
		
		return messageModelByteArrays;
	}
	
	/**
	 * byte[]转对象
	 * @param bs
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public static Object byteArraysToObject(byte[] bs) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bs);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object o = ois.readObject();
		
		if(!ObjectTool.isNull(ois)) ois.close();
		if(!ObjectTool.isNull(bais)) bais.close();
		
		return o;
	}
	
	/**
	 * 对象转json
	 * @param ob
	 * @return
	 */
	public static String objectToJson(Object ob) {
		String json = "class: " + ob.getClass().getName() + ";" 
				+ ob.toString();
		return json;
	}
	
	/**
	 * json转对象
	 * @param json
	 * @return
	 */
	public static Object jsonToObject(String json) {
		String[] rs = json.split(";");
		String className = rs[0].split(":")[1].trim();
		try {
			Class<?> cl = Class.forName(className);
			Object o = cl.newInstance();
			String[] fileds = getFields(rs[1]);		
			for(String filed : fileds) {
				String[] kv = filed.split("=");
				String needField = kv[0].trim();
				Field field = cl.getDeclaredField(needField);
				Class<?> fieldType = field.getType();
				field.setAccessible(true);
				field.set(o, fieldType.cast(kv[0]));
			}
			return o;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 设置对象属性
	 * @param cl
	 * @param subJson
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	public static Object setObject(Class<?> cl, String subJson) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {

		Object o = cl.newInstance();
		String[] fileds = getFields(subJson);		
		for(String filed : fileds) {
			String[] kv = filed.split("=");
			String needField = kv[0].trim();
			Field field = cl.getDeclaredField(needField);
			Class<?> fieldType = field.getType();
			field.setAccessible(true);
			field.set(o, fieldType.cast(kv[0]));
		}
		return o;
		
//		Map<String, String> filedMap = new HashMap<>();
		
//		for (Entry<String, String> fm : filedMap.entrySet()) {
//			String needField = fm.getKey();
//			Field field = cl.getDeclaredField(needField);
//			field.setAccessible(true);
//			field.set(o, fm.getValue());	
//		}		
	}

	/**
	 * 获得表对象中的所有字段, 通过“,”拆分, 并返回一个String类型的数组
	 * @param subJson 表对象
	 * @return 字段数组
	 */
	public static String[] getFields(String subJson){
		String needRowString = subJson.substring(subJson.indexOf("[")+1, subJson.indexOf("]"));
		return needRowString.split(",");
	}

	public static Map<String, String> analysisRequestParameters(String subRequest) throws RequestParameterExcetion {
		Map<String, String> parametersMap = new HashMap<>();
		String[] parameters = subRequest.split("&");
		
		//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符&), 包含则替换相应字符
		
		for (String parameter : parameters) {
			String[] pArrays = parameter.split("=");
			
			if (pArrays.length != 2) 
				throw new RequestParameterExcetion("request parameter set error..");
			
			//TODO 检测parameter中是否包含"时间戳-type:"(拆分用特殊字符=), 包含则替换相应字符
			parametersMap.put(pArrays[0].trim(), pArrays[1]);
			
		}
		
		return parametersMap;
	}

	public static ByteBuffer sendResponseMessage(MessageInterface responseMessageModel) throws IOException {
		String responseLine = "";
		String imageDescribe = "";
		boolean isImage = false;
		byte[] responseByteArrays = null;
		if(responseMessageModel instanceof MessageModel){
			responseLine = "state:200 length:100 type:MessageModel";
			responseByteArrays = ObjectToByteArrays(responseMessageModel);

			if (((MessageModel) responseMessageModel).getMessageContext() instanceof ChatMessages){
				responseLine = "state:200 length:100 type:ChatMessage";
			}

		}else if(responseMessageModel instanceof ResponseImage){
			ResponseImage responseImage = (ResponseImage)responseMessageModel;
			responseByteArrays = responseImage.getImageByte();
			//responseImage.getImageDescribe() : userID
			imageDescribe = "imageName:" + responseImage.getImageDescribe() + " imageSize:"
					+ responseByteArrays.length;
			int imageDescribeLength = imageDescribe.length();
			responseLine = "state:200 length:" + imageDescribeLength + " type:Image" + " existJson:true";
			isImage = true;
		}

		byte[] responseLineByte = responseLine.getBytes(charsetName);
		byte[] imageDescribeByte = imageDescribe.getBytes(charsetName);
		int byteBufferLength = responseLineByte.length + 4;
		if (isImage) {
			byteBufferLength += imageDescribeByte.length + 4 + responseByteArrays.length;
		}else{
			byteBufferLength += responseByteArrays.length + 4;
		}
		ByteBuffer responseByteBuffer = ByteBuffer.allocate(byteBufferLength);
		sendRule(responseByteBuffer, responseLineByte);

		if(!ObjectTool.isNull(imageDescribe) && isImage) {
			sendRule(responseByteBuffer, imageDescribeByte);
			responseByteBuffer.put(responseByteArrays);
		}else{
			sendRule(responseByteBuffer, responseByteArrays);
		}
		responseByteBuffer.flip();
		return responseByteBuffer;
	}

	/**
	 * 流转化成byte[] -- nio用
	 * @param socketChannel
	 * @return
	 * @throws IOException
	 */
	public static byte[] channelSteamToByteArraysForNIO(SocketChannel socketChannel) throws IOException {
		//读取缓冲区
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        //从通道中读数据到缓冲区
        socketChannel.read(byteBuffer);
        byteBuffer.flip();
        int length = byteBuffer.getInt();
//        System.out.println("length:" + length);
        byteBuffer = ByteBuffer.allocate(length);
		int readLength = 0;
		ByteBuffer readByteBuffer;
        while(readLength < length){
			readByteBuffer = ByteBuffer.allocate(length - readLength);
			readLength += socketChannel.read(readByteBuffer);
			readByteBuffer.flip();
			byteBuffer.put(readByteBuffer.array());
		}
//		int readLength = socketChannel.read(byteBuffer);
//		System.out.println("readLength:" + readLength);
		byteBuffer.flip();

		return byteBuffer.array();
	}

	private static void sendRule(ByteBuffer responseByteBufferArrays, byte[] responseByteArrays) {
		responseByteBufferArrays.put((byte)(responseByteArrays.length >> 24));
		responseByteBufferArrays.put((byte)(responseByteArrays.length >> 16));
		responseByteBufferArrays.put((byte)(responseByteArrays.length >> 8));
		responseByteBufferArrays.put((byte)responseByteArrays.length);
		responseByteBufferArrays.put(responseByteArrays);
	}

    /**
     * 通过发送规则发送
     * 规则: 4个字节作为消息长度
     * @param responseModelByteArrays
     * @return
     */
    public static ByteBuffer sendResponseForNIDByRule(byte[] responseModelByteArrays) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(responseModelByteArrays.length + 4);

        byteBuffer.put((byte)(responseModelByteArrays.length >> 24));
        byteBuffer.put((byte)(responseModelByteArrays.length >> 16));
        byteBuffer.put((byte)(responseModelByteArrays.length >> 8));
        byteBuffer.put((byte)responseModelByteArrays.length);

        byteBuffer.put(responseModelByteArrays);
        byteBuffer.flip();
        return byteBuffer;
    }

    public static byte[] getImageBytesByPath(String path){
		byte[] imageByte = null;
		FileImageInputStream fileImageInputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		try {
			fileImageInputStream = new FileImageInputStream(new File(path));
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

		}catch (IOException e){
			e.printStackTrace();
		}finally {
			try {
				if (!ObjectTool.isNull(byteArrayOutputStream)) byteArrayOutputStream.close();
				if (!ObjectTool.isNull(fileImageInputStream)) fileImageInputStream.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		return  imageByte;
	}

	public static boolean saveImage(byte[] iconBytes, String path) {

		File iFile = new File(path);
		FileImageOutputStream fio = null;

		boolean saveIsSuccess = false;
		try {
			if(!iFile.exists()) {
				iFile.getParentFile().mkdirs();
				iFile.createNewFile();
			}
			fio = new FileImageOutputStream(iFile);
			fio.write(iconBytes,0,iconBytes.length);
			saveIsSuccess = true;
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				fio.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return  saveIsSuccess;
	}
}
