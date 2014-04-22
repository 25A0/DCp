package dc;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.InputMismatchException;

public class DCPackage {
	private byte[] header;
	private byte[] payload;

	// NYI
	// private static final int ENTERING = 0, LEAVING = 1;

	// The size of the whole package, in bytes
	public static final int PACKAGE_SIZE = 1024;
	// The number of bytes that make up the header of the package
	public static final int HEADER_SIZE = 1;
	// The size of the payload, in bytes
	public static final int PAYLOAD_SIZE = PACKAGE_SIZE - HEADER_SIZE;

	/**
	 * Creates a new DCPackage for a specific round
	 * @param  number                 The number of the round that this package belongs to
	 * @param  payload                The payload of this package
	 * @throws InputMismatchException If the payload size does not match the expected size
	 */
	public DCPackage(byte number, byte[] payload) throws InputMismatchException {
		if(payload.length > PAYLOAD_SIZE) {
			System.err.println("[DCPackage] Severe: Rejecting input " + 
				String.valueOf(payload) + " since it is too much for a single message.");
			throw new InputMismatchException("Payload size exceeds bounds: Payload size is " + payload.length + " and must at most be " + PAYLOAD_SIZE);
		} else {
			this.payload = payload;
			this.header = makeHeader(number);
		}
	}
	
	/**
	 * Creates a DCPackage from a raw byte array
	 * @param  raw                    The byte array that contains the package
	 * @return                        The package that was constructed from the byte array
	 * @throws InputMismatchException in case that the payload size does not match the expected size.
	 */
	public static DCPackage getPackage(byte[] raw) throws InputMismatchException{
		if(raw.length != PACKAGE_SIZE) {
			throw new InputMismatchException("The size of the raw byte input is " + raw.length+" and does not match the expected package size " + PACKAGE_SIZE);
		} else {
			byte number = raw[0];
			byte[] payload = new byte[PAYLOAD_SIZE];
			int payloadOffset = HEADER_SIZE;
			for(int i = 0; i < PAYLOAD_SIZE; i++) {
				payload[i] = raw[i+payloadOffset];
			}
			return new DCPackage(number, payload);
		}
	}

	/**
	 * Combines this package with another package by XOR-ing the payload.
	 * Note that the returned package is just a reference to this package. The changes are applied to the payload of this package, therefore calling this method makes irrevocable changes to the content of this package.
	 * @param  p The package that is combined with this package
	 * @return   This package. That allows for chaining this method.
	 * @throws InputMismatchException in case the round numbers of the packages don't match.
	 */
	public DCPackage combine(DCPackage p) throws InputMismatchException {
		if(this.getNumber() != p.getNumber()) {
			throw new InputMismatchException("Cannot merge packages: The packages belong to two different rounds");
		} else {
			for(int i = 0; i < PAYLOAD_SIZE; i++) {
				payload[i] ^= p.payload[i];
			}
			return this;
		}
	}

	public byte getNumber() {
		return header[0];
	}

	public byte[] getPayload() {
		return payload;
	}
	
	/**
	 * Creates and returns a byte array that holds the information of the entire package, including the header
	 */
	public byte[] toByteArray() {
		byte[] p = new byte[PACKAGE_SIZE];

		for(int i = 0; i < HEADER_SIZE; i++) {
			p[i] = header[i];
		}

		int payloadOffset = HEADER_SIZE;
		for(int i = 0; i < PAYLOAD_SIZE; i++) {
			p[i + payloadOffset] = payload[i];
		}
		return p;
	}

	public String toString() {
		return String.valueOf(payload);
	}

	private byte[] makeHeader(byte number) {
		byte[] header = new byte[HEADER_SIZE];
		header[0] = number;
		return header;
	}
	
}
