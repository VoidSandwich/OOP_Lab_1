import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SimulationPanel extends JPanel {

    /*
     * ==========================================
     * РАЗМЕРЫ ОТРИСОВКИ
     * ==========================================
     */

    /*
     * Размер одной клетки.
     */
    private static final int CELL_SIZE = 30;

    /*
     * Отступ игрового поля от края панели.
     */
    private static final int PADDING = 4;

    /*
     * ==========================================
     * ЦВЕТА КЛЕТОК
     * ==========================================
     */

    /*
     * Пустая клетка.
     */
    private static final Color EMPTY_CELL_COLOR = Color.WHITE;

    /*
     * Клетка растения.
     */
    private static final Color PLANT_CELL_COLOR = new Color(220, 245, 220);

    /*
     * Клетка травоядного.
     */
    private static final Color HERBIVORE_CELL_COLOR = new Color(220, 235, 255);

    /*
     * Клетка хищника.
     */
    private static final Color PREDATOR_CELL_COLOR = new Color(255, 220, 220);

    /*
     * Линии сетки.
     */
    private static final Color GRID_COLOR = new Color(210, 210, 210);

    /*
     * Внешняя граница поля.
     */
    private static final Color BORDER_COLOR = new Color(150, 150, 150);

    /*
     * ==========================================
     * ЦВЕТА АГЕНТОВ
     * ==========================================
     */

    private static final Color PLANT_TEXT_COLOR = new Color(0, 150, 0);

    private static final Color HERBIVORE_TEXT_COLOR = new Color(0, 90, 200);

    private static final Color PREDATOR_TEXT_COLOR = new Color(200, 0, 0);

    /*
     * ==========================================
     * СРЕДА
     * ==========================================
     */

    private final Environment env;

    /*
     * Шрифт эмодзи.
     */
    private final Font emojiFont;

    /*
     * ==========================================
     * КОНСТРУКТОР
     * ==========================================
     */

    public SimulationPanel(Environment env) {

        if (env == null) {

            throw new IllegalArgumentException("Среда не может быть null.");
        }

        this.env = env;

        /*
         * Находим шрифт один раз.
         *
         * Не нужно делать это
         * при каждой перерисовке.
         */
        this.emojiFont = findEmojiFont();

        /*
         * Белый фон.
         */
        setBackground(EMPTY_CELL_COLOR);

        /*
         * Размер панели определяется
         * размером игрового поля.
         */
        setPreferredSize(new Dimension(env.getWidth() * CELL_SIZE + PADDING * 2,

                env.getHeight() * CELL_SIZE + PADDING * 2));

        /*
         * Рамка вокруг поля.
         */
        setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        /*
         * ======================================
         * КЛИК ПО КЛЕТКЕ
         * ======================================
         */

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                showAgentDetails(e.getX(), e.getY());
            }
        });
    }

    /*
     * ==========================================
     * ПЕРЕРИСОВКА
     * ==========================================
     */

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        /*
         * Создаём отдельную копию
         * объекта Graphics.
         */
        Graphics2D g2 = (Graphics2D) g.create();

        try {

            /*
             * Сглаживание текста.
             */
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            /*
             * Более качественная отрисовка.
             */
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            /*
             * ==================================
             * ОБХОД ВСЕГО ПОЛЯ
             * ==================================
             */

            for (int y = 0; y < env.getHeight(); y++) {

                for (int x = 0; x < env.getWidth(); x++) {

                    /*
                     * Пиксельная позиция клетки.
                     */
                    int px = PADDING + x * CELL_SIZE;

                    int py = PADDING + y * CELL_SIZE;

                    /*
                     * Получаем агента.
                     */
                    Agent agent = env.getAgent(x, y);

                    /*
                     * ==================================
                     * ФОН КЛЕТКИ
                     * ==================================
                     */

                    Color cellColor = getCellColor(agent);

                    g2.setColor(cellColor);

                    g2.fillRect(px, py, CELL_SIZE, CELL_SIZE);

                    /*
                     * ==================================
                     * ЛИНИЯ СЕТКИ
                     * ==================================
                     */

                    g2.setColor(GRID_COLOR);

                    g2.drawRect(px, py, CELL_SIZE, CELL_SIZE);

                    /*
                     * ==================================
                     * ПУСТАЯ КЛЕТКА
                     * ==================================
                     */

                    if (agent == null) {
                        continue;
                    }

                    /*
                     * ==================================
                     * ОТРИСОВКА АГЕНТА
                     * ==================================
                     */

                    drawAgent(g2, agent, px, py);
                }
            }

        } finally {

            /*
             * Освобождаем Graphics.
             */
            g2.dispose();
        }
    }

    /*
     * ==========================================
     * ЦВЕТ ФОНА КЛЕТКИ
     * ==========================================
     */

    private Color getCellColor(Agent agent) {

        if (agent instanceof Plant) {

            return PLANT_CELL_COLOR;
        }

        if (agent instanceof Herbivore) {

            return HERBIVORE_CELL_COLOR;
        }

        if (agent instanceof Predator) {

            return PREDATOR_CELL_COLOR;
        }

        return EMPTY_CELL_COLOR;
    }

    /*
     * ==========================================
     * ЦВЕТ АГЕНТА
     * ==========================================
     */

    private Color getAgentColor(Agent agent) {

        if (agent instanceof Plant) {

            return PLANT_TEXT_COLOR;
        }

        if (agent instanceof Herbivore) {

            return HERBIVORE_TEXT_COLOR;
        }

        if (agent instanceof Predator) {

            return PREDATOR_TEXT_COLOR;
        }

        return Color.BLACK;
    }

    /*
     * ==========================================
     * ОТРИСОВКА АГЕНТА
     * ==========================================
     */

    private void drawAgent(Graphics2D g2, Agent agent, int px, int py) {

        /*
         * Устанавливаем шрифт эмодзи.
         */
        g2.setFont(emojiFont);

        /*
         * Цвет соответствующего вида.
         */
        g2.setColor(getAgentColor(agent));

        /*
         * Получаем emoji.
         */
        String emoji = agent.getEmoji();

        FontMetrics metrics = g2.getFontMetrics();

        /*
         * Размер текста.
         */
        int textWidth = metrics.stringWidth(emoji);

        int textHeight = metrics.getAscent();

        /*
         * Центрируем emoji
         * внутри клетки.
         */
        int drawX = px + (CELL_SIZE - textWidth) / 2;

        int drawY = py + (CELL_SIZE + textHeight) / 2 - 3;

        /*
         * Рисуем emoji.
         */
        g2.drawString(emoji, drawX, drawY);
    }

    /*
     * ==========================================
     * ШРИФТ ЭМОДЗИ
     * ==========================================
     */

    private Font findEmojiFont() {

        /*
         * Приоритетные шрифты.
         */
        String[] preferredFonts = {

                "Noto Color Emoji",

                "Segoe UI Emoji",

                "Apple Color Emoji",

                "Dialog"};

        /*
         * Шрифты, доступные
         * в текущей системе.
         */
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        /*
         * Ищем подходящий.
         */
        for (String preferred : preferredFonts) {

            for (String available : availableFonts) {

                if (available.equalsIgnoreCase(preferred)) {

                    return new Font(available, Font.PLAIN, 22);
                }
            }
        }

        /*
         * Запасной вариант.
         */
        return new Font("Dialog", Font.PLAIN, 22);
    }

    /*
     * ==========================================
     * ПОЛУЧЕНИЕ КООРДИНАТЫ КЛЕТКИ
     * ==========================================
     */

    private int getCellX(int mouseX) {

        return (mouseX - PADDING) / CELL_SIZE;
    }

    private int getCellY(int mouseY) {

        return (mouseY - PADDING) / CELL_SIZE;
    }

    /*
     * ==========================================
     * КЛИК ПО АГЕНТУ
     * ==========================================
     */

    private void showAgentDetails(int mouseX, int mouseY) {

        /*
         * Переводим координаты мыши
         * в координаты игровой клетки.
         */
        int x = getCellX(mouseX);

        int y = getCellY(mouseY);

        /*
         * Клик за пределами поля.
         */
        if (x < 0 || x >= env.getWidth() || y < 0 || y >= env.getHeight()) {

            return;
        }

        /*
         * Получаем агента.
         */
        Agent agent = env.getAgent(x, y);

        /*
         * Пустая клетка.
         */
        if (agent == null) {
            return;
        }

        /*
         * Открываем отдельное окно
         * информации.
         */
        AgentInfoDialog.show(this, agent);
    }
}