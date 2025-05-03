package menu;
import game.Game;
import utils.Constants;
import utils.network.ServerAddress;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GameMenu {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JFrame frame;
    private JPanel menuPanel;
    private LoadingIndicator loadingIndicator;

    private List<ServerAddress> availableServers;

    public GameMenu() {
        availableServers = new ArrayList<>();
        initializeMenu();
    }

    private void initializeMenu() {
        frame = new JFrame("Game Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        frame.setResizable(false);

        menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 48));
                String title = "SHOOTER GAME";
                int titleWidth = g.getFontMetrics().stringWidth(title);
                g.drawString(title, (getWidth() - titleWidth) / 2, 100);
            }
        };
        menuPanel.setLayout(new GridBagLayout());

        setupLoginForm();

        frame.add(menuPanel);
        frame.setVisible(true);
    }

    private void setupLoginForm() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = createButton("LOGIN");
        loadingIndicator = new LoadingIndicator();
        loadingIndicator.setVisible(false);

        loginButton.addActionListener(e -> authenticate());

        menuPanel.add(new JLabel("Username:"), gbc);
        menuPanel.add(usernameField, gbc);
        menuPanel.add(new JLabel("Password:"), gbc);
        menuPanel.add(passwordField, gbc);

        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loginPanel.setOpaque(false);
        loginPanel.add(loginButton);
        loginPanel.add(loadingIndicator);

        menuPanel.add(loginPanel, gbc);
    }

    private void authenticate() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        loadingIndicator.start();
        loginButton.setEnabled(false);

        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInput = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInput.getBytes("utf-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String response = new String(conn.getInputStream().readAllBytes(), "utf-8");
                    response = response.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
                    String[] entries = response.split(",");
                    for (int i = 0; i < entries.length; i += 2) {
                        String ip = entries[i].split(":")[1].replaceAll("\"", "").trim();
                        int port = Integer.parseInt(entries[i + 1].split(":")[1].trim());
                        availableServers.add(new ServerAddress(ip, port));
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    loadingIndicator.stop();
                    loginButton.setEnabled(true);
                    if (responseCode == 200) {
                        menuPanel.removeAll();
                        addMainMenuButtons(); // Теперь тут PLAY и EXIT, а серверы после PLAY
                    } else {
                        JOptionPane.showMessageDialog(frame, "Login failed!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

            } catch (IOException ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    loadingIndicator.stop();
                    loginButton.setEnabled(true);
                    JOptionPane.showMessageDialog(frame, "Error connecting to server: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void addMainMenuButtons() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);

        JButton playButton = createButton("PLAY");
        JButton exitButton = createButton("EXIT");

        playButton.addActionListener(e -> showServerSelection());
        exitButton.addActionListener(e -> System.exit(0));

        menuPanel.add(playButton, gbc);
        menuPanel.add(exitButton, gbc);
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    private void showServerSelection() {
        menuPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);

        JLabel titleLabel = new JLabel("Select Server:");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        menuPanel.add(titleLabel, gbc);

        for (ServerAddress server : availableServers) {
            JButton serverButton = createServerButton(server);
            menuPanel.add(serverButton, gbc);
        }

        JButton backButton = createButton("BACK");
        backButton.addActionListener(e -> {
            menuPanel.removeAll();
            addMainMenuButtons();
            menuPanel.revalidate();
            menuPanel.repaint();
        });
        menuPanel.add(backButton, gbc);

        menuPanel.revalidate();
        menuPanel.repaint();
    }

    private JButton createServerButton(ServerAddress server) {
        JButton button = new JButton(server.getIp() + ":" + server.getPort());
        button.setPreferredSize(new Dimension(300, 50));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));

        button.addActionListener(e -> {
            frame.dispose();
            try {
                Game.start(server.getIp(), String.valueOf(server.getPort()));
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to server: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return button;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 50));
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setFocusPainted(false);
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return button;
    }

    public static void showMenu() {
        SwingUtilities.invokeLater(GameMenu::new);
    }
}