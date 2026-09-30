import javax.swing.*;
import java.awt.*;

public class SettingsFrame extends JFrame {

    /*
     * ==========================================
     * РАЗМЕР ПОЛЯ
     * ==========================================
     */

    private final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(SimulationConfig.DEFAULT_WIDTH, 5, 200, 1));

    private final JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(SimulationConfig.DEFAULT_HEIGHT, 5, 200, 1));

    /*
     * ==========================================
     * РАСТЕНИЯ
     * ==========================================
     */

    private final JSpinner plantsCountSpinner = new JSpinner(new SpinnerNumberModel(SimulationConfig.DEFAULT_PLANTS, 0, 10000, 1));

    private final JSpinner plantsEnergySpinner = new JSpinner(new SpinnerNumberModel(SimulationConfig.DEFAULT_PLANT_ENERGY, 0, Plant.MAX_ENERGY, 1));

    /*
     * ==========================================
     * ТРАВОЯДНЫЕ
     * ==========================================
     */

    private final JSpinner herbivoresCountSpinner = new JSpinner(new SpinnerNumberModel(SimulationConfig.DEFAULT_HERBIVORES, 0, 10000, 1));

    private final JSpinner herbivoresEnergySpinner = new JSpinner(new SpinnerNumberModel(SimulationConfig.DEFAULT_HERBIVORE_ENERGY, 0, Herbivore.MAX_ENERGY, 1));

    /*
     * ==========================================
     * ХИЩНИКИ
     * ==========================================
     */

    private final JSpinner predatorsCountSpinner = new JSpinner(new SpinnerNumberModel(SimulationConfig.DEFAULT_PREDATORS, 0, 10000, 1));

    private final JSpinner predatorsEnergySpinner = new JSpinner(new SpinnerNumberModel(SimulationConfig.DEFAULT_PREDATOR_ENERGY, 0, Predator.MAX_ENERGY, 1));

    /*
     * ==========================================
     * КОНСТРУКТОР
     * ==========================================
     */

    public SettingsFrame() {

        super("Настройка симуляции");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        /*
         * Основная панель.
         */
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        /*
         * ======================================
         * ЗАГОЛОВОК
         * ======================================
         */

        JLabel titleLabel = new JLabel("Настройки искусственной жизни", SwingConstants.CENTER);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));

        mainPanel.add(titleLabel, BorderLayout.NORTH);

        /*
         * ======================================
         * ЦЕНТР
         * ======================================
         */

        mainPanel.add(createSettingsPanel(), BorderLayout.CENTER);

        /*
         * ======================================
         * КНОПКИ
         * ======================================
         */

        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        /*
         * Размер окна.
         */
        setMinimumSize(new Dimension(560, 510));

        pack();

        setLocationRelativeTo(null);
    }

    /*
     * ==========================================
     * ПАНЕЛЬ НАСТРОЕК
     * ==========================================
     */

    private JPanel createSettingsPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        /*
         * ======================================
         * ПОЛЕ
         * ======================================
         */

        addHeader(panel, gbc, 0, "Параметры поля");

        addRow(panel, gbc, 1, "Ширина поля:", widthSpinner);

        addRow(panel, gbc, 2, "Высота поля:", heightSpinner);

        /*
         * ======================================
         * РАСТЕНИЯ
         * ======================================
         */

        addHeader(panel, gbc, 3, "Растения 🌿");

        addRow(panel, gbc, 4, "Количество:", plantsCountSpinner);

        addRow(panel, gbc, 5, "Начальная энергия:", plantsEnergySpinner);

        addLabel(panel, gbc, 5, 2, "максимум " + Plant.MAX_ENERGY);

        /*
         * ======================================
         * ТРАВОЯДНЫЕ
         * ======================================
         */

        addHeader(panel, gbc, 6, "Травоядные 🐇");

        addRow(panel, gbc, 7, "Количество:", herbivoresCountSpinner);

        addRow(panel, gbc, 8, "Начальная энергия:", herbivoresEnergySpinner);

        addLabel(panel, gbc, 8, 2, "максимум " + Herbivore.MAX_ENERGY);

        /*
         * ======================================
         * ХИЩНИКИ
         * ======================================
         */

        addHeader(panel, gbc, 9, "Хищники 🐺");

        addRow(panel, gbc, 10, "Количество:", predatorsCountSpinner);

        addRow(panel, gbc, 11, "Начальная энергия:", predatorsEnergySpinner);

        addLabel(panel, gbc, 11, 2, "максимум " + Predator.MAX_ENERGY);

        return panel;
    }

    /*
     * Добавление заголовка секции.
     */
    private void addHeader(JPanel panel, GridBagConstraints gbc, int row, String text) {

        gbc.gridx = 0;
        gbc.gridy = row;

        gbc.gridwidth = 3;

        JLabel label = new JLabel(text);

        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));

        panel.add(label, gbc);

        gbc.gridwidth = 1;
    }

    /*
     * Добавление строки:
     * текст + компонент.
     */
    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String text, JComponent component) {

        gbc.gridx = 0;
        gbc.gridy = row;

        gbc.weightx = 1;

        panel.add(new JLabel(text), gbc);

        gbc.gridx = 1;

        gbc.weightx = 0;

        panel.add(component, gbc);
    }

    /*
     * Добавление пояснения.
     */
    private void addLabel(JPanel panel, GridBagConstraints gbc, int row, int column, String text) {

        gbc.gridx = column;
        gbc.gridy = row;

        gbc.weightx = 1;

        JLabel label = new JLabel(text);

        label.setForeground(new Color(100, 100, 100));

        panel.add(label, gbc);
    }

    /*
     * ==========================================
     * КНОПКИ
     * ==========================================
     */

    private JPanel createButtonPanel() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton startButton = new JButton("Запустить симуляцию");

        JButton exitButton = new JButton("Выход");

        /*
         * Запуск.
         */
        startButton.addActionListener(e -> startSimulation());

        /*
         * Выход.
         */
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(startButton);

        panel.add(exitButton);

        return panel;
    }

    /*
     * ==========================================
     * ЗАПУСК СИМУЛЯЦИИ
     * ==========================================
     */

    private void startSimulation() {

        int width = (Integer) widthSpinner.getValue();

        int height = (Integer) heightSpinner.getValue();

        int plants = (Integer) plantsCountSpinner.getValue();

        int herbivores = (Integer) herbivoresCountSpinner.getValue();

        int predators = (Integer) predatorsCountSpinner.getValue();

        int plantEnergy = (Integer) plantsEnergySpinner.getValue();

        int herbivoreEnergy = (Integer) herbivoresEnergySpinner.getValue();

        int predatorEnergy = (Integer) predatorsEnergySpinner.getValue();

        try {

            /*
             * Создаём конфигурацию.
             */
            SimulationConfig config = new SimulationConfig(width, height,

                    plants, plantEnergy,

                    herbivores, herbivoreEnergy,

                    predators, predatorEnergy);

            /*
             * Открываем окно симуляции.
             */
            SimulationFrame frame = new SimulationFrame(config);

            frame.setVisible(true);

            /*
             * Стартовое окно закрываем
             * только после успешного создания
             * нового.
             */
            dispose();

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка настроек", JOptionPane.ERROR_MESSAGE);
        }
    }
}