package tablejson;

import java.util.Arrays;

import message.MessageInterface;

public class ResponseImage implements MessageInterface {

	private static final long serialVersionUID = 1L;
	
	private String imageDescribe;
	private byte[] imageByte;
	
	public ResponseImage(String imageDescribe, byte[] imageByte) {
		this.imageDescribe = imageDescribe;
		this.imageByte = imageByte;
	}
	public String getImageDescribe() {
		return imageDescribe;
	}
	public void setImageDescribe(String imageDescribe) {
		this.imageDescribe = imageDescribe;
	}
	public byte[] getImageByte() {
		return imageByte;
	}
	public void setImageByte(byte[] imageByte) {
		this.imageByte = imageByte;
	}
	@Override
	public String toString() {
		return "ResponseImage [imageDescribe=" + imageDescribe + ", imageByte=" + Arrays.toString(imageByte) + "]";
	}

}
