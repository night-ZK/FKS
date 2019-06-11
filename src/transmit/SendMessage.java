package transmit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class SendMessage {
//	
//	/**
//	 * 通过userID获得服务器与客户端的Socket, 用于消息的转发
//	 * @param userID作为Key值
//	 * @return 用于转发的Socket
//	 */
//	public static Socket getChatObjectSocket(Integer key_UserId) {
//		return SocketServerNIO.get_saveChatSocketList().get(key_UserId);
//	}
//	
//	public static MessageInterface sendChatMessage(ChatMessages message) {
//		
//		ObjectOutputStream oos = null;
//		ErrerMessage errerMessage = null;
//		try {
//			Socket socket = getChatObjectSocket(message.getGetterID());
//			oos = new ObjectOutputStream(socket.getOutputStream());
//			oos.writeObject(message);
//			oos.flush();
//			errerMessage = new ErrerMessage(true, "Success Send.");
//		} catch (IOException e) {
//			e.printStackTrace();
//			errerMessage = new ErrerMessage(false, e.getMessage());
//		}finally {
//			try {
//				if (oos != null) {
//					oos.close();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//				errerMessage = new ErrerMessage(false, "oos close faile.");
//			}
//		}
//		return errerMessage;
//	}
	
}
