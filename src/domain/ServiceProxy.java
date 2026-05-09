package domain;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

/**
 *  @author Airton
 */
public class ServiceProxy extends AbstractProxy {

	private boolean interrupt = false;
	private Set<String> sentConfigMessages = new HashSet<>(); // Set to track sent messages

	private double serviceTime;
	private boolean targetIsSource;
	private double std;


	public ServiceProxy(String name, Integer localPort, TargetAddress targetAddress, double serviceTime, double std,boolean targetIsSource) {
		this.targetIsSource = targetIsSource;
		this.targetAddress = targetAddress;
		this.proxyName = name;
		this.localPort = localPort;
		this.serviceTime = serviceTime;
		this.std = std;
	}


	@Override
	public void run() {



		new ConnectionEstablishmentOriginThread().start();
		new ConnectionEstablishmentDestinyThread().start();
		System.out.println("Starting "+ proxyName);


		while (true) {
			processAndSendToDestiny();
			if (interrupt)
				break;
		}
	}

	private void processAndSendToDestiny() {
		if (hasSomethingToProcess()){
			double val = (new Random().nextGaussian() * std + serviceTime);
			contentToProcess += System.currentTimeMillis()+";";
			try {Thread.sleep((long) val);} catch (InterruptedException e) {e.printStackTrace();} //SE DESEJADO PODES SUBSTINUIR PELO SEU PROCESSAMENTO ESPECÍFICO

			if (targetIsSource)
				contentToProcess = registerTimeWhenGoOut(contentToProcess);
			sendCurrentContentToDestiny();

			contentToProcess = null;
		}
	}

	private void sendCurrentContentToDestiny() {
		boolean sent = false;
		while (!sent && !interrupt) {
			try {
				ensureDestinyConnection();
				if (targetIsSource) {
					sendMessageToDestiny(contentToProcess + "\n");
					sent = true;
				} else if (isDestinyFree(connectionDestinySocket)) {
					sendMessageToDestiny(contentToProcess + "\n");
					sent = true;
				} else {
					Thread.sleep(100);
				}
			} catch (IOException | ClassNotFoundException e) {
				reconnectToDestiny();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		}
	}

	private void ensureDestinyConnection() throws IOException {
		if (connectionDestinySocket == null || connectionDestinySocket.isClosed()) {
			createConnectionWithDestiny();
		}
	}

	private void reconnectToDestiny() {
		closeDestinyConnection();
		while (!interrupt) {
			try {
				createConnectionWithDestiny();
				return;
			} catch (IOException e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException interruptedException) {
					Thread.currentThread().interrupt();
					throw new RuntimeException(interruptedException);
				}
			}
		}
	}

	private void closeDestinyConnection() {
		if (connectionDestinySocket == null) {
			return;
		}

		try {
			connectionDestinySocket.close();
		} catch (IOException ignored) {
		}
	}

	public void stopService() {
			this.interrupt = true;
	}


	protected synchronized void createConnectionWithDestiny() throws IOException {
		connectionDestinySocket = new Socket(targetAddress.getIp(),targetAddress.getPort());
	}

	@Override
	protected void receivingMessages(Socket socket) throws IOException {
		System.err.println(proxyName + " enabled to receive messages.");

		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		while (true) {
			String receivedMessage = dataInputStream.readLine();

			if (receivedMessage == null) {
				continue; // Ignora mensagens nulas
			}

			if (receivedMessage.equals("ping")) {
				handlePingMessage(socket);
			} else {
				// Trata outras mensagens
				receivedMessage = registerTimeWhenArrives(receivedMessage);
				setContentToProcess(receivedMessage);
			}
		}
	}

	private void handlePingMessage(Socket socket) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		if (hasSomethingToProcess()) {
			oos.writeObject("busy");
		} else {
			oos.writeObject("free");
		}
	}





	private static String registerTimeWhenArrives(String receivedMessage) {
		String lastRegisteredTimeStampString;
		String[] stringSplited;
		stringSplited = receivedMessage.split(";");
		lastRegisteredTimeStampString = stringSplited[stringSplited.length - 1];
		long timeNow = System.currentTimeMillis();
		receivedMessage += timeNow + ";"+ (timeNow- Long.parseLong(lastRegisteredTimeStampString.trim()))+";";
		return receivedMessage;
	}

	private String registerTimeWhenGoOut(String receivedMessage) {
		//REGISTRAR TEMPO DE PROCESSAMENTO
		receivedMessage += System.currentTimeMillis()+";";
		Long ultimo = Long.valueOf(receivedMessage.split(";")[receivedMessage.split(";").length - 1]);
		Long penultimo = Long.valueOf(receivedMessage.split(";")[receivedMessage.split(";").length - 2]);
		receivedMessage +=  (ultimo - penultimo) + ";";

		return receivedMessage;
	}


}
