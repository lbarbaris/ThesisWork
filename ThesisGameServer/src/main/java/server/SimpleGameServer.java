package server;

import utils.Constants;
import utils.map.CollisionManager;
import utils.map.MapCreator;
import utils.bullets.Bullet;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Math.abs;
import static utils.Constants.*;

public class SimpleGameServer {
    private long packetPreparationStartTime;
    private int pingCounter;
    private final MapCreator mapCreator;
    private final DatagramSocket socket;
    private final List<Bullet> bullets;
    private final CollisionManager collisionManager;
    private final ConcurrentHashMap<String, ServerEnemy> playerStates = new ConcurrentHashMap<>();


    public SimpleGameServer(int port) throws Exception {
        this.bullets = new CopyOnWriteArrayList<>();
        this.pingCounter = 1;
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

                    performServerSideRaycast( shooterX, shooterY, mouseX, mouseY, playerKey, shotTime - Constants.INTERPOLATION_DELAY_MS);
                }
                else if (parts[0].equals("ADVSHOOT")){
                    for (int i = 1; i < parts.length; i += 4) {
                        var startX = Float.parseFloat(parts[i]);
                        var startY = Float.parseFloat(parts[i + 1]);
                        var dx = Float.parseFloat(parts[i + 2]);
                        var dy = Float.parseFloat(parts[i + 3]);

                        Bullet bullet = new Bullet(startX, startY, dx, dy, 1, 1);
                        bullets.add(bullet);
                    }
                }
                else{

                    int clientX = Integer.parseInt(parts[0]);
                    int clientY = Integer.parseInt(parts[1]);
                    long timestamp = Long.parseLong(parts[2]);
                    boolean isBot = Boolean.parseBoolean(parts[3]);
                    int hp = Integer.parseInt(parts[4]);
                    int packetNumber = Integer.parseInt(parts[5]);


                    playerStates.put(playerKey, new ServerEnemy(clientX, clientY, isBot, hp, packetNumber));


                    var state = playerStates.get(playerKey);
                    int maxDistance = 5; // Максимально допустимый шаг
                    int dxAllowed = Math.min(maxDistance, abs(clientX - state.getLast().getX()));
                    int dyAllowed = Math.min(maxDistance, abs(clientY - state.getLast().getY()));

                    int newX = state.getLast().getX() + (int) Math.signum(clientX - state.getLast().getX()) * dxAllowed;
                    int newY = state.getLast().getY() + (int) Math.signum(clientY - state.getLast().getY()) * dyAllowed;

                    if (!collisionManager.isWallHit(new Rectangle(newX, newY, Constants.SQUARE_SIZE, Constants.SQUARE_SIZE))) {
                        state.addFrame(newX, newY, timestamp);
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        ServerEnemy shooter = playerStates.get(shooterId);
        if (shooter == null) return;

        ServerEnemy hitTarget = null;
        Point hitPoint = null;
        double closestEnemyDist = Double.MAX_VALUE;

        // Сначала проверяем попадание во врагов
        for (Map.Entry<String, ServerEnemy> entry : playerStates.entrySet()) {
            String id = entry.getKey();
            if (id.equals(shooterId)) continue;

            ServerEnemy target = entry.getValue();
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
            hitTarget.setHp(hitTarget.getHp() - 20);
            System.out.println("Игрок " + shooterId + " попал по врагу! Осталось HP: " + hitTarget.getHp());
        }
    }

    private void sendPackets() {
        try {
            while (true) {
                packetPreparationStartTime = System.currentTimeMillis();
                for (Map.Entry<String, ServerEnemy> entry : playerStates.entrySet()) {
                    var keyParts = entry.getKey().split(":");
                    var address = InetAddress.getByName(keyParts[0].substring(1));
                    var port = Integer.parseInt(keyParts[1]);
                    var state = entry.getValue();

                    var response = new StringBuilder(state.toString());

                    for (Map.Entry<String, ServerEnemy> entry1: playerStates.entrySet()){
                        if (!Objects.equals(entry1.getKey(), entry.getKey())){
                            response.append(entry1.getValue().getLast().getX()).append(",")
                                    .append(entry1.getValue().getLast().getY()).append(",")
                                    .append(entry1.getKey()).append(",")
                                    .append(entry1.getValue().isBot()).append(",")
                                    .append(entry1.getValue().getHp()).append(",")
                                    .append(entry1.getValue().getPacketNumber()).append(",");
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

    private void updateBullets() {
        for (var bullet : bullets) {
            bullet.update();

            var bulletBounds = bullet.getBounds();

            // Проверка столкновения со стеной
            if (collisionManager.isWallHit(bulletBounds)) {
                var wallPoint = collisionManager.getPointWallHit(bulletBounds);
                System.out.println("Пуля попала в стену: " + wallPoint);
                bullets.remove(bullet);
                continue;
            }

            // Проверка попадания по врагам
            for (var enemy : playerStates.values()) {
                var interpolatedPosition = enemy.getInterpolatedPosition(INTERPOLATION_DELAY_MS);
                var bounds = new Rectangle(interpolatedPosition.x, interpolatedPosition.y, SQUARE_SIZE, SQUARE_SIZE);
                if (bounds.intersects(bulletBounds)) {
                    enemy.doDamage(bullet.getDamage());
                    System.out.println("Пуля попала по врагу: " + enemy);
                    bullets.remove(bullet);
                    break;
                }
            }
        }
    }

    private void startRendering() {
        var frame = new JFrame("Game Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        var renderer = new GameRenderer(mapCreator, playerStates, bullets);
        frame.add(renderer);
        frame.setVisible(true);

        new Timer(16, e -> {renderer.repaint(); updateBullets();}).start();
    }

    public List<Bullet> getBullets() {
        return bullets;
    }
}