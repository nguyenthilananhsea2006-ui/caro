package caro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

public class NameInputDialog extends JDialog {
    private JTextField textField;
    private String inputName = null;
    private boolean isCancelled = false;

    public NameInputDialog(JFrame parent, String title, String message) {
        super(parent, title, true);
        
        setUndecorated(true);
        setSize(420, 250);
        setLocationRelativeTo(parent);
        // Bo góc cho toàn bộ cửa sổ Dialog
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nền Gradient gỗ tối
                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 45, 30), 0, getHeight(), new Color(40, 25, 15));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Viền vàng đồng ngoài cùng
                g2.setColor(new Color(170, 110, 45));
                g2.setStroke(new BasicStroke(5));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 25, 25);

                g2.dispose();
            }
        };
        contentPanel.setLayout(null);
        setContentPane(contentPanel);

        // Label Tiêu đề
        JLabel lblMsg = new JLabel(message, SwingConstants.CENTER);
        lblMsg.setForeground(new Color(255, 225, 120));
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMsg.setBounds(0, 30, 420, 30);
        contentPanel.add(lblMsg);

        // Ô nhập liệu (Màu nhạt hơn + Viền trắng)
        textField = new JTextField();
        textField.setBounds(50, 80, 320, 45);
        textField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        // Màu nền nhạt hơn (Light Brown / Beige)
        textField.setBackground(new Color(230, 220, 200)); 
        textField.setForeground(new Color(50, 30, 10));
        textField.setCaretColor(new Color(50, 30, 10));
        // Viền trắng sắc nét
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        contentPanel.add(textField);

        // --- Cặp nút OK và EXIT ---
        
        // Nút OK (Xác nhận)
        JButton btnOk = createStyledButton("OK", 50, 160, 150, 45, new Color(40, 120, 40));
        btnOk.addActionListener(e -> {
            if (!textField.getText().trim().isEmpty()) {
                inputName = textField.getText().trim();
                dispose();
            } else {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.RED, 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            }
        });

        // Nút EXIT (Hủy bỏ)
        JButton btnExit = createStyledButton("EXIT", 220, 160, 150, 45, new Color(150, 40, 40));
        btnExit.addActionListener(e -> {
            isCancelled = true;
            inputName = null;
            dispose();
        });

        // Phím tắt Enter và ESC
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnOk.doClick();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) btnExit.doClick();
            }
        });

        contentPanel.add(btnOk);
        contentPanel.add(btnExit);
    }

    // Hàm hỗ trợ tạo nút bấm phong cách đồng nhất với game
    private JButton createStyledButton(String text, int x, int y, int w, int h, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                boolean hovered = getModel().isRollover();
                // Hiệu ứng sáng lên khi hover
                Color c1 = hovered ? baseColor.brighter() : baseColor;
                Color c2 = baseColor.darker();
                
                g2.setPaint(new GradientPaint(0, 0, c1, 0, getHeight(), c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setBounds(x, y, w, h);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public String getInputName() {
        return inputName;
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}