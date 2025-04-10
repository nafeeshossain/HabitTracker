import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;

public class StylishHabitTracker extends JFrame {

    // Models
    static class HabitEntry {
        String name, category, date;
        int duration;

        public HabitEntry(String name, int duration, String category) {
            this.name = name;
            this.duration = duration;
            this.category = category;
            this.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }

        public String toString() {
            return date + " | " + name + " - " + duration + " mins (" + category + ")";
        }
    }

    static class User {
        String username;
        Map<String, Integer> goals = new HashMap<>();
        ArrayList<HabitEntry> log = new ArrayList<>();

        public User(String username) {
            this.username = username;
        }

        public void addGoal(String habit, int target) {
            goals.put(habit, target);
        }

        public int todayProgress(String habit) {
            int total = 0;
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            for (HabitEntry h : log) {
                if (h.name.equals(habit) && h.date.equals(today)) {
                    total += h.duration;
                }
            }
            return total;
        }

        public String checkGoals() {
            for (String habit : goals.keySet()) {
                int done = todayProgress(habit);
                int goal = goals.get(habit);
                if (done < goal)
                    return "⚠️ Keep going! You're behind on \"" + habit + "\" (" + done + "/" + goal + " mins)";
            }
            return "🎉 Great work! You're meeting all your goals today!";
        }
    }

    // GUI
    private User currentUser;
    private DefaultListModel<String> habitListModel = new DefaultListModel<>();
    private JList<String> habitList = new JList<>(habitListModel);
    private JLabel statusLabel = new JLabel("Welcome!", SwingConstants.CENTER);

    public StylishHabitTracker(User user) {
        super("Habit Hero - Daily Tracker");
        this.currentUser = user;
        setupUI();
    }

    private void setupUI() {
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel - Gradient Header
        JPanel topPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(123, 31, 162), getWidth(), 0, new Color(32, 76, 255));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setPreferredSize(new Dimension(700, 80));
        topPanel.setLayout(new BorderLayout());
        JLabel title = new JLabel("Hello, " + currentUser.username + "! Let’s build better habits 🧠", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        topPanel.add(title, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Center - Habit list
        habitList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        JScrollPane scroll = new JScrollPane(habitList);
        scroll.setBorder(BorderFactory.createTitledBorder("Today’s Habits"));
        add(scroll, BorderLayout.CENTER);

        // Bottom - Controls
        JPanel bottom = new JPanel(new GridLayout(2, 1));
        JPanel buttons = new JPanel(new FlowLayout());
        JButton add = styledButton("➕ Add Habit", new Color(46, 204, 113));
        JButton report = styledButton("📈 Progress", new Color(52, 152, 219));
        JButton setGoal = styledButton("🎯 Set Goal", new Color(241, 196, 15));

        buttons.add(add);
        buttons.add(setGoal);
        buttons.add(report);

        bottom.add(buttons);

        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        statusLabel.setForeground(new Color(44, 62, 80));
        bottom.add(statusLabel);
        add(bottom, BorderLayout.SOUTH);

        // Action Listeners
        add.addActionListener(e -> showAddHabitDialog());
        setGoal.addActionListener(e -> showSetGoalDialog());
        report.addActionListener(e -> showReportDialog());
    }

    private JButton styledButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 35));
        return btn;
    }

    private void showAddHabitDialog() {
        JTextField nameField = new JTextField();
        JTextField durationField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Health", "Learning", "Work", "Other"});

        Object[] fields = {
                "Habit Name:", nameField,
                "Duration (minutes):", durationField,
                "Category:", categoryBox
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Add New Habit", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                int duration = Integer.parseInt(durationField.getText());
                String category = (String) categoryBox.getSelectedItem();

                HabitEntry entry = new HabitEntry(name, duration, category);
                currentUser.log.add(entry);
                habitListModel.addElement(entry.toString());
                statusLabel.setText(currentUser.checkGoals());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        }
    }

    private void showSetGoalDialog() {
        JTextField habitField = new JTextField();
        JTextField goalField = new JTextField();

        Object[] fields = {
                "Habit Name:", habitField,
                "Daily Goal (mins):", goalField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Set Habit Goal", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String habit = habitField.getText();
                int goal = Integer.parseInt(goalField.getText());
                currentUser.addGoal(habit, goal);
                statusLabel.setText("🎯 Goal for \"" + habit + "\" set to " + goal + " mins.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        }
    }

    private void showReportDialog() {
        Map<String, Integer> summary = new HashMap<>();
        for (HabitEntry h : currentUser.log) {
            summary.merge(h.name, h.duration, Integer::sum);
        }

        StringBuilder report = new StringBuilder("📊 Habit Summary:\n");
        for (String key : summary.keySet()) {
            report.append("• ").append(key).append(": ").append(summary.get(key)).append(" mins\n");
        }
        JOptionPane.showMessageDialog(this, report.toString(), "Your Habit Report", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User demoUser = new User("Nafees");
            new StylishHabitTracker(demoUser).setVisible(true);
        });
    }
}

