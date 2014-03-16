package dc.client;

import java.util.Arrays;
import java.util.HashMap;

import dc.DCMessage;

public class KeyHandler {
	public static final int KEY_SIZE = DCMessage.PAYLOAD_SIZE;
	
	private HashMap<String, byte[]> keys;
	private byte[] currentKeyMix;
	
	public KeyHandler() {
		keys = new HashMap<String, byte[]>();
		currentKeyMix = new byte[KEY_SIZE];
		Arrays.fill(currentKeyMix, (byte) 0);
	}
	
	public void addKey(String identifier, byte[] key) {
		if(key.length != KEY_SIZE) {
			System.err.println("[KeyHandler] Severe: The provided key has length " + key.length + " but it has to be of length " + KEY_SIZE + ".");
		} else {
			synchronized(keys) {
				keys.put(identifier, key);
				
				synchronized(currentKeyMix) {
					for(int i = 0; i < KEY_SIZE; i++) {
						currentKeyMix[i] ^= key[i];
					}
				}
			}			
		}
	}
	
	public void removeKey(String identifier) {
		synchronized(keys) {
			byte[] oldKey = keys.remove(identifier);
			
			synchronized(currentKeyMix) {
				for(int i = 0; i < KEY_SIZE; i++) {
					currentKeyMix[i] ^= oldKey[i];
				}
			}
		}
	}
	
	public byte[] toOutput(byte[] message) {
		byte[] output = new byte[message.length];
		synchronized(currentKeyMix) {
			for(int i = 0; i < message.length; i++) {
				output[i] = (byte) (message[i] ^ currentKeyMix[i % currentKeyMix.length]);
			}
		}
		return output;
	}
}