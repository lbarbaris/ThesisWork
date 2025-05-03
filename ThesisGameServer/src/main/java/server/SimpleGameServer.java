package server;

import utils.Constants;
import utils.map.CollisionManager;
import utils.map.MapCreator;

import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.abs;
import static utils.Constants.SERVER_SEND_SLEEP_MS;

public class SimpleGameServer {
    private long packetPreparationStartTime;
    private int pingCounter;
    private final MapCreator mapCreator;
    private final DatagramSocket socket;
    private final CollisionManager collisionManager;
    private final ConcurrentHashMap<String, Enemy2> playerStates = new ConcurrentHashMap<>();


    public SimpleGameServer(int port) throws Exception {
        pingCounter = 1;
        this.packetPreparationStartTime = 0;
        this.mapCreator = new MapCreator((short) 3);
        this.collisionManager = new CollisionManager(mapCreator.getWalls());
        this.socket = new DatagramSocket(port);
    }

    public void start() {
        new Thread(this::processIncomingPackets).start();
        new Thread(this::sendPackets).start();
        SwingUtilities.invokeLater(this::startRendering);
    }

    private void processIncomingPackets() {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String data = new String(packet.getData(), 0, packet.getLength());
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String playerKey = address.toString() + ":" + port;

                String[] parts = data.split(",");


                if (parts.length < 5) continue;

                if (parts[0].equals("SHOOT")) {
                    // Пример: SHOOT,playerX,playerY,mouseX,mouseY,timestamp
                    int shooterX = Integer.parseInt(parts[1]);
                    int shooterY = Integer.parseInt(parts[2]);
                    int mouseX = Integer.parseInt(parts[3]);
                    int mouseY = Integer.parseInt(parts[4]);
                    long shotTime = Long.parseLong(parts[5]);

                    // Сохрани snapshot мира по времени shotTime
                    Map<String, Enemy2> snapshot = getWorldStateAt(shotTime);

                    performServerSideRaycast( shooterX, shooterY, mouseX, mouseY, playerKey, shotTime - Constants.INTERPOLATION_DELAY_MS);
                }
                else {

                    int clientX = Integer.parseInt(parts[0]);
                    int clientY = Integer.parseInt(parts[1]);
                    long timestamp = Long.parseLong(parts[2]);
                    boolean isBot = Boolean.parseBoolean(parts[3]);
                    int hp = Integer.parseInt(parts[4]);
                    int packetNumber = Integer.parseInt(parts[5]);


                    playerStates.put(playerKey, new Enemy2(clientX, clientY, isBot, hp, packetNumber));


                    Enemy2 state = playerStates.get(playerKey);
                    int maxDistance = 5; // Максимально допустимый шаг
                    int dxAllowed = Math.min(maxDistance, abs(clientX - state.x));
                    int dyAllowed = Math.min(maxDistance, abs(clientY - state.y));

                    int newX = state.x + (int) Math.signum(clientX - state.x) * dxAllowed;
                    int newY = state.y + (int) Math.signum(clientY - state.y) * dyAllowed;

                    if (!collisionManager.isWallHit(new Rectangle(newX, newY, Constants.SQUARE_SIZE, Constants.SQUARE_SIZE))) {
                        state.x = newX;
                        state.y = newY;
                        state.lastProcessedTimestamp = timestamp;
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Enemy2> getWorldStateAt(long timestamp) {
        Map<String, Enemy2> snapshot = new HashMap<>();
        long correctedTimestamp = timestamp - Constants.INTERPOLATION_DELAY_MS;

        for (Map.Entry<String, Enemy2> entry : playerStates.entrySet()) {
            Enemy2 state = entry.getValue();
            Enemy2 past = state.getStateAt(correctedTimestamp);
            if (past != null) {
                snapshot.put(entry.getKey(), past);
            }
        }

        return snapshot;
    }

    private void performServerSideRaycast(int shooterX, int shooterY, int mouseX, int mouseY, String shooterId, long timestamp) {
        double angle = Math.atan2(mouseY - shooterY, mouseX - shooterX);
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        double px = shooterX;
        double py = shooterY;

        double maxRayLength = 10000;
        double step = 1.0;
        double radius = Constants.SQUARE_SIZE / 2.0;

        Enemy2 shooter = playerStates.get(shooterId);
        if (shooter == null) return;

        Enemy2 hitTarget = null;
        Point hitPoint = null;
        double closestEnemyDist = Double.MAX_VALUE;

        // Сначала проверяем попадание во врагов
        for (Map.Entry<String, Enemy2> entry : playerStates.entrySet()) {
            String id = entry.getKey();
            if (id.equals(shooterId)) continue;

            Enemy2 target = entry.getValue();
            Point enemyPos = target.getInterpolatedPosition(timestamp);
            double tx = enemyPos.x + radius;
            double ty = enemyPos.y + radius;

            double vx = tx - px;
            double vy = ty - py;
            double dot = vx * dx + vy * dy;

            if (dot < 0) continue; // враг позади

            double closestX = px + dot * dx;
            double closestY = py + dot * dy;
            double distSq = Math.pow(closestX - tx, 2) + Math.pow(closestY - ty, 2);

            if (distSq < radius * radius && dot < closestEnemyDist) {
                hitTarget = target;
                closestEnemyDist = dot;
                hitPoint = new Point((int) closestX, (int) closestY);
            }
        }


        for (double t = 0; t < maxRayLength; t += step) {
            int checkX = (int) (px + dx * t);
            int checkY = (int) (py + dy * t);
            Rectangle rayRect = new Rectangle(checkX, checkY, 2, 2);
            if (collisionManager.isWallHit(rayRect)) {
                System.out.println("Пуля попала в стену на расстоянии " + t);
                return;
            }

            if (hitTarget != null && t > closestEnemyDist) {
                break;
            }
        }

        // Если попали по врагу
        if (hitTarget != null) {
            hitTarget.hp -= 20;
            System.out.println("Игрок " + shooterId + " попал по врагу! Осталось HP: " + hitTarget.hp);
        }
    }

    private void sendPackets() {
        try {
            while (true) {
                packetPreparationStartTime = System.currentTimeMillis();
                for (Map.Entry<String, Enemy2> entry : playerStates.entrySet()) {
                    String[] keyParts = entry.getKey().split(":");
                    InetAddress address = InetAddress.getByName(keyParts[0].substring(1));
                    int port = Integer.parseInt(keyParts[1]);
                    Enemy2 state = entry.getValue();

                    StringBuilder response = new StringBuilder(state.toString());

                    for (Map.Entry<String, Enemy2> entry1: playerStates.entrySet()){
                        if (!Objects.equals(entry1.getKey(), entry.getKey())){
                            response.append(entry1.getValue().x).append(",").append(entry1.getValue().y).append(",").append(entry1.getKey()).append(",").append(entry1.getValue().isBot).append(",").append(entry1.getValue().hp).append(",").append(entry1.getValue().getPacketNumber()).append(",");
                        }
                    }

                    byte[] responseData = response.toString().getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
                    socket.send(responsePacket);
                }
                Thread.sleep(SERVER_SEND_SLEEP_MS);
/*                if (pingCounter % (1200 / PING_PER_MINUTE) == 0){
                    Thread.sleep(1050);
                }
                else {

                }*/
                pingCounter++;
                //Thread.sleep(abs(8 - (System.currentTimeMillis() - packetPreparationStartTime)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRendering() {
        JFrame frame = new JFrame("Game Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        GameRenderer renderer = new GameRenderer(mapCreator, playerStates);
        frame.add(renderer);
        frame.setVisible(true);

        new Timer(16, e -> renderer.repaint()).start();
    }
}