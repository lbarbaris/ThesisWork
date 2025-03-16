package network;

import movement.MovementManager;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkHandler {
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final MovementManager movementManager;
    private final int serverPort;
    private final ExecutorService networkThreads = Executors.newFixedThreadPool(2);
    private volatile boolean running = true;
    private final boolean isBot = false;
    private HashMap<String, Enemy> PlayerCoords;

    public NetworkHandler(String serverHost, int serverPort, MovementManager movementManager) throws IOException {
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.movementManager = movementManager;
        this.PlayerCoords = new HashMap<>();
    }

    public void startNetworkThreads() {
        networkThreads.execute(() -> {
            while (running) {
                try {
                    Frame frame = new Frame(movementManager.getCoords(), isBot);
                    sendMovementRequest(frame);
                    Thread.sleep(5); // Частота отправки пакетов
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });

        // Поток приёма данных
        networkThreads.execute(() -> {
            while (running) {
                try {
                    receiveServerData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendMovementRequest(Frame frame) throws IOException {
        movementManager.addCommand();
        byte[] data = frame.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
        socket.send(packet);
    }

    private void receiveServerData() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        String response = new String(packet.getData(), 0, packet.getLength());
        String[] parts = response.split(",");

        int serverX = Integer.parseInt(parts[0]);
        int serverY = Integer.parseInt(parts[1]);
        long serverTimeStamp = Long.parseLong(parts[2]);
        movementManager.applyServerData(serverX, serverY, serverTimeStamp);


        for (int i = 3; i < parts.length - 3; i += 4){
            int x = Integer.parseInt(parts[i]);
            int y = Integer.parseInt(parts[i + 1]);
            String id = parts[i + 2];
            boolean bot = Boolean.parseBoolean(parts[i + 3]);

            Enemy enemy = PlayerCoords.getOrDefault(id, new Enemy(bot, x, y, 100, serverTimeStamp));
            enemy.addFrame(x, y, serverTimeStamp);
            PlayerCoords.put(id, enemy);
        }

    }

    public void putToPlayerCoords(Enemy enemy){
        PlayerCoords.put("target", enemy);
    }

    public HashMap<String, Enemy> getPlayerCoords(){
        return PlayerCoords;
    }


    public void stop() {
        running = false;
        socket.close();
        networkThreads.shutdownNow();
    }


}