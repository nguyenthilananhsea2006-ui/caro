package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.sound.sampled.*;

public class Caro extends JFrame {
    private static final int SIZE = 15;
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private BanCo banCo = new BanCo(SIZE, SIZE);
    private AIPlayer ai = new AIPlayer(SIZE);
    private char currentPlayer = 'X';
    private JLabel statusLabel, scoreLabel;
    private boolean vsAI, gameOver = false;
    private String nameX, nameO;
    private int scoreX = 0, scoreO = 0;
    private JLayeredPane layeredPane;

    public Caro(boolean vsAI, String nameX, String nameO) {
        this.vsAI = vsAI;
        this.nameX = nameX;
        this.nameO = nameO;

        setTitle("Game Caro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 950);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(850, 950));
        setContentPane(layeredPane);

        int xPos = (850 - 750) / 2;
        int yPos = (850 - 750) / 2;

        // --- HÌNH NỀN BÀN CỜ ---
        JPanel bgPnl = new JPanel() {
            private BufferedImage bgImage;
            {
                try {
                    java.io.File file = new java.io.File("src/main/java/res.tiles/banco.png");
                    if (file.exists()) bgImage = ImageIO.read(file);
                    else {
                        java.io.InputStream is = getClass().getResourceAsStream("/res.tiles/banco.png");
                        if (is != null) bgImage = ImageIO.read(is);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) g.drawImage(bgImage, 0, 0, 750, 750, this);
            }
        };
        bgPnl.setBounds(xPos, yPos, 750, 750);
        layeredPane.add(bgPnl, JLayeredPane.DEFAULT_LAYER);

        // --- LƯỚI CÁC Ô BẤM ---
        int boardInsideSize = 660;
        int margin = (750 - boardInsideSize) / 2;

        JPanel mainBoardPanel = new JPanel(new GridLayout(SIZE, SIZE));
        mainBoardPanel.setBounds(xPos + margin, yPos + margin, boardInsideSize, boardInsideSize);
        mainBoardPanel.setOpaque(false);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setContentAreaFilled(false);
                buttons[i][j].setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 30)));
                final int r = i, c = j;
                buttons[i][j].addActionListener(e -> xuLyDanh(r, c));
                mainBoardPanel.add(buttons[i][j]);
            }
        }
        layeredPane.add(mainBoardPanel, Integer.valueOf(1));

        // --- PHẦN TIÊU ĐỀ (STATUS & SCORE) ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBounds(0, yPos - 90, 850, 80);
        headerPanel.setOpaque(false);

        statusLabel = new JLabel("Lượt: " + nameX, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 22));
        scoreLabel = new JLabel(nameX + ": " + scoreX + "  -  " + nameO + ": " + scoreO, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));

        headerPanel.add(statusLabel);
        headerPanel.add(scoreLabel);
        layeredPane.add(headerPanel, JLayeredPane.POPUP_LAYER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void xuLyDanh(int r, int c) {
        if (gameOver || !buttons[r][c].getText().equals("")) return;

        thucHienNuocDi(r, c, currentPlayer);

        int[][] winCells = KiemTraThang.layToaDoThang(banCo.getBoard(), r, c, currentPlayer);
        if (winCells != null) {
            xuLyThangCuoc(winCells, currentPlayer == 'X' ? nameX : nameO);
            return;
        }

        if (vsAI) {
            int[] move = ai.getMove(banCo.getBoard());
            if (move != null) {
                thucHienNuocDi(move[0], move[1], 'O');
                int[][] aiWin = KiemTraThang.layToaDoThang(banCo.getBoard(), move[0], move[1], 'O');
                if (aiWin != null) xuLyThangCuoc(aiWin, nameO);
            }
        } else {
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            statusLabel.setText("Lượt: " + (currentPlayer == 'X' ? nameX : nameO));
        }
    }

    private void xuLyThangCuoc(int[][] winCells, String winnerName) {
        gameOver = true;
        for (int[] cell : winCells) {
            buttons[cell[0]][cell[1]].setBackground(new Color(255, 255, 150));
            buttons[cell[0]][cell[1]].setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
            buttons[cell[0]][cell[1]].setOpaque(true);
        }

        if (winnerName.equals(nameX)) scoreX++; else scoreO++;
        scoreLabel.setText(nameX + ": " + scoreX + "  -  " + nameO + ": " + scoreO);
        
        Timer timer = new Timer(2000, e -> showWinMessage(winnerName));
        timer.setRepeats(false);
        timer.start();
        playSound("win.wav");
    }

    private void thucHienNuocDi(int r, int c, char p) {
        banCo.getBoard()[r][c] = p;
        buttons[r][c].setText(String.valueOf(p));
        buttons[r][c].setFont(new Font("Arial", Font.BOLD, 30));
        if (p == 'X') {
            buttons[r][c].setForeground(new Color(255, 80, 80));
        } else {
            buttons[r][c].setForeground(new Color(80, 200, 255));
        }
        playSound("click.wav");
    }

    private void showWinMessage(String winnerName) {
        JPanel overlayPanel = new JPanel() {
            private BufferedImage winImage;
            {
                try {
                    java.io.File file = new java.io.File("src/main/java/res.tiles/winer.png");
                    if (file.exists()) winImage = ImageIO.read(file);
                    else {
                        java.io.InputStream is = getClass().getResourceAsStream("/res.tiles/winer.png");
                        if (is != null) winImage = ImageIO.read(is);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ lớp phủ tối
                g2.setColor(new Color(0, 0, 0, 160));
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (winImage != null) {
                    int imgW = 750, imgH = 750;
                    int xImg = (getWidth() - imgW) / 2;
                    int yImg = (getHeight() - imgH) / 2;
                    g2.drawImage(winImage, xImg, yImg, imgW, imgH, this);

                    // Tên người thắng nghệ thuật
                    String winText = winnerName.toUpperCase();
                    Font winFont = new Font("Palatino Linotype", Font.BOLD, 55);
                    g2.setFont(winFont);
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(winText)) / 2;
                    int textY = yImg + 485;

                    // Vẽ bóng đổ và Outline
                    g2.setColor(new Color(0, 0, 0, 150));
                    g2.drawString(winText, textX + 3, textY + 3);
                    
                    g2.setStroke(new BasicStroke(5f));
                    g2.setColor(new Color(60, 30, 0));
                    java.awt.font.TextLayout tl = new java.awt.font.TextLayout(winText, winFont, g2.getFontRenderContext());
                    java.awt.Shape shape = tl.getOutline(java.awt.geom.AffineTransform.getTranslateInstance(textX, textY));
                    g2.draw(shape);

                    // Gradient Vàng Kim
                    g2.setPaint(new GradientPaint(textX, textY - 40, new Color(255, 255, 150), textX, textY, new Color(255, 180, 0)));
                    g2.fill(shape);
                }
            }
        };

        overlayPanel.setLayout(null);
        overlayPanel.setBounds(0, 0, getWidth(), getHeight());
        overlayPanel.setOpaque(false);

        int btnW = 280, btnH = 60;
        int xCenter = (getWidth() - btnW) / 2;
        int yStart = (getHeight() - 750) / 2 + 510;

        JButton btnNewGame = createStyledWinButton("CHƠI LẠI", xCenter, yStart, btnW, btnH);
        btnNewGame.addActionListener(e -> {
            layeredPane.remove(overlayPanel);
            resetGame();
            layeredPane.repaint();
        });

        JButton btnHome = createStyledWinButton("MENU CHÍNH", xCenter, yStart + 80, btnW, btnH);
        btnHome.addActionListener(e -> {
            this.dispose();
            new Main().setVisible(true);
        });

        overlayPanel.add(btnNewGame);
        overlayPanel.add(btnHome);
        layeredPane.add(overlayPanel, JLayeredPane.DRAG_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private JButton createStyledWinButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isHovered = getModel().isRollover();

                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(3, 5, w, h, 15, 15);

                Color colorTop = isHovered ? new Color(255, 225, 120) : new Color(170, 110, 45);
                Color colorBottom = isHovered ? new Color(210, 130, 35) : new Color(110, 65, 15);
                g2.setPaint(new GradientPaint(0, 0, colorTop, 0, h, colorBottom));
                g2.fillRoundRect(0, 0, w, h, 15, 15);

                g2.setStroke(new BasicStroke(2.5f));
                g2.setColor(new Color(60, 30, 10));
                g2.drawRoundRect(0, 0, w, h, 15, 15);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setBounds(x, y, w + 5, h + 10);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void resetGame() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setBackground(null);
                buttons[i][j].setOpaque(false);
                buttons[i][j].setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 30)));
                banCo.getBoard()[i][j] = ' ';
            }
        }
        gameOver = false;
        currentPlayer = 'X';
        statusLabel.setText("Lượt: " + nameX);
        layeredPane.repaint();
    }

    private void playSound(String soundFile) {
        try {
            java.net.URL url = getClass().getResource("/res.sounds/" + soundFile);
            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}