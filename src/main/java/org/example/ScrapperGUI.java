package org.example;

import javax.swing.*;
import java.awt.*;

public class ScrapperGUI extends JFrame {
    private JTextField keywordField;
    private JTextArea resultArea;
    private JButton startButton, stopButton, exitButton;
    private Thread scrapingThread;

    public ScrapperGUI() {
        setTitle("Scrapper GUI");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel kiri (log)
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        add(scrollPane, BorderLayout.CENTER);

        // Panel kanan (form & tombol)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.setPreferredSize(new Dimension(250, getHeight()));
        add(rightPanel, BorderLayout.EAST);

        // Komponen dalam panel kanan
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 0, 8, 0); // spasi antar komponen
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Label Keyword
        JLabel keywordLabel = new JLabel("Keyword:");
        keywordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        rightPanel.add(keywordLabel, gbc);

        // TextField Keyword
        keywordField = new JTextField();
        keywordField.setPreferredSize(new Dimension(180, 30));
        keywordField.setHorizontalAlignment(SwingConstants.LEFT);
        keywordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        gbc.gridy = 1;
        rightPanel.add(keywordField, gbc);

        // Tombol Start
        startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(180, 35));
        gbc.gridy = 2;
        rightPanel.add(startButton, gbc);

        // Tombol Stop
        stopButton = new JButton("Stop");
        stopButton.setPreferredSize(new Dimension(180, 35));
        gbc.gridy = 3;
        rightPanel.add(stopButton, gbc);

        // Spacer kosong agar tombol Exit berada di bawah
        gbc.gridy = 4;
        gbc.weighty = 1;
        rightPanel.add(Box.createVerticalGlue(), gbc);

        // Tombol Exit
        exitButton = new JButton("Exit");
        exitButton.setPreferredSize(new Dimension(180, 35));
        gbc.gridy = 5;
        gbc.weighty = 0;
        rightPanel.add(exitButton, gbc);

        // Action tombol START
        startButton.addActionListener(e -> {
            String keyword = keywordField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Keyword tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            resultArea.setText("");
            scrapingThread = new Thread(() -> new Scraper().scrapeNews(keyword, 5, resultArea));
            scrapingThread.start();
        });

        // Action tombol STOP
        stopButton.addActionListener(e -> {
            if (scrapingThread != null && scrapingThread.isAlive()) {
                scrapingThread.interrupt();
                resultArea.append("🛑 Proses scraping dihentikan oleh pengguna.\n");
            }
        });

        // Action tombol EXIT
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah kamu yakin ingin keluar?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (scrapingThread != null && scrapingThread.isAlive()) {
                    scrapingThread.interrupt();
                }
                System.exit(0);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ScrapperGUI::new);
    }
}
