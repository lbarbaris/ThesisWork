import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleGameServer {
    private final int PLAYER_SIZE = 20;
    private final MapCreator mapCreator;
    private final DatagramSocket socket;
    private final CollisionManager collisionManager;
    private final ConcurrentHashMap<String, PlayerState> playerStates = new ConcurrentHashMap<>();


    public SimpleGameServer(int port) throws Exception {
        this.mapCreator = new MapCreator((short) 3);
        this.collisionManager = new CollisionManager(mapCreator.getMap());
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

                int dx = Integer.parseInt(parts[0]);
                int dy = Integer.parseInt(parts[1]);
                int clientX = Integer.parseInt(parts[2]);
                int clientY = Integer.parseInt(parts[3]);
                long timestamp = Long.parseLong(parts[4]);
                boolean isBot = Boolean.parseBoolean(parts[5]);

                playerStates.putIfAbsent(playerKey, new PlayerState(clientX, clientY, isBot));

                PlayerState state = playerStates.get(playerKey);
                int maxDistance = 5; // Максимально допустимый шаг
                int dxAllowed = Math.min(maxDistance, Math.abs(clientX - state.x));
                int dyAllowed = Math.min(maxDistance, Math.abs(clientY - state.y));

                int newX = state.x + (int) Math.signum(clientX - state.x) * dxAllowed;
                int newY = state.y + (int) Math.signum(clientY - state.y) * dyAllowed;

                if (!collisionManager.isWallHit(new Rectangle(newX, newY, PLAYER_SIZE, PLAYER_SIZE))) {
                    state.x = newX;
                    state.y = newY;
                    state.lastProcessedTimestamp = timestamp;
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPackets() {
        try {
            while (true) {
                Thread.sleep(50);
                for (Map.Entry<String, PlayerState> entry : playerStates.entrySet()) {
                    String[] keyParts = entry.getKey().split(":");
                    InetAddress address = InetAddress.getByName(keyParts[0].substring(1));
                    int port = Integer.parseInt(keyParts[1]);
                    PlayerState state = entry.getValue();

                    StringBuilder response = new StringBuilder(state.x + "," + state.y + "," + state.lastProcessedTimestamp + ",");

                    for (Map.Entry<String, PlayerState> entry1: playerStates.entrySet()){
                        if (!Objects.equals(entry1.getKey(), entry.getKey())){
                            response.append(entry1.getValue().x).append(",").append(entry1.getValue().y).append(",").append(entry1.getKey()).append(",").append(entry1.getValue().isBot).append(",");
                            System.out.println(response);
                        }

                    }


                    byte[] responseData = response.toString().getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
                    socket.send(responsePacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRendering() {
        JFrame frame = new JFrame("Game Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);

        GameRenderer renderer = new GameRenderer(mapCreator, playerStates);
        frame.add(renderer);
        frame.setVisible(true);

        new Timer(16, e -> renderer.repaint()).start();
    }

    public static void main(String[] args) {
        try {
            SimpleGameServer server = new SimpleGameServer(12345);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}