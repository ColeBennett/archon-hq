package net.thearchon.hq.service.votifier;

import net.thearchon.hq.Archon;
import net.thearchon.hq.service.AbstractService;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;

public class VoteReceiver extends AbstractService<VoteListener> implements Runnable {
	
	private KeyPair keyPair;
	private ServerSocket server;
	private Thread thread;
	private volatile boolean running;

	@Override
	public void initialize() {
		File dir = new File("data/votifier");
		try {
			if (!dir.exists()) {
				dir.mkdir();
				keyPair = generate(2048);
				save(dir, keyPair);
			} else {
				keyPair = load(dir);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!running) {
			try {
				server = new ServerSocket();
				server.bind(new InetSocketAddress(8192));
				thread = new Thread(this, "VoteReceiver");
				running = true;
				thread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void shutdown() {
		if (running) {
			running = false;
			try {
				server.close();
				server = null;
				thread.join();
				thread = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				Socket socket = server.accept();
				socket.setSoTimeout(5000);
				BufferedWriter out = new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream()));
				InputStream in = socket.getInputStream();

				out.write("VOTIFIER 1.9");
				out.newLine();
				out.flush();

				byte[] block = new byte[256];
				in.read(block, 0, block.length);

                try {
                    block = decrypt(block, keyPair.getPrivate());
                } catch (Exception e) {
                    Archon.getInstance().getLogger().log(Level.SEVERE, "Failed to decrypt block from " + socket.getInetAddress().getHostAddress());
                    continue;
                }

				int position = 0;

				String opcode = readString(block, position);
				position += opcode.length() + 1;
				if (!opcode.equals("VOTE")) {
					throw new IOException("Unable to decode RSA");
				}

				String serviceName = readString(block, position);
				position += serviceName.length() + 1;
				String username = readString(block, position);
				position += username.length() + 1;
				String address = readString(block, position);
				position += address.length() + 1;
				String timeStamp = readString(block, position);
				position += timeStamp.length() + 1;

				Vote vote = new Vote(serviceName, username, address, timeStamp);
				Archon.getInstance().runTask(() -> {
                    for (VoteListener listener : getListeners()) {
                        try {
                            listener.voteMade(vote);
                        } catch (Exception e) {
                            Archon.getInstance().getLogger().log(Level.WARNING, "Failed to handle vote", e);
                        }
                    }
                });

				out.close();
				in.close();
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String readString(byte[] data, int offset) {
		StringBuilder buf = new StringBuilder();
		for (int i = offset; i < data.length; i++) {
			if (data[i] == '\n') {
				break;
			}
			buf.append((char) data[i]);
		}
		return buf.toString();
	}

	private byte[] decrypt(byte[] data, PrivateKey key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(data);
	}

	private KeyPair generate(int bits) throws Exception {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(bits,
				RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);
		return keygen.generateKeyPair();
	}

	private void save(File directory, KeyPair keyPair) throws Exception {
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();

		// Store the public key.
		X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(
				publicKey.getEncoded());
		FileOutputStream out = new FileOutputStream(directory + "/public.key");
		out.write(DatatypeConverter.printBase64Binary(publicSpec.getEncoded())
				.getBytes());
		out.close();

		// Store the private key.
		PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
		out = new FileOutputStream(directory + "/private.key");
		out.write(DatatypeConverter.printBase64Binary(privateSpec.getEncoded())
				.getBytes());
		out.close();
	}

	private KeyPair load(File directory) throws Exception {
		// Read the public key file.
		File publicKeyFile = new File(directory + "/public.key");
		FileInputStream in = new FileInputStream(directory + "/public.key");
		byte[] encodedPublicKey = new byte[(int) publicKeyFile.length()];
		in.read(encodedPublicKey);
		encodedPublicKey = DatatypeConverter.parseBase64Binary(new String(
				encodedPublicKey));
		in.close();

		// Read the private key file.
		File privateKeyFile = new File(directory + "/private.key");
		in = new FileInputStream(directory + "/private.key");
		byte[] encodedPrivateKey = new byte[(int) privateKeyFile.length()];
		in.read(encodedPrivateKey);
		encodedPrivateKey = DatatypeConverter.parseBase64Binary(new String(
				encodedPrivateKey));
		in.close();

		// Instantiate and return the key pair.
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return new KeyPair(publicKey, privateKey);
	}
}
