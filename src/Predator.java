public class Predator extends Agent {

    public static final int INITIAL_ENERGY = 72;
    public static final int MAX_ENERGY = 120;

    private static final int TURN_ENERGY_COST = 1;
    private static final int MOVE_COST = 1;

    private static final int MEAT_ENERGY = 64;

    private static final int REPRODUCTION_THRESHOLD = 80;

    private static final int OFFSPRING_ENERGY = 55;

    private static final int VISION_RADIUS = 2;

    private static final int MEMORY_TURNS = 2;

    private int lastKnownTargetX = -1;
    private int lastKnownTargetY = -1;

    private int memoryTurnsLeft = 0;

    public Predator(Environment env, int x, int y) {
        super(env, x, y, INITIAL_ENERGY, 'Х');
    }

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

    @Override
    public void act() {

        if (!beginTurn()) {
            return;
        }

        Herbivore target = findNearestHerbivore();

        /*
         * Приоритет:
         *
         * 1. Видит травоядное -> преследует.
         * 2. Потерял травоядное -> движется к его
         *    последней известной позиции ещё MEMORY_TURNS ходов.
         * 3. Иначе случайно блуждает.
         * 4. После действия рассматривается размножение.
         */

        if (target != null) {

            // Запоминаем последнюю известную позицию цели.
            lastKnownTargetX = target.getX();
            lastKnownTargetY = target.getY();

            memoryTurnsLeft = MEMORY_TURNS;

            chase(target);

        } else if (memoryTurnsLeft > 0) {

            moveToLastKnownPosition();

            memoryTurnsLeft--;

        } else {

            wander();
        }

        tryReproduce();
    }

    // Зрение

    private Herbivore findNearestHerbivore() {

        Herbivore best = null;

        int bestDistance = Integer.MAX_VALUE;

        for (int dy = -VISION_RADIUS; dy <= VISION_RADIUS; dy++) {

            for (int dx = -VISION_RADIUS; dx <= VISION_RADIUS; dx++) {

                if (dx == 0 && dy == 0) {
                    continue;
                }

                int scanX = getX() + dx;
                int scanY = getY() + dy;

                /*
                 * За пределами поля ничего не существует.
                 */
                if (!env.isInside(scanX, scanY)) {
                    continue;
                }

                Agent agent = env.getAgent(scanX, scanY);

                if (agent instanceof Herbivore) {

                    int distance = env.getManhattanDistance(getX(), getY(), agent.getX(), agent.getY());

                    if (distance < bestDistance) {

                        bestDistance = distance;

                        best = (Herbivore) agent;
                    }
                }
            }
        }

        return best;
    }

    // Погоня

    private void chase(Herbivore target) {

        int dx = target.getX() - getX();
        int dy = target.getY() - getY();

        int distance = Math.abs(dx) + Math.abs(dy);

        /*
         * Цель находится в соседней клетке.
         * Хищник атакует и съедает её.
         */
        if (distance == 1) {

            env.moveIntoPrey(this, target, MOVE_COST, MEAT_ENERGY);

            return;
        }

        /*
         * Если цель не в соседней клетке,
         * выбираем лучший одиночный шаг.
         */
        moveOneStepTowards(target.getX(), target.getY());
    }

    /**
     * Выбор лучшего следующего шага к цели.
     * <p>
     * Разрешены только:
     * вверх, вниз, влево, вправо.
     * <p>
     * Выход за пределы поля невозможен.
     * <p>
     * При выборе клетки приоритет отдаётся
     * той, которая уменьшает расстояние до цели.
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
             * Занятая клетка является препятствием.
             */
            if (env.getAgent(newX, newY) != null) {
                continue;
            }

            int distance = env.getManhattanDistance(newX, newY, targetX, targetY);

            if (distance < bestDistance) {

                bestDistance = distance;

                bestX = newX;
                bestY = newY;
            }
        }

        if (bestX >= 0) {

            env.moveAgent(this, bestX, bestY, MOVE_COST);

        } else {

            /*
             * Прямой путь перекрыт.
             * В таком случае делаем обычный случайный шаг.
             */
            wander();
        }
    }

    // Последняя известная позиция

    private void moveToLastKnownPosition() {

        if (lastKnownTargetX < 0 || lastKnownTargetY < 0) {
            wander();
            return;
        }

        int distance = env.getManhattanDistance(getX(), getY(), lastKnownTargetX, lastKnownTargetY);

        /*
         * Хищник уже находится в последней известной
         * позиции цели.
         */
        if (distance == 0) {
            return;
        }

        /*
         * Важный момент:
         * движение тоже только на одну клетку.
         */
        moveOneStepTowards(lastKnownTargetX, lastKnownTargetY);
    }

    // Случайное движение

    private void wander() {

        final int[] dirX = {0, 0, -1, 1};
        final int[] dirY = {-1, 1, 0, 0};

        /*
         * Начинаем со случайного направления,
         * чтобы блуждание не было одинаковым.
         */
        int start = env.getRandom().nextInt(4);

        for (int i = 0; i < 4; i++) {

            int index = (start + i) % 4;

            int newX = getX() + dirX[index];
            int newY = getY() + dirY[index];

            /*
             * За пределы поля выходить нельзя.
             */
            if (!env.isInside(newX, newY)) {
                continue;
            }

            /*
             * В занятую клетку зайти нельзя.
             */
            if (env.getAgent(newX, newY) == null) {

                env.moveAgent(this, newX, newY, MOVE_COST);

                return;
            }
        }
    }

    // Рождение

    @Override
    protected Agent createOffspring(int x, int y) {
        return new Predator(env, x, y);
    }
}