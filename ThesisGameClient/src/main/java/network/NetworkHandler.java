package network;

import movement.MovementManager;
import utils.Constants;
import utils.graphs.GraphResource;
import utils.network.Enemy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.network.Frame;
import utils.player.Player;

public class NetworkHandler {
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final MovementManager movementManager;
    private final GraphResource<Long> graphResource;
    private final int serverPort;
    private final ExecutorService networkThreads = Executors.newFixedThreadPool(2);
    private volatile boolean running = true;
    private final boolean isBot = false;
    private final HashMap<String, Enemy> PlayerCoords;
    private final Player player;
    private int packetCounter;
    private LinkedList<Long> packetNumbers;
    private boolean isPacketsAdded;
    private boolean isGraphSaved;

    public NetworkHandler(String serverHost, int serverPort, MovementManager movementManager, Player player) throws IOException {
        this.packetNumbers = new LinkedList<>();
        this.isPacketsAdded = false;
        this.isGraphSaved = false;
        this.graphResource = new GraphResource<>("test", "количество замеров", "время");
        this.player = player;
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.movementManager = movementManager;
        this.PlayerCoords = new HashMap<>();
        graphResource.addSeries();
        graphResource.addSeries();
        packetCounter = 0;
    }

    public void startNetworkThreads() {
        networkThreads.execute(() -> {
            while (running) {
                try {
                    Frame frame = new Frame(movementManager.getCoords(), isBot, player.getHp(), packetCounter);
                    if (packetCounter < Constants.PACKET_MEASUREMENT_SIZE && !isPacketsAdded){
                        packetNumbers.add(System.currentTimeMillis());
                    }

                    if (packetCounter == Constants.PACKET_MEASUREMENT_SIZE){
                        packetCounter = 0;
                        isPacketsAdded = true;
                    }
                    else{
                        packetCounter++;
                    }

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

    public void sendShootRequest(int playerX, int playerY, int mouseX, int mouseY) throws IOException {
        String shootPacket = "SHOOT," + playerX + "," + playerY + "," + mouseX + "," + mouseY + "," + System.currentTimeMillis();
        byte[] data = shootPacket.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
        socket.send(packet);
    }

    private void receiveServerData() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        //

        String response = new String(packet.getData(), 0, packet.getLength());
        String[] parts = response.split(",");

        int serverX = Integer.parseInt(parts[0]);
        int serverY = Integer.parseInt(parts[1]);
        long serverTimeStamp = Long.parseLong(parts[2]);
        int hp = Integer.parseInt(parts[3]);
        int serverPacketNumber = Integer.parseInt(parts[4]);
        if (serverPacketNumber < Constants.PACKET_MEASUREMENT_SIZE){
            graphResource.addValue(0, (long) serverPacketNumber);
            graphResource.addValue(1, System.currentTimeMillis() - packetNumbers.get(serverPacketNumber));
        } else if (!isGraphSaved){
            graphResource.exportToTxt("ThesisGraphs/resources/test3.txt");
            isGraphSaved = true;
        }


        if (hp <= 0){
            player.respawn(50, 50, 100);
        }
        movementManager.applyServerData(serverX, serverY, serverTimeStamp);


        for (int i = 5; i < parts.length - 5; i += 6){
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