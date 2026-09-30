public class Predator extends Agent {

    /*
     * ==========================================
     * ЭНЕРГИЯ
     * ==========================================
     */

    /*
     * Начальная энергия по умолчанию
     * из консольной версии.
     */
    public static final int INITIAL_ENERGY = 72;

    /*
     * Максимальная энергия.
     */
    public static final int MAX_ENERGY = 120;

    /*
     * ==========================================
     * ЗАТРАТЫ
     * ==========================================
     */

    /*
     * Метаболизм за один ход.
     */
    private static final int TURN_ENERGY_COST = 1;

    /*
     * Стоимость одного перемещения/атаки.
     */
    private static final int MOVE_COST = 1;

    /*
     * ==========================================
     * ПИТАНИЕ
     * ==========================================
     */

    /*
     * Энергия от съеденного травоядного.
     */
    private static final int MEAT_ENERGY = 64;

    /*
     * ==========================================
     * РАЗМНОЖЕНИЕ
     * ==========================================
     */

    /*
     * Порог размножения.
     */
    private static final int REPRODUCTION_THRESHOLD = 80;

    /*
     * Энергия потомка.
     */
    private static final int OFFSPRING_ENERGY = 55;

    /*
     * ==========================================
     * ЗРЕНИЕ
     * ==========================================
     *
     * Радиус 2 = поле зрения 5x5.
     */
    private static final int VISION_RADIUS = 2;

    /*
     * ==========================================
     * ПАМЯТЬ ЦЕЛИ
     * ==========================================
     *
     * После потери травоядного
     * хищник ещё 2 хода движется
     * к последней известной позиции.
     */
    private static final int MEMORY_TURNS = 2;

    /*
     * Последняя известная координата
     * травоядного.
     */
    private int lastKnownTargetX = -1;
    private int lastKnownTargetY = -1;

    /*
     * Сколько ходов ещё действует память.
     */
    private int memoryTurnsLeft = 0;

    /*
     * ==========================================
     * КОНСТРУКТОРЫ
     * ==========================================
     */

    /*
     * Конструктор со стандартной
     * энергией консольной версии.
     */
    public Predator(Environment env, int x, int y) {

        this(env, x, y, INITIAL_ENERGY);
    }

    /*
     * Конструктор с пользовательской
     * начальной энергией.
     */
    public Predator(Environment env, int x, int y, int initialEnergy) {

        super(env, x, y, initialEnergy, 'Х');
    }

    /*
     * ==========================================
     * ЭНЕРГИЯ
     * ==========================================
     */

    @Override
    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    @Override
    protected int getTurnEnergyCost() {
        return TURN_ENERGY_COST;
    }

    @Override
    protected int getReproductionThreshold() {
        return REPRODUCTION_THRESHOLD;
    }

    @Override
    protected int getOffspringEnergy() {
        return OFFSPRING_ENERGY;
    }

    /*
     * ==========================================
     * EMOJI
     * ==========================================
     */

    @Override
    public String getEmoji() {
        return "🐺";
    }

    /*
     * ==========================================
     * ДЕЙСТВИЕ
     * ==========================================
     *
     * Приоритет:
     *
     * 1. Видит травоядное -> преследует.
     * 2. Не видит, но память ещё действует
     *    -> идёт к последней позиции.
     * 3. Иначе блуждает.
     * 4. После этого проверяет размножение.
     */

    @Override
    public void act() {

        /*
         * Сначала метаболизм.
         */
        if (!beginTurn()) {
            return;
        }

        /*
         * Ищем ближайшее травоядное
         * в зоне зрения.
         */
        Herbivore target = findNearestHerbivore();

        /*
         * ======================================
         * ТРАВОЯДНОЕ НАЙДЕНО
         * ======================================
         */
        if (target != null) {

            /*
             * Обновляем последнюю известную
             * позицию цели.
             */
            lastKnownTargetX = target.getX();

            lastKnownTargetY = target.getY();

            /*
             * Сбрасываем память на 2 хода.
             */
            memoryTurnsLeft = MEMORY_TURNS;

            /*
             * Идём к цели / атакуем.
             */
            chase(target);

        }

        /*
         * ======================================
         * ЦЕЛЬ ПОТЕРЯНА
         * ======================================
         */
        else if (memoryTurnsLeft > 0) {

            /*
             * Продолжаем движение
             * к последнему известному месту.
             */
            moveToLastKnownPosition();

            /*
             * Один ход памяти использован.
             */
            memoryTurnsLeft--;
        }

        /*
         * ======================================
         * НИКОГО НЕТ
         * ======================================
         */
        else {

            /*
             * Случайное блуждание.
             */
            wander();
        }

        /*
         * После действия —
         * проверка размножения.
         */
        tryReproduce();
    }

    /*
     * ==========================================
     * ЗРЕНИЕ
     * ==========================================
     */

    private Herbivore findNearestHerbivore() {

        Herbivore best = null;

        int bestDistance = Integer.MAX_VALUE;

        /*
         * Сканируем область 5x5.
         */
        for (int dy = -VISION_RADIUS; dy <= VISION_RADIUS; dy++) {

            for (int dx = -VISION_RADIUS; dx <= VISION_RADIUS; dx++) {

                /*
                 * Себя не проверяем.
                 */
                if (dx == 0 && dy == 0) {
                    continue;
                }

                int scanX = getX() + dx;

                int scanY = getY() + dy;

                /*
                 * За границу поля
                 * смотреть нельзя.
                 */
                if (!env.isInside(scanX, scanY)) {
                    continue;
                }

                Agent agent = env.getAgent(scanX, scanY);

                if (agent instanceof Herbivore) {

                    int distance = env.getManhattanDistance(getX(), getY(), agent.getX(), agent.getY());

                    /*
                     * Выбираем ближайшую цель.
                     */
                    if (distance < bestDistance) {

                        bestDistance = distance;

                        best = (Herbivore) agent;
                    }
                }
            }
        }

        return best;
    }

    /*
     * ==========================================
     * ПОГОНЯ
     * ==========================================
     */

    private void chase(Herbivore target) {

        int dx = target.getX() - getX();

        int dy = target.getY() - getY();

        /*
         * Манхэттенское расстояние.
         */
        int distance = Math.abs(dx) + Math.abs(dy);

        /*
         * Цель в соседней клетке:
         * атакуем.
         */
        if (distance == 1) {

            env.moveIntoPrey(this, target, MOVE_COST, MEAT_ENERGY);

            return;
        }

        /*
         * Иначе выбираем лучший
         * следующий шаг.
         */
        moveOneStepTowards(target.getX(), target.getY());
    }

    /*
     * ==========================================
     * ШАГ К ЦЕЛИ
     * ==========================================
     *
     * Разрешены только 4 направления.
     *
     * Выбирается свободная клетка,
     * максимально приближающая
     * хищника к цели.
     */

    private void moveOneStepTowards(int targetX, int targetY) {

        int bestX = -1;
        int bestY = -1;

        int bestDistance = Integer.MAX_VALUE;

        final int[] dirX = {0, 0, -1, 1};

        final int[] dirY = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) {

            int newX = getX() + dirX[i];

            int newY = getY() + dirY[i];

            /*
             * Жёсткая граница поля.
             */
            if (!env.isInside(newX, newY)) {
                continue;
            }

            /*
             * Занятая клетка —
             * препятствие.
             */
            if (env.getAgent(newX, newY) != null) {

                continue;
            }

            /*
             * Расстояние после
             * предполагаемого шага.
             */
            int distance = env.getManhattanDistance(newX, newY, targetX, targetY);

            /*
             * Выбираем самый короткий путь.
             */
            if (distance < bestDistance) {

                bestDistance = distance;

                bestX = newX;

                bestY = newY;
            }
        }

        /*
         * Нашли подходящий шаг.
         */
        if (bestX >= 0) {

            env.moveAgent(this, bestX, bestY, MOVE_COST);

        } else {

            /*
             * Путь перекрыт —
             * переходим к блужданию.
             */
            wander();
        }
    }

    /*
     * ==========================================
     * ПОСЛЕДНЯЯ ИЗВЕСТНАЯ ПОЗИЦИЯ
     * ==========================================
     */

    private void moveToLastKnownPosition() {

        /*
         * Координаты ещё не были сохранены.
         */
        if (lastKnownTargetX < 0 || lastKnownTargetY < 0) {

            wander();

            return;
        }

        int distance = env.getManhattanDistance(getX(), getY(), lastKnownTargetX, lastKnownTargetY);

        /*
         * Уже на месте.
         */
        if (distance == 0) {
            return;
        }

        /*
         * Движение только на одну клетку.
         */
        moveOneStepTowards(lastKnownTargetX, lastKnownTargetY);
    }

    /*
     * ==========================================
     * СЛУЧАЙНОЕ ДВИЖЕНИЕ
     * ==========================================
     */

    private void wander() {

        final int[] dirX = {0, 0, -1, 1};

        final int[] dirY = {-1, 1, 0, 0};

        /*
         * Случайная стартовая сторона.
         */
        int start = env.getRandom().nextInt(4);

        /*
         * Затем перебираем
         * остальные направления.
         */
        for (int i = 0; i < 4; i++) {

            int index = (start + i) % 4;

            int newX = getX() + dirX[index];

            int newY = getY() + dirY[index];

            /*
             * Граница поля.
             */
            if (!env.isInside(newX, newY)) {
                continue;
            }

            /*
             * Занятая клетка
             * не подходит.
             */
            if (env.getAgent(newX, newY) == null) {

                env.moveAgent(this, newX, newY, MOVE_COST);

                return;
            }
        }
    }

    /*
     * ==========================================
     * РОЖДЕНИЕ
     * ==========================================
     */

    @Override
    protected Agent createOffspring(int x, int y) {

        return new Predator(env, x, y);
    }
}