public class Herbivore extends Agent {

    /*
     * ==========================================
     * ЭНЕРГИЯ
     * ==========================================
     */

    /*
     * Начальная энергия по умолчанию
     * из консольной версии.
     */
    public static final int INITIAL_ENERGY = 30;

    /*
     * Максимальная энергия.
     */
    public static final int MAX_ENERGY = 65;

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
     * Стоимость перемещения на 1 клетку.
     */
    private static final int MOVE_COST = 1;

    /*
     * ==========================================
     * ПИТАНИЕ
     * ==========================================
     */

    /*
     * Энергия, получаемая после
     * поедания растения.
     *
     * Это значение оставлено ровно
     * таким, как в консольной версии.
     */
    private static final int FOOD_ENERGY = 5;

    /*
     * ==========================================
     * РАЗМНОЖЕНИЕ
     * ==========================================
     */

    /*
     * Порог размножения.
     */
    private static final int REPRODUCTION_THRESHOLD = 52;

    /*
     * Энергия, передаваемая ребёнку.
     */
    private static final int OFFSPRING_ENERGY = 20;

    /*
     * ==========================================
     * ЗРЕНИЕ
     * ==========================================
     *
     * Радиус 2 = область 5x5.
     */
    private static final int VISION_RADIUS = 2;

    /*
     * ==========================================
     * КОНСТРУКТОРЫ
     * ==========================================
     */

    /*
     * Конструктор со стандартной энергией.
     */
    public Herbivore(Environment env, int x, int y) {

        this(env, x, y, INITIAL_ENERGY);
    }

    /*
     * Конструктор с пользовательской
     * начальной энергией.
     *
     * Используется оконным приложением.
     */
    public Herbivore(Environment env, int x, int y, int initialEnergy) {

        super(env, x, y, initialEnergy, 'Т');
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
        return "🐇";
    }

    /*
     * ==========================================
     * ДЕЙСТВИЕ
     * ==========================================
     *
     * Приоритет действий:
     *
     * 1. Побег от хищника.
     * 2. Движение к растению.
     * 3. Случайное движение.
     * 4. Размножение.
     *
     * В самой консольной версии
     * движение всегда на одну клетку.
     */

    @Override
    public void act() {

        /*
         * Сначала выполняется метаболизм.
         */
        if (!beginTurn()) {
            return;
        }

        /*
         * Ищем ближайшего видимого хищника.
         */
        Predator nearestPredator = findNearestPredator();

        /*
         * Одновременно ищем ближайшее
         * видимое растение.
         */
        Plant nearestPlant = findNearestPlant();

        /*
         * ======================================
         * ПРИОРИТЕТ ЛОГИКИ
         * ======================================
         */

        if (nearestPredator != null) {

            /*
             * Хищник является приоритетом.
             */
            flee();

        } else if (nearestPlant != null) {

            /*
             * Хищников нет —
             * двигаемся к растению.
             */
            moveToPlant(nearestPlant);

        } else {

            /*
             * Никого не видим —
             * случайно блуждаем.
             */
            wander();
        }

        /*
         * После основного действия
         * проверяем размножение.
         */
        tryReproduce();
    }

    /*
     * ==========================================
     * ЗРЕНИЕ: ХИЩНИК
     * ==========================================
     */

    private Predator findNearestPredator() {

        Predator best = null;

        int bestDistance = Integer.MAX_VALUE;

        /*
         * Анализируем квадрат 5x5
         * вокруг текущего положения.
         */
        for (int dy = -VISION_RADIUS; dy <= VISION_RADIUS; dy++) {

            for (int dx = -VISION_RADIUS; dx <= VISION_RADIUS; dx++) {

                /*
                 * Собственную клетку
                 * не рассматриваем.
                 */
                if (dx == 0 && dy == 0) {
                    continue;
                }

                int scanX = getX() + dx;

                int scanY = getY() + dy;

                /*
                 * За пределами поля
                 * клетка не существует.
                 */
                if (!env.isInside(scanX, scanY)) {
                    continue;
                }

                Agent agent = env.getAgent(scanX, scanY);

                if (agent instanceof Predator) {

                    int distance = env.getManhattanDistance(getX(), getY(), agent.getX(), agent.getY());

                    /*
                     * Выбираем ближайшего.
                     */
                    if (distance < bestDistance) {

                        bestDistance = distance;

                        best = (Predator) agent;
                    }
                }
            }
        }

        return best;
    }

    /*
     * ==========================================
     * ЗРЕНИЕ: РАСТЕНИЕ
     * ==========================================
     */

    private Plant findNearestPlant() {

        Plant best = null;

        int bestDistance = Integer.MAX_VALUE;

        /*
         * Анализ 5x5.
         */
        for (int dy = -VISION_RADIUS; dy <= VISION_RADIUS; dy++) {

            for (int dx = -VISION_RADIUS; dx <= VISION_RADIUS; dx++) {

                /*
                 * Собственную клетку
                 * пропускаем.
                 */
                if (dx == 0 && dy == 0) {
                    continue;
                }

                int scanX = getX() + dx;

                int scanY = getY() + dy;

                if (!env.isInside(scanX, scanY)) {
                    continue;
                }

                Agent agent = env.getAgent(scanX, scanY);

                if (agent instanceof Plant) {

                    int distance = env.getManhattanDistance(getX(), getY(), agent.getX(), agent.getY());

                    /*
                     * Берём ближайшее растение.
                     */
                    if (distance < bestDistance) {

                        bestDistance = distance;

                        best = (Plant) agent;
                    }
                }
            }
        }

        return best;
    }

    /*
     * ==========================================
     * ПОБЕГ
     * ==========================================
     */

    private void flee() {

        Predator predator = findNearestPredator();

        /*
         * Если хищник исчез из поля зрения,
         * переходим к обычному блужданию.
         */
        if (predator == null) {

            wander();

            return;
        }

        /*
         * Выбираем наиболее безопасную
         * соседнюю клетку.
         */
        moveBestEscape();
    }

    /*
     * ==========================================
     * ЛУЧШАЯ КЛЕТКА ДЛЯ ПОБЕГА
     * ==========================================
     *
     * Для каждой из 4 соседних клеток
     * повторно анализируется зона зрения 5x5.
     *
     * Критерии:
     *
     * 1. Максимальная дистанция
     *    до ближайшего хищника.
     *
     * 2. При равенстве —
     *    максимальная суммарная дистанция
     *    до видимых хищников.
     */

    private boolean moveBestEscape() {

        int bestX = getX();

        int bestY = getY();

        int bestMinDistance = Integer.MIN_VALUE;

        int bestTotalDistance = Integer.MIN_VALUE;

        boolean found = false;

        /*
         * Направления:
         *
         * вверх
         * вниз
         * влево
         * вправо
         */
        final int[] dirX = {0, 0, -1, 1};

        final int[] dirY = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) {

            int newX = getX() + dirX[i];

            int newY = getY() + dirY[i];

            /*
             * За границу выходить нельзя.
             */
            if (!env.isInside(newX, newY)) {
                continue;
            }

            Agent targetAgent = env.getAgent(newX, newY);

            /*
             * При побеге разрешены:
             *
             * - свободная клетка;
             * - растение.
             *
             * Другие агенты являются
             * препятствием.
             */
            if (targetAgent != null && !(targetAgent instanceof Plant)) {

                continue;
            }

            /*
             * Минимальная дистанция
             * до видимого хищника.
             */
            int minDistance = Integer.MAX_VALUE;

            /*
             * Суммарная дистанция
             * до всех видимых хищников.
             */
            int totalDistance = 0;

            /*
             * Повторное сканирование 5x5
             * уже относительно предполагаемой
             * новой клетки.
             */
            for (int dy = -VISION_RADIUS; dy <= VISION_RADIUS; dy++) {

                for (int dx = -VISION_RADIUS; dx <= VISION_RADIUS; dx++) {

                    int scanX = newX + dx;

                    int scanY = newY + dy;

                    if (!env.isInside(scanX, scanY)) {
                        continue;
                    }

                    Agent agent = env.getAgent(scanX, scanY);

                    if (agent instanceof Predator) {

                        int predatorDistance = env.getManhattanDistance(newX, newY, agent.getX(), agent.getY());

                        minDistance = Math.min(minDistance, predatorDistance);

                        totalDistance += predatorDistance;
                    }
                }
            }

            /*
             * Хищников не увидели.
             */
            if (minDistance == Integer.MAX_VALUE) {

                minDistance = 0;
            }

            /*
             * Главный критерий:
             * ближайший хищник как можно дальше.
             *
             * Второй критерий:
             * суммарное расстояние как можно больше.
             */
            if (!found || minDistance > bestMinDistance || (minDistance == bestMinDistance && totalDistance > bestTotalDistance)) {

                found = true;

                bestX = newX;

                bestY = newY;

                bestMinDistance = minDistance;

                bestTotalDistance = totalDistance;
            }
        }

        /*
         * Подходящей клетки нет.
         */
        if (!found) {
            return false;
        }

        Agent targetAgent = env.getAgent(bestX, bestY);

        /*
         * Если на выбранной клетке растение,
         * сразу съедаем его.
         */
        if (targetAgent instanceof Plant) {

            return env.moveIntoFood(this, targetAgent, MOVE_COST, FOOD_ENERGY);
        }

        /*
         * Иначе просто двигаемся.
         */
        return env.moveAgent(this, bestX, bestY, MOVE_COST);
    }

    /*
     * ==========================================
     * ДВИЖЕНИЕ К РАСТЕНИЮ
     * ==========================================
     */

    private void moveToPlant(Plant target) {

        int distance = env.getManhattanDistance(getX(), getY(), target.getX(), target.getY());

        /*
         * Если растение рядом —
         * сразу съедаем.
         */
        if (distance == 1) {

            env.moveIntoFood(this, target, MOVE_COST, FOOD_ENERGY);

            return;
        }

        int bestX = -1;
        int bestY = -1;

        int bestDistance = Integer.MAX_VALUE;

        final int[] dirX = {0, 0, -1, 1};

        final int[] dirY = {-1, 1, 0, 0};

        /*
         * Проверяем 4 возможных направления.
         */
        for (int i = 0; i < 4; i++) {

            int newX = getX() + dirX[i];

            int newY = getY() + dirY[i];

            /*
             * Границы поля.
             */
            if (!env.isInside(newX, newY)) {
                continue;
            }

            /*
             * Через занятую клетку
             * идти нельзя.
             */
            if (env.getAgent(newX, newY) != null) {

                continue;
            }

            /*
             * Считаем расстояние
             * от кандидата до растения.
             */
            int candidateDistance = env.getManhattanDistance(newX, newY, target.getX(), target.getY());

            /*
             * Выбираем клетку,
             * которая уменьшает расстояние
             * до растения сильнее всего.
             */
            if (candidateDistance < bestDistance) {

                bestDistance = candidateDistance;

                bestX = newX;

                bestY = newY;
            }
        }

        /*
         * Нашли путь.
         */
        if (bestX >= 0) {

            env.moveAgent(this, bestX, bestY, MOVE_COST);

        } else {

            /*
             * Все направления перекрыты —
             * случайное движение.
             */
            wander();
        }
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
         * Первое направление выбирается
         * случайно.
         */
        int start = env.getRandom().nextInt(4);

        /*
         * После этого проверяем
         * остальные направления
         * по кругу.
         */
        for (int i = 0; i < 4; i++) {

            int index = (start + i) % 4;

            int newX = getX() + dirX[index];

            int newY = getY() + dirY[index];

            /*
             * Жёсткая граница поля.
             */
            if (!env.isInside(newX, newY)) {
                continue;
            }

            /*
             * В занятую клетку нельзя.
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

        /*
         * Потомок получает стандартную
         * начальную энергию класса.
         *
         * Затем родитель отдаёт ему
         * OFFSPRING_ENERGY в tryReproduce().
         */
        return new Herbivore(env, x, y);
    }
}