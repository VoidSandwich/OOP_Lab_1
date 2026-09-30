import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SimulationFrame extends JFrame {

    /*
     * ==========================================
     * СРЕДА
     * ==========================================
     */

    private final Environment env;

    /*
     * ==========================================
     * ПАНЕЛЬ СИМУЛЯЦИИ
     * ==========================================
     */

    private final SimulationPanel simulationPanel;

    /*
     * ==========================================
     * СТАТИСТИКА
     * ==========================================
     */

    private final JLabel stepLabel = new JLabel();

    private final JLabel totalLabel = new JLabel();

    private final JLabel plantsLabel = new JLabel();

    private final JLabel herbivoresLabel = new JLabel();

    private final JLabel predatorsLabel = new JLabel();

    /*
     * ==========================================
     * ФЛАГ ЗАВЕРШЕНИЯ
     * ==========================================
     *
     * Нужен, чтобы окно окончания
     * не показывалось повторно
     * при последовательных нажатиях.
     */

    private boolean simulationFinished = false;

    /*
     * ==========================================
     * КОНСТРУКТОР
     * ==========================================
     */

    public SimulationFrame(SimulationConfig config) {

        super("Искусственная жизнь — пищевая цепочка");

        /*
         * ======================================
         * СОЗДАЁМ СРЕДУ
         * ======================================
         */

        this.env = new Environment(config.getWidth(), config.getHeight());

        /*
         * ======================================
         * СОЗДАЁМ СТАРТОВУЮ ПОПУЛЯЦИЮ
         * ======================================
         */

        spawnInitialAgents(config);

        /*
         * ======================================
         * СОЗДАЁМ ПАНЕЛЬ ПОЛЯ
         * ======================================
         */

        this.simulationPanel = new SimulationPanel(env);

        /*
         * ======================================
         * ОБЩИЕ НАСТРОЙКИ
         * ======================================
         */

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        /*
         * Верх.
         */
        add(createTopPanel(), BorderLayout.NORTH);

        /*
         * Центр.
         */
        add(createCenterPanel(), BorderLayout.CENTER);

        /*
         * Низ.
         */
        add(createBottomPanel(), BorderLayout.SOUTH);

        /*
         * Информация.
         */
        updateInfo();

        /*
         * Минимальный размер.
         */
        setMinimumSize(new Dimension(1000, 700));

        /*
         * Размер по компонентам.
         */
        pack();

        /*
         * Центр экрана.
         */
        setLocationRelativeTo(null);

        /*
         * Enter = шаг ×1.
         *
         * Это сохраняет требование
         * дискретного управления клавишей Enter.
         */
        setupKeyboardControls();
    }

    /*
     * ==========================================
     * ВЕРХНЯЯ ПАНЕЛЬ
     * ==========================================
     */

    private JPanel createTopPanel() {

        // Главная панель верхней части с отступами
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // Панель статистики в 1 ряд на 5 колонок
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 10, 5));

        /*
         * Центрирование текста.
         */
        stepLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        plantsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        herbivoresLabel.setHorizontalAlignment(SwingConstants.CENTER);
        predatorsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        /*
         * Информационные блоки.
         */
        statsPanel.add(makeInfoBlock("Шаг", stepLabel));
        statsPanel.add(makeInfoBlock("Всего", totalLabel));
        statsPanel.add(makeInfoBlock("🌿 Растения", plantsLabel));
        statsPanel.add(makeInfoBlock("🐇 Травоядные", herbivoresLabel));
        statsPanel.add(makeInfoBlock("🐺 Хищники", predatorsLabel));

        /*
         * Подсказка.
         */
        JLabel hint = new JLabel("Enter — шаг ×1. Нажмите на существо на поле, чтобы посмотреть его энергию.", SwingConstants.CENTER);
        hint.setForeground(new Color(90, 90, 90));

        // Размещение элементов, чтобы подсказка занимала всю ширину снизу
        mainPanel.add(statsPanel, BorderLayout.CENTER);
        mainPanel.add(hint, BorderLayout.SOUTH);

        return mainPanel;
    }

    /*
     * ==========================================
     * ИНФОРМАЦИОННЫЙ БЛОК
     * ==========================================
     */

    private JPanel makeInfoBlock(String title, JLabel valueLabel) {

        JPanel panel = new JPanel(new BorderLayout(2, 2));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 16f));

        panel.add(titleLabel, BorderLayout.NORTH);

        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    /*
     * ==========================================
     * ЦЕНТРАЛЬНАЯ ОБЛАСТЬ
     * ==========================================
     */

    private JScrollPane createCenterPanel() {

        JPanel wrapper = new JPanel(new GridBagLayout());

        wrapper.add(simulationPanel);

        JScrollPane scrollPane = new JScrollPane(wrapper);

        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        return scrollPane;
    }

    /*
     * ==========================================
     * НИЖНЯЯ ПАНЕЛЬ
     * ==========================================
     */

    private JPanel createBottomPanel() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        /*
         * Кнопки шагов.
         */

        JButton step1Button = new JButton("Шаг ×1");

        JButton step5Button = new JButton("Шаг ×5");

        JButton step10Button = new JButton("Шаг ×10");

        JButton step100Button = new JButton("Шаг ×100");

        /*
         * Дополнительные кнопки.
         */

        JButton statisticsButton = new JButton("Статистика");

        JButton restartButton = new JButton("Новая симуляция");

        /*
         * ======================================
         * ACTION LISTENERS
         * ======================================
         */

        step1Button.addActionListener(e -> performSteps(1));

        step5Button.addActionListener(e -> performSteps(5));

        step10Button.addActionListener(e -> performSteps(10));

        step100Button.addActionListener(e -> performSteps(100));

        statisticsButton.addActionListener(e -> showStatistics());

        restartButton.addActionListener(e -> {

            dispose();

            SwingUtilities.invokeLater(Main::showSettings);
        });

        /*
         * Добавляем кнопки.
         */

        panel.add(step1Button);

        panel.add(step5Button);

        panel.add(step10Button);

        panel.add(step100Button);

        panel.add(statisticsButton);

        panel.add(restartButton);

        return panel;
    }

    /*
     * ==========================================
     * КЛАВИАТУРА
     * ==========================================
     */

    private void setupKeyboardControls() {

        JRootPane rootPane = getRootPane();

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "simulationStep");

        actionMap.put("simulationStep", new AbstractAction() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {

                performSteps(1);
            }
        });
    }

    /*
     * ==========================================
     * ВЫПОЛНЕНИЕ ШАГОВ
     * ==========================================
     */

    private void performSteps(int count) {

        /*
         * Если симуляция уже закончилась,
         * новые ходы не выполняем.
         */
        if (simulationFinished) {
            return;
        }

        /*
         * Выполняем указанное число шагов.
         */
        for (int i = 0; i < count; i++) {

            /*
             * Если все агенты погибли,
             * прекращаем серию шагов.
             */
            if (env.getAgentCount() == 0) {
                break;
            }

            env.step();
        }

        /*
         * Перерисовываем поле.
         */
        simulationPanel.repaint();

        /*
         * Обновляем верхнюю статистику.
         */
        updateInfo();

        /*
         * Проверяем окончание.
         */
        if (env.getAgentCount() == 0) {

            showSimulationFinished();
        }
    }

    /*
     * ==========================================
     * ОБНОВЛЕНИЕ СТАТИСТИКИ
     * ==========================================
     */

    private void updateInfo() {

        stepLabel.setText(String.valueOf(env.getStep()));

        totalLabel.setText(String.valueOf(env.getAgentCount()));

        plantsLabel.setText(String.valueOf(env.getPlantCount()));

        herbivoresLabel.setText(String.valueOf(env.getHerbivoreCount()));

        predatorsLabel.setText(String.valueOf(env.getPredatorCount()));
    }

    /*
     * ==========================================
     * ЗАВЕРШЕНИЕ
     * ==========================================
     */

    private void showSimulationFinished() {

        if (simulationFinished) {
            return;
        }

        simulationFinished = true;

        JOptionPane.showMessageDialog(this,

                "Все существа погибли.\n" + "Симуляция достигла шага " + env.getStep() + ".",

                "Симуляция завершена",

                JOptionPane.INFORMATION_MESSAGE);
    }

    /*
     * ==========================================
     * ИТОГОВАЯ СТАТИСТИКА
     * ==========================================
     */

    private void showStatistics() {

        String text = "Актуальная статистика\n\n" + "Продолжительность: " + env.getStep() + " шагов\n" + "🌿 Растений: " + env.getPlantCount() + "\n" + "🐇 Травоядных: " + env.getHerbivoreCount() + "\n" + "🐺 Хищников: " + env.getPredatorCount() + "\n" + "Всего сущностей: " + env.getAgentCount();

        JOptionPane.showMessageDialog(this,

                text,

                "Статистика",

                JOptionPane.INFORMATION_MESSAGE);
    }

    /*
     * ==========================================
     * СОЗДАНИЕ НАЧАЛЬНОЙ ПОПУЛЯЦИИ
     * ==========================================
     */

    private void spawnInitialAgents(SimulationConfig config) {

        Random random = new Random();

        /*
         * Растения.
         */
        spawn(random, config.getPlantCount(), 0, config.getPlantInitialEnergy());

        /*
         * Травоядные.
         */
        spawn(random, config.getHerbivoreCount(), 1, config.getHerbivoreInitialEnergy());

        /*
         * Хищники.
         */
        spawn(random, config.getPredatorCount(), 2, config.getPredatorInitialEnergy());
    }

    /*
     * ==========================================
     * РАЗМЕЩЕНИЕ АГЕНТОВ
     * ==========================================
     *
     * 0 = растение
     * 1 = травоядное
     * 2 = хищник
     */

    private void spawn(Random random, int amount, int type, int initialEnergy) {

        int created = 0;

        /*
         * Такой же принцип,
         * как в консольной версии:
         * ограничиваем число попыток,
         * чтобы при полном поле
         * цикл гарантированно завершился.
         */
        int maxAttempts = env.getWidth() * env.getHeight() * 20;

        int attempts = 0;

        while (created < amount && attempts++ < maxAttempts) {

            int x = random.nextInt(env.getWidth());

            int y = random.nextInt(env.getHeight());

            /*
             * Занятая клетка.
             */
            if (env.getAgent(x, y) != null) {

                continue;
            }

            Agent agent;

            /*
             * Создание растения.
             */
            if (type == 0) {

                agent = new Plant(env, x, y, initialEnergy);

            }

            /*
             * Создание травоядного.
             */
            else if (type == 1) {

                agent = new Herbivore(env, x, y, initialEnergy);

            }

            /*
             * Создание хищника.
             */
            else {

                agent = new Predator(env, x, y, initialEnergy);
            }

            /*
             * Добавляем в Environment.
             */
            if (env.addAgent(agent, x, y)) {

                created++;
            }
        }

        /*
         * Если разместить всю запрошенную
         * популяцию не удалось,
         * предупреждаем пользователя.
         */
        if (created < amount) {

            JOptionPane.showMessageDialog(this,

                    "Не удалось разместить " + "всех существ.\n" + "Запрошено: " + amount + "\nСоздано: " + created,

                    "Предупреждение",

                    JOptionPane.WARNING_MESSAGE);
        }
    }
}