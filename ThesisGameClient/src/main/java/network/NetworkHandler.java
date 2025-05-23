package network;

import utils.bullets.Bullet;
import movement.MovementManager;
import utils.Constants;
import utils.graphs.GraphResource;
import utils.network.ClientEnemy;

import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.network.ExtrapolationDotsCounter;
import utils.network.Frame;
import utils.player.Player;

import static utils.Constants.*;

public class NetworkHandler {
    private final DatagramSocket socket;
    private final InetAddress serverAddress;
    private final MovementManager movementManager;
    private final GraphResource<Long> graphResource;
    private final ExtrapolationDotsCounter extrapolationDotsCounter;
    private final String serverPort;
    private final ExecutorService networkThreads = Executors.newFixedThreadPool(2);
    private volatile boolean running = true;
    private final boolean isBot = false;
    private final HashMap<String, ClientEnemy> PlayerCoords;
    private final Player player;
    private int packetCounter;
    private LinkedList<Long> packetNumbers;
    private boolean isPacketsAdded;
    private boolean isGraphSaved;

    private long randomTime;

    private long startTime;

    private boolean isPredictedSaved;

    private Point uniquePoint;
    private Point realPoint;

    private Set<Point> uniquePoints;
    private Set<Point> realPoints;
    private final GraphResource<Double> predictedDotsGraph;
    private final GraphResource<Double> primitivePredictedDotsGraph;
    private final GraphResource<Double> polynomDotsGraph;
    private final GraphResource<Double> polynomPadeDotsGraph;

    private int predictedDotsCounter;


    public NetworkHandler(String serverHost, String serverPort, MovementManager movementManager, Player player) throws IOException {
        this.extrapolationDotsCounter = new ExtrapolationDotsCounter();
        this.packetNumbers = new LinkedList<>();
        this.isPacketsAdded = false;
        this.randomTime = new Random().nextInt(200 - 50 + 1 ) + 50;
        this.isGraphSaved = false;
        this.graphResource = new GraphResource<>("test", "количество замеров", "время");
        this.predictedDotsGraph = new GraphResource<>("Тест предсказания на " + PREDICT_MS + "мс", "количество замеров", "отклонение по расстоянию");
        this.primitivePredictedDotsGraph = new GraphResource<>("Тест предсказания на " + PREDICT_MS + "мс [ПРИМИТИВ]", "количество замеров", "отклонение по расстоянию");
        this.polynomDotsGraph = new GraphResource<>("Тест предсказания на " + PREDICT_MS + "мс [ПОЛИНОМ]", "количество замеров", "отклонение по расстоянию");
        this.polynomPadeDotsGraph = new GraphResource<>("Тест предсказания на " + PREDICT_MS + "мс [ПАДЕ]", "количество замеров", "отклонение по расстоянию");

        this.player = player;
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
        this.movementManager = movementManager;
        this.PlayerCoords = new HashMap<>();
        this.uniquePoints = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.realPoints = Collections.newSetFromMap(new ConcurrentHashMap<>());
        graphResource.addSeries();
        graphResource.addSeries();
        predictedDotsGraph.addSeries();
        predictedDotsGraph.addSeries();
        primitivePredictedDotsGraph.addSeries();
        primitivePredictedDotsGraph.addSeries();
        polynomDotsGraph.addSeries();
        polynomDotsGraph.addSeries();
        polynomPadeDotsGraph.addSeries();
        polynomPadeDotsGraph.addSeries();
        packetCounter = 0;
        predictedDotsCounter = 0;
        startTime = System.currentTimeMillis();
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
        sendStringData(frame.toString());
    }

    public void sendBasicBullets(int playerX, int playerY, int mouseX, int mouseY) {
        var shootPacket = "SHOOT," + playerX + "," + playerY + "," + mouseX + "," + mouseY + "," + System.currentTimeMillis();
        sendStringData(shootPacket);
    }

    private void sendStringData(String data){
        try {
            var bytes = data.getBytes();
            var packet = new DatagramPacket(bytes, bytes.length, serverAddress, Integer.parseInt(serverPort));
            socket.send(packet);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendAdvancedBullets(List<Bullet> bullets) {
        var shootPacket = new StringBuilder("ADVSHOOT,");
        for (var bullet: bullets){
            shootPacket
                    .append(bullet.getStartX()).append(",")
                    .append(bullet.getStartY()).append(",")
                    .append(bullet.getDx()).append(",")
                    .append(bullet.getDy()).append(",");
        }
        sendStringData(shootPacket.toString());
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
        int hp = Integer.parseInt(parts[3]);
        int serverPacketNumber = Integer.parseInt(parts[4]);
        if (serverPacketNumber < Constants.PACKET_MEASUREMENT_SIZE){
            graphResource.addValue(0, (long) serverPacketNumber);
            graphResource.addValue(1, System.currentTimeMillis() - packetNumbers.get(serverPacketNumber));
        } else if (!isGraphSaved){
            //graphResource.exportToTxt("test3.txt");
            isGraphSaved = true;
        }


        if (hp <= 0){
            player.respawn(50, 50, Constants.PLAYER_MAX_HP);
        }
        movementManager.applyServerData(serverX, serverY, serverTimeStamp);


        for (int i = 5; i < parts.length - 5; i += 6){
            int x = Integer.parseInt(parts[i]);
            int y = Integer.parseInt(parts[i + 1]);
            String id = parts[i + 2];
            boolean bot = Boolean.parseBoolean(parts[i + 3]);

            var enemy = PlayerCoords.getOrDefault(id, new ClientEnemy(bot, x, y, Constants.PLAYER_MAX_HP));
/*            if (System.currentTimeMillis() - startTime > randomTime) {*/
/*                if (predictedDotsCounter < PREDICT_SIZE) {*/
                   int j = 100;
                        var point = new Point(enemy.getPredictedPositionWithAcceleration(j));
                        var primitivePoint = new Point(enemy.getPredictedPosition(j));
                        var primitivePoint2 = new Point(enemy.getPredictedPosition(50));
                        var smartPoint = new Point(enemy.getSmartPredictedPosition(System.currentTimeMillis(), j));
                        var padeSmartPoint = new Point(enemy.getPadeSmartPredictedPosition(System.currentTimeMillis(), j));
                        uniquePoint = primitivePoint;
                        realPoint = extrapolationDotsCounter.getDistanceToNearestPoint(primitivePoint2);
                        uniquePoints.add(primitivePoint);
                        realPoints.add(extrapolationDotsCounter.getDistanceToNearestPoint(primitivePoint));
/*                        var distance = extrapolationDotsCounter.getDistanceToNearestPoint(point);
                        var primitiveDistance = extrapolationDotsCounter.getDistanceToNearestPoint(primitivePoint);
                        var smartDistance = extrapolationDotsCounter.getDistanceToNearestPoint(smartPoint);
                        var padeSmartDistance = extrapolationDotsCounter.getDistanceToNearestPoint(padeSmartPoint);
                        predictedDotsGraph.addValue(0, (double) predictedDotsCounter);
                        predictedDotsGraph.addValue(1, distance);
                        primitivePredictedDotsGraph.addValue(0, (double) predictedDotsCounter);
                        primitivePredictedDotsGraph.addValue(1, primitiveDistance);
                        polynomDotsGraph.addValue(0, (double) predictedDotsCounter);
                        polynomDotsGraph.addValue(1, smartDistance);
                        polynomPadeDotsGraph.addValue(0, (double) predictedDotsCounter);
                        polynomPadeDotsGraph.addValue(1, padeSmartDistance);*/
/*                    predictedDotsCounter++;
                } else if (!isPredictedSaved) {
                    predictedDotsGraph.extendSeries();
                    primitivePredictedDotsGraph.extendSeries();
                    polynomPadeDotsGraph.extendSeries();
                    polynomDotsGraph.extendSeries();
                    isPredictedSaved = true;
                    predictedDotsGraph.exportToTxt(predictedDotsGraph.getTitle() + ".txt");
                    primitivePredictedDotsGraph.exportToTxt(primitivePredictedDotsGraph.getTitle() + ".txt");
                    polynomPadeDotsGraph.exportToTxt(polynomPadeDotsGraph.getTitle() + ".txt");
                    polynomDotsGraph.exportToTxt(polynomDotsGraph.getTitle() + ".txt");
                }*/
/*                startTime = System.currentTimeMillis();*/
/*            }*/

            enemy.addFrame(x, y, serverTimeStamp);
            PlayerCoords.put(id, enemy);
        }

    }

    public void putToPlayerCoords(ClientEnemy clientEnemy){
        PlayerCoords.put("target", clientEnemy);
    }

    public HashMap<String, ClientEnemy> getPlayerCoords(){
        return PlayerCoords;
    }


    public void stop() {
        running = false;
        socket.close();
        networkThreads.shutdownNow();
    }




    public Set<Point> getUniquePoints() {
        return uniquePoints;
    }

    public Set<Point> getRealPoints() {
        return realPoints;
    }

    public Point getUniquePoint() {
        return uniquePoint;
    }

    public Point getRealPoint() {
        return realPoint;
    }
}